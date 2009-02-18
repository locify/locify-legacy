/*
 * NetworkLink.java
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
package com.locify.client.data.items;

/**
 * Represents KML network link in the memory and provides the repeat call feature
 * @author Destil
 */
public class NetworkLink {
    private String name;
    private String description;
    private int refreshInterval;
    private String link;
    private String viewFormat;

    public NetworkLink(String name, String description, int refreshInterval, String link, String viewFormat) {
        this.name = name;
        this.description = description;
        this.refreshInterval = refreshInterval;
        this.link = link;
        this.viewFormat = viewFormat;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public String getDescription() {
        return description;
    }

    public String getViewFormat() {
        return viewFormat;
    }
    
}
