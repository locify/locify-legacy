/*
 * RouteManager.java
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
package com.locify.client.route;

import com.locify.client.data.FileSystem;
import com.locify.client.data.items.GeoFiles;
import com.locify.client.gui.polish.TopBarBackground;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;

/**
 * Manages startin, pausing, stopping and recording a route
 * @author Menion
 */
public class RouteManager implements LocationEventListener {

    /** Set if new route without data or stopped route */
    public static final int ROUTE_STATE_NO_ACTION = 0;
    public static final int ROUTE_STATE_RUNNING = 1;
    public static final int ROUTE_STATE_UNFINISHED_ROUTE = 2;
    public static final int ROUTE_STATE_PAUSED = 3;
    private Thread thread;
    private long sleepedTime = 0;
    private int routeSaveUnfinishedTester = 0;
    /* working variables */
    private double stepDistance = 0;
//    private RouteDataIO routeData;
    /** actual date */
    private String actualDate = "";
    private RouteVariables rv;

    public RouteManager() {
        rv = new RouteVariables();
        setState(ROUTE_STATE_NO_ACTION);

        R.getLocator().addLocationChangeListener(this);
        start();
    }

    private void start() {
        thread = new Thread(new Runnable() {

            public void run() {
                try {
                    while (true) {

                        actualizeRouteTime();

                        if (sleepedTime > 1000 && isRunning()) {
                            if (R.getRouteScreen().isShown()) {
                                actualDate = Utils.getActualDate();
                            }

                            // every 3 seconds
                            if (routeSaveUnfinishedTester % 3 == 0) {
                                if (R.isMapScreenInitialized() && R.getMapScreen().isShown()) {
                                    R.getMapScreen().showActualRoute(rv);
                                }

                                rv.dataFlush(false);
                            }

                            if (routeSaveUnfinishedTester % 15 == 0) {
                                saveUnfinishedRoute();
                                routeSaveUnfinishedTester = 0;
                            }

                            routeSaveUnfinishedTester++;
                            sleepedTime = 0;
                        } else {
                            sleepedTime += 100;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    R.getErrorScreen().view(e, "RouteManager.run", null);
                }
            }
        });
        thread.start();
    }

    public void routeStart() {
        if (rv.startTime == 0L) {
            routeReset();
            rv.dataBegin();
            rv.startTime = System.currentTimeMillis();
            rv.pauseStart = rv.startTime;
        //routeVariables.locationActual = R.getLocator().getLastLocation();
        }

        if (rv.paused) {
            //System.out.println("routeVariables.pauseTimeReduction " + routeVariables.pauseTimeReduction);
            //System.out.println("routeVariables.pauseStart " + routeVariables.pauseStart);
            //System.out.println("System.currentTimeMillis() " + System.currentTimeMillis());
            rv.pauseTimeReduction =
                    rv.pauseTimeReduction + (System.currentTimeMillis() - rv.pauseStart);
            //System.out.println("routeVariables.pauseTimeReduction " + routeVariables.pauseTimeReduction);
            rv.paused = false;
            setState(ROUTE_STATE_RUNNING);
        }

        addLocationActual(true);
    }

    /**
     * This is usefull for pausing and unpausing !!
     */
    public void routePause(boolean save) {
        //System.out.println("Route: paused");
        if (!rv.paused) {
            rv.pauseStart = System.currentTimeMillis();
            rv.paused = true;

            addLocationActual(true);
            rv.routeTempSeparateLine();

            setState(ROUTE_STATE_PAUSED);
//        } else if (!routeBD.readyToReset && routeBD.startTime != 0L) {
        } else if (rv.startTime != 0L && !save) {
            routeStart();
        }

        if (save) {
            R.getRouteSaveScreen().viewSaveScreen(rv);
        }
    }

    public void routeReset() {
        rv.resetAll();
        removeFileRouteVariables();
        removeFileRouteTemp();
        setState(ROUTE_STATE_NO_ACTION);
    }

    /**
     * Save Location4D last location into track Vector for every
     * savedCountLocation (variable) measure
     * @param force force save last availeable location
     */
    private void addLocationActual(boolean force) {
//System.out.println("RouteManager - addLocationActual() " + (rv.locationLastSaved == null) + " " +
//        (rv.locationLastSaved != rv.locationActual) + " " + (rv.locationActual == null));

        if (rv.locationLastSaved == null || rv.locationLastSaved != rv.locationActual) {
            if (rv.locationActual != null) {
                if (rv.numOfNewlocation % RouteVariables.SAVED_COUNT_LOCATION == 0 || force) {
                    rv.routeTempSaveLocation(rv.locationActual);
                    rv.locationLastSaved = rv.locationActual;
                }
                if (force) {
                    rv.numOfNewlocation = 1;
                } else {
                    rv.numOfNewlocation++;
                }
            }
        }
    }

    public boolean isRunning() {
        return !rv.paused;
    }

    public boolean isNewData() {
        return rv.newData;
    }

    public void setNewData(boolean newData) {
        this.rv.newData = newData;
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
//System.out.println("RouteManager - locationChanged() " + location.toString());
        if (location.getLatitude() == 0.0 || location.getLongitude() == 0.0) {
            return;
        }

        rv.hdop = R.getLocator().getAccuracyHorizontal();
        rv.vdop = R.getLocator().getAccuracyVertical();
        rv.speedActual = R.getLocator().getSpeed();

        if (isRunning()) {
            if (rv.locationActual != null) {
                stepDistance = rv.locationActual.distanceTo(location);
//System.out.println("RouteManager - locationChanged() stepDistance: " + stepDistance);
                if (stepDistance > RouteVariables.MIN_STEP_DISTANCE &&
                        stepDistance < RouteVariables.MAX_STEP_DISTANCE) {
                    rv.locationActual = location.getLocation4DCopy();

                    rv.routeDist = rv.routeDist + stepDistance;
                    rv.speedMax = (rv.speedMax > rv.speedActual) ? rv.speedMax : rv.speedActual;
                    addLocationActual(false);
                }
            } else {
                rv.locationActual = location;
            }

            rv.speedAverage = rv.routeDist / (rv.routeTime / 1000);
            setNewData(true);
        }
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
    }

    private long actualizeRouteTime() {
        if (isRunning()) {
            rv.routeTime = System.currentTimeMillis() - rv.startTime - rv.pauseTimeReduction;
            return rv.routeTime;
        } else {
            return rv.routeTime;
        }
    }

    /******************************************************/
    /*             ITEMS FOR ACTUALIZATION                */
    /******************************************************/
    public long getRouteTime() {
        return rv.routeTime;
    }

    public double getRouteDist() {
        return rv.routeDist;
    }

    public double getSpeed() {
        return rv.speedActual;
    }

    public double getSpeedAverage() {
        return rv.speedAverage;
    }

    public double getSpeedMax() {
        return rv.speedMax;
    }

    public float getHdop() {
        return rv.hdop;
    }

    public float getVdop() {
        return rv.vdop;
    }

    public String getActualDate() {
        return actualDate;
    }

    public double getLatitude() {
        return R.getLocator().getLastLocation().getLatitude();
    }

    public double getLongitude() {
        return R.getLocator().getLastLocation().getLongitude();
    }

    public double getAltitude() {
        return R.getLocator().getLastLocation().getAltitude();
    }

    public int getState() {
        return rv.state;
    }

    private void setState(int state) {
        rv.state = state;
        TopBarBackground.setRouteStatus(state);
    }

    public RouteVariables getRouteVariables() {
        return rv;
    }

    public boolean loadUnfinishedRoute() {
        try {
            if (GeoFiles.isUnfinishedRoute()) {
                //routeVariables = (RouteVariables) R.getFileSystem().loadObject(FileSystem.RUNNING_ROUTE_DATA);
                rv = (new RouteVariables()).loadRouteVariables();
                if (rv == null) {
                    rv = new RouteVariables();
                    setState(ROUTE_STATE_NO_ACTION);
                    return false;
                } else {
                    // its due to possibility to update routeScreen data and buttons
                    rv.newData = true;
                    // fix pause time if locify ends without correct exit
                    if (rv.paused != true) {
                        rv.pauseStart = rv.startTime + rv.routeTime + rv.pauseTimeReduction;
                        rv.paused = true;
                    }
                    setState(ROUTE_STATE_PAUSED);

                    return true;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "RouteBack.loadUnfinishedRoute", null);
            return false;
        }
    }

    public void saveUnfinishedRoute() {
        if (rv.routePoints.size() > 0) {
            removeFileRouteVariables();
            //R.getFileSystem().saveObject(FileSystem.RUNNING_ROUTE_DATA, routeVariables);
            rv.saveRouteVariables();
        }
    }

    private void removeFileRouteVariables() {
        R.getFileSystem().delete(FileSystem.RUNNING_ROUTE_VARIABLES);
    }

    private void removeFileRouteTemp() {
        R.getFileSystem().delete(FileSystem.RUNNING_TEMP_ROUTE);
    }
}
