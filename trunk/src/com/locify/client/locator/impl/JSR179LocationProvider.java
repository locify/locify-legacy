/*
 * JSR179LocationProvider.java
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

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import com.locify.client.locator.GpsLocationProvider;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationSample;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;

/**
 * This LocationProvider acquires data from internal GPS using javax.microedition.location api.
 * @author Jiri Stepan
 */
public class JSR179LocationProvider extends GpsLocationProvider {

    private float lastValidCourse = 0;
    private float lastValidSpeed = 0;
    private javax.microedition.location.LocationProvider internalProvider; //pozor tohle je jiny LocationProvider	

    private final LocationListener locationListener = new LocationListener() {

        public void locationUpdated(LocationProvider lp, Location location) {
            doUpdateLocation(location);
        }

        private void doUpdateLocation(Location new_location) {
            try {
                if (new_location.isValid()) {
                    String NMEA = new_location.getExtraInfo("application/X-jsr179-location-nmea");
                    if (NMEA != null && NMEA.length() > 0 && NMEA.trim().startsWith("$GPGSV"))
                        satManager.parseNMEASatellites(NMEA);

                    currentCoordinates = new_location.getQualifiedCoordinates();
                    if (!((currentCoordinates.getLatitude() == 0) && (currentCoordinates.getLongitude() == 0))) {
                        actualLocation = new Location4D(currentCoordinates.getLatitude(), currentCoordinates.getLongitude(), altitude = currentCoordinates.getAltitude());
                        horizontalAccuracy = (currentCoordinates).getHorizontalAccuracy();
                        verticalAccuracy = (currentCoordinates).getVerticalAccuracy();
                        speed = new_location.getSpeed();
                        if (Float.isNaN(speed)) {
                            speed = lastValidSpeed;
                        } else {
                            lastValidSpeed = speed;
                        }
                        course = new_location.getCourse();
                        if (Float.isNaN(course)) {
                            course = lastValidCourse;
                        } else {
                            lastValidCourse = course;
                        }
                        //send to filter
                        LocationSample locSampl = new LocationSample(actualLocation.getLatitude(), actualLocation.getLongitude(), actualLocation.getAltitude(), horizontalAccuracy, verticalAccuracy, speed, course);
                        locationFilter.addLocationSample(locSampl);
                        actualLocation = locationFilter.getFilteredLocation();

                        //notify the change
                        notifyNewLocationToListeners();
                    }
                }
                if ((!new_location.isValid()) || (actualLocation == null) || ((actualLocation.getLatitude() == 0.0) && (actualLocation.getLongitude() == 0.0) && (course == 0.0))) {
                    setState(com.locify.client.locator.LocationProvider.WAITING);
                } else {
                    setState(com.locify.client.locator.LocationProvider.READY);
                }
            } catch (Exception e) {
                R.getErrorScreen().view(e, "JSR179LocationProvider.doUpdateLocation", null);
            }
        }

        public void providerStateChanged(LocationProvider lp, int newState) {
            switch (newState) {
                case LocationProvider.OUT_OF_SERVICE:
                    setState(com.locify.client.locator.LocationProvider.CONNECTING);
                    break;
                case LocationProvider.AVAILABLE:
                    setState(com.locify.client.locator.LocationProvider.WAITING);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    setState(com.locify.client.locator.LocationProvider.WAITING);
                    break;
                default:
                    setState(com.locify.client.locator.LocationProvider.STATE_UNKNOWN);
            }
            notifyChangeStateToListeners();
        }
    };

    public JSR179LocationProvider() {
        super();
        createInternalProvider();

    }

    public void stopProvider() {
        System.out.println("stopping provider");
        super.stopProvider();
        internalProvider.setLocationListener(null, 0, 0, 0);
        R.getLocator().setProviderStopped(true);
    }

    /**
     * Initializes internalProvider
     * uses default criteria
     */
    void createInternalProvider() {
        try {
            if (internalProvider == null) {
                Criteria gps = new Criteria();
                gps.setPreferredResponseTime(1000);
                gps.setAltitudeRequired(true);
                gps.setSpeedAndCourseRequired(true);
                Criteria cellid = new Criteria();
                try {
                    internalProvider = LocationProvider.getInstance(gps);
                    if (internalProvider == null) {
                        internalProvider = LocationProvider.getInstance(cellid);
                    }
                } catch (LocationException le) {
                    Logger.error("Cannot create LocationProvider for this criteria");
                }
                internalProvider.setLocationListener(locationListener, 3, 1, 1);
                locationListener.providerStateChanged(internalProvider, internalProvider.getState());
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "JSR179LocationProvider.createInternalProvider", null);
        }
    }
}
