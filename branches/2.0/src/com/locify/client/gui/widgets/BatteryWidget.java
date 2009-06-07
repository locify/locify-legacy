/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.locify.client.gui.widgets;

import com.locify.client.utils.Capabilities;
import com.locify.client.utils.ColorsFonts;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.Border;

/**
 *
 * @author menion
 */
public class BatteryWidget extends Widget {

    private String batteryLevel;

    public BatteryWidget() {
        super();
        setPreferredSize(new Dimension(40, 20));

        getStyle().setBorder(Border.createRoundBorder(4, 4));
    }

    public void paint(Graphics g) {
        super.paint(g);
        getBatteryLevel();

        g.setColor(ColorsFonts.BLACK);
        if (g.getClipWidth() > 20 && g.getClipHeight() > 16) {
            g.drawRect(g.getClipX() + 5, g.getClipY() + 5, g.getClipWidth() - 15, g.getClipHeight() - 10);
            g.drawRect(g.getClipX() + g.getClipWidth() - 10, g.getClipY() + 7, 5, g.getClipHeight() - 14);
        }
        if (batteryLevel == null || batteryLevel.length() == 0) {
            batteryLevel = "?";
        }
//System.out.println("Bat: " + batteryLevel + " " + g.getClipX() + " " + g.getClipY() + " " + g.getClipWidth() + " " + g.getClipHeight());
        g.drawString(batteryLevel,
                g.getClipX() + (g.getClipWidth() - g.getFont().stringWidth(batteryLevel)) / 2,
                g.getClipY() + (g.getClipHeight() - g.getFont().getHeight()) / 2);
    }

    private void getBatteryLevel() {
        if (Capabilities.isNokia())
            batteryLevel = System.getProperty("com.nokia.mid.batterylevel");
        else
            batteryLevel = System.getProperty("batterylevel");
    }
}
