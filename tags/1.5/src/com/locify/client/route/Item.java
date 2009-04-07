/*
 * Item.java
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

package com.locify.client.route;

import com.locify.client.utils.ColorsFonts;
import de.enough.polish.util.BitMapFont;
import de.enough.polish.util.BitMapFontViewer;
import javax.microedition.lcdui.Graphics;

/**
 * Abstract route item
 * @author Menion
 */
public abstract class Item {

    protected BitMapFontViewer bmfvLabel;

    // aligment settings
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_TOP = 3;
    public static final int ALIGN_BOTTOM = 4;
    
    protected int hAlign = 1;
    protected int vAlign = 1;

    protected String textLabel;
    protected int textLabelXPos;
    protected BitMapFont textLabelFont = ColorsFonts.BMF_ARIAL_10_BLACK;

    protected int x = 0;
    protected int y = 0;
    protected int w = 0;
    protected int h = 0;
    protected int hIntend = 0;
    protected int vIntend = 0;
    protected int cornerRadius = 15;
    protected boolean visible = false;
    protected boolean selectable = false;

    // color variables
    protected int colorSelection = ColorsFonts.ORANGE;
    protected int colorBackground = ColorsFonts.LIGHT_GRAY;
    protected int colorBorder = ColorsFonts.ORANGE;

    public Item(String textLabel, int hTextIntend, int vTextIntend) {
        this.hIntend = hTextIntend;
        this.vIntend = vTextIntend;
        setTextLabel(textLabel);
    }

    public Item(String textLabel) {
        this(textLabel, 0, 0);
    }

    /**
     * Sez size and position of item and set them ready to display and visible.
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setSizePos(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        setSize(width, height);
        setVisible(true);
    }

    /**
     * set size of item
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        this.w = width;
        this.h = height;
        setTextLabel(textLabel);
    }

    /**
     * Set button to be visible
     * @param visible true to visible, default false
     * @return true if succesfully set to be visible
     */
    public boolean setVisible(boolean visible) {
        if (w != 0 && h != 0) {
            this.visible = visible;
            return true;
        } else
            return false;
    }
    
    /**
     * set text as label
     * @param textLabel
     */
    public void setTextLabel(String textLabel) {
        this.textLabel = textLabel;
        bmfvLabel = textLabelFont.getViewer(textLabel);
        bmfvLabel.layout(w, w, 0, Graphics.TOP | Graphics.LEFT);

        if (hAlign == ALIGN_LEFT)
            textLabelXPos = x + hIntend;
        else {
            int move;
            move = (w - bmfvLabel.getWidth()) / 2;
            if (move < 0)
                move = 0;

            if (hAlign == ALIGN_CENTER)
                textLabelXPos = x + move + hIntend;
            else if (hAlign == ALIGN_RIGHT)
                textLabelXPos = x + 2 * move + hIntend - 10;
        }
    }

    /**
     * Set colors of item
     * @param colorBackground color for background or '0! for transparent
     * @param colorBackgroundInactive color for inactive button
     * @param selectionColor color for selection
     */
    public void setColors(int colorBackground, int colorSelection, int colorBorder) {
        this.colorBackground = colorBackground;
        this.colorSelection = colorSelection;
        this.colorBorder = colorBorder;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    /**
     * Set fonts
     * @param textLabelFont font for text label
     * @param textValueFont font for display values
     */
    public void setFont(BitMapFont textLabelFont) {
        this.textLabelFont = textLabelFont;
        setTextLabel(textLabel);
    }

    public void setAlignment(int horizontal, int vertical) {
        this.hAlign = horizontal;
        this.vAlign = vertical;
        setTextLabel(textLabel);
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Info if this button is able to select
     * @return true if selectable
     */
    public boolean isSelectable() {
        if (visible) {
            return selectable;
        } else {
            return false;
        }
    }
    
    public abstract void paint(Graphics g);
}
