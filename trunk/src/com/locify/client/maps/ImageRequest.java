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

import javax.microedition.lcdui.Image;

/**
 * Object representing single image request
 * @author Menion
 */
public class ImageRequest {
        protected String fileName;
        protected String tarName;
        protected int byteFromPosition;
        protected Image image;
        // for online maps number of tile
        protected int x;
        protected int y;

        protected boolean requiredTile;

        public ImageRequest(String fileName) {
            this(fileName, null, 0);
        }

        public ImageRequest(String fileName, int x, int y) {
            this(fileName);
            this.x = x;
            this.y = y;
            this.requiredTile = false;
        }

        public ImageRequest(String fileName, String tarName, int byteFromPosition) {
            this.fileName = fileName;
            this.tarName = tarName;
            this.byteFromPosition = byteFromPosition;
            this.image = null;
            this.requiredTile = false;
        }
}
