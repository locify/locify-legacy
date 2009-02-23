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

import com.locify.client.data.items.Waypoint;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.RectangleViewPort;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.R;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

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
    
    /** screen viewport in pixels */
    protected RectangleViewPort screenViewPort;
    
    /** item viewport in pixels */
    protected RectangleViewPort itemViewPort;
    protected Location4D positionTopLeft;
    protected Location4D positionBottomRight;
    
    /* temp objects */
    private Waypoint tempWpt;
    private Location4D tempLoc4D;
    
    protected MapScreen mapScreen;

    /* images */
    protected static Image displayedPointIcon;
    protected static Image waypointDescriptionBackground;
    protected static Image waypointDescription01;
    protected static Image waypointDescription02;
    protected static Image waypointDescription03;

    protected boolean enabled = true;
    protected boolean initialized = false;
    
    public MapItem() {
        mapScreen = R.getMapScreen();
        screenViewPort = new RectangleViewPort(0, 0, mapScreen.getWidth(), mapScreen.getHeight());
        try {
            if (displayedPointIcon == null)
                displayedPointIcon = Image.createImage("/map_point_orange_21x21.png");
            if (waypointDescriptionBackground == null)
                waypointDescriptionBackground = Image.createImage("/wpt_description_background.png");
            if (waypointDescription01 == null)
                waypointDescription01 = Image.createImage("/wpt_description_01.png");
            if (waypointDescription02 == null)
                waypointDescription02 = Image.createImage("/wpt_description_02.png");
            if (waypointDescription03 == null)
                waypointDescription03 = Image.createImage("/wpt_description_03.png");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        actualState = STATE_WAITING;
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
    public abstract void getWaypointsAtPosition(Vector data, int x, int y, int radius);
   
    /**
     * Test if stylus touch inside this object
     */
    public abstract boolean touchInside(int x, int y);

    /**
     * Test if viewport of this object is inside actual map screen.
     * @return true if is inside, otherwise false.
     */
    protected boolean isInside() {
        if (screenViewPort != null && itemViewPort != null) {
            return itemViewPort.intersects(screenViewPort);
        }
        return false;
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
    }
    
    protected boolean initialized() {
        return this.initialized;
    }
    
    protected void disableInitializeState() {
        this.initialized = false;
    }
    
    protected Point2D.Int[] initializePoints(Vector points) {
        try {
            Point2D.Int[] items = new Point2D.Int[points.size()];
            if (actualState == STATE_WAITING) {
                actualState = STATE_INITIALIZING;

                boolean location4D = false;
                if (points.size() > 0) {
                    if (points.elementAt(0) instanceof Location4D)
                        location4D = true;
                    else
                        location4D = false;
                }

                int top = Integer.MAX_VALUE;
                int bottom = Integer.MIN_VALUE;
                int left = Integer.MAX_VALUE;
                int right = Integer.MIN_VALUE;
                double topLat = Double.MIN_VALUE;
                double bottomLat = Double.MAX_VALUE;
                double leftLon = Double.MAX_VALUE;
                double rightLon =  Double.MIN_VALUE;

                for (int i = 0; i < items.length; i++) {
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

                    items[i] = mapScreen.getActualMapLayer().getLocationCoord(tempLoc4D);

                    top = Math.min(top, items[i].y);
                    bottom = Math.max(bottom, items[i].y);
                    left = Math.min(left, items[i].x);
                    right = Math.max(right, items[i].x);
                    if (right == left)
                        right++;
                    if (bottom == top)
                        bottom++;
                }

                this.itemViewPort = new RectangleViewPort(left, top, right - left, bottom - top);
                this.positionTopLeft = new Location4D(topLat, leftLon, 0.0f);
                this.positionBottomRight = new Location4D(bottomLat, rightLon, 0.0f);

                initialized = true;
            }
            actualState = STATE_WAITING;
            return items;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItem.initialize()", null);
            return null;
        }
    }
    
    protected void panItems(Point2D.Int[] items, int moveX, int moveY) {
//        if (enabled && actualState == STATE_WAITING) {
        if (enabled) {
            actualState = STATE_INITIALIZING;
            for (int i = 0; i < items.length; i++) {
                items[i].setLocation(items[i].x + moveX, items[i].y + moveY);
            }
            
            itemViewPort.x += moveX;
            itemViewPort.y += moveY;
        }
        actualState = STATE_WAITING;
    }
}
