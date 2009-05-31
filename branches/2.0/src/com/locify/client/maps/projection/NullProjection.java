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


/**
 * Dummy map projection
 * @author MenKat
 */
public class NullProjection extends Projection {

    public NullProjection() {
        super(ReferenceEllipsoid.WGS84);
    }
    
    public double[] projectionToFlat(double lat, double lon) {
        double[] result = new double[2];
        result[0] = lat;
        result[1] = lon;
        return result;
    }

    public double[] projectionToSphere(double x, double y) {
        double[] result = new double[2];
        result[0] = x;
        result[1] = y;
        return result;
    }

}