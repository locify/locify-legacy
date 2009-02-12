/*
 * RouteMapItem.java
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
package com.locify.client.maps.mapItem;

import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.ColorsFonts;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Route on map
 * @author MenKat
 */
public class RouteMapItem extends MapItem {

    /** default Location4D data */
    private Vector points;
    /** vector containing separatings value */
    private Vector separating;
    /** points for painting */
    private Point2D.Int[] items;

    /**
     * Constructor
     * @param points Location4D vector of points
     */
    public RouteMapItem(Vector points) {
        super();
        this.points = points;
        initialize();
    }

    /**
     * Constructor
     * @param route Route object
     */
    public RouteMapItem(Route route) {
        super();
        this.points = route.getPoints();
        this.separating = route.getSeparating();
        initialize();
    }

    public void initialize() {
        items = initializePoints(points);
    }

    public void panItem(int moveX, int moveY) {
        panItems(items, moveX, moveY);
    }
    
    public void drawItem(Graphics g) {
        if (enabled) {
            if (!initialized)
                initialize();
            
            if (isInside() && actualState == STATE_WAITING && items != null) {
                actualState = STATE_DRAWING;
                boolean space;
                g.setColor(ColorsFonts.MAP_WP_COLOR);
                g.fillArc(items[0].x - 1, items[0].y - 1, 3, 3, 0, 360);
                for (int i = 1; i < items.length; i++) {
                    space = false;
                    if (separating != null) {
                        for (int j = 0; j < separating.size(); j++) {
                            if (i == ((Integer) separating.elementAt(j)).intValue()) {
                                space = true;
                            }
                        }
                    }

                    if (space)
                        g.setColor(ColorsFonts.MAP_TRACK_COLOR_SPACE);
                    else
                        g.setColor(ColorsFonts.MAP_TRACK_COLOR);

                    g.drawLine(items[i - 1].x, items[i - 1].y, items[i].x, items[i].y);

                    g.setColor(ColorsFonts.MAP_WP_COLOR);
                    g.fillArc(items[i].x - 2, items[i].y - 2, 4, 4, 0, 360);
                }
            }
            actualState = STATE_WAITING;
        }
    }

    public void getWaypointsAtPosition(Vector data, int x, int y, int radius) {
        for (int i = 0; i < items.length; i++) {
            Point2D.Int item = items[i];
            if (Math.sqrt((item.x - x) * (item.x - x) + (item.y - y) * (item.y - y)) <= radius)
                data.addElement(
                        new Waypoint(
                            ((Location4D) points.elementAt(i)).getLatitude(),
                            ((Location4D) points.elementAt(i)).getLongitude(),
                            "Route waypoint", "waypoint " + i)
                        );
        }
    }
    
    public void setNewVectorData(Vector locations) {
        this.points = locations;
        initialize();
    }

    public boolean touchInside(int x, int y) {
        return false;
    }
}