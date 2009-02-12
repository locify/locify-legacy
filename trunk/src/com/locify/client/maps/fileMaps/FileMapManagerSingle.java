/*
 * FileMapManagerSingle.java
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

import com.locify.client.locator.Location4D;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.locify.client.utils.R;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;

/**
 * Manages loading of just one offline map
 * @author MenKat
 */
public class FileMapManagerSingle extends FileMapManager {

    private Vector localMapTiles;
    /* variables for temp operations */
    private int localMapTiles_index = 0;

    public FileMapManagerSingle(String mapPath) {
        super(mapPath);
    }

    public boolean drawActualMap(Graphics gr, FileMapViewPort viewPort) {
/*        if (actualMap == null) {
            //this.unloadAllMaps();
            getFirstLocalMap(viewPort.center);
        }

        try {
            getLocalMaps(viewPort.center);
            if ((actualMap != null) && (actualMap.isMapLoaded())) {
                return actualMap.drawImageSingle(gr, viewPort);
            } else {
                return false;
            }
        } catch (Exception e) {
            //this.unloadAllMaps();
            getFirstLocalMap(viewPort.center);
            return false;
        }*/
        return false;
    }

    protected void loadLocalMapFiles() {
        mapTiles = new Vector();
        try {
            FileConnection dir = (FileConnection) Connector.open(mapPathPrefix + mapPath);
            if (dir.exists() && dir.isDirectory()) {
                Enumeration files = dir.list("*.xml", false); //list all nonhidden xml files
                while (files.hasMoreElements()) {
                    String fname = (String) files.nextElement();
                    String ffname = mapPath + fname;
                    if (R.getFileSystem().exists(ffname)) {
                        try {
                            configFile = new ConfigFileTile(ffname, this);
                            if (configFile.isDescriptorLoaded()) {
                                //System.out.println("\nFileMapManager.scanMapFolder(): added: " + ffname + " tileSize: " + tile.getTileSize());
                                mapTiles.addElement(configFile);
                            }
                        } catch (Exception e) {
                            R.getErrorScreen().view(e, "FileMapManager.scanMapFolders", ffname);
                        }
                    }
                }
            } else if (!dir.isDirectory() && mapPath.indexOf(".tar") != -1) {
            }
            dir.close();
        } catch (IOException e) {
            R.getErrorScreen().view(e, "FileMapManager.scanMapFolders", mapPath);
        }
    }
    
    protected void loadHttpMapFiles() {
    }

    /**
     * Returns vector of maps relevat for current position.
     * @param pos location for which maps are searching
     * @return vector containing finded maps
     */
    private Vector getLocalMaps(Location4D pos) {
        localMapTiles = new Vector();
        //context.getLogger().log("generating list of available maps ("+mapLayers.size()+")");
        for (int i = 0; i < mapTiles.size(); i++) {
            ConfigFileTile cft = (ConfigFileTile) mapTiles.elementAt(i);
            if (cft.getAvailableViewPort().containsPosition(pos)) {
                localMapTiles.addElement(cft);
            }
        }
        //System.out.println("\nFileMapManager.getLocalMaps() (size): " + localMapTiles.size());
        return localMapTiles;
    }

    private ConfigFileTile getFirstLocalMap(Location4D pos) {
/*        localMapTiles = getLocalMaps(pos);
        if (localMapTiles.size() > 0) {
            localMapTiles_index = 0;
            ConfigFileTile cft = (ConfigFileTile) localMapTiles.firstElement();
            if (!cft.isMapLoaded()) {
                cft.loadMap();
            }
            return cft;
        }*/
        return null;
    }

    private ConfigFileTile getNextLocalMap() {
/*        if (actualMap != null) {
            actualMap.deactivate();
            System.gc();

            localMapTiles_index = (localMapTiles_index + 1) % localMapTiles.size();
            actualMap = (FileTile) localMapTiles.elementAt(localMapTiles_index);
            if (!actualMap.isMapLoaded()) {
                actualMap.loadMap();
            }
            return actualMap;
        }*/
        return null;
    }
}
