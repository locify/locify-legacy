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

import com.locify.client.data.SettingsData;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.maps.fileMaps.FileMapViewPort;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.fileMaps.FileMapConfig;
import com.locify.client.maps.fileMaps.StoreManager;
import com.locify.client.maps.fileMaps.StoreManagerMapInfo;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.maps.projection.MercatorProjection;
import com.locify.client.maps.projection.ReferenceEllipsoid;
import com.locify.client.maps.projection.S42Projection;
import com.locify.client.maps.projection.UTMProjection;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import javax.microedition.lcdui.Graphics;
import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 * Layer for rendering offline maps
 * @author Menion
 */
public class FileMapLayer implements MapLayer {

    /** parent screen of this layer */
    private MapScreen mapScreen;
    /** map scale in Width*/
    private double mapScaleW;
    /** map scale in Height*/
    private double mapScaleH;

    private FileMapViewPort viewPort;
    /** coeficient defined as moving change per click in X*/
    private double moveCoefPerPixelX = 0;
    /** coeficient defined as moving change per click in Y*/
    private double moveCoefPerPixelY = 0;

    private Vector imageExist;
    private Vector imageNotExist;
    private Vector managers;

public static long TIME;

    public FileMapLayer(MapScreen parent) {
        this.mapScreen = parent;
        this.managers = new Vector();
        this.imageExist = new Vector();
        this.imageNotExist = new Vector();
    }

    public boolean isReady() {
//        return mapManager != null;
        return managers.size() > 0;
    }

    public static double[] convertGeoToMap(FileMapConfig fileMapConfig, Location4D loc) {
System.out.println("\nFileMapLayer.convertLocToMap()");
System.out.println("\n  defLat:" + loc.getLatitude() + " defLon:" + loc.getLongitude());
        double[] coo = new double[2];
        coo[0] = loc.getLatitude();
        coo[1] = loc.getLongitude();

        if (fileMapConfig.getMapProjection() instanceof UTMProjection ||
                fileMapConfig.getMapProjection() instanceof MercatorProjection) {
            if (!fileMapConfig.isSphericCoordinate()) {
                coo = fileMapConfig.getMapProjection().projectionToFlat(loc.getLatitude(), loc.getLongitude());
            }
        } else if (fileMapConfig.getMapProjection() instanceof S42Projection) {
            coo = ReferenceEllipsoid.convertWGS84toS42(loc.getLatitude(), loc.getLongitude());
System.out.println("\n  Lat:" + coo[0] + " lon:" + coo[1]);
            if (!fileMapConfig.isSphericCoordinate()) {
                coo = fileMapConfig.getMapProjection().projectionToFlat(coo[0], coo[1]);
            }
        }
System.out.println("\n  Xmap:" + coo[0] + " Ymap:" + coo[1]);
        return coo;
    }

    public static Location4D convertMapToGeo(FileMapConfig fileMapConfig, double x, double y) {
//System.out.println("\nFileMapLayer.convertMapToGeo()");
//System.out.println("\n  defX:" + x + " defY:" + y);
        double[] coo = new double[2];
        coo[0] = x;
        coo[1] = y;

        if (fileMapConfig.getMapProjection() instanceof UTMProjection ||
                fileMapConfig.getMapProjection() instanceof MercatorProjection) {
            if (!fileMapConfig.isSphericCoordinate()) {
                coo = fileMapConfig.getMapProjection().projectionToSphere(coo[1], coo[0]);
            }
        } else if (fileMapConfig.getMapProjection() instanceof S42Projection) {
            if (!fileMapConfig.isSphericCoordinate()) {
                coo = fileMapConfig.getMapProjection().projectionToSphere(coo[1], coo[0]);
            }
            coo = ReferenceEllipsoid.convertS42toWGS84(coo[0], coo[1]);
        }
//System.out.println("\n  Lat:" + coo[0] + " Lon:" + coo[1]);
        return new Location4D(coo[0], coo[1], 0.0f);
    }

    public boolean setLocationCenter(Location4D loc) {
        if (isReady()) {
            try {
                double[] coo = convertGeoToMap(getFirstManager().getFileMapConfig(), loc);
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
                viewPort.getCenter().getLatitude() - (moveCoefPerPixelY * latMultiplicator),
                viewPort.getCenter().getLongitude() + (moveCoefPerPixelX * lonMultiplicator),
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

    public synchronized boolean drawMap(Graphics gr, int mapPanX, int mapPanY) {
        try {
            if (isReady()) {
//Logger.log("  READY");
//TIME = System.currentTimeMillis();
                imageExist.removeAllElements();
                imageNotExist.removeAllElements();
                for (int i = managers.size() - 1; i >= 0; i--) {
                    int images = imageExist.size();
                    FileMapManager manager = (FileMapManager) managers.elementAt(i);
                    manager.drawActualMap(gr, viewPort, imageExist, imageNotExist, mapPanX, mapPanY);

                    if ((imageExist.size() - images) == 0 && managers.size() > 1) {
                        manager = null;
                        managers.removeElementAt(i);
                    }
                }
                MapScreen.getTileCache().newRequest(imageExist);
//Logger.log("Step 1: " + (System.currentTimeMillis() - TIME) + " managers: " + managers.size() +
//        ", imageExist: " + imageExist.size() + ", imageNotExist: " + imageNotExist.size());

                Image image;
                ImageRequest ir;
                for (int i = 0; i < imageNotExist.size(); i++) {
                    ir = (ImageRequest) imageNotExist.elementAt(i);
                    image = MapScreen.getImageNotExisted();
                    if (image != null) {
//Logger.log("  FileMapLayer.drawImages() NotExist: x: " + ir.x + " y: " + ir.y + " wi: " + image.getWidth() + " he: " + image.getHeight());
                        gr.drawImage(image, ir.x + (ir.tileSizeX - image.getWidth()) / 2,
                                ir.y + (ir.tileSizeY - image.getHeight()) / 2, Graphics.LEFT | Graphics.TOP);
                        gr.setColor(ColorsFonts.BLACK);
                        gr.drawRect(ir.x, ir.y, ir.tileSizeX, ir.tileSizeY);
                    }
                }

//Logger.log("Step 2: " + (System.currentTimeMillis() - TIME));
                for (int i = 0; i < imageExist.size(); i++) {
                    ir = (ImageRequest) imageExist.elementAt(i);
                    image = MapScreen.getTileCache().getImage(ir.fileName);
                    if (image != null) {
//Logger.log("  FileMapLayer.drawImages() Exist: x: " + ir.x + " y: " + ir.y + " wi: " + image.getWidth() + " he: " + image.getHeight());
                        if (image.equals(MapScreen.getImageConnectionNotFound()) ||
                        image.equals(MapScreen.getImageLoading()) ||
                        image.equals(MapScreen.getImageLoading()) ||
                        image.equals(MapScreen.getImageLoading())) {
                            gr.drawImage(image, ir.x + (ir.tileSizeX - image.getWidth()) / 2,
                                ir.y + (ir.tileSizeY - image.getHeight()) / 2, Graphics.LEFT | Graphics.TOP);
                            gr.setColor(ColorsFonts.BLACK);
                            gr.drawRect(ir.x, ir.y, ir.tileSizeX, ir.tileSizeY);
                        } else {
                            gr.drawImage(image, ir.x, ir.y, Graphics.LEFT | Graphics.TOP);
                        }
                    }
                }

//Logger.log("Step 3: " + (System.currentTimeMillis() - TIME) + " ine.size: " + imageNotExist.size());
                if (R.getSettings().getAutoload() == SettingsData.ON && imageNotExist.size() > 0) {
                    Location4D[] locs = getActualBoundingBox();
                    if (locs != null) {
                        Vector findedData = StoreManager.getMapsAroundScreen(locs[0].getLatitude(),
                            locs[0].getLongitude(), locs[1].getLatitude(), locs[1].getLongitude());
//Logger.log("Step 4: " + (System.currentTimeMillis() - TIME));
                        for (int i = 0; i < findedData.size(); i++) {
                            StoreManagerMapInfo smmi = (StoreManagerMapInfo) findedData.elementAt(i);
                            if (smmi.mapZoom == getActualZoomLevel() && smmi.mapZoom != 0) {
//Logger.log("Add: " + smmi.mapName);
                                addNextMapManager(StoreManager.getInitializedOfflineMap(smmi.mapName, false),
                                        false, false);
                            }
                        }
                    }
                }

/*                // RED END MAP LINE
                for (int i = managers.size() - 1; i >= 0; i--) {
                    FileMapManager manager = (FileMapManager) managers.elementAt(i);

                    Point2D.Int p1 = getLocationCoord(convertMapToGeo(manager.getFileMapConfig(),
                            manager.getFileMapConfig().getMapViewPort().getCalibrationCorner(1).getLatitude(),
                            manager.getFileMapConfig().getMapViewPort().getCalibrationCorner(1).getLongitude()));
                    Point2D.Int p2 = getLocationCoord(convertMapToGeo(manager.getFileMapConfig(),
                            manager.getFileMapConfig().getMapViewPort().getCalibrationCorner(4).getLatitude(),
                            manager.getFileMapConfig().getMapViewPort().getCalibrationCorner(4).getLongitude()));
                    if (p1.x < 0)
                        p1.x = -5;
                    if (p1.y < 0)
                        p1.y = -5;
                    if (p2.x < 0)
                        p2.x = -5;
                    if (p2.y < 0)
                        p2.y = -5;
                    gr.setColor(ColorsFonts.RED);
                    gr.drawRect(p1.x, p1.y, p2.x, p2.y);
                }*/
//Logger.log("Step 5: " + (System.currentTimeMillis() - TIME));
                return true;
            }
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "FileMapLayer.drawMap()", " managers: " + managers.size());
        }
        return false;
    }

    public Point2D.Int getLocationCoord(Location4D loc) {
        if (isReady()) {
//Logger.log("Coo1: " + loc.toString());
            double[] coo = convertGeoToMap(getFirstManager().getFileMapConfig(), loc);
//Logger.log("Coo2: " + coo[0] + " " + coo[1]);
//Logger.log("Coo3: " + viewPort.convertGeoToMapPixel(new Location4D(coo[0], coo[1], 0.0f)).toString());
            return viewPort.convertGeoToMapPixel(new Location4D(coo[0], coo[1], 0.0f));
        } else {
            return null;
        }
    }

    /**********************/
    /*  UNUSED FUNCTIONS  */
    /**********************/
    public void nextProvider() {
        return;
    }

    public void nextMode() {
        return;
    }

    public int getProviderCount() {
        return 0;
    }

    public int getModeSize() {
        return 0;
    }

    public String getProviderName() {
        String name = "";
        for (int i = 0; i < managers.size(); i++) {
            name += ((FileMapManager) managers.elementAt(i)).getMapName();
        }
        return name;
    }

    public Vector getProvidersAndModes() {
        return new Vector();
    }

    public boolean addNextMapManager(FileMapManager fmm, boolean removeAll, boolean force) {
//Logger.debug(fmm.isReady() + " " + fmm.getMapName());
        if (alreadyLoaded(fmm))
            return false;
        
        if (removeAll) {
            destroyMap();
            setProviderAndMode(fmm);
            managers.addElement(fmm);
            return true;
        } else {
            if (compareToFirstProvidet(fmm)) {
                setProviderAndMode(fmm);
                managers.addElement(fmm);
                return true;
            } else {
                if (force) {
                    destroyMap();
                    setProviderAndMode(fmm);
                    managers.addElement(fmm);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    private void setProviderAndMode(FileMapManager fmm) {
        mapScaleW = fmm.getFileMapConfig().getLonDiffPerPixel() * Capabilities.getWidth();
        mapScaleH = fmm.getFileMapConfig().getLatDiffPerPixel() * Capabilities.getHeight();

        Location4D center;
        if (viewPort != null)
            center = viewPort.getCenter();
        else
            center = new Location4D(0.0, 0.0, 0.0f);

        this.viewPort = new FileMapViewPort(
                center,
                mapScaleW,
                mapScaleH,
                Capabilities.getWidth(),
                Capabilities.getHeight());
        this.moveCoefPerPixelX = viewPort.getLongitudeDimension() / Capabilities.getWidth();
        this.moveCoefPerPixelY = viewPort.getLatitudeDimension() / Capabilities.getHeight();
//Logger.log("    mapScaleW: " + mapScaleW + " mapScaleH: " + mapScaleH);
//Logger.log("    Xcoef: " + moveCoefPerPixelX + " Ycoef: " + moveCoefPerPixelY);
//Logger.log("    lonDim: " + viewPort.getLongitudeDimension() + " latDim: " + viewPort.getLatitudeDimension());
    }

    private boolean compareToFirstProvidet(FileMapManager fmm) {
        if (managers.size() == 0) {
            return true;
        } else {
            FileMapManager firstFmm = (FileMapManager) managers.elementAt(0);
            double mapScale1W = firstFmm.getFileMapConfig().getLonDiffPerPixel();
            double mapScale1H = firstFmm.getFileMapConfig().getLatDiffPerPixel();
            double mapScale2W = fmm.getFileMapConfig().getLonDiffPerPixel();
            double mapScale2H = fmm.getFileMapConfig().getLatDiffPerPixel();
//Logger.log("  FileMapLayer.compareToFirstProvider() " + mapScale1H + " " + mapScale2H + " " + mapScale1W + " " + mapScale2W);
            if (mapScale1W == mapScale2W && mapScale1H == mapScale2H)
                return true;
            return false;
        }
    }

    private boolean alreadyLoaded(FileMapManager fmm) {
        for (int i = 0; i < managers.size(); i++) {
            FileMapManager test = (FileMapManager) managers.elementAt(i);
            if (test.getMapName().equals(fmm.getMapName()))
                return true;
        }
        return false;
    }

    private FileMapManager getFirstManager() {
        if (isReady())
            return (FileMapManager) managers.elementAt(0);
        else
            return null;
    }

    public int getActualZoomLevel() {
        if (isReady()) {
            return getFirstManager().getActualZoomLevel();
        } else {
            return 0;
        }
    }

    public String getMapLayerName() {
        if (isReady()) {
            return getFirstManager().getMapName();
        } else {
            return "";
        }
    }

    public int getMaxZoomLevel() {
        if (isReady()) {
            return getFirstManager().getMaxZoomLevel();
        } else {
            return 0;
        }
    }

    public int getMinZoomLevel() {
        if (isReady()) {
            return getFirstManager().getMinZoomLevel();
        } else {
            return 0;
        }
    }

    public void zoomIn() {
        setZoomLevel(getActualZoomLevel() + 1);
    }

    public void zoomOut() {
        setZoomLevel(getActualZoomLevel() - 1);
    }

    public void setZoomLevel(int zoom_level) {
        if (isReady() && zoom_level >= getMinZoomLevel() && zoom_level <= getMaxZoomLevel()) {
            getFirstManager().setZoomLevel(zoom_level);
            setProviderAndMode(getFirstManager());
        }
    }

    public void repaint() {
    }

    public void calculateZoomFrom(Location4D[] positions) {
        return;
    }

    public void destroyMap() {
        for (int i = 0; i < managers.size(); i++) {
            FileMapManager fm = ((FileMapManager) managers.elementAt(i));
            fm = null;
        }
        managers.removeAllElements();
    }

    public Location4D[] getActualBoundingBox() {
        if (isReady()) {
            Location4D[] loc = new Location4D[2];
//Logger.log("C " + viewPort.getCalibrationCorner(1).toString());
            if (managers.size() > 0) {
                loc[0] = convertMapToGeo(getFirstManager().getFileMapConfig(),
                        viewPort.getCalibrationCorner(1).getLatitude(),
                        viewPort.getCalibrationCorner(1).getLongitude());
                loc[1] = convertMapToGeo(getFirstManager().getFileMapConfig(),
                        viewPort.getCalibrationCorner(4).getLatitude(),
                        viewPort.getCalibrationCorner(4).getLongitude());
            }
            return loc;
        } else {
            return null;
        }
    }
}
