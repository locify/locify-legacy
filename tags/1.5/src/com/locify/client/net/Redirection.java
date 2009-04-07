/*
 * Redirection.java
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
 * Manages redirection with possible delay
 * @author David Vavra
 */
public class Redirection implements Runnable{
    private String url = "";
    private int interval = 0;
    private Thread thread;
    private boolean stopped = false;

    /**
     * Starts count down until next url call
     * @param u url
     * @param i interval in seconds
     */
    public void start(String u, int i)
    {
        url = u;
        interval = i;
        thread = new Thread(this);
        thread.start();
        stopped = false;
    }
    
    public void stop()
    {
        stopped = true;
    }
            
    public void run() {
        try
        {
            Thread.sleep(interval*1000);
            if (!stopped)
            {
                R.getBack().dontSave();
                R.getURL().call(url);
            }
        }
        catch (Exception e)
        {
            R.getErrorScreen().view(e, "Redirection.run", url);
        }
    }
}

