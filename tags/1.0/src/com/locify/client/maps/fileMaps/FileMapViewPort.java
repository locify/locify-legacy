/*
 * FileMapViewPort.java
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

package com.locify.client.maps.fileMaps;

import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;

/** Tato trida reprezentuje vyrez z WGS84 souradnicoveho systemu
 *  - A - horni levy roh
 *  - B - horni pravy
 *  - C - levy dolni
 *  - D - pravy dolni
 * Trida predpoklada ze nezobrazujeme prilis velkou plochu
 * @author Menion
 **/
public class FileMapViewPort {

    public Location4D A,  B,  C,  D;
    public Location4D center;
    /* kolik uhlovych stupnu a minut zobrazuji */
    public double latitude_dimension; 
    public double longitude_dimension;
    public int xmax;
    public int ymax;
    public double xscale; //velikost v metrech 

    /** construct viewport from enf points and calculate the center */
    public FileMapViewPort(Location4D a, Location4D b, Location4D c, Location4D d, int xmax, int ymax) {
        this.A = a;
        this.B = b;
        this.C = c;
        this.D = d;

        this.xmax = xmax;
        this.ymax = ymax;
        center = new Location4D(0, 0, 0);
        calculateCenter();
        calculateDimension();
        calculateScale();
    }

    /** contsruct viewport from center point and scale & calculate end points */
    public FileMapViewPort(Location4D center, double dim_lon, double dim_lat, int xmax, int ymax) {
        this.xmax = xmax;
        this.ymax = ymax;
        this.center = center;
        this.longitude_dimension = dim_lon;
        this.latitude_dimension = dim_lat;
        A = new Location4D(0, 0, 0);
        B = new Location4D(0, 0, 0);
        C = new Location4D(0, 0, 0);
        D = new Location4D(0, 0, 0);
        calculateEndPoints();
        calculateScale();
    }

    private void calculateDimension() {
        latitude_dimension = (A.getLatitude() - C.getLatitude() + B.getLatitude() - D.getLatitude()) / 2;
        longitude_dimension = (B.getLongitude() - A.getLongitude() + D.getLongitude() - C.getLongitude()) / 2;
    }
    
    private void calculateScale() {
        this.xscale = (A.distanceTo(B) + C.distanceTo(D)) / 2;
    }

    private void calculateEndPoints() {
        A.setPosition(center.getLatitude() + latitude_dimension / 2, center.getLongitude() - longitude_dimension / 2, 0);
        B.setPosition(center.getLatitude() + latitude_dimension / 2, center.getLongitude() + longitude_dimension / 2, 0);
        C.setPosition(center.getLatitude() - latitude_dimension / 2, center.getLongitude() - longitude_dimension / 2, 0);
        D.setPosition(center.getLatitude() - latitude_dimension / 2, center.getLongitude() + longitude_dimension / 2, 0);
    }

    public void calculateCenter() {
        center.setPosition(
                (A.getLatitude() + B.getLatitude() + C.getLatitude() + D.getLatitude()) / 4.0,
                (A.getLongitude() + B.getLongitude() + C.getLongitude() + D.getLongitude()) / 4.0,
                0);
    }

    public void setDimension(double lat_dim, double lon_dim) {
        this.latitude_dimension = lat_dim;
        this.longitude_dimension = lon_dim;
        calculateEndPoints();
        calculateScale();
    }

    public void setCenter(Location4D pos) {
        this.center = pos;
        calculateEndPoints();
        //calculateScale();
    }

    public boolean containsPosition(Location4D pos) {
        double lat = pos.getLatitude();
        double lon = pos.getLongitude();
        return ((lat > (Math.min(A.getLatitude(), C.getLatitude()))) &&
                (lat < Math.max(B.getLatitude(), D.getLatitude())) &&
                (lon > Math.min(C.getLongitude(), D.getLongitude())) &&
                (lon < Math.max(A.getLongitude(), B.getLongitude())));
    }

    public Point2D.Int getPointAnyWhere(Location4D pos) {
        Point2D.Int point = new Point2D.Int(
                xmax / 2 + (int) ((pos.getLongitude() - center.getLongitude()) / longitude_dimension * xmax),
                ymax / 2 - (int) ((pos.getLatitude() - center.getLatitude()) / latitude_dimension * ymax));
//System.out.println("\ngetPointAnyWhere()");
//System.out.println("\npos.lat: " + pos.getLatitude() + " pos.lon: " + pos.getLongitude());
//System.out.println("\nxmax: " + xmax + " ymax: " + ymax);
//System.out.println("\ncenter.lat: " + center.getLatitude() + " center.lon: " + center.getLongitude());
//System.out.println("\nlatDim: " + latitude_dimension + " lonDim: " + longitude_dimension);
//System.out.println("\nres.x: " + point.x + " res.y: " + point.y);
        return point;
    }

    public String toString() {
        return "[" + xmax + "," + ymax + "]" + "\tCent:" + center + "\tdim_lon:" + longitude_dimension + "\tdim_lat:" + latitude_dimension;
    }
}
