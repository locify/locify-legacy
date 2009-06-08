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
 * @version 15 juin 2008, 18:21:29
 */
public class AbstractVirtualEarthTileFactory extends AbstractTileFactory {
    private static final int PROTOCOL_VERSION = 117;

    public AbstractVirtualEarthTileFactory(MapLayer canvasToRepaintWhenLoaded, String provider, String mode, String baseUrl) {
        super(new TileFactoryInfo(
                provider,
                mode,
                9,
                25,
                256,
                true,
                true,
                baseUrl,
                "?g=" + PROTOCOL_VERSION),
                canvasToRepaintWhenLoaded
        );
    }

    protected String getCoordinatePart(int x, int y, int zoom) {
        StringBuffer id = new StringBuffer();
        for (int z = zoom; z >= getInfo().getMinimumZoomLevel(); z--) {
            if (x % 2 != 0) {
                if (y % 2 != 0) {
                    id.insert(0, '3');
                } else {
                    id.insert(0, '1');
                }
            } else {
                if (y % 2 != 0) {
                    id.insert(0, '2');
                } else {
                    id.insert(0, '0');
                }
            }
            x /= 2;
            y /= 2;
        }
        return id.toString();
    }

    protected int getParticularZoomFromReferenceZoom(int referenceZoom) {
        return 25 - referenceZoom;
    }

    protected int getReferenceZoomFromParticularZoom(int particularZoom) {
        return 25 - particularZoom;
    }
}
