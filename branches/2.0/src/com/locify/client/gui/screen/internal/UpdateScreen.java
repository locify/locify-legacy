/*
 * UpdateScreen.java
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

package com.locify.client.gui.screen.internal;

import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import com.locify.client.net.Http;
import com.locify.client.net.browser.HtmlTextArea;
import com.locify.client.utils.Locale;
import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * Manages interface of update Locify
 * @author Destil
 */
public class UpdateScreen extends FormLocify implements ActionListener {
    
    private Button btnDownload;
    private static final String LATEST_VERSION_URL = "http://www.locify.com/download/Locify.jad";
    private static final String UPDATE_URL = "http://www.locify.com/update";
    
    public UpdateScreen() {
        super(Locale.get("Check_version"));
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    }
    
    public void view(String versionInfo) {
        setAsNew(Locale.get("Check_version"));
        addComponent(new Label(Locale.get("Current_version_info")));
        addComponent(new Label(R.getMidlet().getAppProperty("MIDlet-Version")));
        addComponent(new Label(Locale.get("Latest_version_info")));
        addComponent(new HtmlTextArea(versionInfo, false));
        btnDownload = new Button(Locale.get("Download_latest"));
        btnDownload.addActionListener(this);
        addComponent(btnDownload);

        addCommand(Commands.cmdBack);
        addCommand(Commands.cmdHome);
        addCommandListener(this);
        show();
    }
    
    /**
     * Launches update check
     */
    public void checkVersion() {
        R.getHttp().start(UPDATE_URL,Http.UPDATER);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == btnDownload){
            R.getExternalBrowser().open(LATEST_VERSION_URL+"?user="+R.getSettings().getName()+"&version="+R.getMidlet().getAppProperty("MIDlet-Version"));
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }
}
