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

import com.locify.client.data.items.GeoData;
import com.locify.client.data.items.GeoFiles;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.locator.*;
import com.locify.client.maps.*;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.maps.mapItem.*;
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.*;
import com.locify.client.utils.math.LMath;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.BitMapFontViewer;
import de.enough.polish.util.Locale;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Screen for viewing all maps and items on map
 * @author Jiri & Menion
 */
public class MapScreen extends Screen implements CommandListener, LocationEventListener, Runnable {

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


    // command
    private Command cmdZoomIn,  cmdZoomOut,  cmdChangeMapTile,  cmdChangeMapFile,  cmdMyLocation;//,  cmdSelectItem;
    private Command[] providerCommandsTile;
    private Command[] providerCommandsFile;
    private boolean drawLock;
    private static int TOP_MARGIN = 25;
    private static int BOTTOM_MARGIN = 21;
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
    /* path to image tiles */
    public static final String IMAGE_EMPTY_TILE = "/map_tile_64x64.png";
    public static final String IMAGE_ICON_PLUS = "/map_icon_plus.png";
    public static final String IMAGE_ICON_MINUS = "/map_icon_minus.png";
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
    /** thread for painting */
//    private PaintThread paintThread;
    /** if some mapItems are selected this isn't null */
    private Vector selectedMapItemWaypoints;
    /** index of selected items */
    private int selectedMapItemIndex;
    /** support for touch screen */
    private int lastSelectedX,  lastSelectedY;
    /* temp item names */
    private String tempWaypointDescriptionItemName = "selectedItem";
    private String tempMapNavDottedLineItem = "navigationItem";
    private String tempRunningRouteName = "runningRoute";
    /** time of stylus press */
    private long stylusTought;
    /** center of zoom in button */
    private Point2D.Int touchZoomInButtonCenter;
    /** center of zoom out button */
    private Point2D.Int touchZoomOutButtonCenter;
    /** radius of touch buttons */
    private int touchZoomButtonRadius;

    //public static long drawTestTime;
    
    public MapScreen() {
        super(Locale.get("Maps"), true);
        this.setCommandListener(this);
        drawLock = false;
        zoomProcess = false;
        mapItemManager = R.getMapItemManager();
        selectedMapItemWaypoints = new Vector();
        selectedMapItemIndex = -1;
        lastSelectedX = 0;
        lastSelectedY = 0;

        //hasPointerSupport = true;
        touchZoomInButtonCenter = new Point2D.Int(10, TOP_MARGIN + 12);
        touchZoomOutButtonCenter = new Point2D.Int(10, getHeight() - BOTTOM_MARGIN - 42);
        touchZoomButtonRadius = 8;

        mapTile = new TileMapLayer(this);
        mapFile = new FileMapLayer(this);
        map = mapTile;
        map.setProviderAndMode(R.getSettings().getDefaultOnlineMapProvider());
        map.setDefaultZoomLevel();

        cmdZoomIn = new Command(Locale.get("Zoom_in"), Command.SCREEN, 1);
        cmdZoomOut = new Command(Locale.get("Zoom_out"), Command.SCREEN, 2);
        cmdMyLocation = new Command(Locale.get("My_location"), Command.SCREEN, 3);
        //cmdSelectItem = new Command(Locale.get("SelectItem"), Command.SCREEN, 4);
        cmdChangeMapTile = new Command(Locale.get("Change_map_tile"), Command.SCREEN, 5);
        cmdChangeMapFile = new Command(Locale.get("Change_map_file"), Command.SCREEN, 6);
        this.addCommand(Commands.cmdBack);
        //#style imgHome
        this.addCommand(Commands.cmdHome);
        this.addCommand(cmdZoomIn);
        this.addCommand(cmdZoomOut);
        this.addCommand(cmdMyLocation);

        //mapTile provider commands        
        Vector providers = mapTile.getProvidersAndModes();
        providerCommandsTile = new Command[providers.size()];
        if (providers.size() > 0) {
            this.addCommand(cmdChangeMapTile);
            for (int i = 0; i < providers.size(); i++) {
                providerCommandsTile[i] = new Command((String) providers.elementAt(i), Command.SCREEN, i);
                UiAccess.addSubCommand(providerCommandsTile[i], cmdChangeMapTile, this);
            }
        }

        //another location commands
        //#style imgWhere
        this.addCommand(Commands.cmdAnotherLocation);
        for (int i = 0; i < R.getContext().commands.length; i++) {
            if (i != LocationContext.GPS) {
                UiAccess.addSubCommand(R.getContext().commands[i], Commands.cmdAnotherLocation, this);
            }
        }
    //setFileProvider(R.getSettings().getFileMapProviders().getDefaultProvider());
    }

    /**
     * Views map screen
     */
    public void view() {
        try {
            mapItemManager.init();
                //setFileMapProviders();
                //setFileProvider(0);

            if (lastCenterPoint != null) {
                if (map instanceof TileMapLayer) {
                    ((TileMapLayer) map).setCenter(((TileMapLayer) map).getCenter());
                } else {
                    centerMap(lastCenterPoint, centerToActualLocation);
                }
            } else {
                centerMap(R.getLocator().getLastLocation(), centerToActualLocation);
            }

            R.getMidlet().switchDisplayable(null, this);
            selectNearestWaypointsAtCenter();
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
        waypoints.addElement(new Waypoint(lat, lon, name, desc));
        objectsAddToShow(name, new PointMapItem(waypoints));
        centerMap(new Location4D(lat, lon, 0f), false);
        view();
    }

    /**
     * Views file on the map
     * @param fileName
     */
    public void view(String fileName) {
        int type = GeoFiles.getDataTypeFile(fileName);
        if (type == GeoFiles.TYPE_WAYPOINT) {
            view(GeoFiles.loadWaypointFile(fileName));
        } else if (type == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
            view(GeoFiles.loadWaypointsCloudFile(fileName));
        } else if (type == GeoFiles.TYPE_ROUTE) {
            view(GeoFiles.loadRouteFile(fileName, false));
        }
    }
    
    public void view(GeoData data) {
        if (data.getName().equals(""))
            return;
        if (data instanceof Waypoint) {
            Waypoint waypoint = (Waypoint) data;
            Vector waypoints = new Vector();
            waypoints.addElement(waypoint);
            objectsAddToShow(waypoint.getName(), new PointMapItem(waypoints));
            Location4D loc = new Location4D(waypoint.getLatitude(), waypoint.getLongitude(), 0);
            //zooming map to point and actual location pair - by destil
            map.calculateZoomFrom(new Location4D[]{loc,R.getLocator().getLastLocation()});
            objectsDeinitialize();
            centerMap(loc, false);
        } else if (data instanceof WaypointsCloud) {
            WaypointsCloud cloud = (WaypointsCloud) data;
            if (cloud.getWaypointsCount() != 0) {
                PointMapItem mapItem = new PointMapItem(cloud.getWaypointsCloudPoints());
                objectsAddToShow(cloud.getName(), mapItem);
                centerMap(mapItem.getItemCenter(), false);
                objectZoomTo(mapItem);
            }
        } else if (data instanceof Route) {
            Route route = (Route) data;
            if (route.getWaypointCount() != 0) {
                RouteMapItem mapItem = new RouteMapItem(route);
                objectsAddToShow(route.getName(), mapItem);
                centerMap(mapItem.getItemCenter(), false);
                objectZoomTo(mapItem);
            }
        }
        view();
    }

    private void setFileMapProviders() {
        this.removeCommand(cmdChangeMapFile);

        Vector providers = mapFile.getProvidersAndModes();
        providerCommandsFile = new Command[providers.size()];
        if (providers.size() > 0) {
            this.addCommand(cmdChangeMapFile);
            for (int i = 0; i < providers.size(); i++) {
                providerCommandsFile[i] = new Command((String) providers.elementAt(i), Command.SCREEN, i);
                UiAccess.addSubCommand(providerCommandsFile[i], cmdChangeMapFile, this);
            }
        }
    }

    public void centerMap(Location4D newCenter, boolean centerToActualLocation) {
        this.lastCenterPoint = newCenter;
        this.centerToActualLocation = centerToActualLocation;
        map.setLocationCenter(lastCenterPoint);

        objectsDeinitialize();
    }

    public MapLayer getActualMapLayer() {
        return map;
    }

    public void paint(Graphics g2) {
//drawTestTime = System.currentTimeMillis();
//System.out.print("\ndrawStart");
        try {
            super.paint(g2);

            if (drawLock || isMenuOpened()) {
                return;
            }
            drawLock = true;

            if (g2.getClipHeight() > 40) {
                g2.setClip(0, R.getTopBar().height, g2.getClipWidth(), getAvailableHeight());
                drawMap(g2);
            } else {
                TOP_MARGIN = g2.getClipHeight(); //nastavi dle pokusi o vyhresleni v TopBarBackground
            }

            drawLock = false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.paint()", null);
        }
//System.out.println((System.currentTimeMillis() - drawTestTime) + " drawFinished");
    }

    /** 
     * Draws map background (tiles) and other screen components like zoom scale, location pointer etc.
     */
    private void drawMap(Graphics g) {
        // by repaint +100 objects
//System.out.println((System.currentTimeMillis() - drawTestTime) + " step 01");
        try {
            map.drawMap(g);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "map.drawMap()");
        }

//System.out.println((System.currentTimeMillis() - drawTestTime) + " step 02");
        if (R.getLocator().hasValidLocation()) {
            drawActualLocationPoint(g);
        }

//System.out.println((System.currentTimeMillis() - drawTestTime) + " step 03");
        // draw selection circle
        g.setColor(ColorsFonts.BLACK);
        g.drawArc(getWidth() / 2 - 2, getHeight() / 2 - 2, 4, 4, 0, 360);

//System.out.println((System.currentTimeMillis() - drawTestTime) + " step 04");
        try {
            mapItemManager.drawItems(g);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapItemManager.drawItems()");
        }

//System.out.println((System.currentTimeMillis() - drawTestTime) + " step 05");
        if (zoomProcess) {
            try {
                int moveX = touchZoomInButtonCenter.x + touchZoomButtonRadius + 4;
                g.setColor(ColorsFonts.BLUE);
                g.drawRoundRect(pxMoveX - borderW / 2, pxMoveY - borderH / 2,
                        borderW, borderH, 3, 3);

                if (!MainScreen.hasPointerEvents) {
                    g.drawImage(getMapIconPlus(), touchZoomInButtonCenter.x,
                            touchZoomInButtonCenter.y, Graphics.VCENTER | Graphics.HCENTER);
                    g.drawImage(getMapIconMinus(), touchZoomOutButtonCenter.x,
                            touchZoomOutButtonCenter.y, Graphics.VCENTER | Graphics.HCENTER);
                }

                g.setColor(ColorsFonts.BLACK);
                g.drawLine(moveX + 5, TOP_MARGIN + 10, moveX + 5, getHeight() - BOTTOM_MARGIN - 40);
                g.fillRect(moveX, TOP_MARGIN + 10, 10, 3);
                g.fillRect(moveX, getHeight() - BOTTOM_MARGIN - 40, 10, 3);
                double pxPerZoom = (getHeight() - TOP_MARGIN - BOTTOM_MARGIN - 50.0) /
                        (map.getMaxZoomLevel() - map.getMinZoomLevel());

                int actZoomPixel = (int) (TOP_MARGIN + 10 + (map.getMaxZoomLevel() -
                        (map.getActualZoomLevel() + zoomTotalValue)) * pxPerZoom);

                g.setColor(ColorsFonts.RED);
                g.fillRect(moveX, actZoomPixel - 1, 10, 3);
            } catch (Exception e) {
               R.getErrorScreen().view(e, "MapScreen.drawMap()", "zoomProcess");
            }
        } else {
            try {
                g.setColor(ColorsFonts.MAGENTA);
                for (int i = 0; i < selectedMapItemWaypoints.size(); i++) {
                    Point2D.Int temp = map.getLocationCoord(
                            new Location4D(
                            ((Waypoint) selectedMapItemWaypoints.elementAt(i)).getLatitude(),
                            ((Waypoint) selectedMapItemWaypoints.elementAt(i)).getLongitude(),
                            0.0f));
                    g.drawArc(temp.x - 3, temp.y - 3, 6, 6, 0, 360);
                }
            } catch (Exception e) {
               R.getErrorScreen().view(e, "MapScreen.drawMap()", "selectedMapItemWaypoints");
            }
        }

//System.out.println((System.currentTimeMillis() - drawTestTime) + " step 06");
        try {
            if (MainScreen.hasPointerEvents) {
                g.setColor(ColorsFonts.LIGHT_ORANGE);
                g.fillArc(touchZoomInButtonCenter.x - touchZoomButtonRadius,
                        touchZoomInButtonCenter.y - touchZoomButtonRadius,
                        touchZoomButtonRadius * 2, touchZoomButtonRadius * 2,
                        0, 360);
                g.fillArc(touchZoomOutButtonCenter.x - touchZoomButtonRadius,
                        touchZoomOutButtonCenter.y - touchZoomButtonRadius,
                        touchZoomButtonRadius * 2, touchZoomButtonRadius * 2,
                        0, 360);

                g.setColor(ColorsFonts.BLACK);
                g.drawArc(touchZoomInButtonCenter.x - touchZoomButtonRadius,
                        touchZoomInButtonCenter.y - touchZoomButtonRadius,
                        touchZoomButtonRadius * 2, touchZoomButtonRadius * 2,
                        0, 360);
                g.drawArc(touchZoomOutButtonCenter.x - touchZoomButtonRadius,
                        touchZoomOutButtonCenter.y - touchZoomButtonRadius,
                        touchZoomButtonRadius * 2, touchZoomButtonRadius * 2,
                        0, 360);

                g.drawImage(getMapIconPlus(), touchZoomInButtonCenter.x,
                        touchZoomInButtonCenter.y, Graphics.VCENTER | Graphics.HCENTER);
                g.drawImage(getMapIconMinus(), touchZoomOutButtonCenter.x,
                        touchZoomOutButtonCenter.y, Graphics.VCENTER | Graphics.HCENTER);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.drawMap()", "mapZoomButtons");
        }
    }

    public MapItemManager getMapItemManager() {
        return mapItemManager;
    }

    private void drawActualLocationPoint(Graphics g) {
        Point2D actPoint = map.getLocationCoord(R.getLocator().getLastLocation());
        if (actPoint != null) {
            g.setColor(ColorsFonts.MAP_ACTUAL_LOCATION);
            g.drawArc((int) actPoint.getX() - 5, (int) actPoint.getY() - 5, 9, 9, 0, 360);
            g.drawLine((int) actPoint.getX() - 10, (int) actPoint.getY(),
                    (int) actPoint.getX() + 10, (int) actPoint.getY());
            g.drawLine((int) actPoint.getX(), (int) actPoint.getY() - 10,
                    (int) actPoint.getX(), (int) actPoint.getY() + 10);
        }
    }

    public void commandAction(Command cmd, Displayable disp) {
        try {
            if (cmd.equals(Commands.cmdBack)) {
                //map.stop(); //stops loading tiles
                selectNearestWaypoints(0, 0, 0, true); // deselect object selection
                if (R.getContext().isTemporary()) {
                    R.getContext().removeTemporaryLocation();
                }
                R.getBack().goBack();
            //R.getURL().call("locify://mainScreen");
            } else if (cmd.equals(Commands.cmdHome)) {
                //map.stop(); //stops loading tiles
                selectNearestWaypoints(0, 0, 0, true); // deselect object selection
                if (R.getContext().isTemporary()) {
                    R.getContext().removeTemporaryLocation();
                }
                R.getURL().call("locify://mainScreen");
            } else if (cmd.equals(cmdZoomIn)) {
                makeMapAction(MA_ZOOM_IN, null);
            } else if (cmd.equals(cmdZoomOut)) {
                makeMapAction(MA_ZOOM_OUT, null);
            } else if (cmd.equals(cmdMyLocation)) {
                makeMapAction(MA_MY_LOCATION, null);
//            } else if (cmd.equals(cmdSelectItem)) {
//                makeMapAction("select", null);
            } else {
                for (int i = 0; i < providerCommandsTile.length; i++) {
                    if (providerCommandsTile[i].equals(cmd)) {
                        mapFile.destroyMap();
                        map = mapTile;
                        map.setProviderAndMode(i);
                        //centerMap(lastCenterPoint, centerToActualLocation);
                        repaint();
                        return;
                    }
                }

                if (providerCommandsFile != null) {
                    for (int i = 0; i < providerCommandsFile.length; i++) {
                        if (providerCommandsFile[i].equals(cmd)) {
                            setFileProvider(i);
                            //centerMap(lastCenterPoint, centerToActualLocation);
                            repaint();
                            return;
                        }
                    }
                }

                for (int i = 0; i < R.getContext().commands.length; i++) {
                    if (cmd == R.getContext().commands[i]) {
                        lastCenterPoint = null;
                        R.getContext().setTemporaryScreen("locify://maps");
                        R.getURL().call(R.getContext().actions[i]);
                        return;
                    }
                }

                this.view();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.commandAction()", null);
        }
    }

    private void setFileProvider(int provider) {
        if (mapFile.setProviderAndMode(provider)) {
            mapTile.destroyMap();
            map = mapFile;
            //mapTile.stop();
            makeMapAction(MA_MY_LOCATION, null);
        } else {
            //this.setTitle(Locale.get("Maps") + " - " + mapFile.getProviderName() + " not ready!");
//            if (this.isShown()) {
            R.getCustomAlert().quickView(
                    "  " + Locale.get("File_map_cannot_initialize") + " " + mapFile.getProviderName(),
                    Locale.get("Info"), "locify://refresh");
//            } else {
//                errorString = Locale.get("File_map_cannot_initialize") + " " + mapFile.getProviderName();
//            }
        }
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        try {
            if (centerToActualLocation) {
                centerMap(location, true);
            //System.out.println("\nMapScreen.locationChanged() setCenter: " + location.toString())
            }

            if (mapItemManager.existItemTemp(tempMapNavDottedLineItem)) {
                ((DottedLineMapItem) mapItemManager.getItemTemp(tempMapNavDottedLineItem)).actualizeActualPosition(location);
            }
            repaint();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.locationChanged()", null);
        }
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

    /** handle key events on map screen
     * @param key 
     */
    public void keyPressed(int key) {
        try {
            super.keyPressed(key);

            if (isMenuOpened()) {
                return;
            }
            if (key < 0) {
                switch (getGameAction(key)) {
                    case DOWN:
                        makeMapAction(MA_PAN_DOWN, null);
                        break;
                    case UP:
                        makeMapAction(MA_PAN_UP, null);
                        break;
                    case LEFT:
                        makeMapAction(MA_PAN_LEFT, null);
                        break;
                    case RIGHT:
                        makeMapAction(MA_PAN_RIGHT, null);
                        break;
                    case FIRE:
                        if (mapItemManager.getItemTemp(tempWaypointDescriptionItemName)!= null)
                            makeSelectionActionFire();
                        else
                            makeMapAction(MA_SELECT, null);
                        break;
                }
            } else {
                switch (key) {
                    case KEY_STAR:
                        makeMapAction(MA_SWITCH_PROVIDER, null);
                        break;
                    case KEY_POUND:
                        makeMapAction(MA_SWITCH_MODE, null);
                        break;
                    case KEY_NUM1: //zoom out
                        makeMapAction(MA_ZOOM_OUT, null);
                        break;
                    case KEY_NUM3: //zoom in
                        makeMapAction(MA_ZOOM_IN, null);
                        break;
                    case KEY_NUM2:
                        makeMapAction(MA_PAN_UP, null);
                        break;
                    case KEY_NUM4:
                        makeMapAction(MA_PAN_LEFT, null);
                        break;
                    case KEY_NUM6:
                        makeMapAction(MA_PAN_RIGHT, null);
                        break;
                    case KEY_NUM8:
                        makeMapAction(MA_PAN_DOWN, null);
                        break;
                    case KEY_NUM0:
                        makeMapAction(MA_MY_LOCATION, null);
                        break;
                    case KEY_NUM7:
                        //makeMapAction("select", null);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.keyPressed()", null);
        }
    }

    public void keyReleased(int keyCode) {
        try {
            super.keyReleased(keyCode);
            moveWhenPressedThread = null;
            firstMove = true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.keyReleased()", null);
        }
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
                            makeZoomAction(ZOOM_PAN, -1 * (pxMoveX - getWidth() / 2), -1 * (pxMoveY - getHeight() / 2));
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
            this.centerToActualLocation = false;
            map.panUp();
            objectsPan(0, 1 * map.getPanSpeed());
            selectNearestWaypointsAtCenter();
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
            this.centerToActualLocation = false;
            map.panDown();
            objectsPan(0, -1 * map.getPanSpeed());
            selectNearestWaypointsAtCenter();
        }
    }

    private void actionPanLeft() {
        if (zoomProcess) {
            makeZoomAction(ZOOM_LEFT, 0, 0);
        } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
            selectNextFromSelected(true);
//            DescriptionMapItem item = (DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName);
//            if (item != null) {
//                item.selectPrev();
//            }
        } else {
            this.centerToActualLocation = false;
            map.panLeft();
            objectsPan(1 * map.getPanSpeed(), 0);
            selectNearestWaypointsAtCenter();
        }
    }

    private void actionPanRight() {
        if (zoomProcess) {
            makeZoomAction(ZOOM_RIGHT, 0, 0);
        } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
            selectNextFromSelected(false);
//            DescriptionMapItem item = (DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName);
//            if (item != null) {
//                item.selectNext();
//            }
        } else {
            this.centerToActualLocation = false;
            map.panRight();
            objectsPan(-1 * map.getPanSpeed(), 0);
            selectNearestWaypointsAtCenter();

        }
    }

    public boolean isOffLineMapEnable() {
        return map instanceof FileMapLayer;
    }
    
//    private class PaintThread extends Thread {
//
//        private Graphics g;
//        private boolean running = false;
//
//        public PaintThread(Graphics g) {
//            this.g = g;
//            this.start();
//        }
//
//        public void run() {
//            try {
//            running = true;
//            if (g.getClipHeight() > 40) {
//                g.setClip(0, R.getTopBar().height, g.getClipWidth(), getAvailableHeight());
//                drawMap(g);
//            } else {
//                TOP_MARGIN = g.getClipHeight(); //nastavi dle pokusi o vyhresleni v TopBarBackground
//            }
//            running = false;
//            } catch (Exception e) {
//                R.getErrorScreen().view(e, "PaintThread.run()", null);
//            }
//        }
//    }

    
    /************************************************/
    /*           STYLUS SUPPORT SECTION             */
    /************************************************/
    /**
     * Called when the pointer is dragged.
     */
    public void pointerDragged(int x, int y) {
        if (isMenuOpened()) {
            super.pointerDragged(x, y);
        } else {
            super.pointerDragged(x, y);
            if (y > TOP_MARGIN && y < (getHeight() - BOTTOM_MARGIN)) {
                try {
                    if (zoomProcess) {
                        makeZoomAction(ZOOM_PAN, -1 * (pointerX - x), -1 * (pointerY - y));
                    } else {
                        map.pan(pointerX - x, pointerY - y);
                        this.centerToActualLocation = false;
                        objectsPan(x - pointerX, y - pointerY);
                        //selectNearestWaypoints(x, y, map.getPanSpeed() * 2 / 3, false);
                    }
                    pointerX = x;
                    pointerY = y;
                    repaint();
                } catch (Exception e) {
                    R.getErrorScreen().view(e, "MapScreen.pointerDragged()", null);
                }
            } else {
                pointerX = x;
                pointerY = y;
            }
        }
    }

    /**
     * Called when the pointer is pressed.
     */
    public void pointerPressed(int x, int y) {
        if (isMenuOpened()) {
            super.pointerPressed(x, y);
        } else {
            super.pointerPressed(x, y);
            if (y > TOP_MARGIN && y < (getHeight() - BOTTOM_MARGIN) && stylusTought == 0) {
                stylusTought = System.currentTimeMillis();
                pointerX = x;
                pointerY = y;
            }
        }
    }

    /**
     * Called when the pointer is released.
     */
    public void pointerReleased(int x, int y) {
        if (isMenuOpened()) {
            super.pointerReleased(x, y);
        } else {
            super.pointerReleased(x, y);
            if (y > TOP_MARGIN && y < (getHeight() - BOTTOM_MARGIN)) {
//System.out.println("Released X: " + x + " Y: " + y);
                if ((System.currentTimeMillis() - stylusTought) < 250) {
                    if (Math.sqrt((touchZoomInButtonCenter.x - x) * (touchZoomInButtonCenter.x - x) +
                            (touchZoomInButtonCenter.y - y) * (touchZoomInButtonCenter.y - y)) < touchZoomButtonRadius) {
                        makeMapAction(MA_ZOOM_IN, null);
                    } else if (Math.sqrt((touchZoomOutButtonCenter.x - x) * (touchZoomOutButtonCenter.x - x) +
                            (touchZoomOutButtonCenter.y - y) * (touchZoomOutButtonCenter.y - y)) < touchZoomButtonRadius) {
                        makeMapAction(MA_ZOOM_OUT, null);
                    } else if (mapItemManager.existItemTemp(tempWaypointDescriptionItemName)) {
//System.out.println("Click 1.");
                        if (Math.sqrt((lastSelectedX - x) * (lastSelectedX - x) + (lastSelectedY - y) * (lastSelectedY - y)) < (map.getPanSpeed() * 1.0)) {
//System.out.println("Click select next");
                            selectNextFromSelected(false);
                        } else {
                            ((DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName)).selectButtonAt(x, y);
//System.out.println("Click selection fire");
                            makeSelectionActionFire();
                        }
                    } else {
//System.out.println("Click select nearest");
                        selectNearestWaypoints(x, y, map.getPanSpeed() * 2 / 3, false);
                        selectNextFromSelected(false);
                    }
                }
            }
            stylusTought = 0;
        }
        repaint();
    }

    /******************************************/
    /*           MAP ITEM SECTION             */
    /******************************************/
    public void objectZoomTo(MapItem item) {
        if (item != null) {
            map.calculateZoomFrom(item.getBoundingLocations());
            objectsDeinitialize();
        }
    }

    public void objectsAddToShow(String itemName, MapItem item) {
        try {
            mapItemManager.addItem(itemName, item);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.objectsAddToShow()", itemName);
        }
    }

    public void objectsPan(int x, int y) {
        mapItemManager.panItem(x, y);
    }

    public void objectsDeinitialize() {
        mapItemManager.disableInitializeState();
    }

    public void showActualRoute(RouteVariables routeVariables) {
        if (routeVariables.getPointsCount() > 0) {
            if (!mapItemManager.existItemTemp(tempRunningRouteName)) {
                mapItemManager.addItemTemp(tempRunningRouteName, new RouteMapItem(routeVariables.getRoutePoints()));
            } else {
                RouteMapItem item = (RouteMapItem) mapItemManager.getItemTemp(tempRunningRouteName);
                item.setNewVectorData(routeVariables.getRoutePoints());
            }
        }
    }

    /****************************************************/
    /*           IMAGE MANIPULATION SECTION             */
    /****************************************************/
    public static Image getImageNotExisted(int tileSizeX, int tileSizeY) {
        try {
            if (imageNotExisted == null ||
                    (imageNotExisted.getWidth() != tileSizeX && imageNotExisted.getHeight() != tileSizeY)) {
                imageNotExisted = getImageVarious(tileSizeX, tileSizeY, Locale.get("File_map_tile_not_exist"));
            }
            return imageNotExisted;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageNotExisted()", null);
            return null;
        }
    }

    public static Image getImageLoading(int tileSizeX, int tileSizeY) {
        try {
            if (imageLoading == null ||
                    (imageLoading.getWidth() != tileSizeX && imageLoading.getHeight() != tileSizeY)) {
                imageLoading = getImageVarious(tileSizeX, tileSizeY, Locale.get("File_map_tile_loading"));
            }
            return imageLoading;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageLoading()", null);
            return null;
        }
    }

    public static Image getImageConnectionNotFound(int tileSizeX, int tileSizeY) {
        try {
            if (imageConnectionNotFound == null ||
                    (imageConnectionNotFound.getWidth() != tileSizeX && imageConnectionNotFound.getHeight() != tileSizeY)) {
                imageConnectionNotFound = getImageVarious(tileSizeX, tileSizeY, Locale.get("Connection_failed"));
            }
            return imageConnectionNotFound;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageLoading()", null);
            return null;
        }
    }

    public static Image getImageTooBigSize(int tileSizeX, int tileSizeY) {
        try {
            if (imageTooBigSize == null ||
                    (imageTooBigSize.getWidth() != tileSizeX && imageTooBigSize.getHeight() != tileSizeY)) {
                imageTooBigSize = getImageVarious(tileSizeX, tileSizeY, Locale.get("Image_too_big"));
            }
            return imageTooBigSize;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.getImageTooBigSize()", null);
            return null;
        }
    }

    private static final Image getLoadingImage() {
        if (loadingImage == null) {
            try {
                loadingImage = Image.createImage(IMAGE_EMPTY_TILE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return loadingImage;
    }

    private static Image getImageVarious(int tileSizeX, int tileSizeY, String tileText) {
        Image image = Image.createImage(tileSizeX, tileSizeY);
        Graphics g = image.getGraphics();

        // draw background
        g.setColor(ColorsFonts.PINK);
        g.fillRect(0, 0, tileSizeX, tileSizeY);
        g.setColor(ColorsFonts.MAP_BG_COLOR);
        g.fillRect(1, 1, tileSizeX - 1, tileSizeY - 1);

        // draw emptyImage on background
        Image img = Image.createImage(getLoadingImage().getWidth() - 2, getLoadingImage().getHeight() - 2);
        Graphics gImg = img.getGraphics();
        gImg.drawRegion(getLoadingImage(),
                1, 1,
                getLoadingImage().getWidth() - 2,
                getLoadingImage().getHeight() - 2,
                Sprite.TRANS_NONE,
                0, 0,
                Graphics.TOP | Graphics.LEFT);

        BitMapFontViewer viewer = ColorsFonts.BMF_ARIAL_10_BLACK.getViewer(tileText);
        viewer.layout(0, 0, 0, Graphics.TOP | Graphics.LEFT);
        viewer.paint((getLoadingImage().getWidth() - viewer.getWidth()) / 2,
                (getLoadingImage().getHeight() - viewer.getHeight()) / 2, gImg);

        g.drawImage(img,
                (tileSizeX - img.getWidth()) / 2,
                (tileSizeY - img.getHeight()) / 2,
                Graphics.TOP | Graphics.LEFT);

        // draw strring label on background
//        img = Image.createImage(getLoadingImage().getWidth(), getLoadingImage().getHeight());
//        gImg = img.getGraphics();
//        gImg.setColor(Colors.WHITE);
//        gImg.fillRect(0, 0, getLoadingImage().getWidth(), getLoadingImage().getHeight());
//        gImg.setColor(Colors.BLACK);
//        
//        BitMapFontViewer viewer = RouteScreenItem.BMF_ARIAL_10.getViewer(tileText);
//        viewer.layout(0, 0, 0, Graphics.TOP | Graphics.LEFT);
//        viewer.paint((getLoadingImage().getWidth() - viewer.getWidth()) / 2,
//                (getLoadingImage().getHeight() - viewer.getHeight()) / 2, gImg);
//        
//        //RgbImage newStringImage = new RgbImage(img, false);
//        RgbImage newStringImage = 
//        new RgbImage(Colors.getImageCopyWithTransparentBackground(img, Colors.WHITE), true);
//        ImageUtil.rotate(newStringImage, 45);
//
//        img = Image.createImage(tileSize, tileSize);
//        gImg = img.getGraphics();
//        newStringImage.paint(10, 10, gImg);

        //g.drawImage(img, 0, 0, Graphics.TOP | Graphics.LEFT);
        //g.drawImage(Colors.getImageCopyWithTransparentBackground(
//        img, Colors.WHITE), 0, 0, Graphics.TOP | Graphics.LEFT);

        return image;
    }

    public static Image getMapIconPlus() {
        if (imageIconPlus == null) {
            try {
                imageIconPlus = Image.createImage(IMAGE_ICON_PLUS);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return imageIconPlus;
    }

    public static Image getMapIconMinus() {
        if (imageIconMinus == null) {
            try {
                imageIconMinus = Image.createImage(IMAGE_ICON_MINUS);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return imageIconMinus;
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
    private boolean zoomProcess;
    private int zoomTotalValue;
    private long zoomLastAction;
    /** only probably value ...
     * WGS84 R = (6378137m * 2 * PI) / 360 */
    private double angleDistance = 111319.4908;
    private int borderWdef,  borderHdef;
    private int borderW,  borderH;
    private int pxMoveX;
    private int pxMoveY;
    private int movePartImageValue = 3;

    private void makeZoomAction(int actionType, int panX, int panY) {
        if (!zoomProcess) {
            zoomThread = new ZoomThread();
            zoomThread.start();
            zoomProcess = true;
            
            zoomTotalValue = 0;

            borderW = borderWdef = getWidth();
            borderH = borderHdef = getHeight() - TOP_MARGIN - BOTTOM_MARGIN;
            pxMoveX = getWidth() / 2;
            pxMoveY = getHeight() / 2;
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

            borderW = (int) ((1 / LMath.pow(2, zoomTotalValue)) * borderWdef);
            borderH = (int) ((1 / LMath.pow(2, zoomTotalValue)) * borderHdef);
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
                map.pan(pxMoveX - getWidth() / 2, pxMoveY - getHeight() / 2);
                map.setZoomLevel(map.getActualZoomLevel() + zoomTotalValue);
                objectsDeinitialize();
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
        selectNearestWaypoints(getWidth() / 2, getHeight() / 2, map.getPanSpeed() * 2 / 3, false);
    }

    private void selectNearestWaypoints(int x, int y, int radius, boolean deleteDescription) {
        lastSelectedX = x;
        lastSelectedY = y;
        selectedMapItemWaypoints = mapItemManager.getWaypointsAtPosition(x, y, radius);
        selectedMapItemIndex = -1;
        if (deleteDescription) {
            mapItemManager.removeItemTemp(tempWaypointDescriptionItemName);
        }
    }

    private void selectNextFromSelected(boolean inverse) {
        if (selectedMapItemWaypoints.size() != 0) {
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
            mapItemManager.addItemTemp(tempWaypointDescriptionItemName,
                    new DescriptionMapItem((Waypoint) selectedMapItemWaypoints.elementAt(selectedMapItemIndex)));
        }
    }

    private void makeSelectionActionFire() {
        DescriptionMapItem item = (DescriptionMapItem) mapItemManager.getItemTemp(tempWaypointDescriptionItemName);
        if (item != null) {
            switch (item.getSelectedType()) {
                case DescriptionMapItem.BUTTON_NAVIGATE:
                    mapItemManager.removeItemTemp(tempMapNavDottedLineItem);
                    R.getURL().call("locify://navigation" +
                            "?lat=" + item.getSelectedWaypoint().getLatitude() +
                            "&lon=" + item.getSelectedWaypoint().getLongitude() +
                            "&name=" + item.getSelectedWaypoint().getName());
                    break;
                case DescriptionMapItem.BUTTON_NAVIGATE_ON_MAP:
                    mapItemManager.addItemTemp(tempMapNavDottedLineItem,
                            new DottedLineMapItem(
                            new Waypoint(R.getLocator().getLastLocation().getLatitude(),
                            R.getLocator().getLastLocation().getLongitude(), " ", " "),
                            item.getSelectedWaypoint()));
                    repaint();
                    break;
                case DescriptionMapItem.BUTTON_CLOSE:
                    mapItemManager.removeItemTemp(tempMapNavDottedLineItem);
                    repaint();
                    break;
            }

            mapItemManager.removeItemTemp(tempWaypointDescriptionItemName);
        }
    }
}
