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

import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import java.util.Vector;


/**
 * Scale on map
 * @author MenKat
 */
public class ScaleMapItem extends MapItem {

    private Point2D.Int p1, p2;
    
    private double distancePerPixel;
    /** only probably value ...
     * WGS84 R = (6378137m * 2 * PI) / 360 */
    private double angleDistance = 111319.4908;
    /** String dispalyed as measure */
    private String scale = "";
    /** total length of scale image */
    private int scaleSizeWidth;
    /** total height of scale image */
    private int scaleSizeHeight;
    /** imgae of scale meter */
    private Image scaleImage;
    
    public ScaleMapItem() {
        super();
    }
    
    public void initialize() {
        try {
            initialized = false;
            
            if (scaleSizeWidth == 0 || scaleSizeHeight == 0) {
                return;
            }

            createScaleImage();
            
            p1 = R.getMapContent().getActualMapLayer().getLocationCoord(new Location4D(0.1, 14.0, 0.0f));
            p2 = R.getMapContent().getActualMapLayer().getLocationCoord(new Location4D(0.2, 14.0, 0.0f));

            distancePerPixel = (angleDistance / 10) / (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y));
            double distance = scaleSizeWidth * distancePerPixel;
            String units;

            if (distance > 0 && distance < 2000000) {
                if (distance >= 1000.0) {
                    distance = distance / 1000.0;
                    if (distance < 10.0) {
                        distance = Math.floor(distance * 10.0) / 10.0;
                    } else if (distance < 100.0) {
                        distance = Math.floor(distance);
                    } else if (distance < 1000.0) {
                        distance = Math.floor(distance / 10.0) * 10.0;
                    } else {
                        distance = Math.floor(distance / 100.0) * 100.0;
                    }
                    units = " km";
                } else {
                    distance = Math.floor(distance / 10.0) * 10.0;
                    units = " m";
                }
                scale = String.valueOf(GpsUtils.formatDouble(distance, 0)) + units;
            } else {
                scale = "-";
            }
            initialized = true;
        } catch (Exception ex) {
            Logger.error("ScaleMapItem.initialize() " + ex.toString());
        }
    }

    public void panItem(int moveX, int moveY) {
        return;
    }

    public void drawItem(Graphics g) {
        if (enabled) {
            if (!initialized || scaleSizeWidth != g.getClipWidth() / 2 || scaleSizeHeight != (g.getClipHeight() / 50)) {
                scaleSizeWidth = g.getClipWidth() / 2;
                scaleSizeHeight = (g.getClipHeight() / 50) < 5 ? 5 : (g.getClipHeight() / 50);
                initialize();
            }

            if (initialized) {
                g.setColor(ColorsFonts.BLACK);
                g.drawString(scale,
                        g.getClipX() + g.getClipWidth() - 5 - g.getFont().stringWidth(scale),
                        g.getClipY() + g.getClipHeight() - 30);
                if (scaleImage != null)
                    g.drawImage(scaleImage,
                            g.getClipX() + g.getClipWidth() - 5 - scaleImage.getWidth(),
                            g.getClipY() + g.getClipHeight() - 15);
            }
        }
    }

    private void createScaleImage() {
        scaleImage = Image.createImage(scaleSizeWidth, scaleSizeHeight);
        Graphics g = scaleImage.getGraphics();
        
        g.setColor(ColorsFonts.BLACK);
        g.fillRect(0, 0, scaleSizeWidth, scaleSizeHeight);
        g.setColor(ColorsFonts.WHITE);
        for (int i = 0; i < 5; i++) {
            if (i % 2 != 0) {
                g.fillRect(i * (scaleSizeWidth / 5) + 1, 1, scaleSizeWidth / 5 - 1, scaleSizeHeight - 2);
            }
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
