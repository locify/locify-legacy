/*
 * NullProjection.java
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

import com.locify.client.utils.math.LMath;


/**
 * Dummy map projection
 * @author MenKat
 */
public class MercatorProjection extends Projection {

    public MercatorProjection() {
        super(ReferenceEllipsoid.WGS84);
    }
    
    public double[] projectionToFlat(double lat, double lon) {
        lat = lat / LMath.RHO;
        lon = lon / LMath.RHO;

        double[] result = new double[2];
        result[0] = ellipsoid.a * LMath.ln(Math.tan(lat/2 + Math.PI/4));
        result[1] = ellipsoid.a * lon;
//System.out.println("\nProjectionToFlat: " + lat + " " + (lat/2 + Math.PI/4) + " " + Math.tan(lat/2 + Math.PI/4) +
//        " " + LMath.ln(Math.tan(lat/2 + Math.PI/4)));
        return result;
    }

    public double[] projectionToSphere(double X, double Y) {
        double[] result = new double[2];
        result[0] = (2 * LMath.atan(LMath.exp((Y / ellipsoid.a))) - Math.PI / 2) * LMath.RHO;
        result[1] = X / ellipsoid.a * LMath.RHO;
//System.out.println("\nProjectionToSphere: " + X + " " + Y + " " + result[0] + " " + result[1]);
        return result;
    }

}
