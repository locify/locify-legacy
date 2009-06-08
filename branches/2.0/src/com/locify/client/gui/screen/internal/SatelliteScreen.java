/*
 * SatelliteScreen.java
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
package com.locify.client.gui.screen.internal;

import com.locify.client.gui.extension.TopBarBackground;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Capabilities;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.locator.SatellitePosition;
import com.locify.client.utils.Locale;
import com.sun.lwuit.events.ActionEvent;
import com.locify.client.utils.R;
import com.locify.client.utils.math.LMath;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.events.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Screen for showing information about satellites
 * @author menion
 */
public class SatelliteScreen extends FormLocify implements ActionListener, LocationEventListener {

    private int TOP_MARGIN;
    private int BOTTOM_MARGIN;
    private int topPanelHeigh;
    private int space;
    private int lineWidth;
    private int prnHeight;
    private int snrWidth;
    private int snrHeight;
    private int spX;
    private int spY;
    private int radius;
    // temp variables
    private int satInRow;

    public SatelliteScreen() {
        super(Locale.get("Satellites"));

        try {
            addCommand(Commands.cmdBack);
            addCommand(Commands.cmdHome);

            space = 5;

            TOP_MARGIN = R.getTopBar().height + space;
            BOTTOM_MARGIN = R.getTopBar().height + space;
            topPanelHeigh = 80;

            lineWidth = (Capabilities.getWidth() - 2 * space) / 12;
            prnHeight = 15;
            snrWidth = lineWidth - 6;
            snrHeight = topPanelHeigh - prnHeight - space;

            spX = (Capabilities.getWidth() - 2 * space) / 2;
            spY = (Capabilities.getHeight() - TOP_MARGIN - topPanelHeigh - space - BOTTOM_MARGIN) / 2;
            radius = Math.min(spX, spY);
            spX = Capabilities.getWidth() / 2;
            spY = TOP_MARGIN + topPanelHeigh + space + radius;

            setCommandListener(this);
            R.getLocator().addLocationChangeListener(this);
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "SatelliteScreen.constructor()", null);
        }
    }

    public void view() {
        try {
            this.show();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NavigationScreen.view", null);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        satInRow = 0;
        int x, y, angle, dist;
        Hashtable sat = R.getLocator().getSatInView();

        if (sat != null && sat.keys().hasMoreElements()) {
            Enumeration enu = sat.keys();
            g.setColor(ColorsFonts.WHITE);
            g.fillArc(spX - radius, spY - radius, 2 * radius, 2 * radius, 0, 360);
            g.setColor(ColorsFonts.BLACK);
            g.drawArc(spX - radius, spY - radius, 2 * radius, 2 * radius, 0, 360);

            // draw lines
            g.setColor(ColorsFonts.GRAY);
            for (int i = 0; i < 360; i = i + 45) {
                x = (int) (spX + radius * Math.sin(i / LMath.RHO));
                y = (int) (spY + radius * Math.cos(i / LMath.RHO));

                g.drawLine(spX, spY, x, y);
            }
        
            g.setColor(ColorsFonts.BLACK);
            g.setFont(ColorsFonts.FONT_PLAIN_SMALL);
            while (enu.hasMoreElements()) {
                Integer prn = (Integer) enu.nextElement();
                SatellitePosition satel = (SatellitePosition) sat.get(prn);

                g.drawString((prn.intValue() < 10 ? "0" : "") + prn, satInRow * lineWidth + space + 2, TOP_MARGIN + space);
                g.drawRoundRect(satInRow * lineWidth + space, TOP_MARGIN + prnHeight + space,
                        snrWidth, snrHeight, 5, 5);
                int height = (int) (satel.getSnr() / 100.0 * snrHeight);
                g.fillRoundRect(satInRow * lineWidth + space, TOP_MARGIN + prnHeight + space + (snrHeight - height),
                        snrWidth, height, 5, 5);

                angle = (int) satel.getAzimuth();
                dist = (int) (radius - satel.getElevation() * radius / 90.0);
                x = (int) (spX + dist * Math.sin(angle / LMath.RHO));
                y = (int) (spY + dist * Math.cos(angle / LMath.RHO));

                g.drawString("" + prn, x - 4, y - 25);
                Image img = getSatImage(satel.getSnr());
                g.drawImage(img, x - img.getWidth() / 2, y - img.getHeight() / 2);
                satInRow++;
            }
        } else {
            g.setColor(ColorsFonts.BLACK);
            g.drawString(Locale.get("No_satellites"), 10, 50);
        }
    }

    private Image getSatImage(int snr) {
        if (snr == 0) {
            return TopBarBackground.getImgGpsNoSignal();
        } else if (snr > 0 && snr < 33) {
            return TopBarBackground.getImgGpsWeak();
        } else if (snr > 33 && snr < 66) {
            return TopBarBackground.getImgGpsNormal();
        } else {
            return TopBarBackground.getImgGpsStrong();
        }
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        repaint();
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdHome) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getURL().call("locify://mainScreen");
        }
    }
}
