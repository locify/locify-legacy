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

import com.locify.client.data.FileSystem;
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
import com.locify.client.locator.Navigator;
import com.locify.client.locator.impl.WaypointNavigatorModel;
import com.locify.client.locator.impl.WaypointRouteNavigatorModel;
import com.locify.client.utils.Backlight;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import javax.microedition.lcdui.game.GameCanvas;

/** 
 * Screen shows compas, GPS infomation, speed. In case of navigation displays navigation arrow
 * @author Jiri Stepan
 */
public class NavigationScreen extends FormLocify implements
        ActionListener, LocationEventListener {

    // actual navigator
    private static Navigator navigator;
    private Location4D location;
    private Backlight backLight;

    public NavigationScreen() {
        super(Locale.get("Navigation"));
        try {
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

            R.getLocator().addLocationChangeListener(this);

            initializeSkins(FileSystem.SKINS_FOLDER_NAVIGATION);
            registerBackgroundListener();
            
//            labelTime.setTitle(IconData.getLocalImage("alarm"));
//            labelDate.setTitle(IconData.getLocalImage("calendar"));
//            labelDist.setTitle(IconData.getLocalImage("forward"));
//            labelSpeed.setTitle(Locale.get("Speed"));
//            labelHDOP.setTitle(Locale.get("Hdop_route"));
//            labelVDOP.setTitle(Locale.get("Vdop_route"));
//            labelLatitude.setTitle(Locale.get("Latitude"));
//            labelLongitude.setTitle(Locale.get("Longitude"));
//            labelAltitude.setTitle(Locale.get("Altitude"));
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "NavigationScreen.constructor()", null);
        }
    }

    public void view() {
        try {
            //init variables
            location = R.getLocator().getLastLocation();
            selectedWidget = null;

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

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        if (this.isVisible() || sender == null) {
            this.location = location;

            double angleN = R.getLocator().getHeading();
            double angleD = 0;
            if (navigator != null) {
                setTitle(navigator.getToName());
                angleD = navigator.getAzimuthToTaget(location) - angleN;
                if (slDist != null) {
                    slDist.setValue(GpsUtils.formatDistance(navigator.getDistanceToTarget(location)));
                }
            }
            if (compass != null) {
                compass.moveAngles(angleN, angleD);
            }
            if (slAlt != null) {
                slAlt.setValue(GpsUtils.formatDouble(location.getAltitude(), 1) + "m");
            }
            if (slHdop != null) {
                slHdop.setValue(GpsUtils.formatDouble(R.getLocator().getAccuracyHorizontal(), 1));
            }
            if (slLat != null) {
                slLat.setValue(GpsUtils.formatLatitude(location.getLatitude(), R.getSettings().getCoordsFormat()));
            }
            if (slLon != null) {
                slLon.setValue(GpsUtils.formatLongitude(location.getLongitude(), R.getSettings().getCoordsFormat()));
            }
            if (slSpeedAct != null) {
                slSpeedAct.setValue(GpsUtils.formatSpeed(R.getLocator().getSpeed()));
            }
            if (slVdop != null) {
                slVdop.setValue(GpsUtils.formatDouble(R.getLocator().getAccuracyVertical(), 1));
            }

//Logger.debug("NS (" + System.currentTimeMillis() + "), lat: " + location.getLatitude() +
//        ", lon: " + location.getLongitude() + ", angleN: " + angleN + ", angleD: " + angleD);
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
            case GameCanvas.KEY_NUM1:
                switchLeftPanelVisibility();
                break;
            case GameCanvas.KEY_NUM5:
                widgetAction(selectedWidget);
                break;
            case GameCanvas.KEY_NUM4:
                selectPreviousWidget();
                repaint();
                break;
            case GameCanvas.KEY_NUM6:
                selectNextWidget();
                repaint();
                break;
            case GameCanvas.KEY_NUM7:
                if (R.getBacklight().isOn()) {
                    R.getBacklight().off();
                } else {
                    R.getBacklight().on();
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
