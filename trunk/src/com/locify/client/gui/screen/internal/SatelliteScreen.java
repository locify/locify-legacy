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

import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.data.IconData;
import com.locify.client.locator.LocationContext;
import com.locify.client.locator.LocationProvider;
import com.locify.client.locator.SatellitePosition;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import com.locify.client.utils.R;
import de.enough.polish.ui.Form;
import de.enough.polish.util.Locale;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Screen for showing information about satellites
 * @author menion
 */
public class SatelliteScreen extends Form implements CommandListener, LocationEventListener {

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
    private Image satImageNoSignal;
    private Image satImageWeak;
    private Image satImageNormal;
    private Image satImageStrong;
    // temp variables
    private int satInRow;

    public SatelliteScreen() {
        super(Locale.get("Satellites"));

        try {
            addCommand(Commands.cmdBack);
            //#style imgHome
            addCommand(Commands.cmdHome);

            space = 5;

            TOP_MARGIN = R.getTopBar().height + space;
            BOTTOM_MARGIN = R.getTopBar().height + space;
            topPanelHeigh = 80;

            lineWidth = (getWidth() - 2 * space) / 12;
            prnHeight = 15;
            snrWidth = lineWidth - 6;
            snrHeight = topPanelHeigh - prnHeight - space;

            spX = (getWidth() - 2 * space) / 2;
            spY = (getHeight() - TOP_MARGIN - topPanelHeigh - space - BOTTOM_MARGIN) / 2;
            radius = Math.min(spX, spY);
            spX = getWidth() / 2;
            spY = TOP_MARGIN + topPanelHeigh + space + radius;

            satImageNoSignal = IconData.get("locify://icons/status_nosignal.png");
            satImageWeak = IconData.get("locify://icons/status_weak.png");
            satImageNormal = IconData.get("locify://icons/status_normal.png");
            satImageStrong = IconData.get("locify://icons/status_strong.png");

            setCommandListener(this);
            R.getLocator().addLocationChangeListener(this);
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "SatelliteScreen.constructor()", null);
        }
    }

    public void view() {
        try {
            R.getLocator().setSatScreenActive(true);
            R.getMidlet().switchDisplayable(null, this);

        } catch (Exception e) {
            R.getErrorScreen().view(e, "NavigationScreen.view", null);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        satInRow = 0;
        Hashtable sat = R.getLocator().getSatInView();
        g.setColor(ColorsFonts.WHITE);
        g.fillArc(spX - radius, spY - radius, 2 * radius, 2 * radius, 0, 360);
        g.setColor(ColorsFonts.BLACK);
        g.drawArc(spX - radius, spY - radius, 2 * radius, 2 * radius, 0, 360);

        // draw lines
        int x, y, angle, dist;
        g.setColor(ColorsFonts.GRAY);
        for (int i = 0; i < 360; i = i + 45) {
            x = (int) (spX + radius * Math.sin(i / GpsUtils.RHO));
            y = (int) (spY + radius * Math.cos(i / GpsUtils.RHO));

            g.drawLine(spX, spY, x, y);
        }

        Enumeration enu = sat.keys();
        g.setColor(ColorsFonts.BLACK);
        g.setFont(ColorsFonts.FONT_PLAIN_SMALL);
        while (enu.hasMoreElements()) {
            Integer prn = (Integer) enu.nextElement();
            SatellitePosition satel = (SatellitePosition) sat.get(prn);

            g.drawString((prn.intValue() < 10 ? "0" : "") + prn, satInRow * lineWidth + space + 2, TOP_MARGIN + space,
                    Graphics.TOP | Graphics.LEFT);
            g.drawRoundRect(satInRow * lineWidth + space, TOP_MARGIN + prnHeight + space,
                    snrWidth, snrHeight, 5, 5);
            int height = (int) (satel.getSnr() / 100.0 * snrHeight);
            g.fillRoundRect(satInRow * lineWidth + space, TOP_MARGIN + prnHeight + space + (snrHeight - height),
                    snrWidth, height, 5, 5);

            angle = (int) satel.getAzimuth();
            dist = (int) (radius - satel.getElevation() * radius / 90.0);
            x = (int) (spX + dist * Math.sin(angle / GpsUtils.RHO));
            y = (int) (spY + dist * Math.cos(angle / GpsUtils.RHO));

            g.drawString("" + prn, x - 4, y - 25, Graphics.TOP | Graphics.LEFT);
            g.drawImage(getSatImage(satel.getSnr()), x, y, Graphics.VCENTER | Graphics.HCENTER);
            satInRow++;
        }
    }

    private Image getSatImage(int snr) {
        if (snr == 0) {
            return satImageNoSignal;
        } else if (snr > 0 && snr < 33) {
            return satImageWeak;
        } else if (snr > 33 && snr < 66) {
            return satImageNormal;
        } else {
            return satImageStrong;
        }
    }

    public void commandAction(Command cmd, Displayable d) {
        if (cmd.equals(Commands.cmdBack)) {
            R.getLocator().setSatScreenActive(false);
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getBack().goBack();
        } else if (cmd.equals(Commands.cmdHome)) {
            R.getLocator().setSatScreenActive(false);
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getURL().call("locify://mainScreen");
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
}
