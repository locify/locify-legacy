/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.locify.client.gui.widgets;

import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.plaf.Border;

/**
 *
 * @author menion
 */
public class SatelliteWidget extends Widget {

    String label;

    public SatelliteWidget() {
        super();
        getStyle().setBorder(Border.createRoundBorder(4, 4));
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (R.getLocator().hasSatellites()) {
            label = String.valueOf(R.getLocator().getSatInView().size());
        } else {
            label = "-";
        }

        g.setColor(ColorsFonts.BLACK);
//System.out.println("Sat: " + label + " " + g.getClipX() + " " + g.getClipY() + " " + g.getClipWidth() + " " + g.getClipHeight());
        g.drawString(label,
                g.getClipX() + (g.getClipWidth() - g.getFont().stringWidth(label)) / 2,
                g.getClipY() + (g.getClipHeight() - g.getFont().getHeight()) / 2);
    }
}
