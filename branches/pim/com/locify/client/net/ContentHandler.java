/*
 * ContentHandler.java
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
package com.locify.client.net;

import com.locify.client.data.CacheData;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.utils.R;

/**
 * Processes incoming data and decides proper action, also manages caching
 * @author Destil
 */
public class ContentHandler {
    
    private static boolean pragmaNoCache;
    private static boolean loadedFromCache;

    public static void setPragmaNoCache(boolean pragmaNoCache) {
        ContentHandler.pragmaNoCache = pragmaNoCache;
    }

    public static void setLoadedFromCache(boolean loadedFromCache) {
        ContentHandler.loadedFromCache = loadedFromCache;
    }

    /**
     * Handles content from net
     * @param url url of content
     * @param response content
     */
    public static void handle(String url, String response) {
        try {
            //kml
            if (response.indexOf("<kml xmlns=") != -1 && response.indexOf("<kml xmlns=") < 100) {
                R.getGeoDataBrowser().setKml(response);
                R.getBack().dontSave();
                R.getURL().call("locify://geoFileBrowser");
            } //geocoding result
            else if (response.indexOf("<ResultSet") != -1) {
                Geocoding.result(response);
            } //geocoding error
            else if (response.indexOf("<Error xmlns=\"urn:yahoo:api\">") != -1) {
                Geocoding.error();
            } //special Locify xhtml
            else if (response.indexOf("<body class=\"alert\">") != -1 || response.indexOf("<body class=\"list\">") != -1 || response.indexOf("<body class=\"serviceInfo\">") != -1 || response.indexOf("<body class=\"confirmation\">") != -1 || response.indexOf("<body class=\"update\">") != -1 || response.indexOf("<meta http-equiv=\"refresh\"") != -1 || response.indexOf("<locify:call") != -1 || response.indexOf("<sync>") != -1) {
                    boolean shouldCache = R.getXmlParser().parseLocifyXHTML(response);
                    if (!pragmaNoCache && shouldCache) {
                        CacheData.add(url, response);
                    }
            } //some other html or form
            else if (response.startsWith("<?xml version=\"1.0\"?>")) {
                FileMapManager.setObtainedData(response);
            } else {
                R.getHTMLScreen().view(response);
                if (!pragmaNoCache) {
                    CacheData.add(url, response);
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ContentHandler.handle", response);
        }
    }
}

