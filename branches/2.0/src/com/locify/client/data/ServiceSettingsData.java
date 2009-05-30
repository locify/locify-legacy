/*
 * ServiceSettingsData.java
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
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import java.util.Vector;
import java.io.ByteArrayInputStream;
import org.kxml2.io.KXmlParser;

/**
 * Manages storing of service settings
 * @author Destil
 */
public class ServiceSettingsData {

    private static Vector settings;

    public ServiceSettingsData() {
    }

    /**
     * Creates default values or loads existing services
     */
    public static void load() {
        try {
            R.getLoading().setText(Locale.get("Loading_service_settings"));
            if (!R.getFileSystem().exists(FileSystem.SERVICE_SETTINGS_FILE)) {  //prvni start aplikace
                settings = new Vector();
                saveXML();
            } else {
                loadXML();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServiceSettingsData.load", null);
        }
    }

    /**
     * Adds new setting or edit currrent
     * @param name name of setting
     * @param value value of setting
     * @param service service id
     */
    public static void addEdit(String name, String value, String service) {
        try {
            boolean found = false;
            value = Utils.replaceString(value, "&", "&amp;");
            for (int i = 0; i < settings.size(); i++) {
                ServiceSetting setting = (ServiceSetting) settings.elementAt(i);
                if (setting.getName().equals(name) && setting.getService().equals(service)) {
                    setting.setValue(value);
                    Logger.log("Updating setting " + name + " to value " + value + " of service " + service);
                    settings.setElementAt(setting, i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Logger.log("Adding setting " + name + " with value " + value + " of service " + service);
                settings.addElement(new ServiceSetting(name, value, service));
            }
            ServicesData.setSettingsTimestamp(service, Utils.timestamp());
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServiceSettingsData.addEdit", name);
        }
    }

    public static void delete(String service) {
        for (int i = 0; i < settings.size(); i++) {
            ServiceSetting setting = (ServiceSetting) settings.elementAt(i);
            if (setting.getService().equals(service)) {
                settings.removeElementAt(i);
                i--;
            }
        }
    }

    /**
     * Returns value of desired setting name
     * @param name name of setting
     * @param service service id
     * @return value of setting
     */
    public static String getValue(String name, String service) {
        for (int i = 0; i < settings.size(); i++) {
            ServiceSetting setting = (ServiceSetting) settings.elementAt(i);
            if (setting.getName().equals(name) && setting.getService().equals(service)) {
                String value = Utils.replaceString(setting.getValue(), "&amp;", "&");
                return value;
            }
        }
        return "";
    }

    /**
     * Saves data from form to service settings
     * @param data post data
     */
    public static void saveFromForm(String data) {
        try {
            //rozparsovani post dat
            String[] variables = StringTokenizer.getArray(data, "&");
            for (int i = 0; i < variables.length; i++) {
                String[] variable = StringTokenizer.getArray(variables[i], "=");
                addEdit(variable[0], variable[1], ServicesData.getCurrent());
            }
            saveXML();
            //navrat zpet
            R.getBack().goBack();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServiceSettingsData.saveFromForm", data);
        }
    }

    /**
     * Replace settings variables in given string
     * @param text text to be replaced
     * @return replace text
     */
    public static String replaceVariables(String text) {
        try {
            int start = 0;
            while ((start = text.indexOf("$settings[", start)) != -1) {
                int end = text.indexOf("]", start);
                String settingName = text.substring(start + 10, end);
                boolean replaced = false;
                for (int i = 0; i < settings.size(); i++) {
                    ServiceSetting setting = (ServiceSetting) settings.elementAt(i);
                    if (setting.getName().equals(settingName) && setting.getService().equals(ServicesData.getCurrent())) {
                        replaced = true;
                        text = Utils.replaceString(text, text.substring(start, end + 1), setting.getValue());
                    }
                }
                //if setting is not present, replace with blank
                if (!replaced) {
                    text = Utils.replaceString(text, text.substring(start, end + 1), "");
                }
            }
            return text;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServiceSettingsData.replaceVariables", text);
            return text;
        }
    }

    /**
     * Gets sync data for services and its settings
     * @return sync data
     */
    public static String getXML() {
        try {
            String syncData = "";
            //find out which services have settings
            Vector servicesWithSettings = new Vector();
            if (servicesWithSettings != null && settings != null) {
                for (int i = 0; i < settings.size(); i++) {
                    ServiceSetting setting = (ServiceSetting) settings.elementAt(i);
                    if (!servicesWithSettings.contains(setting.getService())) {
                        servicesWithSettings.addElement(setting.getService());
                    }
                }
                //print out settings of each service
                for (int i = 0; i < servicesWithSettings.size(); i++) {
                    String service = (String) servicesWithSettings.elementAt(i);
                    if (!service.equals("Locify")) {
                        syncData += "<file>\n";
                        syncData += "<id>" + service + "</id>\n";
                        syncData += "<type>settings</type>\n";
                        syncData += "<action>allSync</action>\n";
                        syncData += "<ts>" + ServicesData.getService(service).getSettingsTimestamp() + "</ts>\n";
                        syncData += "<content>\n";
                        syncData += "<settings>\n";
                        for (int j = 0; j < settings.size(); j++) {
                            ServiceSetting setting = (ServiceSetting) settings.elementAt(j);
                            if (setting.getService().equals(service)) {
                                syncData += "<variable><name>" + setting.getName() + "</name><value>" + setting.getValue() + "</value></variable>\n";
                            }
                        }
                        syncData += "</settings>\n";
                        syncData += "</content>\n";
                        syncData += "</file>\n";
                    }
                }
            }

            return syncData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServiceSettingsData.getXml", null);
            return "";
        }
    }

    public static void saveXML() {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + getXML();
        R.getFileSystem().saveString(FileSystem.SERVICE_SETTINGS_FILE, data);
    }

    public static void loadXML() {
        try {
            settings = new Vector();
            String data = R.getFileSystem().loadString(FileSystem.SERVICE_SETTINGS_FILE);
            ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(data));
            bais.reset();
            KXmlParser parser = new KXmlParser();
            //parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
            parser.setInput(bais, "utf-8");
            String name = "";
            String value = "";
            String service = "";
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
                    service = parser.nextText();
                } else if (tagName.equals("name")) {
                    name = parser.nextText();
                } else if (tagName.equals("value")) {
                    value = parser.nextText();
                    //zpracovani
                    settings.addElement(new ServiceSetting(name, value, service));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ServiceSettingsData.loadXML", null);
        }
    }
}
