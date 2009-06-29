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

import com.locify.client.net.browser.HtmlTextArea;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;


/**
 * Creates alert from HTML page
 * @author David Vavra
 */
public class AlertScreen implements ActionListener {

    private Dialog dialog;

    private int alertType;
    private String text;
    private String nextUrl;
    private long timeout;
    private boolean alertDismissed;

    public AlertScreen() {
        super();
    }

    /**
     * Sets internal url which should be called after alert is dismissed
     * @param url 
     */

    public void reset() {
        text = "";
        alertType = Dialog.TYPE_INFO;
        timeout = Long.MAX_VALUE;
        nextUrl = "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimeout(long parseLong) {
        this.dialog.setTimeout(parseLong);
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public void setAlertType(int alertType) {
        this.alertType = alertType;
    }


    /**
     * Displays alert
     */
    public void view() {
        dialog = new Dialog("" + alertType);
        dialog.setDialogType(alertType);
        dialog.addComponent(new HtmlTextArea(text, false));
        dialog.addCommand(Commands.cmdOK);
        dialog.setCommandListener(this);
        dialog.show();
    }

    /**
     * Displays quick alert
     * @param alertText alert text
     * @param alertType alert type
     * @param next next screen url
     */
    public void quickView(String alertText, int alertType, String next) {
        setText(alertText);
        setAlertType(alertType);
        setNextUrl(next);
        view();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdOK || evt.getSource() == Commands.cmdOK) {
//System.out.println("\nEbt: " + evt + " " + evt.getSource() + " " + evt.getCommand() + " " + nextUrl);
            alertDismissed = true;
            if (nextUrl.equals("")) {
                R.getBack().goBack();
            } else {
                if (nextUrl.equals("locify://geoFileBrowser")) {
                    R.getBack().dontSave();
                }
                R.getMainScreen().setAutoInstallRequest(false);
                dialog.dispose();
                R.getURL().call(nextUrl);
            }
        }
    }
}
