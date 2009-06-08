/*
 * LocationProvider.java
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

import com.locify.client.locator.impl.DummyLocationFilter;
import com.locify.client.locator.impl.KalmanLocationFilter;
import com.locify.client.utils.R;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import de.enough.polish.android.location.QualifiedCoordinates;

/**
 * This abstract class represents any type of location acquiring and filtering. LocationProvider continously monitors position 
 * using some method and in case of some change sends notification using LocationEventListener to all registrered listeners.
 * 
 * @see LocationEventListener
 * 
 * @author Jiri Stepan
 *
 */
public abstract class LocationProvider implements LocationEventGenerator {
    /* potential states of GPS location provider */
    public static final int STATE_UNKNOWN = 0; //nothing selected
    public static final int WAITING = 2; //no fix
    public static final int READY = 3; //fix
    public static final int CONNECTING = 4; //connecting to gps
    public static final int MANUAL = 5; //from location context
    protected Vector listeners;

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

//    /** phone GPS precision in metres */
//    public float phoneGpsPrec = 1.0f;
    
    public LocationProvider() {
        listeners = new Vector();
//        this.locationFilter = new DummyLocationFilter();
        this.locationFilter = new KalmanLocationFilter();
        this.satManager = new SatelliteManager();
    }

    public void setState(int newstate) {
        try {
            if (newstate != state) {
                this.state = newstate;
                notifyChangeStateToListeners();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GpsLocationProvider.setState", String.valueOf(newstate));
        }
    }

    /** @return satellites in hashtable */
    public Hashtable getSatInView() {
        return satManager.getSatInView();
    }
    
    /**
     * Returns last location after necessary filtering
     */
    public Location4D getActualLocation() {
        return actualLocation;
    }

    public float getAltitude() {
        return altitude;
    }

    /**
     * Returns last location sample before filtering. It means raw data.
     */
    public LocationSample getLastSample() {
        return lastSample;
    }

    /**
     * Returns state of location provider eg. NOT AVAILABLE, WAITING, READY
     * @see com.locify.client.locator.LocatorModel
     */
    public int getState() {
        return state;
    }

    /** @retun speen in m/s */
    public float getSpeed() {
        return speed;
    }

    /** @return course(heading) in radians */
    public float getCourse() {
        return course;
    }

    /** @return accuracy in meters */
    public float getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    /** @return accuracy in meters */
    public float getVerticalAccuracy() {
        return verticalAccuracy;
    }

    /** stops provider */
    public void stopProvider() {
        this.state = LocationProvider.CONNECTING;
        this.listeners = new Vector(); //delete all listeners
    }

    /**************** locationEventGenerator ********************/
    /** sets new state to all listeners */
    synchronized public void notifyChangeStateToListeners() {
        Enumeration lst = this.listeners.elements();
        while (lst.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) lst.nextElement();
            list.stateChanged(this, getState());
        }
    }

    /** sets new location to all listeners */
    synchronized public void notifyNewLocationToListeners() {
        Enumeration lst = this.listeners.elements();
        while (lst.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) lst.nextElement();
            list.locationChanged(this, getActualLocation());
        }
    }

    synchronized public void notifyErrorMessageToListeners(String message) {
        Enumeration lst = this.listeners.elements();
        while (lst.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) lst.nextElement();
            list.errorMessage(this, message);
        }

    }

    public void notifyMessageToListener(String message) {
        Enumeration lst = this.listeners.elements();
        while (lst.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) lst.nextElement();
            list.message(this, message);
        }
    }

    /** Registers a listener for GPS events*/
    public synchronized void addLocationChangeListener(LocationEventListener listener) {
        listeners.addElement(listener);
    }

    /** Unregisters a listener. */
    public synchronized void removeLocationChangeListener(LocationEventListener listener) {
        listeners.removeElement(listener);
    }
}
