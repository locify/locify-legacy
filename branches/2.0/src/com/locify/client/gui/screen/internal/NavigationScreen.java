/*
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
import com.locify.client.data.items.MultiGeoData;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.data.SettingsData;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.utils.GpsUtils;
import com.locify.client.locator.Navigator;
import com.locify.client.locator.impl.WaypointNavigatorModel;
import com.locify.client.locator.impl.WaypointRouteNavigatorModel;
import com.locify.client.utils.Backlight;
import com.locify.client.route.ScreenItem;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Utils;
import com.locify.client.utils.R;
import com.locify.client.utils.math.LMath;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.game.GameCanvas;


/** 
 * Screen shows compas, GPS infomation, speed. In case of navigation displays navigation arrow
 * @author Jiri Stepan
 */
public class NavigationScreen extends FormLocify implements ActionListener, LocationEventListener {

    private static final int VIEW_MODE_MORE = 0;
    private static final int VIEW_MODE_LESS = 1;
    private int viewMode;
    private static Navigator navigator;
    private boolean drawLock;    // screen items
    private Location4D location;
    // navigation angle (heading)
    public static double nAngle;
    // diference angle beetween heading and navigated point
    public static double dAngle;    // items to display
    private Vector items;
    private ScreenItem tempItem;
    private static final int ITEM_DISTANCE = 0;
    private static final int ITEM_SPEED = 1;
    private static final int ITEM_LATITUDE = 2;
    private static final int ITEM_LONGITUDE = 3;
    private static final int ITEM_ALTITUDE = 4;
    private static final int ITEM_ACCURACY = 5;
    private static final int ITEM_TIME = 6;
    private static final int ITEM_DATE = 7;
    // angles images
    private Image[] numbers;
    // center of compas
    private int cX;
    private int cY;
    // compas radius
    private int radius;
    private boolean smallRadius;
    private Backlight backLight;
    private static int BOTTOM_MARGIN;
    private static int TOP_MARGIN;

    public NavigationScreen() {

        super(Locale.get("Navigation"));
        try {
            smallRadius = false;

            this.addCommand(Commands.cmdBack);
            this.addCommand(Commands.cmdMore);
            this.addCommand(Commands.cmdMore);

            if (R.getBacklight().isOn()) {
                this.addCommand(Commands.cmdBacklightOff);
            } else {
                this.addCommand(Commands.cmdBacklightOn);
            }

            this.addCommand(Commands.cmdHome);
            // i know about added gps ... actualy i'm lazy :)
            this.addCommand(new ParentCommand(Locale.get("Another_location"), null, R.getContext().commands));
            this.setCommandListener(this);

            numbers = new Image[12];
            numbers[0] = Image.createImage("/numberN.png");
            numbers[1] = Image.createImage("/number030.png");
            numbers[2] = Image.createImage("/number060.png");
            numbers[3] = Image.createImage("/numberE.png");
            numbers[4] = Image.createImage("/number120.png");
            numbers[5] = Image.createImage("/number150.png");
            numbers[6] = Image.createImage("/numberS.png");
            numbers[7] = Image.createImage("/number210.png");
            numbers[8] = Image.createImage("/number240.png");
            numbers[9] = Image.createImage("/numberW.png");
            numbers[10] = Image.createImage("/number300.png");
            numbers[11] = Image.createImage("/number330.png");

            R.getLocator().addLocationChangeListener(this);
        } catch (IOException ex) {
            R.getErrorScreen().view(ex, "NavigationScreen.constructor()", null);
        }
    }

    public void view() {
        try {
            //init variables
            location = R.getLocator().getLastLocation();
            nAngle = 0;

            drawLock = false;

            TOP_MARGIN = R.getTopBar().height;
            BOTTOM_MARGIN = R.getTopBar().height;

            initializeItems();
            if (radius == 0) {
                setMode(VIEW_MODE_LESS);
            }
            locationChanged(null, location);
            if (R.getSettings().getBacklight() == SettingsData.NAVIGATION || R.getSettings().getBacklight() == SettingsData.MAP_NAVIGATION) {
                R.getBacklight().on();
                removeCommand(Commands.cmdBacklightOn);
                addCommand(Commands.cmdBacklightOff);
            }
            this.show();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NavigationScreen.view", null);
        }
    }

    /**
     * this function initialize RouteScreenItem variables
     * needed for showing them on screen
     * !!! important !!! right order for adding have to be !!!
     */
    private void initializeItems() {
        if (items == null) {
            items = new Vector();

            // ITEM_ROUTE_TIME
            ScreenItem button00 = new ScreenItem(Locale.get("Distance"));
            items.addElement(button00);
            // ITEM_ROUTE_DIST
            ScreenItem button01 = new ScreenItem(Locale.get("Speed"));
            items.addElement(button01);
            // ITEM_SPEED_MAX
            ScreenItem button02 = new ScreenItem(Locale.get("Latitude"));
            items.addElement(button02);
            // ITEM_SPEED_AVERAGE
            ScreenItem button03 = new ScreenItem(Locale.get("Longitude"));
            items.addElement(button03);
            // ITEM_SPEED_ACTUAL
            ScreenItem button04 = new ScreenItem(Locale.get("Altitude"));
            items.addElement(button04);
            // ITEM_LATITUDE
            ScreenItem button05 = new ScreenItem(Locale.get("Accuracy"));
            items.addElement(button05);
            // TIME
            ScreenItem button06 = new ScreenItem(Locale.get("Time"));
            items.addElement(button06);
            // DATE
            ScreenItem button07 = new ScreenItem(Locale.get("Date"));
            items.addElement(button07);

            for (int i = 0; i < items.size(); i++) {
                tempItem = (ScreenItem) items.elementAt(i);
                tempItem.setTextValue(" ");
                //#if WMbig
//#                 tempItem.setFont(ColorsFonts.FONT_BMF_16, ColorsFonts.FONT_BMF_16);
                //#else
                tempItem.setFont(ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_14);
                //#endif
            }
        }
    }

    /**
     * Starts navigation using navigator (waypoint, route)
     * @param navigator
     */
    public void view(Navigator navigator) {
        NavigationScreen.navigator = navigator;
        view();
    }

    public void removeNavigator() {
        navigator = null;
        repaint();
    }

    /**
     * Starts navigation to lat, lon
     * @param lat
     * @param lon
     * @param name
     */
    public void view(double lat, double lon, String name) {
        view(new WaypointNavigatorModel(new Location4D(lat, lon, 0), name));
    }

    public void view(String fileName) {
        MultiGeoData mgd = GeoFiles.parseGeoDataFile(fileName, false);
        if (mgd.getDataSize() > 0) {
            view(mgd.getGeoData(0));
        }
    }

    /**
     * Used when ID of waypoint in map is used
     * @param id
     * @param idc
     */
    public void view(String id, boolean idc) {
        if (R.getMapItemManager().getWaypointById(id) != null) {
            navigator = new WaypointNavigatorModel(R.getMapItemManager().getWaypointById(id));
            R.getMapScreen().setDifferentScreenLock(true);
            R.getMapScreen().resumeNetworkLink();
            R.getBack().deleteLast();
            view();
        }
    }

    /**
     * Determines file type and start navigation to it
     * @param data
     */
    public void view(GeoData data) {
        if (data instanceof Waypoint) {
            Waypoint waypoint = (Waypoint) data;
            view(new WaypointNavigatorModel(waypoint));
        } else if (data instanceof WaypointsCloud) {
            WaypointsCloud cloud = (WaypointsCloud) data;
            view(new WaypointNavigatorModel(cloud.getCenterLocation(), cloud.getName()));
        } else if (data instanceof Route) {
            Route route = (Route) data;
            view(new WaypointRouteNavigatorModel(route));
        }
        view();
    }

    public void updateWaypoint(Waypoint waypoint) {
        if (navigator == null) {
            navigator = new WaypointNavigatorModel(waypoint);
        } else if (navigator instanceof WaypointNavigatorModel) {
            ((WaypointNavigatorModel) navigator).updateWaypoint(waypoint);
            locationChanged(null, location);
        }
    }

    public void paint(Graphics g) {
        try {
            super.paint(g);
            if (drawLock) {
                return;
            }
            this.drawLock = true;

            if (g.getClipHeight() > 40) {
                setCompas(g);
                setArrow(g);
                for (int i = 0; i < items.size(); i++) {
                    ((ScreenItem) items.elementAt(i)).paint(g);
                }
            } else {
                TOP_MARGIN = g.getClipHeight(); //nastavi dle pokusi o vyhresleni v TopBarBackground
            }
            //actualize date and time
            actualizeItem(ITEM_TIME, Utils.getTime());
            actualizeItem(ITEM_DATE, Utils.getDate());
            drawLock = false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.paint()", null);
        }
    }

    // angle in degres
    private void setCompas(Graphics g) {
        g.setColor(ColorsFonts.WHITE);
        g.fillArc(cX - radius, cY - radius, 2 * radius, 2 * radius, 0, 360);

        g.setColor(ColorsFonts.BLACK);
        g.drawArc(cX - radius, cY - radius, 2 * radius, 2 * radius, 0, 360);

        double a;
        int x1, x2, y1, y2;
        int lineLength = radius - 10;
        int stringPosition = radius - 23;
        if (smallRadius) {
            lineLength = radius - 5;
            stringPosition = radius - 18;
        }

        for (int i = 0; i < 36; i++) {
            if (smallRadius && i % 3 != 0) {
                continue;
            }

            a = (i * 10 - nAngle) / LMath.RHO;

            double aSin = Math.sin(a);
            double aCos = Math.cos(a);
            x1 = (int) (aSin * lineLength);
            y1 = (int) (aCos * lineLength);
            x2 = (int) (aSin * radius);
            y2 = (int) (aCos * radius);
            g.drawLine(cX + x1, cY - y1, cX + x2, cY - y2);

            int separator = 3;
            if (smallRadius) {
                separator = 9;
            }

            if (i % separator == 0) {
                x1 = (int) (aSin * stringPosition);
                y1 = (int) (aCos * stringPosition);

                g.drawImage(numbers[i / 3], cX + x1 - numbers[i / 3].getWidth() / 2,
                        cY - y1 - numbers[i / 3].getHeight() / 2);
            }
        }
    }

    private void setArrow(Graphics g) {
        if (navigator != null) {
            g.setColor(ColorsFonts.WHITE);

            double a;
            int x1, x2, x3, x4, y1, y2, y3, y4;

            a = dAngle / LMath.RHO;
            x1 = (int) (Math.sin(a) * radius * 0.65);
            y1 = (int) (Math.cos(a) * radius * 0.65);

            a = (dAngle + 180) / LMath.RHO;
            x2 = (int) (Math.sin(a) * (radius * 0.2));
            y2 = (int) (Math.cos(a) * (radius * 0.2));

            a = (dAngle + 140) / LMath.RHO;
            x3 = (int) (Math.sin(a) * (radius * 0.5));
            y3 = (int) (Math.cos(a) * (radius * 0.5));

            a = (dAngle + 220) / LMath.RHO;
            x4 = (int) (Math.sin(a) * (radius * 0.5));
            y4 = (int) (Math.cos(a) * (radius * 0.5));

            g.setColor(ColorsFonts.RED);

            g.drawLine(cX + x1, cY - y1, cX + x3, cY - y3);
            g.drawLine(cX + x3, cY - y3, cX + x2, cY - y2);
            g.drawLine(cX + x2, cY - y2, cX + x4, cY - y4);
            g.drawLine(cX + x4, cY - y4, cX + x1, cY - y1);

            g.fillTriangle(cX + x1, cY - y1, cX + x2, cY - y2, cX + x3, cY - y3);
            g.fillTriangle(cX + x1, cY - y1, cX + x2, cY - y2, cX + x4, cY - y4);
        }
    }

    private void setMode(int mode) {
        viewMode = mode;
        cX = Capabilities.getWidth() / 2;

        for (int i = 0; i < items.size(); i++) {
            ((ScreenItem) items.elementAt(i)).setVisible(false);
        }

        if (viewMode == VIEW_MODE_LESS) {
            cY = (Capabilities.getHeight() - 60 - TOP_MARGIN - BOTTOM_MARGIN) / 2;
            radius = Math.min(cX, cY) - 5;
            cY = cY + TOP_MARGIN + 10;

            tempItem = (ScreenItem) items.elementAt(ITEM_DISTANCE);
            //#if WMbig
//#             tempItem.setFont(ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_20);
            //#else
            tempItem.setFont(ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_18);
            //#endif
            tempItem.setSizePos(10, Capabilities.getHeight() - 45 - BOTTOM_MARGIN,
                    Capabilities.getWidth() - 20, 35);
            tempItem.setVisible(true);
        } else if (viewMode == VIEW_MODE_MORE) {
            cY = (Capabilities.getHeight() - 100 - TOP_MARGIN - BOTTOM_MARGIN) / 2;
            radius = Math.min(cX, cY) - 5;
            cY = cY + TOP_MARGIN + 10;

            int space = 5;
            int sizeX = (Capabilities.getWidth() - 4 * space) / 3;

            if (radius < 50) {
                cY = (Capabilities.getHeight() - 60 - TOP_MARGIN - BOTTOM_MARGIN) / 2;
                radius = Math.min(cX, cY) - 5;
                cY = cY + TOP_MARGIN + 10;

                ((ScreenItem) items.elementAt(ITEM_DISTANCE)).setSizePos(space, Capabilities.getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_SPEED)).setSizePos(2 * space + sizeX, Capabilities.getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_ACCURACY)).setSizePos(3 * space + 2 * sizeX, Capabilities.getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
            } else {
                ((ScreenItem) items.elementAt(ITEM_TIME)).setSizePos(0, TOP_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_DATE)).setSizePos(Capabilities.getWidth()-sizeX, TOP_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_DISTANCE)).setSizePos(space, Capabilities.getHeight() - 80 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_SPEED)).setSizePos(2 * space + sizeX, Capabilities.getHeight() - 80 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_ACCURACY)).setSizePos(3 * space + 2 * sizeX, Capabilities.getHeight() - 80 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_LATITUDE)).setSizePos(space, Capabilities.getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_LONGITUDE)).setSizePos(2 * space + sizeX, Capabilities.getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.elementAt(ITEM_ALTITUDE)).setSizePos(3 * space + 2 * sizeX, Capabilities.getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
            }

            //#if WMbig
//#             ((ScreenItem) items.elementAt(ITEM_DISTANCE)).setFont(
//#                     ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#             ((ScreenItem) items.elementAt(ITEM_SPEED)).setFont(
//#                     ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
            //#else
            ((ScreenItem) items.elementAt(ITEM_DISTANCE)).setFont(
                    ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
            ((ScreenItem) items.elementAt(ITEM_SPEED)).setFont(
                    ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
            //#endif

            for (int i = 0; i < items.size(); i++) {
                ((ScreenItem) items.elementAt(i)).setVisible(true);
            }
        }

        if (radius < 50) {
            smallRadius = true;
        } else {
            smallRadius = false;
        }
    }

    /**
     * Function which rotate arrow and compas (angles in degrees)
     * @param nAngle new angle for compas north
     * @param dAngle new angle for arrow
     */
    public void moveAngles(final double nAngle, final double dAngle) {
        boolean move = false;
//Logger.log("nAngle: " + nAngle + " dAngle: " + dAngle);
        if (move) {
            final double nDiff = getDiffAngle(NavigationScreen.nAngle, nAngle);
            final double dDiff = getDiffAngle(NavigationScreen.dAngle, dAngle);
            final int numOfSteps = 5;

            Thread thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        for (int i = 0; i < numOfSteps; i++) {
                            NavigationScreen.nAngle = fixDegAngle(NavigationScreen.nAngle + (nDiff / numOfSteps));
                            NavigationScreen.dAngle = fixDegAngle(NavigationScreen.dAngle + (dDiff / numOfSteps));
                            repaint();
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        NavigationScreen.nAngle = nAngle;
                        NavigationScreen.dAngle = dAngle;
                        repaint();
                    } catch (Exception e) {
                        R.getErrorScreen().view(e, "NavigationScreen.moveAngles.run()", null);
                    }
                }
            });

            if (nDiff != 0 || dDiff != 0) {
                thread.start();
            }
        } else {
            NavigationScreen.nAngle = nAngle;
            NavigationScreen.dAngle = dAngle;
        }
    }

    private double getDiffAngle(double oldA, double newA) {
        if (oldA < -360) {
            oldA += 360;
        } else if (oldA > 360) {
            oldA -= 360;
        }
        if (newA < -360) {
            newA += 360;
        } else if (newA > 360) {
            newA -= 360;
        }
        double ang;
        if (newA - oldA <= -180) {
            ang = 360 - (oldA - newA);
        } else if (newA - oldA <= 0) {
            ang = newA - oldA;
        } else if (newA - oldA <= 180) {
            ang = newA - oldA;
        } else {
            ang = (newA - oldA) - 360;
        }
        if (ang < -360) {
            ang += 360;
        }
        if (ang > 360) {
            ang -= 360;
        }
        return ang;
    }

    private double fixDegAngle(double ang) {
        if (ang < 0) {
            ang += 360;
        }
        if (ang > 360) {
            ang -= 360;
        }
        return ang;
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        if (this.isVisible() || sender == null) {
            this.location = location;

            double angleN = R.getLocator().getHeading();
            double angleD = 0;
            if (navigator != null) {
                setTitle(navigator.getToName());
                angleD = navigator.getAzimuthToTaget(location) - angleN;
                actualizeItem(ITEM_DISTANCE, GpsUtils.formatDistance(navigator.getDistanceToTarget(location)));
            }
            moveAngles(angleN, angleD);

            actualizeItem(ITEM_ACCURACY, GpsUtils.formatDouble(R.getLocator().getAccuracyHorizontal(), 1));
            actualizeItem(ITEM_SPEED, GpsUtils.formatSpeed(R.getLocator().getSpeed()));
            actualizeItem(ITEM_LATITUDE, GpsUtils.formatLatitude(location.getLatitude(), R.getSettings().getCoordsFormat()));
            actualizeItem(ITEM_LONGITUDE, GpsUtils.formatLongitude(location.getLongitude(), R.getSettings().getCoordsFormat()));
            actualizeItem(ITEM_ALTITUDE, GpsUtils.formatDouble(location.getAltitude(), 1) + "m");

//Logger.debug("NS (" + System.currentTimeMillis() + "), lat: " + location.getLatitude() +
//        ", lon: " + location.getLongitude() + ", angleN: " + angleN + ", angleD: " + angleD);
            repaint();
        }
    }

    private void actualizeItem(int item, String value) {
        tempItem = (ScreenItem) items.elementAt(item);
        tempItem.setTextValue(value);
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
    }

    public static boolean isRunning() {
        return !(navigator == null);
    }

    public String getWaypointId() {
        if (navigator != null & navigator instanceof WaypointNavigatorModel) {
            return ((WaypointNavigatorModel) navigator).getId();
        }
        return null;
    }

    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        switch (keyCode) {
            case GameCanvas.KEY_NUM7:
                if (R.getBacklight().isOn()) {
                    R.getBacklight().off();
                } else {
                    R.getBacklight().on();
                }
                break;
            case GameCanvas.KEY_NUM5:
                if (viewMode == VIEW_MODE_MORE) {
                    actionPerformed(new ActionEvent(Commands.cmdLess));
                } else {
                    actionPerformed(new ActionEvent(Commands.cmdMore));
                }
                break;
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getBack().goBack();
            if (R.getSettings().getBacklight() != SettingsData.WHOLE_APPLICATION) {
                R.getBacklight().off();
            }
        } else if (evt.getCommand() == Commands.cmdHome) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getURL().call("locify://mainScreen");

            if (R.getSettings().getBacklight() != SettingsData.WHOLE_APPLICATION) {
                R.getBacklight().off();
            }
        } else if (evt.getCommand() == Commands.cmdMore) {
            setMode(VIEW_MODE_MORE);
            removeCommand(Commands.cmdMore);
            addCommand(Commands.cmdLess);
        } else if (evt.getCommand() == Commands.cmdLess) {
            setMode(VIEW_MODE_LESS);
            removeCommand(Commands.cmdLess);
            addCommand(Commands.cmdMore);
        } else if (evt.getCommand() == Commands.cmdBacklightOn) {
            R.getBacklight().on();
            removeCommand(Commands.cmdBacklightOn);
            addCommand(Commands.cmdBacklightOff);
        } else if (evt.getCommand() == Commands.cmdBacklightOff) {
            R.getBacklight().off();
            removeCommand(Commands.cmdBacklightOff);
            addCommand(Commands.cmdBacklightOn);
        } else {
            for (int i = 0; i < R.getContext().commands.length; i++) {
                if (evt.getCommand() == R.getContext().commands[i]) {
                    R.getContext().setTemporaryScreen("locify://navigation");
                    R.getURL().call(R.getContext().actions[i]);
                    return;
                }
            }
        }
    }
}
