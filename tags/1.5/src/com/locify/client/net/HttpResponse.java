/*
 * HttpResponse.java
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

/**
 * This class is object for HTTP responses
 * @author Destil
 */
public class HttpResponse {
    private byte[] data;
    private boolean image = false;
    private boolean audio = false;
    private boolean disabledCaching = false;
    private String newLocation;
    private String url;

    public HttpResponse(String url) {
        this.url = url;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setDisabledCaching(boolean disabledCaching) {
        this.disabledCaching = disabledCaching;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public void setNewLocation(String newLocation) {
        this.newLocation = newLocation;
    }

    public String getNewLocation() {
        return newLocation;
    }

    public boolean isImage() {
        return image;
    }

    public boolean isAudio() {
        return audio;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDisabledCaching() {
        return disabledCaching;
    }
}
