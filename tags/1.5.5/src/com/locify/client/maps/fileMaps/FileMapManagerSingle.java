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

import java.util.Vector;
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

    public boolean drawActualMap(Graphics gr, FileMapViewPort viewPort,
            Vector imageExist, Vector imageNotExist, int mapPanX, int mapPanY) {
        return false;
    }

    protected String createImageName(int i, int j) {
        return "";
    }

    public int getMinZoomLevel() {
        return fileMapConfig.getMapZoom();
    }

    public int getMaxZoomLevel() {
        return fileMapConfig.getMapZoom();
    }

    public void setZoomLevel(int zoom) {
        return;
    }
}
