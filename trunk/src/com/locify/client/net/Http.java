/*
 * Http.java
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
import com.locify.client.data.CookieData;
import com.locify.client.data.IconData;
import com.locify.client.data.ServicesData;
import com.locify.client.data.AudioData;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import com.locify.client.gui.polish.TopBarBackground;
import com.locify.client.utils.R;
import com.locify.client.data.SettingsData;
import com.locify.client.utils.Logger;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import java.io.IOException;

/**
 * Manages almost all connections to the HTTP server - manages caching, headers, cookies etc.
 * @author Destil
 */
public class Http implements Runnable {

    public static String DEFAULT_URL;
    public String url = ""; //aktualni url
    private Thread thread;
    private boolean imageDownload = false;
    private boolean audioDownload = false;
    private boolean httpBasic;
    private String basicResponse;

    public Http() {
        //#if release
//#         DEFAULT_URL = "http://client.locify.com/";
        //#else
        DEFAULT_URL = "http://client.stage.locify.com/";
    //#endif
    }

    /***
     * Opens new url in new thread
     * @param url http url
     */
    public void start(String url) {
        ContentHandler.setPragmaNoCache(false);
        httpBasic = false;
        this.url = url;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Resends previous request via HTTP BASIC AUTH with credentials in basic response
     * @param basicResponse
     */
    public void startBasic(String basicResponse) {
        httpBasic = true;
        this.basicResponse = basicResponse;
        thread = new Thread(this);
        thread.start();
    }

    /***
     * Connects to the internet, sends data through POST, gets response and starts XML parsing
     */
    public void run() {
        HttpConnection httpConnection = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try {
            //cache checking
            String response = CacheData.get(url);
            if (response == null) {
                TopBarBackground.setHttpStatus(TopBarBackground.CONNECTING);
                ContentHandler.setLoadedFromCache(false);

                //not i cache - downloading from net
                Logger.log("---------- REQUEST -----------");
                Logger.log("URL: " + url);
                httpConnection = (HttpConnection) Connector.open(url);
                httpConnection = sendData(httpConnection);

                //receiving data
                is = httpConnection.openInputStream();
                TopBarBackground.setHttpStatus(TopBarBackground.RECEIVING);
                Logger.log("---------- ANSWER -----------");
                //headers
                httpConnection = handleHeaders(httpConnection);
                if (httpConnection == null) {
                    return;
                }
                Logger.log("Response code:" + httpConnection.getResponseCode());
                int responseCode = httpConnection.getResponseCode();
                if (responseCode == 401) {
                    //zacatek autentifikace
                    if (url.startsWith(DEFAULT_URL)) {
                        R.getAuthentication().setNext("locify://refresh");
                        url = "locify://authentication";
                    }
                    R.getPostData().reset();
                    R.getAuthentication().start(url);
                    return;
                } else if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307) {
                    Logger.log("Redirecting to " + url);
                    R.getPostData().reset();
                    start(url);
                    return;
                } else {

                    if (responseCode == 403) //auth failed
                    {
                        R.getSettings().setAutologin(SettingsData.OFF);
                    }

                    //reading data byte by byte
                    baos = new ByteArrayOutputStream();
                    dos = new DataOutputStream(baos);
                    int onebyte;
                    while ((onebyte = is.read()) != -1) {
                        dos.write(onebyte);
                    }
                    byte[] byteArr = baos.toByteArray();

                    if (imageDownload) {
                        ContentHandler.setPragmaNoCache(true);
                        Logger.log("Image received");
                        if (responseCode == HttpConnection.HTTP_OK) {
                            if (url.startsWith(ServicesData.getCurrent())) {
                                R.getCustomList().refreshIcon(url, byteArr);
                            } else {
                                R.getMainScreen().refreshIcon(url, byteArr);
                            }
                            IconData.save(url, byteArr);
                        } else {
                            Logger.log("Image download failed");
                        }
                        imageDownload = false;
                        audioDownload = false;
                        return;
                    } else if (audioDownload) {
                        ContentHandler.setPragmaNoCache(true);
                        Logger.log("Audio received");
                        if (responseCode == HttpConnection.HTTP_OK) {
                            AudioData.save(url, byteArr);
                        } else {
                            Logger.log("Audio download failed");
                        }
                        imageDownload = false;
                        audioDownload = false;
                        return;
                    } else {
                        response = UTF8.decode(byteArr, 0, byteArr.length);
                        if (response.length() == 0) {
                            Logger.log("No data");
                            R.getPostData().reset();
                            return;
                        }
                        Logger.log("Data:");
                        Logger.log(response);
                    }
                }

            } else {
                Logger.log("Loading cache: " + url);
                ContentHandler.setLoadedFromCache(true);
            }
            ContentHandler.handle(url, response);
            R.getPostData().reset();

        } catch (ConnectionNotFoundException e) {
            R.getConnectionProblem().occured();
        } catch (IOException e) {
            R.getConnectionProblem().occured();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Http.run", url);
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
                if (httpConnection != null) {
                    httpConnection.close();
                    httpConnection = null;
                }
                if (baos != null) {
                    baos.close();
                    baos = null;
                }
                if (dos != null) {
                    dos.close();
                    dos = null;
                }
            } catch (Exception e) {
            }
            TopBarBackground.setHttpStatus(TopBarBackground.UNDEFINED);
        }
    }

    private HttpConnection sendData(HttpConnection httpConnection) throws IOException {
        String postData = R.getPostData().getUrlEncoded();
        httpConnection.setRequestMethod(postData.equals("") ? HttpConnection.GET : HttpConnection.POST);
        //cookies
        httpConnection.setRequestProperty("Cookie", CookieData.getHeaderData(url));
        Logger.log("COOKIES: " + CookieData.getHeaderData(url));
        //user-agent
        String userAgent = httpConnection.getRequestProperty("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }
        httpConnection.setRequestProperty("User-Agent", "Locify/" + R.getMidlet().getAppProperty("MIDlet-Version") + "/" + SettingsData.getLanguage() + "/" + userAgent);
        if (httpBasic) {
            httpConnection.setRequestProperty("Authorization", "Basic " + basicResponse);
        }
        if (!postData.equals("")) {
            if (R.getPostData().isUrlEncoded()) {
                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            httpConnection.setRequestProperty("Content-Length", String.valueOf(postData.length()));
            TopBarBackground.setHttpStatus(TopBarBackground.SENDING);
            OutputStream outputStream = httpConnection.openOutputStream();
            outputStream.write(postData.getBytes());
            outputStream.close();
            Logger.log("POST: " + postData);
        }
        return httpConnection;
    }

    private HttpConnection handleHeaders(HttpConnection httpConnection) throws IOException {
        int j = 0;
        while (httpConnection.getHeaderField(j) != null) {
            Logger.debug("input header " + j + ": " + httpConnection.getHeaderFieldKey(j) + "=" + httpConnection.getHeaderField(j));
            //new cookies
            if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("Set-Cookie")) {
                Logger.log("Set-cookie: " + httpConnection.getHeaderField(j));
                CookieData.addEditDelete(httpConnection.getHeaderField(j));
            } //challenge - used in authentification
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("X-Challenge")) {
                Logger.log("Challenge received: " + httpConnection.getHeaderField(j));
                R.getAuthentication().start("locify://authentication?challenge=" + httpConnection.getHeaderField(j));
                return null;
            } //forbid caching
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("Pragma")) {
                if (httpConnection.getHeaderField(j).equalsIgnoreCase("no-cache")) {
                    Logger.log("Pragma: no-cache = page will not be cached");
                    ContentHandler.setPragmaNoCache(true);
                }
            } //location url
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("Location")) {
                url = httpConnection.getHeaderField(j);
            } //image download
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("Content-type")) {
                if (httpConnection.getHeaderField(j).equalsIgnoreCase("image/png")) {
                    imageDownload = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                } else if (httpConnection.getHeaderField(j).equalsIgnoreCase("audio/x-wav")) {
                    audioDownload = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                }
            } // http basic auth
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("WWW-Authenticate")) {
                if (httpConnection.getHeaderField(j).indexOf("Basic") != -1) {
                    String[] parts = StringTokenizer.getArray(httpConnection.getHeaderField(j), "\"");
                    R.getAuthentication().start("locify://authentication?realm=" + parts[1]);
                    return null;
                }
            }
            j++;
        }
        return httpConnection;
    }
}


