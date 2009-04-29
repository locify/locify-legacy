/*
 * Settings.java
 *
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
 *
 */
package com.locify.client.data;

import com.locify.client.utils.R;

import com.locify.client.locator.Location4D;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.Capabilities;
import de.enough.polish.util.Locale;
import de.enough.polish.util.base64.Base64;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordStore;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Global application settings
 * @author David Vavra
 */
public class SettingsData {

    //constants
    public static final int DEFAULT_TCP_PORT = 20175;
    public static final int ALWAYS = 0;
    public static final int NEVER = 1;
    public static final int ASK = 2;
    public static final int FORMAT_WGS84 = 0;   //E 14.94323
    public static final int FORMAT_WGS84_MIN = 1; //E 14°52.123
    public static final int FORMAT_WGS84_SEC = 2; //E 14°52'12.34
    public static final int AUTODETECT = 0;
    public static final int ON = 0;
    public static final int OFF = 1;
    public static final int REPAINT_DURING = 0;
    public static final int WAIT_UNTIL_END_OF_PANNING = 1;
    public static final int METRIC = 0;
    public static final int IMPERIAL = 1;
    public static final int REGULAR = 0;
    public static final int S60_FIX = 1;
    private static String language = "";
    public final String[] locales = {"en", "cs_CZ"};
    public final String[] languageNames = {Locale.get("English"), Locale.get("Czech")};    //settings
    private Hashtable settings;

    public SettingsData() {
    }

    public String getName() {
        return (String) settings.get("name");
    }

    public int getUnits() {
        return Integer.parseInt((String) settings.get("units"));
    }

    public String getPassword() {
        return new String(Base64.decode((String) settings.get("password")));
    }

    public String getLastDevice() {
        return (String) settings.get("lastDevice");
    }

    public Location4D getLastLocation() {
        String location = (String) settings.get("lastLocation");
        String[] parts = StringTokenizer.getArray(location, ",");
        Location4D loc = new Location4D(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), 0);
        return loc;
    }

    public int getLocationSource() {
        return Integer.parseInt((String) settings.get("locationSource"));
    }

    public String getLocationInfo() {
        return (String) settings.get("locationInfo");
    }

    public int getExternalClose() {
        String value = (String) settings.get("externalClose");
        return Integer.parseInt(value);
    }

    public int getPrefferedGps() {
        String value = (String) settings.get("prefferedGps");
        return Integer.parseInt(value);
    }

    public int getAutoLogin() {
        String value = (String) settings.get("autoLogin");
        return Integer.parseInt(value);
    }

    public int getCoordsFormat() {
        String value = (String) settings.get("coordsFormat");
        return Integer.parseInt(value);
    }

    public int getDefaultMapProvider() {
        String value = (String) settings.get("defaultMapProvider");
        if (value.startsWith("f")) {
            return 100;
        } else {
            return Integer.parseInt(value);
        }
    }

    public boolean isDefaultMapProviderOnline() {
        if (((String) settings.get("defaultMapProvider")).startsWith("f")) {
            return false;
        } else {
            return true;
        }
    }

    public int getTcpPort() {
        return DEFAULT_TCP_PORT;
    }

    public int getAutoload() {
        return Integer.parseInt((String) settings.get("autoload"));
    }

    public int getCacheSize() {
        return Integer.parseInt((String) settings.get("cacheSize"));
    }

    public int getPanning() {
        return Integer.parseInt((String) settings.get("panning"));
    }

    public int getMapLoading() {
        return Integer.parseInt((String) settings.get("mapLoading"));
    }

    public boolean getShowIconsHelp() {
        return (settings.get("showIconsHelp").equals("1"));
    }

    public void setLastDevice(String lastDevice) {
        settings.put("lastDevice", lastDevice);
        saveXML();
    }

    public void setLastLocation(Location4D lastLocation, int source, String info) {
        settings.put("lastLocation", lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        settings.put("locationSource", String.valueOf(source));
        settings.put("locationInfo", info);
        saveXML();
    }

    public static String getLanguage() {
        return language;
    }

    public int getSelectedLanguage() {
        int selectedLanguage = 0;
        for (int i = 0; i < locales.length; i++) {
            if (locales[i].equals(language)) {
                selectedLanguage = i;
            }
        }
        return selectedLanguage;
    }

    public static void setLanguage(String language) {
        SettingsData.language = language;
    }

    /**
     * Creates default values or loads existing settings
     */
    public void load() {
        try {
            R.getLoading().setText(Locale.get("Loading_settings"));
            //default settings
            settings = new Hashtable();
            settings.put("name", "");
            settings.put("password", "");
            settings.put("lastDevice", "");
            //easter egg - let's set the initial location to one of locify developers' home
            Random rand = new Random();
            int magicNumber = rand.nextInt(3);
            switch (magicNumber) {
                case 0:
                    //destil
                    settings.put("lastLocation", "49.910850,14.223450");
                    break;
                case 1:
                    //menion
                    settings.put("lastLocation", "50.051614,14.459853");
                    break;
                case 2:
                    //fabian
                    settings.put("lastLocation", "49.949903,14.315392");
                    break;
                default:
                    settings.put("lastLocation", "0,0");
                    break;
            }
            //only for CATCH&RUN testing:
           // settings.put("lastLocation", "50.102226,14.392229");
            settings.put("locationSource", "4");
            settings.put("locationInfo", Locale.get("Unknown_location"));
            settings.put("externalClose", String.valueOf(ASK));
            settings.put("coordsFormat", String.valueOf(FORMAT_WGS84_MIN));
            settings.put("autoLogin", String.valueOf(OFF));
            settings.put("prefferedGps", String.valueOf(AUTODETECT));
            settings.put("defaultMapProvider", "0"); //online google maps
            if (Capabilities.isWindowsMobile()) {
                settings.put("cacheSize", "330"); //kB
            } else {
                //#if applet
//#                 settings.put("cacheSize", "1024");
                //#else
                settings.put("cacheSize", "512"); //kB
            //#endif
            }
            //#if applet
//#             settings.put("panning", String.valueOf(REPAINT_DURING));
            //#else
            settings.put("panning", String.valueOf(WAIT_UNTIL_END_OF_PANNING));
            //#endif
            settings.put("showIconsHelp", "1");
            settings.put("autoload", String.valueOf(OFF));
            settings.put("units", String.valueOf(METRIC));
            if (Capabilities.isNokia()) {
                settings.put("mapLoading", String.valueOf(S60_FIX));
            } else {
                settings.put("mapLoading", String.valueOf(REGULAR));
            }

            if (!R.getFileSystem().exists(FileSystem.SETTINGS_FILE)) {
                saveXML();
            } else {
                //other application starts
                loadXML();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "SettingsData.load", null);
        }
    }

    public void saveLocationSettings(int prefferedGps, int coordinatesFormat) {
        settings.put("prefferedGps", String.valueOf(prefferedGps));
        settings.put("coordsFormat", String.valueOf(coordinatesFormat));
        saveXML();
        R.getCustomAlert().quickView(Locale.get("Settings_saved"), Locale.get("Info"), "locify://refresh");
    }

    public void saveInterfaceSettings(int selectedLanguage, int units) {
        try {
            language = locales[selectedLanguage];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(R.getFileSystem().getRoot());
            dos.writeUTF(language);
            byte[] data = baos.toByteArray();
            dos.close();
            RecordStore recordStore = RecordStore.openRecordStore("locify", true);
            recordStore.setRecord(1, data, 0, data.length);
            recordStore.closeRecordStore();

            settings.put("units", String.valueOf(units));
            saveXML();
            R.getCustomAlert().quickView(Locale.get("Restart_needed"), Locale.get("Info"), "locify://refresh");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "SettingsData.saveInterfaceSettings", null);
        }
    }

    public void saveOtherSettings(int autoLogin, int externalClose) {
        settings.put("autoLogin", String.valueOf(autoLogin));
        settings.put("externalClose", String.valueOf(externalClose));
        saveXML();
        R.getCustomAlert().quickView(Locale.get("Settings_saved"), Locale.get("Info"), "locify://refresh");
    }

    public void setExternalClose(int externalClose) {
        settings.put("externalClose", String.valueOf(externalClose));
        saveXML();
        R.getCustomAlert().quickView(Locale.get("Settings_saved"), Locale.get("Info"), "locify://refresh");
    }

    public void saveMapsSettings(int mapProvider, boolean fileMaps) {
        if (fileMaps) {
            settings.put("defaultMapProvider", "f");
        } else {
            settings.put("defaultMapProvider", String.valueOf(mapProvider));
        }
        saveXML();
        R.getCustomAlert().quickView(Locale.get("Settings_saved"), Locale.get("Info"), "locify://refresh");
    }

    public void setAutologin(int autoLogin) {
        settings.put("autoLogin", String.valueOf(autoLogin));
        saveXML();
    }

    public void setShowIconsHelp(boolean show) {
        if (show) {
            settings.put("showIconsHelp", "1");
        } else {
            settings.put("showIconsHelp", "0");
        }
    }

    /**
     * Saves login and password to database
     * @param login
     * @param password 
     */
    public void saveLocifyCredentials(String login, String password) {
        try {
            settings.put("name", login);
            settings.put("password", Base64.encode(password));
            saveXML();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "SettingsData.saveLocifyCredentials", null);
        }
    }

    public void saveAdvancedMaps(int autoload, int cacheSize, int panning, int mapLoading) {
        settings.put("autoload", String.valueOf(autoload));
        settings.put("cacheSize", String.valueOf(cacheSize));
        settings.put("panning", String.valueOf(panning));
        settings.put("mapLoading", String.valueOf(mapLoading));
        saveXML();
        R.getCustomAlert().quickView(Locale.get("Restart_needed"), Locale.get("Info"), "locify://refresh");
    }

    public void loadXML() {
        FileConnection fileConnection = null;
        InputStream is = null;
        XmlPullParser parser;

        try {
            fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.SETTINGS_FILE);
            if (!fileConnection.exists()) {
                return;
            }

            parser = new KXmlParser();
            is = fileConnection.openInputStream();
            parser.setInput(is, "utf-8");

            String currentName = "";
            String currentValue = "";
            int event;
            String tagName;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();

                    if (tagName.equals("name")) {
                        currentName = parser.nextText();
                    } else if (tagName.equals("value")) {
                        currentValue = parser.nextText();
                        settings.put(currentName, currentValue);
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
                    if (tagName.equals("settings")) {
                        break;
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "SettingsData.loadXML", null);
        }
    }

    private void saveXML() {
        String settingsData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + getXML();
        R.getFileSystem().saveString(FileSystem.SETTINGS_FILE, settingsData);
    }

    private String getXML() {
        String settingsData = "<settings>\n";
        Enumeration keys = settings.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            settingsData += "<variable><name>" + key + "</name><value>" + settings.get(key) + "</value></variable>\n";
        }
        settingsData += "</settings>\n";
        return settingsData;
    }

    /**
     * Gets sync data
     * @return sync file
     */
    public String syncData() {
        try {
            String syncData = "<file>\n";
            syncData += "<id>locify://settings</id>\n";
            syncData += "<type>settings</type>\n";
            syncData += "<action>allSync</action>\n";
            syncData += "<ts>" + R.getFileSystem().getTimestamp(FileSystem.SETTINGS_FILE) + "</ts>\n";
            syncData += "<content>\n";
            syncData += getXML();
            syncData += "</content>\n";
            syncData += "</file>\n";
            return syncData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "SettingsData.syncData", null);
            return "";
        }
    }
}
