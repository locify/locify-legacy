/*
 * LocationFilter.java
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
 * Abstract interface for location filters
 * @author Jiri Stepan
 */
public interface LocationFilter {

    public abstract Location4D getFilteredLocation();

    /**
     * @return filtered speed in m/s
     */
    public abstract float getFilteredSpeed();

    public abstract float getFilteredCourse();

    public abstract void addLocationSample(LocationSample locSamp);
}
