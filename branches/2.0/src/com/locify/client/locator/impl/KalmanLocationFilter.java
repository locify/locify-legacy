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
import com.locify.client.maps.projection.ReferenceEllipsoid;
import com.locify.client.maps.projection.UTMProjection;
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
    private int numOfTest = 370;
    int index1 = 1000;
    private String linSe = "\n";
    private Vector dataMeas,  dataResult,  dataPred;
    private UTMProjection utm = new UTMProjection(ReferenceEllipsoid.WGS84);
    private double[] newCoo = new double[2];

    /* number of averaged measure when standing on same place */
    private int numOfAverageMeasure = 3;
    /** temp variables used for averaging */
    private boolean weightedAverage = false;
    private double waValueLat,  waValueLon,  waWeight;
    private Vector oldMeas;
    private Measure measure;

    public KalmanLocationFilter() {
        initializeMatrix();
        oldMeas = new Vector(numOfAverageMeasure);
        if (debugMode) {
            dataMeas = new Vector(300);
            dataPred = new Vector(300);
            dataResult = new Vector(300);
        }
    }

    /**
     * Add actual measure to filtering
     * @param locSamp actual measure
     */
    public void addLocationSample(LocationSample locSamp) {
//if (debugMode) {
//    Logger.log("KLF new lat: " + locSamp.getLatitude() + " lon: " + locSamp.getLongitude());
//    if (lastSample != null)
//        Logger.log("    old lat: " + lastSample.getLatitude() + " lon: " + lastSample.getLongitude());
//}

        fTime2 = System.currentTimeMillis();
        checkMatrix();

        if (locSamp.getLatitude() == 0.0 && locSamp.getLongitude() == 0.0) {
            if (lastSample == null)
                lastSample = locSamp;
            return;
        }

        if (lastSample != null &&
                // same coordinates as measure last time
                ((Math.abs(lastMeasLat - locSamp.getLatitude()) < 0.0000001 &&
                Math.abs(lastMeasLon - locSamp.getLongitude()) < 0.0000001)
                ||
                // same coordinates as filtered (NMEA parser)
                (Math.abs(lastSample.getLatitude() - locSamp.getLatitude()) < 0.0000001 &&
                Math.abs(lastSample.getLongitude() - locSamp.getLongitude()) < 0.0000001)))
            return;

        lastMeasLat = locSamp.getLatitude();
        lastMeasLon = locSamp.getLongitude();
        lastSample = locSamp;

if (debugMode) {
    index1++;

    newCoo = utm.projectionToFlat(lastSample.getLatitude(), lastSample.getLongitude());
    dataMeas.addElement(new Measure(newCoo[1], newCoo[0], lastSample.getHorizontalAccuracy()));
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
    newCoo = utm.projectionToFlat(lastSample.getLatitude(), lastSample.getLongitude());
    dataPred.addElement(new Measure(newCoo[1], newCoo[0], lastSample.getHorizontalAccuracy()));
}
        } else {
            fHdop2 = lastSample.getHorizontalAccuracy();

            /* distance beetwen last and actual point */
            float lastDist = GpsUtils.computeDistance(state1.get(0, 0), state1.get(2, 0),
                    lastSample.getLatitude(), lastSample.getLongitude());

            /* error distance ... not perfectly but enough precise */
            float lastMaxErrorDist = (float) Math.sqrt((fHdop1 + fHdop2) / 4);

//if (debugMode) {
//    Logger.log("    HDOP1 " + GpsUtils.formatDouble(fHdop1, 1) +
//            "  HDOP2 " + GpsUtils.formatDouble(fHdop2, 1));
//    Logger.log("    lastD " + GpsUtils.formatDouble(lastDist, 2) +
//            "  lastME " + GpsUtils.formatDouble(lastMaxErrorDist, 2));
//}

            if (lastDist <= lastMaxErrorDist) {
if (debugMode) {
//    Logger.log("    ***** average *****");
    newCoo = utm.projectionToFlat(lastSample.getLatitude(), lastSample.getLongitude());
    dataPred.addElement(new Measure(newCoo[1], newCoo[0], lastSample.getHorizontalAccuracy()));
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
//    Logger.log("    ***** kalman *****");
                kalmanFilter();
                weightedAverage = false;
            }

            fTime1 = fTime2;
            fHdop1 = fHdop2;
        }

if (debugMode) {
    newCoo = utm.projectionToFlat(lastSample.getLatitude(), lastSample.getLongitude());
    dataResult.addElement(new Measure(newCoo[1], newCoo[0], lastSample.getHorizontalAccuracy()));
//System.out.println(index1 + " ");
    if (index1 == (1000 + numOfTest)) {
        Logger.log("Save start");
        saveDXF();
        saveLog();
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
        Q.set(0, 0, 10 * fHdop1 / 100.0);
        Q.set(1, 1, 10 * fHdop1 / 50.0);
        Q.set(2, 2, 10 * fHdop1 / 100.0);
        Q.set(3, 3, 10 * fHdop1 / 50.0);
        Ppre = (A.times(P)).times(A.transpose()).plus(Q);

if (debugMode) {
    newCoo = utm.projectionToFlat(state2pre.get(0, 0), state2pre.get(2, 0));
    dataPred.addElement(new Measure(newCoo[1], newCoo[0], 0.0));
}

        /********************************************/
        /*     Update phase (apriori estimate)     */
        /********************************************/
        R.set(0, 0, 100 * fHdop2 / 100.0);
        R.set(1, 1, 100 * fHdop2 / 50.0);
        R.set(2, 2, 100 * fHdop2 / 100.0);
        R.set(3, 3, 100 * fHdop2 / 50.0);
        
//if (debugMode) {
//    Logger.log("    A:\n" + A.print(2));
//    Logger.log("    P:\n" + P.print(2));
//    Logger.log("    H:\n" + H.print(2));
//    Logger.log("    Ppre:\n" + Ppre.print(2));
//    Logger.log("    R:\n" + R.print(2));
//    Logger.log("\n" + (((H.times(Ppre)).times(H.transpose())).plus(R)).print(2));
//}
        /* Kalman gain */
        K = (Ppre.times(H.transpose())).times(
                (((H.times(Ppre)).times(H.transpose())).plus(R)).inverse());
//        System.out.println("state2pre  " + state2pre.get(0, 0) + " " + state2pre.get(1, 0));
//        System.out.println(K.times(state2.minus(H.times(state2pre))).getRowDimension() + " " + K.times(state2.minus(H.times(state2pre))).getColumnDimension());
        state2 = state2pre.plus(K.times(state2.minus(H.times(state2pre))));
        P = (Matrix.identity(4, 4).minus(K.times(H))).times(Ppre);

//if (debugMode) {
//    Logger.log("    K:\n" + K.print(2));
//    Logger.log("    new P:\n" + P.print(2));
//    Logger.log("    state2:\n" + state2.print(5));
//}

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

    /**
     * Save log created during computing, only if debug enable
     * @param data
     */
    private void saveLog() {
        if (debugMode) {
            Thread thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        StringBuffer data = new StringBuffer();
                        for (int i = 0; i < dataMeas.size(); i++) {
                            Measure measure1 = (Measure) dataMeas.elementAt(i);
                            Measure measure2 = (Measure) dataPred.elementAt(i);
                            Measure measure3 = (Measure) dataResult.elementAt(i);

                            double dist1 = Math.sqrt((measure2.lat - measure1.lat) * (measure2.lat - measure1.lat) +
                                    (measure2.lon - measure1.lon) * (measure2.lon - measure1.lon));
                            double dist2 = Math.sqrt((measure3.lat - measure1.lat) * (measure3.lat - measure1.lat) +
                                    (measure3.lon - measure1.lon) * (measure3.lon - measure1.lon));
                            //data.append(i + ";");
                            data.append(GpsUtils.formatDouble(dist1, 3) + ";" + GpsUtils.formatDouble(dist2, 3) + linSe);
                        }

                        //write file
                        com.locify.client.utils.R.getFileSystem().saveString(FileSystem.LOG_FOLDER + System.currentTimeMillis() + ".diff", data.toString());
                        //view alert
                        Logger.log("succesfully saved !!!");

                    } catch (Exception e) {
                        com.locify.client.utils.R.getErrorScreen().view(e, "KalmanLocationFilter.saveLog", null);
                    }
                }
            });
            thread.start();
        }
    }

    /**
     * Save points to DXF, points created only during debug
     */
    private void saveDXF() {
        if (debugMode) {
            Thread thread = new Thread(new Runnable() {

                public void run() {
                    String errorMessage = "no error";
                    try {
                        Logger.log(" ... outDXF algorithm initialized ... \n");

                        String pointLayerMeas = "pointsMeas";
                        String pointLayerPred = "pointsPred";
                        String pointLayerResu = "pointsResu";

                        StringBuffer data = new StringBuffer();

                        data.append("0" + linSe);
                        data.append("SECTION" + linSe);
                        data.append("2" + linSe);
                        data.append("TABLES" + linSe);

                        data.append("0" + linSe);
                        data.append("TABLE" + linSe);
                        data.append("2" + linSe);
                        data.append("LAYER" + linSe);

                        data.append("0" + linSe);
                        data.append("LAYER" + linSe);
                        data.append("100" + linSe);
                        data.append("AcDbSymbolTableRecord" + linSe);
                        data.append("100" + linSe);
                        data.append("AcDbLayerTableRecord" + linSe);
                        data.append("70" + linSe);
                        data.append("0" + linSe);
                        data.append("2" + linSe);
                        data.append(pointLayerMeas + linSe);
                        data.append("6" + linSe);
                        data.append("Continuous" + linSe);
                        data.append("62" + linSe);
                        data.append("7" + linSe);

                        data.append("0" + linSe);
                        data.append("LAYER" + linSe);
                        data.append("100" + linSe);
                        data.append("AcDbSymbolTableRecord" + linSe);
                        data.append("100" + linSe);
                        data.append("AcDbLayerTableRecord" + linSe);
                        data.append("70" + linSe);
                        data.append("0" + linSe);
                        data.append("2" + linSe);
                        data.append(pointLayerPred + linSe);
                        data.append("6" + linSe);
                        data.append("Continuous" + linSe);
                        data.append("62" + linSe);
                        data.append("3" + linSe);

                        data.append("0" + linSe);
                        data.append("LAYER" + linSe);
                        data.append("100" + linSe);
                        data.append("AcDbSymbolTableRecord" + linSe);
                        data.append("100" + linSe);
                        data.append("AcDbLayerTableRecord" + linSe);
                        data.append("70" + linSe);
                        data.append("0" + linSe);
                        data.append("2" + linSe);
                        data.append(pointLayerResu + linSe);
                        data.append("6" + linSe);
                        data.append("Continuous" + linSe);
                        data.append("62" + linSe);
                        data.append("1" + linSe);

                        data.append("0" + linSe);
                        data.append("ENDTAB" + linSe);
                        data.append("0" + linSe);
                        data.append("ENDSEC" + linSe);

                        data.append("0" + linSe);
                        data.append("SECTION" + linSe);
                        data.append("2" + linSe);
                        data.append("ENTITIES" + linSe);

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
                            data.append("0" + linSe);
                            data.append("POINT" + linSe);
                            // 8 ... layer name
                            data.append("8" + linSe);
                            data.append(pointLayerMeas + linSe);
                            // 62 ... color set 256 ... by layer
                            data.append("62" + linSe);
                            data.append("256" + linSe);
                            // 10 .. X coo
                            data.append("10" + linSe);
                            data.append(meas1.getLat() + linSe);
                            data.append("20" + linSe);
                            data.append(meas1.getLon() + linSe);
                            data.append("30" + linSe);
                            data.append("0.0" + linSe);

                            /**************************/
                            /* first point of predict */
                            /**************************/
                            data.append("0" + linSe);
                            data.append("POINT" + linSe);
                            // 8 ... layer name
                            data.append("8" + linSe);
                            data.append(pointLayerPred + linSe);
                            // 62 ... color set 256 ... by layer
                            data.append("62" + linSe);
                            data.append("256" + linSe);
                            // 10 .. X coo
                            data.append("10" + linSe);
                            data.append(pred1.getLat() + linSe);
                            data.append("20" + linSe);
                            data.append(pred1.getLon() + linSe);
                            data.append("30" + linSe);
                            data.append("0.0" + linSe);

                            /*************************/
                            /* first point of result */
                            /*************************/
                            data.append("0" + linSe);
                            data.append("POINT" + linSe);
                            // 8 ... layer name
                            data.append("8" + linSe);
                            data.append(pointLayerResu + linSe);
                            // 62 ... color set 256 ... by layer
                            data.append("62" + linSe);
                            data.append("256" + linSe);
                            // 10 .. X coo
                            data.append("10" + linSe);
                            data.append(resu1.getLat() + linSe);
                            data.append("20" + linSe);
                            data.append(resu1.getLon() + linSe);
                            data.append("30" + linSe);
                            data.append("0.0" + linSe);

                            data.append("0" + linSe);
                            data.append("LINE" + linSe);
                            // 8 ... layer name
                            data.append("8" + linSe);
                            data.append(pointLayerMeas + linSe);
                            // 62 ... color set 256 ... by layer
                            data.append("62" + linSe);
                            data.append("256" + linSe);
                            // 10 .. X coo
                            data.append("10" + linSe);
                            data.append(meas1.getLat() + linSe);
                            data.append("20" + linSe);
                            data.append(meas1.getLon() + linSe);
                            data.append("30" + linSe);
                            data.append("0.0" + linSe);
                            data.append("11" + linSe);
                            data.append(meas2.getLat() + linSe);
                            data.append("21" + linSe);
                            data.append(meas2.getLon() + linSe);
                            data.append("31" + linSe);
                            data.append("0.0" + linSe);

                            data.append("0" + linSe);
                            data.append("LINE" + linSe);
                            // 8 ... layer name
                            data.append("8" + linSe);
                            data.append(pointLayerPred + linSe);
                            // 62 ... color set 256 ... by layer
                            data.append("62" + linSe);
                            data.append("256" + linSe);
                            // 10 .. X coo
                            data.append("10" + linSe);
                            data.append(pred1.getLat() + linSe);
                            data.append("20" + linSe);
                            data.append(pred1.getLon() + linSe);
                            data.append("30" + linSe);
                            data.append("0.0" + linSe);
                            data.append("11" + linSe);
                            data.append(pred2.getLat() + linSe);
                            data.append("21" + linSe);
                            data.append(pred2.getLon() + linSe);
                            data.append("31" + linSe);
                            data.append("0.0" + linSe);

                            data.append("0" + linSe);
                            data.append("LINE" + linSe);
                            // 8 ... layer name
                            data.append("8" + linSe);
                            data.append(pointLayerResu + linSe);
                            // 62 ... color set 256 ... by layer
                            data.append("62" + linSe);
                            data.append("256" + linSe);
                            // 10 .. X coo
                            data.append("10" + linSe);
                            data.append(resu1.getLat() + linSe);
                            data.append("20" + linSe);
                            data.append(resu1.getLon() + linSe);
                            data.append("30" + linSe);
                            data.append("0.0" + linSe);
                            data.append("11" + linSe);
                            data.append(resu2.getLat() + linSe);
                            data.append("21" + linSe);
                            data.append(resu2.getLon() + linSe);
                            data.append("31" + linSe);
                            data.append("0.0" + linSe);
                        }

                        data.append("0" + linSe);
                        data.append("ENDSEC" + linSe);
                        data.append("0" + linSe);
                        data.append("EOF" + linSe);

                        //write file
                        com.locify.client.utils.R.getFileSystem().saveString(FileSystem.LOG_FOLDER + System.currentTimeMillis() + ".dxf", data.toString());
                        Logger.log(" ... outDXF algorithm finished ... \n");
                    } catch (Exception e) {
                        com.locify.client.utils.R.getErrorScreen().view(e, "KalmanLocationFilter.saveDXF", null);
                    }
                }
            });

            thread.start();
        }
    }

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
