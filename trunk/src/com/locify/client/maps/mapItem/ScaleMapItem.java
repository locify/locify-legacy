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
import com.locify.client.utils.Capabilities;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

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
    /** scale position X from top */
    private int posX;
    /** scale position Y from left */
    private int posY;
    /** total length of scale image */
    private int scaleSizeX;
    /** total height of scale image */
    private int scaleSizeY;
    /** imgae of scale meter */
    private Image scaleImage;
    
    public ScaleMapItem() {
        super();
        this.posX = 5;
        this.posY = Capabilities.getHeight() - 50;
        this.scaleSizeX = Capabilities.getWidth() / 2;
        this.scaleSizeY = 6;
        
        createScaleImage();
    }
    
    public void initialize() {
        try {
            p1 = mapScreen.getActualMapLayer().getLocationCoord(new Location4D(0.1, 14.0, 0.0f));
            p2 = mapScreen.getActualMapLayer().getLocationCoord(new Location4D(0.2, 14.0, 0.0f));

            distancePerPixel = (angleDistance / 10) / (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y));
            double distance = (Capabilities.getWidth() / 2) * distancePerPixel;
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
            if (!initialized)
                initialize();
            
            g.setColor(ColorsFonts.BLACK);
            g.drawString(scale, posX + scaleSizeX + 10, posY - 6, Graphics.TOP | Graphics.LEFT);
            if (scaleImage != null)
                g.drawImage(scaleImage, posX, posY, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void createScaleImage() {
        scaleImage = Image.createImage(scaleSizeX, scaleSizeY);
        Graphics g = scaleImage.getGraphics();
        
        g.setColor(ColorsFonts.BLACK);
        g.fillRect(0, 0, scaleSizeX, scaleSizeY);
        g.setColor(ColorsFonts.WHITE);
        for (int i = 0; i < 5; i++) {
            if (i % 2 != 0) {
                g.fillRect(i * (scaleSizeX / 5) + 1, 1, scaleSizeX / 5 - 1, scaleSizeY - 2);
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
