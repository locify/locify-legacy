/*
 * FormLocify.java
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

import com.locify.client.data.FileSystem;
import com.locify.client.data.IconData;
import com.locify.client.gui.widgets.BatteryWidget;
import com.locify.client.gui.widgets.CompassWidget;
import com.locify.client.gui.widgets.GraphItemWidget;
import com.locify.client.gui.widgets.MapWidget;
import com.locify.client.gui.widgets.SatelliteWidget;
import com.locify.client.gui.widgets.StateLabelWidget;
import com.locify.client.gui.widgets.Widget;
import com.locify.client.gui.widgets.WidgetContainer;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Font;
import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.animations.Animation;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.layouts.Layout;
import com.sun.lwuit.list.DefaultListCellRenderer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.game.GameCanvas;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 *
 * @author menion
 */
public class FormLocify extends Form implements BackgroundListener {

    private Dialog menuDialog;
    private ParentCommand selection;

    private static boolean fullScreen = false;
    private static int heightTitle = 0;
    private static int heightMenuBar = 0;
    
    public FormLocify() {
        this("");
    }

    public FormLocify(String title) {
        super(title);

        try {
            // set tile bar
            getTitleComponent().getStyle().setBgTransparency(200);
            getTitleComponent().getStyle().setBgPainter(R.getTopBar());
            getTitleComponent().setAlignment(Component.LEFT);
            getTitleComponent().setShiftText(20);

            // add subMenu support
            setMenuTransitions(null, null);
            setMenuCellRenderer(new DefaultListCellRenderer() {
                private boolean isSubMenu;

                public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
                    isSubMenu = value != null && value instanceof ParentCommand;
                    return super.getListCellRendererComponent(list, value, index, isSelected);
                }

                public void paintBackground(Graphics g) {
                    super.paintBackground(g);

                    if (isSubMenu) {
                        int oldColor = g.getColor();
                        if (hasFocus()) {
                            g.setColor(getSelectedStyle().getFgColor());
                        } else {
                            g.setColor(getStyle().getFgColor());
                        }
                        int leftPoint = getX() + getWidth() - 10;
                        int rightPoint = getX() + getWidth() - 2;
                        int centerPoint = getY() + (getHeight() / 2);
                        int topPoint = centerPoint - 4;
                        int bottomPoint = centerPoint + 4;
                        g.drawLine(leftPoint, topPoint, rightPoint, centerPoint);
                        g.drawLine(leftPoint, bottomPoint, rightPoint, centerPoint);
                        g.drawLine(leftPoint, topPoint, leftPoint, bottomPoint);
                        g.setColor(oldColor);
                    }
                }
            });

            switchFullscreenMode(fullScreen);
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "FormLocify()", null);
        }
    }

    public void setAsNew(String title) {
        removeAll();
        removeAllCommands();
        setTitle(title);
    }

    protected List createCommandList(Vector commands) {
        List commandList = super.createCommandList(commands);
        SelectionMonitor s = new SelectionMonitor(commandList);
        commandList.addSelectionListener(s);
        commandList.addActionListener(s);
        return commandList;
    }

    protected Command showMenuDialog(Dialog menu) {
        menuDialog = menu;
        Command c = super.showMenuDialog(menu);
        menuDialog = null;
        return c;
    }

    public void show() {
Utils.printMemoryState("FormLocify.show()");
        super.show();
System.out.println("01 " + heightTitle);
        if (heightTitle == 0) {
System.out.println("02");
            heightTitle = this.getTitleComponent().getHeight();
System.out.println("03");
            heightMenuBar = getComponentAt(this.getComponentCount() - 1).getHeight();
System.out.println("set: " + heightTitle + ", " + heightMenuBar);
System.out.println("04");
        }
System.out.println("05");
    }

    public void switchFullscreenMode(boolean fullScreen) {
        if (heightTitle == 0)
            return;
        
//System.out.println("setFS: " + heightTitle + ", " + heightMenuBar + ", " + fullScreen);
        FormLocify.fullScreen = fullScreen;
        if (fullScreen) {
            this.getTitleComponent().setPreferredH(0);
            this.getComponentAt(this.getComponentCount() - 1).setPreferredH(10);
        } else {
            this.getTitleComponent().setPreferredH(heightTitle);
            this.getComponentAt(this.getComponentCount() - 1).setPreferredH(heightMenuBar);
        }
        revalidate();
    }

    public void keyPressed(int code) {
        if (code == GameCanvas.KEY_NUM5) {
            switchFullscreenMode(!fullScreen);
        } else {
            super.keyPressed(code);
        }
    }

    /*******************************/
    /*      SKINNING SECTION       */
    /*******************************/
    
    public BatteryWidget battery;
    public CompassWidget compass;
    public GraphItemWidget gi01;
    public GraphItemWidget gi02;
    public SatelliteWidget satellite;
    public StateLabelWidget slAlt;
    public StateLabelWidget slDate;
    public StateLabelWidget slDist;
    public StateLabelWidget slHdop;
    public StateLabelWidget slLat;
    public StateLabelWidget slLon;
    public StateLabelWidget slRouteTime;
    public StateLabelWidget slSpeedAct;
    public StateLabelWidget slSpeedAvg;
    public StateLabelWidget slSpeedMax;
    public StateLabelWidget slTime;
    public StateLabelWidget slVdop;
    
    public Widget selectedWidget;
    
    private Vector widgetList;
    private Vector skins;
    private FlowPanel leftPanel;
    private boolean firstSystemSkin;

    private int STATE_NONE = 0;
    private int STATE_WIDGET_STATE_LABEL = 10;
    private int STATE_WIDGET_COMPASS = 11;
    private int STATE_WIDGET_MAP = 12;
    private int STATE_WIDGET_BATTERY = 13;
    private int STATE_WIDGET_SATELLITE = 14;

    public void initializeSkins(String defaultSystemSkin, String skinDirPath) {
        try {
            widgetList = new Vector();
            skins = new Vector();

            if (defaultSystemSkin != null) {
                skins.addElement(defaultSystemSkin);
                firstSystemSkin = true;
            } else {
                firstSystemSkin = false;
            }

//            Enumeration skinFolders = R.getFileSystem().getFolders(FileSystem.ROOT + skinDirPath);
//            if (skinFolders != null) {
//                while (skinFolders.hasMoreElements()) {
//                    String dir = (String) skinFolders.nextElement();
////System.out.println("Dir: " + (FileSystem.ROOT + FileSystem.SKINS_FOLDER_NAVIGATION + dir));
//                    Vector xmlFiles = R.getFileSystem().listFiles(skinDirPath + dir, new String[] {"*.xml"});
//                    if (xmlFiles.size() > 0) {
//                        skins.addElement(skinDirPath + dir + xmlFiles.elementAt(0));
//                    }
//                }
//            }

            if (skins.size() > 1) {
                leftPanel = new FlowPanel(BorderLayout.WEST);
                leftPanel.getContentPane().setLayout(new BoxLayout(BoxLayout.Y_AXIS));

                ((Container) super.getComponentAt(0).getParent()).addComponent(BorderLayout.WEST, leftPanel);

                for (int i = 0; i < skins.size(); i++) {
                    final String path = (String) skins.elementAt(i);
    //System.out.println("Add: " + path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')));
                    Button button = new Button(path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')));
                    button.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {
                            setScreenSkin(path);
                        }
                    });
                    leftPanel.getContentPane().addComponent(button);
                }
            }

            if (skins.size() > 0) {
                setScreenSkin((String) skins.elementAt(0));
            }
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "FormLocify.initializeSkins", null);
        }
    }

    public void switchLeftPanelVisibility() {
        if (leftPanel != null) {
            leftPanel.switchVisibility();
        }
    }

    public Vector getWidgetList() {
        return widgetList;
    }

    private void setScreenSkin(String skinPath) {
        removeAll();
        widgetList.removeAllElements();

        FileConnection fileConnection = null;
        InputStream is = null;
        XmlPullParser parser;

        String skinDirPath = skinPath.substring(0, skinPath.lastIndexOf('/') + 1);
//System.out.println("\nSkin: " + skinDirPath);
        int actualState = STATE_NONE;

        //Container parent = getContentPane();
        Container parent = new WidgetContainer();
        
        Widget widget = null;

        try {
            if (!firstSystemSkin) {
                fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + skinPath);
                if (!fileConnection.exists()) {
                    return;
                }

                is = fileConnection.openInputStream();
            } else {
//System.out.println("\nGetSkin: '" + skinPath + "'");
                is = FormLocify.class.getResourceAsStream("/" + skinPath);
//System.out.println("1: " + (is == null));
//                StringBuffer data = new StringBuffer();
//                int info;
//                while((info = is.read()) != -1) {
//                    data.append(info);
//                }
//System.out.println("Data: " + data.toString());
            }

            parser = new KXmlParser();
            parser.setInput(is, "utf-8");

            int event;
            String tagName;
            String value;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
//Logger.debug("  parseKML - tagName: " + tagName);
                    if (tagName.equalsIgnoreCase("bindTo")) {
                        value = parser.nextText();
                        if (value != null) {
                            bindWidget(widget, value);
                        }
                    } else if (tagName.equalsIgnoreCase("labels")) {
                        if (actualState == STATE_WIDGET_COMPASS) {
                            ((CompassWidget) widget).setLabelsFont(getFont(parser.getAttributeValue(null, "fontSize")));
                        }
                    } else if (tagName.equalsIgnoreCase("layout")) {
                        parent.setLayout(getLayout(parser));
                    } else if (tagName.equalsIgnoreCase("lcf")) {

                    } else if (tagName.equalsIgnoreCase("linkTo")) {
                        value = parser.nextText();
                        if (actualState != STATE_NONE) {
                            widget.setFocusable(true);
                            widget.setLinkTo(value);
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
                        if (value.equalsIgnoreCase("Battery")) {
                            widget = new BatteryWidget();
                            widget.setWidgetParent(parent, parser);
                            battery = (BatteryWidget) widget;
                            actualState = STATE_WIDGET_BATTERY;
                        } else if (value.equalsIgnoreCase("Compass")) {
                            widget = new CompassWidget();
                            widget.setWidgetParent(parent, parser);
                            actualState = STATE_WIDGET_COMPASS;
                        } else if (value.equalsIgnoreCase("Container")) {
                            widget = new WidgetContainer();
                            widget.setWidgetParent(parent, parser);
                            actualState = STATE_NONE;
                            parent = widget;
                        } else if (value.equalsIgnoreCase("Graph")) {
                            
                        } else if (value.equalsIgnoreCase("Map")) {
                            widget = new MapWidget();
                            widget.setWidgetParent(parent, parser);
                            actualState = STATE_WIDGET_MAP;
                        } else if (value.equalsIgnoreCase("Satellite")) {
                            widget = new SatelliteWidget();
                            widget.setWidgetParent(parent, parser);
                            satellite = (SatelliteWidget) widget;
                            actualState = STATE_WIDGET_SATELLITE;
                        } else if (value.equalsIgnoreCase("StateLabel")) {
                            widget = new StateLabelWidget();
                            widget.setWidgetParent(parent, parser);
                            actualState = STATE_WIDGET_STATE_LABEL;
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
//Logger.debug("  parseKML - tagNameEnd:" + tagName);
                    if (tagName.equalsIgnoreCase("widget")) {
                        widget.addToParent();
                        if (widget instanceof WidgetContainer) {
                            parent = widget.getWidgetParent();
                        } else if (widget.getLinkTo() != null && widget.getLinkTo().length() > 0) {
                            widgetList.addElement(widget);
                        }
                        widget = (Widget) widget.getWidgetParent();
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
            getContentPane().setLayout(new BorderLayout());
            getContentPane().addComponent(BorderLayout.CENTER, parent);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FormLocify.setScreenSkin", skinPath);
//            Logger.error("NavigationScreen.createNavigationScreen() - file: " + skinPath + " ex: " + e.toString());
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

            getContentPane().repaint();
        }
    }

    public static int getAlignValue(String text) {
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

    public static Layout getLayout(XmlPullParser parser) {
        String text = parser.getAttributeValue(null, "type");

        if (text == null) {
            return new BoxLayout(BoxLayout.Y_AXIS);
        } else if (text.equalsIgnoreCase("BorderLayout")) {
            return new BorderLayout();
        } else if (text.equalsIgnoreCase("BoxLayout")) {
            String arrange = parser.getAttributeValue(null, "arrange");
            if (arrange != null && arrange.equalsIgnoreCase("horizontal"))
                return new BoxLayout(BoxLayout.X_AXIS);
            else
                return new BoxLayout(BoxLayout.Y_AXIS);
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
            return new BoxLayout(BoxLayout.Y_AXIS);
        }
    }

    public static Font getFont(String text) {
        if (text == null || text.length() > 1 || text.equals("1")) {
            return ColorsFonts.FONT_SMALL;
        } else if (text.equals("2")) {
            return ColorsFonts.FONT_SMALL;
        } else if (text.equals("3")) {
            return ColorsFonts.FONT_MEDIUM;
        } else if (text.equals("4")) {
            return ColorsFonts.FONT_LARGE;
        } else if (text.equals("5")) {
            return ColorsFonts.FONT_LARGE;
        }
        return ColorsFonts.FONT_MEDIUM;
    }

    public static String getBorderLayoutPositionValue(String text, boolean allowCenter) {
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

    public void bindWidget(Widget widget, String text) {
        if (widget instanceof CompassWidget) {
            if (text.equalsIgnoreCase("compass")) {
                compass = (CompassWidget) widget;
            }
        } else if (widget instanceof GraphItemWidget) {

        } else if (widget instanceof StateLabelWidget) {
            if (text.equalsIgnoreCase("alt")) {
                slAlt = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("date")) {
                slDate = (StateLabelWidget) widget;
                slDate.setValue("");
            } else if (text.equalsIgnoreCase("dist")) {
                slDist = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("hdop")) {
                slHdop = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("lat")) {
                slLat = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("lon")) {
                slLon = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("routeTime")) {
                slRouteTime = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("speedAct")) {
                slSpeedAct = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("speedAvg")) {
                slSpeedAvg = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("speedMax")) {
                slSpeedMax = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("time")) {
                slTime = (StateLabelWidget) widget;
            } else if (text.equalsIgnoreCase("vdop")) {
                slVdop = (StateLabelWidget) widget;
            }
        }
    }

    private int backgroundTaskCount = 0;

    public void runBackgroundTask() {
        if (isVisible()) {
            if (battery != null && (backgroundTaskCount % 300 == 0)) {
                battery.repaint();
            }
            if (satellite != null && (backgroundTaskCount % 60 == 0)) {
                satellite.repaint();
            }
            if (slDate != null && (backgroundTaskCount % 60 == 0 || slDate.getValue().length() == 0)) {
                slDate.setValue(Utils.getDate());
            }
            if (slTime != null) {
                slTime.setValue(Utils.getTime());
            }
        }
        backgroundTaskCount++;
    }

    public void registerBackgroundListener() {
        R.getBackgroundRunner().registerBackgroundListener(this, 1);
    }

    public void selectNextWidget() {
        if (getWidgetList().size() > 0) {
            int index = -1;
            if (selectedWidget != null) {
                index = getWidgetList().indexOf(selectedWidget);
                selectedWidget.showFocused(false);
                selectedWidget.repaint();
            }
            if (index == -1 || (index == getWidgetList().size() - 1))
                index = 0;
            else
                index += 1;
            selectedWidget = (Widget) getWidgetList().elementAt(index);
            selectedWidget.showFocused(true);
            selectedWidget.repaint();
        }
    }

    public void selectPreviousWidget() {
        if (getWidgetList().size() > 0) {
            int index = -1;
            if (selectedWidget != null) {
                index = getWidgetList().indexOf(selectedWidget);
                selectedWidget.showFocused(false);
            }
            if (index == -1)
                index = 0;
            else if (index == 0)
                index = getWidgetList().size() - 1;
            else
                index -= 1;
            selectedWidget = (Widget) getWidgetList().elementAt(index);
            selectedWidget.showFocused(true);
        }
    }

    public void pointerPressed(int x, int y) {
        selectedWidget = null;
        Component comp = getContentPane().getComponentAt(x, y);
        if (comp == null) {

        } else if (comp instanceof Widget) {
            selectedWidget = (Widget) comp;
        } else if (comp.getParent() != null && comp.getParent() instanceof Widget) {
            selectedWidget = (Widget) comp.getParent();
        }

        if (selectedWidget == null)
            super.pointerPressed(x, y);
        else
            widgetAction(selectedWidget);
    }

    public void widgetAction(Widget widget) {
        if  (widget != null && widget.getLinkTo() != null && widget.getLinkTo().length() > 0) {
            R.getURL().call(widget.getLinkTo());
        }
    }

    class SelectionMonitor implements SelectionListener, Animation, ActionListener {

        private List commandList;
        private long selectTime;
        private static final int SUBMENU_POPUP_DELAY = 10000;

        public SelectionMonitor(List commandList) {
            this.commandList = commandList;
        }

        public boolean animate() {
            // we use the animate method as a timer, gauging the time that passed since a selection was made
            if (selection == null) {
                menuDialog.deregisterAnimated(this);
            } else {
                if (System.currentTimeMillis() - selectTime >= SUBMENU_POPUP_DELAY) {
                    menuDialog.deregisterAnimated(this);
                    showSubmenu();
                }
            }

            return false;
        }

        public void selectionChanged(int oldSelected, int newSelected) {
            Object o = commandList.getSelectedItem();
            if (o != null && o instanceof ParentCommand) {
                // cause the animation of the parent form to be invoked
                menuDialog.registerAnimated(this);
                selection = (ParentCommand) o;
                selectTime = System.currentTimeMillis();
            } else {
                selection = null;
            }
        }

        public void paint(Graphics g) {
        }

        public void actionPerformed(ActionEvent evt) {
//System.out.println((selection == null) + " " + commandList.size());
            menuDialog.deregisterAnimated(this);
            if (selection != null) {
                showSubmenu();
            }
        }

        private void showSubmenu() {
            final Dialog subMenu = new Dialog();
            subMenu.getStyle().setBgTransparency(150);
            final List content = new List(selection.getChildren());
//            content.getStyle().setBgTransparency(255);
            content.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    subMenu.dispose();
                    Command c = (Command) content.getSelectedItem();
                    ActionEvent e = new ActionEvent(c);
                    c.actionPerformed(e);
                    actionCommand(c);
                }
            });
            subMenu.setLayout(new BorderLayout());
            subMenu.addComponent(BorderLayout.CENTER, content);
            Command select = new Command("Select") {

                public void actionPerformed(ActionEvent evt) {
                    Command c = (Command) content.getSelectedItem();
                    ActionEvent e = new ActionEvent(c);
                    c.actionPerformed(e);
                    actionCommand(c);
                }
            };
            final Dialog oldMenuDialog = menuDialog;
            Command cancel = new Command("Cancel") {

                public void actionPerformed(ActionEvent evt) {
                    oldMenuDialog.show();
                }
            };
            subMenu.setDialogStyle(menuDialog.getDialogStyle());
            subMenu.addCommand(cancel);
            subMenu.addCommand(select);
            //subMenu.show(getHeight() / 2 - 20, 20, getWidth() / 4, 20, true, true);
            //subMenu.show(getHeight() / 2 - 20, 0, getWidth() / 4, 0, true, true);
            subMenu.show(getHeight() / 2 - 20, 10, 0, getWidth() / 4, true, true);
        }
    }
}
