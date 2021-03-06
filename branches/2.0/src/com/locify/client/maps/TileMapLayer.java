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
import com.locify.client.maps.geometry.Dimension;
import com.locify.client.maps.geometry.Point;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.maps.geometry.Rectangle2D;
import com.locify.client.maps.tiles.TileFactory;
import com.locify.client.maps.tiles.TileFactoryInfo;
import com.locify.client.maps.tiles.impl.GoogleMapAerialTileFactory;
import com.locify.client.maps.tiles.impl.GoogleMapTileFactory;
import com.locify.client.maps.tiles.impl.OpenStreetMapMapnikTileFactory;
import com.locify.client.maps.tiles.impl.VirtualEarthAerialTileFactory;
import com.locify.client.maps.tiles.impl.VirtualEarthHybridTileFactory;
import com.locify.client.maps.tiles.impl.VirtualEarthRoadTileFactory;
import com.locify.client.maps.tiles.impl.YahooMapTileFactory;
import com.locify.client.maps.tiles.impl.YahooSatelliteTileFactory;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import com.sun.lwuit.Display;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import java.util.Vector;

/**
 * Implementation of MapLayer used for online tiled maps.
 * Supports any map impmenting TileFactory
 *   - Google maps
 *   - yahoo maps
 *   - openstreet map
 *   - ms virtual earth
 * 
 * Class doesn't handle user actions, just draws map according to set zoom level, graphics size and locationCenter. 
 * It is responsible for downloading and caching tiles
 * 
 */
public class TileMapLayer implements MapLayer {

    // DRAW MAP TEMP VARIABLES
    private RectangleViewPort viewportBounds;
    private int tileSize;
    private Dimension mapSize;
    private int topLeftTileX;
    private int topLeftTileY;
    private int bottomRightTileX;
    private int bottomRightTileY;
    private int centerTileX;
    private int centerTileY;
    private int numWide;
    private int numHigh;
    private Vector requiredTiles;
    private int tileX;
    private int tileY;
    private int nextPos;
    private ImageRequest ir;
    private RectangleViewPort clipBounds;
    private RectangleViewPort tileBounds;
    private Image tileImage;

    /** actual viewport position X */
    private int viewportX = 0;
    /** actuual viewport position Y */
    private int viewportY = 0;
    /** actual viewport width */
    private int viewportWidth = Display.getInstance().getDisplayWidth();
    /** actual viewport height */
    private int viewportHeight = Display.getInstance().getDisplayHeight();
    
    /**
     * An array of all the available providers.
     * The first dimension corresponds to providers: Microsoft, Google, Yahoo, etc.
     * The second dimension corresponds to modes: Aerial, Roads, etc.
     * Not that all providers don't offer the same number of modes.
     */
    private TileFactory[][] tileProviders;
    /** Current active provider */
    private int provider;

    /** switch provider */
    public void nextProvider() {
        provider = (provider + 1) % tileProviders.length;
        mode = 0;
        setTileFactory(tileProviders[provider][mode]);
    }
    /** Current active mode (arieal, map, hybrid etc.) */
    private int mode;

    public final void nextMode() {
        mode = (mode + 1) % tileProviders[provider].length;
        setTileFactory(tileProviders[provider][mode]);
    }

    /**
     * Returns all names of providers and their modes
     * @return providers and modes
     */
    public Vector getProvidersAndModes() {
        Vector providersAndModes = new Vector();
        for (int i = 0; i < tileProviders.length; i++) {
            for (int j = 0; j < tileProviders[i].length; j++) {
                TileFactoryInfo info = (tileProviders[i][j]).getInfo();
                providersAndModes.addElement(info.getProvider() + " " + info.getMode());
            }
        }
        return providersAndModes;
    }

    /**
     * Sets provider from the list generated from getProviderAndModes()
     * @param number
     */
    public boolean setProviderAndMode(int number) {
        int k = 0;
        for (int i = 0; i < tileProviders.length; i++) {
            for (int j = 0; j < tileProviders[i].length; j++) {
                if (k == number) {
                    setTileFactory(tileProviders[i][j]);
                }
                k++;
            }
        }
        return true;
    }

    /**
     * The position in latitude/longitude of the "address" being mapped. This
     * is a special coordinate that, when moved, will cause the map to be moved
     * as well. It is separate from "center" in that "center" tracks the current
     * center (in pixels) of the viewport whereas this will not change when panning
     * or zooming. Whenever the addressLocation is changed, however, the map will
     * be repositioned.
     */
    private Location4D centerLocation;

    /**
     * Gets the current address location of the map
     *
     * @param addressLocation the new address location
     */
    public final void setAddressLocation(Location4D loc) {
        this.centerLocation = loc;
        setCenter(getTileFactory().geoToPixel(centerLocation, getActualZoomLevel()));
    }

    /**
     * The position, in map coord system of the center point. This is defined
     * as the distance from the top and left edges of the map
     * in pixels. 
     */
    private Point2D center;
    private boolean centerChanged;
    private boolean screenChanged;

    public final Point2D getCenter() {
        return center;
    }

    /**
     * Sets the new center of the map in pixel coordinates.
     * @param center 
     */
    public final void setCenter(Point2D center) {
//        if (isRestrictOutsidePanning()) {
//            int viewportHeight = getHeight();
//            int viewportWidth = getWidth();
//
//            // don't let the user pan over the top edge
//            RectangleViewPort newVP = calculateViewportBounds(center);
//            if (newVP.getY() < 0) {
//                double centerY = viewportHeight / 2;
//                center = new Point2D.Double(center.getX(), centerY);
//            }
//
//            // don't let the user pan over the left edge
//            if (!isHorizontalWrapped() && newVP.getX() < 0) {
//                double centerX = viewportWidth / 2;
//                center = new Point2D.Double(centerX, center.getY());
//            }
//
//            // don't let the user pan over the bottom edge
//            Dimension mapSize = getTileFactory().getMapSize(getActualZoomLevel());
//            int mapHeight = (int) mapSize.getHeight() * getTileFactory().getTileSize(getActualZoomLevel());
//            if (newVP.getY() + newVP.getHeight() > mapHeight) {
//                double centerY = mapHeight - viewportHeight / 2;
//                center = new Point2D.Double(center.getX(), centerY);
//            }
//
//            // don't let the user pan over the right edge
//            int mapWidth = (int) mapSize.getWidth() * getTileFactory().getTileSize(getActualZoomLevel());
//            if (!isHorizontalWrapped() && (newVP.getX() + newVP.getWidth() > mapWidth)) {
//                double centerX = mapWidth - viewportWidth / 2;
//                center = new Point2D.Double(centerX, center.getY());
//            }
//
//            // if map is to small then just center it vert
//            if (mapHeight < newVP.getHeight()) {
//                double centerY = mapHeight / 2;
//                center = new Point2D.Double(center.getX(), centerY);
//            }
//
//            // if map is too small then just center it horiz
//            if (!isHorizontalWrapped() && mapWidth < newVP.getWidth()) {
//                double centerX = mapWidth / 2;
//                center = new Point2D.Double(centerX, center.getY());
//            }
//        }
        this.center = center;
        this.centerChanged = true;
    }

    /** zoom level */
    private int zoom = 19 ;

    public final int getActualZoomLevel() {
        return this.zoom;
    }
    /** active tilefactory - provider */
    private TileFactory tileFactory;

    public final TileFactory getTileFactory() {
        return tileFactory;
    }

    public final void setTileFactory(TileFactory factory) {
        this.tileFactory = factory;
        setZoomLevel(getActualZoomLevel());
    }
    /** 
     * Controls panning mode in case we are in the border
     */
    private boolean restrictOutsidePanning = true;

    public boolean isRestrictOutsidePanning() {
        return restrictOutsidePanning;
    }
    
    private boolean horizontalWrapped = true;

    public boolean isHorizontalWrapped() {
        return horizontalWrapped;
    }

    public TileMapLayer() {
        tileProviders = new TileFactory[][]{
                    new TileFactory[]{
                        new GoogleMapTileFactory(this),
                        new GoogleMapAerialTileFactory(this)
                    },
                    new TileFactory[]{
                        new YahooMapTileFactory(this),
                        new YahooSatelliteTileFactory(this)
                    },
                    new TileFactory[]{
                        new OpenStreetMapMapnikTileFactory(this)
                    },
                    new TileFactory[]{
                        new VirtualEarthRoadTileFactory(this),
                        new VirtualEarthAerialTileFactory(this),
                        new VirtualEarthHybridTileFactory(this)
                    },
                };
    }

    public boolean drawMap(Graphics g, int mapPanX, int mapPanY) {
        try {
            if (viewportX != g.getClipX() || viewportY != g.getClipY() ||
                    viewportWidth != g.getClipWidth() || viewportHeight != g.getClipHeight()) {
                viewportX = g.getClipX();
                viewportY = g.getClipY();
                viewportWidth = g.getClipWidth();
                viewportHeight = g.getClipHeight();
                screenChanged = true;
            }
//System.out.println(g.getClipX() + " " + g.getClipY() + " " + g.getClipWidth() + " " + g.getClipHeight() + " " + screenChanged);

            refreshViewport();

            tileSize = getTileFactory().getTileSize(zoom);
            mapSize = getTileFactory().getMapSize(zoom);

            //calculate coordinates of the top left tile to display
            topLeftTileX = (int) Math.floor(viewportBounds.getX() / tileSize);
            topLeftTileY = (int) Math.floor(viewportBounds.getY() / tileSize);
            bottomRightTileX = (int) Math.floor((viewportBounds.getX() + viewportBounds.getWidth()) / tileSize);
            bottomRightTileY = (int) Math.floor((viewportBounds.getY() + viewportBounds.getHeight()) / tileSize);
            centerTileX = (int) Math.floor((viewportBounds.getX() + viewportBounds.getWidth()/2) / tileSize);
            centerTileY = (int) Math.floor((viewportBounds.getY() + viewportBounds.getHeight()/2) / tileSize);

            numWide = bottomRightTileX - topLeftTileX + 1;
            numHigh = bottomRightTileY - topLeftTileY + 1;

            requiredTiles = new Vector();
            requiredTiles.addElement(new ImageRequest(centerTileX, centerTileY,
                    getTileFactory().getTile(centerTileX, centerTileY, zoom), 256, 256));

            nextPos = 1;
            for (int x = 0; x < numWide; x++) {
                for (int y = 0; y < numHigh; y++) {
                    tileX = x + topLeftTileX;
                    tileY = y + topLeftTileY;
                    if (tileX != centerTileX || tileY != centerTileY) {
                        requiredTiles.addElement(new ImageRequest(tileX, tileY,
                                getTileFactory().getTile(tileX, tileY, zoom), 256, 256));
                        nextPos++;
                    }
                }
            }

            // IMPORTANT calling to tileCache !!!!
            R.getMapTileCache().newRequest(requiredTiles);

            //fetch the tiles from the tileFactory and store them in the tiles cache
            for (int i = 0; i < requiredTiles.size(); i++) {
                ir = (ImageRequest) requiredTiles.elementAt(i);
                //only proceed if the specified tile point lies within the area being painted
                clipBounds = new RectangleViewPort(0, 0, viewportWidth, viewportHeight);
                tileBounds = new RectangleViewPort(ir.x * tileSize - viewportBounds.x,
                        ir.y * tileSize - viewportBounds.y, tileSize, tileSize);
                if (clipBounds.intersects(tileBounds)) {
                    //start downloading
                    tileImage = R.getMapTileCache().getImage(ir.fileName);
                    //if the tile is off the map to the north/south, then just don't paint anything
                    if (isTileOffMap(ir.x, ir.y, mapSize)) {
                        g.setColor(ColorsFonts.GRAY);
                        g.fillRect(tileBounds.x, tileBounds.y, tileBounds.width, tileBounds.height);
                    } else {
                        ir.x = tileBounds.x + mapPanX + viewportX;
                        ir.y = tileBounds.y + mapPanY + viewportY;
                        ir.image = tileImage;
                        MapImages.drawTile(g, ir);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TileMapLayer.drawMap()", null);
            return false;
        }
    }

    public void setCenterPosition(Location4D geoPosition) {
        try {
            setCenter(getTileFactory().geoToPixel(geoPosition, zoom));
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TileMapLayer.setCenterPosition()", null);
        }
    }

    public Location4D getCenterPosition() {
        return getTileFactory().pixelToGeo(getCenter(), zoom);
    }

    /**
     * Returns the bounds of the viewport in pixels. This can be used to transform
     * points into the world bitmap coordinate space.
     *
     * @return the bounds in <em>pixels</em> of the "view" of this map
     */
    private void refreshViewport() {
        if (viewportBounds == null || centerChanged || screenChanged) {
            centerChanged = false;
            screenChanged = false;

            double vpX = (center.getX() - viewportWidth / 2);
            double vpY = (center.getY() - viewportHeight / 2);
            viewportBounds = new RectangleViewPort((int) vpX, (int) vpY, viewportWidth, viewportHeight);
//    System.out.println("Viewport: w" + viewportX + " " + viewportY + " " + viewportWidth + " h" + viewportHeight);
        }
    }

    private boolean isTileOffMap(int x, int y, Dimension mapSize) {
        return y < 0 || y >= mapSize.getHeight();
    }

    public void zoomIn() {
        setZoomLevel(getActualZoomLevel() + 1);
    }

    public void zoomOut() {
        setZoomLevel(getActualZoomLevel() - 1);
    }

    /**
     * Calculates a zoom level so that all points in the specified set will be
     * visible on screen. This is useful if you have a bunch of points in an
     * area like a city and you want to zoom out so that the entire city and it's
     * points are visible without panning.
     *
     * @param positions A set of GeoPositions to calculate the new zoom from
     */
    public void calculateZoomFrom(Location4D[] positions) {
        if (positions.length < 2) {
            return;
        }

        int actualZoom = getActualZoomLevel();
        Rectangle2D rect = generateBoundingRect(positions, actualZoom);
//System.out.println("Rect: " + rect);
        if (rect.getWidth() == 0 || rect.getHeight() == 0)
            return;

        int count = 0;

        refreshViewport();

        if (viewportBounds.contains(rect)) {
            while (viewportBounds.contains(rect)) {
                Point2D cent = new Point2D.Double(
                        rect.getX() + rect.getWidth() / 2,
                        rect.getY() + rect.getHeight() / 2);
                Location4D px = getTileFactory().pixelToGeo(cent, actualZoom);
                setCenterPosition(px);
                count++;
                if (count > 30) {
                    break;
                }
                actualZoom = actualZoom + 1;
                if (actualZoom <= getMaxZoomLevel()) {
                    setZoomLevel(actualZoom);
                    rect = generateBoundingRect(positions, actualZoom);
                } else {
                    break;
                }
            }
            
            actualZoom = actualZoom - 1;
            setZoomLevel(actualZoom);
        } else {
            while (!viewportBounds.contains(rect)) {
                Point2D cent = new Point2D.Double(
                        rect.getX() + rect.getWidth() / 2,
                        rect.getY() + rect.getHeight() / 2);
//System.out.println("View: " + viewportBounds);
//System.out.println("Rect: " + rect + ", cent: " + cent + ", actZoom: " + actualZoom);
                Location4D px = getTileFactory().pixelToGeo(cent, actualZoom);
//System.out.println("New loc: " + px);
                setCenterPosition(px);
                count++;
                if (count > 30) {
                    break;
                }
                if (viewportBounds.contains(rect)) {
//System.out.println("Break");
                    break;
                }
                actualZoom = actualZoom - 1;
                if (actualZoom >= getMinZoomLevel()) {
                    setZoomLevel(actualZoom);
                    rect = generateBoundingRect(positions, actualZoom);
                } else {
//System.out.println("Break2");
                    break;
                }
            }
        }
    }

    /**
     * Move the map by dx pixels horizontally and dy pixels vertically.
     *
     * @param dx Number of clicks to move horizontally
     * @param dy Number of clicks to move vertically
     */
    public void pan(int dx, int dy) {
        refreshViewport();
        if (dx != 0 || dy != 0) {
            double x = viewportBounds.getCenterX() + dx;
            double y = viewportBounds.getCenterY() + dy;
            setCenter(new Point2D.Double(x, y));
        }

    //parent.objectsPan(dx, dy);
    }

    private Rectangle2D generateBoundingRect(final Location4D[] positions, int zoom) {
        Point2D point1 = getTileFactory().geoToPixel(positions[0], zoom);
        Rectangle2D rect = new Rectangle2D.Double(point1.getX(), point1.getY(), 0, 0);

        for (int i = 0; i < positions.length; i++) {
            Point2D point = getTileFactory().geoToPixel(positions[i], zoom);
            rect.add(point);
        }
        return rect;
    }

    public boolean setLocationCenter(Location4D loc) {
        this.setAddressLocation(loc);
        return true;
    }

    public void destroyMap() {
    }

    public String getMapName() {
        return "";
    }

    public boolean isMapReady() {
        return true;
    }

    public int getMaxZoomLevel() {
        return tileFactory.getInfo().getMaximumZoomLevel();
    }

    public int getMinZoomLevel() {
        return tileFactory.getInfo().getMinimumZoomLevel();
    }

    public void setZoomLevel(int zoom_level) {
        TileFactoryInfo info = getTileFactory().getInfo();
        if (info != null) {
            if (zoom_level < info.getMinimumZoomLevel()) {
                zoom_level = info.getMinimumZoomLevel();
            } else if (zoom_level > info.getMaximumZoomLevel()) {
                zoom_level = info.getMaximumZoomLevel();
            }
        }

        int oldzoom = this.zoom;
        if (getCenter() == null)
            setAddressLocation(R.getLocator().getLastLocation());
        Point2D oldCenter = getCenter();

        Dimension oldMapSize = getTileFactory().getMapSize(oldzoom);

        this.zoom = zoom_level;

        Dimension size = getTileFactory().getMapSize(zoom_level);

        setCenter(new Point2D.Double(
                oldCenter.getX() * (size.getWidth() / oldMapSize.getWidth()),
                oldCenter.getY() * (size.getHeight() / oldMapSize.getHeight())));

        refreshViewport();
    }

    public void setDefaultZoomLevel() {
        setZoomLevel(19);
    }

    public int getProviderCount() {
        return this.tileProviders.length;
    }

    public String getProviderName() {
        return this.tileFactory.getInfo().getProvider();
    }

    public int getModeSize() {
        return this.tileProviders[provider].length;
    }

    public String getMapLayerName() {
        return "TileMapLayer";
    }

    public void panRight() {
        this.pan(PAN_PIXELS, 0);
    }

    public void panLeft() {
        this.pan(-PAN_PIXELS, 0);
    }

    public void panUp() {
        this.pan(0, -PAN_PIXELS);
    }

    public void panDown() {
        this.pan(0, PAN_PIXELS);
    }

    public Point2D.Int getLocationCoord(Location4D loc) {
        refreshViewport();
        Point2D point = tileFactory.geoToPixel(loc, zoom);
        //System.out.println("\nvp: " + vp.toString());
        return new Point.Int((int) (point.getX() - viewportBounds.getX()),
                (int) (point.getY() - viewportBounds.getY()));
    }


    public Location4D[] getActualBoundingBox() {
        Location4D[] points = new Location4D[2];
        points[0] = tileFactory.pixelToGeo(
                new Point2D.Int((int) viewportBounds.getX(), (int) viewportBounds.getY()),
                getActualZoomLevel());
        points[1] = tileFactory.pixelToGeo(
                new Point2D.Int((int) (viewportBounds.getX() + viewportBounds.getWidth()),
                (int) (viewportBounds.getY() + viewportBounds.getHeight())),
                getActualZoomLevel());
        return points;
    }

    public Location4D getLocationCoord(int x, int y) {
        refreshViewport();
//System.out.println(x + " " + y + " " + tileFactory.pixelToGeo(new Point2D.Int((int) (viewportBounds.getX() + x),
//                (int) viewportBounds.getY() + y), getActualZoomLevel()));
        return tileFactory.pixelToGeo(new Point2D.Int((int) (viewportBounds.getX() + x),
                (int) viewportBounds.getY() + y), getActualZoomLevel());
    }
}
