/*
 * Progress.java
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

import com.locify.client.utils.ColorsFonts;
import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.Style;

/**
 * Simple progress indicator component that fills out the progress made.
 * Progress is assumed to always be horizontal in this widget
 *
 * @author Shai Almog reqrite Menion
 */
public class Progress extends Component {

    private int percent;
    private Thread runThread;

    /**
     * The default constructor uses internal rendering to draw the progress
     */
    public Progress() {
        setFocusable(false);
        getStyle().setPadding(Component.LEFT, 10);
        getStyle().setPadding(Component.RIGHT, 10);
    }

    /**
     * Indicate to LWUIT the component name for theming in this case "Progress"
     */
    public String getUIID() {
        return "Progress";
    }

    /**
     * Indicates the percent of progress made
     */
    public int getProgress() {
        return percent;
    }

    public void infiniteRun() {
        runThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    percent++;
                    if (percent > 100)
                        percent = 0;
                    repaint();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        runThread.start();
    }

    public void infiniteStop() {
        if (runThread != null)
            runThread = null;
    }

    /**
     * Indicates the percent of progress made, this method is thread safe and
     * can be invoked from any thread although discression should still be kept
     * so one thread doesn't regress progress made by another thread...
     */
    public void setProgress(int percent) {
        this.percent = percent;
        repaint();
    }

    /**
     * Return the size we would generally like for the component
     */
    protected Dimension calcPreferredSize() {
        // we don't really need to be in the font height but this provides
        // a generally good indication for size expectations
        return new Dimension(Display.getInstance().getDisplayWidth(),
                Font.getDefaultFont().getHeight());
    }

    /**
     * Paint the progress indicator
     */
    public void paint(Graphics g) {
        int width = (int) ((((float) percent) / 100.0f) * getWidth());

        // TODO ... cech colors
        Style s = getStyle();
        g.setColor(s.getBgColor());
        int curve = getHeight() / 2 - 1;
        g.fillRoundRect(g.getClipX(), g.getClipY(), getWidth() - 1, getHeight() - 1, curve, curve);
        g.setColor(ColorsFonts.BLACK);
        g.drawRoundRect(g.getClipX(), g.getClipY(), getWidth() - 1, getHeight() - 1, curve, curve);
        g.setColor(ColorsFonts.BLUE);
        g.fillRoundRect(getX(), getY(), width - 1, getHeight() - 1, curve, curve);
    }
}
