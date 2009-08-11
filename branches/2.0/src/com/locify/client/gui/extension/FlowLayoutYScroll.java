/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.locify.client.gui.extension;

import com.locify.client.utils.Logger;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.layouts.FlowLayout;

/**
 *
 * @author menion
 */
public class FlowLayoutYScroll extends FlowLayout {

    public Dimension getPreferredSize(Container parent) {
//Logger.debug("FlowLayoutYScroll.getPreferesSize()");
        int maxWidth = 0, totalHeight = 0;
        int componentHeight = 0, componentWidth = 0, currentLineWidth = 0;
        final int formWidth = parent.getWidth();
        final int numOfcomponents = parent.getComponentCount();
//Logger.debug("com: " + numOfcomponents);
        if (numOfcomponents > 0) {
            Component cmp = parent.getComponentAt(0);
            componentHeight = cmp.getPreferredH() + cmp.getStyle().getMargin(Component.TOP) + cmp.getStyle().getMargin(Component.BOTTOM);
            componentWidth = cmp.getPreferredW() + cmp.getStyle().getMargin(Component.RIGHT) + cmp.getStyle().getMargin(Component.LEFT);
            currentLineWidth = componentWidth;
            totalHeight = componentHeight;
            maxWidth = currentLineWidth;
            if (numOfcomponents > 1) {
                for (int i = 1; i < numOfcomponents; i++) {
                    cmp = parent.getComponentAt(i);
                    componentHeight = cmp.getPreferredH() + cmp.getStyle().getMargin(Component.TOP) + cmp.getStyle().getMargin(Component.BOTTOM);
                    componentWidth = cmp.getPreferredW() + cmp.getStyle().getMargin(Component.RIGHT) + cmp.getStyle().getMargin(Component.LEFT);
                    currentLineWidth += componentWidth;
                    if (currentLineWidth < formWidth) {
                        maxWidth = Math.max(currentLineWidth, maxWidth);
                    } else {
                        currentLineWidth = componentWidth;
                        totalHeight += componentHeight;
                    }
                }
            }
        }
//Logger.debug("dim: " + (maxWidth +
//                parent.getStyle().getPadding(Component.LEFT) +
//                parent.getStyle().getPadding(Component.RIGHT) + ", " +
//                (totalHeight +
//                parent.getStyle().getPadding(Component.TOP) +
//                parent.getStyle().getPadding(Component.BOTTOM))));
        return new Dimension(
                maxWidth +
                parent.getStyle().getPadding(Component.LEFT) +
                parent.getStyle().getPadding(Component.RIGHT),
                totalHeight +
                parent.getStyle().getPadding(Component.TOP) +
                parent.getStyle().getPadding(Component.BOTTOM));
    }
}
