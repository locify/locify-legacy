/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package com.locify.client.maps.projection;

import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.math.LMath;

/**
 * <p> The ReferenceEllipsoid class defines a geodetic reference ellipsoid
 *     used as a standard for geodetic measurements. The World Geodetic System
 *     1984 (WGS84) ellipsoid is the current standard for most geographic and
 *     geodetic coordinate systems, including GPS. The WGS84 ellipsoid is
 *     provided as a static instance of this class.</p>
 *
 * <p> The ellipsoid (actually an oblate spheroid) is uniquely specified by
 *     two parameters, the semimajor (or equatorial) radius and the ellipticity
 *     or flattening. In practice, the reciprocal of the flattening is
 *     specified.</p>
 *
 * <p> The ellipsoid is an approximation of the shape of the earth. Although
 *     not exact, the ellipsoid is much more accurate than a spherical
 *     approximation and is still mathematically simple. The <i>geoid</i> is
 *     a still closer approximation of the shape of the earth (intended to
 *     represent the mean sea level), and is generally specified by it's
 *     deviation from the ellipsoid.</p>
 *
 * <p> Different reference ellipsoids give more or less accurate results at
 *     different locations, so it was previously common for different nations
 *     to use ellipsoids that were more accurate for their areas. More recent
 *     efforts have provided ellipsoids with better overall global accuracy,
 *     such as the WGS84 ellipsiod, and these have now largely supplanted
 *     the others.</p>
 *
 * @author Paul D. Anderson, upgraded Menion
 */
public class ReferenceEllipsoid {

//	Ellipsoid( -1, "Placeholder", 0, 0),//placeholder only, To allow array indices to match id numbers
//	Ellipsoid( 1, "Airy", 6377563, 0.00667054),
//	Ellipsoid( 2, "Australian National", 6378160, 0.006694542),
//	Ellipsoid( 3, "Bessel 1841", 6377397, 0.006674372),
//	Ellipsoid( 4, "Bessel 1841 (Nambia) ", 6377484, 0.006674372),
//	Ellipsoid( 5, "Clarke 1866", 6378206, 0.006768658),
//	Ellipsoid( 6, "Clarke 1880", 6378249, 0.006803511),
//	Ellipsoid( 7, "Everest", 6377276, 0.006637847),
//	Ellipsoid( 8, "Fischer 1960 (Mercury) ", 6378166, 0.006693422),
//	Ellipsoid( 9, "Fischer 1968", 6378150, 0.006693422),
//	Ellipsoid( 10, "GRS 1967", 6378160, 0.006694605),
//	Ellipsoid( 11, "GRS 1980", 6378137, 0.00669438),
//	Ellipsoid( 12, "Helmert 1906", 6378200, 0.006693422),
//	Ellipsoid( 13, "Hough", 6378270, 0.00672267),
//	Ellipsoid( 14, "International", 6378388, 0.00672267),
//	Ellipsoid( 15, "Krassovsky", 6378245, 0.006693422),
//	Ellipsoid( 16, "Modified Airy", 6377340, 0.00667054),
//	Ellipsoid( 17, "Modified Everest", 6377304, 0.006637847),
//	Ellipsoid( 18, "Modified Fischer 1960", 6378155, 0.006693422),
//	Ellipsoid( 19, "South American 1969", 6378160, 0.006694542),
//	Ellipsoid( 20, "WGS 60", 6378165, 0.006693422),
//	Ellipsoid( 21, "WGS 66", 6378145, 0.006694542),
//	Ellipsoid( 22, "WGS-72", 6378135, 0.006694318),
//	Ellipsoid( 23, "WGS-84", 6378137, 0.00669438)

    /**
     * The World Geodetic System 1984 reference ellipsoid.
     */
    public static final ReferenceEllipsoid WGS84
        = new ReferenceEllipsoid(6378137.0, 298.257223563);
    /**
     * The ellipsoid defined for system S42
     */
    public static final ReferenceEllipsoid KRASOVSKY
        = new ReferenceEllipsoid(6378245.0, 298.3);
    
    public static final ReferenceEllipsoid BESSEL
        = new ReferenceEllipsoid(6377397.155, 299.2);
    
    public static final ReferenceEllipsoid HAYFORD
        = new ReferenceEllipsoid(6378388.0, 297.0);
    /**
     * Geodetic Reference System 1980 ellipsoid.
     */
    public static final ReferenceEllipsoid GRS80
        = new ReferenceEllipsoid(6378137.0, 298.257222101);
    /**
     * The World Geodetic System 1972 reference ellipsoid.
     */
    public static final ReferenceEllipsoid WGS72
        = new ReferenceEllipsoid(6378135.0, 298.26);
    /**
     * The International 1924 reference ellipsoid, one of the earliest
     * "global" ellipsoids.
     */
    public static final ReferenceEllipsoid INTERNATIONAL1924
        = new ReferenceEllipsoid(6378388.0, 297.0);

    protected final double a;

    private final double b;

    private final double f;

    private final double ea2;

    private final double e;

    private final double eb2;

    /**
     *  Constructs an instance of a reference ellipsoid.
     *
     * @param semimajorAxis The semimajor or equatorial radius of this
     * reference ellipsoid, in meters.
     * @param inverseFlattening The reciprocal of the ellipticity or flattening
     * of this reference ellipsoid (dimensionless).
     */
    public ReferenceEllipsoid(double semimajorAxis, double inverseFlattening) {
        this.a = semimajorAxis;
        f = 1.0 / inverseFlattening;
        b = semimajorAxis * (1.0 - f);
        ea2 = f * (2.0 - f);
        e = Math.sqrt(ea2);
        eb2 = ea2 / (1.0 - ea2);
    }

    private static double sqr(final double x) {
        return x * x;
    }

    /**
     * Returns the flattening or ellipticity of this reference ellipsoid.
     *
     * @return The flattening.
     */
    public double getFlattening() {
        return f;
    }

    /**
     * Returns the (first) eccentricity of this reference ellipsoid.
     *
     * @return The eccentricity.
     */
    public double getEccentricity() {
        return e;
    }

    /**
     * Returns the square of the (first) eccentricity. This number is frequently
     * used in ellipsoidal calculations.
     *
     * @return The square of the eccentricity.
     */
    public double getEccentricitySquared() {
        return ea2;
    }

    /**
     * Returns the square of the second eccentricity of this reference ellipsoid.
     * This number is frequently used in ellipsoidal calculations.
     *
     * @return The square of the second eccentricity.
     */
    public double getSecondEccentricitySquared() {
        return eb2;
    }

    /**
      * Returns the <i>radius of curvature in the prime vertical</i>
      * for this reference ellipsoid at the specified latitude (N).
      *
      * @param phi The local latitude (radians).
      * @return The radius of curvature in the prime vertical (meters).
      */
     public double verticalRadiusOfCurvature(double phi) {
         return a / Math.sqrt(1.0 - (ea2 * sqr(Math.sin(phi))));
     }

    /**
     *  Returns the <i>radius of curvature in the meridian<i>
     *  for this reference ellipsoid at the specified latitude (M).
     *
     * @param phi The local latitude (in radians).
     * @return  The radius of curvature in the meridian (in meters).
     */
    public double meridionalRadiusOfCurvature(final double phi) {
        return verticalRadiusOfCurvature(phi) / (1.0 + eb2 * sqr(Math.cos(phi)));
    }

    /**
     *  Returns the meridional arc, the true meridional distance on the
     * ellipsoid from the equator to the specified latitude, in meters.
     *
     * @param phi   The local latitude (in radians).
     * @return  The meridional arc (in meters).
     */
    public double meridionalArc(final double phi) {
        final double sin2Phi = Math.sin(2.0 * phi);
        final double sin4Phi = Math.sin(4.0 * phi);
        final double sin6Phi = Math.sin(6.0 * phi);
        final double sin8Phi = Math.sin(8.0 * phi);
        final double n = f / (2.0 - f);
        final double n2 = n * n;
        final double n3 = n2 * n;
        final double n4 = n3 * n;
        final double n5 = n4 * n;
        final double n1n2 = n - n2;
        final double n2n3 = n2 - n3;
        final double n3n4 = n3 - n4;
        final double n4n5 = n4 - n5;
        final double ap = a * (1.0 - n + (5.0 / 4.0) * (n2n3) + (81.0 / 64.0) * (n4n5));
        final double bp = (3.0 / 2.0) * a * (n1n2 + (7.0 / 8.0) * (n3n4) + (55.0 / 64.0) * n5);
        final double cp = (15.0 / 16.0) * a * (n2n3 + (3.0 / 4.0) * (n4n5));
        final double dp = (35.0 / 48.0) * a * (n3n4 + (11.0 / 16.0) * n5);
        final double ep = (315.0 / 512.0) * a * (n4n5);
        return ap * phi - bp * sin2Phi + cp * sin4Phi - dp * sin6Phi + ep * sin8Phi;
    }

    /**
     * Convert geodetic coordinates to XYZ coordinates.
     * @param lat Latitude (in degree).
     * @param lon Longitude (in degree).
     * @param alt Altitude (in metres).
     * @return Array of result coordinates [X][Y][Z] (in metres).
     */
    public double[] convertLatLonAltToXYZ(double lat, double lon, double alt) {
        double N = verticalRadiusOfCurvature(lat);
        lat = lat / GpsUtils.RHO;
        lon = lon / GpsUtils.RHO;
        double[] ret = new double[3];
        ret[0] = (N + alt) * Math.cos(lat) * Math.cos(lon); // X
        ret[1] = (N + alt) * Math.cos(lat) * Math.sin(lon); // Y
        ret[2] = (N * (1 - ea2) + alt) * Math.sin(lat); // Z
        return ret;
    }

    /**
     * Convert XYZ coordinates to geodetic coordinates on ellipsoid.
     * @param X coordinate (in metres).
     * @param Y coordinate (in metres).
     * @param Z coordinate (in metres).
     * @return Array of result geodetic coorinates [Latitude] (in degree) [Longitude] (in degree) [Altitude] (in metres).
     */
    public double[] convertXYZToLatLonAlt(double X, double Y, double Z) {
        double latO = -1;
        double lat = 0, alt = 0, N = 0;

        lat = LMath.atan(Z / ((1 - ea2) * Math.sqrt(sqr(X) + sqr(Y))));
        while (Math.abs(lat - latO) > 0.00001) {
            N = verticalRadiusOfCurvature(lat);
            alt = Math.sqrt(sqr(X) + sqr(Y)) / Math.cos(lat) - N;
            latO = lat;
            lat = LMath.atan(Z / ((1 - (N / (N + alt)) * ea2) * Math.sqrt(sqr(X) + sqr(Y))));
        }

        double[] ret = {lat * GpsUtils.RHO, LMath.atan(Y/X) * GpsUtils.RHO, alt};
        return ret;
    }


//    Moldensky shift parameters (m)	    dX	dY   dZ
//    from S-JTSK to WGS84			   589	76	 480
//    from WGS84 to S-42 (CS)			   -26	121   78
//    from S-42 to S-JTSK			      -563 -197	-558
    private static final int dxWGS84toS42 = -26;
    private static final int dyWGS84toS42 = 121;
    private static final int dzWGS84toS42 = 78;

    public static double[] convertWGS84toS42(double lat, double lon) {
        return convertSomethingToSomething(lat, lon, dxWGS84toS42, dyWGS84toS42, dzWGS84toS42,
                WGS84, KRASOVSKY);
    }

    public static double[] convertS42toWGS84(double lat, double lon) {
        return convertSomethingToSomething(lat, lon, -1 * dxWGS84toS42, -1 * dyWGS84toS42, -1 * dzWGS84toS42,
                KRASOVSKY, WGS84);
    }

    public static double[] convertSomethingToSomething(double lat, double lon,
            int dx, int dy, int dz, ReferenceEllipsoid elFrom, ReferenceEllipsoid elTo) {
        double latR = lat / GpsUtils.RHO;
        double lonR = lon / GpsUtils.RHO;

        // temp variables
        double latSin = Math.sin(latR);
        double lonSin = Math.sin(lonR);
        double latCos = Math.cos(latR);
        double lonCos = Math.cos(lonR);
        double tempSin = Math.sin(Math.PI / 180 / 3600);

        double M = elFrom.meridionalRadiusOfCurvature(latR);
        double N = elFrom.verticalRadiusOfCurvature(latR);
        double dlat = (-1 * dx * latSin * lonCos - dy * latSin * lonSin +
                dz * latCos + (elFrom.a * (elTo.f - elFrom.f) +
                elFrom.f * (elTo.a - elFrom.a)) * Math.sin(2 * latR)) / (M * tempSin);
        double dLon = (-1 * dx * lonSin + dy * lonCos) / (N * latCos * tempSin);

        double[] res = new double[2];
        res[0] = lat + dlat / 3600;
        res[1] = lon + dLon / 3600;
        return res;
    }
}