/*
 * Service.java
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


/**
 * Object for storing service info
 * @author Destil
 */
public class Service {

    private String id;
    private String name;
    private String firstScreenUrl;
    private String description;
    private long timestamp;
    private String icon;
    private long settingsTimestamp;
    private String settingsUrl;

    public Service(String id, String name, String firstScreenUrl, String description, long timestamp, String icon, long settingsTimestamp, String settingsUrl) {
        this.id = id;
        this.name = name;
        this.firstScreenUrl = firstScreenUrl;
        this.description = description;
        this.timestamp = timestamp;
        this.icon = icon;
        this.settingsTimestamp = settingsTimestamp;
        this.settingsUrl = settingsUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFirstScreenUrl() {
        return firstScreenUrl;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIcon() {
        return icon;
    }

    public long getSettingsTimestamp() {
        return settingsTimestamp;
    }

    public String getSettingsUrl() {
        return settingsUrl;
    }

    public void setSettingsTimestamp(long settingsTimestamp) {
        this.settingsTimestamp = settingsTimestamp;
    }
}

