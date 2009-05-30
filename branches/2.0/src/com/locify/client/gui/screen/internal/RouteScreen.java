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

import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.TopBarBackground;
import com.locify.client.locator.LocationContext;
import com.locify.client.route.*;
import com.locify.client.route.routeStyle.RouteStyleSimple;
import com.locify.client.route.routeStyle.RouteStyleExtended;
import com.locify.client.route.routeStyle.RouteStyle;
import com.locify.client.route.routeStyle.RouteStyleGraph;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import java.util.Vector;


/**
 * Screen for showing running route progress
 * @author Menion
 */
public class RouteScreen extends Form implements ActionListener {

    private RouteManager routeManager;
    private Thread thread;
    // index vybrané položky
    protected int selected = 0;

    // prepared item for display
    private Vector items;
    public static final int ITEM_ROUTE_TIME = 0;
    public static final int ITEM_ROUTE_DIST = 1;
    public static final int ITEM_SPEED_MAX = 2;
    public static final int ITEM_SPEED_AVERAGE = 3;
    public static final int ITEM_SPEED_ACTUAL = 4;
    public static final int ITEM_LATITUDE = 5;
    public static final int ITEM_LONGITUDE = 6;
    public static final int ITEM_ALTITUDE = 7;
    public static final int ITEM_HDOP = 8;
    public static final int ITEM_VDOP = 9;
    public static final int ITEM_GRAPH_ALTITUDE_BY_DIST = 10;
    public static final int ITEM_GRAPH_ALTITUDE_BY_TIME = 11;
    private ScreenItem buttonStart,  buttonStop;
    // actual set items to display
    public static int[] displayItems;
    private ScreenItem routeItem;
    private GraphItem graphItem;
    public static final int STYLE_SIMPLE = 0;
    public static final int STYLE_EXTENDED = 1;
    public static final int STYLE_GRAPHS = 2;
    private RouteStyle actualStyleScreen;

    // size of items
    // value increased for bottom buttons (start, stop, etc .., def 22)
    private static int MAIN_BUTTON_HEIGHT = 30;
    public static int BOTTOM_MARGIN;
    // huge value due to date painting (def 22)
    public static int TOP_MARGIN;
    public static int itemWidth;
    public static int itemWidthCount;
    public static int itemHeight;
    public static int itemHeightCount;
    public static int itemBetweenSpace = 5;
    private boolean alreadyInitialized = false;
    private boolean initializePaused = false;

    private ParentCommand actionCommand;

    public RouteScreen() {
        super("");
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
                this.setTitle(Locale.get("Record_route"));
                RouteScreen.TOP_MARGIN = R.getTopBar().height + 2;// + 20;
                RouteScreen.BOTTOM_MARGIN = R.getTopBar().height + MAIN_BUTTON_HEIGHT + 10;

                addMenu();
                initializeMainButtons();
                initializeItems();
                buttonStart.setActive(true);
                buttonStop.setActive(false);

                setStyle(STYLE_SIMPLE);
                //setStyle(STYLE_GRAPHS);
                startRouteScreen();

                if (initializePaused) {
                    actualizeItems();
                    buttonStart.setTextLabel(Locale.get("Resume_route"));
                    buttonStart.setActive(true);
                    buttonStop.setActive(true);

                    // menu buttons
                    actionCommand.setCommand(new Command[] {Commands.cmdActionResume, Commands.cmdActionStop, Commands.cmdActionReset});
                }
                alreadyInitialized = true;
            }
            selected = items.size() - 2;
            this.show();
        }
    }

    private void addMenu() {
        this.addCommand(Commands.cmdBack);

        this.addCommand(new ParentCommand(Locale.get("Style"), null, new Command[] {
            Commands.cmdStyleSimple, Commands.cmdStyleExtended, Commands.cmdStyleGraphs}));

        this.addCommand(new ParentCommand(Locale.get("Record_route"), null,
                new Command[] {Commands.cmdActionStart}));

        this.setCommandListener(this);
    }

    private void initializeMainButtons() {
        int width = (Capabilities.getWidth() - 3 * itemBetweenSpace) / 2;

        // ITEM_BUTTON_START
        buttonStart = new ScreenItem(Locale.get("Start_route"));
        buttonStart.setFont(ColorsFonts.FONT_BMF_20, ColorsFonts.FONT_BMF_20);
        buttonStart.setSelectable(true);
        buttonStart.setSizePos(itemBetweenSpace, Capabilities.getHeight() - BOTTOM_MARGIN,
                width, MAIN_BUTTON_HEIGHT);
        buttonStart.setColors(ColorsFonts.DARK_GRAY, ColorsFonts.LIGHT_GRAY, ColorsFonts.GREEN_SHINY, ColorsFonts.ORANGE);

        // ITEM_BUTTON_STOP
        buttonStop = new ScreenItem(Locale.get("Stop_route"));
        buttonStop.setFont(ColorsFonts.FONT_BMF_20, ColorsFonts.FONT_BMF_20);
        buttonStop.setSelectable(true);
        buttonStop.setSizePos(itemBetweenSpace * 2 + width, Capabilities.getHeight() - BOTTOM_MARGIN,
                width, MAIN_BUTTON_HEIGHT);
        buttonStop.setColors(ColorsFonts.DARK_GRAY, ColorsFonts.LIGHT_GRAY, ColorsFonts.GREEN_SHINY, ColorsFonts.ORANGE);

    }

    /**
     * this function initialize RouteScreenItem variables
     * needed for showing them on screen
     * !!! important !!! right order for adding have to be !!!
     */
    private void initializeItems() {
        if (items == null) {
            items = new Vector();

            // ITEM_ROUTE_TIME
            ScreenItem button00 = new ScreenItem(Locale.get("Time"));
            items.addElement(button00);
            // ITEM_ROUTE_DIST
            ScreenItem button01 = new ScreenItem(Locale.get("Distance"));
            items.addElement(button01);
            // ITEM_SPEED_MAX
            ScreenItem button02 = new ScreenItem(Locale.get("Max_speed"));
            items.addElement(button02);
            // ITEM_SPEED_AVERAGE
            ScreenItem button03 = new ScreenItem(Locale.get("Average_speed"));
            items.addElement(button03);
            // ITEM_SPEED_ACTUAL
            ScreenItem button04 = new ScreenItem(Locale.get("Speed"));
            items.addElement(button04);
            // ITEM_LATITUDE
            ScreenItem button05 = new ScreenItem(Locale.get("Latitude"));
            items.addElement(button05);
            // ITEM_LONGITUDE
            ScreenItem button06 = new ScreenItem(Locale.get("Longitude"));
            items.addElement(button06);
            // ITEM_ALTITUDE
            ScreenItem button07 = new ScreenItem(Locale.get("Altitude"));
            items.addElement(button07);
            // ITEM_HDOP
            ScreenItem button08 = new ScreenItem(Locale.get("Hdop_route"));
            items.addElement(button08);
            // ITEM_VDOP
            ScreenItem button09 = new ScreenItem(Locale.get("Vdop_route"));
            items.addElement(button09);
            // ITEM_GRAPH_ALTITUDE_BY_DIST
            GraphItem button10 = new GraphItem("Altitude / distance", GraphItem.VALUE_X_TOTAL_DIST, GraphItem.VALUE_Y_ALTITUDE, 1000.0);
            button10.setAlignment(Item.ALIGN_RIGHT, Item.ALIGN_TOP);
            items.addElement(button10);
            // ITEM_GRAPH_ALTITUDE_BY_TIME
            GraphItem button11 = new GraphItem("Altitude / time", GraphItem.VALUE_X_TOTAL_TIME, GraphItem.VALUE_Y_ALTITUDE, 300.0);
            button11.setAlignment(Item.ALIGN_RIGHT, Item.ALIGN_TOP);
            items.addElement(button11);

            // ITEM_BUTTON_START
            items.addElement(buttonStart);
            // ITEM_BUTTON_STOP
            items.addElement(buttonStop);
        }

        /* set variables for all items exept buttons */
        for (int i = 0; i < items.size() - 2; i++) {
            if (items.elementAt(i) instanceof ScreenItem) {
                ((ScreenItem) items.elementAt(i)).setFont(
                        ColorsFonts.FONT_BMF_10, ColorsFonts.FONT_BMF_10);
            } else if (items.elementAt(i) instanceof GraphItem) {
                ((GraphItem) items.elementAt(i)).setFont(
                        ColorsFonts.FONT_BMF_20);
            }
        }
    }

    private void actualizeItems() {
        if (this.isVisible()) {
            if (actualStyleScreen instanceof RouteStyleGraph) {
                graphItem = (GraphItem) items.elementAt(ITEM_GRAPH_ALTITUDE_BY_DIST);
                graphItem.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION *
                        routeManager.getSpeedAverage());
                graphItem.refreshGraph(routeManager.getRouteVariables());

                graphItem = (GraphItem) items.elementAt(ITEM_GRAPH_ALTITUDE_BY_TIME);
                graphItem.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION);
                graphItem.refreshGraph(routeManager.getRouteVariables());
            } else {
                routeItem = (ScreenItem) items.elementAt(ITEM_ROUTE_TIME);
                routeItem.setTextValue(GpsUtils.formatTimeShort(routeManager.getRouteTime()));

                if (routeManager.isNewData()) {
                    routeItem = (ScreenItem) items.elementAt(ITEM_HDOP);
                    routeItem.setTextValue(GpsUtils.formatDouble(routeManager.getHdop(), 1));

                    routeItem = (ScreenItem) items.elementAt(ITEM_VDOP);
                    routeItem.setTextValue(GpsUtils.formatDouble(routeManager.getVdop(), 1));

                    routeItem = (ScreenItem) items.elementAt(ITEM_LATITUDE);
                    routeItem.setTextValue(
                            GpsUtils.formatLatitude(routeManager.getLatitude(), R.getSettings().getCoordsFormat()));

                    routeItem = (ScreenItem) items.elementAt(ITEM_LONGITUDE);
                    routeItem.setTextValue(
                            GpsUtils.formatLongitude(routeManager.getLongitude(), R.getSettings().getCoordsFormat()));

                    routeItem = (ScreenItem) items.elementAt(ITEM_ALTITUDE);
                    routeItem.setTextValue(GpsUtils.formatDouble(routeManager.getAltitude(), 0) + " m");

                    routeItem = (ScreenItem) items.elementAt(ITEM_SPEED_ACTUAL);
                    routeItem.setTextValue(GpsUtils.formatSpeed(routeManager.getSpeed()));

                    routeItem = (ScreenItem) items.elementAt(ITEM_ROUTE_DIST);
                    routeItem.setTextValue(GpsUtils.formatDistance(routeManager.getRouteDist()));

                    routeItem = (ScreenItem) items.elementAt(ITEM_SPEED_AVERAGE);
                    routeItem.setTextValue(GpsUtils.formatSpeed(routeManager.getSpeedAverage()));

                    routeItem = (ScreenItem) items.elementAt(ITEM_SPEED_MAX);
                    routeItem.setTextValue(GpsUtils.formatSpeed(routeManager.getSpeedMax()));

                    routeManager.setNewData(false);
                }
            }
            repaint();
        }
    }

    private void calculateBySize(int minItemWidth, int minItemHeight) {
        itemWidthCount = Capabilities.getWidth() / (minItemWidth + itemBetweenSpace);
        itemWidth = (Capabilities.getWidth() - (itemWidthCount + 1) * itemBetweenSpace) / itemWidthCount;

        itemHeightCount = (Capabilities.getHeight() - BOTTOM_MARGIN - TOP_MARGIN) / (minItemHeight + itemBetweenSpace);
        itemHeight = ((Capabilities.getHeight() - BOTTOM_MARGIN - TOP_MARGIN) - (itemHeightCount + 1) *
                itemBetweenSpace) / itemHeightCount;
    }

    private void calculateByCount(int widthCount, int heightCount) {
        itemWidthCount = widthCount;
        itemWidth = (Capabilities.getWidth() - (itemWidthCount + 1) * itemBetweenSpace) / itemWidthCount;

        itemHeightCount = heightCount;
        itemHeight = ((Capabilities.getHeight() - BOTTOM_MARGIN - TOP_MARGIN) - (itemHeightCount + 1) *
                itemBetweenSpace) / itemHeightCount;
    }

    private void setStyle(int RouteStyle) {
        initializeItems();
        if (RouteStyle == STYLE_SIMPLE) {
            calculateByCount(2, 3);
            while (itemHeight < 40 && itemHeightCount > 1) {
                calculateByCount(2, itemHeightCount - 1);
            }
            this.actualStyleScreen = new RouteStyleSimple(this);
        //this.setTitle(Locale.get("Route_screen"));
        } else if (RouteStyle == STYLE_EXTENDED) {
            calculateByCount(3, 4);
            while (itemHeight < 40 && itemHeightCount > 1) {
                calculateByCount(3, itemHeightCount - 1);
            }
            this.actualStyleScreen = new RouteStyleExtended(this);
        //this.setTitle(Locale.get("Route_screen"));
        } else if (RouteStyle == STYLE_GRAPHS) {
            calculateByCount(1, 2);
            while (itemHeight < 80 && itemHeightCount > 1) {
                calculateByCount(1, itemHeightCount - 1);
            }
            this.actualStyleScreen = new RouteStyleGraph(this);
        //this.setTitle(Locale.get("Route_screen"));
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
                setPrevEnabled();
                repaint();
                break;
            case Display.GAME_RIGHT:
                setNextEnabled();
                repaint();
                break;
            case Display.GAME_FIRE:
                selectedAction();
                repaint();
                break;
        }
    }

    /**
     * Called when a key is released.
     */
    public void keyReleased(int keyCode) {
        super.keyReleased(keyCode);
    }

    /**
     * Called when a key is repeated (held down).
     */
    public void keyRepeated(int keyCode) {
        super.keyRepeated(keyCode);
    }

    /**
     * Called when the pointer is dragged.
     */
    public void pointerDragged(int x, int y) {
        super.pointerDragged(x, y);
    }

    /**
     * Called when the pointer is pressed.
     */
    public void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);
    }

    /**
     * Called when the pointer is released.
     */
    public void pointerReleased(int x, int y) {
        super.pointerReleased(x, y);
        for (int i = 0; i < items.size(); i++) {
            if (items.elementAt(i) instanceof ScreenItem &&
                    ((ScreenItem) items.elementAt(i)).isInside(x, y)) {
                selected = i;
                selectedAction();
                repaint();
                break;
            }
        }
    }

    private void selectedAction() {
        if (selected > 0 && selected < items.size() && items.elementAt(selected) instanceof ScreenItem) {
            if (((ScreenItem) items.elementAt(selected)).equals(buttonStart)) {
                if (routeManager.isRunning()) {
                    routePause(false);
                } else {
                    routeStart();
                }

            } else if (((ScreenItem) items.elementAt(selected)).equals(buttonStop)) {
                //routeStop(true);
                routePause(true);
            }
        }
    }

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
        buttonStart.setTextLabel(Locale.get("Pause_route"));
        buttonStart.setActive(true);
        buttonStop.setActive(true);
        selected = items.size() - 2;

        // menu buttons
        actionCommand.setCommand(new Command[] {Commands.cmdActionPause, Commands.cmdActionStop, Commands.cmdActionReset});
    }

    private void routePause(boolean save) {
        routeManager.routePause(save);
        buttonStart.setTextLabel(Locale.get("Resume_route"));
        buttonStart.setActive(true);
        buttonStop.setActive(true);
        selected = items.size() - 2;

        // menu buttons
        actionCommand.setCommand(new Command[] {Commands.cmdActionResume, Commands.cmdActionStop, Commands.cmdActionReset});
    }

    public void routeReset() {
        routeManager.routeReset();
        actualizeItems();

        buttonStart.setTextLabel(Locale.get("Start_route"));
        buttonStart.setActive(true);
        buttonStop.setActive(false);
        selected = items.size() - 2;

        // menu buttons
        actionCommand.setCommand(new Command[] {Commands.cmdActionStart, Commands.cmdActionReset});
    }

    public void paint(Graphics g) {
        super.paint(g);
        for (int i = 0; i < items.size(); i++) {
            if (items.elementAt(i) instanceof ScreenItem) {
                routeItem = (ScreenItem) items.elementAt(i);
                if (selected == i) {
                    routeItem.setSelected(true);
                } else {
                    routeItem.setSelected(false);
                }
            }
        }

        for (int i = 0; i < displayItems.length; i++) {
            if (displayItems[i] != -1) {
                ((Item) items.elementAt(displayItems[i])).paint(g);
            }
        }

        buttonStart.paint(g);
        buttonStop.paint(g);
    }

    public Item getItem(int item) {
        if (item < items.size()) {
            return (Item) items.elementAt(item);
        } else {
            return null;
        }
    }

    private void setNextEnabled() {
        if (selected < items.size() - 1) {
            selected += 1;
        } else {
            selected = 0;
        }

        if (items.elementAt(selected) instanceof ScreenItem &&
                !((ScreenItem) items.elementAt(selected)).isSelectable()) {
            setNextEnabled();
        } else if (!(items.elementAt(selected) instanceof ScreenItem)) {
            setNextEnabled();
        }
    }

    private void setPrevEnabled() {
        if (selected > 0) {
            selected -= 1;
        } else {
            selected = items.size() - 1;
        }
        //text01 = String.valueOf(selected);
        if (items.elementAt(selected) instanceof ScreenItem &&
                !((ScreenItem) items.elementAt(selected)).isSelectable()) {
            setPrevEnabled();
        } else if (!(items.elementAt(selected) instanceof ScreenItem)) {
            setPrevEnabled();
        }
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
        } else if (evt.getCommand() == Commands.cmdStyleSimple) {
            setStyle(STYLE_SIMPLE);
        } else if (evt.getCommand() == Commands.cmdStyleExtended) {
            setStyle(STYLE_EXTENDED);
        } else if (evt.getCommand() == Commands.cmdStyleGraphs) {
            setStyle(STYLE_GRAPHS);
        } else if (evt.getCommand() == Commands.cmdActionStart) {
            routeStart();
        } else if (evt.getCommand() == Commands.cmdActionStop) {
            //routeStop(true);
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