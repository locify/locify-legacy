/*
 * PostData.java
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

import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;

/**
 * This classes stores and manages POST data from forms
 * @author Destil
 */
public class PostData {

    private String postData;
    private boolean urlEncoded;

    public PostData() {
        postData = "";
        urlEncoded = true;
    }

    /**
     * Deletes POST data on new connection
     */
    public void reset() {
        postData = "";
    }
    
    /**
     * Are some POST data set?
     * @return
     */
    public boolean isSet()
    {
        return !postData.equals("");
    }

    /**
     * Returns url encoded POST data
     * @return url encoded POST data
     */
    public String getUrlEncoded() {
        return postData;
    }

    /**
     * Adds POST data externally
     * @param name post variable name
     * @param value post variable value
     */
    public void add(String name, String value) {
        value = Variables.replace(value, true);
        if (!postData.equals("")) {
            postData += "&";
        }
        postData += name + "=" + new String(UTF8.encode(value));
        urlEncoded = true;
    }

    /**
     * Returns value of specified POST item
     * @param name POST name
     * @return POST value
     */
    public String get(String name) {
        try {
            String[] posts = StringTokenizer.getArray(postData, "&");
            for (int i = 0; i < posts.length; i++) {
                String[] namevalue = StringTokenizer.getArray(posts[i], "=");
                if (namevalue[0].equals(name)) {
                    return Utils.urlUTF8decode(namevalue[1]);
                }
            }
            return "";
        } catch (Exception e) {
            R.getErrorScreen().view(e, "PostData.get", name);
            return "";
        }
    }

    /**
     * Sets raw post data (in sync)
     * @param data data
     * @param urlEncoded url encoded data or sync data
     */
    public void setRaw(String data, boolean urlEncoded) {
        postData = new String(UTF8.encode(data));
        this.urlEncoded = urlEncoded;
    }

    /**
     * Is post data url encoded?
     * @return
     */
    public boolean isUrlEncoded() {
        return urlEncoded;
    }
}
