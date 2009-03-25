/*
 * Deleted.java
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
import java.util.Vector;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.Utils;
import de.enough.polish.util.Locale;
import java.io.ByteArrayInputStream;
import com.locify.client.utils.UTF8;
import org.kxml2.io.KXmlParser;

/**
 * Stores cookies
 * 
 * <b>Service database structure:</b>
 * String name
 * String value
 * String domain
 * long expires
 * 
 * @author David Vavra
 */
public class CookieData {

    private static Vector cookies;

    /**
     * Creates default values or loads existing cookies
     */
    public static void load() {
        try {
            R.getLoading().setText(Locale.get("Loading_cookies"));
            if (!R.getFileSystem().exists(FileSystem.COOKIES_FILE)) {  //prvni start aplikace
                cookies = new Vector();
                saveXML();
            } else {
                loadXML();
                checkExpiration();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.load", null);
        }
    }

    /**
     * Adds new cookie, edits in case there is already a cookie with this name and domain
     * @param cookieString standart cookie string from http header
     */
    public static void addEditDelete(String cookieString) {
        try {
            //odstraneni posledniho ; z cookie
            cookieString = Utils.removeLast(cookieString, ';');
            //some web servers send cookies as Set-cookie: cookie1, cookie2;
            //first replace , in dates to _ because , is needed for parsing
            cookieString = Utils.replaceString(cookieString, "Mon, ", "Mon_ ");
            cookieString = Utils.replaceString(cookieString, "Tue, ", "Tue_ ");
            cookieString = Utils.replaceString(cookieString, "Wed, ", "Wed_ ");
            cookieString = Utils.replaceString(cookieString, "Thu, ", "Thu_ ");
            cookieString = Utils.replaceString(cookieString, "Fri, ", "Fri_ ");
            cookieString = Utils.replaceString(cookieString, "Sat, ", "Sat_ ");
            cookieString = Utils.replaceString(cookieString, "Sun, ", "Sun_ ");

            String[] cookiesSeparatedByComma = StringTokenizer.getArray(cookieString, ", ");
            for (int j = 0; j < cookiesSeparatedByComma.length; j++) {

                //defaultni hodnoty
                long expires = -1;
                String name = "";
                String value = "";
                String domain = "";

                //parsovani cookie
                String[] fields = StringTokenizer.getArray(cookiesSeparatedByComma[j].trim(), ";");
                for (int i = 0; i < fields.length; i++) {
                    String[] nameValues = StringTokenizer.getArray(fields[i], "=");
                    if (nameValues[0].trim().equalsIgnoreCase("expires")) {
                        expires = Utils.expiresToTimestamp(nameValues[1]);
                    } else if (nameValues[0].trim().equalsIgnoreCase("domain")) {
                        domain = nameValues[1];
                    } else if (nameValues[0].trim().equalsIgnoreCase("path")) {
                        //ignore
                    } else {
                        name = nameValues[0].trim();
                        value = nameValues[1];
                    }
                }
                addEditDelete(name, value, domain, expires);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.addEditDelete", cookieString);
        }
    }

    /**
     * Adds cookie directly - used in sycn
     * @param name name
     * @param value value
     * @param domain domain
     * @param expires expiration timestamp
     */
    public static void addEditDelete(String name, String value, String domain, long expires) {
        try {
            //vytvoreni domeny pokud neni
            if (domain.equals("") || domain == null) {
                String[] parts1 = StringTokenizer.getArray(R.getHttp().url.substring(7), "/");
                String[] parts2 = StringTokenizer.getArray(R.getHttp().url.substring(7), "?");
                if (parts1[0].length() < parts2[0].length()) {
                    domain = parts1[0];
                } else {
                    domain = parts2[0];
                }
            }

            //smazat pokud je prazdna hodnota
            if (value.equals("deleted")) {
                delete(name, domain);
                return;
            }
            //nepridavam vyprsene
            if (expires < Utils.timestamp() && expires != -1) {
                Logger.log("Expired cookie, not adding");
                return;
            }

            //hledani existujici cookie
            boolean found = false;
            Cookie cookie = null;
            int i = 0;
            for (i = 0; i < cookies.size(); i++) {
                cookie = (Cookie) cookies.elementAt(i);
                if (cookie.getName().equals(name) && domain.endsWith(cookie.getDomain())) {
                    found = true;
                    cookie.setExpires(expires);
                    cookie.setValue(value);
                    break;
                }
            }
            if (found) {
                //editace cookie
                cookies.setElementAt(cookie, i);
                Logger.log("Editing cookie " + cookie.getName());
            } else {
                //nova cookie
                cookies.addElement(new Cookie(name, value, domain, expires));
                Logger.log("Adding new cookie " + name + " expires: " + expires);
            }
            saveXML();
            //menu check
            if (name.equals("session")) {
                R.getMainScreen().checkLoginLogout();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.addEditDelete", null);
        }
    }

    /**
     * Deletes cookie
     */
    private static void delete(String name, String domain) {
        try {
            //hledani existujici cookie
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = (Cookie) cookies.elementAt(i);
                if (cookie.getName().equals(name) && domain.endsWith(cookie.getDomain())) {
                    Logger.log("Deleting cookie " + name + " and domain " + domain);
                    cookies.removeElementAt(i);
                    break;
                }
            }
            saveXML();
            //menu check
            if (name.equals("session")) {
                R.getMainScreen().checkLoginLogout();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.delete", name);
        }
    }

    /** 
     * Returns value of given cookie
     * @param name name of cookie
     * @param domain domain
     * @return value of cookie
     */
    public static String getValue(String name, String domain) {
        try {
            //hledani existujici cookie
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = (Cookie) cookies.elementAt(i);
                if (cookie.getName().equals(name) && domain.indexOf(cookie.getDomain()) != -1) {
                    return cookie.getValue();
                }
            }
            return "";
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.getValue", name);
            return "";
        }
    }

    /**
     * Deletes temporary (expires -1) and expired cookies when app starts
     */
    private static void checkExpiration() {
        try {
            boolean changed = false;
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = (Cookie) cookies.elementAt(i);
                if (cookie.getExpires() < Utils.timestamp()) {
                    changed = true;
                    Logger.log("Removing expired cookie " + cookie.getName());
                    cookies.removeElementAt(i);
                    i--;
                }
            }
            if (changed) {
                saveXML();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.checkExpiration", null);
        }
    }

    /**
     * Returns header data with valid cookies
     * @param url url which data are sent to
     * @return Header data
     */
    public static String getHeaderData(String url) {
        try {
            String postData = "";
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = (Cookie) cookies.elementAt(i);
                if (url.indexOf(cookie.getDomain()) != -1) {
                    postData += cookie.getName() + "=" + cookie.getValue() + "; ";
                }
            }
            return postData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.getHeaderData", url);
            return "";
        }
    }

    /**
     * Merge url with cookie data (while calling internal or external browser)
     * @param url url to be merged with
     * @return Url encoded cookies
     */
    public static String getUrlData(String url) {
        try {
            boolean useQuestion = true;
            if (url.indexOf("?") >= 0) {
                useQuestion = false;
            }
            String postData = "";
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = (Cookie) cookies.elementAt(i);
                if (url.indexOf(cookie.getDomain()) != -1) {
                    if (useQuestion) {
                        postData += "?";
                    } else {
                        postData += "&";
                    }
                    postData += cookie.getName() + "=" + cookie.getValue();
                    useQuestion = false;
                }
            }
            return url + postData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.getUrlData", url);
            return "";
        }
    }

    /**
     * Returns sync data
     * @return Sync data
     */
    public static String syncData() {
        try {
            String syncData = "<file>\n";
            syncData += "<id>locify://cookies</id>\n";
            syncData += "<type>cookies</type>\n";
            syncData += "<action>allSync</action>\n";
            syncData += "<ts>" + R.getFileSystem().getTimestamp(FileSystem.COOKIES_FILE) + "</ts>\n";
            syncData += "<content>\n";
            syncData += getXML();
            syncData += "</content>\n";
            syncData += "</file>\n";
            return syncData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.syncData", null);
            return "";
        }
    }

    private static String getXML() {
        String data = "<cookies>\n";
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookie = (Cookie) cookies.elementAt(i);
            data += "<cookie>\n";
            data += "<name>" + cookie.getName() + "</name>\n";
            data += "<value>" + cookie.getValue() + "</value>\n";
            data += "<domain>" + cookie.getDomain() + "</domain>\n";
            data += "<expires>" + cookie.getExpires() + "</expires>\n";
            data += "</cookie>\n";
        }
        data += "</cookies>\n";
        return data;
    }

    private static void saveXML() {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + getXML();
        R.getFileSystem().saveString(FileSystem.COOKIES_FILE, data);
    }

    public static void loadXML() {
        try {
            cookies = new Vector();
            String data = R.getFileSystem().loadString(FileSystem.COOKIES_FILE);
            ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(data));
            bais.reset();
            KXmlParser parser = new KXmlParser();
            //parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
            parser.setInput(bais, "utf-8");
            String name = "";
            String value = "";
            String domain = "";
            String expires = "";
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
                } else if (tagName.equals("name")) {
                    name = parser.nextText();
                } else if (tagName.equals("value")) {
                    value = parser.nextText();
                } else if (tagName.equals("domain")) {
                    domain = parser.nextText();
                } else if (tagName.equals("expires")) {
                    expires = parser.nextText();
                    //zpracovani
                    cookies.addElement(new Cookie(name, value, domain, Long.parseLong(expires)));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.loadXML", null);
        }
    }

    /**
     * Replace cookies variables in given string
     * @param text text to be replaced
     * @return replaced text
     */
    public static String replaceVariables(String text) {
        try {
            int start = text.indexOf("$cookies[");
            int end = text.indexOf("]");
            String cookieName = text.substring(start + 9, end);
            text = Utils.replaceString(text, text.substring(start, end + 1), getValue(cookieName, R.getHttp().url));
            return text;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "CookieData.replaceVariables", text);
            return text;
        }
    }
}

/**
 * One single cookie object
 * @author Destil
 */
class Cookie {

    private String name;
    private String value;
    private String domain;
    private long expires;

    public Cookie(String name, String value, String domain, long expires) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.expires = expires;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return domain;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
