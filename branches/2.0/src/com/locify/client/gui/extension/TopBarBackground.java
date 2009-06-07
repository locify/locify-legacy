 /*
 * TopBarBackground.java
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
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.utils.R;
import com.locify.client.locator.LocationProvider;
import com.locify.client.route.RouteManager;
import com.locify.client.utils.ColorsFonts;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Painter;
import com.sun.lwuit.geom.Rectangle;

/**
 * This class overrides usual Polish background and add GPS status and other icons
 * @author David Vavra
 */
public class TopBarBackground implements Painter, LocationEventListener {

    public static final int UNDEFINED = 0;
    public static final int MANUAL = 1;
    public static final int CONNECTING = 2;
    public static final int NOSIGNAL = 3;
    public static final int WEAK = 4;
    public static final int NORMAL = 5;
    public static final int STRONG = 6;
    public static final int ANIMATION = 7;
    public static final int SENDING = 8;
    public static final int RECEIVING = 9;
    private static int gpsStatus = UNDEFINED;
    private static int preanimationStatus = UNDEFINED;
    private static Image gpsImage = null;
    private static boolean animatingHttp = false;
    private static int httpStatus = UNDEFINED;
    private static Image httpImage = null;
    private static int routeStatus = RouteManager.ROUTE_STATE_NO_ACTION;
    private static Image routeImage = null;
    private static String message = ""; //messages about progres etc
    public int height; //height of progress bar. It is used in other screens to determine its height (MapScreen, NavigationScreen)
    public int width;
    private static Image imgGpsAnimation;
    private static Image imgGpsNoSignal;
    private static Image imgGpsConnecting;
    private static Image imgGpsWeak;
    private static Image imgGpsNormal;
    private static Image imgGpsStrong;
    private static Image imgGpsManual;
    private static Image imgHttpConnecting;
    private static Image imgHttpAnimation;
    private static Image imgHttpSending;
    private static Image imgHttpReceiving;
    // route images
    private static Image imgRouteRunning;
    private static Image imgRoutePaused;
    private static Image imgRouteUnfinished;
    
    private static int routeImageRepeats;

    private int backGroundColor;

    public TopBarBackground() {
        R.setTopBar(this);
        backGroundColor = R.getMainScreen().getTitleStyle().getBgColor();
        createImages();

        this.height = 0;
        this.width = 0;
    }

    public void paint(Graphics g, Rectangle rect) {
        g.setColor(backGroundColor);
        g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());

        g.setColor(ColorsFonts.BLACK);
        if (height == 0) {
            height = rect.getSize().getHeight();
            width = rect.getSize().getWidth();
        }

        try {
            if ((message != null) && (message.length() > 0)) {
                g.setFont(ColorsFonts.FONT_PLAIN_SMALL);
                g.setColor(ColorsFonts.BLACK);
                //g.drawString(message, x + width - 45, y + 2, Graphics.TOP | Graphics.RIGHT);
                g.drawString(message, width - 45, 2);
            }

            if (httpStatus != UNDEFINED && httpImage != null) {
                g.drawImage(httpImage, width - 44, 1);
            }
            if (gpsStatus != UNDEFINED && gpsImage != null) {
                g.drawImage(gpsImage, width - 22, 1);
            }
            if (routeStatus != RouteManager.ROUTE_STATE_NO_ACTION && routeImage != null) {
                g.drawImage(routeImage, width - 66, 1);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TopBarBackground.paint", null);
        }
    }

    private void createImages() {
        try {
            imgGpsAnimation = IconData.getLocalImage("status_animation.png");
            imgGpsNoSignal = IconData.getLocalImage("status_nosignal.png");
            imgGpsConnecting = IconData.getLocalImage("status_connecting.png");
            imgGpsWeak = IconData.getLocalImage("status_weak.png");
            imgGpsNormal = IconData.getLocalImage("status_normal.png");
            imgGpsStrong = IconData.getLocalImage("status_strong.png");
            imgGpsManual = IconData.getLocalImage("manual.png");
            imgHttpConnecting = IconData.getLocalImage("connecting_21x21.png");
            imgHttpAnimation = IconData.getLocalImage("connecting_animation_21x21.png");
            imgHttpSending = IconData.getLocalImage("sending_21x21.png");
            imgHttpReceiving = IconData.getLocalImage("receiving_21x21.png");
            imgHttpReceiving = IconData.getLocalImage("receiving_21x21.png");
            imgRouteRunning = IconData.getLocalImage("state_recording_21x21.png");
            imgRoutePaused = IconData.getLocalImage("state_pause_21x21.png");
            imgRouteUnfinished = IconData.getLocalImage("state_pause_21x21.png");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TopBarBackground.createImages", null);
        }
    }

    public static Image getImgGpsNoSignal() {
        return imgGpsNoSignal;
    }

    public static Image getImgGpsNormal() {
        return imgGpsNormal;
    }

    public static Image getImgGpsStrong() {
        return imgGpsStrong;
    }

    public static Image getImgGpsWeak() {
        return imgGpsWeak;
    }

    /**
     * Decides which image should be painted in next paint()
     * @return if animation should continue
     */
    public boolean animate() {
        try {
            if (gpsStatus == CONNECTING || gpsStatus == NOSIGNAL) {
                preanimationStatus = gpsStatus;
                gpsStatus = ANIMATION;
                gpsImage = imgGpsAnimation;
            } else if (gpsStatus == ANIMATION) {
                gpsStatus = preanimationStatus;
                if (gpsStatus == CONNECTING) {
                    gpsImage = imgGpsConnecting;
                } else if (gpsStatus == NOSIGNAL) {
                    gpsImage = imgGpsNoSignal;
                }
            }

            switch (httpStatus) {
                case CONNECTING:
                    if (animatingHttp) {
                        httpImage = imgHttpConnecting;
                        animatingHttp = false;
                    } else {
                        httpImage = imgHttpAnimation;
                        animatingHttp = true;
                    }
                    break;
                case RECEIVING:
                    httpImage = imgHttpReceiving;
                    break;
                case SENDING:
                    httpImage = imgHttpSending;
                    break;
            }

            if (routeStatus == RouteManager.ROUTE_STATE_RUNNING) {
                if (routeImage == imgRouteRunning) {
                    if (routeImageRepeats >= 4) {
                        routeImage = null;
                        routeImageRepeats = 0;
                    } else {
                        routeImageRepeats++;
                    }
                } else {
                    routeImage = imgRouteRunning;
                }
            }
            return true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TopBarBackground.animate", null);
            return false;
        }
    }

    /**
     * Sets status on top bar
     * @param newStatus new status
     */
    public static void setGpsStatus(int newStatus) {
        try {
            if (gpsStatus != newStatus) {
                gpsStatus = newStatus;
                switch (gpsStatus) {
                    case UNDEFINED:
                        gpsImage = null;
                        break;

                    case MANUAL:
                        gpsImage = imgGpsManual;
                        break;

                    case CONNECTING:
                        gpsImage = imgGpsConnecting;
                        break;

                    case NOSIGNAL:
                        gpsImage = imgGpsNoSignal;
                        break;

                    case WEAK:
                        gpsImage = imgGpsWeak;
                        break;

                    case NORMAL:
                        gpsImage = imgGpsNormal;
                        break;

                    case STRONG:
                        gpsImage = imgGpsStrong;
                        break;
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TopBarBackground.setGpsStatus", String.valueOf(newStatus));
        }
    }

    /**
     * Sets new http status
     * @param status new http status
     */
    public static void setHttpStatus(int status) {
        httpStatus = status;
    }

    public static void setRouteStatus(int status) {
        routeStatus = status;
        switch (routeStatus) {
            case RouteManager.ROUTE_STATE_NO_ACTION:
                routeImage = null;
                break;

            case RouteManager.ROUTE_STATE_RUNNING:
                routeImage = imgRouteRunning;
                break;

            case RouteManager.ROUTE_STATE_PAUSED:
                routeImage = imgRoutePaused;
                break;

            case RouteManager.ROUTE_STATE_UNFINISHED_ROUTE:
                routeImage = imgRouteUnfinished;
                break;
        }
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        if (R.getContext().getSource() == LocationContext.GPS && R.getLocator().getState() == LocationProvider.READY) {
            float accuracy = R.getLocator().getAccuracyHorizontal();
            if (accuracy < 15) {
                setGpsStatus(TopBarBackground.STRONG);
            } else if (accuracy < 50) {
                setGpsStatus(TopBarBackground.NORMAL);
            } else {
                setGpsStatus(TopBarBackground.WEAK);
            }
        }
    }

    public static void setMessage(String s) {
        message = s;
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
        if (state == LocationProvider.STATE_UNKNOWN) {
            setGpsStatus(TopBarBackground.UNDEFINED);
        } else if (state == LocationProvider.WAITING) {
            setGpsStatus(TopBarBackground.NOSIGNAL);
        } else if (state == LocationProvider.CONNECTING) {
            setGpsStatus(TopBarBackground.CONNECTING);
        } else if (state == LocationProvider.READY) {
            float accuracy = R.getLocator().getAccuracyHorizontal();
            if (accuracy < 15) {
                setGpsStatus(TopBarBackground.STRONG);
            } else if (accuracy < 50) {
                setGpsStatus(TopBarBackground.NORMAL);
            } else {
                setGpsStatus(TopBarBackground.WEAK);
            }
        } else if (state == LocationProvider.MANUAL) {
            setGpsStatus(TopBarBackground.MANUAL);
        }
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
    }
}