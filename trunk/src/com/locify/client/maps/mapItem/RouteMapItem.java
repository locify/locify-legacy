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

import com.locify.client.data.items.GeoFileStyle;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.ColorsFonts;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Route on map
 * @author Menion
 */
public class RouteMapItem extends MapItem {

    /** default Location4D data */
    private Vector points;
    /** vector containing separatings value */
    private Vector separating;
    /** array of actualy selected points */
    private Vector selectedPoints;
    /** points for painting */
    private Point2D.Int[] items;
    /** style of whole route */
    private GeoFileStyle styleNormal;
    private GeoFileStyle styleHightLight;

    /**
     * Constructor
     * @param points Location4D vector of points
     */
    public RouteMapItem(Vector points) {
        super();
        setVectorLocation4D(points);
        this.selectedPoints = new Vector();
    }

    /**
     * Constructor
     * @param route Route object
     */
    public RouteMapItem(Route route) {
        this(route.getPoints());
        this.separating = route.getSeparating();
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
                for (int i = 0; i < items.length; i++) {
                    space = false;
                    if (separating != null) {
                        for (int j = 0; j < separating.size(); j++) {
                            if (i == ((Integer) separating.elementAt(j)).intValue()) {
                                space = true;
                            }
                        }
                    }

                    if (i != 0) {
                        if (space)
                            g.setColor(ColorsFonts.MAP_TRACK_COLOR_SPACE);
                        else
                            g.setColor(ColorsFonts.MAP_TRACK_COLOR);

                        g.drawLine(items[i - 1].x, items[i - 1].y, items[i].x, items[i].y);
                    }

                    // draw points
                    // TODO: biggest problem is creating new Integer for every point !!!
                    if (selectedPoints.size() > 0 && selectedPoints.contains(new Integer(i)))
                        continue;
                        
                    if (styleNormal != null && styleNormal.getIcon() != null) {
                        g.drawImage(styleNormal.getIcon(),
                                items[i].x - styleNormal.getXMove(), items[i].y - styleNormal.getYMove(),
                                Graphics.BOTTOM | Graphics.LEFT);
                    } else {
                        g.setColor(ColorsFonts.MAP_WP_COLOR);
                        g.fillArc(items[i].x - 2, items[i].y - 2, 4, 4, 0, 360);
                    }
                }
            }
            actualState = STATE_WAITING;
        }
    }

    public void getWaypointsAtPosition(Vector data, int x, int y, int radiusSquare) {
        selectedPoints.removeAllElements();
        if (initialized) {
            Waypoint tempWpt;
            Point2D.Int item;
            for (int i = 0; i < items.length; i++) {
                item = items[i];
                if (((item.x - x) * (item.x - x) + (item.y - y) * (item.y - y)) <= radiusSquare) {
                    tempWpt = new Waypoint(
                                ((Location4D) points.elementAt(i)).getLatitude(),
                                ((Location4D) points.elementAt(i)).getLongitude(),
                                "Route waypoint", "waypoint " + (i + 1), null, styleNormal, styleHightLight);
                    tempWpt.state = Waypoint.STATE_HIGHLIGHT;
                    data.addElement(tempWpt);
                    selectedPoints.addElement(new Integer(i));
                }
            }
        }
    }

    /**
     * Set Array of Location4D object as point of this route.
     * @param locations Vector of Location4D object.
     */
    public void setVectorLocation4D(Vector locations) {
        this.points = locations;
        this.separating = null;
        initialize();
    }

    /**
     * Set Array of Location4D object as point of this route.
     * @param locations Vector of Location4D object.
     */
    public void setVectorWaypoints(Vector waypoints) {
        this.points = new Vector();
        Location4D loc;
        Waypoint way;
        for (int i = 0; i < waypoints.size(); i++) {
            way = (Waypoint) waypoints.elementAt(i);
            loc = new Location4D(way.getLatitude(), way.getLongitude(), 0.0f);
            points.addElement(loc);
        }
        this.separating = null;
        initialize();
    }

    public void setStyles(GeoFileStyle styleNormal, GeoFileStyle styleHightLight) {
        //System.out.println("Set style: " + styleNormal.getName() + " " + styleHightLight.getName());
        this.styleNormal = styleNormal;
        this.styleHightLight = styleHightLight;
    }

    public boolean touchInside(int x, int y) {
        return false;
    }
}