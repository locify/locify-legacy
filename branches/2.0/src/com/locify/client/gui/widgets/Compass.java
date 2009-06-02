/*
 * Compass.java
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

import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import com.locify.client.utils.math.LMath;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.layouts.BorderLayout;

/**
 *
 * @author menion
 */
public class Compass extends Widget {

    // navigation angle (heading)
    public double nAngle;
    // diference angle beetween heading and navigated point
    public double dAngle;
    // if navigation is active, to show arrow
    private boolean activeNavigation;
    
    // temp items
    private boolean drawLock;
    // center of compas X coordinate
    private int cX;
    // center of compas Y coordinate
    private int cY;
    // compas radius
    private int radius;
    // check if screen is too small and use lighter version of compass
    private boolean smallRadius;

    // font for labels
    private Font font;
    
    public Compass() {
        super(new BorderLayout());
        this.drawLock = false;
        this.nAngle = 0;
        this.dAngle = 0;
        this.activeNavigation = false;

        this.font = ColorsFonts.FONT_BMF_10;
    }

    public void setLabelsFont(Font font) {
        this.font = font;
    }

    public void paint(Graphics g) {
        try {
//            super.paint(g);
            if (drawLock) {
                return;
            }
            drawLock = true;

//            g.setColor(ColorsFonts.BLUE);
//            g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());

            cX = g.getClipWidth() / 2 + g.getClipX();
            cY = g.getClipHeight() / 2 + g.getClipY();
            radius = Math.min(g.getClipWidth(), g.getClipHeight()) / 2 - 5;
            smallRadius = radius < 60 ? true : false;

            g.setFont(font);
            setCompas(g);
            setArrow(g);

            drawLock = false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.paint()", null);
        }
    }

    // angle in degres
    private void setCompas(Graphics g) {
        g.setColor(ColorsFonts.WHITE);
        g.fillArc(cX - radius, cY - radius, 2 * radius, 2 * radius, 0, 360);

        g.setColor(ColorsFonts.BLACK);
        g.drawArc(cX - radius, cY - radius, 2 * radius, 2 * radius, 0, 360);

        double a;
        int x1, x2, y1, y2;
        int lineLength = radius - 10;
        int stringPosition = radius - 23;
        if (smallRadius) {
            lineLength = radius - 5;
            stringPosition = radius - 18;
        }

        String label;
        for (int i = 0; i < 36; i++) {
            if (smallRadius && i % 3 != 0) {
                continue;
            }

            a = (i * 10 - nAngle) / LMath.RHO;

            double aSin = Math.sin(a);
            double aCos = Math.cos(a);
            x1 = (int) (aSin * lineLength);
            y1 = (int) (aCos * lineLength);
            x2 = (int) (aSin * radius);
            y2 = (int) (aCos * radius);
            g.drawLine(cX + x1, cY - y1, cX + x2, cY - y2);

            int separator = 3;
            if (smallRadius) {
                separator = 9;
            }

            if (i % separator == 0) {
                x1 = (int) (aSin * stringPosition);
                y1 = (int) (aCos * stringPosition);

                label = getCompasLabel(i / 3);
                g.drawString(label, cX + x1 - g.getFont().stringWidth(label) / 2,
                        cY - y1 - g.getFont().getHeight() / 2);
            }
        }
    }

    private String getCompasLabel(int index) {
        switch (index) {
            case 0:
                return "N";
            case 1:
                return "30";
            case 2:
                return "60";
            case 3:
                return "E";
            case 4:
                return "120";
            case 5:
                return "150";
            case 6:
                return "S";
            case 7:
                return "210";
            case 8:
                return "240";
            case 9:
                return "W";
            case 10:
                return "300";
            case 11:
                return "330";
        }
        return "";
    }

    private void setArrow(Graphics g) {
        if (activeNavigation) {
            g.setColor(ColorsFonts.WHITE);

            double a;
            int x1, x2, x3, x4, y1, y2, y3, y4;

            a = dAngle / LMath.RHO;
            x1 = (int) (Math.sin(a) * radius * 0.65);
            y1 = (int) (Math.cos(a) * radius * 0.65);

            a = (dAngle + 180) / LMath.RHO;
            x2 = (int) (Math.sin(a) * (radius * 0.2));
            y2 = (int) (Math.cos(a) * (radius * 0.2));

            a = (dAngle + 140) / LMath.RHO;
            x3 = (int) (Math.sin(a) * (radius * 0.5));
            y3 = (int) (Math.cos(a) * (radius * 0.5));

            a = (dAngle + 220) / LMath.RHO;
            x4 = (int) (Math.sin(a) * (radius * 0.5));
            y4 = (int) (Math.cos(a) * (radius * 0.5));

            g.setColor(ColorsFonts.RED);

            g.drawLine(cX + x1, cY - y1, cX + x3, cY - y3);
            g.drawLine(cX + x3, cY - y3, cX + x2, cY - y2);
            g.drawLine(cX + x2, cY - y2, cX + x4, cY - y4);
            g.drawLine(cX + x4, cY - y4, cX + x1, cY - y1);

            g.fillTriangle(cX + x1, cY - y1, cX + x2, cY - y2, cX + x3, cY - y3);
            g.fillTriangle(cX + x1, cY - y1, cX + x2, cY - y2, cX + x4, cY - y4);
        }
    }

    /**
     * Function which rotate arrow and compas (angles in degrees)
     * @param nAngle new angle for compas north
     * @param dAngle new angle for arrow
     */
    public void moveAngles(final double nAng, final double dAng) {
        boolean move = true;
//Logger.log("nAngle: " + nAngle + " dAngle: " + dAngle);
        if (move) {
            final double nDiff = getDiffAngle(nAngle, nAng);
            final double dDiff = getDiffAngle(dAngle, dAng);
            final int numOfSteps = 5;

            Thread thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        for (int i = 0; i < numOfSteps; i++) {
                            nAngle = fixDegAngle(nAngle + (nDiff / numOfSteps));
                            dAngle = fixDegAngle(dAngle + (dDiff / numOfSteps));
                            repaint();
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        nAngle = nAng;
                        dAngle = dAng;
                        repaint();
                    } catch (Exception e) {
                        R.getErrorScreen().view(e, "NavigationScreen.moveAngles.run()", null);
                    }
                }
            });

            if (nDiff != 0 || dDiff != 0) {
                thread.start();
            }
        } else {
            nAngle = nAng;
            dAngle = dAng;
        }
    }


    private double getDiffAngle(double oldA, double newA) {
        if (oldA < -360) {
            oldA += 360;
        } else if (oldA > 360) {
            oldA -= 360;
        }
        if (newA < -360) {
            newA += 360;
        } else if (newA > 360) {
            newA -= 360;
        }
        double ang;
        if (newA - oldA <= -180) {
            ang = 360 - (oldA - newA);
        } else if (newA - oldA <= 0) {
            ang = newA - oldA;
        } else if (newA - oldA <= 180) {
            ang = newA - oldA;
        } else {
            ang = (newA - oldA) - 360;
        }
        if (ang < -360) {
            ang += 360;
        }
        if (ang > 360) {
            ang -= 360;
        }
        return ang;
    }

    private double fixDegAngle(double ang) {
        if (ang < 0) {
            ang += 360;
        }
        if (ang > 360) {
            ang -= 360;
        }
        return ang;
    }
}
