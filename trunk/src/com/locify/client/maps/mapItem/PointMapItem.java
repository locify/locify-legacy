/*
 * PointMapItem.java
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
import com.locify.client.data.items.Waypoint;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.R;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * One point on map
 * @author MenKat
 */
public class PointMapItem extends MapItem {

    /** default waypoints data */
    private Vector waypoints;
    /** points for painting */
    private Point2D.Int[] items;
    /** array of actualy selected points */
    private Vector selectedPoints;

    public PointMapItem(Vector waypoints) {
        super();
        this.waypoints = waypoints;
        this.selectedPoints = new Vector();
        initializeFirstly(waypoints);
        initialize();
    }

    public int getItemSize() {
        return waypoints.size();
    }

    public Waypoint getWaypoint(int index) {
        if (index < waypoints.size()) {
            return (Waypoint) waypoints.elementAt(index);
        }
        return null;
    }

    public void initialize() {
        try {
            items = initializePoints(waypoints);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "PointMapItem.initialize()", null);
        }
    }

    public void panItem(int moveX, int moveY) {
        panItems(items, moveX, moveY);
    }

    public void drawItem(Graphics g) {
        if (enabled) {
            if (!initialized) {
                initialize();
            }

            if (isInside() && actualState == STATE_WAITING && items != null) {
                actualState = STATE_DRAWING;

                GeoFileStyle style = null;
                for (int i = 0; i < items.length; i++) {
                    // draw points
                    if (selectedPoints.size() > 0 && selectedPoints.contains(new Integer(i))) {
                        continue;
                    }

                    style = ((Waypoint) waypoints.elementAt(i)).getStyleNormal();
                    if (style == null || style.getIcon() == null) {
                        style = stylePointIconNormal;
                    }

                    g.drawImage(style.getIcon(),
                            items[i].x - style.getXMove(), items[i].y - style.getYMove(),
                            Graphics.BOTTOM | Graphics.LEFT);
                }
            }
            actualState = STATE_WAITING;
        }
    }

    public void getWaypointsAtPositionByPoint(Vector data, int x, int y, int radiusSquare) {
        selectedPoints.removeAllElements();
//Logger.debug("PointMapItem.getWaypointsAtPosition() init: " + initialized + " wCount: " + items.length);
        if (initialized) {
            Waypoint tempWpt;
            for (int i = 0; i < items.length; i++) {
                Point2D.Int item = items[i];
                if (((item.x - x) * (item.x - x) + (item.y - y) * (item.y - y)) <= radiusSquare) {
                    tempWpt = (Waypoint) waypoints.elementAt(i);
                    if (tempWpt.getStyleHighLight() == null) {
                        tempWpt.setStyleHighLight(stylePointIconHighlight);
                    }
                    tempWpt.state = Waypoint.STATE_HIGHLIGHT;
                    data.addElement(tempWpt);
                    selectedPoints.addElement(new Integer(i));
                }
            }
        }
    }

    public void getWaypointsAtPositionByIcon(Vector data, int x, int y) {
//Logger.debug("PointMapItem.getWaypointsAtPositionByIcon() init: " + initialized + " wCount: " + items.length);
        if (initialized) {
            Waypoint tempWpt;
            for (int i = 0; i < items.length; i++) {
                Point2D.Int item = items[i];
                tempWpt = (Waypoint) waypoints.elementAt(i);

                GeoFileStyle styleNormal = tempWpt.getStyleNormal();
                if (styleNormal == null || styleNormal.getIcon() == null) {
                    styleNormal = stylePointIconNormal;
                }
//Logger.debug("  get at: " + x + " " + y);
//Logger.debug("  icon: " + styleNormal.getIcon().getWidth() + " " + styleNormal.getIcon().getHeight());
//Logger.debug("  get at: " + (item.y - styleNormal.getIcon().getHeight() + styleNormal.getYMove()) +
//        " " + (item.y + styleNormal.getYMove()) + " " + (item.x - styleNormal.getXMove()) + " " +
//        (item.x + styleNormal.getIcon().getWidth() - styleNormal.getXMove()));

                if (y >= (item.y - styleNormal.getIcon().getHeight() + styleNormal.getYMove()) &&
                        y <= (item.y + styleNormal.getYMove()) &&
                        x >= (item.x - styleNormal.getXMove()) &&
                        x <= (item.x + styleNormal.getIcon().getWidth() - styleNormal.getXMove())) {
                    data.addElement(tempWpt);
                }
            }
        }
    }

    public Waypoint getWaypointById(String id) {
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wpt = (Waypoint) waypoints.elementAt(i);
            if (wpt.id != null && wpt.id.equals(id)) {
                return wpt;
            }
        }
        return null;
    }

    public boolean touchInside(int x, int y) {
        return false;
    }
}
