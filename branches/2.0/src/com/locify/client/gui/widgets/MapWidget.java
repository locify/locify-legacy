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

import com.locify.client.maps.MapContent;
import com.locify.client.utils.R;
import com.sun.lwuit.Graphics;

/**
 *
 * @author menion
 */
public class MapWidget extends Widget {

    private MapContent mapContent;

    public MapWidget() {
        super();
        mapContent = R.getMapContent();
    }

    public void paint(Graphics g) {
        mapContent.registerParent(this);

        mapContent.centerMap(R.getLocator().getLastLocation(), true);
        mapContent.paintAll(g);
//        mapContent.drawActualLocationPoint(g);
//System.out.println("DrawMap");
//        g.setColor(ColorsFonts.BLACK);
//        g.drawRect(g.getClipX(), g.getClipY(), 50, 50);
//        g.drawRect(g.getClipWidth() + g.getClipX() - 50, g.getClipHeight() + g.getClipY() - 50, 50, 50);
    }
}
