/*
 * LoadingScreen.java
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

import com.locify.client.data.IconData;
import com.locify.client.utils.ResourcesLocify;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * This screen is shown until main screen is loaded
 * @author David Vavra
 */
public class LoadingScreen extends Form {

    private Image image;
    private String text;

    public LoadingScreen() {
        super();
        image = IconData.getLocalImage("loading.png");
        text = "Loading ...";

        this.setLayout(new BorderLayout());
        Label label01 = new Label(image);
        label01.setAlignment(Label.CENTER);
        label01.setVerticalAlignment(Label.CENTER);
        this.addComponent(BorderLayout.CENTER, label01);
        Label label02 = new Label(text);
        label02.setAlignment(Label.CENTER);
        this.addComponent(BorderLayout.SOUTH, label02);
    }

    /**
     * Views loading screen
     */
    public void view() {
        this.show();
    }

    /**
     * Sets new loading text
     * @param text
     */
    public void setText(String text) {
        this.text = text;
        repaint();
    }    
}
