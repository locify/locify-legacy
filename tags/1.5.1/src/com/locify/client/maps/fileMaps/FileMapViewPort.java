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
import com.locify.client.utils.Logger;
import java.io.DataOutputStream;
import java.io.IOException;

/** Tato trida reprezentuje vyrez z WGS84 souradnicoveho systemu
 *  - A - horni levy roh
 *  - B - horni pravy
 *  - C - levy dolni
 *  - D - pravy dolni
 * @author Menion
 **/
public class FileMapViewPort {

    /** Top-left point */
    private Location4D A;
    /** Top-right point */
    private Location4D B;
    /** Bottom-left point */
    private Location4D C;
    /** Bottom-right point */
    private Location4D D;
    private Location4D center;
    /* kolik uhlovych stupnu a minut zobrazuji */
    private double latitude_dimension;
    private double longitude_dimension;
    private int xmax;
    private int ymax;

    // helmert transformation parametres
    private double X0;
    private double Y0;
    private double laXS;
    private double laXC;
    private double laYS;
    private double laYC;
    private double X0inv;
    private double Y0inv;
    private double laXSinv;
    private double laXCinv;
    private double laYSinv;
    private double laYCinv;
    private boolean helmert;
    protected boolean sphericalMercator = false;

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
     * Create FileMapViewPort from Transformation coeficients. Its important
     * to compute inverse coeficients and after that call function SetInverseHelmert()
     * which complete settings of this ViewPort.
     * @param X0
     * @param Y0
     * @param la1
     * @param la2
     * @param xmax
     * @param ymax
     */
    public FileMapViewPort(double X0, double Y0, double laXS, double laXC, double laYS, double laYC, int xmax, int ymax) {
        this.X0 = X0;
        this.Y0 = Y0;
        this.laXC = laXC;
        this.laXS  = laXS;
        this.laYC = laYC;
        this.laYS = laYS;
        this.xmax = xmax;
        this.ymax = ymax;
        this.helmert = true;
    }

    public void setInverseTransformParametres(double X0, double Y0, double laXS, double laXC, double laYS, double laYC) {
        this.X0inv = X0;
        this.Y0inv = Y0;
        this.laXSinv  = laXS;
        this.laXCinv = laXC;
        this.laYSinv = laYS;
        this.laYCinv = laYC;
        this.A = helmertTransformInverse(0, 0);
        this.B = helmertTransformInverse(xmax, 0);
        this.C = helmertTransformInverse(0, ymax);
        this.D = helmertTransformInverse(xmax, ymax);
        calculateDimension();
        calculateCenter();
//System.out.println("Set inverse Helmert: " + X0inv + " " + Y0inv + " " + la1inv + " " + la2inv + " " + latitude_dimension + " " + longitude_dimension);
    }

    public void appendHelmertParametres(DataOutputStream dos) {
        try {
            dos.writeDouble(X0);
            dos.writeDouble(Y0);
            dos.writeDouble(laXS);
            dos.writeDouble(laXC);
            dos.writeDouble(laYS);
            dos.writeDouble(laYC);
            dos.writeDouble(X0inv);
            dos.writeDouble(Y0inv);
            dos.writeDouble(laXSinv);
            dos.writeDouble(laXCinv);
            dos.writeDouble(laYSinv);
            dos.writeDouble(laYCinv);
        } catch (IOException ex) {
            Logger.error("FileMapViewPort.appendInverseHelmertParametres() " + ex.toString());
        }
    }

    // transform from coordinates to pixels
    private Point2D.Int helmertTransform(double X, double Y) {
        int x1;
        if (!sphericalMercator)
            x1 = (int) (X0 + laXC * X - laYS * Y);
        else
            x1 = (int) (X0 + laXC * X - laYS * Y);
        int y1 = (int) (Y0 + laXS * X + laYC * Y);
        return new Point2D.Int(x1, y1);
    }

    // transform from pixels to coordinates
    private Location4D helmertTransformInverse(double X, double Y) {
        double x1;
        if (sphericalMercator)
            x1 = X0inv + laXCinv * X - laYSinv * Y;
        else
            x1 = X0inv + laXCinv * X - laYSinv * Y;
        double y1 = Y0inv + laXSinv * X + laYCinv * Y;
        return new Location4D(x1, y1, 0.0f);
    }

    public void setCenter(Location4D pos) {
        this.center = pos;
        calculateEndPoints();
    }

    public Location4D getCenter() {
        return center;
    }

    private void calculateCenter() {
        if (center == null)
            center = new Location4D(0.0, 0.0, 0.0f);
        
        center.setPosition(
                (A.getLatitude() + B.getLatitude() + C.getLatitude() + D.getLatitude()) / 4.0,
                (A.getLongitude() + B.getLongitude() + C.getLongitude() + D.getLongitude()) / 4.0,
                0);
    }

    public boolean containsPosition(Location4D pos) {
        double lat = pos.getLatitude();
        double lon = pos.getLongitude();
        return ((lat > (Math.min(A.getLatitude(), C.getLatitude()))) &&
                (lat < Math.max(B.getLatitude(), D.getLatitude())) &&
                (lon > Math.min(C.getLongitude(), D.getLongitude())) &&
                (lon < Math.max(A.getLongitude(), B.getLongitude())));
    }

    public Location4D convertMapPixelToGeo(int x, int y) {
        Location4D loc;
        if (helmert) {
            loc = helmertTransformInverse(x, y);
        } else {
            loc = new Location4D(
                    center.getLongitude() + (x / xmax  - 0.5) * longitude_dimension,
                    center.getLatitude() + (y / ymax  - 0.5) * latitude_dimension,
                    0.0f);
        }
        return loc;
    }

    /**
     * Convert Geodetic coordinates in appropriate map system (eg. S-42 x=...m, y=...m)
     * to pixel system of this viewport.
     * @param pos Position you want to convert.
     * @return Pixel coordinates in this viewport.
     */
    public Point2D.Int convertGeoToMapPixel(Location4D pos) {
        Point2D.Int point;
        if (helmert) {
            point = helmertTransform(pos.getLatitude(), pos.getLongitude());
//            System.out.println("\n  FileMapViewPort.getPointAnyWhere() - Helmert");
        } else {
            point = new Point2D.Int(
                    xmax / 2 + (int) ((pos.getLongitude() - center.getLongitude()) / longitude_dimension * xmax),
                    ymax / 2 - (int) ((pos.getLatitude() - center.getLatitude()) / latitude_dimension * ymax));
//            System.out.println("\n  FileMapViewPort.getPointAnyWhere()");
//            System.out.println("\n    center.lat: " + center.getLatitude() + " center.lon: " + center.getLongitude());
        }
//        System.out.println("\n    pos.lat: " + pos.getLatitude() + " pos.lon: " + pos.getLongitude());
//        System.out.println("\n    xmax: " + xmax + " ymax: " + ymax);
//        System.out.println("\n    latDim: " + latitude_dimension + " lonDim: " + longitude_dimension);
//        System.out.println("\n    res.x: " + point.x + " res.y: " + point.y);
        return point;
    }

    public double getLatitudeDimension() {
        return latitude_dimension;
    }

    public double getLongitudeDimension() {
        return longitude_dimension;
    }
    
    private void calculateDimension() {
        latitude_dimension = Math.abs(A.getLatitude() - C.getLatitude() + B.getLatitude() - D.getLatitude()) / 2;
        longitude_dimension = Math.abs(B.getLongitude() - A.getLongitude() + D.getLongitude() - C.getLongitude()) / 2;
    }

    private void calculateEndPoints() {
        A.setPosition(center.getLatitude() + latitude_dimension / 2, center.getLongitude() - longitude_dimension / 2, 0);
        B.setPosition(center.getLatitude() + latitude_dimension / 2, center.getLongitude() + longitude_dimension / 2, 0);
        C.setPosition(center.getLatitude() - latitude_dimension / 2, center.getLongitude() - longitude_dimension / 2, 0);
        D.setPosition(center.getLatitude() - latitude_dimension / 2, center.getLongitude() + longitude_dimension / 2, 0);
    }

    public int getXmax() {
        return xmax;
    }

    public int getYmax() {
        return ymax;
    }
    
    /**
     * Get Location4D corner.
     * @param number of the corner.
     * <ul>
     * <li>1. (A) top-left</li>
     * <li>2. (B) top-right</li>
     * <li>3. (C) bottom-left</li>
     * <li>4. (D) bottom-right</li>
     * </ul>
     * @return Location4D corner.
     */
    public Location4D getCalibrationCorner(int number) {
        if (number > 0 && number < 5) {
            switch (number) {
                case 1:
                    return A;
                case 2:
                    return B;
                case 3:
                    return C;
                case 4:
                    return D;
            }
        }
        return null;
    }

    public String toString() {
        return " Map viewport: [" + xmax + "," + ymax + "]" + ", cent:" + center + ", dim_lon:" +
                longitude_dimension + ", dim_lat:" + latitude_dimension;
    }
}
