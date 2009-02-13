/*
 * GpsLocationProvider.java
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
package com.locify.client.locator;

import java.util.Vector;
import javax.microedition.location.QualifiedCoordinates;
import com.locify.client.locator.impl.KalmanLocationFilter;
import com.locify.client.utils.R;
import java.util.Hashtable;

/**
 * Abstract class represents GPS-based location providers. 
 * @author Jiri Stepan
 */
public abstract class GpsLocationProvider extends LocationProvider {

    private static final String LOCATION_PROVIDER_NAME = "GenericGPSLocationProvider";
    protected int state; //is GPS ready to navigate?
    protected LocationFilter locationFilter;
    protected Location4D actualLocation;
    protected LocationSample lastSample;
    /* gps data */
    protected QualifiedCoordinates oldCoordinates;
    protected QualifiedCoordinates currentCoordinates;
    protected float speed;
    protected float course;
    protected float horizontalAccuracy = 100;
    protected float verticalAccuracy = 100;
    protected float altitude;
    
    protected SatelliteManager satManager;

    public GpsLocationProvider() {
        super();
        //this.locationFilter = new DummyLocationFilter();
        this.locationFilter = new KalmanLocationFilter();
        this.satManager = new SatelliteManager();
    }

    public String getName() {
        return LOCATION_PROVIDER_NAME;
    }

    public void setState(int newstate) {
        try
        {
        if (newstate != state) {
            this.state = newstate;
            notifyChangeStateToListeners();
        }
        }
        catch (Exception e)
        {
            R.getErrorScreen().view(e, "GpsLocationProvider.setState", String.valueOf(newstate));
        }
    }

    public Hashtable getSatInView() {
        return satManager.getSatInView();
    }

    public Location4D getActualLocation() {
        return actualLocation;
    }

    public float getAltitude() {
        return altitude;
    }

    public float getCourse() {
        return course;
    }

    public float getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    public LocationSample getLastSample() {
        return lastSample;
    }

    public float getSpeed() {
        return speed;
    }

    public int getState() {
        return state;
    }

    public float getVerticalAccuracy() {
        return verticalAccuracy;
    }

    public void stopProvider() {
        this.state = LocationProvider.CONNECTING;
        this.listeners = new Vector(); //delete all listeners
    }
}
	