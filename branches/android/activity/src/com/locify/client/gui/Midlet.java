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
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.data.SettingsData;
import com.locify.client.data.ServicesData;
import com.locify.client.data.items.GeoFiles;
import de.enough.polish.ui.Alert; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Display; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.android.midlet.MIDlet;
import com.locify.client.utils.R;
import com.locify.client.locator.LocationProvider;
import com.locify.client.utils.Capabilities;

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
            //# if (R.getFirstRun().isFirstTime()) {
                //# R.getFirstRun().start();
            //# } else {
                //# R.getFirstRun().loadLanguage();
                //# if (R.getFirstRun().permissionsTest()) {
                    //# R.getFirstRun().loadRoot();
                    //#else
                    R.getFirstRun().loadEnglish();
                    //#endif
                    CookieData.load();
                    R.getSettings().load();
                    R.getLocator().load();
                    DeletedData.load();
                    ServicesData.load();
                    ServiceSettingsData.load();
                    //#if !applet
                    //# if (!Capabilities.isWindowsMobile()) {
                        //# R.getContactsScreen().load();
                    //# }
                    //# if (GeoFiles.isUnfinishedRoute()) {
                        //# R.getRouteScreen().loadUnfinishedRoute();
                    //# }
                    //#endif
                    R.getMainScreen().load();
                    R.getContext().loadLastKnown();
                    //#if !applet
                    //# if (R.getSettings().getBacklight() == SettingsData.WHOLE_APPLICATION) {
                        //# R.getBacklight().on();
                    //# }
                //# } else {
                    //# R.getFirstRun().viewPermissionsWarning();
                //# }
            //# }
            //#endif

            R.destroyLoading();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Midlet.startMIDlet", null);
        }

//Enumeration root = FileSystemRegistry.listRoots();
//while (root.hasMoreElements()) {
//    Logger.debug("Root: " + (String) root.nextElement());
//}
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
                Alert.setCurrent( display, alert, nextDisplayable );
            }
        }
    }
}
