/*
 * KalmanLocationFilter.java
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

package com.locify.client.locator.impl;

import com.locify.client.data.FileSystem;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationFilter;
import com.locify.client.locator.LocationSample;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.math.Matrix;
import java.util.Vector;

/**
 * Kalman location filter
 * @see http://en.wikipedia.org/wiki/Kalman_filter
 * @author Menion
 */
public class KalmanLocationFilter implements LocationFilter {

    LocationSample lastSample;

    /* variables required for filtering */
    private Matrix state1,  state2,  state2pre;
    private Matrix A,  R,  H,  K,  P,  Ppre,  Q;
    private long fTime1,  fTime2;
    private double dTime;
    private double fHdop1,  fHdop2;
    private float lastCourse;
    private float lastSpeed = 0;
    private double lastMeasLat = 0.0;
    private double lastMeasLon = 0.0;
    
    /* variables for debugging mode */
    private boolean debugMode = false;
    private int numOfTest = 100;
    int index1 = 1000;
    private String linSe = "\n";
    private long startTime = 0;
    private Vector dataMeas,  dataResult,  dataPred;

    /* number of averaged measure when standing on same place */
    private int numOfAverageMeasure = 5;
    /** temp variables used for averaging */
    private boolean weightedAverage = false;
    private double waValueLat,  waValueLon,  waWeight;
    private Vector oldMeas;
    private Measure measure;

    public KalmanLocationFilter() {
        initializeMatrix();
        oldMeas = new Vector(numOfAverageMeasure);
        if (debugMode) {
            dataMeas = new Vector(100);
            dataPred = new Vector(100);
            dataResult = new Vector(100);
        }
    }

    /**
     * Add actual measure to filtering
     * @param locSamp actual measure
     */
    public void addLocationSample(LocationSample locSamp) {
if (debugMode) {
    Logger.log("KLF new lat: " + locSamp.getLatitude() + " lon: " + locSamp.getLongitude());
    if (lastSample != null)
        Logger.log("    old lat: " + lastSample.getLatitude() + " lon: " + lastSample.getLongitude());
}

        fTime2 = System.currentTimeMillis();
        checkMatrix();
        if (lastSample != null &&
                // nulls coordinates
                (locSamp.getLatitude() != 0.0 && locSamp.getLongitude() != 0.0) &&
                // same coordinates as measure last time
                ((Math.abs(lastMeasLat - locSamp.getLatitude()) < 0.000001 &&
                Math.abs(lastMeasLon - locSamp.getLongitude()) < 0.000001)
                ||
                // same coordinates as filtered (NMEA parser)
                (Math.abs(lastSample.getLatitude() - locSamp.getLatitude()) < 0.000001 &&
                Math.abs(lastSample.getLongitude() - locSamp.getLongitude()) < 0.000001)))
            return;

        lastMeasLat = locSamp.getLatitude();
        lastMeasLon = locSamp.getLongitude();
        lastSample = locSamp;

if (debugMode) {
    if (startTime == 0) {
        startTime = System.currentTimeMillis();
    }
    index1++;

    dataMeas.addElement(new Measure(lastSample.getLatitude(),
            lastSample.getLongitude(), lastSample.getHorizontalAccuracy()));
}

        // first move in kalman filter
        if (state1.get(0, 0) == 0.0 || state1.get(2, 0) == 0.0) {
            state1.set(0, 0, lastSample.getLatitude());
            state1.set(1, 0, 0.0);
            state1.set(2, 0, lastSample.getLongitude());
            state1.set(3, 0, 0.0);

            fTime1 = fTime2;
            fHdop1 = lastSample.getHorizontalAccuracy();

if (debugMode) {
    dataPred.addElement(new Measure(lastSample.getLatitude(),
            lastSample.getLongitude(), lastSample.getHorizontalAccuracy()));
}
        } else {
            fHdop2 = lastSample.getHorizontalAccuracy();

            /* distance beetwen last and actual point */
            float lastDist = GpsUtils.computeDistance(state1.get(0, 0), state1.get(2, 0),
                    lastSample.getLatitude(), lastSample.getLongitude());

            /* error distance ... not perfectly but enough precise */
            float lastMaxErrorDist = (float) Math.sqrt((fHdop1 + fHdop2) / 4);
            //lastMaxErrorDist /= 10;
            lastMaxErrorDist /= 5;

if (debugMode) {
    Logger.log("    HDOP1 " + GpsUtils.formatDouble(fHdop1, 1) +
            "  HDOP2 " + GpsUtils.formatDouble(fHdop2, 1));
    Logger.log("    lastD " + GpsUtils.formatDouble(lastDist, 2) +
            "  lastME " + GpsUtils.formatDouble(lastMaxErrorDist, 2));
}

            //lastMaxErrorDist = 0;
            if (lastDist <= lastMaxErrorDist) {
if (debugMode) {
    Logger.log("    ***** average *****");
    dataPred.addElement(new Measure(lastSample.getLatitude(),
            lastSample.getLongitude(), lastSample.getHorizontalAccuracy()));
}
                if (!weightedAverage) {
                    oldMeas.removeAllElements();
                }

                if (lastSample.getHorizontalAccuracy() < 1)
                    lastSample.setHorizontalAccuracy(1.0);
                
                measure = new Measure(lastSample.getLatitude(),
                        lastSample.getLongitude(),
                        1 / lastSample.getHorizontalAccuracy());

                /* weightened average */
                if (oldMeas.size() == numOfAverageMeasure) {
                    oldMeas.removeElementAt(0);
                }
                oldMeas.addElement(measure);

                waValueLat = waValueLon = waWeight = 0;
                for (int i = 0; i < oldMeas.size(); i++) {
                    measure = (Measure) oldMeas.elementAt(i);
                    waValueLat = waValueLat + measure.getLat() * measure.getWeight();
                    waValueLon = waValueLon + measure.getLon() * measure.getWeight();
                    waWeight = waWeight + measure.getWeight();
                }

                lastSample.setLatitude(waValueLat / waWeight);
                lastSample.setLongitude(waValueLon / waWeight);
                lastSpeed = 0.0f;
                lastSample.setSpeed(lastSpeed);
                lastSample.setCourse(lastCourse);

                state1.set(0, 0, lastSample.getLatitude());
                state1.set(1, 0, 0.0);
                state1.set(2, 0, lastSample.getLongitude());
                state1.set(3, 0, 0.0);

                weightedAverage = true;
            } else {
if (debugMode)
    Logger.log("    ***** kalman *****");
                kalmanFilter();
                weightedAverage = false;
            }

            fTime1 = fTime2;
            fHdop1 = fHdop2;
        }

if (debugMode) {
    dataResult.addElement(new Measure(lastSample.getLatitude(),
            lastSample.getLongitude(), lastSample.getHorizontalAccuracy()));

    //Logger.log("dataMeas: " + dataMeas.size() + "  dataResu: " + dataResult.size());

    if (index1 == (1000 + numOfTest)) {
        saveDXF();
        //saveLog(dataMeas);
        debugMode = false;
        Logger.log("Sucesfully saved " + (index1 - 1000) + " ... end");
        System.exit(1);
    }
}
    }

    /**
     * Get last filtered measure
     * @return filtered measure
     */
    public Location4D getFilteredLocation() {
        return new Location4D(lastSample.getLatitude(), lastSample.getLongitude(), (float) lastSample.getAltitude());
    }
    
    public float getFilteredCourse() {
        //return (float) lastSample.getCourse();
        return lastCourse;
    }
    
    public float getFilteredSpeed() {
        //return (float) lastSample.getSpeed();
        return lastSpeed;
    }

    /**
     * Algorithm for Kalman style filtering
     */
    private void kalmanFilter() {
        fHdop2 = lastSample.getHorizontalAccuracy();
        if (fHdop2 < 1.0)
            fHdop2 = 1.0;
        
        dTime = (fTime2 - fTime1) / 1000.0;
        state2.set(0, 0, lastSample.getLatitude());
        state2.set(1, 0, (state2.get(0, 0) - state1.get(0, 0)) / dTime);
        state2.set(2, 0, lastSample.getLongitude());
        state2.set(3, 0, (state2.get(2, 0) - state1.get(2, 0)) / dTime);

        /********************************************/
        /*     Predict phase (apriori estimate)     */
        /********************************************/
        
        A.set(0, 1, dTime);
        A.set(2, 3, dTime);

//        System.out.println(" " + dTime);
//        System.out.println("state1  " + state1.get(0, 0) + " " + state1.get(1, 0));
//        System.out.println("state2  " + state2.get(0, 0) + " " + state2.get(1, 0));
        state2pre = A.times(state1);

        /* predict fictive deviance error */
        Q.set(0, 0, fHdop1 / 1.0);
        Q.set(1, 1, fHdop1 / 1.0);
        Q.set(2, 2, fHdop1 / 1.0);
        Q.set(3, 3, fHdop1 / 1.0);
        Ppre = (A.times(P)).times(A.transpose()).plus(Q);

if (debugMode) {
    dataPred.addElement(new Measure(state2pre.get(0, 0), state2pre.get(2, 0), 0.0));
}

        /********************************************/
        /*     Update phase (apriori estimate)     */
        /********************************************/
        R.set(0, 0, fHdop2);
        R.set(1, 1, 2 * fHdop2);
        R.set(2, 2, fHdop2);
        R.set(3, 3, 2 * fHdop2);
        
if (debugMode) {
    Logger.log("    A:\n" + A.print(2));
    Logger.log("    P:\n" + P.print(2));
//    Logger.log("    H:\n" + H.print(2));
//    Logger.log("    Ppre:\n" + Ppre.print(2));
//    Logger.log("    R:\n" + R.print(2));
//    Logger.log("\n" + (((H.times(Ppre)).times(H.transpose())).plus(R)).print(2));
}
        /* Kalman gain */
        K = (Ppre.times(H.transpose())).times(
                (((H.times(Ppre)).times(H.transpose())).plus(R)).inverse());
//        System.out.println("state2pre  " + state2pre.get(0, 0) + " " + state2pre.get(1, 0));
//        System.out.println(K.times(state2.minus(H.times(state2pre))).getRowDimension() + " " + K.times(state2.minus(H.times(state2pre))).getColumnDimension());
        state2 = state2pre.plus(K.times(state2.minus(H.times(state2pre))));
        P = (Matrix.identity(4, 4).minus(K.times(H))).times(Ppre);

if (debugMode) {
    Logger.log("    K:\n" + K.print(2));
    Logger.log("    new P:\n" + P.print(2));
    Logger.log("    state2:\n" + state2.print(5));
}

        /* prepare for next cycle */
        lastSample.setLatitude(state2.get(0, 0));
        lastSample.setLongitude(state2.get(2, 0));

        /* compute speed */
        float tempSpeed = (float) (GpsUtils.computeDistance(state1.get(0, 0), state1.get(2, 0), state2.get(0, 0), state2.get(2, 0)) / dTime);
//System.out.println("t1: " + tempSpeed);
        if (tempSpeed > 1.5f * lastSpeed && lastSpeed > 0)
            tempSpeed = 1.5f * lastSpeed;
        else if (tempSpeed < lastSpeed / 2 && lastSpeed > 0)
            tempSpeed = lastSpeed / 2;

        lastSample.setSpeed(tempSpeed);
        lastSpeed = tempSpeed;
//System.out.println("t2: " + lastSpeed);
        /* compute heading */
        lastSample.setCourse(lastCourse =
                GpsUtils.computeAzimut(state1.get(0, 0), state1.get(2, 0), state2.get(0, 0), state2.get(2, 0)));

        state2.copyTo(state1);
    }

    /**
     * Initialize needed matrix for Kalman filter
     */
    private void initializeMatrix() {
        ////#debug
        state1 = new Matrix(4, 1, 0.0);
        state2 = new Matrix(4, 1, 0.0);
        state2pre = new Matrix(4, 1, 0.0);

        A = new Matrix(4, 4, 0.0);
        A.set(0, 0, 1);
        A.set(0, 1, 100);
        A.set(1, 1, 1);
        A.set(2, 2, 1);
        A.set(2, 3, 100);
        A.set(3, 3, 1);

        R = new Matrix(4, 4, 0.0);
        R.set(0, 0, 100);
        R.set(1, 1, 100);
        R.set(2, 2, 100);
        R.set(3, 3, 100);

        H = new Matrix(4, 4, 0.0);
        H.set(0, 0, 1);
        H.set(1, 1, 1);
        H.set(2, 2, 1);
        H.set(3, 3, 1);

        P = new Matrix(4, 4, 0.0);
        P.set(0, 0, 100);
        P.set(1, 1, 100);
        P.set(2, 2, 100);
        P.set(3, 3, 100);

        Ppre = new Matrix(4, 4, 0.0);

        K = new Matrix(4, 4, 0.0);

        Q = new Matrix(4, 4, 0.0);
    }

    /**
     * Check matrix if contains only allowed values
     */
    private void checkMatrix() {
        /* for max speed 5000km/h = 1400m/s => max change of coordinates per sec = 0.0125 */
        /* check state1 */
        if (Math.abs(state1.get(0, 0)) > 90) {
            state1.set(0, 0, lastSample.getLatitude());
        }
        if (Math.abs(state1.get(1, 0)) > 0.0125) {
            state1.set(1, 0, 0.0);
        }
        if (Math.abs(state1.get(2, 0)) > 90) {
            state1.set(2, 0, lastSample.getLongitude());
        }
        if (Math.abs(state1.get(3, 0)) > 0.0125) {
            state1.set(3, 0, 0.0);
        }
        if (Math.abs(P.get(0, 0)) <= 0.0) {
            P.set(0, 0, 1.0);
        }
        if (Math.abs(P.get(1, 1)) <= 0.0) {
            P.set(1, 1, 1.0);
        }
        if (Math.abs(P.get(2, 2)) <= 0.0) {
            P.set(2, 2, 1.0);
        }
        if (Math.abs(P.get(3, 3)) <= 0.0) {
            P.set(3, 3, 1.0);
        }
    }

//    /**
//     * Save log created during computing, only if debug enable
//     * @param data
//     */
//    private void saveLog(final Vector data) {
//        if (debugMode) {
//            Thread thread = new Thread(new Runnable() {
//
//                public void run() {
//                    try {
//                        String string = "";
//                        for (int i = 0; i < data.size(); i++) {
//                            Measure measure = (Measure) data.elementAt(i);
//                            string += ("               measure.addElement(new Measure(" + measure.getLat() +
//                                    ", " + measure.getLon() +
//                                    ", " + measure.getWeight() + "));\n");
//                        }
//
//                        //write file
//                        References.getFileSystem().saveString(FileSystem.LOG_FOLDER + System.currentTimeMillis() + ".log", string);
//                        //view alert
//                        Logger.log("succesfully saved !!!");
//
//                    } catch (Exception e) {
//                        References.getErrorScreen().view(e, "KalmanLocationFilter.saveLog", null);
//                    }
//                }
//            });
//            thread.start();
//        }
//    }
//
    /**
     * Save points to DXF, points created only during debug
     */
    private void saveDXF() {
        if (debugMode) {
            Thread thread = new Thread(new Runnable() {

                public void run() {
                    String errorMessage = "no error";

                    try {
                        Logger.debug(" ... outDXF algorithm initialized ... \n");

                        //String fileName = WaypointData.fileName("dxfKalman");

                        String pointLayerMeas = "pointsMeas";
                        String pointLayerPred = "pointsPred";
                        String pointLayerResu = "pointsResu";

                        String data = "";

                        data += ("0" + linSe);
                        data += ("SECTION" + linSe);
                        data += ("2" + linSe);
                        data += ("TABLES" + linSe);

                        data += ("0" + linSe);
                        data += ("TABLE" + linSe);
                        data += ("2" + linSe);
                        data += ("LAYER" + linSe);

                        data += ("0" + linSe);
                        data += ("LAYER" + linSe);
                        data += ("100" + linSe);
                        data += ("AcDbSymbolTableRecord" + linSe);
                        data += ("100" + linSe);
                        data += ("AcDbLayerTableRecord" + linSe);
                        data += ("70" + linSe);
                        data += ("0" + linSe);
                        data += ("2" + linSe);
                        data += (pointLayerMeas + linSe);
                        data += ("6" + linSe);
                        data += ("Continuous" + linSe);
                        data += ("62" + linSe);
                        data += ("7" + linSe);

                        data += ("0" + linSe);
                        data += ("LAYER" + linSe);
                        data += ("100" + linSe);
                        data += ("AcDbSymbolTableRecord" + linSe);
                        data += ("100" + linSe);
                        data += ("AcDbLayerTableRecord" + linSe);
                        data += ("70" + linSe);
                        data += ("0" + linSe);
                        data += ("2" + linSe);
                        data += (pointLayerPred + linSe);
                        data += ("6" + linSe);
                        data += ("Continuous" + linSe);
                        data += ("62" + linSe);
                        data += ("3" + linSe);

                        data += ("0" + linSe);
                        data += ("LAYER" + linSe);
                        data += ("100" + linSe);
                        data += ("AcDbSymbolTableRecord" + linSe);
                        data += ("100" + linSe);
                        data += ("AcDbLayerTableRecord" + linSe);
                        data += ("70" + linSe);
                        data += ("0" + linSe);
                        data += ("2" + linSe);
                        data += (pointLayerResu + linSe);
                        data += ("6" + linSe);
                        data += ("Continuous" + linSe);
                        data += ("62" + linSe);
                        data += ("1" + linSe);

                        data += ("0" + linSe);
                        data += ("ENDTAB" + linSe);
                        data += ("0" + linSe);
                        data += ("ENDSEC" + linSe);

                        data += ("0" + linSe);
                        data += ("SECTION" + linSe);
                        data += ("2" + linSe);
                        data += ("ENTITIES" + linSe);

                        Measure meas1, meas2, pred1, pred2, resu1, resu2;
                        if (dataMeas.size() != dataResult.size()) {
                            errorMessage = "diff size Meas (" + dataMeas.size() + ") & Resu (" + dataResult.size() + ")";
                        }
                        for (int i = 0; i < dataMeas.size() - 1; i++) {
                            // coordinates in the right precission format
                            meas1 = (Measure) dataMeas.elementAt(i);
                            meas2 = (Measure) dataMeas.elementAt(i + 1);
                            pred1 = (Measure) dataPred.elementAt(i);
                            pred2 = (Measure) dataPred.elementAt(i + 1);
                            resu1 = (Measure) dataResult.elementAt(i);
                            resu2 = (Measure) dataResult.elementAt(i + 1);

                            /**************************/
                            /* first point of measure */
                            /**************************/
                            data += ("0" + linSe);
                            data += ("POINT" + linSe);
                            // 8 ... layer name
                            data += ("8" + linSe);
                            data += (pointLayerMeas + linSe);
                            // 62 ... color set 256 ... by layer
                            data += ("62" + linSe);
                            data += ("256" + linSe);
                            // 10 .. X coo
                            data += ("10" + linSe);
                            data += (meas1.getLat() + linSe);
                            data += ("20" + linSe);
                            data += (meas1.getLon() + linSe);
                            data += ("30" + linSe);
                            data += ("0.0" + linSe);

                            /**************************/
                            /* first point of predict */
                            /**************************/
                            data += ("0" + linSe);
                            data += ("POINT" + linSe);
                            // 8 ... layer name
                            data += ("8" + linSe);
                            data += (pointLayerPred + linSe);
                            // 62 ... color set 256 ... by layer
                            data += ("62" + linSe);
                            data += ("256" + linSe);
                            // 10 .. X coo
                            data += ("10" + linSe);
                            data += (pred1.getLat() + linSe);
                            data += ("20" + linSe);
                            data += (pred1.getLon() + linSe);
                            data += ("30" + linSe);
                            data += ("0.0" + linSe);

                            /*************************/
                            /* first point of result */
                            /*************************/
                            data += ("0" + linSe);
                            data += ("POINT" + linSe);
                            // 8 ... layer name
                            data += ("8" + linSe);
                            data += (pointLayerResu + linSe);
                            // 62 ... color set 256 ... by layer
                            data += ("62" + linSe);
                            data += ("256" + linSe);
                            // 10 .. X coo
                            data += ("10" + linSe);
                            data += (resu1.getLat() + linSe);
                            data += ("20" + linSe);
                            data += (resu1.getLon() + linSe);
                            data += ("30" + linSe);
                            data += ("0.0" + linSe);

                            data += ("0" + linSe);
                            data += ("LINE" + linSe);
                            // 8 ... layer name
                            data += ("8" + linSe);
                            data += (pointLayerMeas + linSe);
                            // 62 ... color set 256 ... by layer
                            data += ("62" + linSe);
                            data += ("256" + linSe);
                            // 10 .. X coo
                            data += ("10" + linSe);
                            data += (meas1.getLat() + linSe);
                            data += ("20" + linSe);
                            data += (meas1.getLon() + linSe);
                            data += ("30" + linSe);
                            data += ("0.0" + linSe);
                            data += ("11" + linSe);
                            data += (meas2.getLat() + linSe);
                            data += ("21" + linSe);
                            data += (meas2.getLon() + linSe);
                            data += ("31" + linSe);
                            data += ("0.0" + linSe);

                            data += ("0" + linSe);
                            data += ("LINE" + linSe);
                            // 8 ... layer name
                            data += ("8" + linSe);
                            data += (pointLayerPred + linSe);
                            // 62 ... color set 256 ... by layer
                            data += ("62" + linSe);
                            data += ("256" + linSe);
                            // 10 .. X coo
                            data += ("10" + linSe);
                            data += (pred1.getLat() + linSe);
                            data += ("20" + linSe);
                            data += (pred1.getLon() + linSe);
                            data += ("30" + linSe);
                            data += ("0.0" + linSe);
                            data += ("11" + linSe);
                            data += (pred2.getLat() + linSe);
                            data += ("21" + linSe);
                            data += (pred2.getLon() + linSe);
                            data += ("31" + linSe);
                            data += ("0.0" + linSe);

                            data += ("0" + linSe);
                            data += ("LINE" + linSe);
                            // 8 ... layer name
                            data += ("8" + linSe);
                            data += (pointLayerResu + linSe);
                            // 62 ... color set 256 ... by layer
                            data += ("62" + linSe);
                            data += ("256" + linSe);
                            // 10 .. X coo
                            data += ("10" + linSe);
                            data += (resu1.getLat() + linSe);
                            data += ("20" + linSe);
                            data += (resu1.getLon() + linSe);
                            data += ("30" + linSe);
                            data += ("0.0" + linSe);
                            data += ("11" + linSe);
                            data += (resu2.getLat() + linSe);
                            data += ("21" + linSe);
                            data += (resu2.getLon() + linSe);
                            data += ("31" + linSe);
                            data += ("0.0" + linSe);
                        }

                        data += ("0" + linSe);
                        data += ("ENDSEC" + linSe);
                        data += ("0" + linSe);
                        data += ("EOF" + linSe);

                        //write file
                        com.locify.client.utils.R.getFileSystem().saveString(FileSystem.LOG_FOLDER + System.currentTimeMillis() + ".dxf", data);
                        Logger.debug(" ... outDXF algorithm finished ... \n");
                    } catch (Exception e) {
                        com.locify.client.utils.R.getErrorScreen().view(e, "KalmanLocationFilter.saveDXF", null);
                    }
                }
            });

            thread.start();
        }
    }
//
//    /**
//     * test of kalman filter used only for debug without GPS connected
//     * idealy run this function right after Midlet starts
//     */
//    public void myKalmanTest() {
//        Thread thread = new Thread(new Runnable() {
//
//            public void run() {
//                Vector measure3 = new Vector();
//                measure3.addElement(new Measure(50.051565, 14.459833333333334, 100.0));
//                measure3.addElement(new Measure(50.051568333333336, 14.459818333333333, 1.899999976158142));
//                measure3.addElement(new Measure(50.05157, 14.459806666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.05157333333333, 14.459795, 1.600000023841858));
//                measure3.addElement(new Measure(50.051575, 14.459783333333334, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05157666666667, 14.459751666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05158, 14.459726666666667, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05158333333333, 14.459711666666667, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051588333333335, 14.459698333333334, 1.600000023841858));
//                measure3.addElement(new Measure(50.05159333333334, 14.459683333333333, 1.600000023841858));
//                measure3.addElement(new Measure(50.05159666666667, 14.459668333333333, 1.600000023841858));
//                measure3.addElement(new Measure(50.05160166666667, 14.459636666666666, 1.100000023841858));
//                measure3.addElement(new Measure(50.051605, 14.45962, 1.100000023841858));
//                measure3.addElement(new Measure(50.051606666666665, 14.459606666666666, 1.100000023841858));
//                measure3.addElement(new Measure(50.051615, 14.459591666666666, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05161833333333, 14.459573333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.05162333333333, 14.459558333333334, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051626666666664, 14.459548333333334, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05163, 14.459541666666667, 1.399999976158142));
//                measure3.addElement(new Measure(50.051635, 14.459531666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05164166666667, 14.459525, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05165, 14.459518333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.051655, 14.459508333333334, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051658333333336, 14.459501666666666, 0.8999999761581421));
//                measure3.addElement(new Measure(50.05166333333333, 14.45949, 0.8999999761581421));
//                measure3.addElement(new Measure(50.05166833333333, 14.459481666666667, 1.0));
//                measure3.addElement(new Measure(50.051671666666664, 14.459473333333333, 1.100000023841858));
//                measure3.addElement(new Measure(50.051675, 14.459463333333334, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05168, 14.459455, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05168833333333, 14.459445, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05168333333334, 14.459428333333333, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051678333333335, 14.459423333333334, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05167, 14.45942, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05166166666667, 14.459416666666666, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051655, 14.459418333333334, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05165, 14.459425, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05164666666667, 14.459431666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05164166666667, 14.45944, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051635, 14.45945, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05163, 14.45946, 1.5));
//                measure3.addElement(new Measure(50.05162333333333, 14.459471666666667, 1.5));
//                measure3.addElement(new Measure(50.05162166666667, 14.459483333333333, 1.5));
//                measure3.addElement(new Measure(50.05161833333333, 14.459493333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.051613333333336, 14.459505, 1.5));
//                measure3.addElement(new Measure(50.05161, 14.459516666666667, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051606666666665, 14.459525, 1.5));
//                measure3.addElement(new Measure(50.05160333333333, 14.459533333333333, 1.5));
//                measure3.addElement(new Measure(50.0516, 14.459545, 1.399999976158142));
//                measure3.addElement(new Measure(50.05159833333333, 14.459555, 1.399999976158142));
//                measure3.addElement(new Measure(50.05159333333334, 14.459565, 1.399999976158142));
//                measure3.addElement(new Measure(50.05159, 14.459576666666667, 1.5));
//                measure3.addElement(new Measure(50.051586666666665, 14.459588333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.051581666666664, 14.459601666666666, 1.399999976158142));
//                measure3.addElement(new Measure(50.05157833333333, 14.459613333333333, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05157666666667, 14.459623333333333, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05157333333333, 14.459635, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05157, 14.459646666666666, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051566666666666, 14.459658333333334, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051563333333334, 14.45967, 2.0999999046325684));
//                measure3.addElement(new Measure(50.051561666666665, 14.459683333333333, 1.600000023841858));
//                measure3.addElement(new Measure(50.05155666666667, 14.459695, 1.600000023841858));
//                measure3.addElement(new Measure(50.05155166666667, 14.459706666666667, 1.399999976158142));
//                measure3.addElement(new Measure(50.05154666666667, 14.459716666666667, 1.5));
//                measure3.addElement(new Measure(50.05154, 14.459726666666667, 1.899999976158142));
//                measure3.addElement(new Measure(50.05153833333333, 14.459738333333334, 1.899999976158142));
//                measure3.addElement(new Measure(50.051535, 14.45975, 1.5));
//                measure3.addElement(new Measure(50.05153166666667, 14.459763333333333, 1.5));
//                measure3.addElement(new Measure(50.05152666666667, 14.459775, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051521666666666, 14.459786666666666, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051518333333334, 14.459796666666668, 1.600000023841858));
//                measure3.addElement(new Measure(50.051515, 14.459806666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.05151166666667, 14.45982, 1.600000023841858));
//                measure3.addElement(new Measure(50.05150666666667, 14.459833333333334, 1.600000023841858));
//                measure3.addElement(new Measure(50.051503333333336, 14.459845, 1.5));
//                measure3.addElement(new Measure(50.0515, 14.459855, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051498333333335, 14.459865, 2.0999999046325684));
//                measure3.addElement(new Measure(50.051495, 14.45988, 2.4000000953674316));
//                measure3.addElement(new Measure(50.05149, 14.459906666666667, 1.899999976158142));
//                measure3.addElement(new Measure(50.05148833333333, 14.459923333333334, 1.899999976158142));
//                measure3.addElement(new Measure(50.05148333333333, 14.45994, 2.4000000953674316));
//                measure3.addElement(new Measure(50.05148166666667, 14.459953333333333, 1.5));
//                measure3.addElement(new Measure(50.051478333333336, 14.459966666666666, 1.399999976158142));
//                measure3.addElement(new Measure(50.051476666666666, 14.460011666666666, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051475, 14.460026666666666, 1.600000023841858));
//                measure3.addElement(new Measure(50.051473333333334, 14.460038333333333, 1.5));
//                measure3.addElement(new Measure(50.051471666666664, 14.46005, 1.399999976158142));
//                measure3.addElement(new Measure(50.05147, 14.460063333333334, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05146666666667, 14.460075, 1.100000023841858));
//                measure3.addElement(new Measure(50.05146333333333, 14.46009, 1.399999976158142));
//                measure3.addElement(new Measure(50.05146166666667, 14.460105, 1.5));
//                measure3.addElement(new Measure(50.051458333333336, 14.460118333333334, 1.5));
//                measure3.addElement(new Measure(50.051455, 14.460131666666667, 1.5));
//                measure3.addElement(new Measure(50.051451666666665, 14.460145, 1.5));
//                measure3.addElement(new Measure(50.05144833333333, 14.460158333333334, 1.399999976158142));
//                measure3.addElement(new Measure(50.051445, 14.460171666666668, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05144166666667, 14.460195, 1.399999976158142));
//                measure3.addElement(new Measure(50.05144, 14.460205, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05143833333333, 14.460216666666666, 1.899999976158142));
//                measure3.addElement(new Measure(50.051433333333335, 14.460231666666667, 1.7999999523162842));
//                measure3.addElement(new Measure(50.05143, 14.460248333333332, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051426666666664, 14.460263333333334, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05142166666667, 14.460280000000001, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05142, 14.460295, 1.600000023841858));
//                measure3.addElement(new Measure(50.05141666666667, 14.460306666666666, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051413333333336, 14.460323333333333, 1.600000023841858));
//                measure3.addElement(new Measure(50.05141166666667, 14.460338333333333, 1.600000023841858));
//                measure3.addElement(new Measure(50.05141, 14.460351666666666, 1.600000023841858));
//                measure3.addElement(new Measure(50.051406666666665, 14.460363333333333, 1.899999976158142));
//                measure3.addElement(new Measure(50.05140333333333, 14.460376666666667, 1.899999976158142));
//                measure3.addElement(new Measure(50.05140166666666, 14.46039, 2.0));
//                measure3.addElement(new Measure(50.05139833333333, 14.4604, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05139333333333, 14.460411666666667, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051388333333335, 14.460425, 1.600000023841858));
//                measure3.addElement(new Measure(50.051386666666666, 14.46044, 1.899999976158142));
//                measure3.addElement(new Measure(50.051381666666664, 14.460453333333334, 1.899999976158142));
//                measure3.addElement(new Measure(50.05138, 14.460466666666667, 2.299999952316284));
//                measure3.addElement(new Measure(50.051375, 14.46048, 1.399999976158142));
//                measure3.addElement(new Measure(50.05137166666667, 14.460493333333334, 1.399999976158142));
//                measure3.addElement(new Measure(50.05137, 14.460508333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.051365, 14.460523333333333, 1.600000023841858));
//                measure3.addElement(new Measure(50.051363333333335, 14.460536666666666, 1.7999999523162842));
//                measure3.addElement(new Measure(50.05135833333333, 14.460551666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.05135666666666, 14.460566666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.051355, 14.460601666666667, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05135333333333, 14.460613333333333, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05135666666666, 14.460626666666666, 1.899999976158142));
//                measure3.addElement(new Measure(50.05137, 14.460648333333333, 2.0));
//                measure3.addElement(new Measure(50.05137333333333, 14.460661666666667, 2.4000000953674316));
//                measure3.addElement(new Measure(50.051375, 14.460675, 2.0999999046325684));
//                measure3.addElement(new Measure(50.05137833333333, 14.4607, 1.600000023841858));
//                measure3.addElement(new Measure(50.05138, 14.460711666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.051381666666664, 14.460723333333334, 1.600000023841858));
//                measure3.addElement(new Measure(50.05138, 14.460745, 1.600000023841858));
//                measure3.addElement(new Measure(50.05137833333333, 14.460761666666667, 1.5));
//                measure3.addElement(new Measure(50.05137666666667, 14.46079, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05137333333333, 14.460801666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05137166666667, 14.460811666666666, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05137, 14.460823333333334, 1.100000023841858));
//                measure3.addElement(new Measure(50.051365, 14.460836666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051361666666665, 14.460851666666667, 1.0));
//                measure3.addElement(new Measure(50.05135666666666, 14.460866666666666, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05135166666667, 14.46088, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05135, 14.460893333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.05134666666667, 14.460908333333334, 1.399999976158142));
//                measure3.addElement(new Measure(50.051343333333335, 14.46092, 1.399999976158142));
//                measure3.addElement(new Measure(50.05134, 14.46093, 1.399999976158142));
//                measure3.addElement(new Measure(50.051336666666664, 14.460941666666667, 1.5));
//                measure3.addElement(new Measure(50.05133333333333, 14.460955, 1.5));
//                measure3.addElement(new Measure(50.05132833333333, 14.460968333333334, 2.200000047683716));
//                measure3.addElement(new Measure(50.051323333333336, 14.460983333333333, 2.0999999046325684));
//                measure3.addElement(new Measure(50.051318333333334, 14.461, 2.0999999046325684));
//                measure3.addElement(new Measure(50.051315, 14.461015, 2.200000047683716));
//                measure3.addElement(new Measure(50.05131333333333, 14.46103, 2.200000047683716));
//                measure3.addElement(new Measure(50.05131, 14.461045, 2.5));
//                measure3.addElement(new Measure(50.05131166666666, 14.461061666666666, 2.0));
//                measure3.addElement(new Measure(50.05131, 14.461078333333333, 1.7999999523162842));
//                measure3.addElement(new Measure(50.05130166666667, 14.4611, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05130333333334, 14.461108333333334, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05130833333333, 14.461105, 1.399999976158142));
//                measure3.addElement(new Measure(50.05131166666666, 14.461098333333334, 1.100000023841858));
//                measure3.addElement(new Measure(50.05131333333333, 14.461093333333334, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051316666666665, 14.461088333333333, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051318333333334, 14.461081666666667, 1.5));
//                measure3.addElement(new Measure(50.05132, 14.461073333333333, 2.5));
//                measure3.addElement(new Measure(50.05132166666667, 14.461066666666667, 2.5999999046325684));
//                measure3.addElement(new Measure(50.051323333333336, 14.461041666666667, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05132833333333, 14.461031666666667, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05133333333333, 14.461021666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051338333333334, 14.461015, 2.0));
//                measure3.addElement(new Measure(50.051341666666666, 14.461006666666666, 2.0999999046325684));
//                measure3.addElement(new Measure(50.051345, 14.460996666666666, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05134666666667, 14.460988333333333, 1.7999999523162842));
//                measure3.addElement(new Measure(50.05135, 14.460976666666667, 1.899999976158142));
//                measure3.addElement(new Measure(50.05135333333333, 14.460956666666666, 1.7999999523162842));
//                measure3.addElement(new Measure(50.05135666666666, 14.460945, 2.0999999046325684));
//                measure3.addElement(new Measure(50.05135833333333, 14.460935, 2.0999999046325684));
//                measure3.addElement(new Measure(50.05136, 14.460921666666668, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051363333333335, 14.460893333333333, 2.0999999046325684));
//                measure3.addElement(new Measure(50.05136666666667, 14.46088, 2.0));
//                measure3.addElement(new Measure(50.051368333333336, 14.460865, 2.5));
//                measure3.addElement(new Measure(50.05137333333333, 14.460855, 1.5));
//                measure3.addElement(new Measure(50.05137833333333, 14.460845, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051381666666664, 14.460831666666667, 1.5));
//                measure3.addElement(new Measure(50.051386666666666, 14.460818333333334, 1.5));
//                measure3.addElement(new Measure(50.05139, 14.460806666666667, 2.0999999046325684));
//                measure3.addElement(new Measure(50.05139666666667, 14.460795, 2.299999952316284));
//                measure3.addElement(new Measure(50.0514, 14.460783333333334, 2.200000047683716));
//                measure3.addElement(new Measure(50.051406666666665, 14.460773333333334, 2.0999999046325684));
//                measure3.addElement(new Measure(50.051413333333336, 14.460768333333334, 1.399999976158142));
//                measure3.addElement(new Measure(50.05141666666667, 14.460761666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05141833333333, 14.460741666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.05141666666667, 14.460731666666666, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05142, 14.46071, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05142333333333, 14.460698333333333, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051428333333334, 14.460685, 1.899999976158142));
//                measure3.addElement(new Measure(50.051433333333335, 14.460673333333334, 2.700000047683716));
//                measure3.addElement(new Measure(50.051435, 14.46066, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051433333333335, 14.460616666666667, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05143, 14.46059, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051435, 14.460575, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051433333333335, 14.460546666666666, 1.399999976158142));
//                measure3.addElement(new Measure(50.051435, 14.460508333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.05143833333333, 14.460496666666666, 1.399999976158142));
//                measure3.addElement(new Measure(50.05144333333333, 14.460483333333332, 2.200000047683716));
//                measure3.addElement(new Measure(50.051445, 14.460466666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.05144833333333, 14.460436666666666, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05145, 14.460425, 1.7999999523162842));
//                measure3.addElement(new Measure(50.051453333333335, 14.46037, 2.0));
//                measure3.addElement(new Measure(50.051458333333336, 14.460356666666666, 1.600000023841858));
//                measure3.addElement(new Measure(50.05146166666667, 14.460338333333333, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05146333333333, 14.460321666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051465, 14.460301666666666, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05146666666667, 14.460288333333333, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05146833333333, 14.460271666666667, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051471666666664, 14.460256666666666, 1.5));
//                measure3.addElement(new Measure(50.051475, 14.460238333333333, 1.399999976158142));
//                measure3.addElement(new Measure(50.051478333333336, 14.460223333333333, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05148166666667, 14.460206666666666, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05148333333333, 14.460188333333333, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051485, 14.460173333333334, 1.100000023841858));
//                measure3.addElement(new Measure(50.05148833333333, 14.460158333333334, 1.2000000476837158));
//                measure3.addElement(new Measure(50.051491666666664, 14.460145, 1.5));
//                measure3.addElement(new Measure(50.051498333333335, 14.460128333333333, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05150166666667, 14.460113333333334, 1.0));
//                measure3.addElement(new Measure(50.05150666666667, 14.460096666666667, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05151, 14.46008, 1.399999976158142));
//                measure3.addElement(new Measure(50.05151166666667, 14.460063333333334, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05151, 14.46005, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05151166666667, 14.460035, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051516666666664, 14.460018333333334, 1.399999976158142));
//                measure3.addElement(new Measure(50.05152, 14.460001666666667, 1.7000000476837158));
//                measure3.addElement(new Measure(50.051525, 14.459988333333333, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05152833333333, 14.459971666666666, 1.600000023841858));
//                measure3.addElement(new Measure(50.05153, 14.459958333333333, 1.7000000476837158));
//                measure3.addElement(new Measure(50.05153333333333, 14.459943333333333, 1.600000023841858));
//                measure3.addElement(new Measure(50.05153833333333, 14.459925, 1.399999976158142));
//                measure3.addElement(new Measure(50.051543333333335, 14.459911666666667, 1.600000023841858));
//                measure3.addElement(new Measure(50.05155, 14.459895, 1.399999976158142));
//                measure3.addElement(new Measure(50.05155333333333, 14.459878333333334, 1.2000000476837158));
//                measure3.addElement(new Measure(50.05155833333333, 14.459866666666667, 1.2999999523162842));
//                measure3.addElement(new Measure(50.051563333333334, 14.459855, 1.399999976158142));
//                measure3.addElement(new Measure(50.051568333333336, 14.45984, 1.100000023841858));
//                measure3.addElement(new Measure(50.05157, 14.459826666666666, 1.100000023841858));
//                measure3.addElement(new Measure(50.05157333333333, 14.459813333333333, 1.100000023841858));
//                measure3.addElement(new Measure(50.05157833333333, 14.459801666666667, 1.2999999523162842));
//                measure3.addElement(new Measure(50.05158333333333, 14.459791666666666, 1.0));
//                measure3.addElement(new Measure(50.051585, 14.459781666666666, 1.0));
//                measure3.addElement(new Measure(50.051586666666665, 14.459775, 1.100000023841858));
//                measure3.addElement(new Measure(50.051588333333335, 14.459768333333333, 1.100000023841858));
//                measure3.addElement(new Measure(50.05159, 14.459765, 1.2999999523162842));
//
//
//
//
//                Vector measure = new Vector(250);
//                measure.addElement(new Measure(49.86613833333333, 14.40319, 100.0));
//                measure.addElement(new Measure(49.86614, 14.403185, 25.5));
//                measure.addElement(new Measure(49.866141666666664, 14.403178333333333, 7.400000095367432));
//                measure.addElement(new Measure(49.866145, 14.403168333333333, 7.400000095367432));
//                measure.addElement(new Measure(49.866145, 14.403166666666667, 2.5999999046325684));
//                measure.addElement(new Measure(49.86615333333334, 14.403151666666666, 2.5));
//                measure.addElement(new Measure(49.86616333333333, 14.403135, 3.0999999046325684));
//                measure.addElement(new Measure(49.86617, 14.403116666666667, 3.4000000953674316));
//                measure.addElement(new Measure(49.86617666666667, 14.403095, 2.299999952316284));
//                measure.addElement(new Measure(49.86618333333333, 14.403081666666667, 2.4000000953674316));
//                measure.addElement(new Measure(49.8662, 14.403051666666666, 2.4000000953674316));
//                measure.addElement(new Measure(49.86620833333333, 14.403046666666667, 1.7000000476837158));
//                measure.addElement(new Measure(49.86623, 14.403053333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.866236666666666, 14.403055, 1.2000000476837158));
//                measure.addElement(new Measure(49.86624333333334, 14.403053333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.86624666666667, 14.403056666666666, 1.0));
//                measure.addElement(new Measure(49.86625, 14.40306, 1.2000000476837158));
//                measure.addElement(new Measure(49.866476666666664, 14.40303, 1.2000000476837158));
//                measure.addElement(new Measure(49.866488333333336, 14.403035, 1.2999999523162842));
//                measure.addElement(new Measure(49.86649666666667, 14.40304, 1.600000023841858));
//                measure.addElement(new Measure(49.866495, 14.403041666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.86649666666667, 14.403048333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.866508333333336, 14.403053333333334, 1.2999999523162842));
//                measure.addElement(new Measure(49.86651, 14.403055, 1.5));
//                measure.addElement(new Measure(49.86651333333333, 14.403058333333334, 1.399999976158142));
//                measure.addElement(new Measure(49.86651666666667, 14.403056666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.866521666666664, 14.403043333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.866525, 14.403026666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.866525, 14.403013333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.86652333333333, 14.403005, 1.100000023841858));
//                measure.addElement(new Measure(49.866521666666664, 14.403, 1.0));
//                measure.addElement(new Measure(49.86651666666667, 14.403005, 1.100000023841858));
//                measure.addElement(new Measure(49.866515, 14.403006666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86651333333333, 14.403005, 1.7999999523162842));
//                measure.addElement(new Measure(49.86651166666667, 14.403005, 1.7999999523162842));
//                measure.addElement(new Measure(49.866506666666666, 14.402998333333333, 2.0));
//                measure.addElement(new Measure(49.866503333333334, 14.402996666666667, 2.0));
//                measure.addElement(new Measure(49.866501666666665, 14.402995, 1.7999999523162842));
//                measure.addElement(new Measure(49.8665, 14.402996666666667, 3.0999999046325684));
//                measure.addElement(new Measure(49.86649833333333, 14.402996666666667, 4.400000095367432));
//                measure.addElement(new Measure(49.8665, 14.402998333333333, 2.9000000953674316));
//                measure.addElement(new Measure(49.86649833333333, 14.402998333333333, 2.200000047683716));
//                measure.addElement(new Measure(49.86649666666667, 14.402998333333333, 1.600000023841858));
//                measure.addElement(new Measure(49.86649833333333, 14.402996666666667, 1.5));
//                measure.addElement(new Measure(49.86649833333333, 14.402998333333333, 1.600000023841858));
//                measure.addElement(new Measure(49.8665, 14.402996666666667, 1.600000023841858));
//                measure.addElement(new Measure(49.866501666666665, 14.402995, 1.399999976158142));
//                measure.addElement(new Measure(49.866503333333334, 14.402995, 1.399999976158142));
//                measure.addElement(new Measure(49.866505, 14.402993333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.866505, 14.402995, 1.0));
//                measure.addElement(new Measure(49.866505, 14.402993333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.866506666666666, 14.40299, 1.100000023841858));
//                measure.addElement(new Measure(49.866506666666666, 14.402986666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.866506666666666, 14.402981666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.866506666666666, 14.40298, 1.2999999523162842));
//                measure.addElement(new Measure(49.866505, 14.402978333333333, 1.399999976158142));
//                measure.addElement(new Measure(49.866506666666666, 14.40297, 1.5));
//                measure.addElement(new Measure(49.866506666666666, 14.402966666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.86651333333333, 14.402961666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.866515, 14.402813333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86652, 14.402798333333333, 0.8999999761581421));
//                measure.addElement(new Measure(49.866528333333335, 14.40278, 0.8999999761581421));
//                measure.addElement(new Measure(49.86653166666667, 14.402763333333333, 1.0));
//                measure.addElement(new Measure(49.86653333333334, 14.402746666666667, 1.0));
//                measure.addElement(new Measure(49.86653666666667, 14.402731666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86653666666667, 14.402711666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86654166666667, 14.402693333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.86654166666667, 14.40268, 1.2000000476837158));
//                measure.addElement(new Measure(49.86654333333333, 14.402661666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.86654166666667, 14.402645, 1.2000000476837158));
//                measure.addElement(new Measure(49.86653833333333, 14.402628333333332, 1.100000023841858));
//                measure.addElement(new Measure(49.86653833333333, 14.402608333333333, 1.0));
//                measure.addElement(new Measure(49.86653666666667, 14.40259, 1.0));
//                measure.addElement(new Measure(49.86653833333333, 14.402573333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86653666666667, 14.402555, 1.100000023841858));
//                measure.addElement(new Measure(49.86653666666667, 14.402536666666666, 1.100000023841858));
//                measure.addElement(new Measure(49.86653666666667, 14.402518333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86653666666667, 14.402498333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.86653666666667, 14.402481666666667, 1.0));
//                measure.addElement(new Measure(49.866535, 14.402461666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.86653333333334, 14.402443333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.86653166666667, 14.402428333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86653166666667, 14.402411666666668, 1.100000023841858));
//                measure.addElement(new Measure(49.86653166666667, 14.402393333333332, 1.0));
//                measure.addElement(new Measure(49.86653333333334, 14.402376666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.866535, 14.402363333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.86653166666667, 14.40235, 1.100000023841858));
//                measure.addElement(new Measure(49.866526666666665, 14.402335, 1.2999999523162842));
//                measure.addElement(new Measure(49.866521666666664, 14.40232, 1.600000023841858));
//                measure.addElement(new Measure(49.86652, 14.402305, 1.7000000476837158));
//                measure.addElement(new Measure(49.86651833333333, 14.40229, 1.399999976158142));
//                measure.addElement(new Measure(49.866515, 14.402276666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.866515, 14.40226, 0.8999999761581421));
//                measure.addElement(new Measure(49.866515, 14.402241666666667, 1.0));
//                measure.addElement(new Measure(49.866515, 14.402221666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.866515, 14.402201666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86651166666667, 14.402183333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86651, 14.402165, 1.0));
//                measure.addElement(new Measure(49.86651, 14.402141666666667, 1.0));
//                measure.addElement(new Measure(49.86651, 14.40212, 1.0));
//                measure.addElement(new Measure(49.86651, 14.402098333333333, 1.0));
//                measure.addElement(new Measure(49.866508333333336, 14.402076666666666, 1.0));
//                measure.addElement(new Measure(49.866506666666666, 14.402058333333333, 1.0));
//                measure.addElement(new Measure(49.866505, 14.402038333333333, 1.0));
//                measure.addElement(new Measure(49.866501666666665, 14.402018333333332, 1.100000023841858));
//                measure.addElement(new Measure(49.86649666666667, 14.402, 1.0));
//                measure.addElement(new Measure(49.86649333333333, 14.401981666666666, 1.0));
//                measure.addElement(new Measure(49.86649, 14.401963333333333, 1.0));
//                measure.addElement(new Measure(49.86648666666667, 14.401943333333334, 1.0));
//                measure.addElement(new Measure(49.866483333333335, 14.401925, 1.0));
//                measure.addElement(new Measure(49.86647833333333, 14.401908333333333, 1.0));
//                measure.addElement(new Measure(49.866476666666664, 14.401888333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.866476666666664, 14.401868333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.866475, 14.401848333333334, 1.0));
//                measure.addElement(new Measure(49.86647333333333, 14.401831666666666, 1.0));
//                measure.addElement(new Measure(49.86647333333333, 14.401811666666667, 1.0));
//                measure.addElement(new Measure(49.866475, 14.401793333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.866475, 14.401775, 1.100000023841858));
//                measure.addElement(new Measure(49.86647833333333, 14.40176, 1.100000023841858));
//                measure.addElement(new Measure(49.86648, 14.401745, 1.2000000476837158));
//                measure.addElement(new Measure(49.866485, 14.401731666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.86649, 14.401718333333333, 1.399999976158142));
//                measure.addElement(new Measure(49.86649666666667, 14.401705, 1.2000000476837158));
//                measure.addElement(new Measure(49.866503333333334, 14.401693333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.866508333333336, 14.401681666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86651666666667, 14.401673333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.866525, 14.401665, 1.2999999523162842));
//                measure.addElement(new Measure(49.86653166666667, 14.401656666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.86653833333333, 14.401651666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.866548333333334, 14.401648333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.86655666666667, 14.401641666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.866566666666664, 14.401636666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.86657666666667, 14.401631666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.86658666666666, 14.401628333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.866596666666666, 14.401621666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86661, 14.401616666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86662166666667, 14.401611666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86663333333333, 14.401605, 1.2000000476837158));
//                measure.addElement(new Measure(49.866645, 14.401596666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.866656666666664, 14.401591666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86666833333334, 14.401588333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.86668, 14.401583333333333, 1.399999976158142));
//                measure.addElement(new Measure(49.86669, 14.40158, 1.399999976158142));
//                measure.addElement(new Measure(49.86669833333333, 14.401578333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.866706666666666, 14.401575, 1.2999999523162842));
//                measure.addElement(new Measure(49.86672, 14.401568333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.86673, 14.401561666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86674, 14.401555, 1.399999976158142));
//                measure.addElement(new Measure(49.866748333333334, 14.401548333333333, 1.399999976158142));
//                measure.addElement(new Measure(49.86675833333334, 14.401541666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.86676833333333, 14.401536666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.86678, 14.40153, 1.399999976158142));
//                measure.addElement(new Measure(49.866791666666664, 14.401525, 1.5));
//                measure.addElement(new Measure(49.86680333333334, 14.40152, 1.2999999523162842));
//                measure.addElement(new Measure(49.866816666666665, 14.401511666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.86682833333333, 14.401505, 1.399999976158142));
//                measure.addElement(new Measure(49.866841666666666, 14.401498333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.866855, 14.401491666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.866868333333336, 14.401488333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.86687833333333, 14.401485, 1.2999999523162842));
//                measure.addElement(new Measure(49.86689, 14.401478333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.866901666666664, 14.401471666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86691, 14.401463333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.86692166666667, 14.401456666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.866933333333336, 14.401455, 1.2000000476837158));
//                measure.addElement(new Measure(49.86694333333333, 14.40145, 1.2000000476837158));
//                measure.addElement(new Measure(49.866955, 14.401443333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86696666666667, 14.401438333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.866976666666666, 14.401433333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86698833333333, 14.401428333333333, 0.8999999761581421));
//                measure.addElement(new Measure(49.867, 14.401423333333334, 0.8999999761581421));
//                measure.addElement(new Measure(49.86701166666667, 14.401416666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.867023333333336, 14.40141, 1.399999976158142));
//                measure.addElement(new Measure(49.867035, 14.401403333333333, 1.5));
//                measure.addElement(new Measure(49.86704666666667, 14.401396666666667, 1.7000000476837158));
//                measure.addElement(new Measure(49.86705833333333, 14.401391666666667, 1.7999999523162842));
//                measure.addElement(new Measure(49.867068333333336, 14.401386666666667, 1.7000000476837158));
//                measure.addElement(new Measure(49.86708, 14.401383333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86709166666667, 14.401376666666666, 1.100000023841858));
//                measure.addElement(new Measure(49.86710166666667, 14.401371666666666, 1.100000023841858));
//                measure.addElement(new Measure(49.867113333333336, 14.401366666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.867125, 14.401361666666666, 1.0));
//                measure.addElement(new Measure(49.867135, 14.401358333333333, 1.0));
//                measure.addElement(new Measure(49.86714666666666, 14.401355, 1.0));
//                measure.addElement(new Measure(49.867158333333336, 14.401353333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86716833333333, 14.40135, 1.2000000476837158));
//                measure.addElement(new Measure(49.86718333333334, 14.401348333333333, 2.0));
//                measure.addElement(new Measure(49.867196666666665, 14.40135, 1.2000000476837158));
//                measure.addElement(new Measure(49.86721, 14.40135, 1.399999976158142));
//                measure.addElement(new Measure(49.867223333333335, 14.401351666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.86723666666666, 14.401353333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.86724666666667, 14.401353333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.86725333333333, 14.401355, 1.2999999523162842));
//                measure.addElement(new Measure(49.86726, 14.401356666666667, 1.7000000476837158));
//                measure.addElement(new Measure(49.86727166666667, 14.40136, 1.600000023841858));
//                measure.addElement(new Measure(49.867285, 14.401365, 1.399999976158142));
//                measure.addElement(new Measure(49.86729666666667, 14.40137, 1.2000000476837158));
//                measure.addElement(new Measure(49.867313333333335, 14.401378333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.86734, 14.401388333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.867356666666666, 14.401398333333333, 1.0));
//                measure.addElement(new Measure(49.86737333333333, 14.401411666666666, 0.8999999761581421));
//                measure.addElement(new Measure(49.867385, 14.401423333333334, 1.2999999523162842));
//                measure.addElement(new Measure(49.867398333333334, 14.401440000000001, 1.2000000476837158));
//                measure.addElement(new Measure(49.867405, 14.401456666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86741, 14.40147, 1.100000023841858));
//                measure.addElement(new Measure(49.867415, 14.401481666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.86742, 14.401495, 1.2000000476837158));
//                measure.addElement(new Measure(49.867423333333335, 14.401508333333334, 1.2999999523162842));
//                measure.addElement(new Measure(49.867428333333336, 14.401525, 1.2999999523162842));
//                measure.addElement(new Measure(49.86743166666667, 14.40154, 1.2999999523162842));
//                measure.addElement(new Measure(49.867435, 14.401553333333334, 1.399999976158142));
//                measure.addElement(new Measure(49.867435, 14.401565, 1.399999976158142));
//                measure.addElement(new Measure(49.86743666666667, 14.401575, 1.5));
//                measure.addElement(new Measure(49.86743666666667, 14.401583333333333, 1.5));
//                measure.addElement(new Measure(49.86743666666667, 14.401591666666667, 1.5));
//                measure.addElement(new Measure(49.86743833333333, 14.401598333333334, 1.600000023841858));
//                measure.addElement(new Measure(49.86743833333333, 14.4016, 1.7000000476837158));
//                measure.addElement(new Measure(49.86744, 14.401601666666666, 1.7000000476837158));
//                measure.addElement(new Measure(49.86744, 14.401605, 2.0));
//                measure.addElement(new Measure(49.867441666666664, 14.401611666666666, 2.0999999046325684));
//                measure.addElement(new Measure(49.867443333333334, 14.401623333333333, 1.5));
//                measure.addElement(new Measure(49.867445000000004, 14.401638333333333, 1.5));
//                measure.addElement(new Measure(49.867446666666666, 14.40165, 1.2999999523162842));
//                measure.addElement(new Measure(49.867448333333336, 14.401665, 1.2000000476837158));
//                measure.addElement(new Measure(49.867448333333336, 14.401678333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.867448333333336, 14.401691666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86745, 14.401703333333334, 1.2000000476837158));
//                measure.addElement(new Measure(49.86745166666667, 14.401715, 1.2000000476837158));
//                measure.addElement(new Measure(49.86745333333333, 14.401728333333333, 1.2000000476837158));
//                measure.addElement(new Measure(49.86745333333333, 14.401741666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.867455, 14.401755, 1.2999999523162842));
//                measure.addElement(new Measure(49.867455, 14.401768333333333, 1.2999999523162842));
//                measure.addElement(new Measure(49.86745666666667, 14.401783333333332, 1.2999999523162842));
//                measure.addElement(new Measure(49.867455, 14.401796666666666, 1.399999976158142));
//                measure.addElement(new Measure(49.867455, 14.401811666666667, 1.399999976158142));
//                measure.addElement(new Measure(49.867455, 14.401826666666667, 1.2999999523162842));
//                measure.addElement(new Measure(49.86745666666667, 14.401841666666666, 1.2999999523162842));
//                measure.addElement(new Measure(49.86745833333333, 14.401856666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86745666666667, 14.40187, 1.100000023841858));
//                measure.addElement(new Measure(49.867455, 14.401885, 1.100000023841858));
//                measure.addElement(new Measure(49.867455, 14.401903333333333, 1.100000023841858));
//                measure.addElement(new Measure(49.86745333333333, 14.401921666666667, 1.2000000476837158));
//                measure.addElement(new Measure(49.86745166666667, 14.401941666666666, 1.100000023841858));
//                measure.addElement(new Measure(49.86745166666667, 14.401961666666667, 1.100000023841858));
//                measure.addElement(new Measure(49.86745, 14.401981666666666, 1.2000000476837158));
//                measure.addElement(new Measure(49.86745, 14.402005, 1.2000000476837158));
//                measure.addElement(new Measure(49.867448333333336, 14.402028333333334, 1.0));
//                measure.addElement(new Measure(49.867446666666666, 14.402053333333333, 1.0));
//                measure.addElement(new Measure(49.867446666666666, 14.402073333333334, 1.100000023841858));
//                measure.addElement(new Measure(49.867446666666666, 14.402095, 1.100000023841858));
//
//                KalmanLocationFilter filter = new KalmanLocationFilter();
//                Measure measData;
//                for (int i = 0; i < numOfTest; i++) {
//                    //for (int i = measure3.size()-1; i > 0; i--) {
//                    measData = (Measure) measure3.elementAt(i);
//                    filter.addLocationSample(new LocationSample(measData.lat, measData.lon, 0.0, measData.getWeight(), 0.0, 0.0, 0.0));
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//
//
//            }
//        });
//
//        thread.start();
//
//    }

    /**
     * small class used to store data
     */
    private class Measure {

        private double lat,  lon,  weight;

        public Measure(
                double lat, double lon, double weight) {
            this.lat = lat;
            this.lon = lon;
            this.weight = weight;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public double getWeight() {
            return weight;
        }
    }
}
