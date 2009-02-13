/*
 * RouteStyleSimple.java
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
 * Simple route style with basic info
 * @author Menion
 */
import com.locify.client.utils.ColorsFonts;
public class RouteStyleSimple extends RouteStyle {

    public RouteStyleSimple(RouteScreen aThis) {
        super(aThis);
    }

    protected void setDisplayItem() {
        switch (itemWidthCount) {
            case 2:
                switch (itemHeightCount) {
                    case 2:
                        addItem(RouteScreen.ITEM_ROUTE_DIST, 0);
                        addItem(RouteScreen.ITEM_ROUTE_TIME, 1);
                        addItem(RouteScreen.ITEM_SPEED_ACTUAL, 2);
                        addItem(RouteScreen.ITEM_SPEED_AVERAGE, 3);

                        //#if polish.Vendor == WM-big
                        setFont(0, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_20_BLACK);
                        setFont(1, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_20_BLACK);
                        setFont(2, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
                        setFont(3, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
                        //#else
//#                         setFont(0, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
//#                         setFont(1, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
//#                         setFont(2, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
//#                         setFont(3, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        //#endif
                        break;
                    case 3:
                        addItem(RouteScreen.ITEM_ROUTE_DIST, 0);
                        addItem(RouteScreen.ITEM_ROUTE_TIME, 1);
                        addItem(RouteScreen.ITEM_SPEED_ACTUAL, 2);
                        addItem(RouteScreen.ITEM_SPEED_AVERAGE, 3);
                        addItem(RouteScreen.ITEM_ALTITUDE, 5);

                        //#if polish.Vendor == WM-big
                        setFont(0, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_20_BLACK);
                        setFont(1, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_20_BLACK);
                        setFont(2, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
                        setFont(3, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
                        setFont(5, ColorsFonts.BMF_ARIAL_16_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
                        //#else
//#                         setFont(0, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
//#                         setFont(1, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
//#                         setFont(2, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
//#                         setFont(3, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
//#                         setFont(5, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        //#endif
                        break;
                }
                break;
        }
    }
}
