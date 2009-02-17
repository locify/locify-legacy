/*
 * Waypoint.java
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

package com.locify.client.data.items;

import com.locify.client.locator.Location4D;

/**
 * This is object for representing waypoint
 * @author Destil
 */
public class Waypoint extends GeoData {
    private Location4D location;
    
    public Waypoint(double latitude, double longitude, String name, String description) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
    }
    
    /** returns Location4D instance of this wp */
    public Location4D getLocation(){
        if (location==null){
            location = new Location4D(latitude, longitude, 0);
        }
        return location;
    }
}
