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
import de.enough.polish.io.Serializable;
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
    private static String language = "";
    public final String[] locales = {"en", "cs_CZ"};
    public final String[] languageNames = {Locale.get("English"), Locale.get("Czech")};    //settings
    private Hashtable settings;

    public SettingsData() {
    }

    public String getName() {
        return (String) settings.get("name");
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
            return Integer.parseInt(value.substring(1));
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
            settings.put("locationSource", "4");
            settings.put("locationInfo", "");
            settings.put("externalClose", String.valueOf(ASK));
            settings.put("coordsFormat", String.valueOf(FORMAT_WGS84_MIN));
            settings.put("autoLogin", String.valueOf(OFF));
            settings.put("prefferedGps", String.valueOf(AUTODETECT));
            settings.put("defaultMapProvider", "0"); //online google maps
            settings.put("showScale", "1");

            if (!R.getFileSystem().exists(FileSystem.SETTINGS_FILE)) {
                //imported settings from 0.8.1
                if (R.getFileSystem().exists(FileSystem.SETTINGS_FOLDER + "mainSettings.lcf")) {
                    SettingsObject settingsObject = (SettingsObject) R.getFileSystem().loadObject(FileSystem.SETTINGS_FOLDER + "mainSettings.lcf");
                    settings.put("name", settingsObject.getName());
                    settings.put("password", Base64.encode(settingsObject.getPassword()));
                    settings.put("lastDevice", settingsObject.getLastDevice());
                    settings.put("lastLocation", settingsObject.getLastLatitude() + "," + settingsObject.getLastLongitude());
                    settings.put("externalClose", String.valueOf(settingsObject.getExternalClose()));
                    settings.put("coordsFormat", String.valueOf(settingsObject.getCoordsFormat()));
                    settings.put("autoLogin", String.valueOf(settingsObject.getAutoLogin()));
                    settings.put("prefferedGps", String.valueOf(settingsObject.getPrefferedGps()));
                    R.getFileSystem().delete(FileSystem.SETTINGS_FOLDER + "mainSettings.lcf");
                }
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

    public void saveInterfaceSettings(int selectedLanguage) {
        try {
            language = locales[selectedLanguage];
            Locale.loadTranslations("/" + language + ".loc");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(R.getFileSystem().getRoot());
            dos.writeUTF(language);
            byte[] data = baos.toByteArray();
            dos.close();
            RecordStore recordStore = RecordStore.openRecordStore("locify", true);
            recordStore.setRecord(1, data, 0, data.length);
            recordStore.closeRecordStore();
            R.getCustomAlert().quickView(Locale.get("Changed_language_after_restart"), Locale.get("Info"), "locify://refresh");
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
            settings.put("defaultMapProvider", "f" + String.valueOf(mapProvider));
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

class SettingsObject implements Serializable {

    private String name;
    private String password;
    private String lastDevice;
    private double lastLatitude;
    private double lastLongitude;
    private int externalClose;
    private int prefferedGps;
    private int coordsFormat; //format of coordinate
    private int autoLogin;
    private int connectGps;

    public SettingsObject(String name, String password, String lastDevice,
            double lastLatitude, double lastLongitude,
            int externalClose, int autoConnectGps, int coordsFormat, int autoLogin, int connectGps) {
        this.name = name;
        this.password = password;
        this.lastDevice = lastDevice;
        this.lastLatitude = lastLatitude;
        this.lastLongitude = lastLongitude;
        this.externalClose = externalClose;
        this.prefferedGps = autoConnectGps;
        this.coordsFormat = coordsFormat;
        this.autoLogin = autoLogin;
        this.connectGps = connectGps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastDevice() {
        return lastDevice;
    }

    public void setLastDevice(String lastDevice) {
        this.lastDevice = lastDevice;
    }

    public double getLastLatitude() {
        return lastLatitude;
    }

    public void setLastLatitude(double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public double getLastLongitude() {
        return lastLongitude;
    }

    public void setLastLongitude(double lastLongitude) {
        this.lastLongitude = lastLongitude;
    }

    public int getExternalClose() {
        return externalClose;
    }

    public void setExternalClose(int externalClose) {
        this.externalClose = externalClose;
    }

    public int getPrefferedGps() {
        return prefferedGps;
    }

    public void setPrefferedGps(int prefferedGps) {
        this.prefferedGps = prefferedGps;
    }

    public int getCoordsFormat() {
        return coordsFormat;
    }

    public void setCoordsFormat(int coordsFormat) {
        this.coordsFormat = coordsFormat;
    }

    public void setAutoLogin(int autoLogin) {
        this.autoLogin = autoLogin;
    }

    public int getAutoLogin() {
        return autoLogin;
    }

    public int getConnectGps() {
        return connectGps;
    }

    public void setConnectGps(int connectGps) {
        this.connectGps = connectGps;
    }
}
