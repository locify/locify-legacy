/*
 * RouteStyleExtended.java
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
import com.locify.client.utils.ColorsFonts;

/**
 * Extended route style with many info
 * @author Menion
 */

public class RouteStyleExtended extends RouteStyle {

    public RouteStyleExtended(RouteScreen aThis) {
        super(aThis);
    }

    protected void setDisplayItem() {
        switch (itemWidthCount) {
            case 3:
                switch (itemHeightCount) {
                    case 2:
                        addItem(RouteScreen.ITEM_ROUTE_TIME, 0);
                        addItem(RouteScreen.ITEM_ROUTE_DIST, 1);
                        addItem(RouteScreen.ITEM_SPEED_ACTUAL, 2);
                        addItem(RouteScreen.ITEM_SPEED_AVERAGE, 3);
                        addItem(RouteScreen.ITEM_SPEED_MAX, 4);
                        addItem(RouteScreen.ITEM_ALTITUDE, 5);

                        //#if polish.Vendor == WM-big
                        setFont(0, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(1, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(2, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(3, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(4, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(5, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        //#else
//#                         setFont(0, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(1, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(2, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(3, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(4, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(5, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                        //#endif
                        break;
                    case 3:
                        addItem(RouteScreen.ITEM_ROUTE_TIME, 0);
                        addItem(RouteScreen.ITEM_ROUTE_DIST, 1);
                        addItem(RouteScreen.ITEM_SPEED_ACTUAL, 3);
                        addItem(RouteScreen.ITEM_SPEED_AVERAGE, 4);
                        addItem(RouteScreen.ITEM_SPEED_MAX, 5);
                        addItem(RouteScreen.ITEM_ALTITUDE, 6); 
                        addItem(RouteScreen.ITEM_LONGITUDE, 7); 
                        addItem(RouteScreen.ITEM_ALTITUDE, 8);

                        //#if polish.Vendor == WM-big
                        setFont(0, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(1, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(2, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(3, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(4, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(5, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(6, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                        setFont(7, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        //#else
//#                         setFont(0, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(1, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(2, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(3, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(4, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(5, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(6, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_10_BLACK);
//#                         setFont(7, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                        //#endif
                        break;
                    case 4:
                        addItem(RouteScreen.ITEM_SPEED_ACTUAL, 0);
                        addItem(RouteScreen.ITEM_SPEED_AVERAGE, 1);
                        addItem(RouteScreen.ITEM_SPEED_MAX, 2);
                        addItem(RouteScreen.ITEM_ROUTE_TIME, 3);
                        addItem(RouteScreen.ITEM_ROUTE_DIST, 4);
                        addItem(RouteScreen.ITEM_LATITUDE, 6);
                        addItem(RouteScreen.ITEM_LONGITUDE, 7);
                        addItem(RouteScreen.ITEM_ALTITUDE, 8);
                        addItem(RouteScreen.ITEM_HDOP, 9);
                        addItem(RouteScreen.ITEM_VDOP, 10);
                        
                        //#if polish.Vendor == WM-big
                        setFont(0, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(1, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(2, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(3, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
                        setFont(4, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_18_BLACK);
                        setFont(6, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                        setFont(7, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                        setFont(8, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        setFont(9, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                        setFont(10, ColorsFonts.BMF_ARIAL_14_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
                        //#else
//#                         setFont(0, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(1, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(2, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(3, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
//#                         setFont(4, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_16_BLACK);
//#                         setFont(6, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_10_BLACK);
//#                         setFont(7, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_10_BLACK);
//#                         setFont(8, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
//#                         setFont(9, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_10_BLACK);
//#                         setFont(10, ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_14_BLACK);
                        //#endif  
                        break;
                }
            break;
        }
    }
}
