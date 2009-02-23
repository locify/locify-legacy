/*
 * Icons.java
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
package com.locify.client.data;

import com.locify.client.net.Http;
import javax.microedition.lcdui.Image;
import com.locify.client.utils.R;

/**
 * This class takes cares about storing icons downloaded from web,
 * download neccesary icons, load icons from JAR.
 * @author David Vavra
 */
public class IconData {

    /**
     * Loads desired icon from JAR, database or downloads it
     * @param url
     * @return image
     */
    public static Image get(String url) {
        try {
            if (url == null || url.equals("") || Sync.isRunning() || R.getXmlParser().isAutoInstall()) {
                return null;
            }
            //JAR icons
            if (url.startsWith("locify://icons/")) {
                return Image.createImage(url.substring(14));
            }
            //filesystem icons
            byte[] imageData = R.getFileSystem().loadBytes(FileSystem.ICONS_FOLDER + FileSystem.hashFileName(url) + ".png");
            if (imageData != null) //icon available
            {
                try {
                    return Image.createImage(imageData, 0, imageData.length);
                } catch (IllegalArgumentException e) //spatne ulozeny obrazek
                {
                    (new Http()).start(url);
                    return null;
                }
            }
            //download not available icon
            (new Http()).start(url);
            return null;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "IconData.get", url);
            return null;
        }
    }

    /**
     * Saves icon to database
     * @param url
     * @param imageData
     */
    public static void save(String url, byte[] imageData) {
        try {
            R.getFileSystem().saveBytes(FileSystem.ICONS_FOLDER + FileSystem.hashFileName(url) + ".png", imageData);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "IconData.save", url);
        }
    }

    /**
     * Deletes icon from database
     * @param url
     */
    public static void delete(String url) {
        try {
            R.getFileSystem().delete(FileSystem.ICONS_FOLDER + FileSystem.hashFileName(url) + ".png");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "IconData.delete", url);
        }
    }

    public static Image reScaleImage(Image temp, int newX, int newY) {
        //Need an array (for RGB, with the size of original image)
        int rgb[] = new int[temp.getWidth()*temp.getHeight()];
        //Get the RGB array of image into "rgb"
        temp.getRGB(rgb,0,temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());
        //Call to our function and obtain RGB2
        int rgb2[] = reScaleArray(rgb, temp.getWidth(), temp.getHeight(), newX, newY);
        //Create an image with that RGB array
        return Image.createRGBImage(rgb2, newX, newY, true);
    }

    private static int[] reScaleArray(int[] ini, int x, int y, int x2, int y2) {
        int out[] = new int[x2 * y2];
        for (int yy = 0; yy < y2; yy++) {
            int dy = yy * y / y2;
            for (int xx = 0; xx < x2; xx++) {
                int dx = xx * x / x2;
                out[(x2 * yy) + xx] = ini[(x * dy) + dx];
            }
        }
        return out;
    }
}
