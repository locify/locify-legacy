/*
 * TileCache.java
 * This file is part of Locify.
 *
 * Locify is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * (read more at: http://www.gnu.org/licenses/gpl.html)
 *
 * Commercial licenses are also available, please
 * refer http://code.google.com/p/locify/ for details.
 */
package com.locify.client.maps;

import com.locify.client.data.FileSystem;
import com.locify.client.gui.polish.TopBarBackground;
import com.locify.client.gui.screen.internal.MapScreen;
import java.io.IOException;
import com.locify.client.utils.*;
import de.enough.polish.util.HashMap;
import de.enough.polish.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

/**
 * Manages caching downloaded tiles
 * @author Menion
 */
public class TileCache extends Thread {

    private int maxSize;
    
    private HashMap runningHttpDownloaders;
    /** if new requestsAdded started */
    private boolean cleanRequest;
    /** size of tiles X */
    private int tileSizeX;
    /** size of tiles Y */
    private int tileSizeY;
    /** time last runningHttpDownloaders was check */
    private long lastRunRemover;
    /** time last mapScreen refresh was initialized */
    private long lastRunRefresh;
    private boolean needRefresh;
    /** timeout for httpImage loading */
    private long timeOut;
    /** variable indikates that this refresh was done due to calling from FileTileCache */
    private boolean localRefreshCalling;
    /** file for logging */
    private String logName = FileSystem.LOG_FOLDER + "FileTileCache.log";
    /** request line */
    private Vector tileCache;
    private Vector tileRequest;
    private Vector tileNewRequest;
    
    /**
     * Create file cache for images.
     * @param cacheMaxSize
     * @param imageBasePath - absolute path to files directory
     * @param tileSize
     */
    public TileCache(int tileSizeX, int tileSizeY) {
        // set size of cache ... 1MB
        this.maxSize = (int) ((0.33 * 1048576.0) / (tileSizeX * tileSizeY));
        if (maxSize < 4)
            maxSize = 4;
        this.tileSizeX = tileSizeX;
        this.tileSizeY = tileSizeY;
        
        this.tileRequest = new Vector();
        this.tileNewRequest = new Vector();
        this.tileCache = new Vector();
        
        this.cleanRequest = false;
        this.runningHttpDownloaders = new HashMap();
        this.lastRunRemover = System.currentTimeMillis();
        this.lastRunRefresh = lastRunRemover;
        this.needRefresh = false;
        this.localRefreshCalling = false;
        this.timeOut = 60000;
    }

    public void newRequest(Vector newRequest) {
//System.out.println("New request size: " + newRequest.size() + " localRefresh: " + localRefreshCalling);
        if (!localRefreshCalling) {
            tileNewRequest = newRequest;
            cleanRequest = true;
        }
        localRefreshCalling = false;
    }

    public Image getImage(String fileName) {
//System.out.println("\ngetTile: " + fileName);
        Image img = getImageFromCache(fileName);

        if (img != null) {
            return img;
        } else {
            return MapScreen.getImageLoading(tileSizeX, tileSizeY);
        }
    }

    private void setAllCachedAsNotRequired() {
        for (int i = 0; i < tileCache.size(); i++) {
            ((ImageRequest) tileCache.elementAt(i)).requiredTile = false;
        }
    }

    private Image getImageFromCache(String imageName) {
        ImageRequest actualR;
        for (int i = 0; i < tileCache.size(); i++) {
            actualR = (ImageRequest) tileCache.elementAt(i);
            if (actualR.fileName.equals(imageName)) {
                actualR.requiredTile = true;
                return actualR.image;
            }
        }
        return null;
    }
    
    private boolean existInRequest(String imageName) {
        for (int i = 0; i < tileRequest.size(); i++) {
            if (((ImageRequest) tileRequest.elementAt(i)).fileName.equals(imageName))
                return true;
        }
        return false;
    }

    private void addImageToCache(ImageRequest request) {
        tileCache.addElement(request);
        tileRequest.removeElement(request);
    }

    public void run() {
        ImageRequest actualRequest;
        while (true) {
//System.out.println("A. request size: " + tileRequest.size());
            while (!tileRequest.isEmpty()) {
                manageRequests();
                if (tileRequest.isEmpty())
                    break;
//System.out.println("Actual request size: " + tileRequest.size());
                try {
                    actualRequest = (ImageRequest) tileRequest.elementAt(0);
                    if (actualRequest.fileName.startsWith("http://")) {
                        TopBarBackground.setHttpStatus(TopBarBackground.RECEIVING);
//long tic = System.currentTimeMillis();
//System.out.println((System.currentTimeMillis() - tic) + " start");
                        

                        /* check if same ImageRequest is not already downloading */
                        HttpImageDownloader hid = (HttpImageDownloader) runningHttpDownloaders.get(actualRequest.fileName);
                        if (hid != null) {
                            if (hid.imageLoaded) {
                                if (hid.image == null) {
                                    tileRequest.insertElementAt(actualRequest, tileRequest.size());
                                    tileRequest.removeElementAt(0);
                                } else {
                                    actualRequest.image = hid.image;
                                    addImageToCache(actualRequest);
                                }
                                runningHttpDownloaders.remove(actualRequest.fileName);
                            } else if (System.currentTimeMillis() - hid.startTime > timeOut) {
                                actualRequest.image = MapScreen.getImageNotExisted(tileSizeX, tileSizeY);
                                addImageToCache(actualRequest);
                                runningHttpDownloaders.remove(actualRequest.fileName);
                            } else {
                                tileRequest.insertElementAt(actualRequest, tileRequest.size());
                                tileRequest.removeElementAt(0);
                            }
                        } else {
                            if (runningHttpDownloaders.size() < 2) {
                                HttpImageDownloader http = new HttpImageDownloader(actualRequest.fileName);
                                http.setPriority(Thread.MIN_PRIORITY);
                                http.start();
                                runningHttpDownloaders.put(actualRequest.fileName, http);
                            }
                            tileRequest.insertElementAt(actualRequest, tileRequest.size());
                            tileRequest.removeElementAt(0);
                        }
//System.out.println((System.currentTimeMillis() - tic) + " end");
                    } else if (actualRequest.fileName.startsWith("file:///") &&
                            actualRequest.tarName == null) {
//System.out.println("F: name - " + actualRequest.fileName);
                        actualRequest.image = makeImageFileRequest(actualRequest.fileName);
                        addImageToCache(actualRequest);
                        if (tileRequest.isEmpty() && R.getMapScreen().isOffLineMapEnable()) {
                            R.getMapScreen().repaint();
                        }

                    } else if (actualRequest.tarName != null) {
long time = System.currentTimeMillis();
Logger.debug("!!! Memory before - (free/total) " + Runtime.getRuntime().freeMemory() + "/" + Runtime.getRuntime().totalMemory());
Logger.debug("TileCache.run() tileCache.size(): " + tileCache.size() + " maxSize: " + maxSize);
Logger.debug("TileCache TAR: name - " + actualRequest.tarName + " pos: " + actualRequest.byteFromPosition);
                        byte[] array = StorageTar.loadFile(actualRequest.tarName, actualRequest.byteFromPosition);
                        if (array == null || array.length == 0) {
                            actualRequest.image = MapScreen.getImageConnectionNotFound(tileSizeX, tileSizeY);
                        } else {
                            try {
                                actualRequest.image = Image.createImage(array, 0, array.length);
                            } catch (OutOfMemoryError e) {
                                Logger.error("TileCache.run() outOfMemoryError: " + e.toString());
                            } catch (Exception e) {
                                Logger.error("TileCache.run() Error: " + e.toString());
                            } finally {
                                if (actualRequest.image == null)
                                    actualRequest.image = MapScreen.getImageConnectionNotFound(tileSizeX, tileSizeY);
                            }
                        }

                        addImageToCache(actualRequest);
                        if (tileRequest.isEmpty() && R.getMapScreen().isOffLineMapEnable()) {
                            R.getMapScreen().repaint();
                        }
Logger.debug("TileCache TAR: end after " + (System.currentTimeMillis() - time) + "ms");
Logger.debug("Memory after - (free/total) " + Runtime.getRuntime().freeMemory() + "/" + Runtime.getRuntime().totalMemory());
                    }

                    refreshScreenAndDownloaders();

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        R.getErrorScreen().view(e, "FileTileCache.run() - sleep(250)", null);
                    }
                } catch (Exception e) {
                    R.getErrorScreen().view(e, "FileTileCache.run() - while (!requests.isEmpty())", null);
                }
                needRefresh = true;
            }

//System.out.println("End request");
            manageRequests();
            refreshScreenAndDownloaders();
            TopBarBackground.setHttpStatus(TopBarBackground.UNDEFINED);
                
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                R.getErrorScreen().view(e, "FileTileCache.run(2) - sleep(250)", null);
            }
        }
    }

    private void manageRequests() {
        if (cleanRequest) {
//System.out.println("Switch tileR.size(): " + tileRequest.size() + " tileNewR.size(): " + tileNewRequest.size());
            cleanRequest = false;
            tileRequest = new Vector();

            setAllCachedAsNotRequired();

            ImageRequest actualR;
            for (int i = 0; i < tileNewRequest.size(); i++) {
                actualR = (ImageRequest) tileNewRequest.elementAt(i);
//Logger.debug("New request: " + actualR.fileName);
                if (getImageFromCache(actualR.fileName) == null)
                    tileRequest.addElement(actualR);
            }
            tileNewRequest = null;

            // manage running httpDownloaders
            Iterator iter = runningHttpDownloaders.keysIterator();
            while (iter.hasNext()) {
                HttpImageDownloader hid = (HttpImageDownloader) runningHttpDownloaders.get((String) iter.next());
                if (!existInRequest(hid.path)) {
                    iter.remove();
                }
            }

            // clean cache
            if (tileCache.size() > maxSize) {
Logger.debug("\nTileCache: clear cache tileCache.size(): " + tileCache.size() + " maxSize: " + maxSize);
                for (int i = tileCache.size() - 1; i >=0; i--) {
                    actualR = (ImageRequest) tileCache.elementAt(i);
                    if (!actualR.requiredTile) {
Logger.debug("\n  remove: " + actualR.fileName);
                        tileCache.removeElementAt(i);
                        if (tileCache.size() < ((3 * maxSize) / 5))
                            return;
                    }
                }
            }
            if (tileCache.size() > maxSize) {
Logger.debug("\nTileCache: clear cache FORCE tileCache.size(): " + tileCache.size() + " maxSize: " + maxSize);
                for (int i = tileCache.size() - 1; i >=0; i--) {
                    actualR = (ImageRequest) tileCache.elementAt(i);
Logger.debug("\n  remove: " + actualR.fileName);
                    tileCache.removeElementAt(i);
                    if (tileCache.size() < Math.floor((3 * maxSize) / 5))
                        return;
                }
            }
        }
    }

    private Image makeImageFileRequest(String url) {
        Image image = MapScreen.getImageNotExisted(tileSizeX, tileSizeY);
        FileConnection con = null;
        try {
            con = (FileConnection) Connector.open(url);
            if (con.exists()) {
                image = Image.createImage(con.openInputStream());
            }
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "FileTileCache.makeImageFileRequest()", url);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    private void refreshScreenAndDownloaders() {
        try {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastRunRefresh > 1000 && needRefresh) {
                R.getMapScreen().repaint();
                localRefreshCalling = true;
                lastRunRefresh = currentTime;
                needRefresh = false;
//System.out.println("Refresh");
            }

            if (currentTime - lastRunRemover > timeOut) {
                Iterator iter = runningHttpDownloaders.keysIterator();
                while (iter.hasNext()) {
                    HttpImageDownloader hid = (HttpImageDownloader) runningHttpDownloaders.get((String) iter.next());
                    if (currentTime - hid.startTime > 2 * timeOut)
                        iter.remove();
                }
                lastRunRemover = currentTime;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileTileCache.refreshScreenAndDownloaders()", null);
        }
    }

    public int getTileSizeX() {
        return tileSizeX;
    }

    public int getTileSizeY() {
        return tileSizeY;
    }

    private class HttpImageDownloader extends Thread {

        private Image image;
        private boolean imageLoaded;
        private String path;
        private long startTime;

        private HttpConnection connection;
        private DataInputStream dis;
        private ByteArrayOutputStream baos;
        private DataOutputStream dos;

        public HttpImageDownloader(String path) {
            this.imageLoaded = false;
            this.path = path;
            this.startTime = System.currentTimeMillis();
        }

        public void run() {
            try {
                connection = (HttpConnection) Connector.open(path, Connector.READ);
                connection.setRequestMethod(HttpConnection.GET);
                connection.setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.1");
                connection.setRequestProperty("Accept", "image/png");
                connection.setRequestProperty("Connection", "close");

                //receiving data
                if (connection.getResponseCode() == HttpConnection.HTTP_OK) {
                    dis = connection.openDataInputStream();
                    byte[] data;

                    int length = (int) connection.getLength();
                    if (length != -1) {
                        if (length > 300000) {
                            this.image = MapScreen.getImageTooBigSize(tileSizeX, tileSizeY);
                            return;
                        }
                        data = new byte[length];
                        dis.readFully(data);
                    } else {
                        baos = new ByteArrayOutputStream();
                        dos = new DataOutputStream(baos);
                        int onebyte;
                        while ((onebyte = dis.read()) != -1) {
                            dos.write(onebyte);
                        }
                        data = baos.toByteArray();

                        dos.close();
                        baos.close();
                    }

                    this.image = Image.createImage(data, 0, data.length);
                } else {
                    Logger.error("Error while downloading map tile: " + connection.getResponseCode());
                }
            } catch (IOException e) {
                Logger.error("TileCache.HttpImageDownloader() - imageDownloadError: " + e.toString() + " tile: " + path);
                if (e.getMessage().equals("HTTP Response Code = 404")) {
                    this.image = MapScreen.getImageNotExisted(tileSizeX, tileSizeY);
                } else {
                    this.image = null;
                }
            } finally {
                this.imageLoaded = true;
                try {
                    if (dos != null) {
                        dos.close();
                        dos = null;
                    }
                    if (baos != null) {
                        baos.close();
                        baos = null;
                    }
                    if (dis != null) {
                        dis.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (IOException e) {
                    R.getErrorScreen().view(e, "HttpImageDownloader.run() - finally", path);
                }
            }
        }
    }
}