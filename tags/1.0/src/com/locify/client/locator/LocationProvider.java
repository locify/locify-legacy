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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
   
    public LocationProvider() {
        listeners = new Vector();
    }

    /**
     * Returns last location after necessary filtering
     */
    public abstract Location4D getActualLocation();

    /**
     * Returns last location sample before filtering. It means raw data.
     */
    public abstract LocationSample getLastSample();

    /**
     * Returns state of location provider eg. NOT AVAILABLE, WAITING, READY
     * @see com.locify.client.locator.LocatorModel
     */
    public abstract int getState();

    /** @retun speen in m/s */
    public abstract float getSpeed();

    /** @return course(heading) in radians */
    public abstract float getCourse();

    /** @return accuracy in meters */
    public abstract float getHorizontalAccuracy();

    /** @return accuracy in meters */
    public abstract float getVerticalAccuracy();

    /** @return satellites in hashtable */
     public abstract Hashtable getSatInView();

    /** stops provider */
    public abstract void stopProvider();

    /**************** locationEventGenerator ********************/
    /** sets new state to all listeners */
    synchronized public void notifyChangeStateToListeners() {
        Enumeration listeners = this.listeners.elements();
        while (listeners.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) listeners.nextElement();
            list.stateChanged(this, getState());
        }
    }

    /** sets new location to all listeners */
    synchronized public void notifyNewLocationToListeners() {
        Enumeration listeners = this.listeners.elements();
        while (listeners.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) listeners.nextElement();
            list.locationChanged(this, getActualLocation());
        }
    }

    synchronized public void notifyErrorMessageToListeners(String message) {
        Enumeration listeners = this.listeners.elements();
        while (listeners.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) listeners.nextElement();
            list.errorMessage(this, message);
        }

    }

    public void notifyMessageToListener(String message) {
        Enumeration listeners = this.listeners.elements();
        while (listeners.hasMoreElements()) {
            LocationEventListener list = (LocationEventListener) listeners.nextElement();
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
