/*
 * ListLabelRenderer.java
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
package com.locify.client.gui.extension;

import com.sun.lwuit.Component;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.list.ListCellRenderer;

/**
 *
 * @author menion
 */
public class ListLabelRenderer extends Label implements ListCellRenderer {

    private int selectionTransparency = 100;
    
    public ListLabelRenderer() {
        super();
    }

    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        if (value instanceof MainScreenItem || value instanceof Label) {
            Label item = (Label) value;
            setText(item.getText());
            setIcon(item.getIcon());
        } else {
            setText(value.toString());
        }

        if (isSelected) {
            setFocus(true);
            getStyle().setBgTransparency(selectionTransparency);
        } else {
            setFocus(false);
            getStyle().setBgTransparency(0);
        }

        return this;
    }

    public Component getListFocusComponent(List list) {
        setText("");
        setIcon(null);
        setFocus(true);
        getStyle().setBgTransparency(selectionTransparency);
        return this;
    }

}
