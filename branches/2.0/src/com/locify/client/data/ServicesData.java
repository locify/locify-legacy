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

import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import java.util.Vector;
import java.io.ByteArrayInputStream;
import org.kxml2.io.KXmlParser;

/**
 * Manages services
 *  
 * @author David Vavra
 */
public class ServicesData {

    private static String currentService = "Locify";
    private static Vector services;

    public static String getCurrent() {
        return currentService;
    }

    public static void setCurrent(String id) {
        currentService = id;
    }

    /**
     * Creates default values or loads existing services
     */
    public static void load() {
        try {
            R.getLoading().setText(Locale.get("Loading_services"));
            if (!R.getFileSystem().exists(FileSystem.SERVICES_FILE)) {  //prvni start aplikace
                services = new Vector();
                saveXML();
            } else {
                loadXML();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.load", null);
        }
    }

    /**
     * Adds new service to database
     * @param service service data
     */
    public static void add(Service service) {
        try {
            boolean found = false;
            for (int i = 0; i < services.size(); i++) {
                Service service2 = (Service) services.elementAt(i);
                if (service2.getId().equals(service.getId())) {
                    found = true;
                    services.setElementAt(service, i);
                }
            }
            if (!found) {
                services.addElement(service);
            }
            saveXML();
            R.getMainScreen().addEdit(service);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.add", null);
        }
    }

    /**
     * Deletes specified service
     * @param id id of service
     */
    public static void delete(String id) {
        try {
            for (int i = 0; i < services.size(); i++) {
                Service service = (Service) services.elementAt(i);
                if (service.getId().equals(id)) {
                    DeletedData.add(id, "service");
                    //R.getIcons().delete(service.getIcon());
                    services.removeElementAt(i);
                    ServiceSettingsData.delete(id);
                    break;
                }
            }
            saveXML();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.delete", id);
        }
    }

    /**
     * Deletes all services - used in sync
     */
    public static void deleteAll() {
        try {
            services.removeAllElements();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.deleteAll", null);
        }
    }

    /**  
     * Returns service
     * @param id id of service
     * @return service object
     */
    public static Service getService(String id) {
        try {
            for (int i = 0; i < services.size(); i++) {
                Service service = (Service) services.elementAt(i);
                if (service.getId().equals(id)) {
                    return service;
                }
            }
            return null;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.getService", id);
            return null;
        }
    }

    /**
     * Sets settings timestamp - used in sync
     * @param id id of service
     * @param settingsTimestamp new settings timestamp 
     */
    public static void setSettingsTimestamp(String id, long settingsTimestamp) {
        try {
            for (int i = 0; i < services.size(); i++) {
                Service service = (Service) services.elementAt(i);
                if (service.getId().equals(id)) {
                    service.setSettingsTimestamp(settingsTimestamp);
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.setSettingsTimestamp", id);
        }
    }

    /**
     * Gets sync data for services and its settings
     * @return sync data
     */
    public static String syncData() {
        try {
            String syncData = "";
            for (int i = 0; i < services.size(); i++) {
                Service service = (Service) services.elementAt(i);
                syncData += "<file>\n";
                syncData += "<id>" + service.getId() + "</id>\n";
                syncData += "<type>service</type>\n";
                syncData += "<action>allSync</action>\n";
                syncData += "<ts>" + service.getTimestamp() + "</ts>\n";
                syncData += "<content>\n";
                syncData += "<html>\n";
                syncData += "<head>\n";
                syncData += " <title>" + service.getName() + "</title>\n";
                syncData += "</head>\n";
                syncData += "<body class=\"serviceInfo\">\n";
                syncData += "<div>\n";
                syncData += "<p class=\"description\">" + service.getDescription() + "</p> \n";
                syncData += " <img src=\"" + service.getIcon() + "\" class=\"icon\" alt=\"\" />\n";
                syncData += "  <a href=\"" + service.getFirstScreenUrl() + "\" class=\"firstScreen\">firstScreen</a>\n";
                syncData += "  <a href=\"" + service.getSettingsUrl() + "\" class=\"settings\">settings</a>\n";
                syncData += " </div>\n";
                syncData += "</body>\n";
                syncData += "</html>\n";
                syncData += "</content>\n";
                syncData += "</file>\n";
            }
            return syncData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.syncData", null);
            return "";
        }
    }

    private static void saveXML() {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        data += "<services>\n";
        for (int i = 0; i < services.size(); i++) {
            Service service = (Service) services.elementAt(i);
            data += "  <service>\n";
            data += "    <id>" + service.getId() + "</id>\n";
            data += "    <url>" + service.getFirstScreenUrl() + "</url>\n";
            data += "    <icon>" + service.getIcon() + "</icon>\n";
            data += "    <name>" + Utils.replaceString(service.getName(), "&", "&amp;") + "</name>\n";
            data += "    <desc>" + Utils.replaceString(service.getDescription(), "&", "&amp;") + "</desc>\n";
            data += "    <settings>" + service.getSettingsUrl() + "</settings>\n";
            data += "    <ts>" + service.getTimestamp() + "</ts>\n";
            data += "    <settingsts>" + service.getSettingsTimestamp() + "</settingsts>\n";
            data += "  </service>\n";
        }
        data += "</services>";
        R.getFileSystem().saveString(FileSystem.SERVICES_FILE, data);
    }

    private static void loadXML() {
        try {
            services = new Vector();
            String data = R.getFileSystem().loadString(FileSystem.SERVICES_FILE);
            ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(data));
            bais.reset();
            KXmlParser parser = new KXmlParser();
            //parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
            parser.setInput(bais, "utf-8");
            String id = "";
            String url = "";
            String icon = "";
            String name = "";
            String desc = "";
            String settings = "";
            String ts = "";
            String settingsts = "";
            while (true) {
                int parserType = parser.next();
                if (parserType == KXmlParser.END_DOCUMENT) {
                    break;
                }
                if (parserType != KXmlParser.START_TAG) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName == null) {
                    // nothing to do ... empty tag
                } else if (tagName.equals("id")) {
                    id = parser.nextText();
                } else if (tagName.equals("url")) {
                    url = parser.nextText();
                } else if (tagName.equals("icon")) {
                    icon = parser.nextText();
                } else if (tagName.equals("name")) {
                    name = parser.nextText();
                } else if (tagName.equals("desc")) {
                    desc = parser.nextText();
                } else if (tagName.equals("settings")) {
                    settings = parser.nextText();
                } else if (tagName.equals("ts")) {
                    ts = parser.nextText();
                } else if (tagName.equals("settingsts")) {
                    settingsts = parser.nextText();
                    //zpracovani
                    services.addElement(new Service(id, name, url, desc, Long.parseLong(ts), icon, Long.parseLong(settingsts), settings));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServicesData.loadXML", null);
        }
    }
}
