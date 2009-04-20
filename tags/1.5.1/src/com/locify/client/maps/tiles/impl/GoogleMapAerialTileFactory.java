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
package com.locify.client.maps.tiles.impl;


import com.locify.client.maps.MapLayer;
import com.locify.client.maps.tiles.AbstractTileFactory;
import com.locify.client.maps.tiles.TileFactoryInfo;
import com.locify.client.utils.math.LMath;

/**
 * @author sarbogast
 * @version 14 juin 2008, 15:51:46
 */
public class GoogleMapAerialTileFactory extends AbstractTileFactory {

    /**
     * Creates a new instance of TileFactory
     */
    public GoogleMapAerialTileFactory(MapLayer canvasToRepaintWhenLoaded) {
        super(new TileFactoryInfo(
                "Google Aerial",
                "Maps",
                8,
                25,
                256,
                true,
                true,
                "http://kh0.google.com/kh?n=404&v=23",
                ""),
                canvasToRepaintWhenLoaded
        );
    }

    protected String getCoordinatePart(int x, int y, int referenceZoom) {
//System.out.println("\n x: " + x + " y: " + y + " zoom: " + getParticularZoomFromReferenceZoom(referenceZoom));
        
        String path = "&t=t";
        int numOfTiles = (int) LMath.pow(2.0, getParticularZoomFromReferenceZoom(referenceZoom));
        int startTileX = 0;
        int startTileY = 0;
        int middleTileX = 0;
        int middleTileY = 0;
        
        for (int i = 1; i <= getParticularZoomFromReferenceZoom(referenceZoom); i++) {
            int numOfTilesZoom = (int) LMath.pow(2, i);
            middleTileX = startTileX + numOfTiles / numOfTilesZoom;
            middleTileY = startTileY + numOfTiles / numOfTilesZoom;
//System.out.println("numOfTilesZoom: " + numOfTilesZoom + " middleT x: " + middleTileX + " y: " + middleTileY);
            if (y < middleTileY) {
                if (x < middleTileX) {
                    path += "q";
                } else {
                    path += "r";
                    startTileX = middleTileX;
                }
            } else {
                if (x < middleTileX) {
                    path += "t";
                } else {
                    path += "s";
                    startTileX = middleTileX;
                }
                startTileY = middleTileY;
            }
//System.out.println("\n" + path);
        }
        
        return path;
    }

    protected int getParticularZoomFromReferenceZoom(int referenceZoom) {
        return -8 + referenceZoom;
    }

    protected int getReferenceZoomFromParticularZoom(int particularZoom) {
        return -8 + particularZoom;
    }
}
