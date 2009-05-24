/*
 * InternalURLManager.java
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
package com.locify.client.gui.manager;

import com.locify.client.data.*;
import com.locify.client.data.items.*;
import com.locify.client.gui.screen.internal.MainScreen;
import com.locify.client.utils.R;
import com.locify.client.net.Http;
import com.locify.client.net.Sms;
import com.locify.client.data.Service;
import com.locify.client.data.ServicesData;
import com.locify.client.utils.Utils;
import com.locify.client.utils.Logger;
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.net.Variables;
import com.locify.client.utils.StringTokenizer;
import de.enough.polish.util.HashMap;

/**
 * Defines internal URLs and actions linked with them
 * @author Destil
 */
public class InternalURLManager {

    private HashMap parameters;

    /**
     * Internal urls definition
     */
    public InternalURLManager() {

    }

    /**
     * This method defines all internal url's and actions associated with them
     * @param url url of the shortcut
     */
    public void call(String url) {
        try {
            Logger.log("URL called: " + url);
            //absolute urls
            url = R.getHttp().makeAbsoluteURL(url);
            //support for http://in.locify.com/ internal urls
            url = Utils.replaceString(url, "http://in.locify.com/", "locify://");
            //add url to backscreens
            R.getBack().goForward(url, R.getPostData().getUrlEncoded());
            //replace variables in URL
            url = Variables.replace(url, true);
            //extract parameters
            if (!url.startsWith("http://")) {
                parameters = extractParameters(url);
            }

            //----- internal urls action definition-----
            MainScreen ms = R.getMainScreen();
            if (url.startsWith("http://")) {
                //download page from net
                R.getHttp().start(url,Http.SERVICE);
            } else if (url.equals("locify://mainScreen")) {
                //display main screen
                R.getMainScreen().view();
            } else if (url.equals("locify://login")) {
                //login
                R.getAuthentication().setNext("locify://back");
                R.getAuthentication().start("locify://authentication");
            } else if (url.startsWith("locify://navigation")) {
                if (parameters == null) {
                    R.getNavigationScreen().view();
                } else if (parameters.containsKey("lat")) {
                    try {
                        R.getNavigationScreen().view(Double.parseDouble((String) parameters.get("lat")), Double.parseDouble((String) parameters.get("lon")), (String) parameters.get("name"));
                    } catch (NumberFormatException e) {
                    }
                } else if (parameters.containsKey("file")) {
                    R.getNavigationScreen().view((String) parameters.get("file"));
                } else if (parameters.containsKey("id"))
                {
                    R.getNavigationScreen().view((String)parameters.get("id"), true);
                }
            } else if (url.startsWith("locify://maps")) {
                R.getMapScreen().view();
                if (parameters != null && parameters.containsKey("lat")) {
                    R.getMapScreen().view(Double.parseDouble((String) parameters.get("lat")), Double.parseDouble((String) parameters.get("lon")), (String) parameters.get("name"), (String) parameters.get("desc"));
                } else if (parameters != null && parameters.containsKey("file")) {
                    R.getMapScreen().view((String) parameters.get("file"));
                }
            } else if (url.equals("locify://recordRoute")) {
                R.getRouteScreen().view();
            } else if (url.equals("locify://satellites")) {
                R.getSatelliteScreen().view();
            } else if (url.startsWith("locify://geoFileBrowser")) {
                if (parameters == null) {
                    R.getGeoDataBrowser().view();
                } else {
                    R.getGeoDataBrowser().view((String) parameters.get("file"));
                }
            } else if (url.equals("locify://saveRoute")) {
                R.getRouteSaveScreen().viewLastSaveScreen();
            } else if (url.equals("locify://addService")) {
                //display server service list
                ServicesData.setCurrent("Locify");
                R.getHttp().start(Http.DEFAULT_URL + "services/list",Http.SERVICE);
            } else if (url.equals("locify://addServiceByLink")) {
                //display form to add url manually
                R.getServiceManager().viewAddByLink();
            } else if (url.startsWith("locify://files")) {
                if (url.equals("locify://filesystem/waypoint")) {
                    //create waypoint from values from form
                    GeoFiles.saveWaypoint(Double.parseDouble(R.getPostData().get("latitude")), Double.parseDouble(R.getPostData().get("longitude")), R.getPostData().get("name"), R.getPostData().get("description"));
                } else {
                    if (parameters == null) {
                        //display filesystem
                        R.getFileSystemScreen().view();
                    } else {
                        R.getFileSystemScreen().view((String) parameters.get("filter"), (String) parameters.get("to"));
                    }
                }
            } else if (url.equals("locify://settings")) {
                //display settings
                R.getSettingsScreen().view();
            } else if (url.equals("locify://settings/location")) {
                //view location settings
                R.getSettingsScreen().viewLocationSettings();
            } else if (url.equals("locify://settings/interface")) {
                //view interface settings
                R.getSettingsScreen().viewInterfaceSettings();
            } else if (url.equals("locify://settings/maps")) {
                //view interface settings
                R.getSettingsScreen().viewMapSettings();
            } else if (url.equals("locify://settings/other")) {
                //view other settings
                R.getSettingsScreen().viewOtherSettings();
            } else if (url.equals("locify://synchronize")) {
                //parseSyncItem synchronization
                R.getSync().sendSync();
            } else if (url.equals("locify://checkVersion")) {
                //display current version and check it
                R.getUpdate().checkVersion();
            } else if (url.startsWith("locify://help")) {
                //view help
                if (parameters == null) {
                    R.getHelp().viewMenu();
                } else {
                    R.getHelp().viewText(Integer.parseInt((String) parameters.get("text")));
                }
            } else if (url.equals("locify://exit")) {
                //exit midlet
                R.getMidlet().exitMIDlet();
            } else if (url.equals("locify://serviceInfo")) {
                //display information about service
                R.getServiceManager().viewServiceInfo(ServicesData.getService(ms.getFocusedItem().getId()));
            } else if (url.equals("locify://updateService")) {
                CacheData.deleteAll(R.getMainScreen().getFocusedItem().getId());
                ServicesData.setCurrent(R.getMainScreen().getFocusedItem().getId());
                R.getXmlParser().setUpdateService(true);
                R.getHttp().start(R.getMainScreen().getFocusedItem().getId(),Http.SERVICE);
            } else if (url.equals("locify://serviceSettings")) {
                if (R.getPostData().getUrlEncoded() != null) {
                    //saves values sent to service settings
                    ServiceSettingsData.saveFromForm(R.getPostData().getUrlEncoded());
                } else {
                    //display service settings
                    Service service = ServicesData.getService(ms.getFocusedItem().getId());
                    R.getHttp().start(service.getSettingsUrl(),Http.SERVICE);
                }
            } else if (url.equals("locify://renameItem")) {
                //shows rename item screen
                R.getServiceManager().viewRenameService(ms.getFocusedItem().getTitle());
            } else if (url.equals("locify://selectBTDevice")) {
                //parseSyncItem to connecting to selected BT device
                R.getBluetoothManager().searchForServices();
            } else if (url.equals("locify://register")) {
                //display registration form
                R.getAuthentication().viewRegister();
            } else if (url.equals("locify://logger")) {
                //view one selected helptext
                R.getLoggerScreen().view();
            } else if (url.equals("locify://coordinates")) {
                //view form for entering own coordinates
                R.getLocationScreens().viewCoordinatesScreen();
            } else if (url.equals("locify://gps")) {
                //view form for entering own coordinates
                R.getLocator().connectGps();
            } else if (url.equals("locify://lastKnown")) {
                //sets last known coordinates as location context
                R.getContext().loadLastKnown();
            } else if (url.equals("locify://address")) {
                //view form for selecting waypoint as source of coordinates
                R.getLocationScreens().viewAddressScreen();
            } else if (url.equals("locify://addressSelection")) {
                //view confirmation of geocoded location or list with locations
                R.getLocationScreens().viewAddressSelection();
            } else if (url.equals("locify://externalBrowserOptions")) {
                //display options what to do with midlet while external browser is called
                R.getExternalBrowser().viewCloseAppScreen();
            } else if (url.equals("locify://back")) {
                //returns to previous screen
                R.getBack().goBack();
            } else if (url.equals("locify://backBack"))
            {
                R.getMapScreen().stopNetworkLink();
                R.getBack().goBack(2);
            } else if (url.equals("locify://refresh")) {
                //repeats last url
                R.getBack().repeat();
            } else if (url.equals("locify://errorDetail")) {
                //displays waypoint browser
                R.getErrorScreen().viewDetail();
            } else if (url.startsWith("locify://external/")) {
                //opens page in external browser
                url = Utils.replaceString(url, "locify://external/", "");
                R.getExternalBrowser().open(url);
            } else if (url.startsWith("locify://setLocation")) {
                //displays selection of location context
                if (url.endsWith("GPS")) {
                    R.getLocationScreens().askForContextGPSOnly();
                } else {
                    R.getLocationScreens().askForContext();
                }
            } else if (url.equals("locify://htmlBrowser")) {
                R.getHTMLScreen().view();
            } else if (url.startsWith("locify://contactsScreen")) {
                R.getContactsScreen().viewFiltered((String) parameters.get("text"),
                        Integer.parseInt((String) parameters.get("type")));
            } else if (url.startsWith("locify://internal/")) {
                //opens page in internal browser
                url = Utils.replaceString(url, "locify://internal/", "");
                R.getURL().call(CookieData.getUrlData(url));
            } else if (url.equals("locify://savePlace")) {
                R.getSavePlace().view();
            } else if (url.startsWith("locify://filebrowser")) {
                //file browser for input type file
                if (parameters.get("type") != null) {
                    R.getFileBrowser().start((String) parameters.get("type"));
                } else if (parameters.get("folder") != null) {
                    R.getFileBrowser().view((String) parameters.get("folder"));
                }
            } else if (url.startsWith("locify://sms")) {
                //sms sending
                Sms.send((String) parameters.get("number"), (String) parameters.get("text"));
            } else if (url.startsWith("locify://mapNavigation")) {
                R.getBack().deleteLast();
                R.getMapScreen().startMapNavigation(R.getMapItemManager().getWaypointById((String)parameters.get("id")));
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "InternalURLManager.call", url);
        }
    }

    /**
     * Create map of parameters from some URL
     * @param url
     * @return map of parameters
     */
    public HashMap extractParameters(String url) {
        if (url.indexOf("?") == -1) {
            return null;
        }
        HashMap map = new HashMap();
        String[] parts = StringTokenizer.getArray(url, "?");
        if (parts[1] == null || parts[1].equals("")) {
            return null;
        }
        parts[1] = Utils.replaceString(parts[1], "&amp;", "<amp>");
        String[] items = StringTokenizer.getArray(parts[1], "&");
        for (int i = 0; i < items.length; i++) {
            String[] namevalue = StringTokenizer.getArray(items[i], "=");
            map.put(Utils.urlUTF8decode(namevalue[0]), Utils.urlUTF8decode(Utils.replaceString(namevalue[1],"<amp>","&")));
        }
        return map;
    }
}
