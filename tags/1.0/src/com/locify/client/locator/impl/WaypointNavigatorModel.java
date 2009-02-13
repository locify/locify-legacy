/*
 * WaypointNavigatorModel.java
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
import com.locify.client.locator.Navigator;

import com.locify.client.data.items.Waypoint;
/**
 * Navigator to waypoint.
 * @author Jiri
 */
public class WaypointNavigatorModel implements Navigator {
	
	private Location4D target;
	private String targetName;

        /** 
         * Creates new waypoint navigator
         * @param target 
         * @param name
         */
        public WaypointNavigatorModel(Location4D target, String name) {
            this.target = target;
            this.targetName = name;
        }
	
	public WaypointNavigatorModel(Waypoint waypoint){
		this.targetName=waypoint.getName();
		this.target=waypoint.getLocation();
    }

	public double getAzimuthToTaget(Location4D actualPos) {
		return actualPos.azimutTo(target);
	}

	public double getDistanceToTarget(Location4D actualPos) {
		return actualPos.distanceTo(target);
	}
	
	public String getToName() {
		return targetName;
	}
        
        public String getMessage(){
            return ""; //follow the heading
        }
} 