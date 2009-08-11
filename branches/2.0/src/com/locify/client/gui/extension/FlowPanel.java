/*
 * FlowPanel.java
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

import com.locify.client.data.IconData;
import com.sun.lwuit.Button;
import com.sun.lwuit.Container;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.plaf.Border;
import java.io.IOException;

/**
 *
 * @author menion
 */
public class FlowPanel extends Container {

    private Button visibleButton;
    private Container itemContainer;

    private static Image imgHor;
    private static Image imgVer;

    private boolean isShown;
    
    public FlowPanel(String position) {
        super(new BorderLayout());

        if (imgHor == null) {
            imgHor = IconData.getLocalImage("3Dhor.png").scaledHeight(3);
            imgVer = IconData.getLocalImage("3Dver.png").scaledWidth(3);
        }

        visibleButton = new Button();
        visibleButton.setAlignment(Label.CENTER);
        visibleButton.setVerticalAlignment(Label.CENTER);
        visibleButton.getStyle().setBorder(Border.createLineBorder(2));
//        visibleButton.getStyle().setBgColor(ColorsFonts.CYAN);
        visibleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                switchVisibility();
            }
        });
        itemContainer = new Container();
        if (position.equals(BorderLayout.CENTER) || position.equals(BorderLayout.NORTH)) {
            visibleButton.setIcon(imgHor);
            addComponent(BorderLayout.SOUTH, visibleButton);
        } else if (position.equals(BorderLayout.SOUTH)) {
            visibleButton.setIcon(imgHor);
            addComponent(BorderLayout.NORTH, visibleButton);
        } else if (position.equals(BorderLayout.WEST)) {
            visibleButton.setIcon(imgVer);
            addComponent(BorderLayout.EAST, visibleButton);
        } else if (position.equals(BorderLayout.EAST)) {
            visibleButton.setIcon(imgVer);
            addComponent(BorderLayout.WEST, visibleButton);
        }
        //addComponent(BorderLayout.CENTER, itemContainer);

        isShown = false;
    }

    public Container getContentPane() {
        return itemContainer;
    }

    public void switchVisibility() {
        if (isShown) {
            addComponent(BorderLayout.CENTER, itemContainer);
        } else {
            removeComponent(itemContainer);
        }
        isShown = isShown ? false : true;
        revalidate();
    }
}
