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

import com.locify.client.maps.TileCache;
import com.locify.client.data.FileSystem;
import com.locify.client.maps.projection.NullProjection;
import com.locify.client.maps.projection.Projection;
import com.locify.client.maps.projection.ReferenceEllipsoid;
import com.locify.client.maps.projection.S42Projection;
import com.locify.client.maps.projection.UTMProjection;
import com.locify.client.net.Http;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StorageTar;
import com.locify.client.utils.StorageTar.TarRecord;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Graphics;

/**
 * Manages loading of offline maps
 * @author MenKat
 */
public abstract class FileMapManager {

    public static final int MAP_TYPE_WRONG = -1;
    public static final int MAP_TYPE_SINGLE_TILE = 0;
    public static final int MAP_TYPE_MULTI_TILE = 1;
    public static final int MAP_TYPE_MULTI_TILE_LOCAL_TAR_XML = 2;
    public static final int MAP_TYPE_MULTI_TILE_LOCAL_TAR_MAP = 3;

    /** type of projection */
    protected Projection mapProjection;
    /** vector that contains single map tiles */
    protected Vector mapTiles;
    /** map table for caches images */
    protected static TileCache cache;
    /** map config filename */
    protected String mapFilename;
    /** dir for images relative to mappath */
    protected String mapImageDir;
    /** path to map */
    protected String mapPath;
    /** prefix of mapPath if maps are from local filesystem */
    protected String mapPathPrefix;
    /** temp variables for http string file */
    private static String obtainedData;
    /** config file */
    protected ConfigFileTile configFile;
    /** if mapmanager is succesfully initialized */
    private boolean ready;
    /** storage for tar */
    protected static StorageTar tar;

    public FileMapManager(String mapPath) {
        if (mapPath.startsWith("http://") && mapPath.endsWith(".xml")) {
            this.mapFilename = mapPath.substring(mapPath.lastIndexOf('/') + 1);
            this.mapImageDir = this.mapFilename.substring(0, this.mapFilename.lastIndexOf('.')) + "/";
            this.mapPath = mapPath.substring(0, (mapPath.length() - this.mapFilename.length()));
            this.mapPathPrefix = "";
            this.loadHttpMapFiles();
        } else if (mapPath.endsWith(".xml") || mapPath.endsWith(".tar")) {
            this.mapFilename = mapPath;
            this.mapImageDir = this.mapFilename.substring(0, this.mapFilename.lastIndexOf('.')) + "/";
            this.mapPath = FileSystem.MAP_FOLDER;
            this.mapPathPrefix = "file:///" + FileSystem.ROOT;
            this.loadLocalMapFiles();
        }

        if (configFile != null && configFile.isDescriptorLoaded()) {
            mapProjection = getProjection(configFile.getProjectionType());
            if (cache == null || cache.getTileSizeX() != configFile.getTileSizeX() || cache.getTileSizeY() != configFile.getTileSizeY()) {
                cache = new TileCache(configFile.getTileSizeX(), configFile.getTileSizeY());
                cache.start();
                //Logger.debug("new FileTileCache started!!!");
            }
            ready = true;
        } else {
            ready = false;
        }
    }

    public boolean isReady() {
        return ready;
    }

    public static int getMapType(String mapPath) {
        if (mapPath.startsWith("http://") && mapPath.endsWith(".xml")) {
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
                return MAP_TYPE_WRONG;
            } else
                return MAP_TYPE_MULTI_TILE;
        } else if ((mapPath.startsWith("file:///") && mapPath.endsWith(".xml"))
                || mapPath.endsWith(".xml")) {
            if (!mapPath.startsWith("file:///"))
                mapPath = "file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + mapPath;
            try {
                FileConnection dir = (FileConnection) Connector.open(mapPath);
                if (dir.exists()) {
                    String ffDirName = mapPath.substring(0, mapPath.lastIndexOf('.'));
                    String ffImageName = ffDirName + ".png";

                    dir = (FileConnection) Connector.open(ffDirName);
                    if (dir.exists() && dir.isDirectory()) {
                        return MAP_TYPE_MULTI_TILE;
                    }
                    dir = (FileConnection) Connector.open(ffImageName);
                    if (dir.exists() && !dir.isDirectory()) {
                        return MAP_TYPE_SINGLE_TILE;
                    }
                } 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return MAP_TYPE_WRONG;
        } else {
            String mapPathAbsolute = "file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + mapPath;
            try {
                FileConnection dir = (FileConnection) Connector.open(mapPathAbsolute);
                if (dir.exists() && dir.isDirectory()) {
                    Enumeration files = dir.list("*.xml", false); //list all nonhidden xml files
                    if (files.hasMoreElements() == false) {
                        return MAP_TYPE_WRONG;
                    } else {
                        String fname = (String) files.nextElement();
                        String ffname = mapPath + fname;
                        String ffDirName = ffname.substring(0, ffname.lastIndexOf('.'));
                        String ffImageName = ffname.substring(0, ffname.lastIndexOf('.')) + ".png";
//                    System.out.println("\n" + ffname);
//                    System.out.println("\n" + ffDirName);
//                    System.out.println("\n" + ffImageName);
                        if (R.getFileSystem().exists(FileSystem.MAP_FOLDER + ffname)) {
                            if (R.getFileSystem().exists(FileSystem.MAP_FOLDER + ffDirName)) {
                                return MAP_TYPE_MULTI_TILE;
                            }
                            if (R.getFileSystem().exists(FileSystem.MAP_FOLDER + ffImageName)) {
                                return MAP_TYPE_SINGLE_TILE;
                            }
                        } else {
                            return MAP_TYPE_WRONG;
                        }
                    }
                } else if (!dir.isDirectory() && mapPath.indexOf(".tar") != -1) {
                    tar = new StorageTar(mapPathAbsolute);
                    TarRecord record = tar.getTarRecord(mapPathAbsolute.substring(mapPathAbsolute.lastIndexOf('/') + 1, mapPathAbsolute.lastIndexOf('.')) + ".xml");
                    if (record != null) {
                        obtainedData = new String(StorageTar.loadFile(mapPathAbsolute, record));
//System.out.println("ObtainedData1: " + obtainedData);
                        if (obtainedData != null)
                            return MAP_TYPE_MULTI_TILE_LOCAL_TAR_XML;
                    } else {
                        record = tar.getTarRecord(mapPathAbsolute.substring(mapPathAbsolute.lastIndexOf('/') + 1, mapPathAbsolute.lastIndexOf('.')) + ".map");
                        if (record != null) {
                            obtainedData = new String(StorageTar.loadFile(mapPathAbsolute, record));
//System.out.println("ObtainedData2: " + obtainedData);
                            if (obtainedData != null)
                                return MAP_TYPE_MULTI_TILE_LOCAL_TAR_MAP;
                        }
                    }
                    System.out.println("FileMapManager.getMapType() - unsupported map tar type");
                    return MAP_TYPE_WRONG;
                }
                dir.close();
                return MAP_TYPE_WRONG;
            } catch (IOException e) {
                R.getErrorScreen().view(e, "FileMapManager.scanMapFolders", mapPath);
                return MAP_TYPE_WRONG;
            }
        }
    }

    public ConfigFileTile getConfigFileTile() {
        return configFile;
    }

    protected TileCache getCache() {
        return cache;
    }

    public static void setObtainedData(String httpData) {
        FileMapManager.obtainedData = httpData;
    }
    
    public static String getObtainedData() {
        return obtainedData;
    }

    protected static Projection getProjection(String projection) {
        Projection proj;
        if (projection.equals("UTM")) {
            proj = new UTMProjection(ReferenceEllipsoid.WGS84);
        } else if (projection.equals("S42")) {
            proj = new S42Projection(ReferenceEllipsoid.KRASOVSKY);
        } else {
            //Latitude/Longitude
            proj = new NullProjection();
        }
        return proj;

//        return new NullProjection();
    }
   
    public Projection getMapProjection() {
        return mapProjection;
    }

    public abstract boolean drawActualMap(Graphics gr, FileMapViewPort viewPort, int mapPanX, int mapPanY);

    protected abstract void loadLocalMapFiles();

    protected abstract void loadHttpMapFiles();
}