/*
 * FileMapLayer.java
 * This file is part of Locify.
 *
 * Locify is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * (read more at: http://www.gnu.org/licenses/gpl.html)
 *
 * Commercial licenses are also available, please
 * refer http://code.google.com/p/locify/ for details.
 */
package com.locify.client.maps;

import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.maps.fileMaps.FileMapViewPort;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.fileMaps.ConfigFileTile;
import com.locify.client.maps.fileMaps.FileMapManagerMulti;
import com.locify.client.maps.fileMaps.FileMapManagerSingle;
import com.locify.client.maps.fileMaps.FileMapManagerTar;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.maps.projection.ReferenceEllipsoid;
import com.locify.client.maps.projection.S42Projection;
import com.locify.client.maps.projection.UTMProjection;
import com.locify.client.utils.R;
import javax.microedition.lcdui.Graphics;
import java.util.Vector;

/**
 * Layer for rendering offline maps
 * @author Menion
 */
public class FileMapLayer implements MapLayer {

    /** parent screen of this layer */
    private MapScreen parent;
    /** map scale */
    private double mapScaleW,  mapScaleH;
    private FileMapManager mapManager;
    private FileMapViewPort viewPort;
    /** coeficient defined as moving change per click */
    private double moveCoefPerPixelX = 0;
    private double moveCoefPerPixelY = 0;
    /** actual selected map provider */
    private int selectedProvider;
    private Vector availeableProviders;

    public FileMapLayer(MapScreen parent) {
        this.parent = parent;
        getProvidersAndModes();
    }

    public boolean isReady() {
        return mapManager != null;
    }

    private double[] convertGeoToMap(Location4D loc) {
//System.out.println("\nFileMapLayer.convertLocToMap()");
//System.out.println("\n  defLat:" + loc.getLatitude() + " defLon:" + loc.getLongitude());
        double[] coo = new double[2];
        coo[0] = loc.getLatitude();
        coo[1] = loc.getLongitude();

        if (mapManager.getMapProjection() instanceof UTMProjection) {
            if (!mapManager.getConfigFileTile().isSphericCoordinate()) {
                coo = mapManager.getMapProjection().projectionToFlat(loc.getLatitude(), loc.getLongitude());
            }
        } else if (mapManager.getMapProjection() instanceof S42Projection) {
            coo = ReferenceEllipsoid.convertWGS84toS42(loc.getLatitude(), loc.getLongitude());
//System.out.println("\n  Lat:" + coo[0] + " lon:" + coo[1]);
            if (!mapManager.getConfigFileTile().isSphericCoordinate()) {
                coo = mapManager.getMapProjection().projectionToFlat(coo[0], coo[1]);
            }
        }
//System.out.println("\n  Xmap:" + coo[0] + " Ymap:" + coo[1]);
        return coo;
    }

    private Location4D convertMapToGeo(double x, double y) {
//System.out.println("\nFileMapLayer.convertLocToMap()");
//System.out.println("\n  defX:" + x + " defY:" + y);
        double[] coo = new double[2];
        coo[0] = x;
        coo[1] = y;

        if (mapManager.getMapProjection() instanceof UTMProjection) {
            if (!mapManager.getConfigFileTile().isSphericCoordinate()) {
                coo = mapManager.getMapProjection().projectionToSphere(coo[0], coo[1]);
            }
        } else if (mapManager.getMapProjection() instanceof S42Projection) {
            if (!mapManager.getConfigFileTile().isSphericCoordinate()) {
                coo = mapManager.getMapProjection().projectionToSphere(coo[1], coo[0]);
            }
            coo = ReferenceEllipsoid.convertS42toWGS84(coo[0], coo[1]);
        }
//System.out.println("\n  Lat:" + coo[0] + " Lon:" + coo[1]);
        return new Location4D(coo[0], coo[1], 0.0f);
    }

    public boolean setLocationCenter(Location4D loc) {
        if (isReady()) {
            try {
                double[] coo = convertGeoToMap(loc);
                this.viewPort.setCenter(new Location4D(coo[0], coo[1], 0.0f));
                return true;
            } catch (Exception e) {
                R.getErrorScreen().view(e, "FileMapLayer.setLocationCenter", null);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Move in x, y direction.
     * @param lonMultiplicator
     * @param latMultiplicator
     */
    public void pan(int lonMultiplicator, int latMultiplicator) {
        viewPort.setCenter(new Location4D(
                viewPort.center.getLatitude() - (moveCoefPerPixelY * latMultiplicator),
                viewPort.center.getLongitude() + (moveCoefPerPixelX * lonMultiplicator),
                0));
        repaint();
    }

    public void panRight() {
        pan(PAN_PIXELS, 0);
    }

    public void panLeft() {
        pan(-PAN_PIXELS, 0);
    }

    public void panUp() {
        pan(0, -PAN_PIXELS);
    }

    public void panDown() {
        pan(0, PAN_PIXELS);
    }

    public boolean drawMap(Graphics gr, int mapPanX, int mapPanY) {
        if (isReady()) {
            return mapManager.drawActualMap(gr, viewPort, mapPanX, mapPanY);
        } else {
            return false;
        }
    }

    public Point2D.Int getLocationCoord(Location4D loc) {
        if (isReady()) {
            double[] coo = convertGeoToMap(loc);
            return viewPort.convertGeoToMapPixel(new Location4D(coo[0], coo[1], 0.0f));
        } else {
            return null;
        }
    }

    /**********************/
    /*  UNUSED FUNCTIONS  */
    /**********************/
    public void nextProvider() {
        //actually using as previous mode
        if (selectedProvider > 0) {
            setProviderAndMode(selectedProvider--);
        } else {
            setProviderAndMode(availeableProviders.size() - 1);
        }
    }

    public void nextMode() {
        if (selectedProvider < availeableProviders.size() - 1) {
            setProviderAndMode(selectedProvider++);
        } else {
            setProviderAndMode(0);
        }
    }

    public int getProviderCount() {
        return availeableProviders.size();
    }

    public int getModeSize() {
        return 0;
    }

    public String getProviderName() {
        return (String) availeableProviders.elementAt(selectedProvider);
    }

    public Vector getProvidersAndModes() {
        availeableProviders = new Vector();
        for (int i = 0; i < R.getFileMapProviders().getNumOfProviders(); i++) {
            availeableProviders.addElement(R.getFileMapProviders().getProviderName(i));
        }

        return availeableProviders;
    }

    public boolean setProviderAndMode(int number) {
        if (number < availeableProviders.size()) {
            FileMapManager manager;
            System.gc();

            String mapPath = R.getFileMapProviders().getProviderPath(
                    (String) availeableProviders.elementAt(number));
            int mapType[] = FileMapManager.getMapType(mapPath);
            ConfigFileTile map = null;
//System.out.println("\nMapPath: " + mapPath + " MapType: " + mapType[0] + " MapCategory: " + mapType[1]);
            if (mapType[0] == FileMapManager.MAP_TYPE_SINGLE_TILE) {
                manager = new FileMapManagerSingle(mapPath, mapType[1]);
            } else if (mapType[0] == FileMapManager.MAP_TYPE_MULTI_TILE) {
                manager = new FileMapManagerMulti(mapPath, mapType[1]);
            } else if (mapType[0] == FileMapManager.MAP_TYPE_MULTI_TILE_LOCAL_TAR) {
                manager = new FileMapManagerTar(mapPath, mapType[1]);
            } else {
                return false;
            }

            /* SET MAP SCALE */
            if (manager.isReady()) {
                map = manager.getConfigFileTile();

                if (map != null) {
                    mapScaleW = map.getLonDiffPerPixel() * parent.getWidth();
                    mapScaleH = map.getLatDiffPerPixel() * parent.getHeight();
                } else {
                    mapScaleW = 1.0 / 60.0;
                    mapScaleH = mapScaleW * parent.getHeight() / parent.getWidth();
                }

                this.viewPort = new FileMapViewPort(
                        new Location4D(0, 0, 0),
                        mapScaleW,
                        mapScaleH,
                        parent.getWidth(),
                        parent.getHeight());

                this.moveCoefPerPixelX = viewPort.longitude_dimension / parent.getWidth();
                this.moveCoefPerPixelY = viewPort.latitude_dimension / parent.getHeight();
//System.out.println("\n  Xcoef: " + moveCoefPerPixelX + " Ycoef: " + moveCoefPerPixelY);
//System.out.println("\n  lonDim: " + viewPort.longitude_dimension + " latDim: " + viewPort.latitude_dimension);
            } else {
                return false;
            }
            this.selectedProvider = number;
            this.mapManager = manager;
            return true;
        }
        return false;
    }

    public int getActualZoomLevel() {
        return 0;
    }

    public String getMapLayerName() {
        return "Maps";
    }

    public int getMaxZoomLevel() {
        return 0;
    }

    public int getMinZoomLevel() {
        return 0;
    }

    public void zoomIn() {
        return;
    }

    public void zoomOut() {
        return;
    }

    public void setZoomLevel(int zoom_level) {
        return;
    }

    public void setDefaultZoomLevel() {
        return;
    }

    public void repaint() {
    }

    public void calculateZoomFrom(Location4D[] positions) {
    }

    public void destroyMap() {
        mapManager = null;
    }

    public Location4D[] getActualBoundingBox() {
        Location4D[] loc = new Location4D[2];

        loc[0] = convertMapToGeo(viewPort.A.getLatitude(), viewPort.A.getLongitude());
        loc[1] = convertMapToGeo(viewPort.D.getLatitude(), viewPort.D.getLongitude());
        return loc;
    }
}
