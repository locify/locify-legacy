package com.locify.client.maps.tiles.impl;

import com.locify.client.maps.MapLayer;
import com.locify.client.maps.tiles.AbstractTileFactory;
import com.locify.client.maps.tiles.TileFactoryInfo;

public class OpenStreetMapCycloTileFactory extends AbstractTileFactory {

    public OpenStreetMapCycloTileFactory(MapLayer canvasToRepaintWhenLoaded) {
        super(new TileFactoryInfo(
                "OSM Cyclo",
                "",
                8,
                25,
                256,
                true,
                true,
                "http://b.andy.sandbox.cloudmade.com/tiles/cycle",
                ".png"), canvasToRepaintWhenLoaded);
    }

    protected String getCoordinatePart(int x, int y, int referenceZoom) {
        return "/" + getParticularZoomFromReferenceZoom(referenceZoom) +
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
