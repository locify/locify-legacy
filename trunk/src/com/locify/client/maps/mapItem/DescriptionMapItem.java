/*
 * DescriptionMapItem.java
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

import com.locify.client.data.items.Waypoint;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.RectangleViewPort;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.route.ScreenItem;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import de.enough.polish.util.BitMapFontViewer;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * A bubble with description about some selected waypoint, route etc.
 * @author MenKat
 */
public class DescriptionMapItem extends MapItem {

    /** item */
    Waypoint item;
    /** actual 2D coordinates */
    Point2D.Int actualItem;
    /** bit map font writer */
    private BitMapFontViewer bmfvName;
    private BitMapFontViewer bmfvDesc;
    private BitMapFontViewer bmfvLatLon;
    /** width of text */
    private int textWidth;
    /** buttons */
    public static final int BUTTON_CLOSE = 0;
    public static final int BUTTON_NAVIGATE = 1;
    public static final int BUTTON_NAVIGATE_ON_MAP = 2;
    /** screenItem close */
    private ScreenItem[] buttons;
    /** selected button index */
    private int selectedButton;
    
    public DescriptionMapItem(Waypoint waypoint) {
        super();
        this.item = waypoint;
        textWidth = waypointDescriptionBackground.getWidth() - 4;
        
        buttons = new ScreenItem[3];
        buttons[0] = new ScreenItem(waypointDescription01);
        buttons[1] = new ScreenItem(waypointDescription02);
        buttons[2] = new ScreenItem(waypointDescription03);
        selectedButton = 0;
        initialize();
    }

    public void updateWaypoint(Waypoint waypoint)
    {
        this.item = waypoint;
        initialize();
    }

    public Waypoint getWaypoint()
    {
        return item;
    }
    
    public void initialize() {
        if (actualState == STATE_WAITING) {
            actualState = STATE_INITIALIZING;

            actualItem = mapScreen.getActualMapLayer().getLocationCoord(
                    new Location4D(item.getLatitude(), item.getLongitude(), 0.0f)
                    );

            this.itemViewPort = new RectangleViewPort(actualItem.x, actualItem.y, 1, 1);
            this.positionTopLeft = new Location4D(item.getLatitude(), item.getLongitude(), 0.0f);
            this.positionBottomRight = new Location4D(item.getLatitude() - 0.00001, item.getLongitude() + 0.00001, 0.0f);

            String text = " " + item.getName();
            if  (text.length() > 17)
                text = text.substring(0, 14) + "...";
            bmfvName = ColorsFonts.BMF_ARIAL_10_BLACK.getViewer(text);
            bmfvName.layout(textWidth, textWidth, 0, Graphics.TOP | Graphics.LEFT);

            text = "  " + item.getDescription();
            if (text.length() > 38)
                text = text.substring(0, 35) + "...";
            bmfvDesc = ColorsFonts.BMF_ARIAL_10_BLACK.getViewer(text);
            bmfvDesc.layout(textWidth, textWidth, 0, Graphics.TOP | Graphics.LEFT);
            
            text = "  " + GpsUtils.formatLatitude(item.getLatitude(), R.getSettings().getCoordsFormat()) + "\n" +
                   "  " + GpsUtils.formatLongitude(item.getLongitude(), R.getSettings().getCoordsFormat());
            bmfvLatLon = ColorsFonts.BMF_ARIAL_10_BLACK.getViewer(text);
            bmfvLatLon.layout(textWidth, textWidth, 0, Graphics.TOP | Graphics.LEFT);
            
            buttons[0].setSizePos(actualItem.x + (waypointDescriptionBackground.getWidth() / 2) - 2,
                    actualItem.y - waypointDescriptionBackground.getHeight() + 2, 20, 20);
            buttons[1].setSizePos(actualItem.x + (waypointDescriptionBackground.getWidth() / 2) - 2,
                    actualItem.y - waypointDescriptionBackground.getHeight() + 22, 20, 20);
            buttons[2].setSizePos(actualItem.x + (waypointDescriptionBackground.getWidth() / 2) - 2,
                    actualItem.y - waypointDescriptionBackground.getHeight() + 42, 20, 20);
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setCornerRadius(2);
                buttons[i].setColors(ColorsFonts.LIGHT_ORANGE, ColorsFonts.LIGHT_ORANGE,
                        ColorsFonts.ORANGE, ColorsFonts.ORANGE);
                buttons[i].setSelectable(true);
            }
        }
        actualState = STATE_WAITING;
        initialized = true;
    }

    public void panItem(int moveX, int moveY) {
        if (enabled) {
            actualItem.x += moveX;
            actualItem.y += moveY;
            
            itemViewPort.x += moveX;
            itemViewPort.y += moveY;
            
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setPos(buttons[i].getX() + moveX, buttons[i].getY() + moveY);
            }
        }
    }

    public void drawItem(Graphics g) {
        if (enabled) {
            if (!initialized)
                initialize();
            
            if (isInside() && actualState == STATE_WAITING) {
                actualState = STATE_DRAWING;

                g.setColor(ColorsFonts.RED);
                g.fillArc(actualItem.x - 3, actualItem.y - 3, 6, 6, 0, 360);
                
                g.drawImage(waypointDescriptionBackground,
                        actualItem.x, actualItem.y - waypointDescriptionBackground.getHeight(),
                        Graphics.TOP | Graphics.HCENTER);

                int textPosX = actualItem.x - (waypointDescriptionBackground.getWidth() / 2) + 2;
                int textPosY = actualItem.y - waypointDescriptionBackground.getHeight() + 3;
                
                bmfvName.paint(textPosX, textPosY, g);
                textPosY += (bmfvName.getNumberOfLines() * 13);
                g.setColor(ColorsFonts.GRAY);
                g.drawLine(textPosX, textPosY - 2, textPosX + waypointDescriptionBackground.getWidth() - 4, textPosY - 2);
                
                if (!item.getDescription().equals("")) {
                    bmfvDesc.paint(textPosX, textPosY, g);
                    textPosY += (bmfvDesc.getNumberOfLines() * 13);
                }
                bmfvLatLon.paint(textPosX, textPosY, g);
                textPosY += (bmfvLatLon.getNumberOfLines() * 13);
                
                for (int i = 0; i < buttons.length; i++) {
                    if (i == selectedButton)
                        buttons[i].setSelected(true);
                    else
                        buttons[i].setSelected(false);
                    
                    buttons[i].paint(g);
                }
            }
            actualState = STATE_WAITING;
        }
    }

    public void getWaypointsAtPosition(Vector data, int x, int y, int radiusSquare) {
    }
    
    public void selectPrev() {
        if (selectedButton == buttons.length - 1)
            selectedButton = 0;
        else
            selectedButton++;
    }
    
    public void selectNext() {
        if (selectedButton == 0)
            selectedButton = buttons.length - 1;
        else
            selectedButton--;
    }
    
    public void selectButtonAt(int x, int y) {
        for (int i = 0; i < buttons.length; i++) {
//System.out.println("touchInside: x " + x + " y: " + y + " bx: " + buttons[i].getX() + " by: " + buttons[i].getY());
            if (buttons[i].isInside(x, y))
                selectedButton = i;
        }
    }
    
    public int getSelectedType() {
        return selectedButton;
    }
    
    public Waypoint getSelectedWaypoint() {
        return item;
    }

    public boolean touchInside(int x, int y) {
        selectedButton = 0;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isInside(x, y)) {
                selectedButton = i;
                return true;
            }
        }
        return false;
    }
}
