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

package com.locify.client.route;

import com.locify.client.data.FileSystem;
import com.locify.client.locator.Location4D;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import java.util.Stack;
import java.util.Vector;

/**
 * Class that stores all variables of running route.
 * @author Menion
 */
public class RouteVariables {

    private static final String linSe = "\n";

    public static final String DESC_LENGTH = "Route length:";
    public static final String DESC_TRAVEL_TIME = "Route travel time:";
    public static final String DESC_POINTS = "Route points:";

    /** this number determinate which x-th location4d
     * have to be saved as actual location4d track */
    public static final int SAVED_COUNT_LOCATION = 1;
    /** save route point every x metres */
    public static final double MIN_STEP_DISTANCE = 10;
    /** max distance beetwen two stepas */
    public static final double MAX_STEP_DISTANCE = 10000;
    /** number of points and distances holded in memory */
    public static final int MAX_PAD = 30;

    /** start time of route */
    protected long startTime;
    /** total time of route */
    protected long routeTime;
    /** start of route pause */
    protected long pauseStart;
    /** total time reduction due to pause */
    protected long pauseTimeReduction;
    
    /** total distance of done route */
    protected double routeDist;
    /** maximal speed of this route */
    protected double speedMax;
    /** actual speed of this route */
    protected double speedActual;
    /** average speed of actual route */
    protected double speedAverage;
    
    /** actual (last) location4D */
    protected Location4D locationActual;
    /** lastky saved location4d ... (becouse of duplicates) */
    protected Location4D locationLastSaved;
    /** counting gained location4d */
    protected int numOfNewlocation;
    
    /** horizontal accuracy */
    protected float hdop;
    /** vertical accuracy */
    protected float vdop;
    
    /* state variables */
    protected boolean paused;
    protected boolean newData;

    /** vector containing location4D */
    protected Stack routePoints;
    /** vector containing distances for points */
    protected Stack routeDistances;

    /** counting saved location4d */
    private int pointsCount;

    /** state of actual routeBack */
    protected int state;

    /** data which have to be saved into file */
    private StringBuffer dataToSave;
    /** check if point was added and separating is possible */
    private boolean canSeparate;

    public static long bytesToSkipAtBegin;
    
    public RouteVariables() {
        resetAll();
    }
    
    protected void resetAll() {
        startTime = 0L;
        routeTime = 0L;
        pauseStart = 0L;
        pauseTimeReduction = 0L;

        routeDist = 0.0;
        speedActual = 0.0;
        speedAverage = 0.0;
        speedMax = 0.0;
        
        locationActual = null;
        locationLastSaved = null;
        numOfNewlocation = 0;

        hdop = 0;
        vdop = 0;
        
        paused = true;
        newData = true;

        routePoints = new Stack();
        routeDistances = new Stack();
        
        pointsCount = 0;

        state = RouteManager.ROUTE_STATE_NO_ACTION;

        dataToSave = new StringBuffer();
        canSeparate = false;

        bytesToSkipAtBegin = 0;
    }

    protected void routeTempSeparateLine() {
        dataAddSpace();
    }

    protected void routeTempSaveLocation(Location4D locationActual) {
//System.out.println("RouteVariables - routeTempSaveLocation(): " + locationActual.toString());
        if (routePoints.size() > 0) {
            routeDistances.push(new Double(locationActual.distanceTo(
                    (Location4D) routePoints.lastElement())));
        } else {
            routeDistances.push(new Double(0.0));
        }

        routePoints.push(locationActual);
        if (routePoints.size() > MAX_PAD) {
            routePoints.removeElementAt(0);
            routeDistances.removeElementAt(0);
        }

        dataAddPoint(locationActual);

        pointsCount++;
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public double getRouteDist() {
        return routeDist;
    }

    public long getRouteTime() {
        return routeTime;
    }

    public Vector getRoutePoints() {
        return routePoints;
    }

    protected void dataBegin() {
        dataToSave.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + linSe +
                "<kml xmlns=\"http://earth.google.com/kml/2.2\">" + linSe +
                "  <Placemark>" + linSe);
        bytesToSkipAtBegin = dataToSave.length();
        dataToSave.append(
                // important space for route name
                "                                                                            " + linSe +
                "    <MultiGeometry>" + linSe);
    }

    private void dataAddPoint(Location4D point) {
        if (!canSeparate)
            dataToSave.append(
                    "      <LineString>" + linSe +
                    "        <coordinates>" + linSe);
        dataToSave.append("          " +
                GpsUtils.formatDouble(point.getLongitude(), 6) + "," +
                GpsUtils.formatDouble(point.getLatitude(), 6) + "," +
                GpsUtils.formatDouble(point.getAltitude(), 2) + linSe);
        canSeparate = true;
    }

    private void dataAddSpace() {
        if (canSeparate)
            dataToSave.append(
                    "        </coordinates>" + linSe +
                    "      </LineString>" + linSe);
        canSeparate = false;
    }

    public void dataEnd(String routeName, String description) {
        if (description == null || description.length() == 0)
            description = "";
        else
            description = "      " + description;

        dataAddSpace();
        dataToSave.append(
                "    </MultiGeometry>" + linSe +
                "    <description>" + linSe +
                "      " + DESC_LENGTH + " " + GpsUtils.formatDistance(routeDist) + linSe +
                //"      " + DESC_TRAVEL_TIME + " " + GpsUtils.formatDouble(routeTime / 1000.0, 0) + linSe +
                "      " + DESC_TRAVEL_TIME + " " + routeTime + " ms" + linSe +
                "      " + DESC_POINTS + " " + pointsCount + linSe);
        if (!description.equals(""))
            dataToSave.append(description + linSe);

        dataToSave.append("    </description>" + linSe +
                "  </Placemark>" + linSe +
                "</kml>");

        dataFlush(false);

        dataToSave.append("    <name>" + routeName + "</name>");
        dataFlush(true);
    }

    public void dataFlush(boolean name) {
//System.out.println("Data to write: " + dataToSave);
        String data = dataToSave.toString();
        if (!data.equals("")) {
            if (name) {
                R.getFileSystem().saveStringToBytePos(FileSystem.RUNNING_TEMP_ROUTE, data, bytesToSkipAtBegin);
            } else {
                R.getFileSystem().saveStringToEof(FileSystem.RUNNING_TEMP_ROUTE, data);
            }
            dataToSave.delete(0, dataToSave.length());
        }
    }

    public void saveRouteVariables() {
        StringBuffer data = new StringBuffer();
        data.append(startTime + ";" + routeTime + ";" + pauseStart + ";" + pauseTimeReduction + ";" +
                routeDist + ";" + speedMax + ";" + speedActual + ";" + speedAverage + "\n");
        data.append(locationActual.getLatitude() + ";" + locationActual.getLongitude() + ";" + locationActual.getAltitude() + ";" + locationActual.getTime() + ";" +
                locationLastSaved.getLatitude() + ";" + locationLastSaved.getLongitude() + ";" + locationLastSaved.getAltitude() + ";" + locationLastSaved.getTime() + "\n");
        data.append(numOfNewlocation + ";" + hdop + ";" + vdop + ";" + paused + ";" +
                newData + ";" + pointsCount + ";" + state + ";" + bytesToSkipAtBegin + "\n");

        Location4D act;
        for (int i = 0; i < routePoints.size(); i++) {
            act = (Location4D) routePoints.elementAt(i);
            data.append(act.getLatitude() + ";" + act.getLongitude() + ";" + act.getAltitude() + ";" + act.getTime());
            if (i < routePoints.size() - 1)
                data.append(";");
        }
        data.append("\n");

        Double dist;
        for (int i = 0; i < routePoints.size(); i++) {
            dist = (Double) routeDistances.elementAt(i);
            data.append(dist);
            if (i < routeDistances.size() - 1)
                data.append(";");
        }
        data.append("\n");

        R.getFileSystem().saveString(FileSystem.RUNNING_ROUTE_VARIABLES, data.toString());
    }

    public RouteVariables loadRouteVariables() {
        try {
            String data = R.getFileSystem().loadString(FileSystem.RUNNING_ROUTE_VARIABLES);
            Vector lines = StringTokenizer.getVector(data, "\n");
            if (lines.size() < 4)
                return null;
            else {
                Vector token = StringTokenizer.getVector(String.valueOf(lines.elementAt(0)), ";");
                startTime = Long.parseLong(String.valueOf(token.elementAt(0)));
                routeTime = Long.parseLong(String.valueOf(token.elementAt(1)));
                pauseStart = Long.parseLong(String.valueOf(token.elementAt(2)));
                pauseTimeReduction = Long.parseLong(String.valueOf(token.elementAt(3)));
                routeDist = Double.parseDouble(String.valueOf(token.elementAt(4)));
                speedMax = Double.parseDouble(String.valueOf(token.elementAt(5)));
                speedActual = Double.parseDouble(String.valueOf(token.elementAt(6)));
                speedAverage = Double.parseDouble(String.valueOf(token.elementAt(7)));

                token = StringTokenizer.getVector(String.valueOf(lines.elementAt(1)), ";");
                locationActual = new Location4D(
                        Double.parseDouble(String.valueOf(token.elementAt(0))),
                        Double.parseDouble(String.valueOf(token.elementAt(1))),
                        Float.parseFloat(String.valueOf(token.elementAt(2))),
                        Long.parseLong(String.valueOf(token.elementAt(3))));

                locationLastSaved = new Location4D(
                        Double.parseDouble(String.valueOf(token.elementAt(4))),
                        Double.parseDouble(String.valueOf(token.elementAt(5))),
                        Float.parseFloat(String.valueOf(token.elementAt(6))),
                        Long.parseLong(String.valueOf(token.elementAt(7))));

                token = StringTokenizer.getVector(String.valueOf(lines.elementAt(2)), ";");
                numOfNewlocation = Integer.parseInt(String.valueOf(token.elementAt(0)));
                hdop = Float.parseFloat(String.valueOf(token.elementAt(1)));
                vdop = Float.parseFloat(String.valueOf(token.elementAt(2)));
                paused = (String.valueOf(token.elementAt(3)).equals("true"));
                newData = (String.valueOf(token.elementAt(4)).equals("true"));
                pointsCount = Integer.parseInt(String.valueOf(token.elementAt(5)));
                state = Integer.parseInt(String.valueOf(token.elementAt(6)));
                bytesToSkipAtBegin = Integer.parseInt(String.valueOf(token.elementAt(7)));

                token = StringTokenizer.getVector(String.valueOf(lines.elementAt(3)), ";");
                for (int i = 0; i < token.size(); i = i + 4) {
                    routePoints.push(new Location4D(
                        Double.parseDouble(String.valueOf(token.elementAt(i))),
                        Double.parseDouble(String.valueOf(token.elementAt(i + 1))),
                        Float.parseFloat(String.valueOf(token.elementAt(i + 2))),
                        Long.parseLong(String.valueOf(token.elementAt(i + 3)))
                            ));
                }

                if (lines.size() > 4 && String.valueOf(lines.elementAt(4)).length() > 0) {
                    token = StringTokenizer.getVector(String.valueOf(lines.elementAt(4)), ";");
                    for (int i = 0; i < token.size(); i++) {
                        routeDistances.push(new Double(
                                Double.parseDouble(String.valueOf(token.elementAt(i)))
                                ));
                    }
                }
            }
            return this;
        } catch (Exception ex) {
            Logger.error("RouteVariables.loadRouteVariables()");
            return null;
        }
    }

}
