/*
 * StateLabelWidget.java
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

import com.sun.lwuit.Font;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.plaf.Border;

/**
 *
 * @author menion
 */
public class StateLabelWidget extends Widget {

    private Label labelTitle;
    private Label labelValue;

    public StateLabelWidget() {
        this(BorderLayout.NORTH, Label.CENTER, Label.CENTER, Label.CENTER, Label.CENTER);
    }

    public StateLabelWidget(String titlePosition) {
        this(titlePosition, Label.CENTER, Label.CENTER, Label.CENTER, Label.CENTER);
    }

    public StateLabelWidget(String titlePosition, int titleHAlign, int titleVAlign, int valueHAlign, int valueVAlign) {
        super(new BorderLayout());
        if (titlePosition.equals(BorderLayout.CENTER))
            titlePosition = BorderLayout.NORTH;

        labelTitle = new Label();
        labelTitle.setAlignment(titleHAlign);
        labelTitle.setVerticalAlignment(titleVAlign);
        labelValue = new Label();
        labelValue.setAlignment(valueHAlign);
        labelValue.setVerticalAlignment(valueVAlign);
        labelValue.setText("0.0");

        addComponent(titlePosition, labelTitle);
        addComponent(BorderLayout.CENTER, labelValue);

        labelTitle.getStyle().setMargin(0, 0, 0, 0);
        labelTitle.getStyle().setPadding(0, 0, 0, 0);
        labelValue.getStyle().setMargin(0, 0, 0, 0);
        labelValue.getStyle().setPadding(0, 0, 0, 0);

        getStyle().setBorder(Border.createRoundBorder(4, 4));
        getStyle().setPadding(2, 2, 2, 2); // inner border
        getStyle().setMargin(2, 2, 2, 2); // outer border
    }

    public void setTitle(String title) {
        labelTitle.setText(title);
    }

    public void setTitle(Image icon) {
        labelTitle.setIcon(icon);
    }

    public void setTitleHAlign(int align) {
        labelTitle.setAlignment(align);
    }

    public void setTitleVAlign(int align) {
        labelTitle.setVerticalAlignment(align);
    }

    public void setTitleFont(Font font) {
        labelTitle.getStyle().setFont(font, false);
    }

    public void setTitlePosition(String position) {
        removeComponent(labelTitle);
        addComponent(position, labelTitle);
    }

    public void setValue(String value) {
        labelValue.setText(value);
    }

    public void setValue(Image icon) {
        labelValue.setIcon(icon);
    }

    public void setValueHAlign(int align) {
        labelValue.setAlignment(align);
    }

    public void setValueVAlign(int align) {
        labelValue.setVerticalAlignment(align);
    }

    public void setValueFont(Font font) {
        labelValue.getStyle().setFont(font, false);
    }

    public String getValue() {
        return labelValue.getText();
    }
    
    public void setFonts(Font titleFont, Font valueFont) {
        labelTitle.getStyle().setFont(titleFont, false);
        labelValue.getStyle().setFont(valueFont, false);
    }


}
