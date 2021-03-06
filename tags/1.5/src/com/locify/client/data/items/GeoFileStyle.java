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
    private String iconUrl;
    private Image icon;
    protected int hotSpotX;
    protected String hotSpotXunits;
    protected int hotSpotY;
    protected String hotSpotYunits;

    public GeoFileStyle(String name) {
        this.name = name;
        this.scale = 1.0f;
        this.hotSpotX = 0;
        this.hotSpotXunits = "pixels";
        this.hotSpotY = 0;
        this.hotSpotYunits = "pixels";
    }

    public String getName() {
        return name;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
        finalizeData();
    }
    
    public void setIcon(String url) {
        this.iconUrl = url;
        this.icon = IconData.get(url);
        finalizeData();
    }

    public Image getIcon() {
        if (icon == null && iconUrl != null) {
            icon = IconData.get(iconUrl);
            finalizeData();
        }
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

    private void finalizeData() {
        if (icon != null) {
            icon = IconData.reScaleImage(icon,
                    (int) (icon.getWidth() * scale),
                    (int) (icon.getHeight() * scale));
            hotSpotX = (int) (scale * hotSpotX);
            hotSpotY = (int) (scale * hotSpotY);
        }
    }
}
