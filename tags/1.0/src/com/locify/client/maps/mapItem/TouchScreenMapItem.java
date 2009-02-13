/*
 * TouchScreenMapItem.java
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

package com.locify.client.maps.mapItem;

import com.locify.client.route.ScreenItem;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Tool for zooming only for touch screens
 * @author MenKat
 */
public class TouchScreenMapItem extends MapItem {

    private boolean stateActive = false;
    private int topMargin;
    private Image arrowToScreenImage;
    private ScreenItem arrowToScreen;
    private Image arrowFromScreenImage;
    private ScreenItem arrowFromScreen;

    public TouchScreenMapItem() {
        topMargin = R.getTopBar().height + 20;

        arrowToScreenImage = Image.createImage(7, 20);
        Graphics g = arrowToScreenImage.getGraphics();
        g.setColor(ColorsFonts.GREEN);
        g.fillRect(0, 0, g.getClipWidth(), g.getClipHeight());
        g.setColor(ColorsFonts.BLACK);
        g.fillTriangle(1, 10, 6, 1, 6, 19);
        arrowToScreen = new ScreenItem(arrowToScreenImage);
        arrowToScreen.setColors(ColorsFonts.LIGHT_ORANGE, ColorsFonts.BLACK, ColorsFonts.GREEN, ColorsFonts.BLUE);
        arrowToScreen.setSizePos(mapScreen.getWidth() - 11, topMargin, 10, 20);
        arrowToScreen.setCornerRadius(3);

        arrowFromScreenImage = Image.createImage(15, 20);
        g = arrowFromScreenImage.getGraphics();
        g.setColor(ColorsFonts.GREEN);
        g.fillRect(0, 0, g.getClipWidth(), g.getClipHeight());
        g.setColor(ColorsFonts.BLACK);
        g.fillTriangle(1, 1, 6, 10, 1, 19);
        arrowFromScreen = new ScreenItem(arrowFromScreenImage);
        arrowFromScreen.setColors(ColorsFonts.LIGHT_ORANGE, ColorsFonts.BLACK, ColorsFonts.GREEN, ColorsFonts.BLUE);
        arrowFromScreen.setSizePos(mapScreen.getWidth() - 51, topMargin, 10, 20);
        arrowFromScreen.setCornerRadius(3);
    }

    public void initialize() {
    }

    public void panItem(int moveX, int moveY) {
    }

    public void drawItem(Graphics g) {
        if (stateActive) {
            arrowFromScreen.paint(g);
        } else {
            arrowToScreen.paint(g);
        }
    }

    public void getWaypointsAtPosition(Vector data, int x, int y, int radius) {
    }

    public boolean touchInside(int x, int y) {
        return false;
    }
}
