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
import com.locify.client.gui.extension.FlowPanel;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ImprovedLabel;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.locator.Navigator;
import com.locify.client.locator.impl.WaypointNavigatorModel;
import com.locify.client.locator.impl.WaypointRouteNavigatorModel;
import com.locify.client.utils.Backlight;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.locify.client.utils.math.LMath;
import com.sun.lwuit.Container;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.GridLayout;
import java.io.IOException;
import javax.microedition.lcdui.game.GameCanvas;


/** 
 * Screen shows compas, GPS infomation, speed. In case of navigation displays navigation arrow
 * @author Jiri Stepan
 */
public class NavigationScreen extends FormLocify implements ActionListener, LocationEventListener {

    // components
    private Container mainCont;
    // top container
    private FlowPanel topPanel;
    private ImprovedLabel labelTime;
    private ImprovedLabel labelDate;
    // bootom container
    private FlowPanel bottomPanel;
    private ImprovedLabel labelDist;
    // left container
    private FlowPanel leftPanel;
    private ImprovedLabel labelSpeed;
    private ImprovedLabel labelHDOP;
    private ImprovedLabel labelVDOP;
    //right container
    private FlowPanel rightPanel;
    private ImprovedLabel labelLatitude;
    private ImprovedLabel labelLongitude;
    private ImprovedLabel labelAltitude;

    private static Navigator navigator;
    
    private Location4D location;
    // navigation angle (heading)
    public static double nAngle;
    // diference angle beetween heading and navigated point
    public static double dAngle;
    // angles images
    private Image[] numbers;

    
    private boolean smallRadius;
    private Backlight backLight;

    // temp items
    private boolean drawLock;
    // center of compas X coordinate
    private int cX;
    // center of compas Y coordinate
    private int cY;
    // compas radius
    private int radius;

    public NavigationScreen() {
        super(Locale.get("Navigation"));

        this.addCommand(Commands.cmdBack);
        this.addCommand(Commands.cmdHome);

        if (R.getBacklight().isOn()) {
            this.addCommand(Commands.cmdBacklightOff);
        } else {
            this.addCommand(Commands.cmdBacklightOn);
        }
        
        // i know about added gps ... actualy i'm lazy :)
        this.addCommand(new ParentCommand(Locale.get("Another_location"), null, R.getContext().commands));
        this.setCommandListener(this);

        smallRadius = false;
        
        try {
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

            setLayout(new BorderLayout());
            mainCont = new Container() {

                public void paint(Graphics g) {
                    try {
//                        super.paint(g);
                        if (drawLock) {
                            return;
                        }
                        drawLock = true;

//                        g.setColor(ColorsFonts.BLUE);
//                        g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());

                        cX = g.getClipWidth() / 2 + g.getClipX();
                        cY = g.getClipHeight() / 2 + g.getClipY();
                        radius = Math.min(g.getClipWidth(), g.getClipHeight()) / 2 - 5;
                        smallRadius = radius < 60 ? true : false;
                        
                        setCompas(g);
                        setArrow(g);

                        //actualize date and time
                        labelTime.setValue(Utils.getTime());
                        labelDate.setValue(Utils.getDate());
                        drawLock = false;
                    } catch (Exception e) {
                        R.getErrorScreen().view(e, "MapScreen.paint()", null);
                    }
                }
            };
            addComponent(BorderLayout.CENTER, mainCont);

            topPanel = new FlowPanel(BorderLayout.NORTH);
            topPanel.getContentPane().setLayout(new GridLayout(1, 2));

            labelTime = new ImprovedLabel(BorderLayout.NORTH);
            labelTime.setTitle(Locale.get("Time"));
            labelTime.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            topPanel.getContentPane().addComponent(labelTime);

            labelDate = new ImprovedLabel(BorderLayout.NORTH);
            labelDate.setTitle(Locale.get("Date"));
            labelDate.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            topPanel.getContentPane().addComponent(labelDate);

            addComponent(BorderLayout.NORTH, topPanel);

            bottomPanel = new FlowPanel(BorderLayout.SOUTH);
            bottomPanel.getContentPane().setLayout(new BorderLayout());

            labelDist = new ImprovedLabel(BorderLayout.NORTH);
            labelDist.setTitle(Locale.get("Distance"));
            labelDist.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            bottomPanel.getContentPane().addComponent(BorderLayout.CENTER, labelDist);

            addComponent(BorderLayout.SOUTH, bottomPanel);

            leftPanel = new FlowPanel(BorderLayout.WEST);
            leftPanel.getContentPane().setLayout(new GridLayout(3, 1));

            labelSpeed = new ImprovedLabel(BorderLayout.NORTH);
            labelSpeed.setTitle(Locale.get("Speed"));
            labelSpeed.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            leftPanel.getContentPane().addComponent(labelSpeed);

            labelHDOP = new ImprovedLabel(BorderLayout.NORTH);
            labelHDOP.setTitle(Locale.get("Hdop_route"));
            labelHDOP.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            leftPanel.getContentPane().addComponent(labelHDOP);

            labelVDOP = new ImprovedLabel(BorderLayout.NORTH);
            labelVDOP.setTitle(Locale.get("Vdop_route"));
            labelVDOP.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            leftPanel.getContentPane().addComponent(labelVDOP);

            addComponent(BorderLayout.WEST, leftPanel);

            rightPanel = new FlowPanel(BorderLayout.EAST);
            rightPanel.getContentPane().setLayout(new GridLayout(3, 1));

            labelLatitude = new ImprovedLabel(BorderLayout.NORTH);
            labelLatitude.setTitle(Locale.get("Latitude"));
            labelLatitude.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            rightPanel.getContentPane().addComponent(labelLatitude);

            labelLongitude = new ImprovedLabel(BorderLayout.NORTH);
            labelLongitude.setTitle(Locale.get("Longitude"));
            labelLongitude.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            rightPanel.getContentPane().addComponent(labelLongitude);

            labelAltitude = new ImprovedLabel(BorderLayout.NORTH);
            labelAltitude.setTitle(Locale.get("Altitude"));
            labelAltitude.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);
            rightPanel.getContentPane().addComponent(labelAltitude);

            addComponent(BorderLayout.EAST, rightPanel);
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

            locationChanged(null, location);
            if (R.getSettings().getBacklight() == SettingsData.NAVIGATION || R.getSettings().getBacklight() == SettingsData.MAP_NAVIGATION) {
                R.getBacklight().on();
                removeCommand(Commands.cmdBacklightOn);
                addCommand(Commands.cmdBacklightOff);
            }

            revalidate();
            show();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NavigationScreen.view", null);
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

    /**
     * Function which rotate arrow and compas (angles in degrees)
     * @param nAngle new angle for compas north
     * @param dAngle new angle for arrow
     */
    public void moveAngles(final double nAngle, final double dAngle) {
        boolean move = true;
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
                            mainCont.repaint();
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        NavigationScreen.nAngle = nAngle;
                        NavigationScreen.dAngle = dAngle;
                        mainCont.repaint();
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
                labelDist.setValue(GpsUtils.formatDistance(navigator.getDistanceToTarget(location)));
            }
            moveAngles(angleN, angleD);

            labelHDOP.setValue(GpsUtils.formatDouble(R.getLocator().getAccuracyHorizontal(), 1));
            labelVDOP.setValue(GpsUtils.formatDouble(R.getLocator().getAccuracyVertical(), 1));
            labelSpeed.setValue(GpsUtils.formatSpeed(R.getLocator().getSpeed()));
            labelLatitude.setValue(GpsUtils.formatLatitude(location.getLatitude(), R.getSettings().getCoordsFormat()));
            labelLongitude.setValue(GpsUtils.formatLongitude(location.getLongitude(), R.getSettings().getCoordsFormat()));
            labelAltitude.setValue(GpsUtils.formatDouble(location.getAltitude(), 1) + "m");

//Logger.debug("NS (" + System.currentTimeMillis() + "), lat: " + location.getLatitude() +
//        ", lon: " + location.getLongitude() + ", angleN: " + angleN + ", angleD: " + angleD);
            repaint();
        }
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
            case GameCanvas.KEY_NUM2:
                topPanel.switchVisibility();
                break;
            case GameCanvas.KEY_NUM4:
                leftPanel.switchVisibility();
                break;
            case GameCanvas.KEY_NUM6:
                rightPanel.switchVisibility();
                break;
            case GameCanvas.KEY_NUM8:
                bottomPanel.switchVisibility();
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
