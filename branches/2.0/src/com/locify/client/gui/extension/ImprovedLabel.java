/*
 * ImprovedLabel.java
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

import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;

/**
 *
 * @author menion
 */
public class ImprovedLabel extends Container {

    private Label labelTitle;
    private Label labelValue;

    public ImprovedLabel(String titlePosition) {
        super(new BorderLayout());
        if (titlePosition.equals(BorderLayout.CENTER))
            titlePosition = BorderLayout.NORTH;

        labelTitle = new Label();
        labelValue = new Label();
        labelValue.setText("0.0");

        addComponent(titlePosition, labelTitle);
        addComponent(BorderLayout.CENTER, labelValue);

        labelTitle.getStyle().setMargin(0, 0, 0, 0);
        labelTitle.getStyle().setPadding(0, 0, 0, 0);
        labelValue.getStyle().setMargin(0, 0, 0, 0);
        labelValue.getStyle().setPadding(0, 0, 0, 0);
    }

    public void setTitle(String title) {
        labelTitle.setText(title);
    }

    public void setTitle(Image titleImage) {
        labelTitle.setIcon(titleImage);
    }

    public void setValue(String value) {
        labelValue.setText(value);
    }
    
    public void setFonts(Font titleFont, Font valueFont) {
        labelTitle.getStyle().setFont(titleFont, false);
        labelValue.getStyle().setFont(valueFont, false);
    }


}
