/*
 * LocationSample.java
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

/**
 * Reresentation of location sample which contains all data relevant for filtering
 * @author Jiri
 */
public class LocationSample {

    private double latitude;
    private double longitude;
    private double altitude;
    private double horizontalAccuracy;
    private double verticalAccuracy;
    private double speed;
    private double course;

    public LocationSample(double lat, double lon, double alt, double ha, double va, double speed, double course) {
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.horizontalAccuracy = ha;
        this.verticalAccuracy = va;
        this.speed = speed;
        this.course = course;
    }

    public LocationSample(LocationSample s) {
        this(s.latitude, s.longitude, s.altitude, s.horizontalAccuracy, s.verticalAccuracy, s.speed, s.course);
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public double getCourse() {
        return course;
    }

    public void setCourse(float course) {
        this.course = course;
    }

    public double getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    public void setHorizontalAccuracy(double horizontalAccuracy) {
        this.horizontalAccuracy = horizontalAccuracy;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getVerticalAccuracy() {
        return verticalAccuracy;
    }

    public void setVerticalAccuracy(double verticalAccuracy) {
        this.verticalAccuracy = verticalAccuracy;
    }

    public String toString() {
        return "lat: " + latitude + "lon: " + longitude + " alt: " + altitude;
    }
}
