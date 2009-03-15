/*
 * FileMapManagerMulti.java
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

import com.locify.client.utils.Logger;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Graphics;
import com.locify.client.utils.R;
import java.io.IOException;

/**
 * Manages loading of multiple offline maps
 * @author MenKat
 */
public class FileMapManagerMulti extends FileMapManager {

    public FileMapManagerMulti(String mapPath) {
        super(mapPath);
    }

    public boolean drawActualMap(Graphics gr, FileMapViewPort viewPort, int mapPanX, int mapPanY) {
        //System.out.println("FileMapManager.drawActualMap() multi tile: " + viewPort.toString());
        return configFile.drawImageMulti(gr, viewPort, null, mapPanX, mapPanY);
    }

    protected void loadLocalMapFiles() {
        try {
            FileConnection dir = (FileConnection) Connector.open(mapPathPrefix + mapPath + mapFilename);
            /*if (dir.exists() && dir.isDirectory()) {
                Enumeration files = dir.list("*.xml", false); //list all nonhidden xml files
                while (files.hasMoreElements()) {
                    String fname = (String) files.nextElement();
                    String ffname = mapPath + fname;
                    if (R.getFileSystem().exists(ffname)) {
                        try {
                            configFile = new ConfigFileTile(ffname, this);
                            if (configFile.isDescriptorLoaded()) {
                                break;
                            }
                        } catch (Exception e) {
                            R.getErrorScreen().view(e, "FileMapManager.scanMapFolders", ffname);
                        }
                    }
                }
            } else */
            if (!dir.isDirectory() && mapFilename.indexOf(".xml") != -1) {
                try {
                    configFile = new ConfigFileTile(mapPath + mapFilename, this);
                } catch (Exception e) {
                    R.getErrorScreen().view(e, "FileMapManager.scanMapFolders", mapPath);
                }
            } else {
                Logger.debug("FileMapManagerMulti.loadLocalMapFiles() - unsupported map type");
            }
            dir.close();
        } catch (IOException e) {
            R.getErrorScreen().view(e, "FileMapManager.scanMapFolders", mapPath);
        }
    }

    protected void loadHttpMapFiles() {
        String fileValue = FileMapManager.getObtainedData();
        if (fileValue != null) {
            try {
                configFile = new ConfigFileTile(fileValue, this);
            } catch (Exception e) {
                R.getErrorScreen().view(e, "FileMapManager.scanMapFolders", null);
            }
        }
    }
}
