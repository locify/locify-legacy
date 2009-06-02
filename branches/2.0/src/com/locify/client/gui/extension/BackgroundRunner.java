/*
 * BackgroundRunner.java
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
package com.locify.client.gui.extension;

import com.locify.client.utils.GpsUtils;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author menion
 */
public class BackgroundRunner extends Thread {

    private Hashtable listeners;
    private int timeCycles;

    public BackgroundRunner() {
        listeners = new Hashtable();
        timeCycles = 0;
    }

    /**
     * Register listener to recieve periodic call to time updates.
     * @param listener object to register
     * @param taskRefreshTime refresh time in seconds !!
     */
    public void registerBackgroundListener(BackgroundListener listener, int taskRefreshTime) {
        if (!listeners.contains(listener)) {
            if (taskRefreshTime < 0)
                taskRefreshTime = 1;
            listeners.put(listener, new Integer(taskRefreshTime));

            if (!isAlive())
                notify();
        }
    }

    public void removeBackgroundListener(BackgroundListener listener) {
        try {
            listeners.remove(listener);
            if (listeners.size() == 0) {
                wait();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                Enumeration keys = listeners.keys();
                while (keys.hasMoreElements()) {
                    BackgroundListener listener = (BackgroundListener) keys.nextElement();
                    int time = GpsUtils.parseInt(String.valueOf(listeners.get(listener)));

                    if (timeCycles % time == 0) {
                        listener.runBackgroundTask();
                    }
                }
                timeCycles++;
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
