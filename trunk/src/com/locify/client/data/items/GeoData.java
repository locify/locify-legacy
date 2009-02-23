/*
 * GeoData.java
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

import java.util.Hashtable;

/**
 * Object for generalizing geo data - waypoint, route and waypoint cloud
 * @author MenKat
 */
public abstract class GeoData {
    
    protected String name;
    protected String description;
    protected double latitude;
    protected double longitude;
    protected String styleName;
    protected GeoFileStyle style;
    protected GeoFileStyleMap styleMap;

    public GeoData() {
        this.name = "";
        this.description = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.styleName = null;
        this.style = null;
        this.styleMap = null;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }

    public GeoFileStyle getStyleNormal() {
        if (styleMap == null)
            return style;
        else {
            return styleMap.styleNormal;
        }
    }

    public GeoFileStyle getStyleHighLight() {
        if (styleMap == null)
            return style;
        else {
            return styleMap.styleHighLight;
        }
    }

    public String toString() {
        return "Name: " + name + " desc.: " + description + " lat: " + latitude + " lon: " + longitude;
    }
}
