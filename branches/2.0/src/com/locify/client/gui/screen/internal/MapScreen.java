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

import com.locify.client.data.SettingsData;
import com.locify.client.data.items.GeoData;
import com.locify.client.data.items.GeoFiles;
import com.locify.client.data.items.MultiGeoData;
import com.locify.client.data.items.NetworkLink;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.maps.MapContent;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.maps.FileMapLayer;
import com.locify.client.maps.MapImages;
import com.locify.client.maps.NetworkLinkDownloader;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.maps.mapItem.DescriptionMapItem;
import com.locify.client.maps.mapItem.MapItem;
import com.locify.client.maps.mapItem.MapNavigationItem;
import com.locify.client.maps.mapItem.PointMapItem;
import com.locify.client.maps.mapItem.RouteMapItem;
import com.locify.client.maps.mapItem.ScreenOverlayMapItem;
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.layouts.BorderLayout;
import java.util.Vector;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author menion
 */
public class MapScreen extends FormLocify implements Runnable, LocationEventListener {

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
    
    /** main map widget */
    private MapContent mapContent;
    /** commands for TileMap */
    private Command[] providerCommandsTile;
    /** main map container */
    private Container mainContainer;
    /** downloader for network link */
    private NetworkLinkDownloader networkLinkDownloader;
    /** screen lock for drawing */
    private boolean drawLock;
    /* selected item marked as desk with informations */
    public static String tempWaypointDescriptionItemName = "selectedItem";
    /** navigation item is highlited line */
    public static String tempMapNavigationItem = "navigationItem";
    /** route showing for actually recording route */
    public static String tempRunningRouteName = "runningRoute";
    /** if some mapItems are selected this isn't null */
    private Vector selectedMapItemWaypoints;
    /** index of selected items */
    private int selectedMapItemIndex;
    /** support for touch screen */
    private int lastSelectedX,  lastSelectedY;
    /** thread for panning */
    private Thread moveWhenPressedThread;

    /** should all the files show on map directly? */
    private static boolean nowDirectly = false;
    /** is firstly centere after now directly? */
    private boolean firstCenterAfterND;
    /** different screen that map has lock, so map will not be shown */
    private boolean differentScreenLock;
    /** last action key */
    private int lastAction;
    /* stylus support */
    int pointerX, pointerY;
    /** time of stylus press */
    private long stylusTought;
    /** first move from keyboard */
    private boolean firstMove;
    /** set new center after reapint */
    private boolean setNewCenterAfterRepaint;
    
    public MapScreen() {
        super(Locale.get("Maps"));

        mapContent = R.getMapContent();
        
        drawLock = false;
        firstMove = true;
        setNewCenterAfterRepaint = false;

        selectedMapItemWaypoints = new Vector();
        selectedMapItemIndex = -1;
        lastSelectedX = 0;
        lastSelectedY = 0;

        this.addCommand(Commands.cmdBack);
        this.addCommand(Commands.cmdHome);
        this.addCommand(new ParentCommand(Locale.get("Map_function"), null, new Command[]
                {Commands.cmdZoomIn, Commands.cmdZoomOut, Commands.cmdMyLocation}));

        if (R.getBacklight().isOn()) {
            this.addCommand(Commands.cmdBacklightOff);
        } else {
            this.addCommand(Commands.cmdBacklightOn);
        }

        this.addCommand(Commands.cmdItemManager);

        //mapTile provider commands
        Vector providers = mapContent.getTileMapLayer().getProvidersAndModes();
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

        setLayout(new BorderLayout());
        mainContainer = new Container() {

            public void paint(Graphics g) {
                try {
//System.out.println("Paint " + drawLock + " " + setNewCenterAfterRepaint);
                    super.paint(g);
                    if (drawLock) {
                        return;
                    }
                    drawLock = true;

                    drawMap(g);

                    if (setNewCenterAfterRepaint) {
                        mapContent.setLastCenterPoint(mapContent.getActualMapLayer().getLocationCoord(
                                mainContainer.getWidth() / 2, mainContainer.getHeight() / 2));
                        setNewCenterAfterRepaint = false;
                    }
                    
                    drawLock = false;
                } catch (Exception e) {
                    R.getErrorScreen().view(e, "MapScreen.mainContainer.paint()", null);
                }
            }
        };
        addComponent(BorderLayout.CENTER, mainContainer);
        R.getLocator().addLocationChangeListener(this);
    }

    /**
     * Views map screen
     */
    public void view() {
        try {
            differentScreenLock = false;

            if (mapContent.getActualMapLayer() instanceof FileMapLayer && !mapContent.getFileMapLayer().isReady()) {
                R.getMapOfflineChooseScreen().view(R.getLocator().getLastLocation().getLatitude(), R.getLocator().getLastLocation().getLongitude(),
                        R.getLocator().getLastLocation().getLatitude(), R.getLocator().getLastLocation().getLongitude());
            } else {
                if (mapContent.getLastCenterPoint() != null) {
                    centerMap(mapContent.getLastCenterPoint(), mapContent.isCenterToActualLocation());
                } else {
                    centerMap(R.getLocator().getLastLocation(), mapContent.isCenterToActualLocation());
                }

                mapContent.registerParent(this);
                
                show();
//System.out.println("Container: " + mainContainer.getWidth() + " " + mainContainer.getHeight());

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
        mapContent.getMapItemManager().addItem(name, new PointMapItem(waypoints), MapItem.PRIORITY_MEDIUM);
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
            mapContent.getMapItemManager().addItem(waypoint.getName(), new PointMapItem(waypoints), MapItem.PRIORITY_MEDIUM);
            if (!nowDirectly || !firstCenterAfterND) {
                Location4D loc = new Location4D(waypoint.getLatitude(), waypoint.getLongitude(), 0);
                //zooming map to point and actual location pair - by destil -- yeah yeah that's goood :)) by menion
                mapContent.getActualMapLayer().calculateZoomFrom(new Location4D[]{loc, R.getLocator().getLastLocation()});
                centerMap(loc, false);
            }
        } else if (data instanceof WaypointsCloud) {
            WaypointsCloud cloud = (WaypointsCloud) data;
            PointMapItem mapItem = new PointMapItem(cloud.getWaypointsCloudPoints());
            //mapItemManager.removeAll();
            mapContent.getMapItemManager().addItem(cloud.getName(), mapItem, MapItem.PRIORITY_MEDIUM);
            if (cloud.getWaypointsCount() != 0 && (!nowDirectly || !firstCenterAfterND)) {
                if (mapContent.getActualMapLayer() instanceof FileMapLayer && !mapContent.getFileMapLayer().isReady()) {
                    mapContent.setNewMapItem(mapItem);
                } else {
                    centerMap(mapItem.getItemCenter(), false);
                    objectZoomTo(mapItem);
                }
            }
        } else if (data instanceof Route) {
            Route route = (Route) data;
//System.out.println("AddRoute: " + route.getDescription() + ", points: " + route.getWaypointCount());
            if (route.getWaypointCount() != 0) {
                RouteMapItem mapItem = new RouteMapItem(route);
                mapItem.setStyles(route.getStyleNormal(), route.getStyleHighLight());
                mapContent.getMapItemManager().addItem(route.getName(), mapItem, MapItem.PRIORITY_MEDIUM);
                if (!nowDirectly || !firstCenterAfterND) {
                    if (mapContent.getActualMapLayer() instanceof FileMapLayer && !mapContent.getFileMapLayer().isReady()) {
                        mapContent.setNewMapItem(mapItem);
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
            mapContent.getMapItemManager().removeItem("overlay");
            MapItem overlay = new ScreenOverlayMapItem(data.getScreenOverlay());
            overlay.setPriority(MapItem.PRIORITY_LOW);
            overlay.setEnabled(true);
            mapContent.getMapItemManager().addItemFixed("overlay", overlay);
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

    public void centerMap(Location4D newCenter, boolean centerToActualLocation) {
        if (mapContent.isCenterToActualLocation() || !firstCenterAfterND ||
                mapContent.getLastCenterPoint() == null || !nowDirectly) {

            if (nowDirectly) {
                firstCenterAfterND = true;
            }

            mapContent.centerMap(newCenter, centerToActualLocation);
        }
    }
    
    /**
     * Draws map background (tiles) and other screen components like zoom scale,
     * location pointer etc.
     */
    private void drawMap(Graphics g) {
//Utils.printMemoryState("Maps - draw");
        mapContent.drawMap(g);
        mapContent.drawActualLocationPoint(g);
        mapContent.drawSelectionCircle(g);
        mapContent.drawMapItem(g, MapItem.PRIORITY_HIGH);

        if (!mapContent.isPanProcess() || mapContent.isPaintDuringPanning()) {
            mapContent.drawMapItem(g, MapItem.PRIORITY_MEDIUM);

            try {
                Waypoint wpt;
                for (int i = 0; i < selectedMapItemWaypoints.size(); i++) {
                    if (i != selectedMapItemIndex) {
                        wpt = (Waypoint) selectedMapItemWaypoints.elementAt(i);
                        Point2D.Int temp = mapContent.getActualMapLayer().getLocationCoord(
                                new Location4D(wpt.getLatitude(), wpt.getLongitude(), 0.0f));
                        wpt.paint(g, temp.x, temp.y);
                    }
                }
            } catch (Exception e) {
                R.getErrorScreen().view(e, "MapScreen.drawMap()", "selectedMapItemWaypoints");
            }

            mapContent.drawZoomProcess(g);
        }

        try {
            if (MainScreen.hasPointerEvents && mapContent.getActualMapLayer().getMaxZoomLevel() -
                    mapContent.getActualMapLayer().getMinZoomLevel() > 0) {
                g.drawImage(MapImages.getMapIconZoomPlus(mainContainer.getHeight()), 0, 0);
                g.drawImage(MapImages.getMapIconZoomMinus(mainContainer.getHeight()), 0,
                        mainContainer.getHeight() - MapImages.getMapIconZoomMinus(mainContainer.getHeight()).getHeight());
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapZoomButtons");
        }

        if (!mapContent.isPanProcess() || mapContent.isPaintDuringPanning()) {
            mapContent.drawMapItem(g, MapItem.PRIORITY_LOW);
        }
    }

    /**
     * Create and return bounding box of actual screen.
     * @return Array of two Loaction4D objects.
     * <p>1st is Location4D of Top Left cornet and</p> <p>2nd is the bottom right cornet of actual screen.</p>
     */
    public Location4D[] getBoundingBox() {
        Location4D[] bbox = mapContent.getActualMapLayer().getActualBoundingBox();
        return bbox;
    }

    public boolean isMapNavigationRunning() {
        return mapContent.getMapItemManager().existItemTemp(tempMapNavigationItem);
    }
    
    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        try {
            if (mapContent.isCenterToActualLocation()) {
                boolean fc = firstCenterAfterND;
                firstCenterAfterND = fc;
            }

            if (isMapNavigationRunning()) {
                ((MapNavigationItem) mapContent.getMapItemManager().getItemTemp(tempMapNavigationItem)).actualizeActualPosition(location);
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
            } else if (cmd == Commands.cmdZoomIn) {
                makeMapAction(MA_ZOOM_IN, null);
            } else if (cmd == Commands.cmdZoomOut) {
                makeMapAction(MA_ZOOM_OUT, null);
            } else if (cmd == Commands.cmdMyLocation) {
                makeMapAction(MA_MY_LOCATION, null);
            } else if (cmd == Commands.cmdItemManager) {
                mapContent.getMapItemManager().viewMapSettings();
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
                        mapContent.getTileMapLayer().setProviderAndMode(i);
                        mapContent.setMapLayer(mapContent.getTileMapLayer());
                        if (mapContent.isCenterToActualLocation()) {
                            mapContent.centerMap(mapContent.getLastCenterPoint(),
                                    mapContent.isCenterToActualLocation());
                        }
                        repaint();
                        return;
                    }
                }

                for (int i = 0; i < R.getContext().commands.length; i++) {
                    if (cmd == R.getContext().commands[i]) {
//                        lastCenterPoint = null;
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
                        if (mapContent.getMapItemManager().getItemTemp(tempWaypointDescriptionItemName) != null) {
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

//    public void longKeyPress(int keyCode) {
//        System.out.println("Long key: " + keyCode);
//    }

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
                    if (!mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_PAN_DOWN:
                    actionPanDown();
                    if (!mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_PAN_LEFT:
                    actionPanLeft();
                    if (!mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_PAN_RIGHT:
                    actionPanRight();
                    if (!mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                        moveWhenPressedThread = new Thread(this);
                        moveWhenPressedThread.start();
                    }
                    break;
                case MA_ZOOM_IN:
                    if (!mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                        mapContent.makeZoomAction(MapContent.ZOOM_IN, 0, 0);
                        selectNearestWaypointsAtCenter();
                    }
                    break;
                case MA_ZOOM_OUT:
                    if (!mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                        mapContent.makeZoomAction(MapContent.ZOOM_OUT, 0, 0);
                        selectNearestWaypointsAtCenter();
                    }
                    break;
                case MA_SWITCH_PROVIDER:
                    mapContent.getActualMapLayer().nextProvider();
                    break;
                case MA_SWITCH_MODE:
                    mapContent.getActualMapLayer().nextMode();
                    break;
                case MA_MY_LOCATION:
                    if (!mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                        if (mapContent.isZoomProcess()) {
//                            mapWidget.makeZoomAction(MapContent.ZOOM_PAN, -1 * mapWidget.getActualZoomPanX(),
//                                    -1 * mapWidget.getActualZoomPanY());
                            mapContent.makeZoomAction(MapContent.ZOOM_PAN,
                                    -1 * (mapContent.getActualZoomPanX() - mainContainer.getWidth() / 2),
                                    -1 * (mapContent.getActualZoomPanY() - mainContainer.getHeight() / 2));
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
        if (mapContent.isZoomProcess()) {
            mapContent.makeZoomAction(MapContent.ZOOM_UP, 0, 0);
        } else if (mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
            DescriptionMapItem item = (DescriptionMapItem) mapContent.getMapItemManager().getItemTemp(tempWaypointDescriptionItemName);
            if (item != null) {
                item.selectNext();
            }
        } else {
            if (mapContent.isPaintDuringPanning()) {
                mapContent.setCenterToActualLocation(false);
                mapContent.getActualMapLayer().panUp();
                setNewCenterAfterRepaint = true;
                mapContent.getMapItemManager().panItem(0, 1 * mapContent.getActualMapLayer().PAN_PIXELS);
                selectNearestWaypointsAtCenter();
            } else {
                mapContent.makePanAction(0, -1 * mapContent.getActualMapLayer().PAN_PIXELS);
            }
        }
    }

    private void actionPanDown() {
        if (mapContent.isZoomProcess()) {
            mapContent.makeZoomAction(MapContent.ZOOM_DOWN, 0, 0);
        } else if (mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
            DescriptionMapItem item = (DescriptionMapItem) mapContent.getMapItemManager().getItemTemp(tempWaypointDescriptionItemName);
            if (item != null) {
                item.selectPrev();
            }
        } else {
            if (mapContent.isPaintDuringPanning()) {
                mapContent.setCenterToActualLocation(false);
                mapContent.getActualMapLayer().panDown();
                setNewCenterAfterRepaint = true;
                mapContent.getMapItemManager().panItem(0, -1 * mapContent.getActualMapLayer().PAN_PIXELS);
                selectNearestWaypointsAtCenter();
            } else {
                mapContent.makePanAction(0, mapContent.getActualMapLayer().PAN_PIXELS);
            }
        }
    }

    private void actionPanLeft() {
        if (mapContent.isZoomProcess()) {
            mapContent.makeZoomAction(MapContent.ZOOM_LEFT, 0, 0);
        } else if (mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
            selectNextFromSelected(true);
        } else {
            if (mapContent.isPaintDuringPanning()) {
                mapContent.setCenterToActualLocation(false);
                mapContent.getActualMapLayer().panLeft();
                setNewCenterAfterRepaint = true;
                mapContent.getMapItemManager().panItem(1 * mapContent.getActualMapLayer().PAN_PIXELS, 0);
                selectNearestWaypointsAtCenter();
            } else {
                mapContent.makePanAction(-1 * mapContent.getActualMapLayer().PAN_PIXELS, 0);
            }
        }
    }

    private void actionPanRight() {
        if (mapContent.isZoomProcess()) {
            mapContent.makeZoomAction(MapContent.ZOOM_RIGHT, 0, 0);
        } else if (mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
            selectNextFromSelected(false);
        } else {
            if (mapContent.isPaintDuringPanning()) {
                mapContent.setCenterToActualLocation(false);
                mapContent.getActualMapLayer().panRight();
                setNewCenterAfterRepaint = true;
                mapContent.getMapItemManager().panItem(-1 * mapContent.getActualMapLayer().PAN_PIXELS, 0);
                selectNearestWaypointsAtCenter();
            } else {
                mapContent.makePanAction(mapContent.getActualMapLayer().PAN_PIXELS, 0);
            }
        }
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
                if (mapContent.isZoomProcess()) {
                    mapContent.makeZoomAction(MapContent.ZOOM_PAN, -1 * (pointerX - x), -1 * (pointerY - y));
                } else if ((pointerX - x) != 0 || (pointerY - y) != 0) {
                    if (mapContent.isPaintDuringPanning()) {
                        mapContent.getActualMapLayer().pan(pointerX - x, pointerY - y);
                        mapContent.setCenterToActualLocation(false);
                        setNewCenterAfterRepaint = true;
                        mapContent.getMapItemManager().panItem(x - pointerX, y - pointerY);
                    } else {
                        mapContent.makePanAction(pointerX - x, pointerY - y);
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
//System.out.println(x + " " + y + " " + mainContainer.getAbsoluteY() + " " + mainContainer.getY() + " " + mainContainer.getHeight() + " " + MapImages.imageIconZoomSideSize);
                if (x < MapImages.imageIconZoomSideSize &&
                        y > mainContainer.getAbsoluteY() &&
                        y < mainContainer.getAbsoluteY() + MapImages.imageIconZoomSideSize &&
                        y - mainContainer.getAbsoluteY() < -1 * x + MapImages.imageIconZoomSideSize) {
                    makeMapAction(MA_ZOOM_IN, null);
                } else if (x < MapImages.imageIconZoomSideSize &&
                        y > (mainContainer.getAbsoluteY() + mainContainer.getHeight() - MapImages.imageIconZoomSideSize) &&
                        y < mainContainer.getAbsoluteY() + mainContainer.getHeight() &&
                        y - mainContainer.getAbsoluteY() > x + (mainContainer.getHeight() - MapImages.imageIconZoomSideSize)) {
                    makeMapAction(MA_ZOOM_OUT, null);
                } else if (mapContent.getMapItemManager().existItemTemp(tempWaypointDescriptionItemName)) {
                    if (Math.sqrt((lastSelectedX - x) * (lastSelectedX - x) + (lastSelectedY - y) *
                            (lastSelectedY - y)) < (mapContent.getActualMapLayer().PAN_PIXELS * 1.0)) {
                        selectNextFromSelected(false);
                    } else {
                        ((DescriptionMapItem) mapContent.getMapItemManager().getItemTemp(tempWaypointDescriptionItemName)).selectButtonAt(x, y);
                        makeSelectionActionFire();
                    }
                } else {
                    selectNearestWaypoints(x, y, mapContent.getActualMapLayer().PAN_PIXELS * 2 / 3, false);
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
            mapContent.getActualMapLayer().calculateZoomFrom(item.getBoundingLocations());
            mapContent.getMapItemManager().disableInitializeState();
        }
    }

    public void showActualRoute(RouteVariables routeVariables) {
        if (routeVariables.getPointsCount() > 0) {
            if (!mapContent.getMapItemManager().existItemTemp(tempRunningRouteName)) {
                mapContent.getMapItemManager().addItemTemp(tempRunningRouteName, new RouteMapItem(
                        routeVariables.getRoutePoints()), MapItem.PRIORITY_MEDIUM);
            } else {
                RouteMapItem item = (RouteMapItem) mapContent.getMapItemManager().getItemTemp(tempRunningRouteName);
                item.setVectorLocation4D(routeVariables.getRoutePoints());
            }
        }
    }

    /**************************************************/
    /*           SELECTION ACTION SECTION             */
    /**************************************************/
    public void selectNearestWaypointsAtCenter() {
        selectNearestWaypoints(mainContainer.getWidth() / 2, mainContainer.getHeight() / 2, mapContent.getActualMapLayer().PAN_PIXELS * 2 / 3, false);
    }

    private void selectNearestWaypoints(int x, int y, int radius, boolean deleteDescription) {
        lastSelectedX = x;
        lastSelectedY = y;
        selectedMapItemWaypoints = mapContent.getMapItemManager().getWaypointsAtPosition(x, y, radius * radius);
        selectedMapItemIndex = -1;
        if (deleteDescription) {
            mapContent.getMapItemManager().removeItemTemp(tempWaypointDescriptionItemName);
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
//            addComponent(BorderLayout.NORTH, new MapItemInfoPanel());
            mapContent.getMapItemManager().addItemTemp(tempWaypointDescriptionItemName,
                    new DescriptionMapItem((Waypoint) selectedMapItemWaypoints.elementAt(selectedMapItemIndex)),
                    MapItem.PRIORITY_LOW);
        }
    }

    private void makeSelectionActionFire() {
        DescriptionMapItem item = (DescriptionMapItem) mapContent.getMapItemManager().getItemTemp(tempWaypointDescriptionItemName);
        if (item != null) {
            if (item.getSelectedType() == -1) {
                return;
            }

            switch (item.getSelectedType()) {
                case DescriptionMapItem.BUTTON_NAVIGATE:
                    mapContent.getMapItemManager().removeItemTemp(tempWaypointDescriptionItemName);
                    differentScreenLock = true;
                    R.getNavigationScreen().updateWaypoint(item.getSelectedWaypoint());
                    R.getURL().call("locify://navigation");
                    break;
                case DescriptionMapItem.BUTTON_NAVIGATE_ON_MAP:
                    startMapNavigation(item.getSelectedWaypoint());
                    break;
                case DescriptionMapItem.BUTTON_CLOSE:
                    mapContent.getMapItemManager().removeItemTemp(tempWaypointDescriptionItemName);
                    ((Waypoint) selectedMapItemWaypoints.elementAt(selectedMapItemIndex)).state = Waypoint.STATE_HIGHLIGHT;
                    selectedMapItemIndex = -1;
                    repaint();
                    break;
            }
        }
    }

    public void startMapNavigation(Waypoint waypoint) {
        differentScreenLock = false;
        mapContent.getMapItemManager().addItemTemp(tempMapNavigationItem,
                new MapNavigationItem(
                new Waypoint(R.getLocator().getLastLocation().getLatitude(),
                R.getLocator().getLastLocation().getLongitude(), " ", " ", null),
                waypoint), MapItem.PRIORITY_MEDIUM);
        mapContent.getMapItemManager().removeItemTemp(tempWaypointDescriptionItemName);
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
