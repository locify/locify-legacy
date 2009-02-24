/*
 * Location4D.java
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
package com.locify.client.locator;

import java.util.Date;

import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.math.LMath;

/**
 * Reprezents position in WGS84 coordinate system in 4 dimensions - lat, lon, alt and time
 * @author Jiri Stepan
 */
public class Location4D {

    public final static double R = 6378137;
    public final static double RADIANSPERDEGREE = Math.PI / 180;
    
    private double latitude;
    private double longitude;
    private float altitude;
    private long time; //time in milis from 1.1.1970

    public Location4D(double latitude, double longitude, float altitude) {
        this(latitude, longitude, altitude, new Date().getTime());
    }

    public Location4D(double latitude, double longitude, float altitude, long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.time = time;
    }

    /** calculates distance between two positions in meters */
    public double distanceTo(Location4D to) {
        double lat1, lat2, lon1, lon2;
        lat1 = this.latitude;
        lon1 = this.longitude;
        lat2 = to.getLatitude();
        lon2 = to.getLongitude();

        lat1 = lat1 * RADIANSPERDEGREE;
        lon1 = lon1 * RADIANSPERDEGREE;
        lat2 = lat2 * RADIANSPERDEGREE;
        lon2 = lon2 * RADIANSPERDEGREE;

        double dlon = lon2 - lon1; // difference in longitude
        double dlat = lat2 - lat1; // difference in latitude

        double a = (Math.sin(dlat / 2.0) * Math.sin(dlat / 2.0)) +
                (Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dlon / 2.0) * Math.sin(dlon / 2.0));

        double c = 2.0 * LMath.asin(Math.sqrt(a));

        return (R * c); // Final distance is in meters
    }

    /*    public double azimutTo(Location4D target) {
    double lat1, lon1, lat2, lon2;

    try {
    lat1 = latitude * RADIANSPERDEGREE;
    lon1 = longitude * RADIANSPERDEGREE;
    lat2 = target.getLatitude() * RADIANSPERDEGREE;
    lon2 = target.getLongitude() * RADIANSPERDEGREE;

    double barcosvalval = Math.cos(Math.PI / 2 - lat2) * Math.cos(Math.PI / 2 - lat1) + Math.sin(Math.PI / 2 - lat2) * Math.sin(Math.PI / 2 - lat1) * Math.cos(lon2 - lon1);
    double b = GpsUtils.acos(barcosvalval);
    double d = distanceTo(target) / R;
    double r = GpsUtils.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(d)) / (Math.sin(d) * Math.cos(lat1))); //je to opravdu acos? Nema byt asin?

    double sindif = Math.sin(lon2 - lon1);
    double out = r / RADIANSPERDEGREE; //vystup ve stupnich
    if (sindif < 0) {
    return 360 - out;
    } else if (Double.isNaN(out)) {
    return 0;
    } else {
    return out;
    }
    } catch (java.lang.NumberFormatException e) {
    return 0;

    }
    }
     */
    
    public double azimutTo(Location4D target) {
        //inspired by book Advanced geodesy
        double lat1 = GpsUtils.degToRad(latitude);
        double lat2 = GpsUtils.degToRad(target.getLatitude());

        double dLon = GpsUtils.degToRad(target.getLongitude()) - GpsUtils.degToRad(longitude);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        return (GpsUtils.radToDeg(LMath.atan2(y, x)) + 360) % 360;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public String getFormattedDate() {
        Date date = new Date(this.time);
        String[] parts = StringTokenizer.getArray(date.toString(), " ");
        return parts[1] + " " + parts[2] + " " + parts[5] + " " + parts[3];
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String toString() {
        return ("{" + GpsUtils.formatDouble(latitude, 6) + "; " +
                GpsUtils.formatDouble(longitude, 6) + "; " +
                GpsUtils.formatDouble(altitude, 6) + "; " +
                GpsUtils.formatTime(time) + "}");
    }

    public void setPosition(double lat, double lon, int alt) {
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
    }

    public Location4D getLocation4DCopy() {
        return new Location4D(latitude, longitude, altitude, time);
    }
}
