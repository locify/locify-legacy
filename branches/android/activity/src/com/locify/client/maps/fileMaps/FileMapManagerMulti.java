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
import com.locify.client.utils.Utils;
import java.io.IOException;
import java.util.Vector;
import de.enough.polish.android.io.Connector;
import de.enough.polish.android.io.file.FileConnection;
import de.enough.polish.android.lcdui.Graphics;

/**
 * Manages loading of multiple offline maps
 * @author MenKat
 */
public class FileMapManagerMulti extends FileMapManager {

    private int fileNameType;
    private String tempMapName;
    
    private static final int FILENAME_TYPE_UNKNOWN = 0;
    private static final int FILENAME_TYPE_01 = 1;
    private static final int FILENAME_TYPE_02 = 2;
    private static final int FILENAME_TYPE_WRONG = 10;
    public FileMapManagerMulti(String mapPath) {
        super(mapPath);
        fileNameType = FILENAME_TYPE_UNKNOWN;
    }

    public boolean drawActualMap(Graphics gr, FileMapViewPort viewPort,
            Vector imageExist, Vector imageNotExist, int mapPanX, int mapPanY) {
        //return drawImageMulti(gr, viewPort, null, mapPanX, mapPanY);
        appendRequests(imageExist, imageNotExist, viewPort, null, mapPanX, mapPanY);
        return true;
    }

    protected String createImageName(int i, int j) {
        while (fileNameType == FILENAME_TYPE_UNKNOWN) {
            FileConnection con = null;
            try {
                tempMapName = mapFilename.substring(0, mapFilename.lastIndexOf('.'));

//Logger.log("Try: " + mapPath + tempMapName + "/" + tempMapName + "_0_0.png");
                con = (FileConnection) Connector.open(mapPath + tempMapName + "/" + tempMapName + "_0_0.png");
                if (con.exists()) {
                    fileNameType = FILENAME_TYPE_01;
                    break;
                }
                if (con != null) {
                    con.close();
                }

//Logger.log("Try: " + mapPath + "000_000.png");
                con = (FileConnection) Connector.open(mapPath + "000_000.png");
                if (con.exists()) {
                    fileNameType = FILENAME_TYPE_02;
                    break;
                }
                if (con != null) {
                    con.close();
                }

                fileNameType = FILENAME_TYPE_WRONG;
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
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


        if (stringBuffer.length() > 0)
            stringBuffer.delete(0, stringBuffer.length());
        
        if (fileNameType == FILENAME_TYPE_01) {
//Logger.log("Create name: " + tempMapName + "/" + tempMapName + "_" + (i * fileMapConfig.tileSizeX) + "_" +
//                    (j * fileMapConfig.tileSizeY) + ".png");
            stringBuffer.append(tempMapName + "/" + tempMapName + "_");
            stringBuffer.append((i * fileMapConfig.tileSizeX) + "_" +
                    (j * fileMapConfig.tileSizeY) + ".png");
        } else if (fileNameType == FILENAME_TYPE_02) {
            stringBuffer.append(tempMapName + "/");
            stringBuffer.append(Utils.addZerosBefore("" + i, 3) + "_" +
                    Utils.addZerosBefore("" + j, 3) + ".png");
        }

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
