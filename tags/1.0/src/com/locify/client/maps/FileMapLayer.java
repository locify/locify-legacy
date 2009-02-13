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
import com.locify.client.maps.fileMaps.FileMapManagerTarLocify;
import com.locify.client.maps.fileMaps.FileMapManagerTarTrekBuddy;
import com.locify.client.maps.geometry.Point2D;
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
    private double mapScaleW, mapScaleH;
    private FileMapManager mapManager;
    private FileMapViewPort viewPort;
    /** coeficient defined as moving change per click */
    private double moveCoefPerPixelX = 0;
    private double moveCoefPerPixelY = 0;
    /** actual selected map provider */
    private int selectedProvider;
    /** pan value */
    private int panValue = 25;

    private Vector availeableProviders;

    public FileMapLayer(MapScreen parent) {   
        this.parent = parent;
        this.availeableProviders = new Vector();
    }

    public boolean isReady() {
        return mapManager != null;
    }
    
    public boolean setLocationCenter(Location4D loc) {
        if (isReady()) {
            try {
                double[] res = mapManager.getMapProjection().projectionToFlat(loc.getLatitude(), loc.getLongitude());
                this.viewPort.setCenter(new Location4D(res[0], res[1], loc.getAltitude()));
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
        pan(panValue, 0);
    }

    public void panLeft() {
        pan(-panValue, 0);
    }

    public void panUp() {
        pan(0, -panValue);
    }

    public void panDown() {
        pan(0, panValue);
    }
    
    public int getPanSpeed() {
        return panValue;
    }

    public boolean drawMap(Graphics gr) {
        if (isReady()) {
            return mapManager.drawActualMap(gr, viewPort);
        } else {
            return false;
        }
    }
    
    public Point2D.Int getLocationCoord(Location4D loc) {
        if (isReady()) {
            double[] res = mapManager.getMapProjection().projectionToFlat(loc.getLatitude(), loc.getLongitude());
            return viewPort.getPointAnyWhere(new Location4D(res[0], res[1], loc.getAltitude()));
        } else {
            return null;
        }
    }

    /**********************/
    /*  UNUSED FUNCTIONS  */
    /**********************/
    public void nextProvider() {
        //actually using as previous mode
        if (selectedProvider > 0)
            setProviderAndMode(selectedProvider--);
        else
            setProviderAndMode(availeableProviders.size() - 1);
    }

    public void nextMode() {
        if (selectedProvider < availeableProviders.size() - 1)
            setProviderAndMode(selectedProvider++);
        else
            setProviderAndMode(0);
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
        for (int i = 0; i < R.getSettings().getFileMapProviders().getNumOfProviders(); i++) {
            if (R.getSettings().getFileMapEnable(R.getSettings().getFileMapProviders().getProviderName(i)))
                availeableProviders.addElement(R.getSettings().getFileMapProviders().getProviderName(i));
        }
    
        return availeableProviders;
    }

    public boolean setProviderAndMode(int number) {
        if (number < availeableProviders.size()) {
            FileMapManager manager;
            System.gc();
            
            String mapPath = R.getSettings().getFileMapProviders().getProviderPath(
                    (String) availeableProviders.elementAt(number));
            int mapType = FileMapManager.getMapType(mapPath);
            ConfigFileTile map = null;
//System.out.println("\nMapPath: " + mapPath + " MapType: " + mapType);
            if (mapType == FileMapManager.MAP_TYPE_SINGLE_TILE) {
                manager = new FileMapManagerSingle(mapPath);
            } else if (mapType == FileMapManager.MAP_TYPE_MULTI_TILE) {
                manager = new FileMapManagerMulti(mapPath);
            } else if (mapType == FileMapManager.MAP_TYPE_MULTI_TILE_LOCAL_TAR_LOCIFY) {
                manager = new FileMapManagerTarLocify(mapPath);
            } else if (mapType == FileMapManager.MAP_TYPE_MULTI_TILE_LOCAL_TAR_TREKBUDDY) {
                manager = new FileMapManagerTarTrekBuddy(mapPath);
            } else {
                return false;
            }
        
            /* SET MAP SCALE */
            if (manager.isReady()) {
                    map = manager.getMapForScaling();

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
//System.out.println("\nXcoef: " + moveCoefPerPixelX + " Ycoef: " + moveCoefPerPixelY);
//System.out.println(parent.getWidth()  + "  " + parent.getHeight());
//System.out.println(viewPort.longitude_dimension  + "  " + viewPort.latitude_dimension);
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
}
