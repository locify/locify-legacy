/*
 * MapContent.java
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
import com.locify.client.gui.screen.internal.MainScreen;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.locator.*;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.maps.mapItem.MapItem;
import com.locify.client.maps.mapItem.MapItemManager;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.math.LMath;
import com.sun.lwuit.Container;
import com.sun.lwuit.Graphics;

/**
 *
 * @author menion
 */
public class MapContent implements LocationEventListener {

    /** map holder - container */
    private Container parent;
    /** state value if zooming is activated */
    private boolean zoomProcess;
    /** lock for painting */
    private boolean drawLock;
    /** show all items during panning */
    private boolean showAllDuringPanning;
    /** flag indikates if map is fixed to actual position */
    private boolean centerToActualLocation;
    /** laste center point */
    private Location4D lastCenterPoint;
    /** actual selected map layer */
    private MapLayer map;
    /** map manager for online maps */
    private TileMapLayer mapTile;
    /** map manager for file maps */
    private FileMapLayer mapFile;
    /** manager for objects to show on screen */
    private MapItemManager mapItemManager;
    /** if item was added before map was inicialized, call it after that */
    private MapItem newMapItem;
    /** actual content size width */
    private int actualWidth;
    /** actual content size height */
    private int actualHeight;

    public MapContent() {
        drawLock = false;
        zoomProcess = false;
        centerToActualLocation = true;

        mapItemManager = R.getMapItemManager();
        mapItemManager.init();
        
        // set map tiles and providers
        mapTile = new TileMapLayer();
        mapFile = new FileMapLayer();

        if (!R.getSettings().isDefaultMapProviderOnline()) {
            map = mapFile;
        } else {
            mapTile.setProviderAndMode(R.getSettings().getDefaultMapProvider());
            mapTile.setDefaultZoomLevel();
            map = mapTile;
        }

        if (R.getSettings().getPanning() == SettingsData.REPAINT_DURING) {
            showAllDuringPanning = true;
        } else {
            showAllDuringPanning = false;
        }

        if (lastCenterPoint != null) {
            centerMap(lastCenterPoint, centerToActualLocation);
        } else {
            centerMap(R.getLocator().getLastLocation(), centerToActualLocation);
        }

        R.getLocator().addLocationChangeListener(this);
    }

    public TileMapLayer getTileMapLayer() {
        return mapTile;
    }

    public FileMapLayer getFileMapLayer() {
        return mapFile;
    }

    public MapLayer getActualMapLayer() {
        return map;
    }

    public void  setMapLayer(MapLayer mapLayer) {
        if (mapLayer != null)
            this.map = mapLayer;
    }

    public boolean isZoomProcess() {
        return zoomProcess;
    }

    public boolean isPanProcess() {
        return panProcess;
    }

    public boolean isPaintDuringPanning() {
        return showAllDuringPanning;
    }

    public boolean isCenterToActualLocation() {
        return centerToActualLocation;
    }

    public void setCenterToActualLocation(boolean center) {
        this.centerToActualLocation = center;
    }

    public Location4D getLastCenterPoint() {
        return lastCenterPoint;
    }

    public void setLastCenterPoint(Location4D center) {
        this.lastCenterPoint = center;
    }

    public void setNewMapItem(MapItem item) {
        this.newMapItem = item;
    }

    public MapItemManager getMapItemManager() {
        return mapItemManager;
    }

    public void setOnlineMaps() {
        mapTile.setProviderAndMode(0);
        mapTile.setDefaultZoomLevel();
        map = mapTile;
    }

    public void setFileMap(FileMapManager fmm, Location4D center) {
        try {
            if (fmm != null && fmm.isReady()) {
                mapFile.addNextMapManager(fmm, true, true);
                map = mapFile;
                if (newMapItem != null) {
                    centerMap(newMapItem.getItemCenter(), false);
//                    objectZoomTo(newMapItem);
                    newMapItem = null;
                } else {
                    centerMap(center, false);
                }
            } else {
                Logger.error("MapScreen.setFileMap() error");
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.SetFileMap()", null);
        }
    }

    public void centerMap(Location4D newCenter, boolean centerToActualLocation) {
        this.lastCenterPoint = newCenter;
        this.centerToActualLocation = centerToActualLocation;
        
        map.setLocationCenter(lastCenterPoint);
        mapItemManager.disableInitializeState();
    }

    public void registerParent(Container parent) {
        this.parent = parent;
    }

    public Container getParent() {
        return parent;
    }

    public boolean isOffLineMapEnable() {
        return getActualMapLayer() instanceof FileMapLayer;
    }

    /**
     * Still not functional ... parent canvas didn't call paint() ... :(
     */
    public void repaint() {
        if (parent != null && parent.isVisible()) {
            parent.repaint();
        }
    }

    public void paintAll(Graphics g) {
        try {
            if (drawLock) {
                return;
            }
            drawLock = true;
            
            drawMap(g);
            drawActualLocationPoint(g);
            drawSelectionCircle(g);
            drawMapItem(g, MapItem.PRIORITY_HIGH);
            drawMapItem(g, MapItem.PRIORITY_MEDIUM);
            drawZoomProcess(g);
            drawMapItem(g, MapItem.PRIORITY_LOW);
            
            drawLock = false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.paint()", null);
        }
    }
    
    /**
     * Draws map background (tiles) and other screen components like zoom scale,
     * location pointer etc.
     */
    public void drawMap(Graphics g) {
        actualWidth = g.getClipWidth();
        actualHeight = g.getClipHeight();
        
        try {
            map.drawMap(g, -1 * panMoveX, -1 * panMoveY);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "map.drawMap()");
        }
    }

    public void drawActualLocationPoint(Graphics g) {
        if (R.getLocator().hasValidLocation()) {
            Point2D.Int actPoint = map.getLocationCoord(R.getLocator().getLastLocation());
            float heading = R.getLocator().getHeading();

            if (actPoint != null) {
                g.drawImage(MapImages.getMapIconActualLocation(), actPoint.x - panMoveX - MapImages.getMapIconActualLocation().getWidth() / 2 + g.getClipX(),
                        actPoint.y - panMoveY - MapImages.getMapIconActualLocation().getHeight() / 2 + g.getClipY());

                if (R.getContext().getSource() == LocationContext.GPS && !R.getLocator().isSimulatedGPS()) {
                    int x1 = (int) (20.0 * Math.sin((heading - 30) / LMath.RHO));
                    int y1 = (int) (20.0 * Math.cos((heading - 30) / LMath.RHO));
                    int x2 = (int) (40.0 * Math.sin((heading) / LMath.RHO));
                    int y2 = (int) (40.0 * Math.cos((heading) / LMath.RHO));
                    int x3 = (int) (20.0 * Math.sin((heading + 30) / LMath.RHO));
                    int y3 = (int) (20.0 * Math.cos((heading + 30) / LMath.RHO));
                    int x4 = (int) (25.0 * Math.sin((heading) / LMath.RHO));
                    int y4 = (int) (25.0 * Math.cos((heading) / LMath.RHO));

                    int cooX = actPoint.x - panMoveX + g.getClipX();
                    int cooY = actPoint.y - panMoveY + g.getClipY();

                    g.setColor(ColorsFonts.GREEN_SHINY);
                    g.fillTriangle(cooX + x1, cooY - y1, cooX + x2, cooY - y2, cooX + x4, cooY - y4);
                    g.fillTriangle(cooX + x3, cooY - y3, cooX + x2, cooY - y2, cooX + x4, cooY - y4);

                    g.setColor(ColorsFonts.BLACK);
                    g.drawLine(cooX + x1, cooY - y1, cooX + x2, cooY - y2);
                    g.drawLine(cooX + x2, cooY - y2, cooX + x3, cooY - y3);
                    g.drawLine(cooX + x3, cooY - y3, cooX + x4, cooY - y4);
                    g.drawLine(cooX + x4, cooY - y4, cooX + x1, cooY - y1);
                }
            }
        }
    }

    public void drawSelectionCircle(Graphics g) {
        g.setColor(ColorsFonts.BLACK);
        g.drawArc(actualWidth / 2 - 2 + g.getClipX(), actualHeight / 2 - 2 + g.getClipY(), 4, 4, 0, 360);
    }

    public void drawZoomProcess(Graphics g) {
        if (zoomProcess) {
            try {
                int moveX = 30;
                int bw = borderW / 2;
                int bh = borderH / 2;
                int val1 = 0;
                int val2 = 0;

                if (zoomTotalValue > 0) {
                    g.setColor(ColorsFonts.BLUE);
                    val1 = 5;
                    val2 = 15;
                } else if (zoomTotalValue < 0) {
                    g.setColor(ColorsFonts.RED);
                    val1 = 15;
                    val2 = 5;
                } else {
                    g.setColor(ColorsFonts.LIGHT_ORANGE);
                }
                g.drawRoundRect(pxMoveX - bw, pxMoveY - bh, borderW, borderH, 5, 5);

                if (zoomTotalValue != 0) {
                    g.fillTriangle(pxMoveX - bw - val1, pxMoveY,
                            pxMoveX - bw - val2, pxMoveY - 5,
                            pxMoveX - bw - val2, pxMoveY + 5);
                    g.fillTriangle(pxMoveX + bw + val1, pxMoveY,
                            pxMoveX + bw + val2, pxMoveY - 5,
                            pxMoveX + bw + val2, pxMoveY + 5);
                    g.fillTriangle(pxMoveX, pxMoveY - bh - val1,
                            pxMoveX - 5, pxMoveY - bh - val2,
                            pxMoveX + 5, pxMoveY - bh - val2);
                    g.fillTriangle(pxMoveX, pxMoveY + bh + val1,
                            pxMoveX - 5, pxMoveY + bh + val2,
                            pxMoveX + 5, pxMoveY + bh + val2);
                }

                if (!MainScreen.hasPointerEvents) {
                    g.drawImage(MapImages.getMapIconPlus(), 20 - MapImages.getMapIconPlus().getWidth() / 2,
                            10 - MapImages.getMapIconPlus().getHeight() / 2);
                    g.drawImage(MapImages.getMapIconMinus(), 20 - MapImages.getMapIconMinus().getWidth() / 2,
                            actualHeight - 10 - MapImages.getMapIconMinus().getHeight() / 2);
                }

                g.setColor(ColorsFonts.BLACK);
                g.drawLine(moveX + 5, 10, moveX + 5, actualHeight - 10);
                g.fillRect(moveX, 10, 10, 3);
                g.fillRect(moveX, actualHeight - 10, 10, 3);
                double pxPerZoom = (actualHeight - 20.0) /
                        (map.getMaxZoomLevel() - map.getMinZoomLevel());

                int actZoomPixel = (int) (10 + (map.getMaxZoomLevel() -
                        (map.getActualZoomLevel() + zoomTotalValue)) * pxPerZoom);

                g.setColor(ColorsFonts.RED);
                g.fillRect(moveX, actZoomPixel - 1, 10, 3);
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.drawMap()", "zoomProcess");
            }
        }
    }

    public void drawMapItem(Graphics g, int priority) {
        try {
            mapItemManager.drawItems(g, priority);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapItemManager.drawItems(), prioriry: " + priority);
        }
    }
    /*********************************************/
    /*           PAN ACTION SECTION             */
    /*********************************************/

    /** thread for zooming */
    private PanThread panThread;
    /** boolean if zoom managing is active */
    private boolean panProcess = false;
    private long panLastAction;
    private int panMoveX;
    private int panMoveY;

    public void makePanAction(int panX, int panY) {
        if (!panProcess) {
            panThread = new PanThread();
            panThread.start();
            panProcess = true;

            panMoveX = 0;
            panMoveY = 0;
        }

        panMoveX += panX;
        panMoveY += panY;

        panLastAction = System.currentTimeMillis();
        repaint();
    }

    private class PanThread extends Thread {

        public PanThread() {
        }

        public void run() {
            try {
                panLastAction = System.currentTimeMillis();
                while (true) {
                    Thread.sleep(250);
                    if ((System.currentTimeMillis() - panLastAction) > 300) {
                        break;
                    }
                }
                panProcess = false;
                map.pan(panMoveX, panMoveY);

                // set as center new location
                centerToActualLocation = false;
                mapItemManager.panItem(-1 * panMoveX, -1 * panMoveY);

                panMoveX = 0;
                panMoveY = 0;
                // UGLY SPECIFIC ... grrrr

                if (parent instanceof MapScreen)
                    R.getMapScreen().selectNearestWaypointsAtCenter();
//System.out.println("RepaintCall");
                repaint();
                // call this after repaint (need to refresh mapViewPort bounds in TileMapLayer)
                lastCenterPoint = map.getLocationCoord(actualWidth / 2, actualHeight / 2);
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.PanThread.run()", null);
            }
        }
    }

    /*********************************************/
    /*           ZOOM ACTION SECTION             */
    /*********************************************/

    public static final int ZOOM_IN = 0;
    public static final int ZOOM_OUT = 1;
    public static final int ZOOM_LEFT = 2;
    public static final int ZOOM_RIGHT = 3;
    public static final int ZOOM_UP = 4;
    public static final int ZOOM_DOWN = 5;
    public static final int ZOOM_PAN = 6;
    /** thread for zooming */
    private ZoomThread zoomThread;
    private int zoomTotalValue;
    private long zoomLastAction;
    private int borderWdef,  borderHdef;
    private int borderW,  borderH;
    private int pxMoveX;
    private int pxMoveY;
    private int movePartImageValue = 3;

    public void makeZoomAction(int actionType, int panX, int panY) {
        // disable zoom for maps without zoom
        if (map.getMaxZoomLevel() - map.getMinZoomLevel() == 0) {
            return;
        }

        if (!zoomProcess) {
            zoomThread = new ZoomThread();
            zoomThread.start();
            zoomProcess = true;

            zoomTotalValue = 0;

            borderW = borderWdef = actualWidth;
            borderH = borderHdef = actualHeight;
            pxMoveX = actualWidth / 2;
            pxMoveY = actualHeight / 2;
        }

        if (actionType == ZOOM_IN || actionType == ZOOM_OUT) {
            if (actionType == ZOOM_IN) {
                if ((map.getActualZoomLevel() + zoomTotalValue) < map.getMaxZoomLevel()) {
                    zoomTotalValue += 1;
                }
            }
            if (actionType == ZOOM_OUT) {
                if ((map.getActualZoomLevel() + zoomTotalValue) > map.getMinZoomLevel()) {
                    zoomTotalValue -= 1;
                }
            }

            borderW = (int) ((1 / LMath.pow(2, Math.abs(zoomTotalValue))) * borderWdef);
            borderH = (int) ((1 / LMath.pow(2, Math.abs(zoomTotalValue))) * borderHdef);
        } else if (actionType == ZOOM_UP) {
            pxMoveY -= borderH / movePartImageValue;
        } else if (actionType == ZOOM_DOWN) {
            pxMoveY += borderH / movePartImageValue;
        } else if (actionType == ZOOM_LEFT) {
            pxMoveX -= borderW / movePartImageValue;
        } else if (actionType == ZOOM_RIGHT) {
            pxMoveX += borderW / movePartImageValue;
        } else if (actionType == ZOOM_PAN) {
            pxMoveX += panX;
            pxMoveY += panY;
        }

        zoomLastAction = System.currentTimeMillis();
        repaint();
    }

    public int getActualZoomPanX() {
        return pxMoveX;
    }

    public int getActualZoomPanY() {
        return pxMoveY;
    }

    private class ZoomThread extends Thread {

        public ZoomThread() {
        }

        public void run() {
            try {
                zoomLastAction = System.currentTimeMillis();
                while (true) {
                    Thread.sleep(250);
                    if ((System.currentTimeMillis() - zoomLastAction) > 1500) {
                        break;
                    }
                }
                zoomProcess = false;
                if ((pxMoveX - actualWidth / 2) != 0 || (pxMoveY - actualHeight / 2) != 0) {
                    map.pan(pxMoveX - actualWidth / 2, pxMoveY - actualHeight / 2);
                }
                if (zoomTotalValue != 0) {
                    map.setZoomLevel(map.getActualZoomLevel() + zoomTotalValue);
                }
                mapItemManager.disableInitializeState();

                // UGLY SPECIFIC ... grrrr
                if (parent instanceof MapScreen)
                    R.getMapScreen().selectNearestWaypointsAtCenter();
                repaint();
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.ZoomThread.run()", null);
            }
        }
    }

    /***************************************************/
    /*           LOCATION LISTENER SECTION             */
    /***************************************************/

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        try {
            if (centerToActualLocation) {
                centerMap(location, centerToActualLocation);
            }
            repaint();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.locationChanged()", null);
        }
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
    }
}
