/*
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
import com.locify.client.data.IconData;
import com.locify.client.data.items.GeoData;
import com.locify.client.data.items.GeoFiles;
import com.locify.client.data.items.MultiGeoData;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.data.SettingsData;
import com.locify.client.gui.extension.BackgroundListener;
import com.locify.client.gui.extension.FlowPanel;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.widgets.CompassWidget;
import com.locify.client.gui.widgets.MapWidget;
import com.locify.client.gui.widgets.StateLabelWidget;
import com.locify.client.gui.widgets.Widget;
import com.locify.client.gui.widgets.WidgetContainer;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.locator.Navigator;
import com.locify.client.locator.impl.WaypointNavigatorModel;
import com.locify.client.locator.impl.WaypointRouteNavigatorModel;
import com.locify.client.utils.Backlight;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Button;
import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.layouts.Layout;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.game.GameCanvas;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/** 
 * Screen shows compas, GPS infomation, speed. In case of navigation displays navigation arrow
 * @author Jiri Stepan
 */
public class NavigationScreen extends FormLocify implements
        ActionListener, LocationEventListener, BackgroundListener {

    // components
    private WidgetContainer mainContainer;

    // bindable widgets
    private StateLabelWidget slAlt;
    private StateLabelWidget slDate;
    private StateLabelWidget slDist;
    private StateLabelWidget slHdop;
    private StateLabelWidget slLat;
    private StateLabelWidget slLon;
    private StateLabelWidget slSpeed;
    private StateLabelWidget slTime;
    private StateLabelWidget slVdop;
    private CompassWidget compass;
    // flow panel
    private FlowPanel leftPanel;
    // actual navigator
    private static Navigator navigator;
    private Location4D location;
    private Backlight backLight;
    // skin container
    private Vector skins;

    public NavigationScreen() {
        super(Locale.get("Navigation"));
        try {

            this.addCommand(Commands.cmdBack);
            this.addCommand(Commands.cmdHome);

            if (R.getBacklight().isOn()) {
                this.addCommand(Commands.cmdBacklightOff);
            } else {
                this.addCommand(Commands.cmdBacklightOn);
            }

            // i know about added gps ... actualy i'm lazy :)
            this.addCommand(new ParentCommand(Locale.get("Another_location"), null, R.getContext().commands));
            this.setCommandListener(this);

            setLayout(new BorderLayout());
            this.mainContainer = new WidgetContainer(new BorderLayout());
            addComponent(BorderLayout.CENTER, mainContainer);

            R.getLocator().addLocationChangeListener(this);
            R.getBackgroundRunner().registerBackgroundListener(this, 1);

            skins = new Vector();
            Enumeration skinFolders = R.getFileSystem().getFolders(FileSystem.ROOT + FileSystem.SKINS_FOLDER_NAVIGATION);
            if (skinFolders != null) {
                while (skinFolders.hasMoreElements()) {
                    String dir = (String) skinFolders.nextElement();
//System.out.println("Dir: " + (FileSystem.ROOT + FileSystem.SKINS_FOLDER_NAVIGATION + dir));
                    Vector xmlFiles = R.getFileSystem().listFiles(FileSystem.SKINS_FOLDER_NAVIGATION + dir, new String[] {"*.xml"});
                    if (xmlFiles.size() > 0) {
                        skins.addElement(FileSystem.SKINS_FOLDER_NAVIGATION + dir + xmlFiles.elementAt(0));
                    }
                }
            }

            leftPanel = new FlowPanel(BorderLayout.WEST);
            leftPanel.getContentPane().setLayout(new BoxLayout(BoxLayout.Y_AXIS));
            addComponent(BorderLayout.WEST, leftPanel);

            for (int i = 0; i < skins.size(); i++) {
                final String path = (String) skins.elementAt(i);
//System.out.println("Add: " + path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')));
                Button button = new Button(path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')));
                button.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        setNavigationScreenSkin(path);
                    }
                });
                leftPanel.getContentPane().addComponent(button);
            }

            if (skins.size() > 0) {
                setNavigationScreenSkin((String) skins.elementAt(0));
            }


            
//            labelTime.setTitle(IconData.getLocalImage("alarm"));
//            labelDate.setTitle(IconData.getLocalImage("calendar"));
//            labelDist.setTitle(IconData.getLocalImage("forward"));
//            labelSpeed.setTitle(Locale.get("Speed"));
//            labelHDOP.setTitle(Locale.get("Hdop_route"));
//            labelVDOP.setTitle(Locale.get("Vdop_route"));
//            labelLatitude.setTitle(Locale.get("Latitude"));
//            labelLongitude.setTitle(Locale.get("Longitude"));
//            labelAltitude.setTitle(Locale.get("Altitude"));
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "NavigationScreen.constructor()", null);
        }
    }

    public void view() {
        try {
            //init variables
            location = R.getLocator().getLastLocation();

            locationChanged(null, location);
            if (R.getSettings().getBacklight() == SettingsData.NAVIGATION || R.getSettings().getBacklight() == SettingsData.MAP_NAVIGATION) {
                R.getBacklight().on();
                removeCommand(Commands.cmdBacklightOn);
                addCommand(Commands.cmdBacklightOff);
            }

            revalidate();
            show();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "NavigationScreen.view", null);
        }
    }

    /**
     * Starts navigation using navigator (waypoint, route)
     * @param navigator
     */
    public void view(Navigator navigator) {
        NavigationScreen.navigator = navigator;
        view();
    }

    public void removeNavigator() {
        navigator = null;
        repaint();
    }

    /**
     * Starts navigation to lat, lon
     * @param lat
     * @param lon
     * @param name
     */
    public void view(double lat, double lon, String name) {
        view(new WaypointNavigatorModel(new Location4D(lat, lon, 0), name));
    }

    public void view(String fileName) {
        MultiGeoData mgd = GeoFiles.parseGeoDataFile(fileName, false);
        if (mgd.getDataSize() > 0) {
            view(mgd.getGeoData(0));
        }
    }

    /**
     * Used when ID of waypoint in map is used
     * @param id
     * @param idc
     */
    public void view(String id, boolean idc) {
        if (R.getMapItemManager().getWaypointById(id) != null) {
            navigator = new WaypointNavigatorModel(R.getMapItemManager().getWaypointById(id));
            R.getMapScreen().setDifferentScreenLock(true);
            R.getMapScreen().resumeNetworkLink();
            R.getBack().deleteLast();
            view();
        }
    }

    /**
     * Determines file type and start navigation to it
     * @param data
     */
    public void view(GeoData data) {
        if (data instanceof Waypoint) {
            Waypoint waypoint = (Waypoint) data;
            view(new WaypointNavigatorModel(waypoint));
        } else if (data instanceof WaypointsCloud) {
            WaypointsCloud cloud = (WaypointsCloud) data;
            view(new WaypointNavigatorModel(cloud.getCenterLocation(), cloud.getName()));
        } else if (data instanceof Route) {
            Route route = (Route) data;
            view(new WaypointRouteNavigatorModel(route));
        }
        view();
    }

    public void updateWaypoint(Waypoint waypoint) {
        if (navigator == null) {
            navigator = new WaypointNavigatorModel(waypoint);
        } else if (navigator instanceof WaypointNavigatorModel) {
            ((WaypointNavigatorModel) navigator).updateWaypoint(waypoint);
            locationChanged(null, location);
        }
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
        if (this.isVisible() || sender == null) {
            this.location = location;

            double angleN = R.getLocator().getHeading();
            double angleD = 0;
            if (navigator != null) {
                setTitle(navigator.getToName());
                angleD = navigator.getAzimuthToTaget(location) - angleN;
                if (slDist != null) {
                    slDist.setValue(GpsUtils.formatDistance(navigator.getDistanceToTarget(location)));
                }
            }

            if (compass != null) {
                compass.moveAngles(angleN, angleD);
            }

            if (slAlt != null) {
                slAlt.setValue(GpsUtils.formatDouble(location.getAltitude(), 1) + "m");
            }
            if (slHdop != null) {
                slHdop.setValue(GpsUtils.formatDouble(R.getLocator().getAccuracyHorizontal(), 1));
            }
            if (slLat != null) {
                slLat.setValue(GpsUtils.formatLatitude(location.getLatitude(), R.getSettings().getCoordsFormat()));
            }
            if (slLon != null) {
                slLon.setValue(GpsUtils.formatLongitude(location.getLongitude(), R.getSettings().getCoordsFormat()));
            }
            if (slSpeed != null) {
                slSpeed.setValue(GpsUtils.formatSpeed(R.getLocator().getSpeed()));
            }
            if (slVdop != null) {
                slVdop.setValue(GpsUtils.formatDouble(R.getLocator().getAccuracyVertical(), 1));
            }

//Logger.debug("NS (" + System.currentTimeMillis() + "), lat: " + location.getLatitude() +
//        ", lon: " + location.getLongitude() + ", angleN: " + angleN + ", angleD: " + angleD);
//            repaint();
        }
    }

    private int backgroundTaskCount = 0;

    public void runBackgroundTask() {
        if (slTime != null) {
            slTime.setValue(Utils.getTime());
        }
        if (slDate != null && (backgroundTaskCount % 60 == 0 || slDate.getValue().length() == 0)) {
            slDate.setValue(Utils.getDate());
        }

        backgroundTaskCount++;
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
    }

    public static boolean isRunning() {
        return !(navigator == null);
    }

    public String getWaypointId() {
        if (navigator != null & navigator instanceof WaypointNavigatorModel) {
            return ((WaypointNavigatorModel) navigator).getId();
        }
        return null;
    }

    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        switch (keyCode) {
            case GameCanvas.KEY_NUM7:
                if (R.getBacklight().isOn()) {
                    R.getBacklight().off();
                } else {
                    R.getBacklight().on();
                }
                break;
            case GameCanvas.KEY_NUM4:
                leftPanel.switchVisibility();
                break;
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getBack().goBack();
            if (R.getSettings().getBacklight() != SettingsData.WHOLE_APPLICATION) {
                R.getBacklight().off();
            }
        } else if (evt.getCommand() == Commands.cmdHome) {
            if (R.getContext().isTemporary()) {
                R.getContext().removeTemporaryLocation();
            }
            R.getURL().call("locify://mainScreen");

            if (R.getSettings().getBacklight() != SettingsData.WHOLE_APPLICATION) {
                R.getBacklight().off();
            }
        } else if (evt.getCommand() == Commands.cmdBacklightOn) {
            R.getBacklight().on();
            removeCommand(Commands.cmdBacklightOn);
            addCommand(Commands.cmdBacklightOff);
        } else if (evt.getCommand() == Commands.cmdBacklightOff) {
            R.getBacklight().off();
            removeCommand(Commands.cmdBacklightOff);
            addCommand(Commands.cmdBacklightOn);
        } else {
            for (int i = 0; i < R.getContext().commands.length; i++) {
                if (evt.getCommand() == R.getContext().commands[i]) {
                    R.getContext().setTemporaryScreen("locify://navigation");
                    R.getURL().call(R.getContext().actions[i]);
                    return;
                }
            }
        }
    }

    private void setNavigationScreenSkin(String skinPath) {
        mainContainer.removeAll();

        FileConnection fileConnection = null;
        InputStream is = null;
        XmlPullParser parser;

        String skinDirPath = skinPath.substring(0, skinPath.lastIndexOf('/') + 1);
//System.out.println("\nSkin: " + skinDirPath);
        int STATE_NONE = 0;
        int STATE_WIDGET_STATE_LABEL = 10;
        int STATE_WIDGET_COMPASS = 11;
        int STATE_WIDGET_MAP = 12;
        int actualState = STATE_NONE;

        Container parent = mainContainer;
        Widget widget = null;

        try {
            fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + skinPath);
            if (!fileConnection.exists()) {
                return;
            }

            is = fileConnection.openInputStream();
            parser = new KXmlParser();
            parser.setInput(is, "utf-8");

            int event;
            String tagName;
            String value;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
//                    Logger.debug("  parseKML - tagName: " + tagName);
                    if (tagName.equalsIgnoreCase("bindTo")) {
                        value = parser.nextText();
                        if (value != null) {
                            if (widget instanceof StateLabelWidget) {
                                bindStateLabel((StateLabelWidget) widget, value);
                            } else if (widget instanceof CompassWidget) {
                                bindCompass((CompassWidget) widget, value);
                            }
                        }
                    } else if (tagName.equalsIgnoreCase("labels")) {
                        if (actualState == STATE_WIDGET_COMPASS) {
                            ((CompassWidget) widget).setLabelsFont(getFont(parser.getAttributeValue(null, "fontSize")));
                        }
                    } else if (tagName.equalsIgnoreCase("lcf")) {
                        value = parser.getAttributeValue(null, "layout");
                        if (value == null) {
                            return;
                        } else if (value.equalsIgnoreCase("BorderLayout")) {
                            parent.setLayout(new BorderLayout());
                        }
                    } else if (tagName.equalsIgnoreCase("title")) {
                        if (actualState == STATE_WIDGET_STATE_LABEL) {
                            ((StateLabelWidget) widget).setTitleHAlign(getAlignValue(parser.getAttributeValue(null, "hAlign")));
                            ((StateLabelWidget) widget).setTitleVAlign(getAlignValue(parser.getAttributeValue(null, "vAlign")));
                            ((StateLabelWidget) widget).setTitlePosition(getBorderLayoutPositionValue(parser.getAttributeValue(null, "position"), false));
                            ((StateLabelWidget) widget).setTitleFont(getFont(parser.getAttributeValue(null, "fontSize")));
                            value = parser.nextText();
                            if (value != null && value.startsWith("file:")) {
                                value = skinDirPath + value.substring("file:".length());
                                ((StateLabelWidget) widget).setTitle(IconData.get(value));
                            } else {
                                ((StateLabelWidget) widget).setTitle(value);
                            }
                        }
                    } else if (tagName.equalsIgnoreCase("value")) {
                        if (actualState == STATE_WIDGET_STATE_LABEL) {
                            ((StateLabelWidget) widget).setValueHAlign(getAlignValue(parser.getAttributeValue(null, "hAlign")));
                            ((StateLabelWidget) widget).setValueVAlign(getAlignValue(parser.getAttributeValue(null, "vAlign")));
                            ((StateLabelWidget) widget).setValueFont(getFont(parser.getAttributeValue(null, "fontSize")));
                            value = parser.nextText();
                            if (value != null && value.startsWith("file:")) {
                                value = skinDirPath + value.substring("file:".length());
                                ((StateLabelWidget) widget).setValue(IconData.get(value));
                            } else {
                                ((StateLabelWidget) widget).setValue(value);
                            }
                        }
                    } else if (tagName.equalsIgnoreCase("widget")) {
                        widget = null;
                        value = parser.getAttributeValue(null, "type");
                        if (value.equalsIgnoreCase("Compass")) {
                            widget = new CompassWidget();
                            widget.setWidgetParent(parent);
                            if (parent.getLayout() instanceof BorderLayout) {
                                widget.setConstrains(getBorderLayoutPositionValue(parser.getAttributeValue(null, "position"), true));
                            }
                            actualState = STATE_WIDGET_COMPASS;
                        } else if (value.equalsIgnoreCase("Container")) {
                            widget = new WidgetContainer(getLayout(parser));
                            widget.setWidgetParent(parent);
                            if (parent.getLayout() instanceof BorderLayout) {
                                widget.setConstrains(getBorderLayoutPositionValue(parser.getAttributeValue(null, "position"), true));
                            }
                            actualState = STATE_NONE;
                            parent = widget;
                        } else if (value.equalsIgnoreCase("Map")) {
                            widget = new MapWidget();
                            widget.setWidgetParent(parent);
                            if (parent.getLayout() instanceof BorderLayout) {
                                widget.setConstrains(getBorderLayoutPositionValue(parser.getAttributeValue(null, "position"), true));
                            }
                            actualState = STATE_WIDGET_MAP;
                        } else if (value.equalsIgnoreCase("StateLabel")) {
                            widget = new StateLabelWidget();
                            widget.setWidgetParent(parent);
                            if (parent.getLayout() instanceof BorderLayout) {
                                widget.setConstrains(getBorderLayoutPositionValue(parser.getAttributeValue(null, "position"), true));
                            }
                            actualState = STATE_WIDGET_STATE_LABEL;
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
//                    Logger.debug("  parseKML - tagNameEnd:" + tagName);
                    if (tagName.equalsIgnoreCase("widget")) {
                        widget.addToParent();
                        if (widget instanceof WidgetContainer) {
                            parent = widget.getWidgetParent();
                        }
                        widget = (Widget) widget.getWidgetParent();
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
        } catch (Exception e) {
            //R.getErrorScreen().view(e, "RouteData.isRoute", null);
            Logger.error("NavigationScreen.createNavigationScreen() - file: " + skinPath + " ex: " + e.toString());
            return;
        } finally {
            try {
                if (fileConnection != null) {
                    fileConnection.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            mainContainer.repaint();
        }
    }

    private int getAlignValue(String text) {
        if (text == null || text.equalsIgnoreCase("center")) {
            return Label.CENTER;
        } else if (text.equalsIgnoreCase("right")) {
            return Label.RIGHT;
        } else if (text.equalsIgnoreCase("left")) {
            return Label.LEFT;
        } else if (text.equalsIgnoreCase("top")) {
            return Label.TOP;
        } else if (text.equalsIgnoreCase("bottom")) {
            return Label.BOTTOM;
        } else {
            return Label.CENTER;
        }
    }

    private Layout getLayout(XmlPullParser parser) {
        String text = parser.getAttributeValue(null, "layout");

        if (text == null) {
            return new FlowLayout();
        } else if (text.equalsIgnoreCase("BorderLayout")) {
            return new BorderLayout();
        } else if (text.equalsIgnoreCase("GridLayout")) {
            int rows = GpsUtils.parseInt(parser.getAttributeValue(null, "rows"));
            int columns = GpsUtils.parseInt(parser.getAttributeValue(null, "columns"));
            if (rows == 0) {
                rows = 1;
            }
            if (columns == 0) {
                columns = 1;
            }
            return new GridLayout(rows, columns);
        } else {
            return new FlowLayout();
        }
    }

    private Font getFont(String text) {
        if (text == null || text.length() > 1 || text.equals("1")) {
            return ColorsFonts.FONT_BMF_10;
        } else if (text.equals("2")) {
            return ColorsFonts.FONT_BMF_14;
        } else if (text.equals("3")) {
            return ColorsFonts.FONT_BMF_16;
        } else if (text.equals("4")) {
            return ColorsFonts.FONT_BMF_18;
        } else if (text.equals("5")) {
            return ColorsFonts.FONT_BMF_20;
        }
        return ColorsFonts.FONT_BMF_10;
    }

    private String getBorderLayoutPositionValue(String text, boolean allowCenter) {
        if (text == null) {
            if (allowCenter) {
                return BorderLayout.CENTER;
            } else {
                return BorderLayout.NORTH;
            }
        } else if (text.equalsIgnoreCase("north")) {
            return BorderLayout.NORTH;
        } else if (text.equalsIgnoreCase("south")) {
            return BorderLayout.SOUTH;
        } else if (text.equalsIgnoreCase("west")) {
            return BorderLayout.WEST;
        } else if (text.equalsIgnoreCase("east")) {
            return BorderLayout.EAST;
        } else if (allowCenter) {
            return BorderLayout.CENTER;
        } else {
            return BorderLayout.NORTH;
        }
    }

    private void bindStateLabel(StateLabelWidget widget, String text) {
        if (text.equalsIgnoreCase("alt")) {
            slAlt = widget;
        } else if (text.equalsIgnoreCase("date")) {
            slDate = widget;
            slDate.setValue("");
        } else if (text.equalsIgnoreCase("dist")) {
            slDist = widget;
        } else if (text.equalsIgnoreCase("hdop")) {
            slHdop = widget;
        } else if (text.equalsIgnoreCase("lat")) {
            slLat = widget;
        } else if (text.equalsIgnoreCase("lon")) {
            slLon = widget;
        } else if (text.equalsIgnoreCase("speed")) {
            slSpeed = widget;
        } else if (text.equalsIgnoreCase("time")) {
            slTime = widget;
        } else if (text.equalsIgnoreCase("vdop")) {
            slVdop = widget;
        }
    }

    private void bindCompass(CompassWidget widget, String text) {
        if (text.equalsIgnoreCase("compass")) {
            compass = widget;
        }
    }
}
