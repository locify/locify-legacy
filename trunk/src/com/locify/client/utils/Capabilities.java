/*
 * Capabilities.java
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
package com.locify.client.utils;

/**
 * This class contains method for getting info about user's phone. It is used for customizing Locify to the capabilities of the phone
 * @author Destil
 */
public class Capabilities {

    public static boolean hasJSR179() {
        if (System.getProperty("microedition.location.version") == null) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasPIMSupport() {
        String version = "";
        version = System.getProperty("microedition.pim.version");
        if (version != null) {
            if (!version.equals("1.0"))
                return false;
                //throw new IOException("Package is not version 1.0.");
        } else
            return false;
            //throw new IOException("PIM optional package is not available.");
        return true;
    }

    public static boolean hasLandmarks() {
        if (isWindowsMobile()) {
            return false;
        }
        try {
            Class.forName("javax.microedition.location.LandmarkStore");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasBluetooth() {
        if (isWindowsMobile()) {
            return false;
        }
        //few devices does not have bluetooth api and freezes when Class.forName is used
        if (System.getProperty("microedition.platform")!=null && System.getProperty("microedition.platform").indexOf("W350i") != -1)
        {
            return false;
        }
        try {
            Class.forName("javax.bluetooth.LocalDevice");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isNokia() {
        if (System.getProperty("microedition.platform")!=null && System.getProperty("microedition.platform").indexOf("Nokia") == -1) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isSonyEricsson() {
        if (System.getProperty("microedition.platform")!=null && System.getProperty("microedition.platform").indexOf("SonyEricsson") == -1) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasCOMMs() {
        if (System.getProperty("microedition.commports") == null) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isWindowsMobile() {
        if (R.getMidlet().getAppProperty("Device").startsWith("WM")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isHGE100Connected() {
        if (System.getProperty("microedition.commports").indexOf("AT5") != -1) {
            return true;
        } else {
            return false;
        }
    }
}
