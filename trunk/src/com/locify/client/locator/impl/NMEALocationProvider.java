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

import java.io.IOException;
import java.io.InputStream;

import com.locify.client.utils.StringTokenizer;

import com.locify.client.locator.LocationProvider;
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
 * @author Jiri, Menion
 *
 */
public class NMEALocationProvider extends LocationProvider {

    protected NMEAParser parser;
    protected InputStream inputStream;
    private String connectionUrl;
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
        private String nmea;
        private boolean stopRequest;
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

            StringBuffer s = new StringBuffer();
            while (!stopRequest) {
                if (inputStream != null) {
                    try {
                        s.delete(0, s.length());
                        int ch = 0;
                        //cteni dat
                        while (((ch = inputStream.read()) != '\n') && (ch != -1)) {
                            s.append((char) ch);
                        }
                        nmea = s.toString();
//Logger.debug(nmea);
                        receiveNmea(nmea);
                        if (lastLocation == null || (
                                lastLocation.getLatitude() != actualLocation.getLatitude() &&
                                lastLocation.getLongitude() != actualLocation.getLongitude())) {

                            LocationSample locSampl = new LocationSample(
                                    actualLocation.getLatitude(), actualLocation.getLongitude(), actualLocation.getAltitude(),
                                    getHorizontalAccuracy(), getVerticalAccuracy(),
                                    speed, course);

                            locationFilter.addLocationSample(locSampl);
                            actualLocation = locationFilter.getFilteredLocation();

                            //course = locationFilter.getFilteredCourse();
                            //speed = locationFilter.getFilteredSpeed();
                            parent.notifyNewLocationToListeners();

                            lastLocation = actualLocation.getLocation4DCopy();
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
        private void receiveNmea(String nmea) {
            try {
                int starIndex = nmea.indexOf('*');
                if (starIndex == -1) {
                    return;
                }
                if (nmea.length() < 10) {
                // Satellites in view
                } else if (nmea.startsWith("$GPGSV")) {
                    satManager.parseNMEASatellites(nmea);
                    nmeaCount++;
                // Geographic Latitude and Longitude
                } else if (nmea.startsWith("$GPGLL")) {
                    // not required
                    nmeaCount++;
                // NMEA format Recommender minimum
                } else if (nmea.startsWith("$GPRMC")) {
                    String[] param = StringTokenizer.getArray(nmea.substring(0, starIndex), ",");
                    //parseLatitude(param[3], param[4]);
                    //parseLongitude(param[5], param[6]);
                    fix = (param[2].charAt(0) == 'A');
                    if (fix) {
//                        day = GpsUtils.parseInt(param[9].substring(0, 2));
//                        month = GpsUtils.parseInt(param[9].substring(2, 4));
//                        year = 2000 + GpsUtils.parseInt(param[9].substring(4, 6));
                        speed = 0;
                        course = 0;
                        if (param.length > 7 && param[7].length() > 0) {
                            // recompute from knots to m/s (1 knots = 0.514444444 meters / second)
                            speed = 0.514444444f * GpsUtils.parseFloat(param[7]);
                        }
                        if (param.length > 8 && param[8].length() > 0) {
                            course = GpsUtils.parseFloat(param[8]);
                        }
                    }
                    nmeaCount++;
                // main message !!!
                } else if (nmea.startsWith("$GPGGA")) {
                    String[] param = StringTokenizer.getArray(nmea.substring(0, starIndex), ",");
                    if (param[6].equals("0")) {
                        fix = false;
                    } else if (param.length > 5) {
                        parseLatitude(param[2], param[3]);
                        parseLongitude(param[4], param[5]);
                        if (param.length > 7 && param[7].length() > 0)
                            //fixSatellites = GpsUtils.parseInt(param[7]);
                        if (param.length > 10 && param[9].length() > 0 && param[10].length() > 0) {
                            parseAltitude(param[9], param[10]);
                        }
                    }
                    nmeaCount++;
                // GPS GDOP and active satellites
                } else if (nmea.startsWith("$GPGSA")) {
                    String[] param = StringTokenizer.getArray(nmea.substring(0, starIndex), ",");
                    if (param.length > 16 && param[16].length() > 0) {
                        parseHdop(param[16]);
                    }
                    if (param.length > 17 && param[17].length() > 0) {
                        parseVdop(param[17]);
                    }
                    nmeaCount++;
                } else if (nmea.length() > 0) {
                    nmeaCount++;
                }
            } catch (Exception e) {
//                Logger.error("NMEALocationProvider.receiveNmea: " + nmea + " Ex: " + e.toString());
            }
        }

        private void parseLatitude(String value, String sfere) {
            double lat = 0.0;
            if (value.length() > 8 && sfere.length() == 1) {
                int degree, minute, fraction;
                degree = GpsUtils.parseInt(value.substring(0, 2));
                minute = GpsUtils.parseInt(value.substring(2, 4));
                fraction = GpsUtils.parseInt(value.substring(5, 9).concat("0000").substring(0, 4));
                lat = (degree + ((double) minute + (double) fraction / 10000) / 60);
                if (sfere.charAt(0) == 'S') {
                    lat = -lat;
                }
            }
            parent.actualLocation.setLatitude(lat);
        }

        private void parseLongitude(String value, String sfere) {
            double lon = 0.0;
            if (value.length() > 9 && sfere.length() == 1) {
                int degree, minute, fraction;
                degree = GpsUtils.parseInt(value.substring(0, 3));
                minute = GpsUtils.parseInt(value.substring(3, 5));
                fraction = GpsUtils.parseInt(value.substring(6, 10).concat("0000").substring(0, 4));
                lon = degree + ((double) minute + (double) fraction / 10000) / 60;
                if (sfere.charAt(0) == 'W') {
                    lon = -lon;
                }
            }
            parent.actualLocation.setLongitude(lon);
        }

        private void parseAltitude(String value, String sfere) {
            float alt = 0.0f;
            if (value.length() > 3 && sfere.length() == 1) {
                alt = GpsUtils.parseFloat(value);
            }
            parent.actualLocation.setAltitude(alt);
        }

        private void parseHdop(String value) {
            parent.horizontalAccuracy = GpsUtils.parseFloat(value);
        }

        private void parseVdop(String value) {
            parent.verticalAccuracy = GpsUtils.parseFloat(value);
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
            R.getLocator().setProviderStopped(true);
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

    /**
     * Child can override and react to disconnection
     */
    protected void notifyDisconnect() {
    }
}
