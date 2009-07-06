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

import com.locify.client.utils.Utils;
import com.locify.client.utils.R;

/**
 * Represents KML network link in the memory and provides the repeat call feature
 * @author Destil
 */
public class NetworkLink extends GeoData {

    protected int refreshInterval;
    protected String link;
    protected String viewFormat;

    public NetworkLink() {
        super();
        this.refreshInterval = 0;
        this.link = "";
        this.viewFormat = "";
    }

    public NetworkLink(String name, String description, int refreshInterval, String link, String viewFormat) {
        super();
        this.name = name;
        this.description = description;

        this.refreshInterval = refreshInterval;
        this.link = link;
        this.viewFormat = viewFormat;
    }

    public String getLink() {
        if (viewFormat != null) {
            String replaced = Utils.replaceString(viewFormat, "[lookatLat]", String.valueOf(R.getLocator().getLastLocation().getLatitude()));
            replaced = Utils.replaceString(replaced, "[lookatLon]", String.valueOf(R.getLocator().getLastLocation().getLongitude()));
            if (link.indexOf("?") != -1) {
                return link + "&amp;" + replaced;
            } else {
                return link + "?" + replaced;
            }
        } else {
            return link;
        }
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
