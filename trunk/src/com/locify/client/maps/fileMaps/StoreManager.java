/*
 * RmsManager.java
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
import com.locify.client.gui.screen.internal.MapOfflineChooseScreen;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.FileMapLayer;
import com.locify.client.utils.*;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Gauge;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

/**
 *
 * @author menion
 */
public class StoreManager {

    private static final String MAP_FILE = "_mapFileStore";
    private static byte[] tempBuffer;
    private static boolean STOP;

    private static byte[] areaData;
    public static Form form;

    public StoreManager() {
    }

    /**
     *
     * @param mocs
     */
    public static void initializeOfflineMaps(final MapOfflineChooseScreen mocs) {
        try {
            form = new Form(Locale.get("Searching"));
            form.setCommandListener(new CommandListener() {
                public void commandAction(Command c, Displayable d) {
                    if (c.equals(Commands.cmdCancel)) {
                        STOP = true;
                    } else if (c.equals(Commands.cmdOK)) {
                        form.deleteAll();
                        mocs.view(mocs.lastLat1, mocs.lastLon1, mocs.lastLat2, mocs.lastLon2);
                    }
                 }
            });

            form.addCommand(Commands.cmdCancel);
            R.getMidlet().switchDisplayable(null, form);
            STOP = false;

            Thread thread = new Thread(new Runnable() {
                public void run() {
long time = System.currentTimeMillis();
//Logger.debug("StoreManager.initializeOfflineMaps() - started");

                    Enumeration f1 = null;
                    Enumeration f2 = null;
                    try{
                        f1 = R.getFileSystem().listFiles(FileSystem.MAP_FOLDER, null);
                        f2 = R.getFileSystem().listFiles(FileSystem.CACHE_MAP_FOLDER, null);
                    } catch (Exception ex) {
                        Logger.error("StoreManager.initializeOfflineMaps() error01: " + ex.toString());
                    } catch (OutOfMemoryError ex) {
                        Logger.error("StoreManager.initializeOfflineMaps() error02: " + ex.toString());
                    }

                    Vector files = new Vector();
                    if (f1 != null) {
                        while (f1.hasMoreElements()) {
                            files.addElement(f1.nextElement());
                        }
                    }
                    f1 = null;

                    Vector filesCached = new Vector();
                    if (f2 != null) {
                        while (f2.hasMoreElements()) {
                            filesCached.addElement(f2.nextElement());
                        }
                    }
                    f2 = null;
//Logger.debug("  step1 (" + (System.currentTimeMillis() - time) + "ms)");

                    // delete old config file
                    R.getFileSystem().delete(FileSystem.CACHE_MAP_FOLDER + MAP_FILE);

                    // create buffer for output file
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);

                    Gauge gauge = new Gauge(Locale.get("Initializing"), true, files.size(), 0);
                    form.insert(0, gauge);

                    if (files.size() > 0) {
                        String fileName;
                        String temp;
                        for (int k = 0; k < files.size(); k++) {
                            if (STOP)
                                break;
                            fileName = (String) files.elementAt(k);
                            temp = fileName.substring(fileName.lastIndexOf('.') + 1);
                            tempBuffer = null;
//Logger.debug("  checking file: " + fileName + " (" + (System.currentTimeMillis() - time) + "ms)");
                            if (temp.equalsIgnoreCase("tar") || temp.equalsIgnoreCase("xml") || temp.equalsIgnoreCase("map")) {
                                temp = FileSystem.hashFileName("file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + fileName);
                                long fileSize = R.getFileSystem().getFileSize(FileSystem.ROOT + FileSystem.MAP_FOLDER + fileName);
//Logger.debug("  fileName: " + fileName + " fileSize: " + fileSize);
                                // map is already cached
                                if (filesCached.contains(temp + fileSize)) {
                                    filesCached.removeElement(temp + fileSize);
                                // map needs to be cached
                                } else {
//Logger.debug("  start init (" + (System.currentTimeMillis() - time) + "ms)");
                                    initializeOfflineMap(fileName, temp + fileSize, true);
                                    //getInitializedOfflineMap(fileName, true);
//Logger.debug("  initialized (" + (System.currentTimeMillis() - time) + "ms)");
                                }

                                // add map to map database
                                if (tempBuffer == null) {
                                    tempBuffer = R.getFileSystem().loadBytes(FileSystem.CACHE_MAP_FOLDER + (temp + fileSize));
                                }

                                if (tempBuffer != null) {
                                    try {
                                        ByteArrayInputStream bais = new ByteArrayInputStream(tempBuffer);
                                        DataInputStream dis = new DataInputStream(bais);
                                        // mapType
                                        dis.readInt();
                                        // fmc
                                        FileMapConfig fmc = FileMapConfig.loadConfigFile(dis);
//Logger.debug("  fmc ready: " + fmc.isReady() + " - " + fmc.toString());
                                        if (fmc.isReady()) {
                                            dos.writeUTF(fileName);
                                            dos.writeInt(fmc.getMapZoom());
                                            for (int i = 1; i < 5; i++) {
                                                Location4D point = FileMapLayer.convertMapToGeo(fmc,
                                                        fmc.getMapViewPort().getCalibrationCorner(i).getLatitude(),
                                                        fmc.getMapViewPort().getCalibrationCorner(i).getLongitude());
//Logger.debug("Dos write: " + point.getLatitude() + " " + point.getLongitude());
                                                dos.writeDouble(point.getLatitude());
                                                dos.writeDouble(point.getLongitude());
                                            }
                                        }

                                        fmc = null;
                                        dis.close();
                                        bais.close();
                                        form.insert(1, new StringItem(fileName, Locale.get("Map_ready") + " (" + (System.currentTimeMillis() - time) / 1000 + "s)"));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    form.insert(1, new StringItem(fileName, Locale.get("File_map_cannot_initialize") + " (" + (System.currentTimeMillis() - time) / 1000 + "s)"));
                                }
                            } else {
                                form.insert(1, new StringItem(fileName, Locale.get("Unsupported") + " (" + (System.currentTimeMillis() - time) / 1000 + "s)"));
                            }
                            gauge.setValue(k+1);
                        }

                        // destroy all variables
                        fileName = null;
                        temp = null;

                        if (STOP) {
                            form.insert(1, new StringItem(Locale.get("Process_stopped"), null));
                        }
                    } else {
                        form.insert(1, new StringItem(Locale.get("No_files"), null));
                    }
                    try {
                        dos.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    
                    // now write bytes of config file
                    R.getFileSystem().saveBytes(FileSystem.CACHE_MAP_FOLDER + MAP_FILE, baos.toByteArray());

                    // remove all unused cached files
                    filesCached.removeElement(MAP_FILE);
                    for (int i = 0; i < filesCached.size(); i++) {
                        R.getFileSystem().delete(FileSystem.CACHE_MAP_FOLDER + filesCached.elementAt(i));
                    }
                    form.insert(1, new StringItem(Locale.get("Completed") + " (" + (System.currentTimeMillis() - time) / 1000 + "s)", ""));

                    form.addCommand(Commands.cmdOK);
                    form.removeCommand(Commands.cmdCancel);

                    // remove everything
                    files = null;
                    filesCached = null;
                    dos = null;
                    baos = null;
                    tempBuffer = null;
                    areaData = null;

                    System.gc();
                }
            });
            thread.start();
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "StoreManager.initializeOfflineMaps()", null);
        }

    }

    /**
     * Create vector filled with names of map that layes inside actual screen.
     * @param center Center of actual screen.
     * @param halfWidthScreen Half of width of this screen but in spheric coordinates
     * @param halfHeightScreen
     * @return
     */
    public static Vector getMapsAroundScreen(double topLeftLat, double topLeftLon,
            double bottomRightLat, double bottomRightLon) {
        double[] viewPort = {topLeftLat, topLeftLon, topLeftLat, bottomRightLon,
        bottomRightLat, topLeftLon, bottomRightLat, bottomRightLon};
        double viewPortHeight = Math.abs(topLeftLat - bottomRightLat);
        double viewPortWidth = Math.abs(bottomRightLon - topLeftLon);
//        long time = System.currentTimeMillis();
//Logger.debug("  StoreManager.getMapsAroundScreen() " + topLeftLat + " " + topLeftLon + " " + bottomRightLat + " " + bottomRightLon);
        Vector maps = new Vector();
        try {
            if (areaData == null)
                areaData = R.getFileSystem().loadBytes(FileSystem.CACHE_MAP_FOLDER + MAP_FILE);
            if (areaData != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(areaData);
                DataInputStream dis = new DataInputStream(bais);

                String name;
                int zoom;
                double[] coordinates = new double[8];

                while (dis.available() > 0) {
                    name = dis.readUTF();
                    zoom = dis.readInt();
//Logger.log(name);
                    for (int i = 0; i < 8; i++) {
                        coordinates[i] = dis.readDouble();
                    }

                    if (viewPortWidth == 0 && viewPortHeight == 0) {
                        if (pointInPolyline(coordinates, topLeftLat, topLeftLon))
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                    } else if (Math.abs(coordinates[0] - coordinates[4]) > viewPortHeight &&
                            Math.abs(coordinates[1] - coordinates[3]) > viewPortWidth) {
                        if (pointInPolyline(coordinates, topLeftLat, topLeftLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(coordinates, topLeftLat, bottomRightLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(coordinates, bottomRightLat, bottomRightLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(coordinates, bottomRightLat, topLeftLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        }
                    } else if (Math.abs(coordinates[0] - coordinates[4]) < viewPortHeight &&
                            Math.abs(coordinates[1] - coordinates[3]) < viewPortWidth) {
                        if (pointInPolyline(viewPort, coordinates[0], coordinates[1])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(viewPort, coordinates[2], coordinates[3])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(viewPort, coordinates[4], coordinates[5])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(viewPort, coordinates[6], coordinates[7])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        }
                    } else {
                        if (pointInPolyline(coordinates, topLeftLat, topLeftLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(coordinates, topLeftLat, bottomRightLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(coordinates, bottomRightLat, bottomRightLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(coordinates, bottomRightLat, topLeftLon)) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(viewPort, coordinates[0], coordinates[1])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(viewPort, coordinates[2], coordinates[3])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(viewPort, coordinates[4], coordinates[5])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        } else if (pointInPolyline(viewPort, coordinates[6], coordinates[7])) {
                            maps.addElement(new StoreManagerMapInfo(name, zoom));
                        }
                    }
                }
                dis.close();
                bais.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
//Logger.debug("    finished - " + maps.size() + " maps founded (" + (System.currentTimeMillis() - time) + "ms)");
        return maps;
    }

    private static boolean pointInPolyline(double[] coordinates, double pointLat, double pointLon) {
        boolean c = false;
//        int i, j;
//        int pointCount = coordinates.length / 2;
//        for (i = 0, j = pointCount - 1; i < pointCount; j = i++) {
//            if (((coordinates[i * 2 + 1] > pointLon) !=
//                    (coordinates[j * 2 + 1] > pointLon)) &&
//                    (pointLat < (coordinates[j * 2] - coordinates[i * 2]) *
//                    (pointLon - coordinates[i * 2 + 1]) /
//                    (coordinates[j * 2 + 1] - coordinates[i * 2 + 1]) +
//                    coordinates[i * 2])) {
//                c = !c;
//            }
//        }

        // stupid myself algorithm !!! need to rewrite to whatever polygon not this !!!
        if (coordinates[0] > pointLat && coordinates[4] < pointLat &&
                coordinates[1] < pointLon && coordinates[3] > pointLon)
            c = true;

//Logger.log("pointInPolyline: " + coordinates[0] + ", " + coordinates[1] + ", " + coordinates[2] + ", " + coordinates[3] + ",\n  " + coordinates[4] + ", " + coordinates[5] + ", " + coordinates[6] + ", " + coordinates[7] +
//        "\n  pointLat: " + pointLat + " pointLon: " + pointLon + ", " + c);
        return c;
    }

    private static FileMapManager initializeOfflineMap(String fileName, String hashedName, boolean justInitialize) {
        byte[] data = R.getFileSystem().loadBytes(FileSystem.CACHE_MAP_FOLDER + hashedName);
        FileMapManager manager = null;
        if (data == null) {
Logger.debug("    StoreManager.getInitializedOfflineMap() - no map cached ... loading new " + fileName);
            // map isn't cached ... need to load and cache
            manager = FileMapManager.getMapType(fileName);

            if (manager == null || !manager.isReady()) {
                return null;
            }

            // now save initialized map for the future ;)
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                if (manager instanceof FileMapManagerSingle) {
                    dos.writeInt(FileMapManager.MAP_TYPE_SINGLE_TILE);
                } else if (manager instanceof FileMapManagerMulti) {
                    dos.writeInt(FileMapManager.MAP_TYPE_MULTI_TILE);
                } else if (manager instanceof FileMapManagerTar) {
                    dos.writeInt(FileMapManager.MAP_TYPE_MULTI_TILE_LOCAL_TAR);
                }

                manager.fileMapConfig.saveConfigFile(dos);
                if (manager instanceof FileMapManagerTar) {
                    manager.storageTar.saveStorageTar(dos);
                }

                dos.flush();
                tempBuffer = baos.toByteArray();
                R.getFileSystem().saveBytes(FileSystem.CACHE_MAP_FOLDER + hashedName, tempBuffer);

                baos.close();
                dos.close();
            } catch (IOException ex) {
                Logger.error("StoreManager.getInitializedOfflineMap() error: " + ex.toString());
                return null;
            }
        } else {
            // map is already cached ... if needed load it and set by data from cache
Logger.debug("    StoreManager.getInitializedOfflineMap() - map cached ... loading new");
            if (!justInitialize) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    DataInputStream dis = new DataInputStream(bais);
                    switch (dis.readInt()) {
                        case FileMapManager.MAP_TYPE_SINGLE_TILE:
                            manager = new FileMapManagerSingle(fileName);
                            break;
                        case FileMapManager.MAP_TYPE_MULTI_TILE:
                            manager = new FileMapManagerMulti(fileName);
                            break;
                        case FileMapManager.MAP_TYPE_MULTI_TILE_LOCAL_TAR:
                            manager = new FileMapManagerTar(fileName);
                            break;
                    }
                    FileMapConfig fmc = FileMapConfig.loadConfigFile(dis);
                    StorageTar storageTar = StorageTar.loadStorageTar(dis);

                    manager.setStorageTar(storageTar);
                    manager.setFileMapConfig(fmc);

                    dis.close();
                    bais.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
//Logger.debug("    finished");
        if (justInitialize) {
            manager = null;
            return null;
        }

        return manager;
    }

    public static FileMapManager getInitializedOfflineMap(String fileName, boolean justInitialize) {
        long fileSize = R.getFileSystem().getFileSize(FileSystem.ROOT + FileSystem.MAP_FOLDER + fileName);
        String hashedFileName = FileSystem.hashFileName("file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + fileName);

        return initializeOfflineMap(fileName, hashedFileName + fileSize, justInitialize);
    }
}
