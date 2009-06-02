/*
 * LocatorModel.java
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

import com.locify.client.data.SettingsData;
import java.util.Enumeration;
import java.util.Vector;
import com.locify.client.utils.R;
import com.locify.client.locator.impl.BTNMEALocationProvider;
import com.locify.client.locator.impl.COMNMEALocationProvider;
import com.locify.client.locator.impl.JSR179LocationProvider;
import com.locify.client.locator.impl.SimulatorLocationProvider;
import com.locify.client.locator.impl.TCPNMEALocationProvider;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Locale;
import java.util.Date;
import java.util.Hashtable;

/**
 * Class is responsible for separating LocationProviders into separate thread and managing them.
 * @author Jiri Stepan
 **/
public class LocatorModel extends Thread implements LocationEventListener, LocationEventGenerator {

    /** simple class used for wainting for GPS signal */
    private class WaitForLocator extends Thread {

        private static final long TIMEOUT = 60000; //timeout in milis
        private static final long SLEEP_TIME = 1000; //sleep time
        private boolean stop;
        private boolean hasFix;

        public void run() {
            try {
                stop = false;
                hasFix = false;
                long start = new Date().getTime();
                long delta = 0;
                do {
                    delta = new Date().getTime() - start;

                    if (hasFix()) {
                        hasFix = true;
                        break;
                    }
                    try {
                        sleep(SLEEP_TIME);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } while ((delta < TIMEOUT) & (!stop));

                if (stop) {
                    return;
                }
                if (hasFix) {
                    notifyNewLocationToListeners();
                    notifyMessageToListener("Has_fix");
                }
            } catch (Exception e) {
                R.getErrorScreen().view(e, "LocatorModel.run()", null);
            }
        }

        private void stop() {
            stop = true;
        }
    }
    private WaitForLocator waitThread = null;
    private LocationProvider locationProvider;
    private Vector providers; // all providers according to capabilities
    protected Vector listeners;
    private String providerChangeRequest = null;
    private Location4D lastLocation; //last aquired location from gps
    private int state;
    private float locationQuality = 0; //<0-1>
    private String requestAction;
    private boolean providerSelected = false; // indikuje zda byl vybec nejaky provider vybran
    private boolean providerStopped = false;
    private boolean satScreenActive = false;

    public LocatorModel() {
        this.listeners = new Vector();
        requestAction = "";
    }

    /**
     * Loads settings at the app start
     */
    public void load() {
        try {
            R.getLoading().setText(Locale.get("Loading_locator"));
            //acquire last location
            lastLocation = R.getSettings().getLastLocation();
            //provider adding
            providers = new Vector();
            if (Capabilities.hasJSR179()) {
                providers.addElement(new Provider("JSR179LocationProvider", Locale.get("JSR179LocationProvider"), "internal_gps_21x21.png"));
            }
            if (Capabilities.hasBluetooth()) {
                providers.addElement(new Provider("BTNMEALocationProvider", Locale.get("BTNMEALocationProvider"), "bluetooth_gps_21x21.png"));
            }
            if (Capabilities.isWindowsMobile()) {
                providers.addElement(new Provider("TCPNMEALocationProvider", Locale.get("TCPNMEALocationProvider"), "pda_porter_21x21.png"));
            }
            if (Capabilities.isSonyEricsson() && Capabilities.hasCOMMs()) {
                providers.addElement(new Provider("COMNMEALocationProvider", Locale.get("COMNMEALocationProvider"), "com_gps_21x21.png"));
            }
            //#if !release || applet
            providers.addElement(new Provider("SimulatorLocationProvider", Locale.get("SimulatorLocationProvider"), "simulator_gps_21x21.png"));
        //#endif
        } catch (Exception e) {
            R.getErrorScreen().view(e, "LocatorModel.load", null);
        }
    }
    private static final int SIGNAL_TIMEOUT = 120;

    public String[] getProviderNames() {
        String[] names = new String[providers.size()];
        for (int i = 0; i < providers.size(); i++) {
            names[i] = ((Provider) providers.elementAt(i)).getName();
        }
        return names;
    }

    public String[] getProviderIcons() {
        String[] icons = new String[providers.size()];
        for (int i = 0; i < providers.size(); i++) {
            icons[i] = ((Provider) providers.elementAt(i)).getIcon();
        }
        return icons;
    }

    public void setSatScreenActive(boolean active) {
        satScreenActive = active;
    }

    public boolean isSatScreenActive() {
        return satScreenActive;
    }

    public boolean hasSatellites() {
        if (R.getContext().getSource() != LocationContext.GPS) {
            return false;
        }
        if (locationProvider == null) {
            return false;
        }
        return true;
    }

    public int getProviderCount() {
        return providers.size();
    }

    public synchronized LocationProvider getLocationProvider() {
        return this.locationProvider;
    }

    public void setProviderStopped(boolean stopped) {
        providerStopped = stopped;
        locationProvider = null;
        providerChangeRequest = null;
    }

    /** 
     * Starts new gps location provider
     */
    public synchronized void connectGps() {
        try {
            providerSelected = true;
            String oldProvider = this.providerChangeRequest;
            if (R.getSettings().getPrefferedGps() == SettingsData.AUTODETECT) {
                if (Capabilities.isSonyEricsson() && Capabilities.hasCOMMs() && Capabilities.isHGE100Connected()) {
                    this.providerChangeRequest = "COMNMEALocationProvider";
                } else {
                    this.providerChangeRequest = ((Provider) providers.firstElement()).getClassName();
                }
            } else {
                try {
                    this.providerChangeRequest = ((Provider) providers.elementAt(R.getSettings().getPrefferedGps() - 1)).getClassName();
                } catch (ArrayIndexOutOfBoundsException e) {
                    this.providerChangeRequest = ((Provider) providers.firstElement()).getClassName();
                }
            }
            if (oldProvider == null || !providerChangeRequest.equals(oldProvider)) {
                providerStopped = false;
                locationProvider = this.getLocationProviderInstance(providerChangeRequest);
            }
            if (!providerStopped) {
                R.getContext().setLocation(null, LocationContext.GPS, null);
                locationProvider.addLocationChangeListener(this);
                this.stateChanged(locationProvider, locationProvider.getState());
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "LocatorModel.connectGps", null);
        }
    }

    public float getLocationQuality() {
        return locationQuality;
    }

    public boolean hasValidLocation() {
        return (getState() == LocationProvider.READY || getState() == LocationProvider.MANUAL || R.getContext().isTemporary());
    }

    public void setLastLocation(Location4D lastLocation) {
        this.lastLocation = lastLocation;
    }

    /******* LocationEventListener section *******/
    public void message(LocationEventGenerator sender, String message) {
        this.notifyMessageToListener(message);
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
        this.notifyErrorMessageToListeners(message);
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        if (((sender != R.getContext() && R.getContext().getSource() == LocationContext.GPS) || sender == R.getContext()) && !R.getContext().isTemporary()) {
            this.lastLocation = location;
            this.notifyNewLocationToListeners();
        }
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
        if ((sender != R.getContext() && R.getContext().getSource() == LocationContext.GPS) || sender == R.getContext()) {
            this.state = state;
            this.notifyChangeStateToListeners();
        }
    }

    /******* LocationEventGenerator section *******/
    /** sets new state to all listeners */
    synchronized public void notifyChangeStateToListeners() {
        Enumeration listeners = this.listeners.elements();
        if (listeners != null) {
            while (listeners.hasMoreElements()) {
                LocationEventListener list = (LocationEventListener) listeners.nextElement();
                list.stateChanged(this, state);
            }
        }
    }

    /** sets new location to all listeners */
    synchronized public void notifyNewLocationToListeners() {
        Enumeration listeners = this.listeners.elements();
        if (listeners != null) {
            while (listeners.hasMoreElements()) {
                LocationEventListener list = (LocationEventListener) listeners.nextElement();
                list.locationChanged(this, lastLocation);
            }
        }
    }

    synchronized public void notifyErrorMessageToListeners(String message) {
        Enumeration listeners = this.listeners.elements();
        if (listeners != null) {
            while (listeners.hasMoreElements()) {
                LocationEventListener list = (LocationEventListener) listeners.nextElement();
                list.errorMessage(this, message);
            }
        }
    }

    public void notifyMessageToListener(String message) {
        Enumeration listeners = this.listeners.elements();
        if (listeners != null) {
            while (listeners.hasMoreElements()) {
                LocationEventListener list = (LocationEventListener) listeners.nextElement();
                list.message(this, message);
            }
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

    public int getState() {
        return state;
    }

    public Location4D getLastLocation() {
        return lastLocation;
    }

    /**
     * Has current location provider fix?
     * @return whether has fix
     */
    public boolean hasFix() {
        boolean fix = false;
        if (getLocationProvider() == null) {
            fix = false;
        } else {
            fix = (getLocationProvider().getState() == LocationProvider.READY);
        }
        return fix;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }

    public float getSpeed() {
        if (locationProvider != null) {
            return locationProvider.getSpeed();
        }
        return 0;
    }

    public float getHeading() {
        if (locationProvider != null) {
            return locationProvider.getCourse();
        }
        return 0;
    }

    public float getAccuracyHorizontal() {
        if (locationProvider != null) {
            return locationProvider.getHorizontalAccuracy();
        }
        return 0;
    }

    public float getAccuracyVertical() {
        if (locationProvider != null) {
            return locationProvider.getVerticalAccuracy();
        }
        return 0;
    }

    public Hashtable getSatInView() {
        if (locationProvider != null) {
            return locationProvider.getSatInView();
        }
        return null;
    }

    public boolean isProviderSelected() {
        return providerSelected;
    }

    public void setProviderSelected(boolean providerSelected) {
        this.providerSelected = providerSelected;
    }

    public boolean isSimulatedGPS() {
        return (providerChangeRequest.equals("SimulatorLocationProvider"));
    }

    /** instead of using Class.forName() */
    private LocationProvider getLocationProviderInstance(String classname) {
        if ("JSR179LocationProvider".equals(classname)) {
            return new JSR179LocationProvider();
        } else if ("BTNMEALocationProvider".equals(classname)) {
            return new BTNMEALocationProvider();
        } else if ("TCPNMEALocationProvider".equals(classname)) {
            return new TCPNMEALocationProvider();
        } else if ("COMNMEALocationProvider".equals(classname)) {
            return new COMNMEALocationProvider();
        } else if ("SimulatorLocationProvider".equals(classname)) {
            return new SimulatorLocationProvider();
        }
        return null;
    }

    /** starts wait thread, which checks if device i still not connected */
    public void startWaitForLocation() {
        if ((!hasFix())) { //dont run wait thread if this provider isn't real
            /* start wainting thread */
            if (waitThread != null) {
                waitThread.stop();
            }
            waitThread = new WaitForLocator();
            waitThread.start();
        }
    }

    public void stopWaitForLocator() {
        if (waitThread != null) {
            waitThread.stop();
        }
    }
}

class Provider {

    private String className;
    private String icon;
    private String name;

    public Provider(String className, String name, String icon) {
        this.className = className;
        this.name = name;
        this.icon = icon;
    }

    public String getClassName() {
        return className;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
