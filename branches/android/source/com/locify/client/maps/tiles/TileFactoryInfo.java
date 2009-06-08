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

import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.math.LMath;

/**
 * A TileFactoryInfo encapsulates all information
 * specific to a map server. This includes everything from
 * the url to load the map tiles from to the size and depth
 * of the tiles. Theoretically any map server can be used
 * by installing a customized TileFactoryInfo.
 */
public class TileFactoryInfo {
    /**
     * Minimum zoom level relative to the reference zoom scale
     */
    private int minimumZoomLevel;
    /**
     * Maximum zoom level relative to the reference zoom scale
     */
    private int maximumZoomLevel;

    /**
     * the size of each tile (assumes they are square)
     */
    private int tileSize = 256;

    /**
     * The number of tiles wide at each zoom level
     */
    private int[] mapWidthInTilesAtZoom;

    /**
     * An array of coordinates in <em>pixels</em> that indicates the center in the
     * world map for the given zoom level.
     */
    private Point2D[] mapCenterInPixelsAtZoom;

    /**
     * An array of doubles that contain the number of pixels per degree of
     * longitude at a give zoom level.
     */
    private double[] longitudeDegreeWidthInPixels;

    /**
     * An array of doubles that contain the number of radians per degree of
     * longitude at a given zoom level (where longitudeRadianWidthInPixels[0] is
     * the most zoomed out)
     */
    private double[] longitudeRadianWidthInPixels;

    /**
     * The base url for loading tiles from.
     */
    private String baseUrl;
    private String urlSuffix;
    private boolean zeroTileAbscissaLeft = true;
    private boolean zeroTileOrdinateTop = true;

    private int defaultZoomLevel;

    /**
     * The name of the provider of the tiles
     */
    private String provider;

    /**
     * The type of tile (map, satellite, etc.)
     */
    private String mode;

    /**
     * Creates a new instance of TileFactoryInfo. Note that TileFactoryInfo
     * should be considered invariate, meaning that subclasses should
     * ensure all of the properties stay the same after the class is
     * constructed. Returning different values of getTileSize() for example
     * is considered an error and may result in unexpected behavior.
     *
     * @param minimumZoomLevel     The minimum zoom level
     * @param maximumZoomLevel     the maximum zoom level
     * @param tileSize             the size of the tiles in pixels (must be square)
     * @param zeroTileAbscissaLeft true if tile x is measured from the far left of the map to the far right, or
     *                             else false if based on the center line.
     * @param zeroTileOrdinateTop  true if tile y is measured from the top (north pole) to the bottom (south pole)
     *                             or else false if based on the equator.
     * @param baseUrl              the base url for grabbing tiles
     */
    public TileFactoryInfo(String provider,
                           String mode,
                           int minimumZoomLevel,
                           int maximumZoomLevel,
                           int tileSize,
                           boolean zeroTileAbscissaLeft,
                           boolean zeroTileOrdinateTop,
                           String baseUrl,
                           String urlSuffix) {
        this.provider = provider;
        this.mode = mode;
        this.minimumZoomLevel = minimumZoomLevel;
        this.maximumZoomLevel = maximumZoomLevel;
        this.baseUrl = baseUrl;
        this.urlSuffix = urlSuffix;

        this.setZeroTileAbscissaLeft(zeroTileAbscissaLeft);
        this.setZeroTileOrdinateTop(zeroTileOrdinateTop);

        this.tileSize = tileSize;

        longitudeDegreeWidthInPixels = new double[32];
        longitudeRadianWidthInPixels = new double[32];
        mapCenterInPixelsAtZoom = new Point2D.Double[32];
        mapWidthInTilesAtZoom = new int[32];

        // for each zoom level
        for (int z = minimumZoomLevel; z <= maximumZoomLevel; z++) {
            double mapSide = LMath.pow(2, z); //JST (pow(2,z))
            // how wide is ea degree of longitude chin pixels
            longitudeDegreeWidthInPixels[z] = mapSide / 360;
            // how wide is each radian of longitude in pixels
            longitudeRadianWidthInPixels[z] = mapSide / (2.0 * Math.PI);
            mapCenterInPixelsAtZoom[z] = new Point2D.Double(mapSide / 2, mapSide / 2);
            mapWidthInTilesAtZoom[z] = (int) (mapSide / tileSize);
        }
    }

    /**
     * @return
     */
    public int getMinimumZoomLevel() {
        return minimumZoomLevel;
    }

    /**
     * @return
     */
    public int getMaximumZoomLevel() {
        return maximumZoomLevel;
    }

    /**
     * @param zoom
     * @return
     */
    public int getMapWidthInTilesAtZoom(int zoom) {
        return mapWidthInTilesAtZoom[zoom];
    }

    /**
     * @param zoom
     * @return
     */
    public Point2D getMapCenterInPixelsAtZoom(int zoom) {
        return mapCenterInPixelsAtZoom[zoom];
    }

    /**
     * Get the tile size.
     *
     * @return the tile size
     */
    public int getTileSize(int zoom) {
        return tileSize;
    }

    /**
     * @param zoom
     * @return
     */
    public double getLongitudeDegreeWidthInPixels(int zoom) {
        return longitudeDegreeWidthInPixels[zoom];
    }

    /**
     * @param zoom
     * @return
     */
    public double getLongitudeRadianWidthInPixels(int zoom) {
        return longitudeRadianWidthInPixels[zoom];
    }

    /**
     * A property indicating if the X coordinates of tiles go
     * from right to left or left to right.
     *
     * @return
     */
    public boolean isZeroTileAbscissaLeft() {
        return zeroTileAbscissaLeft;
    }

    /**
     * A property indicating if the X coordinates of tiles go
     * from right to left or left to right.
     *
     * @param zeroTileAbscissaLeft
     */
    public void setZeroTileAbscissaLeft(boolean zeroTileAbscissaLeft) {
        this.zeroTileAbscissaLeft = zeroTileAbscissaLeft;
    }

    /**
     * A property indicating if the Y coordinates of tiles go
     * from right to left or left to right.
     *
     * @return
     */
    public boolean isZeroTileOrdinateTop() {
        return zeroTileOrdinateTop;
    }

    /**
     * A property indicating if the Y coordinates of tiles go
     * from right to left or left to right.
     *
     * @param zeroTileOrdinateTop
     */
    public void setZeroTileOrdinateTop(boolean zeroTileOrdinateTop) {
        this.zeroTileOrdinateTop = zeroTileOrdinateTop;
    }

    public int getDefaultZoomLevel() {
        return defaultZoomLevel;
    }

    public void setDefaultZoomLevel(int defaultZoomLevel) {
        this.defaultZoomLevel = defaultZoomLevel;
    }

    /**
     * @return The name of the provider for tiles
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @return The type of tiles (map, satellite, etc.)
     */
    public String getMode() {
        return mode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUrlSuffix() {
        return urlSuffix;
    }

    public void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
    }

    public int getNumberOfZoomLevels() {
        return getMaximumZoomLevel() - getMinimumZoomLevel() + 1;
    }
}
