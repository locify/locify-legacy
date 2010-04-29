/*
 * Copyright (c) 2008, Epseelon. All Rights Reserved.
 *
 * This file is part of MobiMap for Java ME.
 *
 * MobiMap for Java ME is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobiMap for Java ME is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please go to http://epseelon.com/mobimap
 * or email us at mobimap@epseelon.com
 */
package com.locify.client.maps;

import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/** Any map layer implementation should implement this interface
 * pro ovladani objektu zobrazovanych na mape :<br>
 *   setLocationCentre() - initialize<br>
 *   nextProvider() - initialize<br>
 *   setProviderAndMode() - initialize<br>
 *   nextMode() - initialize<br>
 *   setZoomLevel() - initialize<br>
 *   pan() - pan<br>
 */
public interface MapLayer {

    /**
     * Speed of panning in pixels.
     */
    public static int PAN_PIXELS = 20;

    /**
     * Draw map to place defined by center.
     * @param g Graphics to draw at.
     * @param mapPanX Move drawed map about fixing space (X), not real movement.
     * @param mapPanY Move drawed map about fixing space (Y), not real movement.
     * @return true if drawed without problem.
     */
    public boolean drawMap(Graphics g, int mapPanX, int mapPanY);

    /**
     * Sets location center to new location.
     * @param loc New location center.
     * @return If setted allright <b>true</b>, otherwise <b>false</b>.
     */
    public boolean setLocationCenter(Location4D loc);

    /**
     * Each mapLayer can contain many providers for current location.
     * This switch the next.
     */
    public void nextProvider();

    /** 
     * How many providers are available for current location
     */
    public int getProviderCount();

    public String getProviderName();

    public Vector getProvidersAndModes();
    /**
     * Each map providre can contain many modes (arieal, map, hybrid, foto, ...).
     * This function returns how many modes has selected provider.
     * @return Number of modes.
     */
    public int getModeSize();

    public void nextMode();

    public String getMapLayerName();

    public int getMaxZoomLevel();

    public int getMinZoomLevel();

    public int getActualZoomLevel();

    public void zoomIn();

    public void zoomOut();

    public void setZoomLevel(int zoom_level);

    public void calculateZoomFrom(Location4D[] positions);

    /* panning over the map */
    public void pan(int dx, int dy);

    public void panRight();

    public void panLeft();

    public void panUp();

    public void panDown();

    public void repaint();

    /** returns coordinates in screen coord. system for given location */
    public Point2D.Int getLocationCoord(Location4D loc);

    public void destroyMap();

    public Location4D[] getActualBoundingBox();
}