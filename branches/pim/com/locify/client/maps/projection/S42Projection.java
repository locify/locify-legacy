/*
 * S42Projection.java
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
package com.locify.client.maps.projection;

import com.locify.client.utils.GpsUtils;

/**
 * Map projection for system S-42
 * @author MenKat
 */
public class S42Projection extends Projection {
    
   /**
     * The UTM "false easting" value. This quantity is added to the true
     * easting to avoid using negative numbers in the coordinates.
     */
    private static final double FALSE_EASTING = 500000;
        
    /**
     * Holds the longitude zone identifier.
     */
    private int _longitudeZone;

    /**
     * Holds the latitude zone identifier.
     */
    private char _latitudeZone;

    /**
     * Holds the easting in meters.
     */
    private double _easting;

    /**
     * Holds the northing in meters.
     */
    private double _northing;
    
    public S42Projection(ReferenceEllipsoid ellipsoid) {
        super(ellipsoid);
    }
    
    /**
     * Returns the projected UTM position corresponding to the specified
     * coordinates.
     *
     * @param longitudeZone the longitude zone number.
     * @param latitudeZone  the longitude zone character.
     * @param easting       the easting value stated in the specified unit.
     * @param northing      the northing value stated in the specified unit.
     * @param unit          the easting/northing length unit.
     * @return the corresponding surface position.
     */
    public void valueOf(int longitudeZone, char latitudeZone, double easting, double northing) {
        _longitudeZone = longitudeZone;
        _latitudeZone = latitudeZone;
        _easting = easting;
        _northing = northing;
    }
    
    public double[] projectionToFlat(double lat, double lon) {
        double[] result = new double[2];

        char latitudeZone = getLatitudeZone(lat, lon);
        int longitudeZone = getLongitudeZone(lat, lon);

        double phi = lat / GpsUtils.RHO;

        double cosPhi = Math.cos(phi);
        double cos2Phi = cosPhi * cosPhi;
        double cos3Phi = cos2Phi * cosPhi;
        double cos4Phi = cos3Phi * cosPhi;
        double cos5Phi = cos4Phi * cosPhi;
        double cos6Phi = cos5Phi * cosPhi;
        double cos7Phi = cos6Phi * cosPhi;
        double cos8Phi = cos7Phi * cosPhi;

        double tanPhi = Math.tan(phi);
        double tan2Phi = tanPhi * tanPhi;
        double tan4Phi = tan2Phi * tan2Phi;
        double tan6Phi = tan4Phi * tan2Phi;

        double eb2 = ellipsoid.getSecondEccentricitySquared();
        double eb4 = eb2 * eb2;
        double eb6 = eb4 * eb2;
        double eb8 = eb6 * eb2;

        double e2c2 = eb2 * cos2Phi;
        double e4c4 = eb4 * cos4Phi;
        double e6c6 = eb6 * cos6Phi;
        double e8c8 = eb8 * cos8Phi;

        double t2e2c2 = tan2Phi * e2c2;
        double t2e4c4 = tan2Phi * e4c4;
        double t2e6c6 = tan2Phi * e6c6;
        double t2e8c8 = tan2Phi * e8c8;

        double nu = ellipsoid.verticalRadiusOfCurvature(phi);
        double kn1 = nu * Math.sin(phi);
        double t1 = ellipsoid.meridionalArc(phi);
        double t2 = kn1 * cosPhi / 2.0;
        double t3 = (kn1 * cos3Phi / 24.0) * (5.0 - tan2Phi + 9.0 * e2c2 + 4.0 * e4c4);
        double t4 = (kn1 * cos5Phi / 720.0) * (61.0 - 58.0 * tan2Phi + tan4Phi + 270.0 * e2c2 - 330.0 * t2e2c2 + 445.0 * e4c4 - 680.0 * t2e4c4 + 324.0 * e6c6 - 600.0 * t2e6c6 + 88.0 * e8c8 - 192.0 * t2e8c8);
        double t5 = (kn1 * cos7Phi / 40320.0) * (1385.0 - 3111.0 * tan2Phi + 543.0 * tan4Phi - tan6Phi);

        double kn2 = nu;
        double t6 = kn2 * cosPhi;
        double t7 = (kn2 * cos3Phi / 6.0) * (1.0 - tan2Phi + e2c2);
        double t8 = (kn2 * cos5Phi / 120.0) * (5.0 - 18.0 * tan2Phi + tan4Phi + 14.0 * e2c2 - 58.0 * t2e2c2 + 13.0 * e4c4 - 64.0 * t2e4c4 + 4.0 * e6c6 - 24.0 * t2e6c6);
        double t9 = (kn2 * cos7Phi / 50.40) * (61.0 - 479.0 * tan2Phi + 179.0 * tan4Phi - tan6Phi);

        double lambda = lon / GpsUtils.RHO;
        double lambda0 = getCentralMeridian(longitudeZone, latitudeZone);
        double dL = lambda - lambda0;
        double dL2 = dL * dL;
        double dL3 = dL2 * dL;
        double dL4 = dL3 * dL;
        double dL5 = dL4 * dL;
        double dL6 = dL5 * dL;
        double dL7 = dL6 * dL;
        double dL8 = dL7 * dL;

        double falseEasting = FALSE_EASTING;
        falseEasting += (longitudeZone * 1000000);
        double northing = t1 + dL2 * t2 + dL4 * t3 + dL6 * t4 + dL8 * t5;
        double easting = falseEasting + dL * t6 + dL3 * t7 + dL5 * t8 + dL7 * t9;

        valueOf(longitudeZone, latitudeZone, easting, northing);

        /** interesting conversion from S42 to UTM */
        northing += 40;
        easting += 100;
        
        result[0] = northing;
        result[1] = easting;
        return result;
    }

    public double[] projectionToSphere(double x, double y) {
        double[] result = new double[2];

        return result;
    }

    /**
     * Returns the latitude zone identifier for the specified coordinates.
     *
     * @param latLong The coordinates.
     * @return the latitude zone character.
     */
    private char getLatitudeZone(double lat, double lon) {
        if (isNorthPolar(lat)) {
            if (lon < 0) {
                return 'Y';
            } else {
                return 'Z';
            }
        }
        if (isSouthPolar(lat)) {
            if (lon < 0) {
                return 'A';
            } else {
                return 'B';
            }
        }

        char zone = (char) ((lat + 80) / 8 + 'C');
        if (zone > 'H') {
            zone++;
        }
        if (zone > 'N') {
            zone++;
        }
        if (zone > 'X') {
            zone = 'X';
        }
        return zone;
    }

    /**
     * Returns the UTM/UPS longitude zone number for the specified
     * coordinates.
     *
     * @param latLong  The coordinates.
     * @return the longitude zone number.
     */
    private int getLongitudeZone(double lat, double lon) {
        return (int) (lon / 6) + 1;
    }

    /**
     * Returns true if the position indicated by the coordinates is
     * north of the northern limit of the UTM grid (84 degrees).
     *
     * @param latLong The coordinates.
     * @return True if the latitude is greater than 84 degrees.
     */
    private boolean isNorthPolar(double lat) {
        return lat > 84.0;
    }

    /**
     * Returns true if the position indicated by the coordinates is
     * south of the southern limit of the UTM grid (-80 degrees).
     *
     * @param latLong The coordinates.
     * @return True if the latitude is less than -80 degrees.
     */
    private boolean isSouthPolar(double lat) {
        return lat < -80.0;
    }
    
   /**
     * Returns the central meridian (in radians) for the specified
     * UTM/UPS zone.
     * @param longitudeZone The UTM/UPS longitude zone number.
     * @param latitudeZone  The UTM/UPS latitude zone character.
     * @return The central meridian for the specified zone.
     */
    private double getCentralMeridian(int longitudeZone, char latitudeZone) {
//        // polar zones
//        if (latitudeZone < 'C' || latitudeZone > 'X') {
//            return 0.0;
//        }
//        // X latitude zone exceptions
//        if (latitudeZone == 'X' && longitudeZone > 31 && longitudeZone <= 37) {
//            return Math.toRadians((longitudeZone - 1) * 6 - 180 + 4.5);
//        }
//        // V latitude zone exceptions
//        if (longitudeZone == 'V') {
//            if (latitudeZone == 31) {
//                return Math.toRadians(1.5);
//            } else if (latitudeZone == 32) {
//                return Math.toRadians(7.5);
//            }
//        }
        return Math.toRadians((longitudeZone - 1) * 6 + 3);
    }
}
