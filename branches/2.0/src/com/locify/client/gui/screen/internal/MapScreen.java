/*
 * MapScreen.java
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
package com.locify.client.gui.screen.internal;

import com.locify.client.data.IconData;
import com.locify.client.data.items.*;
import com.locify.client.data.SettingsData;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.maps.FileMapLayer;
import com.locify.client.maps.MapLayer;
import com.locify.client.maps.NetworkLinkDownloader;
import com.locify.client.maps.TileCache;
import com.locify.client.maps.TileMapLayer;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.maps.mapItem.DescriptionMapItem;
import com.locify.client.maps.mapItem.MapItem;
import com.locify.client.maps.mapItem.MapItemManager;
import com.locify.client.maps.mapItem.MapNavigationItem;
import com.locify.client.maps.mapItem.PointMapItem;
import com.locify.client.maps.mapItem.RouteMapItem;
import com.locify.client.maps.mapItem.ScreenOverlayMapItem;
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Logger;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Locale;
import com.locify.client.utils.math.LMath;
import com.locify.client.utils.R;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.layouts.BorderLayout;
import java.util.Vector;
import javax.microedition.lcdui.game.GameCanvas;
//#if planstudio
//# import com.locify.client.maps.planStudio.PlanStudioManager;
//#endif

/**
 * Screen for viewing all maps and items on map
 * @author Jiri & Menion
 */
public class MapScreen extends FormLocify implements LocationEventListener, Runnable {

    // map actions
    private static final int MA_PAN_UP = 0;
    private static final int MA_PAN_DOWN = 1;
    private static final int MA_PAN_LEFT = 2;
    private static final int MA_PAN_RIGHT = 3;
    private static final int MA_ZOOM_IN = 4;
    private static final int MA_ZOOM_OUT = 5;
    private static final int MA_SWITCH_PROVIDER = 6;
    private static final int MA_SWITCH_MODE = 7;
    private static final int MA_MY_LOCATION = 8;
    private static final int MA_SELECT = 9;

    private Command[] providerCommandsTile;

    private Container mainContainer;
    
    private boolean drawLock;
    private MapLayer map;
    /** map manager for online maps */
    private TileMapLayer mapTile;
    /** map manager for file maps */
    private FileMapLayer mapFile;
    /** flag indikates if map is fixed to actual position */
    boolean centerToActualLocation = true;
    private Location4D lastCenterPoint;
    private Thread moveWhenPressedThread;
    private boolean firstMove = true;
    private int lastAction;
    /* stylus support */
    int pointerX, pointerY;
    /** manager for objects to show on screen */
    private MapItemManager mapItemManager;
    /** downloader for network link */
    private NetworkLinkDownloader networkLinkDownloader;
    /** image displayed while tile is loading */
    private static Image loadingImage;
    /** image for nonexsited tile */
    private static Image imageNotExisted;
    /** image for loading text */
    private static Image imageLoading;
    /** image for connecion problem */
    private static Image imageConnectionNotFound;
    /** if image is too big */
    private static Image imageTooBigSize;
    /** plus icon image */
    private static Image imageIconPlus;
    /** minus icon image */
    private static Image imageIconMinus;
    /** plus icon zoom image */
    private static Image imageIconZoomPlus;
    /** minus icon zoom image */
    private static Image imageIconZoomMinus;
    /** actual position image */
    private static Image imageActualLocation;
    /** size of zoom icon */
    private static int imageIconZoomSideSize;
    /** if some mapItems are selected this isn't null */
    private Vector selectedMapItemWaypoints;
    /** index of selected items */
    private int selectedMapItemIndex;
    /** support for touch screen */
    private int lastSelectedX,  lastSelectedY;
    /* selected item marked as desk with informations */
    public static String tempWaypointDescriptionItemName = "selectedItem";
    /** navigation item is highlited line */
    public static String tempMapNavigationItem = "navigationItem";
    /** route showing for actually recording route */
    public static String tempRunningRouteName = "runningRoute";
    /** time of stylus press */
    private long stylusTought;
    /** should all the files show on map directly? */
    private static boolean nowDirectly = false;
    /** is firstly centere after now directly? */
    private boolean firstCenterAfterND;
    /** show all items during panning */
    private boolean showAllDuringPanning;
    /** different screen that map has lock, so map will not be shown */
    private boolean differentScreenLock;
    /** map table for caches images */
    private static TileCache cache;
    /** if item was added before map was inicialized, call it after that */
    private MapItem newMapItemAdded;

    public MapScreen() {
        super(Locale.get("Maps"));
        
        drawLock = false;
        zoomProcess = false;
        mapItemManager = R.getMapItemManager();

        selectedMapItemWaypoints = new Vector();
        selectedMapItemIndex = -1;
        lastSelectedX = 0;
        lastSelectedY = 0;

        this.addCommand(Commands.cmdBack);
        this.addCommand(Commands.cmdHome);
        //#if planstudio
//#         this.addCommand(cmdPlanStudio);
        //#endif
        this.addCommand(new ParentCommand(Locale.get("Map_function"), null, new Command[]
                {Commands.cmdZoomIn, Commands.cmdZoomOut, Commands.cmdMyLocation}));

        if (R.getBacklight().isOn()) {
            this.addCommand(Commands.cmdBacklightOff);
        } else {
            this.addCommand(Commands.cmdBacklightOn);
        }

        this.addCommand(Commands.cmdItemManager);

        // set map tiles and providers
        mapTile = new TileMapLayer(this);
        mapFile = new FileMapLayer(this);

        if (!R.getSettings().isDefaultMapProviderOnline()) {
            map = mapFile;
        } else {
            mapTile.setProviderAndMode(R.getSettings().getDefaultMapProvider());
            mapTile.setDefaultZoomLevel();
            map = mapTile;
        }

        //mapTile provider commands        
        Vector providers = mapTile.getProvidersAndModes();
        providerCommandsTile = new Command[providers.size()];
        for (int i = 0; i < providers.size(); i++) {
            providerCommandsTile[i] = new Command((String) providers.elementAt(i), i);
        }

        if (providerCommandsTile.length > 0)
            this.addCommand(new ParentCommand(Locale.get("Change_map_tile"), null, providerCommandsTile));

        this.addCommand(Commands.cmdChangeMapFile);

        // i know about added gps ... actualy i'm lazy :)
        this.addCommand(new ParentCommand(Locale.get("Another_location"), null, R.getContext().commands));

        nowDirectly = false;
        firstCenterAfterND = false;
        differentScreenLock = false;
        if (R.getSettings().getPanning() == SettingsData.REPAINT_DURING) {
            showAllDuringPanning = true;
        } else {
            showAllDuringPanning = false;
        }

        setLayout(new BorderLayout());
        mainContainer = new Container() {

            public void paint(Graphics g) {
                try {
//System.out.println("Paint");
                    super.paint(g);
                    if (drawLock) {
                        return;
                    }
                    drawLock = true;

                    if (g.getClipHeight() > 40) {
                        drawMap(g);
                    }
                    drawLock = false;
                } catch (Exception e) {
                    R.getErrorScreen().view(e, "MapScreen.paint()", null);
                }
            }
        };
        addComponent(BorderLayout.CENTER, mainContainer);
    }

    public static TileCache getTileCache() {
        if (cache == null) {
            cache = new TileCache();
            cache.start();
        }
        return cache;
    }

    /**
     * Views map screen
     */
    public void view() {
        try {
            differentScreenLock = false;

            if (map instanceof FileMapLayer && !mapFile.isReady()) {
                R.getMapOfflineChooseScreen().view(R.getLocator().getLastLocation().getLatitude(), R.getLocator().getLastLocation().getLongitude(),
                        R.getLocator().getLastLocation().getLatitude(), R.getLocator().getLastLocation().getLongitude());
            } else {
                if (lastCenterPoint != null) {
                    centerMap(lastCenterPoint, centerToActualLocation);
                } else {
                    centerMap(R.getLocator().getLastLocation(), centerToActualLocation);
                }

                show();
                mapItemManager.init();
//System.out.println("Container: " + getContentPane().getWidth() + " " + getContentPane().getHeight());

                selectNearestWaypointsAtCenter();
                repaint();
                resumeNetworkLink();
            }

            if (R.getSettings().getBacklight() == SettingsData.MAP || R.getSettings().getBacklight() == SettingsData.MAP_NAVIGATION) {
                R.getBacklight().on();
                removeCommand(Commands.cmdBacklightOn);
                addCommand(Commands.cmdBacklightOff);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.view()", null);
        }
    }

    /**
     * Views point with specified atributes
     * @param lat
     * @param lon
     * @param name
     * @param desc
     */
    public void view(double lat, double lon, String name, String desc) {
        Vector waypoints = new Vector();
        waypoints.addElement(new Waypoint(lat, lon, name, desc, null));
        mapItemManager.addItem(name, new PointMapItem(waypoints), MapItem.PRIORITY_MEDIUM);
        centerMap(new Location4D(lat, lon, 0f), false);
        view();
    }

    public void view(GeoData data) {
        if (data.getName().equals("")) {
            return;
        }
        if (data instanceof Waypoint) {
            Waypoint waypoint = (Waypoint) data;
            Vector waypoints = new Vector();
            waypoints.addElement(waypoint);
            mapItemManager.addItem(waypoint.getName(), new PointMapItem(waypoints), MapItem.PRIORITY_MEDIUM);
            if (!nowDirectly || !firstCenterAfterND) {
                Location4D loc = new Location4D(waypoint.getLatitude(), waypoint.getLongitude(), 0);
                //zooming map to point and actual location pair - by destil -- yeah yeah that's goood :)) by menion
                map.calculateZoomFrom(new Location4D[]{loc, R.getLocator().getLastLocation()});
                centerMap(loc, false);
            }
        } else if (data instanceof WaypointsCloud) {
            WaypointsCloud cloud = (WaypointsCloud) data;
            PointMapItem mapItem = new PointMapItem(cloud.getWaypointsCloudPoints());
            //mapItemManager.removeAll();
            mapItemManager.addItem(cloud.getName(), mapItem, MapItem.PRIORITY_MEDIUM);
            if (cloud.getWaypointsCount() != 0 && (!nowDirectly || !firstCenterAfterND)) {
                if (map instanceof FileMapLayer && !mapFile.isReady()) {
                    newMapItemAdded = mapItem;
                } else {
                    centerMap(mapItem.getItemCenter(), false);
                    objectZoomTo(mapItem);
                }
            }
        } else if (data instanceof Route) {
            Route route = (Route) data;
            if (route.getWaypointCount() != 0) {
                RouteMapItem mapItem = new RouteMapItem(route);
                mapItem.setStyles(route.getStyleNormal(), route.getStyleHighLight());
                mapItemManager.addItem(route.getName(), mapItem, MapItem.PRIORITY_MEDIUM);
                if (!nowDirectly || !firstCenterAfterND) {
                    if (map instanceof FileMapLayer && !mapFile.isReady()) {
                        newMapItemAdded = mapItem;
                    } else {
                        centerMap(mapItem.getItemCenter(), false);
                        objectZoomTo(mapItem);
                    }
                }
            }
        }
        if (!differentScreenLock) {
            view();
        }
        //free memory or networklink will have problems
        System.gc();
    }

    /**
     * Views file on the map
     * @param fileName
     */
    public void view(String fileName) {
        MultiGeoData mgd = GeoFiles.parseGeoDataFile(fileName, false);
        view(mgd);
    }

    public void view(MultiGeoData data) {
        if (data.getScreenOverlay() != null) {
            mapItemManager.removeItem("overlay");
            MapItem overlay = new ScreenOverlayMapItem(data.getScreenOverlay());
            overlay.setPriority(MapItem.PRIORITY_LOW);
            overlay.setEnabled(true);
            mapItemManager.addItemFixed("overlay", overlay);
        }
        for (int i = 0; i < data.getDataSize(); i++) {
            view(data.getGeoData(i));
        }
    }

    public void view(NetworkLink link) {
        if (networkLinkDownloader != null) {
            networkLinkDownloader.stop();
        }
        networkLinkDownloader = new NetworkLinkDownloader(link);
        networkLinkDownloader.start();

        differentScreenLock = false;
        view();
        nowDirectly = true;
        firstCenterAfterND = false;
    }

    /**
     * It is called when leaving map with network link
     */
    public void stopNetworkLink() {
        if (networkLinkDownloader != null) {
            nowDirectly = false;
            networkLinkDownloader.stop();
        }
    }

    public static boolean isNowDirectly() {
        return nowDirectly;
    }

    public void setOnlineMaps() {
        mapTile.setProviderAndMode(0);
        mapTile.setDefaultZoomLevel();
        map = mapTile;
        view();
    }

    public void setFileMap(FileMapManager fmm, Location4D center) {
        try {
            if (fmm != null && fmm.isReady()) {
                mapFile.addNextMapManager(fmm, true, true);
                map = mapFile;
                if (newMapItemAdded != null) {
                    centerMap(newMapItemAdded.getItemCenter(), false);
                    objectZoomTo(newMapItemAdded);
                    newMapItemAdded = null;
                } else {
                    centerMap(center, false);
                }
            } else {
                Logger.error("MapScreen.setFileMap() error");
            }
            view();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.SetFileMap()", null);
        }
    }

    public void centerMap(Location4D newCenter, boolean centerToActualLocation) {
        if (centerToActualLocation || !firstCenterAfterND || lastCenterPoint == null || !nowDirectly) {

            if (nowDirectly) {
                firstCenterAfterND = true;
            }
            this.lastCenterPoint = newCenter;
            this.centerToActualLocation = centerToActualLocation;
            this.map.setLocationCenter(lastCenterPoint);

            mapItemManager.disableInitializeState();
        }
    }

    public MapLayer getActualMapLayer() {
        return map;
    }

    public FileMapLayer getFileMapLayer() {
        return mapFile;
    }

//    public void paint(Graphics g) {
//
//    }

    /** 
     * Draws map background (tiles) and other screen components like zoom scale,
     * location pointer etc.
     */
    private void drawMap(Graphics g) {
//        g.setColor(ColorsFonts.LIGHT_ORANGE);
//        g.fillRect(0, 0, Capabilities.getWidth(), getContentPane().getHeight());

        try {
            map.drawMap(g, -1 * panMoveX, -1 * panMoveY);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "map.drawMap()");
        }

        if (R.getLocator().hasValidLocation()) {
            drawActualLocationPoint(g);
        }

        // draw selection circle
        g.setColor(ColorsFonts.BLACK);
        g.drawArc(getContentPane().getWidth() / 2 - 2, getContentPane().getHeight() / 2 - 2, 4, 4, 0, 360);

        try {
            mapItemManager.drawItems(g, MapItem.PRIORITY_HIGH);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapItemManager.drawItems(PRIORITY_HIGH)");
        }

        if (!panProcess || showAllDuringPanning) {
            try {
                mapItemManager.drawItems(g, MapItem.PRIORITY_MEDIUM);
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapItemManager.drawItems(PRIORITY_MEDIUM)");
            }

            try {
                Waypoint wpt;
                for (int i = 0; i < selectedMapItemWaypoints.size(); i++) {
                    if (i != selectedMapItemIndex) {
                        wpt = (Waypoint) selectedMapItemWaypoints.elementAt(i);
                        Point2D.Int temp = map.getLocationCoord(
                                new Location4D(wpt.getLatitude(), wpt.getLongitude(), 0.0f));
                        wpt.paint(g, temp.x, temp.y);
                    }
                }
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.drawMap()", "selectedMapItemWaypoints");
            }

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
                        g.drawImage(getMapIconPlus(), 20 - getMapIconPlus().getWidth() / 2,
                                10 - getMapIconPlus().getHeight() / 2);
                        g.drawImage(getMapIconMinus(), 20 - getMapIconMinus().getWidth() / 2,
                                getContentPane().getHeight() - 10 - getMapIconMinus().getHeight() / 2);
                    }

                    g.setColor(ColorsFonts.BLACK);
                    g.drawLine(moveX + 5, 10, moveX + 5, getContentPane().getHeight() - 10);
                    g.fillRect(moveX, 10, 10, 3);
                    g.fillRect(moveX, getContentPane().getHeight() - 10, 10, 3);
                    double pxPerZoom = (getContentPane().getHeight() - 20.0) /
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

        try {
            if (MainScreen.hasPointerEvents && map.getMaxZoomLevel() - map.getMinZoomLevel() > 0) {
                g.drawImage(getMapIconZoomPlus(), 0, 0);
                g.drawImage(getMapIconZoomMinus(), 0,
                        getContentPane().getHeight() - getMapIconZoomMinus().getHeight());
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapZoomButtons");
        }

        if (!panProcess || showAllDuringPanning) {
            try {
                mapItemManager.drawItems(g, MapItem.PRIORITY_LOW);
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapItemManager.drawItems(PRIORITY_LOW)");
            }
        }
    }

    public MapItemManager getMapItemManager() {
        return mapItemManager;
    }

    private void drawActualLocationPoint(Graphics g) {
        Point2D.Int actPoint = map.getLocationCoord(R.getLocator().getLastLocation());
        float heading = R.getLocator().getHeading();

        if (actPoint != null) {
            g.drawImage(getMapIconActualLocation(), actPoint.x - panMoveX - getMapIconActualLocation().getWidth() / 2,
                    actPoint.y - panMoveY - getMapIconActualLocation().getHeight() / 2);

            if (R.getContext().getSource() == LocationContext.GPS && !R.getLocator().isSimulatedGPS()) {
                int x1 = (int) (20.0 * Math.sin((heading - 30) / LMath.RHO));
                int y1 = (int) (20.0 * Math.cos((heading - 30) / LMath.RHO));
                int x2 = (int) (40.0 * Math.sin((heading) / LMath.RHO));
                int y2 = (int) (40.0 * Math.cos((heading) / LMath.RHO));
                int x3 = (int) (20.0 * Math.sin((heading + 30) / LMath.RHO));
                int y3 = (int) (20.0 * Math.cos((heading + 30) / LMath.RHO));
                int x4 = (int) (25.0 * Math.sin((heading) / LMath.RHO));
                int y4 = (int) (25.0 * Math.cos((heading) / LMath.RHO));
                g.setColor(ColorsFonts.GREEN_SHINY);
                g.fillTriangle(actPoint.x - panMoveX + x1, actPoint.y - panMoveY - y1,
                        actPoint.x - panMoveX + x2, actPoint.y - panMoveY - y2,
                        actPoint.x - panMoveX + x4, actPoint.y - panMoveY - y4);
                g.fillTriangle(actPoint.x - panMoveX + x3, actPoint.y - panMoveY - y3,
                        actPoint.x - panMoveX + x2, actPoint.y - panMoveY - y2,
                        actPoint.x - panMoveX + x4, actPoint.y - panMoveY - y4);
                g.setColor(ColorsFonts.BLACK);
                g.drawLine(actPoint.x - panMoveX + x1, actPoint.y - panMoveY - y1,
                        actPoint.x - panMoveX + x2, actPoint.y - panMoveY - y2);
                g.drawLine(actPoint.x - panMoveX + x2, actPoint.y - panMoveY - y2,
                        actPoint.x - panMoveX + x3, actPoint.y - panMoveY - y3);
                g.drawLine(actPoint.x - panMoveX + x3, actPoint.y - panMoveY - y3,
                        actPoint.x - panMoveX + x4, actPoint.y - panMoveY - y4);
                g.drawLine(actPoint.x - panMoveX + x4, actPoint.y - panMoveY - y4,
                        actPoint.x - panMoveX + x1, actPoint.y - panMoveY - y1);
            }
        }
    }

    /**
     * Create and return bounding box of actual screen.
     * @return Array of two Loaction4D objects.
     * <p>1st is Location4D of Top Left cornet and</p> <p>2nd is the bottom right cornet of actual screen.</p>
     */
    public Location4D[] getBoundingBox() {
        Location4D[] bbox = map.getActualBoundingBox();
        return bbox;
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        try {
            if (centerToActualLocation) {
                boolean fc = firstCenterAfterND;
                centerMap(location, centerToActualLocation);
                firstCenterAfterND = fc;
            }

            if (isMapNavigationRunning()) {
                ((MapNavigationItem) mapItemManager.getItemTemp(tempMapNavigationItem)).actualizeActualPosition(location);
            }
            repaint();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.locationChanged()", null);
        }
    }

    public boolean isMapNavigationRunning() {
        return mapItemManager.existItemTemp(tempMapNavigationItem);
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void message(LocationEventGenerator sender, String message) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void actionCommand(Command cmd) {
//System.out.println("Ev: " + cmd.toString());
        try {
            if (cmd == Commands.cmdBack) {
                //map.stop(); //stops loading tiles
                stopNetworkLink();
                selectNearestWaypoints(0, 0, 0, true); // deselect object selection
                if (R.getContext().isTemporary()) {
                    R.getContext().removeTemporaryLocation();
                }
                R.getBack().goBack();
                if (R.getSettings().getBacklight() != SettingsData.WHOLE_APPLICATION) {
                    R.getBacklight().off();
                }
            } else if (cmd == Commands.cmdHome) {
                //map.stop(); //stops loading tiles
                stopNetworkLink();
                selectNearestWaypoints(0, 0, 0, true); // deselect object selection
                if (R.getContext().isTemporary()) {
                    R.getContext().removeTemporaryLocation();
                }
                R.getURL().call("locify://mainScreen");
                if (R.getSettings().getBacklight() != SettingsData.WHOLE_APPLICATION) {
                    R.getBacklight().off();
                }
            } else if (cmd == Commands.cmdChangeMapFile) {
                Location4D[] locs = getBoundingBox();
                if (locs != null) {
                    R.getMapOfflineChooseScreen().view(locs[0].getLatitude(),
                            locs[0].getLongitude(), locs[1].getLatitude(), locs[1].getLongitude());
                }
            //#if planstudio
//#             } else if (cmd == cmdPlanStudio) {
//#                 R.getPlanstudio().view();
            //#endif
            } else if (cmd == Commands.cmdZoomIn) {
                makeMapAction(MA_ZOOM_IN, null);
            } else if (cmd == Commands.cmdZoomOut) {
                makeMapAction(MA_ZOOM_OUT, null);
            } else if (cmd == Commands.cmdMyLocation) {
                makeMapAction(MA_MY_LOCATION, null);
            } else if (cmd == Commands.cmdItemManager) {
                mapItemManager.viewMapSettings();
            } else if (cmd == Commands.cmdBacklightOn) {
                R.getBacklight().on();
                removeCommand(Commands.cmdBacklightOn);
                addCommand(Commands.cmdBacklightOff);
            } else if (cmd == Commands.cmdBacklightOff) {
                R.getBacklight().off();
                removeCommand(Commands.cmdBacklightOff);
                addCommand(Commands.cmdBacklightOn);
            } else {
                for (int i = 0; i < providerCommandsTile.length; i++) {
                    if (providerCommandsTile[i] == cmd) {
                        mapTile.setProviderAndMode(i);
                        map = mapTile;
                        if (centerToActualLocation) {
                            centerMap(lastCenterPoint, centerToActualLocation);
                        }
                        repaint();
                        return;
                    }
                }

                for (int i = 0; i < R.getContext().commands.length; i++) {
                    if (cmd == R.getContext().commands[i]) {
                        lastCenterPoint = null;
                        if (networkLinkDownloader != null) {
                            networkLinkDownloader.stop();
                        }
                        R.getContext().setTemporaryScreen("locify://maps");
                        R.getURL().call(R.getContext().actions[i]);
                        return;
                    }
                }

                this.view();
            }
        } catch (Exception e) {
            Logger.error("MapScreen.actionCommand key: " + cmd.toString() + " Ex: " + e.toString());
        }
    }

    public void keyPressed(int key) {
        try {
            if (key < 0) {
                switch (Display.getInstance().getGameAction(key)) {
                    case Display.GAME_DOWN:
                        makeMapAction(MA_PAN_DOWN, null);
                        break;
                    case Display.GAME_UP:
                        makeMapAction(MA_PAN_UP, null);
                        break;
                    case Display.GAME_LEFT:
                        makeMapAction(MA_PAN_LEFT, null);
                        break;
                    case Display.GAME_RIGHT:
                        makeMapAction(MA_PAN_RIGHT, null);
                        break;
                    case Display.GAME_FIRE:
                        if (mapItemManager.getItemTemp(tempWaypointDescriptionItemName) != null) {
                            makeSelectionActionFire();
                        } else {
                            makeMapAction(MA_SELECT, null);
                        }
                        break;
                }
            } else {
                switch (key) {
                    case GameCanvas.KEY_STAR:
                        makeMapAction(MA_SWITCH_PROVIDER, null);
                        break;
                    case GameCanvas.KEY_POUND:
                        makeMapAction(MA_SWITCH_MODE, null);
                        break;
                    case GameCanvas.KEY_NUM1: //zoom out
                        makeMapAction(MA_ZOOM_OUT, null);
                        break;
                    case GameCanvas.KEY_NUM3: //zoom in
                        makeMapAction(MA_ZOOM_IN, null);
                        break;
                    case GameCanvas.KEY_NUM2:
                        makeMapAction(MA_PAN_UP, null);
                        break;
                    case GameCanvas.KEY_NUM4:
                        makeMapAction(MA_PAN_LEFT, null);
                        break;
                    case GameCanvas.KEY_NUM6:
                        makeMapAction(MA_PAN_RIGHT, null);
                        break;
                    case GameCanvas.KEY_NUM8:
                        makeMapAction(MA_PAN_DOWN, null);
                        break;
                    case GameCanvas.KEY_NUM0:
                        makeMapAction(MA_MY_LOCATION, null);
                        break;
                    case GameCanvas.KEY_NUM9:
                        actionCommand(Commands.cmdChangeMapFile);
                        break;
                    case GameCanvas.KEY_NUM7:
                        if (R.getBacklight().isOn()) {
                            actionCommand(Commands.cmdBacklightOff);
                        } else {
                            actionCommand(Commands.cmdBacklightOn);
                        }
                        break;
                    case GameCanvas.KEY_NUM5:
                        actionCommand(Commands.cmdItemManager);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            Logger.error("MapScreen.keyPressed() key: " + String.valueOf(key) + " Ex: " + e.toString());
        //R.getErrorScreen().view(e, "MapScreen.keyPressed()", String.valueOf(key));
        }
    }

    public void keyReleased(int keyCode) {
        try {
            super.keyReleased(keyCode);
            moveWhenPressedThread = null;
            firstMove = true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.keyReleased()", String.valueOf(keyCode));
        }
    }

    public void longKeyPress(int keyCode) {
        System.out.println("Long key: " + keyCode);
    }

    /** operation with map. Allowed actions:
     * <ul>
     * <li>"pan-up" scroll map up</li> 
     * <li>"pan-down"</li>
     * <li>"pan-left"</li>
     * <li>"pan-right"</li>
     * <li>"zoom-in"</li>
     * <li>"zoom-out"</li>
     * <li>"switch-provider"</li>
     * <li>"switch-mode"</li>
     * <li>"switch-tracking-mode" changes if map is cetered to actual position or not</li>
     * <li>"select"</li>
     * </ul>
     * 
     * 
     * @param action action type
     * @param arg optional action typu
     */
    public void makeMapAction(int action, Object arg) {
        try {
            lastAction = action;
            switch (action) {
                case MA_PAN_UP:
                    actionPanUp();
                    if (!mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_PAN_DOWN:
                    actionPanDown();
                    if (!mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_PAN_LEFT:
                    actionPanLeft();
                    if (!mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_PAN_RIGHT:
                    actionPanRight();
                    if (!mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_ZOOM_IN:
                    if (!mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                        makeZoomAction(ZOOM_IN, 0, 0);
                        selectNearestWaypointsAtCenter();
                    }
                    break;
                case MA_ZOOM_OUT:
                    if (!mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                        makeZoomAction(ZOOM_OUT, 0, 0);
                        selectNearestWaypointsAtCenter();
                    }
                    break;
                case MA_SWITCH_PROVIDER:
                    map.nextProvider();
                    break;
                case MA_SWITCH_MODE:
                    map.nextMode();
                    break;
                case MA_MY_LOCATION:
                    if (!mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                        if (zoomProcess) {
                            makeZoomAction(ZOOM_PAN, -1 * pxMoveX, -1 * pxMoveY);
                            makeZoomAction(ZOOM_PAN, -1 * (pxMoveX - Capabilities.getWidth() / 2), -1 * (pxMoveY - getContentPane().getHeight() / 2));
                        } else {
                            centerMap(R.getLocator().getLastLocation(), true);
                        }
                    }
                    break;
                case MA_SELECT:
                    selectNextFromSelected(false);
                    break;
                default:
                    return;
            }
            repaint();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.makeMapAction()", "action type: " + action);
        }
    }

    public String createCssSelector() {
        return "";
    }

    /**
     * Thread for automatic calling of action when button is pressed
     */
    public void run() {
        try {
            while (moveWhenPressedThread != null) {
                Thread.sleep(200);
                if (firstMove) {
                    firstMove = false;
                } else {
                    if (lastAction == MA_PAN_UP) {
                        actionPanUp();
                    } else if (lastAction == MA_PAN_DOWN) {
                        actionPanDown();
                    } else if (lastAction == MA_PAN_LEFT) {
                        actionPanLeft();
                    } else if (lastAction == MA_PAN_RIGHT) {
                        actionPanRight();
                    }
                    repaint();
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.run()", "lastAction: " + lastAction);
        }
    }

    private void actionPanUp() {
        if (zoomProcess) {
            makeZoomAction(ZOOM_UP, 0, 0);
        } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
            DescriptionMapItem item = (DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName);
            if (item != null) {
                item.selectNext();
            }
        } else {
            if (showAllDuringPanning) {
                centerToActualLocation = false;
                map.panUp();
                mapItemManager.panItem(0, 1 * map.PAN_PIXELS);
                selectNearestWaypointsAtCenter();
            } else {
                makePanAction(0, -1 * map.PAN_PIXELS);
            }
        }
    }

    private void actionPanDown() {
        if (zoomProcess) {
            makeZoomAction(ZOOM_DOWN, 0, 0);
        } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
            DescriptionMapItem item = (DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName);
            if (item != null) {
                item.selectPrev();
            }
        } else {
            if (showAllDuringPanning) {
                centerToActualLocation = false;
                map.panDown();
                mapItemManager.panItem(0, -1 * map.PAN_PIXELS);
                selectNearestWaypointsAtCenter();
            } else {
                makePanAction(0, map.PAN_PIXELS);
            }
        }
    }

    private void actionPanLeft() {
        if (zoomProcess) {
            makeZoomAction(ZOOM_LEFT, 0, 0);
        } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
            selectNextFromSelected(true);
        } else {
            if (showAllDuringPanning) {
                centerToActualLocation = false;
                map.panLeft();
                mapItemManager.panItem(1 * map.PAN_PIXELS, 0);
                selectNearestWaypointsAtCenter();
            } else {
                makePanAction(-1 * map.PAN_PIXELS, 0);
            }
        }
    }

    private void actionPanRight() {
        if (zoomProcess) {
            makeZoomAction(ZOOM_RIGHT, 0, 0);
        } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
            selectNextFromSelected(false);
        } else {
            if (showAllDuringPanning) {
                centerToActualLocation = false;
                map.panRight();
                mapItemManager.panItem(-1 * map.PAN_PIXELS, 0);
                selectNearestWaypointsAtCenter();
            } else {
                makePanAction(map.PAN_PIXELS, 0);
            }
        }
    }

    public boolean isOffLineMapEnable() {
        return map instanceof FileMapLayer;
    }

    /************************************************/
    /*           STYLUS SUPPORT SECTION             */
    /************************************************/
    /**
     * Called when the pointer is dragged.
     */
    public void pointerDragged(int x, int y) {
//System.out.println("Dragged: " + x + " " + y);
        if (mainContainer.contains(x, y)) {
            try {
                if (zoomProcess) {
                    makeZoomAction(ZOOM_PAN, -1 * (pointerX - x), -1 * (pointerY - y));
                } else if ((pointerX - x) != 0 || (pointerY - y) != 0) {
                    if (showAllDuringPanning) {
                        map.pan(pointerX - x, pointerY - y);
                        centerToActualLocation = false;
                        mapItemManager.panItem(x - pointerX, y - pointerY);
                    } else {
                        makePanAction(pointerX - x, pointerY - y);
                    }
                } else {
                    return;
                }
                repaint();
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.pointerDragged()", null);
            }
        } else {
            super.pointerDragged(x, y);
        }
        pointerX = x;
        pointerY = y;
    }

    /**
     * Called when the pointer is pressed.
     * @param x
     * @param y
     */
    public void pointerPressed(int x, int y) {
//System.out.println("Pressed: " + x + " " + y + " " + stylusTought);
        if (mainContainer.contains(x, y) && stylusTought == 0) {
            stylusTought = System.currentTimeMillis();
            pointerX = x;
            pointerY = y;
        } else {
            super.pointerPressed(x, y);
        }
    }

    /**
     * Called when the pointer is released.
     */
    public void pointerReleased(int x, int y) {
//System.out.println("Released: " + x + " " + y);
        if (mainContainer.contains(x, y)) {
            if ((System.currentTimeMillis() - stylusTought) < 250) {
                if (x < imageIconZoomSideSize && y > 0 && y < 0 + imageIconZoomSideSize &&
                        y < -1 * x + imageIconZoomSideSize) {
                    makeMapAction(MA_ZOOM_IN, null);
                } else if (x < imageIconZoomSideSize &&
                        y > (getContentPane().getHeight() - imageIconZoomSideSize) && y < getContentPane().getHeight() &&
                        y > x + (getContentPane().getHeight() - imageIconZoomSideSize)) {
                    makeMapAction(MA_ZOOM_OUT, null);
                } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
                    if (Math.sqrt((lastSelectedX - x) * (lastSelectedX - x) + (lastSelectedY - y) *
                            (lastSelectedY - y)) < (map.PAN_PIXELS * 1.0)) {
                        selectNextFromSelected(false);
                    } else {
                        ((DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName)).selectButtonAt(x, y);
                        makeSelectionActionFire();
                    }
                } else {
                    selectNearestWaypoints(x, y, map.PAN_PIXELS * 2 / 3, false);
                    selectNextFromSelected(false);
                }
            }
        } else {
            super.pointerReleased(x, y);
        }
        stylusTought = 0;
        repaint();
    }

    /******************************************/
    /*           MAP ITEM SECTION             */
    /******************************************/
    public void objectZoomTo(MapItem item) {
        if (item != null) {
            map.calculateZoomFrom(item.getBoundingLocations());
            mapItemManager.disableInitializeState();
        }
    }

    public void showActualRoute(RouteVariables routeVariables) {
        if (routeVariables.getPointsCount() > 0) {
            if (!mapItemManager.existItemTemp(tempRunningRouteName)) {
                mapItemManager.addItemTemp(tempRunningRouteName, new RouteMapItem(
                        routeVariables.getRoutePoints()), MapItem.PRIORITY_MEDIUM);
            } else {
                RouteMapItem item = (RouteMapItem) mapItemManager.getItemTemp(tempRunningRouteName);
                item.setVectorLocation4D(routeVariables.getRoutePoints());
            }
        }
    }

    /****************************************************/
    /*           IMAGE MANIPULATION SECTION             */
    /****************************************************/
    public static Image getImageNotExisted() {
        try {
            if (imageNotExisted == null) {
                imageNotExisted = getImageVarious(Locale.get("File_map_tile_not_exist"));
            }
            return imageNotExisted;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageNotExisted()", null);
            return null;
        }
    }

    public static Image getImageLoading() {
        try {
            if (imageLoading == null) {
                imageLoading = getImageVarious(Locale.get("File_map_tile_loading"));
            }
            return imageLoading;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageLoading()", null);
            return null;
        }
    }

    public static Image getImageConnectionNotFound() {
        try {
            if (imageConnectionNotFound == null) {
                imageConnectionNotFound = getImageVarious(Locale.get("Connection_failed"));
            }
            return imageConnectionNotFound;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageLoading()", null);
            return null;
        }
    }

    public static Image getImageTooBigSize() {
        try {
            if (imageTooBigSize == null) {
                imageTooBigSize = getImageVarious(Locale.get("Image_too_big"));
            }
            return imageTooBigSize;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageTooBigSize()", null);
            return null;
        }
    }

    private static final Image getLoadingImage() {
        if (loadingImage == null) {
            loadingImage = IconData.getLocalImage("map_tile_64x64.png");
        }
        return loadingImage;
    }

    private static Image getImageVarious(String tileText) {
        Image image = Image.createImage(64, 64);
        Graphics g = image.getGraphics();

        g.drawImage(getLoadingImage(), 0, 0);
        g.setFont(ColorsFonts.FONT_BMF_14);
        g.drawString(tileText, (64 - ColorsFonts.FONT_BMF_14.stringWidth(tileText)) / 2, (64 - 10) / 2);

        return image;
    }

    private Image getMapIconPlus() {
        if (imageIconPlus == null) {
            imageIconPlus = IconData.getLocalImage("map_icon_plus.png");
        }
        return imageIconPlus;
    }

    private Image getMapIconMinus() {
        if (imageIconMinus == null) {
            imageIconMinus = IconData.getLocalImage("map_icon_minus.png");
        }
        return imageIconMinus;
    }

    private Image getMapIconZoomPlus() {
        if (imageIconZoomPlus == null) {
            imageIconZoomPlus = IconData.getLocalImage("map_icon_zoom_plus.png");

            if (imageIconZoomSideSize == 0) {
                int size = getContentPane().getHeight() * imageIconZoomPlus.getHeight() / 1000;
                imageIconZoomSideSize = size < 35 ? 35 : size;
            }

            imageIconZoomPlus = imageIconZoomPlus.scaled(imageIconZoomSideSize, imageIconZoomSideSize);
        }
        return imageIconZoomPlus;
    }

    private Image getMapIconZoomMinus() {
        if (imageIconZoomMinus == null) {
            imageIconZoomMinus = IconData.getLocalImage("map_icon_zoom_minus.png");

            if (imageIconZoomSideSize == 0) {
                int size = getContentPane().getHeight() * imageIconZoomPlus.getHeight() / 1000;
                imageIconZoomSideSize = size < 35 ? 35 : size;
            }

            imageIconZoomMinus = imageIconZoomMinus.scaled(imageIconZoomSideSize, imageIconZoomSideSize);
        }
        return imageIconZoomMinus;
    }

    private Image getMapIconActualLocation() {
        if (imageActualLocation == null) {
            imageActualLocation = IconData.getLocalImage("map_icon_actualLoc.png");
        }
        return imageActualLocation;
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

    private void makePanAction(int panX, int panY) {
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
                centerToActualLocation = false;
                mapItemManager.panItem(-1 * panMoveX, -1 * panMoveY);
                selectNearestWaypointsAtCenter();
                panMoveX = 0;
                panMoveY = 0;
                repaint();
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.PanThread.run()", null);
            }
        }
    }
    
    /*********************************************/
    /*           ZOOM ACTION SECTION             */
    /*********************************************/
    private static final int ZOOM_IN = 0;
    private static final int ZOOM_OUT = 1;
    private static final int ZOOM_LEFT = 2;
    private static final int ZOOM_RIGHT = 3;
    private static final int ZOOM_UP = 4;
    private static final int ZOOM_DOWN = 5;
    private static final int ZOOM_PAN = 6;
    /** thread for zooming */
    private ZoomThread zoomThread;
    /** boolean if zoom managing is active */
    private boolean zoomProcess = false;
    private int zoomTotalValue;
    private long zoomLastAction;
    private int borderWdef,  borderHdef;
    private int borderW,  borderH;
    private int pxMoveX;
    private int pxMoveY;
    private int movePartImageValue = 3;

    private void makeZoomAction(int actionType, int panX, int panY) {
        // disable zoom for maps without zoom
        if (map.getMaxZoomLevel() - map.getMinZoomLevel() == 0) {
            return;
        }

        if (!zoomProcess) {
            zoomThread = new ZoomThread();
            zoomThread.start();
            zoomProcess = true;

            zoomTotalValue = 0;

            borderW = borderWdef = Capabilities.getWidth();
            borderH = borderHdef = getContentPane().getHeight();
            pxMoveX = Capabilities.getWidth() / 2;
            pxMoveY = getContentPane().getHeight() / 2;
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
                if ((pxMoveX - Capabilities.getWidth() / 2) != 0 || (pxMoveY - getContentPane().getHeight() / 2) != 0) {
                    map.pan(pxMoveX - Capabilities.getWidth() / 2, pxMoveY - getContentPane().getHeight() / 2);
                }
                if (zoomTotalValue != 0) {
                    map.setZoomLevel(map.getActualZoomLevel() + zoomTotalValue);
                }
                mapItemManager.disableInitializeState();
                selectNearestWaypointsAtCenter();
                repaint();
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.ZoomThread.run()", null);
            }
        }
    }

    /**************************************************/
    /*           SELECTION ACTION SECTION             */
    /**************************************************/
    private void selectNearestWaypointsAtCenter() {
        selectNearestWaypoints(Capabilities.getWidth() / 2, getContentPane().getHeight() / 2, map.PAN_PIXELS * 2 / 3, false);
    }

    private void selectNearestWaypoints(int x, int y, int radius, boolean deleteDescription) {
        lastSelectedX = x;
        lastSelectedY = y;
        selectedMapItemWaypoints = mapItemManager.getWaypointsAtPosition(x, y, radius * radius);
        selectedMapItemIndex = -1;
        if (deleteDescription) {
            mapItemManager.removeItemTemp(tempWaypointDescriptionItemName);
        }
    }

    private void selectNextFromSelected(boolean inverse) {
        if (selectedMapItemWaypoints.size() != 0) {
            if (selectedMapItemIndex != -1) {
                ((Waypoint) selectedMapItemWaypoints.elementAt(selectedMapItemIndex)).state = Waypoint.STATE_HIGHLIGHT;
            }
            if (!inverse) {
                selectedMapItemIndex++;
                if (selectedMapItemIndex > selectedMapItemWaypoints.size() - 1) {
                    selectedMapItemIndex = 0;
                }
            } else {
                selectedMapItemIndex--;
                if (selectedMapItemIndex < 0) {
                    selectedMapItemIndex = selectedMapItemWaypoints.size() - 1;
                }
            }
            ((Waypoint) selectedMapItemWaypoints.elementAt(selectedMapItemIndex)).state = Waypoint.STATE_SELECTED;
            mapItemManager.addItemTemp(tempWaypointDescriptionItemName,
                    new DescriptionMapItem((Waypoint) selectedMapItemWaypoints.elementAt(selectedMapItemIndex)),
                    MapItem.PRIORITY_LOW);
        }
    }

    private void makeSelectionActionFire() {
        DescriptionMapItem item = (DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName);
        if (item != null) {
            if (item.getSelectedType() == -1) {
                return;
            }

            switch (item.getSelectedType()) {
                case DescriptionMapItem.BUTTON_NAVIGATE:
                    mapItemManager.removeItemTemp(tempWaypointDescriptionItemName);
                    differentScreenLock = true;
                    R.getNavigationScreen().updateWaypoint(item.getSelectedWaypoint());
                    R.getURL().call("locify://navigation");
                    break;
                case DescriptionMapItem.BUTTON_NAVIGATE_ON_MAP:
                    startMapNavigation(item.getSelectedWaypoint());
                    break;
                case DescriptionMapItem.BUTTON_CLOSE:
                    mapItemManager.removeItemTemp(tempWaypointDescriptionItemName);
                    ((Waypoint) selectedMapItemWaypoints.elementAt(selectedMapItemIndex)).state = Waypoint.STATE_HIGHLIGHT;
                    selectedMapItemIndex = -1;
                    repaint();
                    break;
            }
        }
    }

    public void startMapNavigation(Waypoint waypoint) {
        differentScreenLock = false;
        mapItemManager.addItemTemp(tempMapNavigationItem,
                new MapNavigationItem(
                new Waypoint(R.getLocator().getLastLocation().getLatitude(),
                R.getLocator().getLastLocation().getLongitude(), " ", " ", null),
                waypoint), MapItem.PRIORITY_MEDIUM);
        mapItemManager.removeItemTemp(tempWaypointDescriptionItemName);
        selectNearestWaypointsAtCenter();
        view();
    }

    public void resumeNetworkLink() {
        if (networkLinkDownloader != null && networkLinkDownloader.isStopped()) {
            networkLinkDownloader.resume();
        }
    }

    public void setDifferentScreenLock(boolean differentScreenLock) {
        this.differentScreenLock = differentScreenLock;
    }

    public boolean getDifferentScreenLock() {
        return this.differentScreenLock;
    }
}
