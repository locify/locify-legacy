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
package com.locify.client.gui.polish;

import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import de.enough.polish.ui.backgrounds.GradientVerticalBackground;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.locify.client.utils.R;
import com.locify.client.locator.LocationProvider;
import com.locify.client.route.RouteManager;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Logger;
import javax.microedition.lcdui.Font;

/**
 * This class overrides usual Polish background and add GPS status and other icons
 * @author David Vavra
 */
public class TopBarBackground extends GradientVerticalBackground implements LocationEventListener {

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

    public TopBarBackground(int leftColor, int rightColor, int stroke) {
        super(leftColor, rightColor, stroke);
        R.setTopBar(this);
        createImages();
    }

    public TopBarBackground(int leftColor, int rightColor, int stroke, int start, int end, boolean isPercent) {
        super(leftColor, rightColor, stroke, start, end, isPercent);
        R.setTopBar(this);
        createImages();
    }

    private void createImages() {
        try {
            imgGpsAnimation = Image.createImage("/status_animation.png");
            imgGpsNoSignal = Image.createImage("/status_nosignal.png");
            imgGpsConnecting = Image.createImage("/status_connecting.png");
            imgGpsWeak = Image.createImage("/status_weak.png");
            imgGpsNormal = Image.createImage("/status_normal.png");
            imgGpsStrong = Image.createImage("/status_strong.png");
            imgGpsManual = Image.createImage("/manual.png");
            imgHttpConnecting = Image.createImage("/connecting_21x21.png");
            imgHttpAnimation = Image.createImage("/connecting_animation_21x21.png");
            imgHttpSending = Image.createImage("/sending_21x21.png");
            imgHttpReceiving = Image.createImage("/receiving_21x21.png");
            imgHttpReceiving = Image.createImage("/receiving_21x21.png");
            imgRouteRunning = Image.createImage("/state_recording_21x21.png");
            imgRoutePaused = Image.createImage("/state_pause_21x21.png");
            imgRouteUnfinished = Image.createImage("/state_pause_21x21.png");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TopBarBackground.createImages", null);
        }
    }

    public void paint(int x, int y, int width, int height, Graphics g) {
        g.setClip(x, y, width, height);
        this.height = height;
        try {
            super.paint(x, y, width, height, g);
            if ((message != null) && (message.length() > 0)) {
                g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
                g.setColor(ColorsFonts.BLACK);
                g.drawString(message, x + width - 45, y + 2, Graphics.TOP | Graphics.RIGHT);
            }

            if (httpStatus != UNDEFINED && httpImage != null) {
                g.drawImage(httpImage, x + width - 44, y + 1, Graphics.TOP | Graphics.LEFT);
            }
            if (gpsStatus != UNDEFINED && gpsImage != null) {
                g.drawImage(gpsImage, x + width - 22, y + 1, Graphics.TOP | Graphics.LEFT);
            }
            if (routeStatus != RouteManager.ROUTE_STATE_NO_ACTION && routeImage != null) {
                g.drawImage(routeImage, x + width - 66, y + 1, Graphics.TOP | Graphics.LEFT);
            }
            g.setClip(0, 0, R.getMainScreen().getWidth(), R.getMainScreen().getHeight()+2*height+10);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "TopBarBackground.paint", null);
        }
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
        if (R.getContext().getSource() == LocationContext.GPS && R.getLocator().getState()==LocationProvider.READY) {
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