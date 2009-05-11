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

import com.locify.client.gui.polish.TopBarBackground;
import com.locify.client.locator.LocationContext;
import com.locify.client.route.*;
import com.locify.client.route.routeStyle.RouteStyleSimple;
import com.locify.client.route.routeStyle.RouteStyleExtended;
import com.locify.client.route.routeStyle.RouteStyle;
import com.locify.client.route.routeStyle.RouteStyleGraph;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.locify.client.utils.Capabilities;
import de.enough.polish.ui.Form;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

/**
 * Screen for showing running route progress
 * @author Menion
 */
public class RouteScreen extends Form implements CommandListener {

    private RouteManager routeManager;
    private Thread thread;
    // index vybrané položky
    protected int selected = 0;

    // prepared item for display
    private ArrayList items;
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
    private Command cmdBack;
    private Command cmdStyle;
    private Command cmdStyleSimple;
    private Command cmdStyleExtended;
    private Command cmdStyleGraphs;
    private Command cmdAction;
    private Command cmdActionStart;
    private Command cmdActionStop;
    private Command cmdActionReset;
    private Command cmdActionPause;
    private Command cmdActionResume;
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

    public RouteScreen() {
        super("");
        routeManager = new RouteManager();
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
                    removeSubCommands();
                    this.addSubCommand(cmdActionResume, cmdAction);
                    this.addSubCommand(cmdActionStop, cmdAction);
                    this.addSubCommand(cmdActionReset, cmdAction);
                }
                alreadyInitialized = true;
            }
            selected = items.size() - 2;
            R.getMidlet().switchDisplayable(null, this);
        }
    }

    private void addMenu() {
        cmdBack = new Command(Locale.get("Back"), Command.BACK, 1);

        cmdStyle = new Command(Locale.get("Style"), Command.SCREEN, 2);
        cmdStyleSimple = new Command(Locale.get("Simple_style"), Command.SCREEN, 2);
        cmdStyleExtended = new Command(Locale.get("Extended_style"), Command.SCREEN, 2);
        cmdStyleGraphs = new Command(Locale.get("Graphs_style"), Command.SCREEN, 2);

        cmdAction = new Command(Locale.get("Record_route"), Command.SCREEN, 2);
        cmdActionStart = new Command(Locale.get("Start_action"), Command.SCREEN, 2);
        cmdActionStop = new Command(Locale.get("Stop_action"), Command.SCREEN, 2);
        cmdActionReset = new Command(Locale.get("Reset_route"), Command.SCREEN, 2);
        cmdActionPause = new Command(Locale.get("Pause_action"), Command.SCREEN, 2);
        cmdActionResume = new Command(Locale.get("Resume_action"), Command.SCREEN, 2);

        this.addCommand(cmdBack);

        this.addCommand(cmdStyle);
        this.addSubCommand(cmdStyleSimple, cmdStyle);
        this.addSubCommand(cmdStyleExtended, cmdStyle);
        this.addSubCommand(cmdStyleGraphs, cmdStyle);

        this.addCommand(cmdAction);
        this.addSubCommand(cmdActionStart, cmdAction);

        this.setCommandListener(this);
    }

    private void initializeMainButtons() {
        int width = (Capabilities.getWidth() - 3 * itemBetweenSpace) / 2;

        // ITEM_BUTTON_START
        buttonStart = new ScreenItem(Locale.get("Start_route"));
        buttonStart.setFont(ColorsFonts.BMF_ARIAL_18_WHITE_BIG_BOLD_LETTERS_ONLY, ColorsFonts.BMF_ARIAL_18_WHITE_BIG_BOLD_LETTERS_ONLY);
        buttonStart.setSelectable(true);
        buttonStart.setSizePos(itemBetweenSpace, Capabilities.getHeight() - BOTTOM_MARGIN,
                width, MAIN_BUTTON_HEIGHT);
        buttonStart.setColors(ColorsFonts.DARK_GRAY, ColorsFonts.LIGHT_GRAY, ColorsFonts.GREEN_SHINY, ColorsFonts.ORANGE);

        // ITEM_BUTTON_STOP
        buttonStop = new ScreenItem(Locale.get("Stop_route"));
        buttonStop.setFont(ColorsFonts.BMF_ARIAL_18_WHITE_BIG_BOLD_LETTERS_ONLY, ColorsFonts.BMF_ARIAL_18_WHITE_BIG_BOLD_LETTERS_ONLY);
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
            items = new ArrayList();

            // ITEM_ROUTE_TIME
            ScreenItem button00 = new ScreenItem(Locale.get("Time"));
            items.add(button00);
            // ITEM_ROUTE_DIST
            ScreenItem button01 = new ScreenItem(Locale.get("Distance"));
            items.add(button01);
            // ITEM_SPEED_MAX
            ScreenItem button02 = new ScreenItem(Locale.get("Max_speed"));
            items.add(button02);
            // ITEM_SPEED_AVERAGE
            ScreenItem button03 = new ScreenItem(Locale.get("Average_speed"));
            items.add(button03);
            // ITEM_SPEED_ACTUAL
            ScreenItem button04 = new ScreenItem(Locale.get("Speed"));
            items.add(button04);
            // ITEM_LATITUDE
            ScreenItem button05 = new ScreenItem(Locale.get("Latitude"));
            items.add(button05);
            // ITEM_LONGITUDE
            ScreenItem button06 = new ScreenItem(Locale.get("Longitude"));
            items.add(button06);
            // ITEM_ALTITUDE
            ScreenItem button07 = new ScreenItem(Locale.get("Altitude"));
            items.add(button07);
            // ITEM_HDOP
            ScreenItem button08 = new ScreenItem(Locale.get("Hdop_route"));
            items.add(button08);
            // ITEM_VDOP
            ScreenItem button09 = new ScreenItem(Locale.get("Vdop_route"));
            items.add(button09);
            // ITEM_GRAPH_ALTITUDE_BY_DIST
            GraphItem button10 = new GraphItem("Altitude / distance", GraphItem.VALUE_X_TOTAL_DIST, GraphItem.VALUE_Y_ALTITUDE, 1000.0);
            button10.setAlignment(Item.ALIGN_RIGHT, Item.ALIGN_TOP);
            items.add(button10);
            // ITEM_GRAPH_ALTITUDE_BY_TIME
            GraphItem button11 = new GraphItem("Altitude / time", GraphItem.VALUE_X_TOTAL_TIME, GraphItem.VALUE_Y_ALTITUDE, 300.0);
            button11.setAlignment(Item.ALIGN_RIGHT, Item.ALIGN_TOP);
            items.add(button11);

            // ITEM_BUTTON_START
            items.add(buttonStart);
            // ITEM_BUTTON_STOP
            items.add(buttonStop);
        }

        /* set variables for all items exept buttons */
        for (int i = 0; i < items.size() - 2; i++) {
            if (items.get(i) instanceof ScreenItem) {
                ((ScreenItem) items.get(i)).setFont(
                        ColorsFonts.BMF_ARIAL_10_BLACK, ColorsFonts.BMF_ARIAL_10_BLACK);
            } else if (items.get(i) instanceof GraphItem) {
                ((GraphItem) items.get(i)).setFont(
                        ColorsFonts.BMF_ARIAL_18_WHITE_BIG_BOLD_LETTERS_ONLY);
            }
        }
    }

    private void actualizeItems() {
        if (this.isShown()) {
            if (actualStyleScreen instanceof RouteStyleGraph) {
                graphItem = (GraphItem) items.get(ITEM_GRAPH_ALTITUDE_BY_DIST);
                graphItem.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION *
                        routeManager.getSpeedAverage());
                graphItem.refreshGraph(routeManager.getRouteVariables());

                graphItem = (GraphItem) items.get(ITEM_GRAPH_ALTITUDE_BY_TIME);
                graphItem.setMeasureX(RouteVariables.MAX_PAD * RouteVariables.SAVED_COUNT_LOCATION);
                graphItem.refreshGraph(routeManager.getRouteVariables());
            } else {
                routeItem = (ScreenItem) items.get(ITEM_ROUTE_TIME);
                routeItem.setTextValue(GpsUtils.formatTimeShort(routeManager.getRouteTime()));

                if (routeManager.isNewData()) {
                    routeItem = (ScreenItem) items.get(ITEM_HDOP);
                    routeItem.setTextValue(GpsUtils.formatDouble(routeManager.getHdop(), 1));

                    routeItem = (ScreenItem) items.get(ITEM_VDOP);
                    routeItem.setTextValue(GpsUtils.formatDouble(routeManager.getVdop(), 1));

                    routeItem = (ScreenItem) items.get(ITEM_LATITUDE);
                    routeItem.setTextValue(
                            GpsUtils.formatLatitude(routeManager.getLatitude(), R.getSettings().getCoordsFormat()));

                    routeItem = (ScreenItem) items.get(ITEM_LONGITUDE);
                    routeItem.setTextValue(
                            GpsUtils.formatLongitude(routeManager.getLongitude(), R.getSettings().getCoordsFormat()));

                    routeItem = (ScreenItem) items.get(ITEM_ALTITUDE);
                    routeItem.setTextValue(GpsUtils.formatDouble(routeManager.getAltitude(), 0) + " m");

                    routeItem = (ScreenItem) items.get(ITEM_SPEED_ACTUAL);
                    routeItem.setTextValue(GpsUtils.formatSpeed(routeManager.getSpeed()));

                    routeItem = (ScreenItem) items.get(ITEM_ROUTE_DIST);
                    routeItem.setTextValue(GpsUtils.formatDistance(routeManager.getRouteDist()));

                    routeItem = (ScreenItem) items.get(ITEM_SPEED_AVERAGE);
                    routeItem.setTextValue(GpsUtils.formatSpeed(routeManager.getSpeedAverage()));

                    routeItem = (ScreenItem) items.get(ITEM_SPEED_MAX);
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

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd.equals(cmdBack)) {
            R.getBack().goBack();
        } else if (cmd.equals(cmdStyleSimple)) {
            setStyle(STYLE_SIMPLE);
        } else if (cmd.equals(cmdStyleExtended)) {
            setStyle(STYLE_EXTENDED);
        } else if (cmd.equals(cmdStyleGraphs)) {
            setStyle(STYLE_GRAPHS);
        } else if (cmd.equals(cmdActionStart)) {
            routeStart();
        } else if (cmd.equals(cmdActionStop)) {
            //routeStop(true);
            routePause(true);
        } else if (cmd.equals(cmdActionReset)) {
            routeReset();
        } else if (cmd.equals(cmdActionPause)) {
            routePause(false);
        } else if (cmd.equals(cmdActionResume)) {
            routeStart();
        }
    }

    /**
     * Called when a key is pressed.
     */
    public void keyPressed(int key) {
        super.keyPressed(key);
        if (!isMenuOpened()) {
            int action = getGameAction(key);
            switch (action) {
                case Canvas.LEFT:
                    setPrevEnabled();
                    repaint();
                    break;
                case Canvas.RIGHT:
                    setNextEnabled();
                    repaint();
                    break;
                case Canvas.FIRE:
                    selectedAction();
                    repaint();
                    break;
            }
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
        if (isMenuOpened()) {
            super.pointerReleased(x, y);
        } else {
            super.pointerReleased(x, y);
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) instanceof ScreenItem &&
                        ((ScreenItem) items.get(i)).isInside(x, y)) {
                    selected = i;
                    selectedAction();
                    repaint();
                    break;
                }
            }
        }
    }

    private void selectedAction() {
        if (selected > 0 && selected < items.size() && items.get(selected) instanceof ScreenItem) {
            if (((ScreenItem) items.get(selected)).equals(buttonStart)) {
                if (routeManager.isRunning()) {
                    routePause(false);
                } else {
                    routeStart();
                }

            } else if (((ScreenItem) items.get(selected)).equals(buttonStop)) {
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
                            if (isShown()) {
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

        // manage lights
        //R.getMidlet().getDisplay().flashBacklight(1);
        //DeviceControl.lightOn();
        //DeviceControl.vibrate(5);

        // menu buttons
        removeSubCommands();
        this.addSubCommand(cmdActionPause, cmdAction);
        this.addSubCommand(cmdActionStop, cmdAction);
        this.addSubCommand(cmdActionReset, cmdAction);
    }

    private void routePause(boolean save) {
        routeManager.routePause(save);
        buttonStart.setTextLabel(Locale.get("Resume_route"));
        buttonStart.setActive(true);
        buttonStop.setActive(true);
        selected = items.size() - 2;

        // menu buttons
        removeSubCommands();
        this.addSubCommand(cmdActionResume, cmdAction);
        this.addSubCommand(cmdActionStop, cmdAction);
        this.addSubCommand(cmdActionReset, cmdAction);
    }

    public void routeReset() {
        routeManager.routeReset();
        actualizeItems();

        //R.getMidlet().getDisplay().flashBacklight(1);

        buttonStart.setTextLabel(Locale.get("Start_route"));
        buttonStart.setActive(true);
        buttonStop.setActive(false);
        selected = items.size() - 2;

        // menu buttons
        removeSubCommands();
        this.addSubCommand(cmdActionStart, cmdAction);
        this.addSubCommand(cmdActionReset, cmdAction);
    }

    private void removeSubCommands() {
        Utils.removeSubCommand(cmdActionStart, cmdAction, this);
        Utils.removeSubCommand(cmdActionPause, cmdAction, this);
        Utils.removeSubCommand(cmdActionResume, cmdAction, this);
        Utils.removeSubCommand(cmdActionStop, cmdAction, this);
        Utils.removeSubCommand(cmdActionReset, cmdAction, this);
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (!this.isMenuOpened()) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) instanceof ScreenItem) {
                    routeItem = (ScreenItem) items.get(i);
                    if (selected == i) {
                        routeItem.setSelected(true);
                    } else {
                        routeItem.setSelected(false);
                    }
                }
            }

            for (int i = 0; i < displayItems.length; i++) {
                if (displayItems[i] != -1) {
                    ((Item) items.get(displayItems[i])).paint(g);
                }
            }

            buttonStart.paint(g);
            buttonStop.paint(g);

        //        g.setColor(0, 0, 0);
        //        if (text01 != null) {
        //            g.drawString(text01, 20, 25, Graphics.TOP | Graphics.LEFT);
        //        }
        }
    //if (this.isMenuOpened())
    //   super.paint(g);
    }

    public Item getItem(int item) {
        if (item < items.size()) {
            return (Item) items.get(item);
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

        if (items.get(selected) instanceof ScreenItem &&
                !((ScreenItem) items.get(selected)).isSelectable()) {
            setNextEnabled();
        } else if (!(items.get(selected) instanceof ScreenItem)) {
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
        if (items.get(selected) instanceof ScreenItem &&
                !((ScreenItem) items.get(selected)).isSelectable()) {
            setPrevEnabled();
        } else if (!(items.get(selected) instanceof ScreenItem)) {
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
}