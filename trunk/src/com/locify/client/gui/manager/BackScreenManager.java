/*
 * BackScreens.java
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
package com.locify.client.gui.manager;

import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.Logger;
import java.util.Vector;

/**
 * Saves history of internal urls
 * @author David Vavra
 */
public class BackScreenManager {

    private Vector screens;
    private boolean dontSave;
    private static String[] noBack = {"locify://filesystem/waypoint", "locify://searchBTDevices", "locify://BTDevices", "locify://serviceSettings", "locify://back", "locify://updateService", "locify://refresh", "locify://gps", "locify://lastKnown", "locify://externalBrowserOptions", "locify://sms", "locify://mapNavigation"};
    private boolean debug = false;

    public BackScreenManager() {
        reset();
    }

    /**
     * Resets backscreen memory - when user returns to mainscreen
     */
    public void reset() {
        screens = new Vector();
        screens.addElement("locify://mainScreen");
        dontSave = false;
    }

    /**
     * Adds new screen to database
     * @param url Url of screen
     * @param postData post data
     */
    public void goForward(String url, String postData) {
        try {
            if (debug) {
                Logger.debug("BACK - go forward: " + url);
            }
            //is this a forbidden url?
            for (int i = 0; i < noBack.length; i++) {
                if (url.startsWith(noBack[i])) {
                    dontSave();
                }
            }

            if (url.equals("locify://mainScreen")) {
                reset();
            } else if (!dontSave) {
                //was I there before?
                boolean iWasThere = false;
                for (int i = 0; i < screens.size(); i++) {
                    String screen = (String) screens.elementAt(i);
                    if (screen.equals(url + " " + postData) || ((screen.startsWith("http://") || screen.startsWith("locify://savePlace")) && url.equals("locify://htmlBrowser"))) {
                        iWasThere = true;
                    } else if (iWasThere) {
                        if (debug) {
                            Logger.debug("BACK - I was there: " + url);
                        }
                        //removing all subsequent screens - found shorter path
                        screens.removeElementAt(i);
                        i--;
                    }
                }
                if (!iWasThere) {
                    if (debug) {
                        Logger.debug("BACK - adding: " + url);
                    }
                    screens.addElement(url + " " + postData);
                }
            }
            dontSave = false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BackScreenManager.goForward", null);
        }
    }

    /**
     * Stops saving back screen url temporarily
     * Has to be used before each R.getURL().call() when we don't want
     * to save previous screen for example alerts etc.
     */
    public void dontSave() {
        if (debug) {
            Logger.debug("BACK - don't save");
        }
        dontSave = true;
    }

    public void goBack() {
        goBack(1);
    }

    /**
     * Returns to previous screen
     * @param steps number of steps to go back
     */
    public void goBack(int steps) {
        try {
            for (int i = 0; i < steps; i++) {
                //remove last element
                if (screens.size() != 1) {
                    screens.removeElementAt(screens.size() - 1);
                }
            }
            //go to element previous to last
            dontSave();
            String last = (String) screens.lastElement();
            String[] parts = StringTokenizer.getArray(last, " ");
            if (parts.length == 2) {
                R.getPostData().setRaw(parts[1], true);
            }
            if (debug) {
                Logger.debug("BACK - to:" + parts[0] + " steps=" + steps);
            }
            R.getURL().call(parts[0]);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BackScreenManager.goBack", null);
        }
    }

    /**
     * Deletes last back screen
     */
    public void deleteLast() {
        //remove last element
        if (debug) {
            Logger.debug("BACK - delete: " + screens.lastElement());
        }
        if (screens.size() != 1) {
            screens.removeElementAt(screens.size() - 1);
        }
    }

    /**
     * Repeats last internal URL
     */
    public void repeat() {
        //go to element previous to last
        if (debug) {
            Logger.debug("BACK - repeat");
        }
        dontSave();
        String last = (String) screens.lastElement();
        String[] parts = StringTokenizer.getArray(last, " ");
        R.getPostData().setRaw(parts[1], true);
        R.getURL().call(parts[0]);
    }
}
