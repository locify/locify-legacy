/*
 * GraphItem.java
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
package com.locify.client.route;

import com.locify.client.locator.Location4D;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Graph for showing altitude
 * @author Menion
 */
public class GraphItem extends Item {
    /* text varibables */
    private int typeValueX;
    private int typeValueY;

    public static final int VALUE_X_TOTAL_TIME = 0;
    public static final int VALUE_X_TOTAL_DIST = 1;
    public static final int VALUE_Y_ALTITUDE = 2;
    public static final int VALUE_Y_LATITUDE = 3;
    public static final int VALUE_Y_LONGITUDE = 4;

    private int colorAround = ColorsFonts.LIGHT_ORANGE;
    private int colorGraph = ColorsFonts.BLUE;
    private int colorText = ColorsFonts.BLACK;
    
    //private int distScale = 1000; // m
    //private int timeScale = 300; // s
    private double measureX;
    private int lastSize = 1;
    private String unitX;
    private String unitY;

    private Vector values;

    private Image graphImage;
    
    /**
     * Basic constructor
     * @param label text label of item
     */
    public GraphItem(String textLabel, int typeValueX, int typeValueY, double measureX) {
        super(textLabel);
        
        this.typeValueX = typeValueX;
        this.typeValueY = typeValueY;
        this.measureX = measureX;
        this.values = new Vector();

        if (typeValueX == VALUE_X_TOTAL_DIST) {
            unitX = "m";
        } else if (typeValueX == VALUE_X_TOTAL_TIME) {
            unitX = "s";
        }

        if (typeValueY == VALUE_Y_ALTITUDE ||
                typeValueY == VALUE_Y_LATITUDE ||
                typeValueY == VALUE_Y_LONGITUDE)
            unitY = "m";
    }

/*    public void reset() {
        values = new Vector();
        printGraph();
    }
 */

    public void refreshGraph(RouteVariables variables) {
        try {
            double value = 0;
            Location4D loc;
            values = new Vector();

            if (variables.routePoints.size() > 0) {
                if (typeValueX == VALUE_X_TOTAL_DIST) {
//System.out.println("2 " + variables.routePoints.size());
                    for (int i = variables.routePoints.size() - 1; i >= 0; i--) {
                        if (value > measureX)
                            break;
                        else {
                            loc = (Location4D) variables.routePoints.elementAt(i);
                            if (loc == null)
                                continue;
                            value += ((Double) variables.routeDistances.elementAt(i)).doubleValue();
                            addValues(value, loc);
                        }
                    }
                } else if (typeValueX == VALUE_X_TOTAL_TIME) {
//System.out.println("3");
                    double endTime = ((Location4D) variables.routePoints.elementAt(variables.routePoints.size() - 1)).getTime();
                    for (int i = variables.routePoints.size() - 1; i >= 0; i--) {
                        if (value > measureX)
                            break;
                        else {
                            loc = (Location4D) variables.routePoints.elementAt(i);
                            if (loc == null)
                                continue;
                            value = ((endTime - loc.getTime()) / 1000.0);
                            addValues(value, loc);
                        }
//System.out.println(values.size());
                    }
                }
                lastSize = variables.routePoints.size();
                printGraph();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GraphItem.refreshGraph()", null);
        }
    }

    public void setMeasureX(double measureX) {
        this.measureX = measureX;
    }

    private void printGraph() {
        Image image = createEmptyImage();
        if (image != null) {
            Graphics g = image.getGraphics();

            int move = 0;
            if (vAlign == ALIGN_TOP)
                move = hIntend;
            else if (vAlign == ALIGN_CENTER)
                move = hIntend + (h - bmfvLabel.getHeight()) / 2;
            else if (vAlign == ALIGN_BOTTOM)
                move = hIntend + (h - bmfvLabel.getHeight());
            bmfvLabel.paint(textLabelXPos, move, g);

            // variables
            int transX = 15;
            int transY = 15;
            int border = 5;
            int graphWidth = g.getClipWidth() - transX - 2 * border;
            int graphHeight = g.getClipHeight() - transY - 2 * border;
            double yMin = Double.MAX_VALUE;
            double yMax = Double.MIN_VALUE;
            GraphValues value;
            for (int i = 0; i < values.size(); i++) {
                value = (GraphValues) values.elementAt(i);
                yMin = yMin < value.yDouble ? yMin : value.yDouble;
                yMax = yMax > value.yDouble ? yMax : value.yDouble;
            }

            // lines and texts
            g.setColor(colorText);
            g.drawLine(transX, border, transX, g.getClipHeight() - border);
            g.drawLine(border, g.getClipHeight() - transY, g.getClipWidth() - border, g.getClipHeight() - transY);
            g.drawLine(g.getClipWidth() - border, g.getClipHeight() - transY, g.getClipWidth() - border, g.getClipHeight() - border);

            g.setFont(ColorsFonts.FONT_BOLD_MEDIUM);
            String text = GpsUtils.formatDouble(measureX, 0) + " " + unitX;
            g.drawString(text, (g.getClipWidth() - ColorsFonts.FONT_BOLD_MEDIUM.stringWidth(text)) / 2,
                    g.getClipHeight() - 15, Graphics.TOP | Graphics.LEFT);

            Math.ceil(yMin);
            Math.floor(yMax);
            int det = 1;
            if ((yMax - yMin) < 50)
                det = 10;
            else if ((yMax - yMin) < 500)
                det = 100;
            else if ((yMax - yMin) < 5000)
                det = 1000;

            g.setColor(ColorsFonts.GRAY);
            for (int i = (int) Math.ceil(yMin / det); i <= Math.floor(yMax / det); i++) {
                int yValue = border + graphHeight - ((int) ((i * det - yMin) / (yMax - yMin) * graphHeight));
                text = i * det + unitY;
                g.drawLine(transX, yValue, g.getClipWidth() - border - ColorsFonts.FONT_BOLD_MEDIUM.stringWidth(text) - 5, yValue);
                g.drawString(text, g.getClipWidth() - border, yValue - 7, Graphics.TOP | Graphics.RIGHT);
            }

            for (int i = 0; i < values.size(); i++) {
                value = (GraphValues) values.elementAt(i);
                value.x = transX + 2 + (int) (value.xDouble / measureX * graphWidth);
                value.y = border + graphHeight - ((int) ((value.yDouble - yMin) / (yMax - yMin) * graphHeight));
            }

            g.setColor(colorGraph);
            for (int i = 1; i < values.size(); i++) {
                g.drawLine(((GraphValues) values.elementAt(i - 1)).x, ((GraphValues) values.elementAt(i - 1)).y,
                        ((GraphValues) values.elementAt(i)).x, ((GraphValues) values.elementAt(i)).y);
            }

            graphImage = image;
        }
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        graphImage = createEmptyImage();
        printGraph();
    }

    private Image createEmptyImage() {
        if (w != 0 && h != 0) {
            Image image = Image.createImage(w, h);
            Graphics g = image.getGraphics();

            // background
            g.setColor(colorAround);
            g.fillRect(0, 0, w, h);
            g.setColor(colorBackground);
            g.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);
            g.setColor(colorBorder);
            g.drawRoundRect(0, 0, w-1, h-1, cornerRadius, cornerRadius);

            return image;
        } else {
            return null;
        }
    }

    public void setColors(int colorAroundItem, int colorBackground, int colorBorder,
            int colorText, int colorGraph) {
        super.setColors(colorBackground, -1, colorBorder);
        this.colorAround = colorAroundItem;
        this.colorText = colorText;
        this.colorGraph = colorGraph;

    }

    private void addValues(double value, Location4D loc) {
        if (typeValueY == VALUE_Y_LATITUDE)
            values.addElement(new GraphValues(value, loc.getLatitude()));
        else if (typeValueY == VALUE_Y_LONGITUDE)
            values.addElement(new GraphValues(value, loc.getLongitude()));
        else if (typeValueY == VALUE_Y_ALTITUDE)
            values.addElement(new GraphValues(value, loc.getAltitude()));
    }

    public void paint(Graphics g) {
        try {
            if (visible && graphImage != null) {
                g.drawImage(graphImage, x, y, Graphics.TOP | Graphics.LEFT);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ScreenItem.paint(Graphics g)", null);
        }
    }

    private class GraphValues {
        double xDouble;
        double yDouble;
        int x;
        int y;
        public GraphValues(double x, double y) {
            this.xDouble = x;
            this.yDouble = y;
        }

        public String toString() {
            return "{X: " + xDouble + "  Y: " + yDouble + " x: " + x + "  y: " + y + "}";
        }
    }
}
