/*
 * FileMapManagerTarTrekBuddy.java
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
import com.locify.client.utils.R;
import com.locify.client.utils.StorageTar;
import javax.microedition.lcdui.Graphics;

/**
 * Manages loading offline maps in TrekBuddy specific TAR format
 * @author MenKat
 */
public class FileMapManagerTarTrekBuddy extends FileMapManager {

    /** main tile in multi tile map */
    private StorageTar storage;
    
    public FileMapManagerTarTrekBuddy(String mapPath) {
        super(mapPath);
    }

    public boolean drawActualMap(Graphics gr, FileMapViewPort viewPort) {
        return configFile.drawImageMulti(gr, viewPort, storage);
    }

    protected void loadLocalMapFiles() {
        String fileValue = FileMapManager.getObtainedData();
        if (fileValue != null) {
            try {
                configFile = new ConfigFileTile(fileValue, this);
                if (configFile.isDescriptorLoaded()) {
                    storage = tar;
                }
            } catch (Exception e) {
                R.getErrorScreen().view(e, "FileMapManagerTar.loadLocalMapFiles()", null);
            }
        }
    }

    protected void loadHttpMapFiles() {
        Logger.error("FileMapManagerTar.loadHttpMapFiles() - unexpected operation");
    }
}
