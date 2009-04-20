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

import com.locify.client.utils.Utils;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Manages loading of multiple offline maps
 * @author MenKat
 */
public class FileMapManagerMulti extends FileMapManager {

    public FileMapManagerMulti(String mapPath) {
        super(mapPath);
    }

    public boolean drawActualMap(Graphics gr, FileMapViewPort viewPort,
            Vector imageExist, Vector imageNotExist, int mapPanX, int mapPanY) {
        //return drawImageMulti(gr, viewPort, null, mapPanX, mapPanY);
        appendRequests(imageExist, imageNotExist, viewPort, null, mapPanX, mapPanY);
        return true;
    }

    protected String createImageName(int i, int j) {
        if (stringBuffer.length() > 0)
            stringBuffer.delete(0, stringBuffer.length());
        stringBuffer.append(mapImageDir);
        stringBuffer.append(Utils.addZerosBefore("" + i, 3) + "_" + Utils.addZerosBefore("" + j, 3));
        return stringBuffer.toString();
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
