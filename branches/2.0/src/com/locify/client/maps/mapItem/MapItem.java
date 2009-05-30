/*
 * MapItem.java
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
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.FileMapLayer;
import com.locify.client.maps.RectangleViewPort;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.R;
import com.locify.client.utils.ResourcesLocify;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import java.util.Vector;

/**
 * Abstract map item
 * @author MenKat
 */
public abstract class MapItem {

    /* possibles state */
    protected static int STATE_WAITING = 1;
    protected static int STATE_INITIALIZING = 2;
    protected static int STATE_DRAWING = 2;
    protected int actualState;

    // state values
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_HIGH = 3;
    protected int priority;
    protected boolean fixed;
    protected boolean enabled = true;
    protected boolean initialized = false;
    /** screen viewport in pixels */
    protected RectangleViewPort screenViewPort;
    /** item viewport in pixels */
    protected RectangleViewPort itemViewPort;
    protected Location4D positionTopLeft;
    protected Location4D positionBottomRight;
    protected MapScreen mapScreen;

    /* images */
    protected static GeoFileStyle stylePointIconNormal;
    protected static GeoFileStyle stylePointIconHighlight;
    protected static Image waypointDescriptionBackground;
    protected static Image waypointDescription01;
    protected static Image waypointDescription02;
    protected static Image waypointDescription03;

    public MapItem() {
        mapScreen = R.getMapScreen();
        screenViewPort = new RectangleViewPort(0, 0, Capabilities.getWidth(), Capabilities.getHeight());

        if (stylePointIconNormal == null) {
            stylePointIconNormal = new GeoFileStyle("StylePointNormal");
            stylePointIconNormal.setIcon("locify://icons/map_point_orange_21x21.png");
        }
        if (stylePointIconHighlight == null) {
            stylePointIconHighlight = new GeoFileStyle("StylePOintHighLight");
            stylePointIconHighlight.setIcon(stylePointIconNormal.getIcon().scaled(
                    (int) (stylePointIconNormal.getIcon().getWidth() * 1.5),
                    (int) (stylePointIconNormal.getIcon().getHeight() * 1.5)));
        }
        if (waypointDescriptionBackground == null) {
            waypointDescriptionBackground = ResourcesLocify.getImage("wpt_description_background.png");
        }
        if (waypointDescription01 == null) {
            waypointDescription01 = ResourcesLocify.getImage("wpt_description_01.png");
        }
        if (waypointDescription02 == null) {
            waypointDescription02 = ResourcesLocify.getImage("wpt_description_02.png");
        }
        if (waypointDescription03 == null) {
            waypointDescription03 = ResourcesLocify.getImage("wpt_description_03.png");
        }

        actualState = STATE_WAITING;
        priority = PRIORITY_MEDIUM;
        fixed = false;
    }

    /**
     * Initialize object after new scale or new provider is set.
     */
    public abstract void initialize();

    /**
     * If map screen only move, just use pan funcion.
     * @param moveX pixels in axis X.
     * @param moveY pixels in axis Y.
     */
    public abstract void panItem(int moveX, int moveY);

    /**
     * Draw item into apropriate Graphics object
     * @param g
     */
    public abstract void drawItem(Graphics g);

    /**
     * Base on given coordinates and radius find if any of items is inside and add them into data vector.
     * @param data vector containing wapoints
     * @param x center X
     * @param y center Y
     * @param radius radius of tested area
     * @return selected waypoint or null if nothing
     */
    public abstract void getWaypointsAtPositionByPoint(Vector data, int x, int y, int radiusSquare);

    /**
     * Base on given coordinates, find if any of item icon is under this point and add them into data vector.
     * @param data vector containing wapoints
     * @param x center X
     * @param y center Y
     * @return selected waypoint or null if nothing
     */
    public abstract void getWaypointsAtPositionByIcon(Vector data, int x, int y);

    /**
     * Test if stylus touch inside this object
     */
    public abstract boolean touchInside(int x, int y);

    /**
     * Test if viewport of this object is inside actual map screen.
     * @return true if is inside, otherwise false.
     */
    protected boolean isInside() {
        try {
            if (screenViewPort != null && itemViewPort != null) {
                return itemViewPort.intersects(screenViewPort);
            }
            return false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItem.isInside()", null);
            return false;
        }
    }

    public Location4D getItemCenter() {
        return new Location4D((positionTopLeft.getLatitude() + positionBottomRight.getLatitude()) / 2,
                (positionTopLeft.getLongitude() + positionBottomRight.getLongitude()) / 2,
                (positionTopLeft.getAltitude() + positionBottomRight.getAltitude()) / 2);
    }

    public Location4D[] getBoundingLocations() {
        Location4D[] loc = new Location4D[2];
        loc[0] = positionTopLeft;
        loc[1] = positionBottomRight;
        return loc;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        disableInitializeState();
    }

    protected boolean isInitialized() {
        return this.initialized;
    }

    protected void disableInitializeState() {
        this.initialized = false;
    }

    /**
     * Call this immediately after adding new Location4D or Waypoints into item!!!
     * @param points
     */
    protected void initializeFirstly(Vector points) {
        try {
            if (actualState == STATE_WAITING) {
                actualState = STATE_INITIALIZING;
                if (points != null) {
                    boolean location4D = false;
                    if (points.size() > 0) {
                        if (points.elementAt(0) instanceof Location4D) {
                            location4D = true;
                        } else {
                            location4D = false;
                        }
                    }

                    double topLat = Double.NEGATIVE_INFINITY;
                    double bottomLat = Double.POSITIVE_INFINITY;
                    double leftLon = Double.POSITIVE_INFINITY;
                    double rightLon = Double.NEGATIVE_INFINITY;

                    Waypoint tempWpt;
                    Location4D tempLoc4D;
                    for (int i = 0; i < points.size(); i++) {
                        if (points.elementAt(i) != null) {
                            if (!location4D) {
                                tempWpt = (Waypoint) points.elementAt(i);
                                tempLoc4D = new Location4D(tempWpt.getLatitude(), tempWpt.getLongitude(), 0f);
                            } else {
                                tempLoc4D = (Location4D) points.elementAt(i);
                            }
                            topLat = Math.max(topLat, tempLoc4D.getLatitude());
                            bottomLat = Math.min(bottomLat, tempLoc4D.getLatitude());
                            leftLon = Math.min(leftLon, tempLoc4D.getLongitude());
                            rightLon = Math.max(rightLon, tempLoc4D.getLongitude());
                        }
                    }

                    this.positionTopLeft = new Location4D(topLat, leftLon, 0.0f);
                    this.positionBottomRight = new Location4D(bottomLat, rightLon, 0.0f);
                }
            }
            actualState = STATE_WAITING;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItem.initializeFirstly()", null);
        }
    }

    /**
     * Call after every big change in mapItem position.
     * @param points
     * @return
     */
    protected Point2D.Int[] initializePoints(Vector points) {
        try {
            if (points == null || mapScreen.getActualMapLayer() instanceof FileMapLayer &&
                    !((FileMapLayer) mapScreen.getActualMapLayer()).isReady()) {
                return null;
            }

            Point2D.Int[] items = new Point2D.Int[points.size()];
            if (actualState == STATE_WAITING) {
                actualState = STATE_INITIALIZING;

                boolean location4D = false;
                if (points.size() > 0) {
                    if (points.elementAt(0) instanceof Location4D) {
                        location4D = true;
                    } else {
                        location4D = false;
                    }
                }

                int top = Integer.MAX_VALUE;
                int bottom = Integer.MIN_VALUE;
                int left = Integer.MAX_VALUE;
                int right = Integer.MIN_VALUE;

                Waypoint tempWpt;
                Location4D tempLoc4D;
                for (int i = 0; i < items.length; i++) {
                    if (points.elementAt(i) != null) {
                        if (!location4D) {
                            tempWpt = (Waypoint) points.elementAt(i);
                            tempLoc4D = new Location4D(tempWpt.getLatitude(), tempWpt.getLongitude(), 0f);
                        } else {
                            tempLoc4D = (Location4D) points.elementAt(i);
                        }

//                    Thread.sleep(5);
                        items[i] = mapScreen.getActualMapLayer().getLocationCoord(tempLoc4D);
//                    Thread.sleep(5);

                        top = Math.min(top, items[i].y);
                        bottom = Math.max(bottom, items[i].y);
                        left = Math.min(left, items[i].x);
                        right = Math.max(right, items[i].x);
                        if (right == left) {
                            right++;
                        }
                        if (bottom == top) {
                            bottom++;
                        }
                    }
                }

                this.itemViewPort = new RectangleViewPort(left, top, right - left, bottom - top);

                initialized = true;
            }
            actualState = STATE_WAITING;
            return items;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItem.initializePoints()", null);
            return null;
        }
    }

    protected void panItems(Point2D.Int[] items, int moveX, int moveY) {
        try {
            if (enabled && initialized) {
                actualState = STATE_INITIALIZING;
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null) {
                        items[i].setLocation(items[i].x + moveX, items[i].y + moveY);
                    }
                }

                itemViewPort.x += moveX;
                itemViewPort.y += moveY;
            }
            actualState = STATE_WAITING;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItem.panItems()", null);
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
