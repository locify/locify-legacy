/*
 * XmlParser.java
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

import com.locify.client.data.CookieData;
import com.locify.client.data.FileSystem;
import com.locify.client.data.Service;
import com.locify.client.data.ServicesData;
import com.locify.client.gui.screen.service.AlertScreen;
import java.io.ByteArrayInputStream;
import de.enough.polish.util.Locale;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.Utils;
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.utils.Logger;
import com.locify.client.utils.UTF8;

/**
 * This class parses given xml and take actions
 * @author David Vavra
 */
public class XmlParser {

    KXmlParser parser;
    private String stringResponse;
    private int messagePos;
    private String syncId = "";
    private long syncTimestamp = 0;
    String title = ""; //titulek stranky, nekdy se pouziva
    private boolean updateService = false;
    private boolean autoInstall = false;

    public XmlParser() {
    }

    public boolean isUpdateService() {
        return updateService;
    }

    public void setUpdateService(boolean updateService) {
        this.updateService = updateService;
    }

    public void setAutoInstall(boolean autoInstall) {
        this.autoInstall = autoInstall;
    }

    public boolean isAutoInstall() {
        return autoInstall;
    }

    /** 
     * Parses Locify special page
     * @param response response
     * @return if page should be cached or not
     */
    public boolean parseLocifyXHTML(String response) {
        try {
            this.stringResponse = response.trim();
            messagePos = 0;
            ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(stringResponse));
            parser = new KXmlParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
            parser.setInput(bais, "utf-8");

            //header
            String redirectionUrl = "";
            int redirectionTime = -1;
            while (parser.nextTag() != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                Logger.logNoBreak("<" + name + ">");
                if (name.equals("title")) {
                    title = parser.nextText();
                }
                if (name.equals("locify:call")) {
                    String url = parser.getAttributeValue(null, "url");
                    R.getBack().dontSave();
                    R.getURL().call(url);
                    return false;
                }
                if (name.equals("meta")) //redirection
                {
                    if (parser.getAttributeValue(null, "http-equiv").equalsIgnoreCase("refresh")) {
                        String url = parser.getAttributeValue(null, "content");
                        String[] parts = StringTokenizer.getArray(url, ";url=");
                        redirectionUrl = parts[1].substring(4);
                        redirectionTime = Integer.parseInt(parts[0]);
                        if (redirectionTime != 0) {
                            R.getAutoSend().start(redirectionUrl, redirectionTime);
                        }
                    }
                    parser.nextText();
                }
                if (name.equals("body")) {
                    if (redirectionTime == 0) {
                        R.getURL().call(redirectionUrl);
                        return false;
                    } else {
                        break;
                    }
                }
            }

            //body
            String messageType;
            try {
                messageType = parser.getAttributeValue(null, "class");
            } catch (Exception e) {
                R.getCustomAlert().quickView(Locale.get("Not_valid_Locify_XHTML"), "Error", "locify://back");
                return false;
            }

            //pripadne preskoceni divu
            parser.nextTag();
            if (parser.getName().equals("div")) {
                parser.nextTag();
            }
            //rozhodnuti o typu parsovani
            if (messageType.equals("alert")) {
                parseAlert();
                return false;
            }
            if (messageType.equals("serviceInfo")) {
                parseServiceInfo();
                return false;
            }
            if (messageType.equals("list")) {
                parseList();
                return true;
            }
            if (messageType.equals("confirmation")) {
                parseConfirmation();
                return false;
            }
            if (messageType.equals("update")) {
                parseUpdate();
                return false;
            }
            return false;

        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.parseLocifyXHTML", stringResponse);
            return false;
        }

    }

    /** 
     * Parses xml tag "list"
     */
    void parseList() {
        try {
            R.getCustomList().setTitle(title);
            String iconUrl = null;
            do {
                String name = parser.getName();
                int type = parser.getEventType();

                if (type == XmlPullParser.START_TAG) {
                    Logger.logNoBreak("<" + name + ">");
                    if (name.equals("li")) {
                        iconUrl = null;
                    }
                    if (name.equals("img")) {
                        iconUrl = parser.getAttributeValue(null, "src");
                    }
                    if (name.equals("a")) {
                        String itemUrl = parser.getAttributeValue(null, "href");
                        String itemTitle = parser.nextText();
                        R.getCustomList().addMenuItem(itemTitle, itemUrl, iconUrl);
                    }
                }
            } while (parser.nextTag() != KXmlParser.END_TAG || !parser.getName().equals("ul"));
            //provedeni akce
            R.getCustomList().view(R.getHttp().getLastUrl());
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.parseList", null);
        }
    }

    /** 
     * Parses xml tag "alert"
     */
    void parseAlert() {
        try {
            AlertScreen alert = R.getCustomAlert();
            alert.reset();
            alert.setType(title);

            do {
                String name = parser.getName();

                Logger.logNoBreak("<" + name + ">");

                if (name.equals("p")) {
                    alert.setText(parser.nextText());
                } else if (name.equals("a")) {
                    alert.setNext(parser.getAttributeValue(null, "href"));
                    parser.nextText();
                } else if (name.equals("locify:timeout")) {
                    alert.setTimeout(parser.getAttributeValue(null, "value"));
                // parser.nextTag();
                } else {
                    parser.skipSubTree();
                }

            } while (parser.nextTag() != KXmlParser.END_TAG);
            //provedeni akce
            alert.view();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.parseAlert", null);
        }
    }

    /** 
     * Parses xml tag "serviceInfo"
     */
    void parseServiceInfo() {
        try {
            String serviceName = title;
            String serviceUrl = "";
            String serviceDesc = "";
            String serviceIcon = "";
            String serviceSettings = "";
            do {
                String name = parser.getName();
                if (name != null) {
                    String elementClass = parser.getAttributeValue(null, "class");

                    Logger.logNoBreak("<" + name + ">");

                    if (name.equals("p") && elementClass.equals("description")) {
                        serviceDesc = parser.nextText();
                    } else if (name.equals("img") && elementClass.equals("icon")) {
                        serviceIcon = parser.getAttributeValue(null, "src");
                        parser.nextText();
                    } else if (name.equals("a") && elementClass.equals("firstScreen")) {
                        serviceUrl = parser.getAttributeValue(null, "href");
                        parser.nextText();
                    } else if (name.equals("a") && elementClass.equals("settings")) {
                        serviceSettings = parser.getAttributeValue(null, "href");
                        parser.nextText();
                    } else {
                        parser.skipSubTree();
                    }
                }
            } while (parser.nextTag() != KXmlParser.END_TAG);

            //provedeni akce
            if (autoInstall) {
                Service service = new Service(R.getHttp().getLastUrl(), serviceName, serviceUrl, serviceDesc, syncTimestamp, serviceIcon, syncTimestamp, serviceSettings);
                ServicesData.add(service);
                R.getMainScreen().autoInstallNext();
            } else if (isUpdateService()) //service info stazene z netu
            {
                Service service = new Service(R.getHttp().getLastUrl(), serviceName, serviceUrl, serviceDesc, Utils.timestamp(), serviceIcon, Utils.timestamp(), serviceSettings);
                ServicesData.add(service);
                setUpdateService(false);
                R.getURL().call(serviceUrl);
            } else {
                Service service = new Service(R.getHttp().getLastUrl(), serviceName, serviceUrl, serviceDesc, Utils.timestamp(), serviceIcon, Utils.timestamp(), serviceSettings);
                R.getConfirmScreen().setService(service);
                R.getConfirmScreen().view(serviceName + ":\n" + serviceDesc + "\n\n" + Locale.get("Really_add_service"));
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.parseServiceInfo", null);
        }
    }

    /** 
     * Parses xml tag "confirmation"
     */
    public void parseConfirmation() {
        try {
            String question = "";
            String confirmUrl = "";
            do {
                String name = parser.getName();

                Logger.logNoBreak("<" + name + ">");

                if (name.equals("p")) {
                    question = parser.nextText();
                } else if (name.equals("a")) {
                    confirmUrl = parser.getAttributeValue(null, "href");
                    parser.nextText();
                } else {
                    parser.skipSubTree();
                }
            } while (parser.nextTag() != KXmlParser.END_TAG);

            //provedeni akce
            R.getConfirmScreen().setURL(confirmUrl);
            R.getConfirmScreen().view(question);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.parseConfirmation", null);
        }
    }

    /** 
     * Parses xml tag "update"
     */
    void parseUpdate() {
        try {
            String updateText = "";
            do {
                String name = parser.getName();

                Logger.logNoBreak("<" + name + ">");

                if (name.equals("p")) {
                    updateText = parser.nextText();
                } else {
                    parser.skipSubTree();
                }
            } while (parser.nextTag() != KXmlParser.END_TAG);
            //provedeni akce
            R.getUpdate().view(updateText);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.parseUpdate", null);
        }
    }

    /**
     * This method is called when data to be parsed come from sync
     * @param content
     * @param id
     * @param type
     * @param timestamp 
     */
    public void parseSyncItem(String content, String id, String type, long timestamp) {
        try {
            syncId = id;
            syncTimestamp = timestamp;
            stringResponse = content;
            messagePos = 0;
            if (type.equals("service")) {
                parseLocifyXHTML(content);
            } else {
                ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(stringResponse));
                parser = new KXmlParser();
                parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
                parser.setInput(bais, "utf-8");

                if (id.equals("locify://settings")) {
                    R.getFileSystem().saveString(FileSystem.SETTINGS_FILE, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + content);
                    Thread.sleep(500);
                    R.getSettings().loadXML();
                } else if (type.equals("settings")) {
                    R.getFileSystem().saveString(FileSystem.SERVICE_SETTINGS_FILE, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<id>" + id + "</id>" + content);
                    Thread.sleep(500);
                    ServiceSettingsData.loadXML();
                } else if (type.equals("cookies")) {
                    R.getFileSystem().saveString(FileSystem.COOKIES_FILE, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + content);
                    Thread.sleep(500);
                    CookieData.loadXML();
                } else if (type.equals("view")) {
                    R.getFileSystem().saveString(FileSystem.MAINSCREEN_FILE, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + content);
                    Thread.sleep(500);
                    R.getMainScreen().loadXML();
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.start", content + " " + id);
        }
    }

    /**
     * Gets xml as string from <element></element>
     */
    String subtreeToString(String element) {
        try {
            int pos1 = stringResponse.indexOf("<" + element + ">", messagePos);
            int pos2 = stringResponse.indexOf("</" + element + ">", pos1);
            messagePos = pos2;
            return stringResponse.substring(pos1 + element.length() + 2, pos2);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XmlParser.subtreeToString", element);
            return "";
        }
    }
}
