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
    private Label label;

    public LoadingScreen() {
        super();
        image = IconData.getLocalImage("loading.png");

        setLayout(new BorderLayout());
        Label label01 = new Label(image);
        label01.setAlignment(Label.CENTER);
        label01.setVerticalAlignment(Label.CENTER);
        this.addComponent(BorderLayout.CENTER, label01);
        label = new Label("Loading ...");
        label.setAlignment(Label.CENTER);
        this.addComponent(BorderLayout.SOUTH, label);
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
        label.setText(text);
    }    
}
