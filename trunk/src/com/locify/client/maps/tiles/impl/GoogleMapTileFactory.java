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

/**
 * @author sarbogast
 * @version 14 juin 2008, 15:51:46
 */
public class GoogleMapTileFactory extends AbstractTileFactory {

    /**
     * Creates a new instance of TileFactory
     */
    public GoogleMapTileFactory(MapLayer canvasToRepaintWhenLoaded) {
        super(new TileFactoryInfo(
                "Google",
                "Maps",
                8,
                25,
                256,
                true,
                true,
                "http://mt0.google.com/mt/v=w2.95&hl=en",
                ""),
                canvasToRepaintWhenLoaded
        );
    }

    protected String getCoordinatePart(int x, int y, int referenceZoom) {
        return "&x=" + x +
                "&y=" + y +
                "&z=" + getParticularZoomFromReferenceZoom(referenceZoom);
    }

    protected int getParticularZoomFromReferenceZoom(int referenceZoom) {
        return referenceZoom - 8;
    }

    protected int getReferenceZoomFromParticularZoom(int particularZoom) {
        return particularZoom - 8;
    }
}
