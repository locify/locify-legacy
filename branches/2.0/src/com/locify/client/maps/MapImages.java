/*
 * MapImages.java
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

import com.locify.client.data.IconData;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;

/**
 *
 * @author menion
 */
public class MapImages {

    /** image displayed while tile is loading */
    private static Image loadingImage;
    /** image for nonexsited tile */
    private static Image imageNotExisted;
    /** image for loading text */
    private static Image imageLoading;
    /** image for connecion problem */
    private static Image imageConnectionNotFound;
    /** if image is too big */
    private static Image imageTooBigSize;
    /** plus icon image */
    private static Image imageIconPlus;
    /** minus icon image */
    private static Image imageIconMinus;
    /** plus icon zoom image */
    private static Image imageIconZoomPlus;
    /** minus icon zoom image */
    private static Image imageIconZoomMinus;
    /** actual position image */
    private static Image imageActualLocation;
    /** size of zoom icon */
    public static int imageIconZoomSideSize;
    
    public static Image getImageNotExisted() {
        try {
            if (imageNotExisted == null) {
                imageNotExisted = getImageVarious(Locale.get("File_map_tile_not_exist"));
            }
            return imageNotExisted;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageNotExisted()", null);
            return null;
        }
    }

    public static Image getImageLoading() {
        try {
            if (imageLoading == null) {
                imageLoading = getImageVarious(Locale.get("File_map_tile_loading"));
            }
            return imageLoading;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageLoading()", null);
            return null;
        }
    }

    public static Image getImageConnectionNotFound() {
        try {
            if (imageConnectionNotFound == null) {
                imageConnectionNotFound = getImageVarious(Locale.get("Connection_failed"));
            }
            return imageConnectionNotFound;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageLoading()", null);
            return null;
        }
    }

    public static Image getImageTooBigSize() {
        try {
            if (imageTooBigSize == null) {
                imageTooBigSize = getImageVarious(Locale.get("Image_too_big"));
            }
            return imageTooBigSize;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageTooBigSize()", null);
            return null;
        }
    }

    public static final Image getLoadingImage() {
        if (loadingImage == null) {
            loadingImage = IconData.getLocalImage("map_tile_64x64.png");
        }
        return loadingImage;
    }

    private static Image getImageVarious(String tileText) {
        Image image = Image.createImage(64, 64);
        Graphics g = image.getGraphics();

        g.drawImage(getLoadingImage(), 0, 0);
        g.setFont(ColorsFonts.FONT_MEDIUM);
        g.drawString(tileText, (64 - ColorsFonts.FONT_MEDIUM.stringWidth(tileText)) / 2, (64 - 10) / 2);

        return image;
    }

    public static Image getMapIconPlus() {
        if (imageIconPlus == null) {
            imageIconPlus = IconData.getLocalImage("map_icon_plus.png");
        }
        return imageIconPlus;
    }

    public static Image getMapIconMinus() {
        if (imageIconMinus == null) {
            imageIconMinus = IconData.getLocalImage("map_icon_minus.png");
        }
        return imageIconMinus;
    }

    public static Image getMapIconZoomPlus(int screenHeight) {
        if (imageIconZoomPlus == null) {
            imageIconZoomPlus = IconData.getLocalImage("map_icon_zoom_plus.png");

            if (imageIconZoomSideSize == 0) {
                int size = screenHeight * imageIconZoomPlus.getHeight() / 1000;
                imageIconZoomSideSize = size < 35 ? 35 : size;
            }

            imageIconZoomPlus = imageIconZoomPlus.scaled(imageIconZoomSideSize, imageIconZoomSideSize);
        }
        return imageIconZoomPlus;
    }

    public static Image getMapIconZoomMinus(int screenHeight) {
        if (imageIconZoomMinus == null) {
            imageIconZoomMinus = IconData.getLocalImage("map_icon_zoom_minus.png");

            if (imageIconZoomSideSize == 0) {
                int size = screenHeight * imageIconZoomPlus.getHeight() / 1000;
                imageIconZoomSideSize = size < 35 ? 35 : size;
            }

            imageIconZoomMinus = imageIconZoomMinus.scaled(imageIconZoomSideSize, imageIconZoomSideSize);
        }
        return imageIconZoomMinus;
    }

    public static Image getMapIconActualLocation() {
        if (imageActualLocation == null) {
            imageActualLocation = IconData.getLocalImage("map_icon_actualLoc.png");
        }
        return imageActualLocation;
    }
}
