/*
 * RouteScreen.java
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

import com.locify.client.data.FileSystem;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.TopBarBackground;
import com.locify.client.locator.LocationContext;
import com.locify.client.route.*;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Locale;
import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.GridLayout;
import javax.microedition.lcdui.game.GameCanvas;


/**
 * Screen for showing running route progress
 * @author Menion
 */
public class RouteScreen extends FormLocify implements ActionListener {

    private Button buttonStart;
    private Button buttonStop;

    private RouteManager routeManager;
   
    private boolean alreadyInitialized = false;
    private boolean initializePaused = false;

    private ParentCommand actionCommand;

    public RouteScreen() {
        super(Locale.get("Record_route"));
    }

    public void view() {
        if (R.getContext().getSource() != LocationContext.GPS) {
            R.getURL().call("locify://setLocationGPS");
        } else {
            if (!alreadyInitialized) {
                setLayout(new BorderLayout());

                this.addCommand(Commands.cmdBack);
                this.addCommand(new ParentCommand(Locale.get("Record_route"), null,
                        new Command[] {Commands.cmdActionStart}));
                actionCommand = new ParentCommand(Locale.get("Record_route"), null, new Command[1]);
                this.setCommandListener(this);

                registerBackgroundListener();

                routeManager = new RouteManager();

                initializeSkins(FileSystem.SKINS_FOLDER_ROUTE_RECORD);
                initializeButtons();

                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);

                if (initializePaused) {
                    actualizeItems();
                    buttonStart.setText(Locale.get("Resume_route"));
                    buttonStart.setEnabled(true);
                    buttonStop.setEnabled(true);

                    // menu buttons
                    actionCommand.setCommand(new Command[] {Commands.cmdActionResume, Commands.cmdActionStop, Commands.cmdActionReset});
                }
                alreadyInitialized = true;
            }
            show();
        }
    }

    private void initializeButtons() {
        buttonStart = new Button(Locale.get("Start_route"));
        buttonStart.getStyle().setFont(ColorsFonts.FONT_BMF_20);
        buttonStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (routeManager.isRunning()) {
                    routePause(false);
                } else {
                    routeStart();
                }
            }
        });

        buttonStop = new Button(Locale.get("Stop_route"));
        buttonStop.getStyle().setFont(ColorsFonts.FONT_BMF_20);
        buttonStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                 routePause(true);
            }
        });

        Container buttonContainer = new Container(new GridLayout(1, 2));
        buttonContainer.addComponent(buttonStart);
        buttonContainer.addComponent(buttonStop);

        addComponent(BorderLayout.SOUTH, buttonContainer);
        
//        slAlt.setTitle(Locale.get("Altitude"));
//        slHdop.setTitle(Locale.get("Hdop_route"));
//        slLat.setTitle(Locale.get("Latitude"));
//        slLon.setTitle(Locale.get("Longitude"));
//        slRouteDist.setTitle(Locale.get("Distance"));
//        slRouteTime.setTitle(Locale.get("Time"));
//        slSpeedAct.setTitle(Locale.get("Speed"));
//        slSpeedAvg.setTitle(Locale.get("Average_speed"));
//        slSpeedMax.setTitle(Locale.get("Max_speed"));
//        slVdop.setTitle(Locale.get("Vdop_route"));
//        gi01 = new GraphItemWidget("Altitude / distance", GraphItemWidget.VALUE_X_TOTAL_DIST, GraphItemWidget.VALUE_Y_ALTITUDE, 1000.0);
//        gi02 = new GraphItemWidget("Altitude / time", GraphItemWidget.VALUE_X_TOTAL_TIME, GraphItemWidget.VALUE_Y_ALTITUDE, 300.0);
    }

    public void runBackgroundTask() {
        super.runBackgroundTask();
        if (isVisible()) {
            actualizeItems();
        }
    }
    
    private void actualizeItems() {
        if (slAlt != null)
            slAlt.setValue(GpsUtils.formatDouble(routeManager.getAltitude(), 0) + " m");
        if (slHdop!= null)
            slHdop.setValue(GpsUtils.formatDouble(routeManager.getHdop(), 1));
        if (slLat != null)
            slLat.setValue(GpsUtils.formatLatitude(routeManager.getLatitude(), R.getSettings().getCoordsFormat()));
        if (slLon != null)
            slLon.setValue(GpsUtils.formatLongitude(routeManager.getLongitude(), R.getSettings().getCoordsFormat()));
        if (slRouteTime != null)
            slRouteTime.setValue(GpsUtils.formatTimeShort(routeManager.getRouteTime()));
        if (slSpeedAct != null)
            slSpeedAct.setValue(GpsUtils.formatSpeed(routeManager.getSpeed()));
        if (slVdop != null)
            slVdop.setValue(GpsUtils.formatDouble(routeManager.getVdop(), 1));

        if (routeManager.isNewData()) {
            if (slDist != null)
                slDist.setValue(GpsUtils.formatDistance(routeManager.getRouteDist()));
            if (slSpeedAvg != null)
                slSpeedAvg.setValue(GpsUtils.formatSpeed(routeManager.getSpeedAverage()));
            if (slSpeedMax != null)
                slSpeedMax.setValue(GpsUtils.formatSpeed(routeManager.getSpeedMax()));
            if (gi01 != null) {
                gi01.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION * routeManager.getSpeedAverage());
                gi01.refreshGraph(routeManager.getRouteVariables());
                gi01.repaint();
            }
            if (gi02 != null) {
                gi02.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION);
                gi02.refreshGraph(routeManager.getRouteVariables());
                gi02.repaint();
            }

            routeManager.setNewData(false);
        }
    }

    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        switch (keyCode) {
            case GameCanvas.KEY_NUM1:
                switchLeftPanelVisibility();
                break;
            case GameCanvas.KEY_NUM5:
                widgetAction(selectedWidget);
                break;
            case GameCanvas.KEY_NUM4:
                selectPreviousWidget();
                repaint();
                break;
            case GameCanvas.KEY_NUM6:
                selectNextWidget();
                repaint();
                break;
            case GameCanvas.KEY_NUM7:
                if (R.getBacklight().isOn()) {
                    R.getBacklight().off();
                } else {
                    R.getBacklight().on();
                }
                break;
        }
    }

    private void routeStart() {
        routeManager.routeStart();
        // visibe buttons
        buttonStart.setText(Locale.get("Pause_route"));
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(true);

        // menu buttons
        actionCommand.setCommand(new Command[] {Commands.cmdActionPause, Commands.cmdActionStop, Commands.cmdActionReset});
    }

    private void routePause(boolean save) {
        routeManager.routePause(save);
        buttonStart.setText(Locale.get("Resume_route"));
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(true);

        // menu buttons
        actionCommand.setCommand(new Command[] {Commands.cmdActionResume, Commands.cmdActionStop, Commands.cmdActionReset});
    }

    public void routeReset() {
        routeManager.routeReset();
        actualizeItems();

        buttonStart.setText(Locale.get("Start_route"));
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);

        // menu buttons
        actionCommand.setCommand(new Command[] {Commands.cmdActionStart, Commands.cmdActionReset});
    }

    public void loadUnfinishedRoute() {
        try {
            if (routeManager.loadUnfinishedRoute()) {
                initializePaused = true;
                TopBarBackground.setRouteStatus(RouteManager.ROUTE_STATE_PAUSED);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "RouteScreen.loadUnfinishedRoute", null);
        }
    }

    public void saveUnfinishedRoute() {
        try {
            if (routeManager != null) {
                if (routeManager.getState() == RouteManager.ROUTE_STATE_RUNNING) {
                    routePause(false);
                }

                if (routeManager.getState() == RouteManager.ROUTE_STATE_PAUSED) {
                    routeManager.saveUnfinishedRoute();
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "RouteScreen.saveUnfinishedRoute", null);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdActionStart) {
            routeStart();
        } else if (evt.getCommand() == Commands.cmdActionStop) {
            routePause(true);
        } else if (evt.getCommand() == Commands.cmdActionReset) {
            routeReset();
        } else if (evt.getCommand() == Commands.cmdActionPause) {
            routePause(false);
        } else if (evt.getCommand() == Commands.cmdActionResume) {
            routeStart();
        }
    }
}