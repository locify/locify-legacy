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

import de.enough.polish.ui.Canvas; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.android.lcdui.Font;
import de.enough.polish.android.lcdui.Graphics;
import de.enough.polish.android.lcdui.Image;
import com.locify.client.utils.R;
import com.locify.client.utils.Capabilities;

/**
 * This screen is shown until main screen is loaded
 * @author David Vavra
 */
public class LoadingScreen extends Canvas {

    private Image image;
    private String text;


    public LoadingScreen() {
    }

    /**
     * Views loading screen
     */
    public void view() {
        try {
            this.setFullScreenMode(true);
            image = Image.createImage("/loading.png");
            text = "Loading";
            R.getMidlet().switchDisplayable(null, this);            
        } catch (Exception e) {
        }
    }

    /**
     * Sets new loading text
     * @param text
     */
    public void setText(String text) {
        this.text = text;
        repaint();
    }

    public void paint(Graphics g) {
        int width = getWidth();
        Capabilities.setHeight(getHeight());
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, width, getHeight());
        g.drawImage(image, width / 2, getHeight() / 2 - image.getHeight() / 2, Graphics.TOP | Graphics.HCENTER);
        g.setColor(255, 143, 0);
        g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString(text + " ...", width / 2, getHeight() - 20, Graphics.TOP | Graphics.HCENTER);
    }
    
}
