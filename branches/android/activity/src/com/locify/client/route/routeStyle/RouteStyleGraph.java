/*
 * RouteStyleGraph.java
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
package com.locify.client.route.routeStyle;

import com.locify.client.gui.screen.internal.RouteScreen;

/**
 * Route style showing altitude graphs
 * @author Menion
 */
import com.locify.client.utils.ColorsFonts;

public class RouteStyleGraph extends RouteStyle {

    public RouteStyleGraph(RouteScreen aThis) {
        super(aThis);
    }

    protected void setDisplayItem() {
        addItem(RouteScreen.ITEM_GRAPH_ALTITUDE_BY_DIST, 0);
        addItem(RouteScreen.ITEM_GRAPH_ALTITUDE_BY_TIME, 1);

        //#if polish.Vendor == WM-big
        //# setFont(0, ColorsFonts.BMF_ARIAL_14_BLACK, null);
        //# setFont(1, ColorsFonts.BMF_ARIAL_14_BLACK, null);
        //#else
        setFont(0, ColorsFonts.BMF_ARIAL_16_BLACK, null);
        setFont(1, ColorsFonts.BMF_ARIAL_16_BLACK, null);
        //#endif
    }
}
