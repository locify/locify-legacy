/*
 * ListLabelItem.java
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

import com.sun.lwuit.Image;
import com.sun.lwuit.Label;

/**
 *
 * @author menion
 */
public class ListLabelItem extends Label {

    public ListLabelItem(String text, Image image) {
        super(text);
        setIcon(image);
    }
}
