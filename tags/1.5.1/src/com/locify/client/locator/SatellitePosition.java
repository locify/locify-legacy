/*
 * SatellitePosition.java
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
 * Representing one satellite
 * @author menion
 */
public class SatellitePosition {

    protected Integer prn;
    protected float azimuth;
    protected float elevation;
    /** signal to noise ratio */
    protected int snr;

    public SatellitePosition() {
        this.prn = new Integer(0);
        this.azimuth = 0.0f;
        this.elevation = 0.0f;
        this.snr = 0;
    }

    public Integer getPrn() {
        return prn;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getElevation() {
        return elevation;
    }

    public int getSnr() {
        return snr;
    }
}
