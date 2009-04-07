/*
 * LocationContext.java
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

import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;

/**
 * This class provides methods for acquiring location context other than gps
 * @author Destil
 */
public class LocationContext implements LocationEventGenerator {

    private int source = 4;
    private Location4D location = null;
    private boolean temporary = false;
    private boolean back = false;
    private String backScreen = "";
    private String sourceData = "";
    public static final int GPS = 0;
    public static final int SAVED_LOCATION = 1;
    public static final int ADDRESS = 2;
    public static final int COORDINATES = 3;
    public static final int LAST_KNOWN = 4;
    public Command[] commands;
    public String[] actions;

    public LocationContext() {
        commands = new Command[5];
        commands[GPS] = new Command(Locale.get("Gps"), Command.SCREEN, 10);
        commands[SAVED_LOCATION] = new Command(Locale.get("Saved_location"), Command.SCREEN, 11);
        commands[ADDRESS] = new Command(Locale.get("Address"), Command.SCREEN, 12);
        commands[COORDINATES] = new Command(Locale.get("Coordinates"), Command.SCREEN, 13);
        commands[LAST_KNOWN] = new Command(Locale.get("Last_known"), Command.SCREEN, 14);
        actions = new String[5];
        actions[GPS] = "locify://gps";
        actions[SAVED_LOCATION] = "locify://files?to=location&filter=place";
        actions[ADDRESS] = "locify://address";
        actions[COORDINATES] = "locify://coordinates";
        actions[LAST_KNOWN] = "locify://lastKnown";
    }

    /**
     * Set up new location context
     * @param location  new location
     * @param source location source
     * @param sourceData additional source data eg. geocoded address
     */
    public void setLocation(Location4D location, int source, String sourceData) {
        if (location != null) {
            R.getLocator().setProviderSelected(true);
        }
        if (source != GPS) {
            LocationProvider lp = R.getLocator().getLocationProvider();
            if (lp != null) {
                lp.stopProvider();
            }
        }
        if (temporary) {
            R.getLocator().setLastLocation(location);
            R.getBack().deleteLast();
            R.getURL().call(backScreen);
        } else {
            this.location = location;
            this.source = source;
            this.sourceData = sourceData;
            notifyNewLocationToListeners();
            notifyChangeStateToListeners();
            if (back) {
                R.getURL().call(backScreen);
            } else {
                R.getURL().call("locify://mainScreen");
            }
        }
    }

    /**
     * Removes temporary location - has to be called when form is leaved
     */
    public void removeTemporaryLocation() {
        temporary = false;
        if (source != GPS) {
            R.getLocator().setLastLocation(location);
        }
    }

    public int getSource() {
        return source;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporaryScreen(String temporaryScreen) {
        temporary = true;
        this.backScreen = temporaryScreen;
    }

    public void setBackScreen(String backScreen) {
        back = true;
        this.backScreen = backScreen;
    }

    public String getSourceData() {
        return sourceData;
    }

    /**
     * Called at start or in the middle
     */
    public void loadLastKnown() {
        if (source == GPS) {
            setLocation(R.getLocator().getLastLocation(), LAST_KNOWN, "GPS: " + R.getLocator().getLastLocation().getFormattedDate());
        } else {
            setLocation(R.getSettings().getLastLocation(), R.getSettings().getLocationSource(), R.getSettings().getLocationInfo());
        }
    }

    /**
     * Called at application exit
     */
    public void saveLastKnown() {
        try {
            if (source == GPS) {
                R.getSettings().setLastLocation(R.getLocator().getLastLocation(), LAST_KNOWN, "GPS: " + R.getLocator().getLastLocation().getFormattedDate());
            } else {
                R.getSettings().setLastLocation(location, source, sourceData);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "LocationContext.saveLastKnown", null);
        }
    }

    public void addLocationChangeListener(LocationEventListener listener) {
    }

    public void removeLocationChangeListener(LocationEventListener listener) {
    }

    public void notifyChangeStateToListeners() {
        if (source != GPS) {
            R.getLocator().stateChanged(this, LocationProvider.MANUAL);
        }
    }

    public void notifyNewLocationToListeners() {
        if (location != null) {
            R.getLocator().locationChanged(this, location);
        }
    }

    public void notifyMessageToListener(String message) {
    }

    public void notifyErrorMessageToListeners(String message) {
    }
}
