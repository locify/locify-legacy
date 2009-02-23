/*
 * GeoFileStyle.java
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
package com.locify.client.data.items;

import com.locify.client.data.IconData;
import javax.microedition.lcdui.Image;

/**
 *
 * @author menion
 */
public class GeoFileStyle {

    protected String name;
    protected float scale;
    protected Image icon;
    protected int hotSpotX;
    protected String hotSpotXunits;
    protected int hotSpotY;
    protected String hotSpotYunits;

    public GeoFileStyle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIcon(String url) {
        icon = IconData.get(url);
    }

    public Image getIcon() {
        return icon;
    }

    /**
     * Center of image from left side
     * @return distance in pixels
     */
    public int getXMove() {
        return hotSpotX;
    }

    /**
     * Center of image from bottom side
     * @return distance in pixels
     */
    public int getYMove() {
        return hotSpotY;
    }

    public void finalizeData() {
        if (scale != 0 && icon != null) {
            icon = IconData.reScaleImage(icon,
                    (int) (icon.getWidth() * scale),
                    (int) (icon.getHeight() * scale));
            hotSpotX = (int) (scale * hotSpotX);
            hotSpotY = (int) (scale * hotSpotY);
        }
    }
}
