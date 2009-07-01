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
import com.locify.client.maps.geometry.*;
import com.locify.client.maps.projection.Projection;
import com.locify.client.utils.R;
import com.locify.client.utils.math.LMath;

/**
 * Some utility methods for maps
 */
public class GeoUtil {

    // temp variables
    private static double x;
    private static double y;
    private static double e;

    /**
     * Recompute metres from latitude and longitude
     * @param latitude latitude
     * @param longitude longitude from greenwich
     * @return point as X and Y coordinates in metres on sphere
     */
    public static Point2D computeMetersFromDegree(double latitude, double longitude) {
        //double lonRadius = Math.tan(90-latitude) * Constants.WGS84_RADIUS;
        double x = (Projection.WGS84_EARTH_PERIMETER / 360) * (longitude + Projection.SJTSK_FERRO_REDUCTION);
        double y = (Projection.WGS84_EARTH_PERIMETER / 360) * latitude;
        //System.out.print("\n !!! lat: " + latitude + " lon: " + longitude + " x: " + x + " y: " + y);
        return new Point2D.Double(x, y);
    }

    public static Location4D computeDegreeFromMetres(double x, double y) {
        double lon = (x / (Projection.WGS84_EARTH_PERIMETER / 360)) - Projection.SJTSK_FERRO_REDUCTION;
        double lat = y / (Projection.WGS84_EARTH_PERIMETER / 360);
        return new Location4D(lat, lon, 0f);
    }

    /**
     * @return the size of the map at the given zoom, in tiles (num tiles tall
     *         by num tiles wide)
     */
    public static Dimension getMapSize(int referenceZoomLevel, TileFactoryInfo info) {
        return new Dimension(info.getMapWidthInTilesAtZoom(referenceZoomLevel), info.getMapWidthInTilesAtZoom(referenceZoomLevel));
    }

    /**
     * Convert an on screen pixel coordinate and a zoom level to a geo position
     */
    public static Location4D getPosition(Point2D pixelCoordinate, int referenceZoomLevel, TileFactoryInfo info) {
        x = pixelCoordinate.getX();
        y = pixelCoordinate.getY();
        // this reverses getBitmapCoordinates
        double flon = (x - info.getMapCenterInPixelsAtZoom(referenceZoomLevel).getX()) / info.getLongitudeDegreeWidthInPixels(referenceZoomLevel);
        double e1 = (y - info.getMapCenterInPixelsAtZoom(referenceZoomLevel).getY()) / (-1 * info.getLongitudeRadianWidthInPixels(referenceZoomLevel));
        double e2 = (2 * LMath.atan(LMath.exp(e1)) - Math.PI / 2) / (Math.PI / 180.0);
        double flat = e2;
        Location4D wc = new Location4D(flat, flon, 0);
        return wc;
    }

    /**
     * Given a position (latitude/longitude pair) and a zoom level, return
     * the appropriate point in <em>pixels</em>. The zoom level is necessary because
     * pixel coordinates are in terms of the zoom level
     *
     * @param c         A lat/lon pair
     * @param zoomLevel the zoom level to extract the pixel coordinate for
     */
    public static Point2D getBitmapCoordinate(Location4D loc, int zoomLevel, TileFactoryInfo info) {
        return getBitmapCoordinate(loc.getLatitude(), loc.getLongitude(), zoomLevel, info);
    }

    /**
     * Given a position (latitude/longitude pair) and a zoom level, return
     * the appropriate point in <em>pixels</em>. The zoom level is necessary because
     * pixel coordinates are in terms of the zoom level
     *
     * @param latitude
     * @param longitude
     * @param referenceZoomLevel the zoom level to extract the pixel coordinate for
     */
    public static Point2D getBitmapCoordinate(
            double latitude,
            double longitude,
            int referenceZoomLevel,
            TileFactoryInfo info) {
        try {
//System.out.println("GeoUtil.getBitmapCoordinate() lat: " + latitude + " lon: " + longitude + " ref. zoom: " + referenceZoomLevel + " info: " + info.toString());
//System.out.println("GeoUtil x1: " + info.getMapCenterInPixelsAtZoom(referenceZoomLevel));
            x = info.getMapCenterInPixelsAtZoom(referenceZoomLevel).getX() + longitude * info.getLongitudeDegreeWidthInPixels(referenceZoomLevel);
//System.out.println("GeoUtil x: " + x);
            e = Math.sin(latitude * (Math.PI / 180.0));
            if (e > 0.9999) {
                e = 0.9999;
            }
            if (e < -0.9999) {
                e = -0.9999;
            }
//System.out.println("GeoUtil x: " + x);
            y = info.getMapCenterInPixelsAtZoom(referenceZoomLevel).getY() + 0.5 *
                    LMath.log((1 + e) / (1 - e)) * -1 *
                    (info.getLongitudeRadianWidthInPixels(referenceZoomLevel));
//System.out.println("GeoUtil y: " + y);
            //y = info.getMapCenterInPixelsAtZoom(referenceZoomLevel).getY() + 0.5 * 1000 * -1 * (info.getLongitudeRadianWidthInPixels(referenceZoomLevel));
            //System.out.print("\nGeoUtil.getBitmapCoordinate : lat: " + latitude + " lon: " + longitude + " x: " + x + " y: " + y);
            return new Point2D.Double(x, y);
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "GeoUtil.getBitmapCoordinate()",
                    "lat: " + latitude + " lon: " + longitude + " ref. zoom: " + referenceZoomLevel + " info: " + info.toString());
            return new Point2D.Double(0.0, 0.0);
        }
    }

    /**
     * @return true if this point in <em>tiles</em> is valid at this zoom level. For example,
     * if the zoom level is 0 (zoomed all the way out, where there is only
     * one tile), then x,y must be 0,0
     */
    public static boolean isValidTile(int x, int y, int referenceZoomLevel, TileFactoryInfo info) {
        //int x = (int)coord.getX();
        //int y = (int)coord.getY();
        // if off the map to the top or left
        if (x < 0 || y < 0) {
            return false;
        }
        // if of the map to the right
        if (info.getMapCenterInPixelsAtZoom(referenceZoomLevel).getX() * 2 <= x * info.getTileSize(referenceZoomLevel)) {
            return false;
        }
        // if off the map to the bottom
        if (info.getMapCenterInPixelsAtZoom(referenceZoomLevel).getY() * 2 <= y * info.getTileSize(referenceZoomLevel)) {
            return false;
        }
        //if out of zoom bounds
        //noinspection RedundantIfStatement
        if (referenceZoomLevel < info.getMinimumZoomLevel() || referenceZoomLevel > info.getMaximumZoomLevel()) {
            return false;
        }
        return true;
    }
}
