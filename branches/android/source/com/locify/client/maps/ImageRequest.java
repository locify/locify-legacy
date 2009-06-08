/*
 * ImageRequest.java
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

import com.locify.client.utils.StorageTar;
import com.locify.client.utils.StorageTar.TarRecord;
import de.enough.polish.android.lcdui.Image;

/**
 * Object representing single image request
 * @author Menion
 */
public class ImageRequest {
    protected String fileName;
    protected StorageTar tar;
    protected TarRecord record;
    protected Image image;

    /**
     * Online maps - number of tile X<br>
     * Offline maps - top left pixel for print X.
     */
    protected int x;
    /**
     * Online maps - number of tile Y<br>
     * Offline maps - top left pixel for print Y.
     */
    protected int y;

    /**
     * TileSize X
     */
    protected int tileSizeX;
    /**
     * TileSize Y
     */
    protected int tileSizeY;

    /**
     * State variables for helping cache work.
     */
    protected boolean requiredTile;

    /**
     * Create ImageRequest for Offline <b>unpacked</b> maps.
     * @param fileName Absolute path to file.
     * @param tileSizeX Width of required image.
     * @param tileSizeY Height of required image.
     * @param drawFromX Pixel from start draw (top-left X).
     * @param drawFromY Pixel from start draw (top-left Y).
     */
    public ImageRequest(String fileName, int tileSizeX, int tileSizeY, int drawFromX, int drawFromY) {
        this(fileName, null, null, tileSizeX, tileSizeY, drawFromX, drawFromY);
    }

    /**
     * Create ImageRequest for Online maps.
     * @param x X index of map on world map.
     * @param y Y index of map on wolrd map.
     * @param fileName Absolute path to file.
     * @param tileSizeX Width of required image.
     * @param tileSizeY Height of required image.
     */
    public ImageRequest(int x, int y, String fileName, int tileSizeX, int tileSizeY) {
        this(fileName, null, null, tileSizeX, tileSizeY, x, y);
    }

    /**
     * Create ImageRequest for Offline <b>packed</b> maps.
     * @param fileName Absolute path to file.
     * @param tar Tar file from take images.
     * @param record Record that is used for receiving image.
     * @param tileSizeX Width of required image.
     * @param tileSizeY Height of required image.
     * @param drawFromX Pixel from start draw (top-left X).
     * @param drawFromY Pixel from start draw (top-left Y).
     */
    public ImageRequest(String fileName, StorageTar tar, TarRecord record,
            int tileSizeX, int tileSizeY, int drawFromX, int drawFromY) {
        this.fileName = fileName;
        this.tar = tar;
        this.record = record;
        this.tileSizeX = tileSizeX;
        this.tileSizeY = tileSizeY;
        this.x = drawFromX;
        this.y = drawFromY;
        this.image = null;
        this.requiredTile = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return "ImageRequest: [fileName: " + fileName + ", tar: " + tar + ", tileSizeX: " + tileSizeX + ", tileSizeY: " +
                tileSizeY + ", x: " + x + ", y: " + y + "]";
    }
}
