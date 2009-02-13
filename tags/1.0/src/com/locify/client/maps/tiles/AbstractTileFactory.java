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

import com.locify.client.maps.MapLayer;

public abstract class AbstractTileFactory extends TileFactory {

    /**
     * Creates a new instance of TileFactory
     *
     * @param info a TileFactoryInfo to configure this TileFactory
     */
    protected AbstractTileFactory(TileFactoryInfo info, MapLayer canvasToRepaintWhenLoaded) {
        super(info);
    }

    /**
     * @param x    Abscissa of the tile to retrieve (between 0 and 2^zoom/tilesize - 1)
     * @param y    Ordinate of the tile to retrieve (between 0 and 2^zoom/tilesize - 1)
     * @param zoom Zoom of the tile to load
     * @return The tile corresponding to those coordinates
     */
    public String getTile(int x, int y, int zoom) {
//System.out.println("\nAbstractTileFactory.getTile() : " + x + "   " + y + "   " + zoom);
        int numTilesWide = (int) getMapSize(zoom).getWidth();
        if (x < 0) {
            x = numTilesWide - (Math.abs(x) % numTilesWide);
        }

        x = x % numTilesWide;
        String url = getTileUrl(x, y, zoom);

        return url;
//        Tile tile;
//        if (!buffer.containsKey(url)) {
//            if (!GeoUtil.isValidTile(tileX, tileY, zoom, getInfo())) {
//                tile = new Tile(tileX, tileY, zoom, null, this);
//            } else {
//                tile = new Tile(tileX, tileY, zoom, url, this);
//                startLoading(tile);
//            }
//            buffer.put(url, tile);
//        } else {
//            tile = buffer.get(url);
//        }
//
//        return tile;
        //return null;
    }

    /**
     * Returns the tile url for the specified tile at the specified
     * zoom level. By default it will generate a tile url using the
     * base url and parameters specified in the constructor.
     *
     * @param zoom the zoom level
     * @param x    abscissa of the tile point
     * @param y    ordinate of the tile point
     * @return a valid url to load the tile
     */
    public String getTileUrl(int x, int y, int zoom) {
        String url = new StringBuffer().append(getInfo().getBaseUrl()).append(getCoordinatePart(x, y, zoom)).append(getInfo().getUrlSuffix()).toString();
        return url;
    }
}
