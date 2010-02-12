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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import com.locify.client.utils.R;
import de.enough.polish.ui.Form;
import de.enough.polish.util.Locale;

/**
 * Creates alert from HTML page
 * @author David Vavra
 */
public class AlertScreen implements CommandListener, Runnable {

    private Form form;
    private String text;
    private String type;
    private int timeout;
    private String nextUrl;
    private Command cmdOk = new Command(Locale.get("OK"), Command.OK, 1);
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
            //#style alertForm
            form = new Form(type);
        }
        form.append(text);
        form.addCommand(cmdOk);
        R.getMidlet().switchDisplayable(null, form);
        form.setCommandListener(this);
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

    public void commandAction(Command c, Displayable d) {
        if (c == cmdOk) {
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

    public void run() {
        try {
            try {
                alertDismissed = false;
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
            }
            if (!alertDismissed) {
                commandAction(cmdOk, form);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "AlertScreen.run()", null);
        }
    }
}
