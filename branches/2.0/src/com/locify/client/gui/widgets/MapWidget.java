/*
 * MapWidget.java
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
package com.locify.client.gui.widgets;

import com.locify.client.gui.extension.BackgroundListener;
import com.locify.client.maps.MapContent;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.layouts.FlowLayout;

/**
 *
 * @author menion
 */
public class MapWidget extends Widget implements BackgroundListener {

    private MapContent mapContent;

    public MapWidget() {
        super(new FlowLayout());
        mapContent = R.getMapContent();
        mapContent.registerParent(this);
        R.getBackgroundRunner().registerBackgroundListener(this, 1);
    }

    public void paint(Graphics g) {
        mapContent.centerMap(R.getLocator().getLastLocation(), true);
        mapContent.drawMap(g);
        mapContent.drawActualLocationPoint(g);

//        g.setColor(ColorsFonts.BLACK);
//        g.drawRect(g.getClipX(), g.getClipY(), 50, 50);
//        g.drawRect(g.getClipWidth() + g.getClipX() - 50, g.getClipHeight() + g.getClipY() - 50, 50, 50);
    }

    public void runBackgroundTask() {
        repaint();
    }
}
