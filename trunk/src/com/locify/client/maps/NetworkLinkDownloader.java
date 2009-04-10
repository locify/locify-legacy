/*
 * NetworkLinkDownloader.java
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
package com.locify.client.maps;

import com.locify.client.data.items.NetworkLink;
import com.locify.client.utils.R;

/**
 * Manages automated downloading of KML inside NetworkLink
 * @author Destil
 */
public class NetworkLinkDownloader extends Thread {

    private NetworkLink link;
    private boolean stop = false;

    public NetworkLinkDownloader(NetworkLink link) {
        this.link = link;
    }

    public void stop() {
        System.out.println("stopping networklink downloader");
        stop = true;
    }

    public void resume() {
        stop = false;
        try {
            start();
        } catch (IllegalThreadStateException e) {
        }
    }

    public boolean isStopped() {
        return stop;
    }

    public void run() {
        try {
            while (!stop) {
                R.getHttp().start(link.getLink());
                Thread.sleep(link.getRefreshInterval() * 1000);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NetworkLinkDownloader.run", null);
        }
    }
}
