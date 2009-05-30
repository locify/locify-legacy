package com.locify.client.gui;


import com.locify.client.data.CookieData;
import com.locify.client.data.DeletedData;
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.data.ServicesData;
import com.locify.client.data.SettingsData;
import com.locify.client.data.items.GeoFiles;
import com.locify.client.locator.LocationProvider;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/*
 * Midlet.java
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
/**
 *
 * @author menion
 */
public class Midlet extends MIDlet {

    private boolean midletPaused = false;

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
        midletPaused = true;
    }

    protected void startApp() throws MIDletStateChangeException {
Utils.printMemoryState("Init - start");
        try {
            if (!midletPaused) {
                startMIDlet();
            }
            midletPaused = false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Midlet.startApp", null);
        }
Utils.printMemoryState("Init - end");

//        Resources r = null;
//        Hashtable h = null;
//        try {
//            r = Resources.open("/resources.res");
//            //UIManager.getInstance().setThemeProps(r.getTheme(r.getThemeResourceNames()[0]));
//            h = Resources.open("/lang_en.res").getL10N("localize", "en");
//            UIManager.getInstance().setResourceBundle(h);
//        } catch (java.io.IOException e) {
//        }
//
//        Form f = new Form();
//        f.setTitle("Hello World");
//        f.setLayout(new BorderLayout());
//        Label lab = new Label((String) h.get("Ask_every_time"));
//        lab.setIcon(r.getImage("corruptedFile.png"));
//        lab.setAlignment(Label.CENTER);
//        f.addComponent(BorderLayout.CENTER, new Label("I am a Label"));
//        f.addComponent(BorderLayout.NORTH, lab);
//        f.show();
    }

    public void startMIDlet() {
        try {
Utils.printMemoryState("01");
            new R(this);
Utils.printMemoryState("02");
            //init the LWUIT Display
            Display.init(this);
Utils.printMemoryState("03");
            R.getLoading().view();
Utils.printMemoryState("04");
            //#if !applet
            if (R.getFirstRun().isFirstTime()) {
                R.getFirstRun().start();
Utils.printMemoryState("06");
            } else {
Utils.printMemoryState("07");
                R.getFirstRun().loadLanguage();
                if (R.getFirstRun().permissionsTest()) {
Utils.printMemoryState("08");
                    R.getFirstRun().loadRoot();
                    //#else
//#                     R.getFirstRun().loadEnglish();
                    //#endif
                    CookieData.load();
                    R.getSettings().load();
                    R.getLocator().load();
                    DeletedData.load();
Utils.printMemoryState("09");
                    ServicesData.load();
                    ServiceSettingsData.load();
Utils.printMemoryState("10");
                    //#if !applet
                    if (!Capabilities.isWindowsMobile()) {
                        R.getContactsScreen().load();
                    }
                    if (GeoFiles.isUnfinishedRoute()) {
                        R.getRouteScreen().loadUnfinishedRoute();
                    }
                    //#endif
Utils.printMemoryState("21");
                    R.getMainScreen().load();
Utils.printMemoryState("22");
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
        try {
            destroyApp(true);
        } catch (MIDletStateChangeException ex) {
            ex.printStackTrace();
        }
        notifyDestroyed();
    }
}
