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
public class NetworkLinkDownloader implements Runnable {

    private NetworkLink link;
    private boolean stop = false;
    private Thread thread;

    public NetworkLinkDownloader(NetworkLink link) {
        System.out.println("creating downloder");
        this.link = link;
        thread = new Thread(this);
        thread.start();
    }

    public void stop()
    {
        stop = true;
        thread = null;
    }

    public void run() {
        try {
            System.out.println("thread start");
            while (!stop) {
                System.out.println("downloading");
                R.getHttp().start(link.getLink());
                System.out.println("sleep:"+(link.getRefreshInterval() * 1000));
                thread.sleep(link.getRefreshInterval() * 1000);
            }
            System.out.println("stopping");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NetworkLinkDownloader.run", null);
        }
    }
}
