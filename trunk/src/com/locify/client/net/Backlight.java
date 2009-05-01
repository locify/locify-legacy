/*
 * Backlight.java
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
package com.locify.client.net;

import com.locify.client.utils.R;

/**
 * Manages automated downloading of KML inside NetworkLink
 * @author Destil
 */
public class Backlight extends Thread {

    private static int FLASHBACK_PERIOD = 4;
    private boolean stop = false;
    private int flashbackLightPause = 0;

    public Backlight() {
    }

    public void stop() {
        stop = true;
    }

    public void resume() {
        stop = false;
    }

    public boolean isStopped() {
        return stop;
    }

    public void run() {
        try {
            while (true) {
                if (!stop && FLASHBACK_PERIOD > 0)
                {
                    if (flashbackLightPause < FLASHBACK_PERIOD*2-1)
                    {
                        flashbackLightPause++;
                    }
                    else
                    {
                        R.getMidlet().getDisplay().flashBacklight(1);
                        flashbackLightPause = 0;
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NetworkLinkDownloader.run", null);
        }
    }
}
