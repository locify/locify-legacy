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
import com.locify.client.utils.StringTokenizer;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import java.util.Vector;

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
    /** image forbidden */
    private static Image imageForbidden;
    
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

    public static void drawTile(Graphics g, ImageRequest ir) {
        drawTile(g, ir, 0, 0);
    }

    public static void drawTile(Graphics g, ImageRequest ir, int mapPanX, int mapPanY) {
        String label = getLabelForImage(ir.image);
        if (label == null) {
            g.drawImage(ir.image, ir.x, ir.y);
        } else {
            g.setColor(ColorsFonts.MAP_BG_COLOR);
            g.fillRect(ir.x, ir.y, ir.tileSizeX, ir.tileSizeY);
            g.drawImage(getLoadingImage(),
                    ir.x + (ir.tileSizeX - getLoadingImage().getWidth()) / 2,
                    ir.y + (ir.tileSizeY - getLoadingImage().getHeight()) / 2);

            g.setColor(ColorsFonts.BLACK);
            g.drawRect(ir.x, ir.y, ir.tileSizeX, ir.tileSizeY);

            Vector labels = StringTokenizer.getVector(label, "\n");
            int startHeight = (ir.tileSizeY - labels.size() * g.getFont().getHeight() - (labels.size() - 1) * 5) / 2;
            for (int i = 0; i < labels.size(); i++) {
                String text = (String) labels.elementAt(i);
                int textX = ir.x + (ir.tileSizeX - g.getFont().stringWidth(text)) / 2;
                int textY = ir.y + startHeight + i * (g.getFont().getHeight() + 5);
                g.drawString(text, textX, textY);
            }
        }
    }

    /**
     * Here is hold key for describtion of non-loaded images.
     * @param image Actual image in ImageRequest.
     * @return String if image is not correct map image or <b>null</b> if is.
     */
    private static String getLabelForImage(Image image) {
        if (image == null) {
            return "Problem";
        } else if (image.getWidth() > 1 && image.getHeight() > 1) {
            return null;
        } else if (image.equals(getImageConnectionNotFound())) {
            return Locale.get("Connection_failed");
        } else if (image.equals(getImageLoading())) {
            return Locale.get("File_map_tile_loading");
        } else if (image.equals(getImageNotExisted())) {
            return Locale.get("File_map_tile_not_exist");
        } else if (image.equals(getImageTooBigSize())) {
            return Locale.get("Image_too_big");
        } else if (image.equals(getImageForbidden())) {
            return Locale.get("Access_denied");
        } else
            return "";
    }

    public static Image getImageNotExisted() {
        if (imageNotExisted == null) {
            imageNotExisted = Image.createImage(1, 1);
        }
        return imageNotExisted;
    }

    public static Image getImageLoading() {
        if (imageLoading == null) {
            imageLoading = Image.createImage(1, 1);
        }
        return imageLoading;
    }

    public static Image getImageConnectionNotFound() {
        if (imageConnectionNotFound == null) {
            imageConnectionNotFound = Image.createImage(1, 1);
        }
        return imageConnectionNotFound;
    }

    public static Image getImageTooBigSize() {
        if (imageTooBigSize == null) {
            imageTooBigSize = Image.createImage(1, 1);
        }
        return imageTooBigSize;
    }

    public static Image getImageForbidden() {
        if (imageForbidden == null) {
            imageForbidden = Image.createImage(1, 1);
        }
        return imageForbidden;
    }
    
    public static final Image getLoadingImage() {
        if (loadingImage == null) {
            loadingImage = IconData.getLocalImage("map_tile_64x64.png");
        }
        return loadingImage;
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
