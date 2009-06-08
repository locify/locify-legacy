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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import de.enough.polish.android.io.Connector;
import de.enough.polish.android.io.HttpConnection;
import com.locify.client.gui.polish.TopBarBackground;
import com.locify.client.utils.R;
import com.locify.client.data.SettingsData;
import com.locify.client.utils.Logger;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import java.io.IOException;
import java.util.Vector;

/**
 * Manages almost all connections to the HTTP server - manages caching, headers, cookies etc.
 * @author Destil
 */
public class Http implements Runnable {

    public static String DEFAULT_URL;
    //sources - where request is originated
    public static int SERVICE = 0;
    public static int NETWORKLINK_DOWNLOADER = 1;
    public static int IMAGE_DOWNLOADER = 2;
    public static int AUDIO_DOWNLOADER = 3;
    public static int STRAIGHT_TO_CACHE = 4;
    public static int GEOCODING = 5;
    public static int MAPS = 6;
    public static int AUTH = 7;
    public static int UPDATER = 8;
    public static int SYNC = 9;
    private Thread thread = null;
    private HttpConnection httpConnection = null;
    private InputStream is = null;
    private ByteArrayOutputStream baos = null;
    private DataOutputStream dos = null;
    private Vector requestQueue;
    private HttpRequest request;
    private HttpResponse response;
    private long lastRequestTime = 0;
    private String lastUrl = "";

    public Http() {
        //#if release
//#         DEFAULT_URL = "http://client.locify.com/";
        //#else
        DEFAULT_URL = "http://client.stage.locify.com/";
        //#endif
        requestQueue = new Vector();
    }

    /***
     * Opens new request in new thread
     * @param url
     */
    public void start(String url, int source) {
        start(new HttpRequest(url, R.getPostData().getUrlEncoded(), R.getPostData().isUrlEncoded(), CookieData.getHeaderData(url), source));
    }

    /***
     * Opens new request in new thread
     * @param newRequest
     */
    private void start(HttpRequest newRequest) {
        newRequest.setUrl(makeAbsoluteURL(newRequest.getUrl()));
        //cancel multiple same request - user is mad and clicking over and over
        if (lastRequestTime != 0 && (System.currentTimeMillis()-lastRequestTime)<2000 && request.toString().equals(newRequest.toString()))
        {
            Logger.log("Cancelling request - too soon before another");
            return;
        }
        requestQueue.addElement(newRequest);
        R.getPostData().reset();
        lastRequestTime = System.currentTimeMillis();
        if (newRequest.getSource()!=GEOCODING)
        {
            lastUrl = newRequest.getUrl();
        }
        if (requestQueue.size() == 1) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Repeats last request with additional http basic header
     * @param basic http basic string
     */
    public void repeat(String basic) {
        HttpRequest newRequest = request.clone();
        newRequest.setHttpBasicResponse(basic);
        start(newRequest);
    }

    /**
     * This thread performs all downloading using request queue
     */
    public void run() {
        String debug = "";
        try {
            while (!requestQueue.isEmpty()) {
                request = (HttpRequest) requestQueue.firstElement();
                response = new HttpResponse(request.getUrl(), request.getSource());
                //firstly try cache
                response.setData(CacheData.get(request.getUrl()));
                if (response.getData() != null) {
                    Logger.log("Loading page from cache");
                } else //not cache page - downloading
                {
                    try {
                        TopBarBackground.setHttpStatus(TopBarBackground.CONNECTING);
                        Logger.log("---------- REQUEST -----------");
                        Logger.log("URL: " + request.getUrl());

                        //open connection
                        debug += "opening connection, ";
                        httpConnection = (HttpConnection) Connector.open(request.getUrl(),Connector.READ);

                        debug += "get post, ";
                        if (request.getPostData() == null) { //no post data => get
                            httpConnection.setRequestMethod(HttpConnection.GET);
                        } else //post data => post
                        {
                            httpConnection.setRequestMethod(HttpConnection.POST);
                            sendPostData();
                        }
                        
                        //set request headers
                        debug += "setting headers, ";
                        setRequestHeaders();
                        //set cookies
                        debug += "settings cookies, ";
                        httpConnection.setRequestProperty("Cookie", CookieData.getHeaderData(request.getUrl()));
                        Logger.log("COOKIES: " + CookieData.getHeaderData(request.getUrl()));

                        //receiving data

                        debug += "opening inputstream, ";
                        is = httpConnection.openInputStream();
                        TopBarBackground.setHttpStatus(TopBarBackground.RECEIVING);
                        Logger.log("---------- ANSWER -----------");
                        //handle incoming headers

                        debug += "opening headers, ";
                        if (handleHeaders()) //action based on headers, no need for downloading
                        {
                            requestQueue.removeElementAt(0);
                            continue;
                        }
                        //handle response code

                        debug += "opening response code, ";
                        if (handleResponseCode()) //action based on response code, no need for downloading
                        {
                            requestQueue.removeElementAt(0);
                            continue;
                        }
                        //read data

                        debug += "reading data, ";
                        readData();

                        debug += "complete";
                    } catch (Exception e) {
                        R.getErrorScreen().view(e, "Http.run.request", request.getUrl()+debug);
                    } finally {
                        stop();
                    }
                }
                //process content
                if (response.getData() != null) {
                    if (request.getSource()!=STRAIGHT_TO_CACHE) {
                        ContentHandler.handle(response);
                    } else //dont display, save to cache instead
                    {
                        Logger.log("Saving straight into cache");
                        CacheData.add(response.getUrl(), new String(UTF8.decode(response.getData(), 0, response.getData().length)));
                    }
                }
                //remove request from the queue
                requestQueue.removeElementAt(0);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Http.run", null);
        }
    }

    public String getLastUrl() {
        return lastUrl;
    }

    private void setRequestHeaders() {
        try {
            //user-agent
            String userAgent = httpConnection.getRequestProperty("User-Agent");
            if (userAgent == null) {
                userAgent = "";
            }
            httpConnection.setRequestProperty("User-Agent", "Locify/" + R.getMidlet().getAppProperty("MIDlet-Version") + "/" + SettingsData.getLanguage() + "/" + userAgent);
            if (request.getHttpBasicResponse() != null) {
                httpConnection.setRequestProperty("Authorization", "Basic " + request.getHttpBasicResponse());
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Http.setRequestHeaders", null);
        }
    }

    private void sendPostData() throws IOException {
        if (request.isPostDataUrlEncoded()) {
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        }
        httpConnection.setRequestProperty("Content-Length", String.valueOf(request.getPostData().length()));
        TopBarBackground.setHttpStatus(TopBarBackground.SENDING);
        OutputStream outputStream = httpConnection.openOutputStream();
        outputStream.write(request.getPostData().getBytes());
        outputStream.close();
        Logger.log("POST: " + request.getPostData());
    }

    /**
     * Handles incoming headers
     * @return true when header stops current download
     * @throws java.io.IOException
     */
    private boolean handleHeaders() throws IOException {
        int j = 0;
        while (httpConnection.getHeaderField(j) != null) {
            //Logger.log("input header " + j + ": " + httpConnection.getHeaderFieldKey(j) + "=" + httpConnection.getHeaderField(j));
            if (httpConnection.getHeaderFieldKey(j) == null) {
                j++;
                continue;
            }
            //new cookies
            if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("Set-Cookie")) {
                Logger.log("Set-cookie: " + httpConnection.getHeaderField(j));
                CookieData.addEditDelete(httpConnection.getHeaderField(j));
            } //challenge - used in authentification
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("X-Challenge")) {
                Logger.log("Challenge received: " + httpConnection.getHeaderField(j));
                R.getAuthentication().start("locify://authentication?challenge=" + httpConnection.getHeaderField(j));
                return true;
            } //forbid caching
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("Pragma")) {
                if (httpConnection.getHeaderField(j).equalsIgnoreCase("no-cache")) {
                    Logger.log("Pragma: no-cache = page will not be cached");
                    response.setDisabledCaching(true);
                }
            } //location url
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("Location")) {
                response.setNewLocation(httpConnection.getHeaderField(j));
            } // http basic auth
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("WWW-Authenticate")) {
                if (httpConnection.getHeaderField(j).indexOf("Basic") != -1) {
                    String[] parts = StringTokenizer.getArray(httpConnection.getHeaderField(j), "\"");
                    parts[1] = Utils.replaceString(parts[1], "-1067", ""); //strange behaviour hack
                    R.getAuthentication().start("locify://authentication?realm=" + parts[1]);
                    return true;
                }
            }//header for downloading multiple pages straith into cache
            else if (httpConnection.getHeaderFieldKey(j).equalsIgnoreCase("X-Cache-Store")) {
                Logger.log("Download these urls to cache: " + httpConnection.getHeaderField(j));
                String[] parts = StringTokenizer.getArray(httpConnection.getHeaderField(j), " ");
                for (int i = 0; i < parts.length; i++) {
                    start(new HttpRequest(parts[i], null, false, CookieData.getHeaderData(parts[i]), STRAIGHT_TO_CACHE));
                }
                response.setSaveAfterDownload(true);
            }
            j++;
        }
        return false;
    }

    /**
     * Handles incoming response code
     * @return true if response code stops current download
     * @throws java.io.IOException
     */
    private boolean handleResponseCode() throws IOException {
        int responseCode = httpConnection.getResponseCode();
        Logger.log("Response code:" + responseCode);
        if (responseCode == 401) {
            //auth start
            if (request.getUrl().startsWith(DEFAULT_URL)) {
                R.getAuthentication().setNext("locify://refresh");
                R.getAuthentication().start("locify://authentication");
            } else {
                R.getAuthentication().start(request.getUrl());
            }
            return true;
        } else if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307) {
            Logger.log("Redirecting to " + response.getNewLocation());
            start(response.getNewLocation(), response.getSource());
            return true;
        } else {
            if (responseCode == 403) //auth failed
            {
                R.getSettings().setAutologin(SettingsData.OFF);
            }
            if ((response.getSource()==AUDIO_DOWNLOADER || response.getSource() == IMAGE_DOWNLOADER) && responseCode != 200) {
                Logger.log("Media download failed: " + responseCode);
                return true;
            }
            return false;
        }
    }

    private void readData() throws IOException {
        //reading data byte by byte
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        int onebyte;
        while ((onebyte = is.read()) != -1) {
            dos.write(onebyte);
        }
        response.setData(baos.toByteArray());
    }

    private void stop() {
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
            if (thread != null) {
                thread = null;
            }

            TopBarBackground.setHttpStatus(TopBarBackground.UNDEFINED);
        } catch (Exception e) {
        }
    }

    public String makeAbsoluteURL(String url) {
        if (url.startsWith("http://") || url.startsWith("locify://")) {
            return url;
        }
        if (url.startsWith("/")) {
            //root url
            int thirdSlash = getLastUrl().indexOf('/',8);
            return getLastUrl().substring(0, thirdSlash) + url;
        } else {
            //relative url
            int lastSlash = getLastUrl().lastIndexOf('/');
            return getLastUrl().substring(0, lastSlash + 1) + url;
        }
    }
}

class HttpRequest {

    private String url;
    private String postData;
    private boolean postDataUrlEncoded;
    private String cookies;
    private String httpBasicResponse;
    private int source;

    public HttpRequest(String url, String postData, boolean postDataUrlEncoded, String cookies, int source) {
        this.url = url;
        this.postData = postData;
        this.postDataUrlEncoded = postDataUrlEncoded;
        this.cookies = cookies;
        this.source = source;
    }

    public String getPostData() {
        return postData;
    }

    public String getHttpBasicResponse() {
        return httpBasicResponse;
    }

    public String getUrl() {
        return url;
    }

    public String getCookies() {
        return cookies;
    }

    public boolean isPostDataUrlEncoded() {
        return postDataUrlEncoded;
    }

    public void setHttpBasicResponse(String httpBasicResponse) {
        this.httpBasicResponse = httpBasicResponse;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String toString() {
        return url+postData+postDataUrlEncoded+cookies+httpBasicResponse+source;
    }

    public HttpRequest clone() {
        return new HttpRequest(url, postData, postDataUrlEncoded, cookies, source);
    }

    public int getSource() {
        return source;
    }
}



