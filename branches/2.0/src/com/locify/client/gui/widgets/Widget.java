/*
 * Widget.java
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
package com.locify.client.gui.widgets;

import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.ColorsFonts;
import com.sun.lwuit.Container;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.plaf.Border;
import org.xmlpull.v1.XmlPullParser;

/**
 *
 * @author menion
 */
public class Widget extends Container {

    private Container parent;
    private String constraints;
    private String linkTo;
    private Border defaultBorder;
    private int defaultBgColor;

    public Widget() {
        super();
        this.defaultBorder = getStyle().getBorder();
        if (defaultBorder == null)
            defaultBorder = Border.getDefaultBorder();
        this.defaultBgColor = getStyle().getBgColor();

//System.out.println("Border: " + defaultBorder);
//System.out.println("Color: " + defaultBgColor);
    }

    public void setWidgetParent(Container parent, XmlPullParser parser) {
//System.out.println("SetParent: " + parent);
        this.parent = parent;
        if (parent.getLayout() instanceof BorderLayout) {
            setConstrains(FormLocify.getBorderLayoutPositionValue(parser.getAttributeValue(null, "position"), true));
        }
    }

    public Container getWidgetParent() {
        return parent;
    }

    public void setConstrains(String constraints) {
        this.constraints = constraints;
    }

    public String getConstaints() {
        return constraints;
    }

    public void addToParent() {
//System.out.println("AddToParent: " + parent);
//System.out.println("AddToParentCom: " + this);
        if (parent.getLayout() instanceof BorderLayout)
            parent.addComponent(constraints, this);
        else
            parent.addComponent(this);
    }

    public void setLinkTo(String linkTo) {
        this.linkTo = linkTo;
    }

    public String getLinkTo() {
        return linkTo;
    }

    public void showFocused(boolean focused) {
        if (focused) {
            this.getStyle().setBorder(Border.createBevelRaised());
            this.getStyle().setBgColor(ColorsFonts.RED);
        } else {
            this.getStyle().setBorder(defaultBorder);
            this.getStyle().setBgColor(defaultBgColor);
        }
    }
}
