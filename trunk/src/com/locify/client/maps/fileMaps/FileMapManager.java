/*
 * FileMapManager.java
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
package com.locify.client.maps.fileMaps;

import com.locify.client.data.FileSystem;
import com.locify.client.maps.ImageRequest;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.net.Http;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StorageTar;
import com.locify.client.utils.StorageTar.TarRecord;
import com.locify.client.utils.Utils;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Manages loading of offline maps
 * @author MenKat
 */
public abstract class FileMapManager {

    public static final int MAP_TYPE_SINGLE_TILE = 1;
    public static final int MAP_TYPE_MULTI_TILE = 2;
    public static final int MAP_TYPE_MULTI_TILE_LOCAL_TAR = 3;

    public static final int CATEGORY_XML = 10;
    public static final int CATEGORY_MAP = 11;

    /** storage for tar */
    protected StorageTar storageTar;
    /** if mapmanager is succesfully initialized */
    private boolean ready;
    
    /** config file with all important map parametres */
    protected FileMapConfig fileMapConfig;

    /** map config filename */
    protected String mapFilename;
    /** dir for images relative to mappath */
    protected String mapImageDir;
    /** path to map */
    protected String mapPath;

    
    ///////////////////////////////////////////
    //            TEMP VARIABLES             //
    ///////////////////////////////////////////
    /** StringBuffer for many circumstances */
    private static StringBuffer stringBuffer = new StringBuffer();
    /** size of screen */
    private int screenWidth = 0,  screenHeight = 0;
    /** starting number of tile X-line */
    private int tileX1;
    /** starting number of tile Y-line */
    private int tileY1;
    /** ending number of tile X-line */
    private int tileX2;
    /** ending number of tile Y-line */
    private int tileY2;
    /** num of files able to draw on screen X-line */
    private int numOfTilesXperScreen;
    /** num of files able to draw on screen Y-line */
    private int numOfTilesYperScreen;
    /** num of files of whole map X-line */
    private int numOfTotalTilesX;
    /** num of files of whole map Y-line */
    private int numOfTotalTilesY;
    /** move result in X */
    private int moveX;
    /** move result in Y */
    private int moveY;
    /** loaded image used for drawing*/
    private Image image;
    /** data for Http transfer */
    private static String obtainedData;

    public FileMapManager(String mapPath) {
        this.ready = false;

        if ((mapPath.startsWith("http://") && mapPath.endsWith(".xml")) ||
                (mapPath.startsWith("http://") && mapPath.endsWith(".map"))) {
            this.mapFilename = mapPath.substring(mapPath.lastIndexOf('/') + 1);
            this.mapImageDir = this.mapFilename.substring(0, this.mapFilename.lastIndexOf('.')) + "/";
            this.mapPath = mapPath.substring(0, (mapPath.length() - this.mapFilename.length()));

        } else if (mapPath.endsWith(".xml") || mapPath.endsWith(".map") || mapPath.endsWith(".tar")) {
            this.mapFilename = mapPath;
            if (!mapPath.endsWith(".tar")) {
                this.mapImageDir = this.mapFilename.substring(0, this.mapFilename.lastIndexOf('.')) + "/";
                if (!R.getFileSystem().exists(FileSystem.MAP_FOLDER + mapImageDir)) {
                    Logger.error("FileMapManager.constructor() - cannot find image directory in " + mapPath);
                    ready = false;
                    return;
                }
            } else {
                this.mapImageDir = null;
            }
            this.mapPath = "file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER;
        } else {
            Logger.error("FileMapManager.constructor() - unknown map type: " + mapPath);
            ready = false;
            return;
        }

        ready = true;
//Logger.debug("FileMapManager.constructor() - new Map mapFileName: " + this.mapFilename +
//        " mapImageDir: " + this.mapImageDir + " mapPath: " + this.mapPath);
    }

    public String getMapName() {
        return mapFilename;
    }

    public boolean isReady() {
//Logger.debug("    FileMapManager.isReady() ready: " + ready + " fileMapConfig.isReady() " +
//        fileMapConfig.isReady() + " mapImageDir: " + mapImageDir);
        return ready && fileMapConfig != null && fileMapConfig.isReady() && mapImageDir != null;
    }

    /**
     * Main function that check what king of map type is taht file.
     * @param mapPath Path to file, if started with <i>HTTP</i>,
     * its absolute web, otherwise it's relative within MAP directory.
     * @return Array of two values.
     * <ul><li>1. Type of map (wrong, single, multi, multiTar).
     * <li>2. Map category (xml, map).</ul>
     */
    public static FileMapManager getMapType(String mapPath) {
        FileMapManager fmm = null;

        /* HTTP maps */
        if ((mapPath.startsWith("http://") && mapPath.endsWith(".xml")) ||
            (mapPath.startsWith("http://") && mapPath.endsWith(".map"))) {
            try {
                obtainedData = "";
                Http http = R.getHttp();
                http.start(mapPath);
                long actualTime = System.currentTimeMillis();
                while (obtainedData.equals("") && (System.currentTimeMillis() - actualTime) < 10000) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                if (obtainedData.equals("")) {
                    return null;
                } else {
                    if (mapPath.endsWith(".xml")) {
                        fmm = new FileMapManagerMulti(mapPath);
                        fmm.setFileMapConfig(FileMapConfig.parseLocifyDescriptor(obtainedData));
                    } else if (mapPath.endsWith(".map")) {
                        fmm = new FileMapManagerMulti(mapPath);
                        fmm.setFileMapConfig(FileMapConfig.parseDotMapDescriptor(obtainedData));
                    } else {
                        return null;
                    }
                    return fmm;
                }
            } catch (Exception ex) {
                R.getErrorScreen().view(ex, "FileMapManager.getMapType() (http)", mapPath);
            }
        /* LOCAL maps (single or multi) */
        } else if (mapPath.endsWith(".xml") || mapPath.endsWith(".map")) {
            String mapPathAbsolute = "file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + mapPath;
            try {
                FileConnection dir;
                if (R.getFileSystem().exists(mapPathAbsolute)) {
                    String ffDirName = mapPathAbsolute.substring(0, mapPathAbsolute.lastIndexOf('.'));
                    String ffImageName = ffDirName + ".png";
                    String data = R.getFileSystem().loadString(FileSystem.MAP_FOLDER + mapPath);

                    dir = (FileConnection) Connector.open(ffDirName);                    
                    if (dir.exists() && dir.isDirectory()) {
                        if (mapPath.endsWith(".xml")) {
                            fmm = new FileMapManagerMulti(mapPath);
                            fmm.setFileMapConfig(FileMapConfig.parseLocifyDescriptor(data));
                        } else if (mapPath.endsWith(".map")) {
                            fmm = new FileMapManagerMulti(mapPath);
                            fmm.setFileMapConfig(FileMapConfig.parseDotMapDescriptor(data));
                        }
                        dir.close();
                        return fmm;
                    }

                    dir = (FileConnection) Connector.open(ffImageName);
                    if (dir.exists() && !dir.isDirectory()) {
                        if (mapPath.endsWith(".xml")) {
                            fmm = new FileMapManagerSingle(mapPath);
                            fmm.setFileMapConfig(FileMapConfig.parseLocifyDescriptor(data));
                        } else if (mapPath.endsWith(".map")) {
                            fmm = new FileMapManagerSingle(mapPath);
                            fmm.setFileMapConfig(FileMapConfig.parseDotMapDescriptor(data));
                        }
                        
                        dir.close();
                        return fmm;
                    }
                }
            } catch (IOException ex) {
                R.getErrorScreen().view(ex, "FileMapManager.getMapType() (map + xml)", mapPath);
            }
        /* LOCAL maps (tar) */
        } else if (mapPath.endsWith(".tar")) {
            try {
                String mapPathAbsolute = "file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + mapPath;
//Logger.debug("  FileMapManager.getMapType() - " + mapPathAbsolute);
                FileConnection connection = (FileConnection) Connector.open(mapPathAbsolute);
                if (connection.exists() && !connection.isDirectory()) {
                    StorageTar storageTar = new StorageTar(mapPathAbsolute);
                    TarRecord record = storageTar.getConfigFile();
                    String data;
                    if (storageTar.getConfigFileType() == FileMapManager.CATEGORY_XML && record != null) {
                        data = new String(storageTar.loadFile(record));
                        if (data != null) {
                            fmm = new FileMapManagerTar(mapPath);
                            fmm.setStorageTar(storageTar);
                            fmm.setFileMapConfig(FileMapConfig.parseLocifyDescriptor(data));
                        }
                    } else if (storageTar.getConfigFileType() == FileMapManager.CATEGORY_MAP && record != null) {
                        data = new String(storageTar.loadFile(record));
                        if (data != null) {
                            fmm = new FileMapManagerTar(mapPath);
                            fmm.setStorageTar(storageTar);
                            fmm.setFileMapConfig(FileMapConfig.parseDotMapDescriptor(data));
                        }
                    } else {
                        Logger.error("FileMapManager.getMapType() - unsupported map tar type");
                        return null;
                    }
                    connection.close();
                    return fmm;
                }
            } catch (IOException ex) {
                R.getErrorScreen().view(ex, "FileMapManager.getMapType() (tar)", mapPath);
            }
        }
        return null;
    }

    public boolean setFileMapConfig(FileMapConfig fileMapConfig) {
        if (fileMapConfig != null) {
            this.fileMapConfig = fileMapConfig;
            if (!fileMapConfig.isReady())
                calculateTileSize();

            return true;
        } else {
            return false;
        }
    }

    public void setStorageTar(StorageTar tar) {
        this.storageTar = tar;
        this.mapImageDir = storageTar.getImageDir();
    }

    private void calculateTileSize() {
        Image img = null;
        if (storageTar != null) {
            byte[] data = storageTar.loadFile(storageTar.getTarRecord(0));
            try {
                img = Image.createImage(data, 0, data.length);
            } catch (Exception e) {
                Logger.error("FileMapManager.calculateTileSize() " + e.toString());
            }
        } else {
            FileConnection con = null;
            try {
                con = (FileConnection) Connector.open(mapPath + createImageName(0, 0, null));
//Logger.log("File path: " + (manager.mapPathPrefix + manager.mapPath + createImageName(0, 0)));
                if (con.exists()) {
                    img = Image.createImage(con.openInputStream());
                }
            } catch (Exception ex) {
                R.getErrorScreen().view(ex, "ConfigFileTile.getMapTileSizeDir()", null);
            } finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (img != null) {
            fileMapConfig.tileSizeX = img.getWidth();
            fileMapConfig.tileSizeY = img.getHeight();
            img = null;
        } else {
            Logger.error("FileMapManager.calculateTileSize() - problem");
        }
    }

    private String createImageName(int i, int j, StorageTar tar) {
        if (stringBuffer.length() > 0)
            stringBuffer.delete(0, stringBuffer.length());
        stringBuffer.append(mapImageDir);
        if (tar != null) {
            stringBuffer.append(i);
            stringBuffer.append("_");
            stringBuffer.append(j);
        } else {
            stringBuffer.append(Utils.addZerosBefore("" + i, 3) + "_" + Utils.addZerosBefore("" + j, 3));
        }
        return stringBuffer.toString();
    }

    public synchronized boolean drawImageSingle(Graphics gr, FileMapViewPort targetPort) {
        try {
            Point2D mapPoint = fileMapConfig.getMapViewPort().convertGeoToMapPixel(targetPort.getCenter());
            int x1, x2, y1, y2;
            if (fileMapConfig.getMapViewPort().containsPosition(targetPort.getCenter())) {
                x1 = Math.max(0, (int) (mapPoint.getX() - targetPort.getXmax() / 2));
                y1 = Math.max(0, (int) (mapPoint.getY() - targetPort.getYmax() / 2));
                x2 = Math.min(image.getWidth(), (int) (mapPoint.getX() + targetPort.getXmax() / 2));
                y2 = Math.min(image.getHeight(), (int) (mapPoint.getY() + targetPort.getYmax() / 2));

                drawImageSingle(image, x1, x2, y1, y2, gr, targetPort);
                return true;
            }
            return false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileTile.draw()", null);
            return false;
        }
    }
    
    private boolean drawImageSingle(Image image, int x1, int x2, int y1, int y2, Graphics gr, FileMapViewPort targetPort) {
        int width = x2 - x1;
        int height = y2 - y1;
        int to_x = 0, to_y = 0;

        try {
            if (width == targetPort.getXmax()) {
                to_x = 0;
            } else {
                if (x1 == 0) {
                    to_x = targetPort.getXmax() - width;
                } else {
                    to_x = 0;
                }
            }

            if (height == targetPort.getYmax()) {
                to_y = 0;
            } else {
                if (y1 == 0) {
                    to_y = targetPort.getYmax() - height;
                } else {
                    to_y = 0;
                }
            }

            gr.drawRegion(
                    //source
                    image,
                    x1, y1, width, height,
                    Sprite.TRANS_NONE,
                    //target
                    to_x, to_y, Graphics.LEFT | Graphics.TOP);

            return true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileTile.drawImageSingle()", " draw:" +
                    " image null: " + (image == null) +
                    " orig img.width: " + image.getWidth() +
                    " orig img.height: " + image.getHeight() +
                    "  x1: " + x1 +
                    "  y1: " + y1 +
                    "  x2: " + x2 +
                    "  y2: " + y2 +
                    "  to_X: " + to_x +
                    "  to_y: " + to_y);
            return false;
        }
    }

    public synchronized void appendRequests(Vector imageExist, Vector imageNotExist,
            FileMapViewPort targetPort, StorageTar tar, int mapPanX, int mapPanY) {
        try {
            Point2D mapPoint = fileMapConfig.getMapViewPort().convertGeoToMapPixel(targetPort.getCenter());
//Logger.log(" FileMapManager.appendRequests() - " + mapPath + mapImageDir);
//Logger.log("   mapViewPort " + fileMapConfig.getMapViewPort().toString());
//Logger.log("   targetPort " + targetPort.toString());
//Logger.log("   center: " + targetPort.getCenter().toString());

            if (screenWidth == 0) {
                screenWidth = R.getMapScreen().getWidth();
                screenHeight = R.getMapScreen().getHeight();
                numOfTotalTilesX = (int) (fileMapConfig.xmax / fileMapConfig.tileSizeX);
                if (fileMapConfig.xmax % fileMapConfig.tileSizeX != 0)
                    numOfTotalTilesX++;
                numOfTotalTilesY = (int) (fileMapConfig.ymax / fileMapConfig.tileSizeY);
                if (fileMapConfig.ymax % fileMapConfig.tileSizeY != 0)
                    numOfTotalTilesY++;
            }
            // top left map point (in map pixel coordinates)
            Point2D mapPointTL = new Point2D.Double(mapPoint.getX() - screenWidth / 2,
                    mapPoint.getY() - screenHeight / 2);
            // bottom right map point (in map pixel coordinates)
            Point2D mapPointBR = new Point2D.Double(mapPoint.getX() + screenWidth / 2,
                    mapPoint.getY() + screenHeight / 2);

            tileX1 = (int) Math.floor(mapPointTL.getX() / fileMapConfig.tileSizeX);
            tileY1 = (int) Math.floor(mapPointTL.getY() / fileMapConfig.tileSizeY);
            tileX2 = (int) Math.floor(mapPointBR.getX() / fileMapConfig.tileSizeX);
            tileY2 = (int) Math.floor(mapPointBR.getY() / fileMapConfig.tileSizeY);
            numOfTilesXperScreen = tileX2 - tileX1 + 1;
            numOfTilesYperScreen = tileY2 - tileY1 + 1;
            moveX = (int) ((tileX1 * fileMapConfig.tileSizeX) - mapPointTL.getX());
            moveY = (int) ((tileY1 * fileMapConfig.tileSizeY) - mapPointTL.getY());
//Logger.log("   " + numOfTotalTilesX + " " + numOfTotalTilesY);
//Logger.log("   " + fileMapConfig.xmax + " " + fileMapConfig.ymax);
//Logger.log("   " + fileMapConfig.tileSizeX + " " + fileMapConfig.tileSizeY);
//Logger.log("   " + mapPointTL.getX() + " " + fileMapConfig.tileSizeX + " " + tileX1);
//Logger.log("   " + mapPointTL.getY() + " " + fileMapConfig.tileSizeY + " " + tileY1);
//Logger.log("   " + mapPointBR.getX() + " " + fileMapConfig.tileSizeX + " " + tileX2);
//Logger.log("   " + mapPointBR.getY() + " " + fileMapConfig.tileSizeY + " " + tileY2);
//Logger.log("   drawImageMulti - moveX: " + moveX + " moveY: " + moveY);
//Logger.log("   drawImageMulti - centerMapPointX: " + mapPoint.getX() + " mapPointY: " + mapPoint.getY());
//Logger.log("   drawImageMulti - tileX1: " + tileX1 + " tileY1: " + tileY1);
//Logger.log("   drawImageMulti - tileX2: " + tileX2 + " tileY2: " + tileY2);

            String imageName = null;

            ImageRequest ir;
            int x1;
            int y1;
            for (int i = 0; i < numOfTilesXperScreen; i++) {
                for (int j = 0; j < numOfTilesYperScreen; j++) {
                    x1 = moveX + mapPanX + i * fileMapConfig.tileSizeX;
                    y1 = moveY + mapPanY + j * fileMapConfig.tileSizeY;

                    for (int k = imageNotExist.size() - 1; k >= 0; k--) {
                        ir = (ImageRequest) imageNotExist.elementAt(k);
                        if (Math.abs(ir.getX() - x1) < 2 && Math.abs(ir.getY() - y1) < 2) {
//Logger.log("  FileMapManager.appendRequests() - remove: x: " + ir.getX() + ", y: " + ir.getY());
                            imageNotExist.removeElementAt(k);
                            break;
                        }
                    }

                    if (imageHaveToExist(tileX1 + i, tileY1 + j)) {
                        imageName =  createImageName(tileX1 + i, tileY1 + j, tar);
                        if (tar != null) {
                            ir = new ImageRequest(mapPath + imageName,
                                    tar, tar.getTarRecord(numOfTotalTilesY * (tileX1 + i) + (tileY1 + j)),
                                    fileMapConfig.tileSizeX, fileMapConfig.tileSizeY, x1, y1);
                        } else {
                            
                            ir = new ImageRequest(mapPath + imageName, fileMapConfig.tileSizeX,
                                    fileMapConfig.tileSizeY, x1, y1);
                        }
//Logger.log("  FileMapManager.appendRequests() - ImageExist: " + ir.toString());
                        imageExist.addElement(ir);
                    } else {
                        ir = new ImageRequest(null, fileMapConfig.tileSizeX,
                                fileMapConfig.tileSizeY, x1, y1);
//Logger.log("  FileMapManager.appendRequests() - ImageNotExist: " + ir.toString());
                        imageNotExist.addElement(ir);
                    }
                }
            }
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "FileMapManager.appendRequest()", mapPath + mapImageDir);
        }
    }

    private boolean imageHaveToExist(int tileX, int tileY) {
//Logger.log("    ConfigFileTile.imageHaveToExist() - getTile: tX: " + tileX + " tY: " + tileY);
        return (tileX >= 0 && tileY >= 0 && tileX < numOfTotalTilesX && tileY < numOfTotalTilesY);
    }

    public FileMapConfig getFileMapConfig() {
        return fileMapConfig;
    }

    public static void setObtainedData(String data) {
        FileMapManager.obtainedData = data;
    }

    public abstract boolean drawActualMap(Graphics gr, FileMapViewPort viewPort,
            Vector imageExist, Vector imageNotExist, int mapPanX, int mapPanY);
}