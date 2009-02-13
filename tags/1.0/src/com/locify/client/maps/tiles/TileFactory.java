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
package com.locify.client.maps.tiles;

import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Dimension;
import com.locify.client.maps.geometry.Point2D;

/**
 * A class that can produce tiles and convert coordinates to pixels
 *
 * @author sarbogast
 */
public abstract class TileFactory {

    private TileFactoryInfo info;
  

    /**
     * Creates a new instance of TileFactory
     *
     * @param info a TileFactoryInfo to configure this TileFactory
     */
    protected TileFactory(TileFactoryInfo info) {
        this.info = info;
    }

    /**
     * Gets the size of an edge of a tile in pixels at the current zoom level. Tiles must be square.
     *
     * @param referenceZoom the current zoom level
     * @return the size of an edge of a tile in pixels
     */
    public int getTileSize(int referenceZoom) {
        return getInfo().getTileSize(referenceZoom);
    }

    /**
     * Returns a Dimension containing the width and height of the map,
     * in tiles at the
     * current zoom level.
     * So a Dimension that returns 10x20 would be 10 tiles wide and 20 tiles
     * tall. These values can be multipled by getTileSize() to determine the
     * pixel width/height for the map at the given zoom level
     *
     * @param referenceZoom the current zoom level
     * @return the size of the world bitmap in tiles
     */
    public Dimension getMapSize(int referenceZoom) {
        return GeoUtil.getMapSize(referenceZoom, getInfo());
    }

    /**
     * Return the Tile at a given TilePoint and zoom level
     *
     * @param y             ordinate of the tilePoint
     * @param referenceZoom the current zoom level
     * @return the tile that is located at the given tilePoint for this zoom level. For
     *         example, if getMapSize() returns 10x20 for this zoom, and the
     *         tilePoint is (3,5), then the appropriate tile will be located
     *         and returned. This method must not return null. However, it
     *         can return dummy tiles that contain no data if it wants. This is appropriate,
     *         for example, for tiles which are outside of the bounds of the map and if the
     *         factory doesn't implement wrapping. @param x abscissa of the tilePoint
     */
    public abstract String getTile(int x, int y, int referenceZoom);

    /**
     * Convert a pixel in the world bitmap at the specified
     * zoom level into a GeoPosition
     *
     * @param pixelCoordinate a Point2D representing a pixel in the world bitmap
     * @param referenceZoom   the zoom level of the world bitmap
     * @return the converted GeoPosition
     */
    public Location4D pixelToGeo(Point2D pixelCoordinate, int referenceZoom) {
        return GeoUtil.getPosition(pixelCoordinate, referenceZoom, getInfo());
    }

    /**
     * Convert a GeoPosition to a pixel position in the world bitmap
     * a the specified zoom level.
     *
     * @param c             a GeoPosition
     * @param referenceZoom the zoom level to extract the pixel coordinate for
     * @return the pixel point
     */
    public Point2D geoToPixel(Location4D c, int referenceZoom) {
        return GeoUtil.getBitmapCoordinate(c, referenceZoom, getInfo());
    }

    /**
     * Get the TileFactoryInfo describing this TileFactory
     *
     * @return a TileFactoryInfo
     */
    public TileFactoryInfo getInfo() {
        return info;
    }
    
    protected abstract String getCoordinatePart(int x, int y, int referenceZoom);

    protected abstract int getParticularZoomFromReferenceZoom(int referenceZoom);

    protected abstract int getReferenceZoomFromParticularZoom(int particularZoom);

}
