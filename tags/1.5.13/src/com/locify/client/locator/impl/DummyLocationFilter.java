/*
 * DummyLocationFilter.java
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
package com.locify.client.locator.impl;

import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationFilter;
import com.locify.client.locator.LocationSample;

/**
 * Trivial null location filter which does nothing
 * @author Jiri Stepan
 */
public class DummyLocationFilter implements LocationFilter {

    LocationSample lastSample;

    public void addLocationSample(LocationSample locSamp) {
        this.lastSample = locSamp;
    }

    public Location4D getFilteredLocation() {
        return new Location4D(lastSample.getLatitude(), lastSample.getLongitude(), (float)lastSample.getAltitude());
    }

    public float getFilteredSpeed() {
        return 0;
    }

    public float getFilteredCourse() {
        return 0;
    }
}
