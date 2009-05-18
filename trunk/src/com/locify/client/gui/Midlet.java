/*
 * Gui.java
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
package com.locify.client.gui;

import com.locify.client.data.CookieData;
import com.locify.client.data.DeletedData;
import com.locify.client.data.IconData;
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.data.SettingsData;
import com.locify.client.data.ServicesData;
import com.locify.client.data.items.GeoFiles;
import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import com.locify.client.utils.R;
import com.locify.client.locator.LocationProvider;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Utils;
import javax.microedition.lcdui.Image;

/**
 * Controls main MIDlet lifecycle
 * @author David Vavra
 */
public class Midlet extends MIDlet {

    private boolean midletPaused = false;

    public Midlet() {
        new R(this);
    }

    /**
     * Manages midlet life cycle
     */
    public void startApp() {
        try {
            if (!midletPaused) {
                startMIDlet();
            }
            midletPaused = false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Midlet.startApp", null);
        }
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }

    public void startMIDlet() {
        try {
            R.getLoading().view();
            //#if !applet
            if (R.getFirstRun().isFirstTime()) {
                R.getFirstRun().start();
            } else {
                R.getFirstRun().loadLanguage();
                if (R.getFirstRun().permissionsTest()) {
                    R.getFirstRun().loadRoot();
                    //#else
//#                     R.getFirstRun().loadEnglish();
                    //#endif
                    CookieData.load();
                    R.getSettings().load();
                    R.getLocator().load();
                    DeletedData.load();
                    ServicesData.load();
                    ServiceSettingsData.load();
                    //#if !applet
                    if (!Capabilities.isWindowsMobile()) {
                        R.getContactsScreen().load();
                    }
                    if (GeoFiles.isUnfinishedRoute()) {
                        R.getRouteScreen().loadUnfinishedRoute();
                    }
                    //#endif
                    R.getMainScreen().load();
                    R.getContext().loadLastKnown();
                    //#if !applet
                    if (R.getSettings().getBacklight() == SettingsData.WHOLE_APPLICATION) {
                        R.getBacklight().on();
                    }
                } else {
                    R.getFirstRun().viewPermissionsWarning();
                }
            }
            //#endif

            R.destroyLoading();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Midlet.startMIDlet", null);
        }
//Utils.printMemoryState("Start");
//        Utils.printMemoryState();
//        System.gc();
//        Utils.printMemoryState();
//        try {
    //Image i1 = Image.createImage("/map_tile_64x64.png");
//            Image i2 = Image.createImage("/wpt_description_background.png");
    //Image i3 = Image.createImage("/map_icon_zoom_plus.png");

//            Image x1 = IconData.get("locify://icons/gps.png");
//            Image x2 = IconData.get("locify://icons/savedLocation.png");
//            Image x3 = IconData.get("locify://icons/coordinates.png");
//            Image x4 = IconData.get("locify://icons/lastKnown.png");
//            Image x5 = IconData.get("locify://icons/savePlace.png");
//            Image x6 = IconData.get("locify://icons/recordRoute.png");
//            Image x7 = IconData.get("locify://icons/browse.png");
//            Image x8 = IconData.get("locify://icons/sync.png");
//            Image x9 = IconData.get("locify://icons/viewLocation.png");
//            Image x10 = IconData.get("locify://icons/viewPlace.png");
//            Image x11 = IconData.get("locify://icons/viewRoute.png");
//            Image x12 = IconData.get("locify://icons/select.png");
//            Image x13 = IconData.get("locify://icons/compass.png");
//            Image x14 = IconData.get("locify://icons/navigateTo.png");
//            Image x15 = IconData.get("locify://icons/navigateAlong.png");
//            Image x16 = IconData.get("locify://icons/gps.png");
//            Image x17 = IconData.get("locify://icons/settings.png");
//            Image x18 = IconData.get("locify://icons/login.png");
//            Image x19 = IconData.get("locify://icons/logout.png");
//            Image x20 = IconData.get("locify://icons/checkVersion.png");
//            Image x21 = IconData.get("locify://icons/help.png");
//            Image x22 = IconData.get("locify://icons/moreInfo.png");
//            Utils.printMemoryState();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        System.gc();
//        Utils.printMemoryState();


//        byte[] data = R.getFileSystem().loadBytes("img.png");
//System.out.println("\nLoadedData: " + new String(data));
//        PlanStudioManager.parseOfflineMap();
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        R.getContext().saveLastKnown();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        R.getRouteScreen().saveUnfinishedRoute();
        GeoFiles.saveDataTypeDatabase();
        LocationProvider lp = R.getLocator().getLocationProvider();
        if (lp != null) {
            lp.stopProvider();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }

        switchDisplayable(null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }

    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        if (R.getErrorScreen().isNoErrorOrViewed()) {
            Display display = Display.getDisplay(this);
            if (alert == null) {
                display.setCurrent(nextDisplayable);
            } else {
                display.setCurrent(alert, nextDisplayable);
            }
        }
    }
}
