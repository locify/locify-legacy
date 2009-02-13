/*
 * GeocodeResult.java
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

package com.locify.client.gui.screen.service;

/**
 * Encapsulates data about geocoding result
 * @author Destil
 */
public class GeocodeResult {

    private double latitude;
    private double longitude;
    private String warning;
    private String address;
    private String city;
    private String state;
    private String country;

    public GeocodeResult(double latitude, double longitude, String warning, String address, String city, String state, String country) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.warning = warning;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getState() {
        return state;
    }

    public String getWarning() {
        return warning;
    }

    public String getFullAddress()
    {
        return ((getAddress().equals(""))?"":(getAddress() + ", ")) + getCity() + ", " + getState() + " (" + getCountry() + ")";
    }

    public String getReducedAddress()
    {
        return ((getAddress().equals(""))?"":(getAddress() + ", ")) + getCity() + ", " + getCountry();
    }
}
