/*
 * AlertScreen.java
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
package com.locify.client.gui.screen.service;

import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;


/**
 * Creates alert from HTML page
 * @author David Vavra
 */
public class AlertScreen implements ActionListener, Runnable {

    private Form form;
    private String text;
    private String type;
    private int timeout;
    private String nextUrl;
    private boolean alertDismissed;

    public AlertScreen() {
    }

    /**
     * Sets default values
     */
    public void reset() {
        text = "";
        type = "Info";
        timeout = -1;
        nextUrl = "";
    }

    public void setText(String t) {
        text = t;
    }

    public void setType(String t) {
        this.type = t;
    }

    public void setTimeout(String t) {
        timeout = Integer.parseInt(t);
    }

    /**
     * Sets internal url which should be called after alert is dismissed
     * @param url 
     */
    public void setNext(String url) {
        url = R.getHttp().makeAbsoluteURL(url);
        nextUrl = url;
    }

    /**
     * Displays alert
     */
    public void view() {
        if (Capabilities.isWindowsMobile()) {
            //vypnuti stmivani u WM
            form = new Form(type);
        } else {
            form = new Form(type);
        }
        form.addComponent(new Label(text));
        form.addCommand(Commands.cmdOK);
        form.setCommandListener(this);
        form.show();

        //timout start
        if (timeout != -1) {
            (new Thread(this)).start();
        }
    }

    /**
     * Displays quick alert
     * @param alertText alert text
     * @param alertType alert type
     * @param next next screen url
     */
    public void quickView(String alertText, String alertType, String next) {
        reset();
        setText(alertText);
        setType(alertType);
        setNext(next);
        view();
    }

    public void run() {
        try {
            try {
                alertDismissed = false;
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
            }
            if (!alertDismissed) {
                actionPerformed(new ActionEvent(form, Integer.MAX_VALUE));
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "AlertScreen.run()", null);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdOK || evt.getKeyEvent() == Integer.MAX_VALUE) {
            alertDismissed = true;
            if (nextUrl.equals("")) {
                R.getBack().goBack();
            } else {
                if (nextUrl.equals("locify://geoFileBrowser")) {
                    R.getBack().dontSave();
                }
                R.getURL().call(nextUrl);
            }
        }
    }
}
