/*
 * ScreenItem.java
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
import com.locify.client.utils.R;
import de.enough.polish.util.BitMapFont;
import de.enough.polish.util.BitMapFontViewer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Bubble for showing info on route recording screen
 * @author Menion
 */
public class ScreenItem extends Item {
   
    private BitMapFontViewer bmfvValue;
    
    /* imageVariables imageLabel has priorite*/
    private Image imageLabel;
    
    private String textValue;
    private boolean textEmpty;
    private int textValueXPos;
    private BitMapFont textValueFont = ColorsFonts.BMF_ARIAL_10_BLACK;
    
    private boolean selected = false;
    private boolean active = true;
    
    private int colorBackgroundInactive = ColorsFonts.CYAN;

    private int move = 0;
    private Image backgroundImage;

    /**
     * Basic constructor
     * @param label text label of item
     * @param hTextIntend horizontal intend
     * @param vTextIntend vertical intend
     */
    public ScreenItem(String textLabel, int hTextIntend, int vTextIntend) {
        super(textLabel, hTextIntend, vTextIntend);
        setTextValue("");
    }

    public ScreenItem(String textLabel) {
        this(textLabel, 0, 0);
    }
    
    /**
     * create item only with image
     * @param imageLabel
     */
    public ScreenItem(Image imageLabel) {
        super("");
        this.imageLabel = imageLabel;
        setTextValue("");
    }        

    /**
     * set text as actual displayed value.
     * @param textValue
     */
    public void setTextValue(String textValue) {
        this.textValue = textValue;
        this.textEmpty = (textValue.equals(""));
        bmfvValue = textValueFont.getViewer(textValue);
        bmfvValue.layout(w, w, 0, Graphics.TOP | Graphics.LEFT);

        if (hAlign == ALIGN_LEFT)
            textValueXPos = x + hIntend;
        else {
            move = (w - bmfvValue.getWidth()) / 2;
            if (move < 0)
                move = 0;

            if (hAlign == ALIGN_CENTER)
                textValueXPos = x + move + hIntend;
            else if (hAlign == ALIGN_RIGHT)
                textValueXPos = x + 2 * move + hIntend;        
        }
    }
    
    public void setAlignment(int horizontal, int vertical) {
        super.setAlignment(horizontal, vertical);
        setTextValue(textValue);
    }

    /**
     * set size of item
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        super.setSize(width, height);
        setTextValue(textValue);
    }
    
    /**
     * Set fonts
     * @param textLabelFont font for text label
     * @param textValueFont font for display values
     */
    public void setFont(BitMapFont textLabelFont, BitMapFont textValueFont) {
        super.setFont(textLabelFont);
        this.textValueFont = textValueFont;
        setTextValue(textValue);
    }
    
    /**
     * Set colors of item
     * @param colorBackground color for background or '0! for transparent
     * @param colorBackgroundInactive color for inactive button
     * @param selectionColor color for selection
     */
    public void setColors(int colorBackground, int colorBackgroundInactive, int colorSelection, int colorBorder) {
        super.setColors(colorBackground, colorSelection, colorBorder);
        this.colorBackgroundInactive = colorBackgroundInactive;
    }

    private Image getBackgroundImage() {
        if (backgroundImage == null) {
            backgroundImage = Image.createImage(w, h);
            Graphics g = backgroundImage.getGraphics();
            g.setColor(ColorsFonts.LIGHT_ORANGE);
            g.fillRect(0, 0, w, h);

            // draw background
            if (colorBackground == 0 && !selected && active) {
                // nothing to draw
            } else {
                if (selected) {
                    g.setColor(colorSelection);
                } else if (!active) {
                    g.setColor(colorBackgroundInactive);
                } else {
                    g.setColor(colorBackground);
                }
                g.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);
            }

            g.setColor(colorBorder);
            g.drawRoundRect(0, 0, w-1, h-1, cornerRadius, cornerRadius);
        }
        return backgroundImage;
    }

    public void paint(Graphics g) {
        try {
            if (visible) {
                g.drawImage(getBackgroundImage(), x, y, Graphics.TOP | Graphics.LEFT);

                // draw label
                if (imageLabel != null) {
                    int drawX = x;// + 50;
                    int drawY = y + (h - imageLabel.getHeight()) / 2;
                    g.drawImage(imageLabel, drawX, drawY, Graphics.TOP | Graphics.LEFT);
                } else {
                    if (textEmpty)
                        move = y + hIntend + (h - bmfvLabel.getHeight()) / 2;
                    else
                        move = y + hIntend + (h - bmfvLabel.getHeight() - bmfvValue.getHeight()) / 3;

                    bmfvLabel.paint(textLabelXPos, move, g);
                }
                
                //draw value
                if (!textEmpty) {
                    move = (h - bmfvLabel.getHeight() - bmfvValue.getHeight()) / 3;
                    move = y + hIntend + bmfvLabel.getHeight() + 2 * move;
                    bmfvValue.paint(textValueXPos, move, g);
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ScreenItem.paint(Graphics g)", null);
        }
    }

    /**
     * test for pointer click if is inside object
     * @param x coordinate x
     * @param y coordinate y
     * @return true if inside, otherwise false
     */
    public boolean isInside(int x, int y) {
        if (selectable) {
            if ((x > this.x && x < (this.x + this.w)) &&
                    (y > this.y && y < (this.y + this.h))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set button to state active/inactive
     * @param inactive set to active mode
     */
    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            this.selectable = active;
            backgroundImage = null;
        }
    }

    /**
     * Set if button is actually selected
     * @param selected true if selected, default false
     */
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            backgroundImage = null;
        }
    }

    /**
     * Set if button can be selected
     * @param selectable true if can, default false
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }
}
