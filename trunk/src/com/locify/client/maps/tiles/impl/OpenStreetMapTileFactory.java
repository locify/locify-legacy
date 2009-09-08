package com.locify.client.maps.tiles.impl;

import com.locify.client.maps.MapLayer;
import com.locify.client.maps.tiles.AbstractTileFactory;
import com.locify.client.maps.tiles.TileFactoryInfo;

/**
 * @author sarbogast
 * @version 15 juin 2008, 20:34:33
 */
public class OpenStreetMapTileFactory extends AbstractTileFactory {

    /**
     * Creates a new instance of TileFactory
     */
    public OpenStreetMapTileFactory(MapLayer canvasToRepaintWhenLoaded) {
        super(new TileFactoryInfo(
                "OSM",
                "",
                8,
                25,
                256,
                true,
                true,
                "http://tile.openstreetmap.org",
                ".png"), canvasToRepaintWhenLoaded);
    }

    protected String getCoordinatePart(int x, int y, int zoom) {
        return "/" + getParticularZoomFromReferenceZoom(zoom) +
                "/" + x +
                "/" + y;
    }

    protected int getParticularZoomFromReferenceZoom(int referenceZoom) {
        return referenceZoom - 8;
    }

    protected int getReferenceZoomFromParticularZoom(int particularZoom) {
        return particularZoom + 8;
    }
}
