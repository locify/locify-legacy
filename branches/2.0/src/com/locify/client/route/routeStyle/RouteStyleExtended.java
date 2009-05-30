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

                        //#if WMbig
//#                         setFont(0, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(1, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(2, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(3, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(4, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(5, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
                        //#else
                        setFont(0, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(1, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(2, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(3, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(4, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(5, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
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

                        //#if WMbig
//#                         setFont(0, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(1, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(2, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(3, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(4, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(5, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(6, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_14);
//#                         setFont(7, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
                        //#else
                        setFont(0, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(1, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(2, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(3, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(4, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(5, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(6, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_10);
                        setFont(7, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
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
                        
                        //#if WMbig
//#                         setFont(0, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(1, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(2, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(3, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_18);
//#                         setFont(4, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_18);
//#                         setFont(6, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_14);
//#                         setFont(7, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_14);
//#                         setFont(8, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
//#                         setFont(9, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_14);
//#                         setFont(10, ColorsFonts.FONT_BMF_14, ColorsFonts.FONT_BMF_16);
                        //#else
                        setFont(0, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(1, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(2, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(3, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_16);
                        setFont(4, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_16);
                        setFont(6, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_10);
                        setFont(7, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_10);
                        setFont(8, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        setFont(9, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_10);
                        setFont(10, ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_14);
                        //#endif
                        break;
                }
            break;
        }
    }
}
