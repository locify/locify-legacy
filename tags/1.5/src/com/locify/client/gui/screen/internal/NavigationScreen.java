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
import com.locify.client.locator.LocationContext;
import java.io.IOException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.utils.GpsUtils;

import com.locify.client.locator.Navigator;
import com.locify.client.locator.impl.WaypointNavigatorModel;
import com.locify.client.locator.impl.WaypointRouteNavigatorModel;
import com.locify.client.route.ScreenItem;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Logger;
import com.locify.client.utils.Commands;
import de.enough.polish.util.Locale;
import com.locify.client.utils.R;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.ArrayList;
import javax.microedition.lcdui.Image;

/** 
 * Screen shows compas, GPS infomation, speed. In case of navigation displays navigation arrow
 * @author Jiri Stepan
 */
public class NavigationScreen extends Form implements CommandListener, LocationEventListener {

    private static final int VIEW_MODE_MORE = 0;
    private static final int VIEW_MODE_LESS = 1;
    private int view_mode;
    private Command cmdMore;
    private Command cmdLess;
    private static Navigator navigator;
    private boolean drawLock;    // screen items
    private Location4D location;
    // navigation angle (heading)
    public static double nAngle;
    // diference angle beetween heading and navigated point
    public static double dAngle;    // items to display
    private ArrayList items;
    private ScreenItem tempItem;
    private static final int ITEM_DISTANCE = 0;
    private static final int ITEM_SPEED = 1;
    private static final int ITEM_LATITUDE = 2;
    private static final int ITEM_LONGITUDE = 3;
    private static final int ITEM_ALTITUDE = 4;
    private static final int ITEM_ACCURACY = 5;
    // angles images
    private Image[] numbers;
    // center of compas
    private int cX;
    private int cY;
    // compas radius
    private int radius;
    private boolean smallRadius;
    private static int BOTTOM_MARGIN;
    private static int TOP_MARGIN;

    public NavigationScreen() {
        ////#style .navigationScreen
        super(Locale.get("Navigation"));
        try {
            cmdMore = new Command(Locale.get("Dock_more"), Command.ITEM, 2);
            cmdLess = new Command(Locale.get("Navi-less"), Command.ITEM, 3);

            smallRadius = false;

            addCommand(cmdMore);
            addCommand(Commands.cmdBack);
            //#style imgMore
            addCommand(cmdMore);
            //#style imgHome
            addCommand(Commands.cmdHome);
            //another location commands
            addCommand(Commands.cmdAnotherLocation);
            for (int i = 0; i < R.getContext().commands.length; i++) {
                if (i != LocationContext.GPS) {
                    UiAccess.addSubCommand(R.getContext().commands[i], Commands.cmdAnotherLocation, this);
                }
            }

            setCommandListener(this);

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
            R.getMidlet().switchDisplayable(null, this);
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
            items = new ArrayList();

            // ITEM_ROUTE_TIME
            ScreenItem button00 = new ScreenItem(Locale.get("Distance"));
            //button00.setTextValue(" ");
            items.add(button00);
            // ITEM_ROUTE_DIST
            ScreenItem button01 = new ScreenItem(Locale.get("Speed"));
            items.add(button01);
            // ITEM_SPEED_MAX
            ScreenItem button02 = new ScreenItem(Locale.get("Latitude"));
            items.add(button02);
            // ITEM_SPEED_AVERAGE
            ScreenItem button03 = new ScreenItem(Locale.get("Longitude"));
            items.add(button03);
            // ITEM_SPEED_ACTUAL
            ScreenItem button04 = new ScreenItem(Locale.get("Altitude"));
            items.add(button04);
            // ITEM_LATITUDE
            ScreenItem button05 = new ScreenItem(Locale.get("Accuracy"));
            items.add(button05);

            for (int i = 0; i < items.size(); i++) {
                tempItem = (ScreenItem) items.get(i);
                tempItem.setTextValue(" ");
                //#if polish.Vendor == WM-big
                tempItem.setFont(ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                //#else
//#                 tempItem.setFont(ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_10_BLACK);
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
        MultiGeoData mgd = GeoFiles.parseKmlFile(fileName, false);
        for (int i = 0; i < mgd.getDataSize(); i++) {
            view(mgd.getGeoData(i));
        }
//        int type = GeoFiles.getDataTypeFile(fileName);
//        if (type == GeoFiles.TYPE_WAYPOINT) {
//            view(GeoFiles.loadWaypointFile(fileName));
//        } else if (type == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
//            view(GeoFiles.loadWaypointsCloudFile(fileName));
//        } else if (type == GeoFiles.TYPE_ROUTE) {
//            view(GeoFiles.loadRouteFile(fileName, false));
//        }
    }

    /**
     * Determines file type and start navigation to it
     * @param fileName
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

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd.equals(Commands.cmdBack)) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getBack().goBack();
        } else if (cmd.equals(Commands.cmdHome)) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getURL().call("locify://mainScreen");
        } else if (cmd.equals(cmdMore)) {
            setMode(VIEW_MODE_MORE);
            removeCommand(cmdMore);
            addCommand(cmdLess);
        } else if (cmd.equals(cmdLess)) {
            setMode(VIEW_MODE_LESS);
            removeCommand(cmdLess);
            addCommand(cmdMore);
        } else {
            for (int i = 0; i < R.getContext().commands.length; i++) {
                if (cmd == R.getContext().commands[i]) {
                    R.getContext().setTemporaryScreen("locify://navigation");
                    R.getURL().call(R.getContext().actions[i]);
                    return;
                }
            }
        }
    }

    public void paint(Graphics gr) {
        super.paint(gr);
        if (drawLock || isMenuOpened()) {
            return;
        }
        this.drawLock = true;

        if (gr.getClipHeight() > 40) {
            setCompas(gr);
            setArrow(gr);
            for (int i = 0; i < items.size(); i++) {
                ((ScreenItem) items.get(i)).paint(gr);
            }
        } else {
            TOP_MARGIN = gr.getClipHeight(); //nastavi dle pokusi o vyhresleni v TopBarBackground
        }
        drawLock = false;
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

            a = (i * 10 + nAngle) / GpsUtils.RHO;

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

                g.drawImage(numbers[i / 3], cX + x1, cY - y1, Graphics.VCENTER | Graphics.HCENTER);
            }
        }
    }

    private void setArrow(Graphics g) {
        if (navigator != null) {
            g.setColor(0, 0, 0);

            double a;
            int x1, x2, x3, x4, y1, y2, y3, y4;

            a = dAngle / GpsUtils.RHO;
            x1 = (int) (Math.sin(a) * radius * 0.65);
            y1 = (int) (Math.cos(a) * radius * 0.65);

            a = (dAngle + 180) / GpsUtils.RHO;
            x2 = (int) (Math.sin(a) * (radius * 0.2));
            y2 = (int) (Math.cos(a) * (radius * 0.2));

            a = (dAngle + 140) / GpsUtils.RHO;
            x3 = (int) (Math.sin(a) * (radius * 0.5));
            y3 = (int) (Math.cos(a) * (radius * 0.5));

            a = (dAngle + 220) / GpsUtils.RHO;
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
        view_mode = mode;
        cX = getWidth() / 2;

        for (int i = 0; i < items.size(); i++) {
            ((ScreenItem) items.get(i)).setVisible(false);
        }

        if (view_mode == VIEW_MODE_LESS) {
            cY = (getHeight() - 60 - TOP_MARGIN - BOTTOM_MARGIN) / 2;
            radius = Math.min(cX, cY) - 5;
            cY = cY + TOP_MARGIN + 10;

            tempItem = (ScreenItem) items.get(ITEM_DISTANCE);
            //#if polish.Vendor == WM-big
            tempItem.setFont(ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_20_BLACK);
            //#else
//#             tempItem.setFont(ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
            //#endif
            tempItem.setSizePos(10, getHeight() - 45 - BOTTOM_MARGIN,
                    getWidth() - 20, 35);
            tempItem.setVisible(true);
        } else if (view_mode == VIEW_MODE_MORE) {
            cY = (getHeight() - 100 - TOP_MARGIN - BOTTOM_MARGIN) / 2;
            radius = Math.min(cX, cY) - 5;
            cY = cY + TOP_MARGIN + 10;

            int space = 5;
            int sizeX = (getWidth() - 4 * space) / 3;

            if (radius < 50) {
                cY = (getHeight() - 60 - TOP_MARGIN - BOTTOM_MARGIN) / 2;
                radius = Math.min(cX, cY) - 5;
                cY = cY + TOP_MARGIN + 10;

                ((ScreenItem) items.get(ITEM_DISTANCE)).setSizePos(space, getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.get(ITEM_SPEED)).setSizePos(2 * space + sizeX, getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.get(ITEM_ACCURACY)).setSizePos(3 * space + 2 * sizeX, getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
            } else {
                ((ScreenItem) items.get(ITEM_DISTANCE)).setSizePos(space, getHeight() - 80 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.get(ITEM_SPEED)).setSizePos(2 * space + sizeX, getHeight() - 80 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.get(ITEM_ACCURACY)).setSizePos(3 * space + 2 * sizeX, getHeight() - 80 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.get(ITEM_LATITUDE)).setSizePos(space, getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.get(ITEM_LONGITUDE)).setSizePos(2 * space + sizeX, getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
                ((ScreenItem) items.get(ITEM_ALTITUDE)).setSizePos(3 * space + 2 * sizeX, getHeight() - 40 - BOTTOM_MARGIN, sizeX, 30);
            }

            //#if polish.Vendor == WM-big
            ((ScreenItem) items.get(ITEM_DISTANCE)).setFont(
                    ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
            ((ScreenItem) items.get(ITEM_SPEED)).setFont(
                    ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
            //#else
//#             ((ScreenItem) items.get(ITEM_DISTANCE)).setFont(
//#                     ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#             ((ScreenItem) items.get(ITEM_SPEED)).setFont(
//#                     ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
            //#endif

            for (int i = 0; i < items.size(); i++) {
                ((ScreenItem) items.get(i)).setVisible(true);
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

        if (move) {
            final double nDiff = getDiffAngle(NavigationScreen.nAngle, nAngle);
            final double dDiff = getDiffAngle(NavigationScreen.dAngle, dAngle);
            final int numOfSteps = 5;

            Thread thread = new Thread(new Runnable() {

                public void run() {
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
                }
            });

            if (nDiff != 0 || dDiff != 0) {
                thread.start();
            }
        } else {
            NavigationScreen.nAngle = nAngle;
            NavigationScreen.dAngle = dAngle;
            repaint();
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
        this.location = location;

        double angleN = R.getLocator().getHeading();
        double angleD = 0;
        if (navigator != null) {
            setTitle(navigator.getToName());
            angleD = navigator.getAzimuthToTaget(location) - angleN;
            actualizeItem(ITEM_DISTANCE, GpsUtils.formatDistance(navigator.getDistanceToTarget(location)));
        }
        moveAngles(angleN, angleD);

        nAngle = angleN;
        dAngle = angleD;

        actualizeItem(ITEM_ACCURACY, GpsUtils.formatDouble(R.getLocator().getAccuracyHorizontal(), 1));
        actualizeItem(ITEM_SPEED, GpsUtils.formatSpeed(R.getLocator().getSpeed()));
        actualizeItem(ITEM_ALTITUDE, GpsUtils.formatDouble(R.getLocator().getLastLocation().getAltitude(), 1) + "m");
        actualizeItem(ITEM_LATITUDE, GpsUtils.formatLatitude(
                R.getLocator().getLastLocation().getLatitude(), R.getSettings().getCoordsFormat()));
        actualizeItem(ITEM_LONGITUDE, GpsUtils.formatLongitude(
                R.getLocator().getLastLocation().getLongitude(), R.getSettings().getCoordsFormat()));

        repaint();
    }

    private void actualizeItem(int item, String value) {
        tempItem = (ScreenItem) items.get(item);
        tempItem.setTextValue(value);
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
        repaint();
    }

    public static boolean isRunning() {
        return !(navigator == null);
    }
}
