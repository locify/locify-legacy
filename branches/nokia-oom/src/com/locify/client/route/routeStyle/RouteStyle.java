/*
 * RouteStyle.java
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
import com.locify.client.route.*;
import de.enough.polish.util.BitMapFont;

/**
 * Abstract route style
 * @author Menion
 */
public abstract class RouteStyle {

    protected int itemWidth = RouteScreen.itemWidth;
    protected int itemWidthCount = RouteScreen.itemWidthCount;
    protected int itemHeight = RouteScreen.itemHeight;
    protected int itemHeightCount = RouteScreen.itemHeightCount;
    protected int itemBetweenSpace = RouteScreen.itemBetweenSpace;

    protected RouteScreen routeScreen;
    
    public RouteStyle(RouteScreen routeScreen) {
        this.routeScreen = routeScreen;

        RouteScreen.displayItems = new int[itemWidthCount * itemHeightCount];
        for (int i = 0; i < RouteScreen.displayItems.length; i++) {
            RouteScreen.displayItems[i] = -1;
        }

        setDisplayItem();
        initializeDisplayItems();
    }
    
    private void initializeDisplayItems() {
        // indexed from zero
        int row, column;

        for (int i = 0; i < RouteScreen.displayItems.length; i++) {
            if (RouteScreen.displayItems[i] != -1) {
                row = (i / itemWidthCount);
                column = (i % itemWidthCount);

                Item item = routeScreen.getItem(RouteScreen.displayItems[i]);
                item.setSizePos(column * itemWidth + (column + 1) * itemBetweenSpace,
                        RouteScreen.TOP_MARGIN + row * itemHeight + (row + 1) * itemBetweenSpace,
                        itemWidth, itemHeight);

                if (item instanceof ScreenItem) {
                    ((ScreenItem) item).setSelectable(false);
                }
            }
        }
    }

    protected boolean addItem(int itemType, int position) {
        if (position < RouteScreen.displayItems.length) {
            RouteScreen.displayItems[position] = itemType;
            return true;
        } else {
            return false;
        }
    }
    
    protected boolean setFont(int position, BitMapFont textLabelFont, BitMapFont textValueFont) {
        if (position < RouteScreen.displayItems.length && 
                RouteScreen.displayItems[position] != -1) {
            Item item = routeScreen.getItem(RouteScreen.displayItems[position]);
            if (item instanceof ScreenItem)
                ((ScreenItem) item).setFont(textLabelFont, textValueFont);
            else if (item instanceof GraphItem)
                item.setFont(textLabelFont);
            else
                return false;
            return true;
        } else {
            return false;
        }
    }
    
    protected abstract void setDisplayItem();
}
