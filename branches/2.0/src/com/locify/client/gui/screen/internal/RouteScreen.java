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

import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.widgets.StateLabel;
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
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.TabbedPane;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.GridLayout;


/**
 * Screen for showing running route progress
 * @author Menion
 */
public class RouteScreen extends FormLocify implements ActionListener {

    private TabbedPane tabbedPane;
    private Container tabPanel01;
    private Container tabPanel02;
    private Container tabPanel03;
    
    private Container buttonContainer;

    private Button buttonStart;
    private Button buttonStop;

    private StateLabel labelRouteTime;
    private StateLabel labelRouteDist;
    private StateLabel labelSpeedMax;
    private StateLabel labelSpeedAverage;
    private StateLabel labelSpeedActual;
    private StateLabel labelLatitude;
    private StateLabel labelLongitude;
    private StateLabel labelAltitude;
    private StateLabel labelHDOP;
    private StateLabel labelVDOP;

    private GraphItem gi01;
    private GraphItem gi02;

    private RouteManager routeManager;
    private Thread thread;
   
    private boolean alreadyInitialized = false;
    private boolean initializePaused = false;

    private ParentCommand actionCommand;

    public RouteScreen() {
        super(Locale.get("Record_route"));
        setLayout(new BorderLayout());

        routeManager = new RouteManager();
        actionCommand = new ParentCommand(Locale.get("Record_route"), null, new Command[1]);
    }

    /**
     * Views map screen
     */
    public void view() {
        if (R.getContext().getSource() != LocationContext.GPS) {
            R.getURL().call("locify://setLocationGPS");
        } else {
            if (!alreadyInitialized) {
                addMenu();
                initializeItems();
                initializeContainers();

                addComponent(BorderLayout.CENTER, tabbedPane);
                addComponent(BorderLayout.SOUTH, buttonContainer);
                
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);

                //setStyle(STYLE_SIMPLE);
                //setStyle(STYLE_GRAPHS);
                startRouteScreen();

                if (initializePaused) {
                    actualizeItems();
                    buttonStart.setText(Locale.get("Resume_route"));
                    buttonStart.setEnabled(true);
                    buttonStop.setEnabled(true);

                    // menu buttons
                    actionCommand.setCommand(new Command[] {Commands.cmdActionResume, Commands.cmdActionStop, Commands.cmdActionReset});
                }
                showTab(0);
                alreadyInitialized = true;
            }
            show();
        }
    }

    private void addMenu() {
        this.addCommand(Commands.cmdBack);

        this.addCommand(new ParentCommand(Locale.get("Record_route"), null,
                new Command[] {Commands.cmdActionStart}));

        this.setCommandListener(this);
    }

    /**
     * this function initialize RouteScreenItem variables
     * needed for showing them on screen
     * !!! important !!! right order for adding have to be !!!
     */
    private void initializeItems() {
        buttonStart = new Button(Locale.get("Start_route"));
        buttonStart.getStyle().setFont(ColorsFonts.FONT_BMF_20);
//        buttonStart.getStyle().setBgColor(ColorsFonts.DARK_GRAY);
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
//        buttonStop.getStyle().setBgColor(ColorsFonts.DARK_GRAY);
        buttonStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                 routePause(true);
            }
        });
        
        labelAltitude = new StateLabel(BorderLayout.NORTH);
        labelAltitude.setTitle(Locale.get("Altitude"));
        labelAltitude.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelHDOP = new StateLabel(BorderLayout.NORTH);
        labelHDOP.setTitle(Locale.get("Hdop_route"));
        labelHDOP.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelLatitude = new StateLabel(BorderLayout.NORTH);
        labelLatitude.setTitle(Locale.get("Latitude"));
        labelLatitude.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelLongitude = new StateLabel(BorderLayout.NORTH);
        labelLongitude.setTitle(Locale.get("Longitude"));
        labelLongitude.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelRouteDist = new StateLabel(BorderLayout.NORTH);
        labelRouteDist.setTitle(Locale.get("Distance"));
        labelRouteDist.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelRouteTime = new StateLabel(BorderLayout.NORTH);
        labelRouteTime.setTitle(Locale.get("Time"));
        labelRouteTime.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelSpeedActual = new StateLabel(BorderLayout.NORTH);
        labelSpeedActual.setTitle(Locale.get("Speed"));
        labelSpeedActual.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelSpeedAverage = new StateLabel(BorderLayout.NORTH);
        labelSpeedAverage.setTitle(Locale.get("Average_speed"));
        labelSpeedAverage.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelSpeedMax = new StateLabel(BorderLayout.NORTH);
        labelSpeedMax.setTitle(Locale.get("Max_speed"));
        labelSpeedMax.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        labelVDOP = new StateLabel(BorderLayout.NORTH);
        labelVDOP.setTitle(Locale.get("Vdop_route"));
        labelVDOP.setFonts(ColorsFonts.FONT_PLAIN_SMALL, ColorsFonts.FONT_PLAIN_SMALL);

        gi01 = new GraphItem("Altitude / distance", GraphItem.VALUE_X_TOTAL_DIST, GraphItem.VALUE_Y_ALTITUDE, 1000.0);

        gi02 = new GraphItem("Altitude / time", GraphItem.VALUE_X_TOTAL_TIME, GraphItem.VALUE_Y_ALTITUDE, 300.0);
    }

    private void initializeContainers() {
        tabbedPane = new TabbedPane();
        tabbedPane.addTabsListener(new SelectionListener() {

            public void selectionChanged(int oldSelected, int newSelected) {
                showTab(newSelected);
            }
        });

        tabPanel01 = new Container(new GridLayout(2, 2));
        tabbedPane.addTab("Simple", tabPanel01);

        tabPanel02 = new Container(new GridLayout(4, 3));
        tabbedPane.addTab("Advanced", tabPanel02);

        tabPanel03 = new Container(new GridLayout(2, 1));
        tabbedPane.addTab("Graphs", tabPanel03);

        buttonContainer = new Container(new GridLayout(1, 2));
        buttonContainer.addComponent(buttonStart);
        buttonContainer.addComponent(buttonStop);
    }

    private void clearTabs() {
        tabPanel01.removeAll();
        tabPanel02.removeAll();
        tabPanel03.removeAll();
    }

    private void showTab(int index) {
        clearTabs();
        switch (index) {
            case 0:
                tabPanel01.addComponent(labelRouteDist);
                tabPanel01.addComponent(labelRouteTime);
                tabPanel01.addComponent(labelSpeedActual);
                tabPanel01.addComponent(labelSpeedAverage);
                break;
            case 1:
                tabPanel02.addComponent(labelSpeedActual);
                tabPanel02.addComponent(labelSpeedAverage);
                tabPanel02.addComponent(labelSpeedMax);
                tabPanel02.addComponent(labelRouteDist);
                tabPanel02.addComponent(labelRouteTime);
                tabPanel02.addComponent(new Label());
                tabPanel02.addComponent(labelLatitude);
                tabPanel02.addComponent(labelLongitude);
                tabPanel02.addComponent(labelAltitude);
                tabPanel02.addComponent(labelHDOP);
                tabPanel02.addComponent(labelVDOP);
                break;
            case 2:
                tabPanel03.addComponent(gi01);
                tabPanel03.addComponent(gi02);
                break;
        }
    }

    private void actualizeItems() {
        labelRouteTime.setValue(GpsUtils.formatTimeShort(routeManager.getRouteTime()));

        if (routeManager.isNewData()) {
            labelAltitude.setValue(GpsUtils.formatDouble(routeManager.getAltitude(), 0) + " m");
            labelHDOP.setValue(GpsUtils.formatDouble(routeManager.getHdop(), 1));
            labelLatitude.setValue(
                    GpsUtils.formatLatitude(routeManager.getLatitude(), R.getSettings().getCoordsFormat()));
            labelLongitude.setValue(
                    GpsUtils.formatLongitude(routeManager.getLongitude(), R.getSettings().getCoordsFormat()));
            labelRouteDist.setValue(GpsUtils.formatDistance(routeManager.getRouteDist()));
            labelSpeedActual.setValue(GpsUtils.formatSpeed(routeManager.getSpeed()));
            labelSpeedAverage.setValue(GpsUtils.formatSpeed(routeManager.getSpeedAverage()));
            labelSpeedMax.setValue(GpsUtils.formatSpeed(routeManager.getSpeedMax()));
            labelVDOP.setValue(GpsUtils.formatDouble(routeManager.getVdop(), 1));

            gi01.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION *
                    routeManager.getSpeedAverage());
            gi01.refreshGraph(routeManager.getRouteVariables());
            gi01.repaint();

            gi02.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION);
            gi02.refreshGraph(routeManager.getRouteVariables());
            gi02.repaint();

            routeManager.setNewData(false);
        }
    }


    /**
     * Called when a key is pressed.
     */
    public void keyPressed(int key) {
        super.keyPressed(key);
        int action = Display.getInstance().getGameAction(key);
        switch (action) {
            case Display.GAME_LEFT:
                if (tabbedPane.getSelectedIndex() > 0)
                    tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() - 1);
                else
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount());
                break;
            case Display.GAME_RIGHT:
                if (tabbedPane.getSelectedIndex() < tabbedPane.getTabCount())
                    tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() + 1);
                else
                    tabbedPane.setSelectedIndex(0);
                break;
            case Display.GAME_FIRE:
                break;
        }
    }
//
//    /**
//     * Called when a key is released.
//     */
//    public void keyReleased(int keyCode) {
//        super.keyReleased(keyCode);
//    }
//
//    /**
//     * Called when a key is repeated (held down).
//     */
//    public void keyRepeated(int keyCode) {
//        super.keyRepeated(keyCode);
//    }
//
//    /**
//     * Called when the pointer is dragged.
//     */
//    public void pointerDragged(int x, int y) {
//        super.pointerDragged(x, y);
//    }
//
//    /**
//     * Called when the pointer is pressed.
//     */
//    public void pointerPressed(int x, int y) {
//        super.pointerPressed(x, y);
//    }
//
//    /**
//     * Called when the pointer is released.
//     */
//    public void pointerReleased(int x, int y) {
//        super.pointerReleased(x, y);
//        for (int i = 0; i < items.size(); i++) {
//            if (items.elementAt(i) instanceof ScreenItem &&
//                    ((ScreenItem) items.elementAt(i)).isInside(x, y)) {
//                selected = i;
//                selectedAction();
//                repaint();
//                break;
//            }
//        }
//    }

    private void startRouteScreen() {
        if (thread == null) {
            thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        while (true) {
                            if (isVisible()) {
                                actualizeItems();
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        R.getErrorScreen().view(e, "RouteScreen.run()", null);
                    }
                }
            });
        }

        if (!thread.isAlive()) {
            thread.start();
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

//    public void paint(Graphics g) {
//        super.paint(g);
//        for (int i = 0; i < items.size(); i++) {
//            if (items.elementAt(i) instanceof ScreenItem) {
//                routeItem = (ScreenItem) items.elementAt(i);
//                if (selected == i) {
//                    routeItem.setSelected(true);
//                } else {
//                    routeItem.setSelected(false);
//                }
//            }
//        }
//
//        for (int i = 0; i < displayItems.length; i++) {
//            if (displayItems[i] != -1) {
//                ((Item) items.elementAt(displayItems[i])).paint(g);
//            }
//        }
//
//        buttonStart.paint(g);
//        buttonStop.paint(g);
//    }
//
//    public Item getItem(int item) {
//        if (item < items.size()) {
//            return (Item) items.elementAt(item);
//        } else {
//            return null;
//        }
//    }
//
//    private void setNextEnabled() {
//        if (selected < items.size() - 1) {
//            selected += 1;
//        } else {
//            selected = 0;
//        }
//
//        if (items.elementAt(selected) instanceof ScreenItem &&
//                !((ScreenItem) items.elementAt(selected)).isSelectable()) {
//            setNextEnabled();
//        } else if (!(items.elementAt(selected) instanceof ScreenItem)) {
//            setNextEnabled();
//        }
//    }
//
//    private void setPrevEnabled() {
//        if (selected > 0) {
//            selected -= 1;
//        } else {
//            selected = items.size() - 1;
//        }
//        //text01 = String.valueOf(selected);
//        if (items.elementAt(selected) instanceof ScreenItem &&
//                !((ScreenItem) items.elementAt(selected)).isSelectable()) {
//            setPrevEnabled();
//        } else if (!(items.elementAt(selected) instanceof ScreenItem)) {
//            setPrevEnabled();
//        }
//    }

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