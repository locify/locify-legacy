/*
 * CalibrationPoint.java
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

/**
 *
 * @author menion
 */
public class CalibrationPoint {

    public int x;
    public int y;
    public Location4D position;

    public CalibrationPoint() {
        x = 0;
        y = 0;
        position = new Location4D(0.0, 0.0, 0.0f);
    }

    public String toString() {
        return "[" + x + "," + y + "] = {" + position.getLongitude() + "," + position.getLatitude() + "}";
    }
}
