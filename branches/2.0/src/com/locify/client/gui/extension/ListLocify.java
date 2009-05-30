/*
 * ListLocify.java
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

import com.sun.lwuit.List;
import com.sun.lwuit.list.DefaultListModel;

/**
 *
 * @author menion
 */
public class ListLocify extends List {

    public ListLocify() {
        setModel(new DefaultListModel());
        setListCellRenderer(new ListLabelRenderer());
    }

    public void removeAll() {
        for (int i = getModel().getSize() - 1; i >= 0; i--) {
            getModel().removeItem(i);
        }
    }

    public int getSize() {
        return getModel().getSize();
    }
    
    public Object getItemAt(int index) {
        return getModel().getItemAt(index);
    }

    public void removeItem(int index) {
        getModel().removeItem(index);
    }
}
