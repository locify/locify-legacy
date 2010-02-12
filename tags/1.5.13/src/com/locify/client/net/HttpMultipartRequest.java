/*
 * HttpMultipartRequest.java
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
import com.locify.client.data.SettingsData;
import com.locify.client.utils.R;
import com.locify.client.gui.polish.TopBarBackground;
import de.enough.polish.browser.Browser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

/**
 * Sends request to server in multipart format - used for uploading files to server
 * @author Destil
 */
public class HttpMultipartRequest implements Runnable {

    static final String BOUNDARY = "---------------------------41184676334";
    String url = null;
    String path = null;
    String startBoundary = null;
    String endBoundary = null;
    Browser browser = null;

    public HttpMultipartRequest(String url, Hashtable params, String fileField, String fileName, String fileType, String fullPath, Browser browser) throws Exception {
        this.url = url;
        this.path = fullPath;
        this.browser = browser;

        String boundary = getBoundaryString();

        startBoundary = getBoundaryMessage(boundary, params, fileField, fileName, fileType);
        endBoundary = "\r\n--" + boundary + "--\r\n";
    }

    String getBoundaryString() {
        return BOUNDARY;
    }

    String getBoundaryMessage(String boundary, Hashtable params, String fileField, String fileName, String fileType) {
        StringBuffer res = new StringBuffer("--").append(boundary).append("\r\n");

        Enumeration keys = params.keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) params.get(key);

            res.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n").append("\r\n").append(value).append("\r\n").append("--").append(boundary).append("\r\n");
        }
        res.append("Content-Disposition: form-data; name=\"").append(fileField).append("\"; filename=\"").append(fileName).append("\"\r\n").append("Content-Type: ").append(fileType).append("\r\n\r\n");

        return res.toString();
    }

    /**
     * Sends the multipart request and processes answer
     */
    public void send() {
        R.getBack().goForward(url, null);
        (new Thread(this)).start();
    }

    public void run() {
        long fileSize = R.getFileSystem().getSize(path, FileSystem.SIZE_FILE);

        R.getUploadProgress().view((int) (fileSize / 1024));

        HttpConnection hc = null;

        InputStream is = null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] res = null;

        try {

            TopBarBackground.setHttpStatus(TopBarBackground.CONNECTING);

            hc = (HttpConnection) Connector.open(url);

            hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + getBoundaryString());

            //cookies
            hc.setRequestProperty("Cookie", CookieData.getHeaderData(url));
            //user-agent
            String userAgent = hc.getRequestProperty("User-Agent");
            if (userAgent == null) {
                userAgent = "";
            }
            hc.setRequestProperty("User-Agent", "Locify/" + R.getMidlet().getAppProperty("MIDlet-Version") + "/" + SettingsData.getLanguage() + "/" + userAgent);


            hc.setRequestMethod(HttpConnection.POST);

            OutputStream os = hc.openOutputStream();
            TopBarBackground.setHttpStatus(TopBarBackground.SENDING);
            os.write(startBoundary.getBytes());

            //read from file, write to net

            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + path);
            InputStream fis = fileConnection.openInputStream();
            byte[] buffer = new byte[1024];
            int byteNum = 0;
            int uploaded = 0;
            while ((byteNum = fis.read(buffer)) != -1) {
                if (byteNum == buffer.length) {
                    os.write(buffer);
                    uploaded++;
                } else {
                    for (int i = 0; i < byteNum; i++) {
                        os.write(buffer[i]);
                    }
                }
                R.getUploadProgress().update(uploaded);
            }
            fis.close();
            fileConnection.close();

            os.write(endBoundary.getBytes());
            R.getUploadProgress().complete();
            os.close();


            //read and view the answer
            TopBarBackground.setHttpStatus(TopBarBackground.RECEIVING);
            is = hc.openInputStream();
            this.browser.loadPage(is);
            R.getURL().call("locify://htmlBrowser");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "HttpMultipartRequest.send", path);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }

                if (is != null) {
                    is.close();
                }

                if (hc != null) {
                    hc.close();
                }

                TopBarBackground.setHttpStatus(TopBarBackground.UNDEFINED);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}