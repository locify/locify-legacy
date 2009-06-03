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

import com.sun.lwuit.Container;
import com.sun.lwuit.layouts.BorderLayout;

/**
 *
 * @author menion
 */
public class Widget extends Container {

    private Container parent;
    private String constraints;

    public Widget() {
        super();
    }

    public void setWidgetParent(Container parent) {
//System.out.println("SetParent: " + parent);
        this.parent = parent;
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
}
