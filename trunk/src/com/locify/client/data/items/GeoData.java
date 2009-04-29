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

import com.locify.client.locator.Location4D;

/**
 * Object for generalizing geo data - waypoint, route and waypoint cloud
 * @author MenKat
 */
public abstract class GeoData {
    
    protected String name;
    protected String description;
    protected String service;
    protected String styleName;
    protected Location4D position;
    protected GeoFileStyle styleNormal;
    protected GeoFileStyle styleHighlight;

    public GeoData() {
        this.name = "";
        this.description = "";
        this.service = "";
        this.position = new Location4D(0.0, 0.0, 0.0f, 0);
        this.styleName = null;
        this.styleNormal = null;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getService() {
        return service;
    }
    
    public double getLatitude() {
        return position.getLatitude();
    }
    
    public double getLongitude() {
        return position.getLongitude();
    }

    public double getAltitude() {
        return position.getAltitude();
    }

    public void setLatitude(double latitude) {
        position.setLatitude(latitude);
    }

    public void setLongitude(double longitude) {
        position.setLongitude(longitude);
    }

    public void setAltitude(float altitude) {
        position.setAltitude(altitude);
    }

    public GeoFileStyle getStyleNormal() {
        return styleNormal;
    }

    public GeoFileStyle getStyleHighLight() {
        return styleHighlight;
    }

    public String toString() {
        return "Name: " + name + " desc.: " + description + " lat: " + getLatitude() + " lon: " + getLongitude();
    }
}
