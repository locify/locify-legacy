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

import com.locify.client.utils.R;
import com.locify.client.net.Http;
import com.sun.lwuit.Image;
import java.io.IOException;

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
//System.out.println("GetImage: " + url);
            //JAR icons
            if (url.startsWith("locify://icons/")) {
                return getLocalImage(url.substring(15));
            }
            //filesystem icons
            byte[] imageData = R.getFileSystem().loadBytes(url);
            if (imageData == null) {
                imageData = R.getFileSystem().loadBytes(FileSystem.IMAGES_FOLDER + FileSystem.hashFileName(url) + ".png");
            }

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

    public static Image getLocalImage(String image) {
        //return null;
        try {
            if (!image.endsWith(".png"))
                image += ".png";
            return Image.createImage(image);
        } catch (IOException ex) {
            System.out.println("GetLocalImage: " + image + " ex: " + ex.toString());
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
}
