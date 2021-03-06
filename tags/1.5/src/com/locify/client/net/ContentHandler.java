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

import com.locify.client.data.IconData;
import com.locify.client.data.AudioData;
import com.locify.client.data.CacheData;
import com.locify.client.data.ServicesData;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.maps.NetworkLinkDownloader;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.UTF8;

/**
 * Processes incoming data and decides proper action, also manages caching
 * @author Destil
 */
public class ContentHandler {

    /**
     * Handles content from net
     * @param response
     */
    public static void handle(HttpResponse response) {
        try {
            if (response.isImage()) {
                if (response.getUrl().startsWith(ServicesData.getCurrent())) {
                    R.getCustomList().refreshIcon(response.getUrl(), response.getData());
                } else {
                    R.getMainScreen().refreshIcon(response.getUrl(), response.getData());
                }
                IconData.save(response.getUrl(), response.getData());
            } else if (response.isAudio()) {
                AudioData.save(response.getUrl(), response.getData());
            } else { //string content
                String data = UTF8.decode(response.getData(), 0, response.getData().length);
                if (data.length() == 0) {
                    Logger.log("No data");
                    return;
                }
                Logger.log("Data:");
                Logger.log(data);

                //kml
                if (data.indexOf("<kml xmlns=") != -1 && data.indexOf("<kml xmlns=") < 100) {
                    //System.out.println("kml content");
                    R.getGeoDataBrowser().setKml(data);
                    R.getBack().dontSave();
                    R.getURL().call("locify://geoFileBrowser");
                } //geocoding result
                else if (data.indexOf("<ResultSet") != -1) {
                    Geocoding.result(data);
                } //geocoding error
                else if (data.indexOf("<Error xmlns=\"urn:yahoo:api\">") != -1) {
                    Geocoding.error();
                } //special Locify xhtml
                else if (data.indexOf("<body class=\"alert\">") != -1 || data.indexOf("<body class=\"list\">") != -1 || data.indexOf("<body class=\"serviceInfo\">") != -1 || data.indexOf("<body class=\"confirmation\">") != -1 || data.indexOf("<body class=\"update\">") != -1 || data.indexOf("<meta http-equiv=\"refresh\"") != -1 || data.indexOf("<locify:call") != -1 || data.indexOf("<sync>") != -1) {
                    boolean shouldCache = R.getXmlParser().parseLocifyXHTML(data);
                    if (!response.isDisabledCaching() && shouldCache) {
                        CacheData.add(response.getUrl(), data);
                    }
                    R.getMapScreen().stopNetworkLink();
                } //some other html or form
                else if (data.startsWith("<?xml version=\"1.0\"?>")) {
                    FileMapManager.setObtainedData(data);
                } else {
                    R.getHTMLScreen().view(data);
                    if (!response.isDisabledCaching()) {
                        CacheData.add(response.getUrl(), data);
                    }
                    R.getMapScreen().stopNetworkLink();
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ContentHandler.handle", response.getUrl());
        }
    }
}

