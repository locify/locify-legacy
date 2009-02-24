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
import com.locify.client.utils.ColorsFonts;
import javax.microedition.lcdui.Graphics;

/**
 * This is object for representing waypoint
 * @author Destil
 */
public class Waypoint extends GeoData {
    
    /** normal state */
    public static final int STATE_NONE = 0;
    /** item is hightlighted */
    public static final int STATE_HIGHLIGHT = 1;
    /** selected item showing info */
    public static final int STATE_SELECTED = 2;

    private Location4D location;
    public int state;
    
    public Waypoint(double latitude, double longitude, String name, String description) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.state = STATE_NONE;
    }

    public Waypoint(double latitude, double longitude, String name, String description,
            GeoFileStyle styleNormal, GeoFileStyle styleHightLight) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;

        this.styleNormal = styleNormal;
        this.styleHighlight = styleHightLight;
        
        this.state = STATE_NONE;
    }
    
    /** returns Location4D instance of this wp */
    public Location4D getLocation(){
        if (location==null){
            location = new Location4D(latitude, longitude, 0);
        }
        return location;
    }

    public void setStyleHighLight(GeoFileStyle styleHighlight) {
        this.styleHighlight = styleHighlight;
    }

    public String toString() {
        return super.toString();
    }

    public void paint(Graphics g, int x, int y) {
        if (state == Waypoint.STATE_NONE) {
            if (styleNormal != null) {
                g.drawImage(styleNormal.icon,
                        x - styleNormal.getXMove(), y - styleNormal.getYMove(),
                        Graphics.BOTTOM | Graphics.LEFT);
            } else {
                g.setColor(ColorsFonts.MAP_WP_COLOR);
                g.drawArc(x - 2, y - 2, 4, 4, 0, 360);
            }
        } else if (state == Waypoint.STATE_HIGHLIGHT) {
            if (styleHighlight != null) {
                g.drawImage(styleHighlight.icon,
                        x - styleHighlight.getXMove(), y - styleHighlight.getYMove(),
                        Graphics.BOTTOM | Graphics.LEFT);
            } else {
                g.setColor(ColorsFonts.MAP_WP_COLOR_HIGHLIGHT);
                g.fillArc(x - 3, y - 3, 6, 6, 0, 360);
            }
        } else if (state == Waypoint.STATE_SELECTED) {
            return;
        }
    }
}
