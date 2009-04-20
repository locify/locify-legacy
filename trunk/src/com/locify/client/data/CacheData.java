/*
 * Services.java
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

import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;

/**
 * Manages cache
 * 
 * @author David Vavra
 */
public class CacheData {

    /**
     * Returns xml of given url saved in cache
     * @param url cache url
     * @return cache html
     */
    public static byte[] get(String url) {
        try {
            url = FileSystem.CACHE_FOLDER + FileSystem.hashFileName(ServicesData.getCurrent()) + "/" + FileSystem.hashFileName(url) + ".html";
            return R.getFileSystem().loadBytes(url);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CacheData.get", url);
            return null;
        }
    }

    /**
     * Adds new cache item to database
     * @param url 
     * @param xhtml 
     */
    public static void add(String url, String xhtml) {
        try {
            url = R.getHttp().makeAbsoluteURL(url);
            xhtml = Utils.removeXmlHeaders(xhtml);
            //hledani polozky v kesi
            url = FileSystem.CACHE_FOLDER + FileSystem.hashFileName(ServicesData.getCurrent()) + "/" + FileSystem.hashFileName(url) + ".html";
            R.getFileSystem().saveString(url, xhtml);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CacheData.add", url);
        }
    }

    /**
     * Deletes item from the cache
     * @param service service welcome message url
     * @param url cache item url
     */
    public static void delete(String service, String url) {
        try {
            R.getFileSystem().delete(FileSystem.CACHE_FOLDER + FileSystem.hashFileName(service) + "/" + FileSystem.hashFileName(url) + ".html");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CacheData.delete", url);
        }
    }
    
    /**
     * Deletes all cache from one service
     * @param service service to clear
     */
    public static void deleteAll(String service)
    {
        try {
            R.getFileSystem().deleteAll(FileSystem.CACHE_FOLDER + FileSystem.hashFileName(service)+"/");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CacheData.deleteAll", null);
        }
    }
}
