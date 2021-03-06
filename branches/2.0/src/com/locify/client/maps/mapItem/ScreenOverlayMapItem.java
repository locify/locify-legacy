/*
 * ScaleMapItem.java
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

import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.route.ScreenItem;
import com.locify.client.utils.Capabilities;
import com.sun.lwuit.Graphics;
import java.util.Vector;

/**
 * Scale on map
 * @author MenKat
 */
public class ScreenOverlayMapItem extends MapItem {

    private String description;
    private ScreenItem screenItem;
    
    public ScreenOverlayMapItem(String description) {
        super();
        this.description = description;
    }
    
    public void initialize() {
        try {
            screenItem = new ScreenItem(description);
            screenItem.setFont(ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_14);
            int textWidth = ColorsFonts.FONT_BMF_14.stringWidth(description);
            screenItem.setSizePos(Capabilities.getWidth() - textWidth - 10,
                    R.getTopBar().height + 4, textWidth + 8,
                    ////ColorsFonts.FONT_BMF_14.getFontHeight() + 2
                    16
                    );
            initialized = true;
        } catch (Exception ex) {
            Logger.error("ScreenOverlayMapItem.initialize() " + ex.toString());
        }
    }

    public void panItem(int moveX, int moveY) {
        return;
    }

    public void drawItem(Graphics g) {
        if (enabled) {
            if (!initialized)
                initialize();
            
            screenItem.paint(g);
        }
    }

    public boolean touchInside(int x, int y) {
        return false;
    }

    public void getWaypointsAtPositionByPoint(Vector data, int x, int y, int radiusSquare) {
        return;
    }

    public void getWaypointsAtPositionByIcon(Vector data, int x, int y) {
        return;
    }
}
