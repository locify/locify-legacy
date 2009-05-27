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

import javax.microedition.lcdui.Image;
import com.locify.client.utils.R;
import com.locify.client.net.Http;

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
            if (url == null || url.equals("") || R.getXmlParser().isAutoInstall()) {
                return null;
            }
            //JAR icons
            if (url.startsWith("locify://icons/")) {
                return Image.createImage(url.substring(14));
            }
            //filesystem icons
            byte[] imageData = R.getFileSystem().loadBytes(FileSystem.IMAGES_FOLDER + FileSystem.hashFileName(url) + ".png");
            if (imageData != null) //icon available
            {
                try {
                    return Image.createImage(imageData, 0, imageData.length);
                } catch (IllegalArgumentException e) //spatne ulozeny obrazek
                {
                    R.getHttp().start(url,Http.IMAGE_DOWNLOADER);
                    return null;
                }
            }
            //download not available icon
            R.getHttp().start(url,Http.IMAGE_DOWNLOADER);
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
            url = R.getHttp().makeAbsoluteURL(url);
            R.getFileSystem().saveBytes(FileSystem.IMAGES_FOLDER + FileSystem.hashFileName(url) + ".png", imageData);
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
            R.getFileSystem().delete(FileSystem.IMAGES_FOLDER + FileSystem.hashFileName(url) + ".png");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "IconData.delete", url);
        }
    }

    /**
     * Function that rescale image.
     * @param temp Image to rescale.
     * @param newX New width in px.
     * @param newY New height in px.
     * @return Rescaled image.
     */
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

/*    // GetRotatedBitmap	- Create a new bitmap with rotated image
    // Returns		- Returns new bitmap with rotated image
    // hBitmap		- Bitmap to rotate
    // radians		- Angle of rotation in radians
    // clrBack		- Color of pixels in the resulting bitmap that do
    //			  not get covered by source pixels
    // Note			- If the bitmap uses colors not in the system palette
    //			  then the result is unexpected. You can fix this by
    //			  adding an argument for the logical palette.
    public static Image rotateImage(Image image, int angle) {
        float radians = (float) (angle / (180 / Math.PI));
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);

        // Compute dimensions of the resulting bitmap
        // First get the coordinates of the 3 corners other than origin
        int x1 = (int)(-1 * image.getHeight() * sine);
        int y1 = (int)(image.getHeight() * cosine);
        int x2 = (int)(image.getWidth() * cosine - image.getHeight() * sine);
        int y2 = (int)(image.getHeight() * cosine + image.getWidth() * sine);
        int x3 = (int)(image.getWidth() * cosine);
        int y3 = (int)(image.getHeight() * sine);

        int minx = Math.min(0, Math.min(x1, Math.min(x2,x3)));
        int miny = Math.min(0,Math.min(y1, Math.min(y2,y3)));
        int maxx = Math.max(x1, Math.max(x2,x3));
        int maxy = Math.max(y1, Math.max(y2,y3));

        int w = maxx - minx;
        int h = maxy - miny;


        // Create a bitmap to hold the result
        Image result = Image.createImage(w, h);
        Graphics g = result.getGraphics();
        g.setColor(ColorsFonts.CYAN);

        // Now do the actual rotating - a pixel at a time
        // Computing the destination point for each source point
        // will leave a few pixels that do not get covered
        // So we use a reverse transform - e.i. compute the source point
        // for each destination point

        int[] pixel = new int[1];

        for( int y = miny; y < maxy; y++ ) {
            for( int x = minx; x < maxx; x++ ) {
                int sourcex = (int)(x*cosine + y*sine);
                int sourcey = (int)(y*cosine - x*sine);
                if(sourcex >= 0 && sourcex < image.getWidth() && sourcey >= 0
                        && sourcey < image.getHeight()) {
                    
                    
                    image.getRGB(pixel, 0, image.getWidth() * image.getHeight(), sourcex, sourcey, 1, 1);
                    g.drawRGB(pixel, 0, 2, x, y, 1, 1, true);
                }
            }
        }

        return result;
    }
 */
}
