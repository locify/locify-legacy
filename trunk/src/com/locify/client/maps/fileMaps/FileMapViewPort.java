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

    // helmert transformation parametres
    private double X0;
    private double Y0;
    private double la1;
    private double la2;
    private double X0inv;
    private double Y0inv;
    private double la1inv;
    private double la2inv;
    public boolean helmert;

    /** construct viewport from end points and calculate the center */
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
    }

    /**
     * Create FileMapViewPort from Helmert transformation coeficients. Its important
     * to compute inverse coeficients and after that call function SetInverseHelmert()
     * which complete settings of this ViewPort.
     * @param X0
     * @param Y0
     * @param la1
     * @param la2
     * @param xmax
     * @param ymax
     */
    public FileMapViewPort(double X0, double Y0, double la1, double la2, int xmax, int ymax) {
        this.X0 = X0;
        this.Y0 = Y0;
        this.la1 = la1;
        this.la2  = la2;
        this.xmax = xmax;
        this.ymax = ymax;
        this.helmert = true;
    }

    public void setInverseHelmert(double X0, double Y0, double la1, double la2) {
        this.X0inv = X0;
        this.Y0inv = Y0;
        this.la1inv = la1;
        this.la2inv  = la2;
        this.A = helmertTransformInverse(0, 0);
        this.B = helmertTransformInverse(xmax, 0);
        this.C = helmertTransformInverse(0, ymax);
        this.D = helmertTransformInverse(xmax, ymax);
        calculateDimension();
    }

    // transform from coordinates to pixels
    private Point2D.Int helmertTransform(double X, double Y) {
        int x1 = (int) (X0 + la1 * X - la2 * Y);
        int y1 = (int) (Y0 + la1 * Y + la2 * X);
        return new Point2D.Int(x1, y1);
    }

    // transform from pixels to coordinates
    private Location4D helmertTransformInverse(double X, double Y) {
        double x1 = X0inv + la1inv * X - la2inv * Y;
        double y1 = Y0inv + la1inv * Y + la2inv * X;
        return new Location4D(x1, y1, 0.0f);
    }

    public double getLatitudeDimension() {
        return latitude_dimension;
    }

    public double getLongitudeDimension() {
        return longitude_dimension;
    }

    public void setCenter(Location4D pos) {
        this.center = pos;
        calculateEndPoints();
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
        Point2D.Int point;
        if (helmert) {
            point = helmertTransform(pos.getLatitude(), pos.getLongitude());
            System.out.println("\n  FileMapViewPort.getPointAnyWhere() - Helmert");
        } else {
            point = new Point2D.Int(
                    xmax / 2 + (int) ((pos.getLongitude() - center.getLongitude()) / longitude_dimension * xmax),
                    ymax / 2 - (int) ((pos.getLatitude() - center.getLatitude()) / latitude_dimension * ymax));
            System.out.println("\n  FileMapViewPort.getPointAnyWhere()");
            System.out.println("\n    center.lat: " + center.getLatitude() + " center.lon: " + center.getLongitude());
        }
        System.out.println("\n    pos.lat: " + pos.getLatitude() + " pos.lon: " + pos.getLongitude());
        System.out.println("\n    xmax: " + xmax + " ymax: " + ymax);
        System.out.println("\n    latDim: " + latitude_dimension + " lonDim: " + longitude_dimension);
        System.out.println("\n    res.x: " + point.x + " res.y: " + point.y);
        return point;
    }

    public String toString() {
        return "[" + xmax + "," + ymax + "]" + "\tCent:" + center + "\tdim_lon:" + longitude_dimension + "\tdim_lat:" + latitude_dimension;
    }

    private void calculateDimension() {
        System.out.println(A.toString());
        System.out.println(B.toString());
        System.out.println(C.toString());
        System.out.println(D.toString());

        latitude_dimension = Math.abs(A.getLatitude() - C.getLatitude() + B.getLatitude() - D.getLatitude()) / 2;
        longitude_dimension = Math.abs(B.getLongitude() - A.getLongitude() + D.getLongitude() - C.getLongitude()) / 2;
    }

    private void calculateEndPoints() {
        A.setPosition(center.getLatitude() + latitude_dimension / 2, center.getLongitude() - longitude_dimension / 2, 0);
        B.setPosition(center.getLatitude() + latitude_dimension / 2, center.getLongitude() + longitude_dimension / 2, 0);
        C.setPosition(center.getLatitude() - latitude_dimension / 2, center.getLongitude() - longitude_dimension / 2, 0);
        D.setPosition(center.getLatitude() - latitude_dimension / 2, center.getLongitude() + longitude_dimension / 2, 0);
    }

    private void calculateCenter() {
        center.setPosition(
                (A.getLatitude() + B.getLatitude() + C.getLatitude() + D.getLatitude()) / 4.0,
                (A.getLongitude() + B.getLongitude() + C.getLongitude() + D.getLongitude()) / 4.0,
                0);
    }
}
