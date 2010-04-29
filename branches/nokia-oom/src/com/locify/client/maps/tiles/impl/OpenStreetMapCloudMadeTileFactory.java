package com.locify.client.maps.tiles.impl;

import com.locify.client.maps.MapLayer;
import com.locify.client.maps.tiles.AbstractTileFactory;
import com.locify.client.maps.tiles.TileFactoryInfo;

public class OpenStreetMapCloudMadeTileFactory extends AbstractTileFactory {

    public OpenStreetMapCloudMadeTileFactory(MapLayer canvasToRepaintWhenLoaded) {
        super(new TileFactoryInfo(
                "OSM CloudMade",
                "",
                8,
                25,
                256,
                true,
                true,
                "http://b.tile.cloudmade.com/aa1ead9bc962504992b97a6219b4a8d0/4/256",
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
