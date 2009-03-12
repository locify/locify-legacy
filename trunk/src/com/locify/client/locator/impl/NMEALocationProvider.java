/*
 * NMEALocationProvider.java
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
import java.io.IOException;
import java.io.InputStream;

import com.locify.client.utils.StringTokenizer;

import com.locify.client.locator.GpsLocationProvider;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationSample;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/** 
 * 
 * This location provider reads reads continously InputStream containing NMEA messages. Afret parsing thme is ready to return 
 * actualLocation and other location data. 
 * 
 * NMEALocationProvider doesn't have any method for opening InputStream. It assumes open InputStream in setInputStream(InputStream) method.
 * 
 * @see SimulatorLocationProvider
 * 
 * @author Jiri Stepan
 *
 */
public class NMEALocationProvider extends GpsLocationProvider {

    protected NMEAParser parser;
    protected InputStream inputStream;
    private String connectionUrl;
    /** phone GPS precision in metres */
    public float phoneGpsPrec = 1.0f;
    protected long gpsAlive = 0;

    public NMEAParser getParser() {
        return parser;
    }

    public void setParser(NMEAParser parser) {
        this.parser = parser;
    }

    protected class NMEAParser implements Runnable {

        private String communicationURL;
        private NMEALocationProvider parent;
        private int nmeaCount;
        private boolean fix;
        private int day;
        private int month;
        private int year;
        private int fixSatellites;
        private String nmea;
        private boolean stopRequest;
        private boolean waitingStarted;
        private Location4D lastLocation;

        public NMEAParser(NMEALocationProvider parent) {
            super();
            actualLocation = new Location4D(0, 0, 0, 0);
            this.parent = parent;

        }

        public void run() {
            stopRequest = false;
            setState(CONNECTING);
            parent.gpsAlive = 0;
            //cteni dat
            while (!stopRequest) {
                if (inputStream != null) {

                    try {
                        String s = "";
                        int ch = 0;
                        //cteni dat
                        if (inputStream != null) {
                            while ((ch = inputStream.read()) != '\n') {
                                s += (char) ch;
                            }
                        }

                        nmea = s;
                        receiveNmea(s);
                        if (lastLocation == null || (
                                lastLocation.getLatitude() != actualLocation.getLatitude() &&
                                lastLocation.getLongitude() != actualLocation.getLongitude())) {
//System.out.println("actualLoc: " + actualLocation);

                            LocationSample locSampl = new LocationSample(
                                    actualLocation.getLatitude(), actualLocation.getLongitude(), actualLocation.getAltitude(),
                                    horizontalAccuracy, verticalAccuracy,
                                    speed, course);

                            locationFilter.addLocationSample(locSampl);
                            actualLocation = locationFilter.getFilteredLocation();

                            //course = locationFilter.getFilteredCourse();
                            //speed = locationFilter.getFilteredSpeed();
                            parent.notifyNewLocationToListeners();

                            lastLocation = actualLocation.getLocation4DCopy();
//System.out.println("filteredLoc: " + actualLocation);
                        }

                        parent.gpsAlive = System.currentTimeMillis();

                        //nastaveni gps statusu
                        if (!fix) {
                            setState(WAITING);
                        } else {
                            setState(READY);
                        }

                    } catch (Exception e) {
                        stop();
                    }
                } else {
                    stop();
                }
            }
        }

        public void stop() {
            try {
                stopRequest = true;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                    }
                    inputStream = null;
                }
                parent.notifyDisconnect();
            } catch (Exception e) {
                R.getErrorScreen().view(e, "NMEALocationProvider.stop", null);
            }
        }

        /**
         * Parses NMEA message
         * @param nmea nmea string to be parsed
         */
        protected void receiveNmea(String nmea) {
            try {
                //nmea logging - should be commented in release version
                //R.getFileSystem().saveStringToEof(FileSystem.LOG_FOLDER + "nmea.log", nmea+"\n");

                int starIndex = nmea.indexOf('*');
                if (starIndex == -1) {
                    return;
                }
                String[] param = StringTokenizer.getArray(nmea.substring(0, starIndex), ",");
                if (param[0] == null) {
                // satellites in view
                } else if (param[0].equals("$GPGSV")) {
                    nmeaCount++;
                    satManager.parseNMEASatellites(param);
                // geographic Latitude and Longitude
                } else if (param[0].equals("$GPGLL")) {
                    nmeaCount++;
                    // not required
                    //extractData(param, 1, 2, 3, 4, 5);
                    //fix = (param[6].charAt(0) == 'A');
                // NMEA format recommender minimum
                } else if (param[0].equals("$GPRMC")) {
                    nmeaCount++;
                    extractData(param, 3, 4, 5, 6, 1);
                    fix = (param[2].charAt(0) == 'A');
                    if (fix) {
                        day = GpsUtils.parseInt(param[9].substring(0, 2));
                        month = GpsUtils.parseInt(param[9].substring(2, 4));
                        year = 2000 + GpsUtils.parseInt(param[9].substring(4, 6));
                        if (param[7].length() > 0) {
                            speed = GpsUtils.parseFloat(param[7]);
                        } else {
                            speed = 0;
                        }
                        if (param[8].length() > 0) {
                            course = GpsUtils.parseFloat(param[8]);
                        }
                    }
                // main message
                } else if (param[0].equals("$GPGGA")) {
                    nmeaCount++;
                    if (param[6].equals("0")) {
                        fix = false;
                    } else {
                        extractData(param, 2, 3, 4, 5, 1);
                        fixSatellites = GpsUtils.parseInt(param[7]);
                        if (param[9].length() > 0) {
                            //parent.altitude = parse3(param[9]);
                            parent.actualLocation.setAltitude(GpsUtils.parseFloat(param[9]));
                        }
                    }
                // dop and satellites
                } else if (param[0].equals("$GPGSA")) {
                    if (param.length > 16 && param[16].length() > 0) {
                        parent.horizontalAccuracy = GpsUtils.parseFloat(param[16]);
                    }
                    if (param.length > 17 && param[17].length() > 0) {
                        parent.verticalAccuracy = GpsUtils.parseFloat(param[17]);
                    }
                    nmeaCount++;
                } else if (!param[0].equals(null)) {
                    nmeaCount++;
                }
            } catch (Exception e) {
                //R.getErrorScreen().view(e, "NMEALocationProvider.receiveNmea", nmea);
                Logger.error("NMEALocationProvider.receiveNmea: " + nmea + " Ex: " + e.toString());
            }
        }

        /**
         * Extracts coordinats from various NMEAs
         */
        private void extractData(String[] param, int a, int b, int c, int d, int e) {
            try {
                int degree, minute, fraction;

                if (param[a].length() > 8 && param[b].length() == 1) {
                    degree = GpsUtils.parseInt(param[a].substring(0, 2));
                    minute = GpsUtils.parseInt(param[a].substring(2, 4));
                    fraction = GpsUtils.parseInt(param[a].substring(5, 9).concat("0000").substring(0, 4));
                    double lat = (degree + ((double) minute + (double) fraction / 10000) / 60);
                    if (param[b].charAt(0) == 'S') {
                        lat = -lat;
                    }
                    parent.actualLocation.setLatitude(lat);

                }
                if (param[c].length() > 9 && param[d].length() == 1) {
                    degree = GpsUtils.parseInt(param[c].substring(0, 3));
                    minute = GpsUtils.parseInt(param[c].substring(3, 5));
                    fraction = GpsUtils.parseInt(param[c].substring(6, 10).concat("0000").substring(0, 4));
                    double lon = degree + ((double) minute + (double) fraction / 10000) / 60;
                    if (param[d].charAt(0) == 'W') {
                        lon = -lon;
                    }

                    parent.actualLocation.setLongitude(lon);
                }
            } catch (Exception ex) {
                String error = "params=";
                for (int i=0;i<param.length;i++)
                {
                    error += param[i]+";";
                }
                error += "a="+a+"b="+b+"c="+c+"d="+d+"e="+e;
                R.getErrorScreen().view(ex, "NMEALocationProvider.extractData", error);
            }
        }

        public String getCommunicationURL() {
            return communicationURL;
        }

        public void setCommunicationURL(String communicationURL) {
            this.communicationURL = communicationURL;
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream in) {
        this.inputStream = in;
        parser = new NMEAParser(this);
        new Thread(parser).start();
    }

    public NMEALocationProvider() {
        //this.setInputStream(this.getInputStream());
    }

    public void stopProvider() {
        try {
            if (parser != null) {
                parser.stop();
            }
            parser = null;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            R.getLocator().setProviderStopped(true);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NMEALocationProvider.stopProvider", null);
        }
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;

        try {
            StreamConnection streamConnection = null;
            streamConnection = (StreamConnection) Connector.open(connectionUrl);
            inputStream = streamConnection.openInputStream();
            this.setInputStream(inputStream);
        } catch (Exception ex) {
            R.getSettings().setLastDevice("");
        }
    }

    public float getHorizontalAccuracy() {
        return horizontalAccuracy * phoneGpsPrec;
    }

    /**
     * Child can override and react to disconnection
     */
    protected void notifyDisconnect() {
    }
}
