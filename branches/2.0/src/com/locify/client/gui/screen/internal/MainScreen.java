/**
 * MainScreen.java
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

import com.locify.client.gui.extension.MainScreenItem;
import com.locify.client.data.FileSystem;
import com.locify.client.data.IconData;
import com.locify.client.data.Service;
import com.locify.client.data.ServicesData;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ListLocify;
import com.locify.client.gui.screen.service.ConfirmScreen;
import com.locify.client.net.Http;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.TabbedPane;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.BorderLayout;
import java.io.ByteArrayInputStream;
import java.util.Vector;
import org.kxml2.io.KXmlParser;

/**
 * Main screen of application with all services, tab menus and context menus
 * @author Destil
 */
public class MainScreen extends FormLocify implements ActionListener, SelectionListener {

    private Vector items;
    private MainScreenItem itemToBeMoved = null;
    private TabbedPane tabbedPane;
    private ListLocify cgServices;
    private ListLocify cgWhere;
    private ListLocify cgSaved;
    private ListLocify cgMaps;
    private ListLocify cgNavigation;
    private ListLocify cgMore;
    private boolean autoInstallRequest = false;
    private Vector autoInstallUrls;

    // how to check it? hmmm ... only idea throught user interaction
    public static boolean hasPointerEvents = Display.getInstance().isTouchScreenDevice();

    /**
     * Contructs a new <code>MainScreen</code> object.
     */
    public MainScreen() {
        super();
        setLayout(new BorderLayout());
        tabbedPane = new TabbedPane();
        tabbedPane.setTabbedPaneBorderWidth(0);
        addComponent(BorderLayout.CENTER, tabbedPane);
        this.items = new Vector();
    }

    /**
     * Creates default values or loads existing main screen items
     */
    public void load() {
        try {
            cgServices = new ListLocify();
            cgServices.addActionListener(this);
            cgWhere = new ListLocify();
            cgWhere.addActionListener(this);
            cgSaved = new ListLocify();
            cgSaved.addActionListener(this);
            cgMaps = new ListLocify();
            cgMaps.addActionListener(this);
            cgNavigation = new ListLocify();
            cgNavigation.addActionListener(this);
            cgMore = new ListLocify();
            cgMore.addActionListener(this);

            tabbedPane.addTab(null, IconData.getLocalImage("home.png"), cgServices);
            tabbedPane.addTab(null, IconData.getLocalImage("where.png"), cgWhere);
            tabbedPane.addTab(null, IconData.getLocalImage("saved.png"), cgSaved);
            tabbedPane.addTab(null, IconData.getLocalImage("maps.png"), cgMaps);
            tabbedPane.addTab(null, IconData.getLocalImage("navigation.png"), cgNavigation);
            tabbedPane.addTab(null, IconData.getLocalImage("more.png"), cgMore);
            tabbedPane.addTabsListener(this);

            //loading
            R.getLoading().setText(Locale.get("Loading_mainscreen"));
            if (!R.getFileSystem().exists(FileSystem.MAINSCREEN_FILE)) {  //prvni start aplikace
                items = new Vector();
                //#if applet
//#                 addEdit("http://localhost/m", "", R.getURL().getIcon("locify://shortcut"));
//#                 /*
//#                 addEdit("http://services.locify.com/wikipedia/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/nearestCaches/", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/twitter/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/panoramio/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/eventful/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/opencaching/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/fireeagle/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/upcoming/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/zvents/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://services.locify.com/accuweather/welcome", "", R.getURL().getIcon("locify://shortcut"));
//#                 addEdit("http://locify.destil.cz/geomail/", "", R.getURL().getIcon("locify://shortcut"));
//#                  * */
                //#else
                //nacitani zastupcu na download z JADu
                Logger.log("Loading shortcuts from JAD file");
                int i = 1;
                while (true) {
                    if (R.getMidlet().getAppProperty("Shortcut-" + i) == null) {
                        break;
                    } else {
                        String[] parts = StringTokenizer.getArray(R.getMidlet().getAppProperty("Shortcut-" + i), "|");
                        //shorcut to download
                        addEdit(parts[1], parts[0], "locify://icons/shortcut_to_download_25x25.png");
                        i++;
                    }
                }
                //#endif
                autoInstallRequest = true;
                saveXML();
            } else {
                loadXML();
            }

            //loading other tabs
            loadTab(1);
            loadTab(2);
            loadTab(3);
            loadTab(4);
            loadTab(5);

            this.setCommandListener(this);
            selectionChanged(-1, 0);

            //#if applet
//#             autoInstall();
            //#else
            if (autoInstallRequest) {
                R.getConfirmScreen().view(Locale.get("Autoinstall_confirmation"), ConfirmScreen.AUTOINSTALL_SERVICES);
            }
        //#endif
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.load", null);
        }
    }

    /**
     * Views the screen
     */
    public void view() {
        if (!autoInstallRequest) {
            this.show();
        }
    }

    public void setAutoInstallRequest(boolean request) {
        this.autoInstallRequest = request;
    }

    private void loadTab(int number) {
        Label label;
        switch (number) {
            case 1:
                label = new Label(Locale.get("Gps"));
                label.setIcon(IconData.get("locify://icons/gps.png"));
                cgWhere.addItem(label);
                label = new Label(Locale.get("Saved_location"));
                label.setIcon(IconData.get("locify://icons/savedLocation.png"));
                cgWhere.addItem(label);
                label = new Label(Locale.get("Address"));
                label.setIcon(IconData.get("locify://icons/address.png"));
                cgWhere.addItem(label);
                label = new Label(Locale.get("Coordinates"));
                label.setIcon(IconData.get("locify://icons/coordinates.png"));
                cgWhere.addItem(label);
                label = new Label(Locale.get("Last_known"));
                label.setIcon(IconData.get("locify://icons/lastKnown.png"));
                cgWhere.addItem(label);
                break;
            case 2:
                label = new Label(Locale.get("Save_place"));
                label.setIcon(IconData.get("locify://icons/savePlace.png"));
                cgSaved.addItem(label);
                label = new Label(Locale.get("Record_route"));
                label.setIcon(IconData.get("locify://icons/recordRoute.png"));
                cgSaved.addItem(label);
                label = new Label(Locale.get("Browse"));
                label.setIcon(IconData.get("locify://icons/browse.png"));
                cgSaved.addItem(label);
                break;
            case 3:
                label = new Label(Locale.get("View_location"));
                label.setIcon(IconData.get("locify://icons/viewLocation.png"));
                cgMaps.addItem(label);
                label = new Label(Locale.get("View_place"));
                label.setIcon(IconData.get("locify://icons/viewPlace.png"));
                cgMaps.addItem(label);
                label = new Label(Locale.get("View_route"));
                label.setIcon(IconData.get("locify://icons/viewRoute.png"));
                cgMaps.addItem(label);
                break;
            case 4:
                if (NavigationScreen.isRunning()) {
                    label = new Label(Locale.get("Continue"));
                    label.setIcon(IconData.get("locify://icons/select.png"));
                    cgNavigation.addItem(label);
                } else {
                    label = new Label(Locale.get("Compass"));
                    label.setIcon(IconData.get("locify://icons/compass.png"));
                    cgNavigation.addItem(label);
                }

                label = new Label(Locale.get("To_place"));
                label.setIcon(IconData.get("locify://icons/navigateTo.png"));
                cgNavigation.addItem(label);
                label = new Label(Locale.get("Along_route"));
                label.setIcon(IconData.get("locify://icons/navigateAlong.png"));
                cgNavigation.addItem(label);

                if (R.getLocator().hasSatellites()) {
                    label = new Label(Locale.get("Satellites"));
                    label.setIcon(IconData.get("locify://icons/gps.png"));
                    cgNavigation.addItem(label);
                }
                break;
            case 5:
                label = new Label(Locale.get("Settings"));
                label.setIcon(IconData.get("locify://icons/settings.png"));
                cgMore.addItem(label);
                label = new Label(Locale.get("Check_version"));
                label.setIcon(IconData.get("locify://icons/checkVersion.png"));
                cgMore.addItem(label);
                label = new Label(Locale.get("Help"));
                label.setIcon(IconData.get("locify://icons/help.png"));
                cgMore.addItem(label);
                //#if !release
                label = new Label(Locale.get("Logger"));
                label.setIcon(IconData.get("locify://icons/moreInfo.png"));
                cgMore.addItem(label);
                //#endif
                break;
        }
    }

    public void actionPerformed(ActionEvent evt) {
        // handle Container items
//System.out.println("ActionPerformed: " + evt.getCommand() + " " + evt.getSource());
        if (evt.getSource() instanceof ListLocify) {
            ListLocify list = (ListLocify) evt.getSource();
            int listItem = list.getSelectedIndex();

            switch (tabbedPane.getSelectedIndex()) {
                case 0:
                    if (itemToBeMoved == null) {
                        MainScreenItem item = getFocusedItem();
                        if (item.isShortcutToDownload() || item.isShortcut()) //shorcut
                        {
                            R.getURL().call(item.getId());
                        } else //service
                        {
                            Service service = ServicesData.getService(item.getId());
                            ServicesData.setCurrent(item.getId());
                            R.getURL().call(service.getFirstScreenUrl());
                        }
                    } else {
                        actionPerformed(new ActionEvent(Commands.cmdMoveTo));
                    }
                    break;
                case 1:
                    if (listItem == 0 && R.getSettings().getShowIconsHelp()) {
                        R.getContext().setBackScreen("locify://help?text=3");
                        R.getHelp().viewIconsHelp();
                    }
                    R.getURL().call(R.getContext().actions[listItem]); //don't disable warning
                    break;
                case 2:
                    switch (listItem) {
                        case 0:
                            R.getURL().call("locify://savePlace");
                            break;
                        case 1:
                            R.getURL().call("locify://recordRoute");
                            break;
                        case 2:
                            R.getURL().call("locify://files");
                            break;
                    }
                    break;
                case 3:
                    switch (listItem) {
                        case 0:
                            R.getURL().call("locify://maps");
                            break;
                        case 1:
                            R.getURL().call("locify://files?to=maps&filter=place");
                            break;
                        case 2:
                            R.getURL().call("locify://files?to=maps&filter=route");
                            break;
                    }
                    break;
                case 4:
                    switch (listItem) {
                        case 0:
                            R.getURL().call("locify://navigation");
                            break;
                        case 1:
                            R.getURL().call("locify://files?to=navigation&filter=place");
                            break;
                        case 2:
                            R.getURL().call("locify://files?to=navigation&filter=route");
                            break;
                        case 3:
                            R.getURL().call("locify://satellites");
                            break;
                    }
                    break;
                case 5:
                    switch (listItem) {
                        case 0:
                            R.getURL().call("locify://settings");
                            break;
                        case 1:
                            R.getURL().call("locify://checkVersion");
                            break;
                        case 2:
                            R.getURL().call("locify://help");
                            break;
                        case 3:
                            R.getURL().call("locify://logger");
                            break;
                    }
                    break;
            }
        }
    }

    /**
     * Capture all!! actions from Command on form. If using subMenu then click on subMenu trigger two
     * actionCommand events. One from top level of menu, second from item clicked !!!
     * @param cmd
     */
    public void actionCommand(Command cmd) {
        if (cmd == Commands.cmdSelect) {
            actionPerformed(new ActionEvent(tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())));
        } else if (cmd == Commands.cmdExit) {
            R.getConfirmScreen().view(Locale.get("Really_exit"), ConfirmScreen.EXIT);
        } else if (cmd == Commands.cmdMoreInfo) {
            R.getURL().call("locify://serviceInfo");
        } else if (cmd == Commands.cmdServiceSettings) {
            R.getURL().call("locify://serviceSettings");
        } else if (cmd == Commands.cmdUpdateService) {
            R.getURL().call("locify://updateService");
        } else if (cmd == Commands.cmdRename) {
            R.getURL().call("locify://renameItem");
        } else if (cmd == Commands.cmdMove) {
            this.removeAllCommands();
            this.addCommand(Commands.cmdMoveTo);
            this.addCommand(Commands.cmdCancel);
            // this.setSelectCommand(cmdMoveTo);
            itemToBeMoved = getFocusedItem();
        } else if (cmd == Commands.cmdMoveTo) {
            move(itemToBeMoved, getFocusedItem());
            tabbedPane.setSelectedIndex(0);
////            cgServices.focus(items.indexOf(itemToBeMoved));
            checkServiceMenu();
            itemToBeMoved = null;
        } else if (cmd == Commands.cmdCancel) {
            tabbedPane.setSelectedIndex(0);
////            cgServices.focus(items.indexOf(itemToBeMoved));
            checkServiceMenu();
            itemToBeMoved = null;
        } else if (cmd == Commands.cmdDelete) {
            delete(getFocusedItem());
        } else if (cmd == Commands.cmdAddService) {
            R.getURL().call("locify://addService");
        } else if (cmd == Commands.cmdAddByLink) {
            R.getURL().call("locify://addServiceByLink");
        } else {
            for (int i = 0; i < R.getContext().commands.length; i++) {
                if (cmd == R.getContext().commands[i]) {
                    R.getURL().call(R.getContext().actions[i]);
                }
            }
        }
    }

    /**
     * Views mainscreen with services
     */
    private void createServiceMenu() {
        this.removeAllCommands();
        checkServiceMenu();
        this.addCommand(Commands.cmdExit);
        this.addCommand(Commands.cmdSelect);
        this.addCommand(new ParentCommand(Locale.get("Add"), IconData.getLocalImage("add"),
                new Command[]{Commands.cmdAddService, Commands.cmdAddByLink}));
    }

    private void createOtherMenu() {
        this.removeAllCommands();
        this.addCommand(Commands.cmdExit);
        this.addCommand(Commands.cmdSelect);
    }

    /**
     * Show/hide menu options Info and Settings
     */
    public void checkServiceMenu() {
        try {
            MainScreenItem focused = getFocusedItem();
            if (focused != null && itemToBeMoved == null) {
                this.removeCommand(Commands.cmdService);
                this.removeCommand(Commands.cmdShortcut);
                if (focused.isService()) {
                    //service specific commands
                    Service service = ServicesData.getService(focused.getId());
                    if ((service != null) && (!"".equals(service.getSettingsUrl()))) {
                        this.addCommand(new ParentCommand(Locale.get("Service"), null, new Command[]{Commands.cmdMoreInfo,
                                    Commands.cmdServiceSettings, Commands.cmdUpdateService, Commands.cmdRename, Commands.cmdMove, Commands.cmdDelete}));
                    } else {
                        this.addCommand(new ParentCommand(Locale.get("Service"), null, new Command[]{Commands.cmdMoreInfo,
                                    Commands.cmdUpdateService, Commands.cmdRename, Commands.cmdMove, Commands.cmdDelete}));
                    }
                } else {
                    //shortcut specific commands
                    this.addCommand(new ParentCommand(Locale.get("Shortcut"), null, new Command[]{
                                Commands.cmdRename, Commands.cmdMove, Commands.cmdDelete}));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.checkServiceMenu", null);
        }
    }

    /*
     * --------------------- MAINSCREEN DATA --------------------------------
     */
    /**
     * Adds item to mainscreen or edit current.
     *
     * @param id item id
     * @param title item title
     * @param iconUrl url of icon
     * @param timestamp timestamp
     */
    public void addEdit(String id, String title, String iconUrl, long timestamp) {
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            MainScreenItem item = (MainScreenItem) items.elementAt(i);
            if (item.getId().equals(id)) {
                item.setIcon(iconUrl);
                item.setText(title);
                item.setTimestamp(timestamp);
                items.setElementAt(item, i);

////                cgServices.set(i, title, IconData.get(iconUrl));
//                Button btn = (Button) cgServices.getComponentAt(i);
//                btn.setText(title);
//                btn.setIcon(IconData.get(iconUrl));
                found = true;
                break;
            }
        }
        // create new item
        if (!found) {
            MainScreenItem item = new MainScreenItem(id, title, iconUrl, items.size(), timestamp);
//System.out.println("get: " + iconUrl);
            item.setIcon(IconData.get(iconUrl));
            items.addElement(item);
            cgServices.addItem(item);
        }
        saveXML();
        checkServiceMenu();
    }

    public void addEdit(String id, String name, String iconUrl) {
        addEdit(id, name, iconUrl, Utils.timestamp());
    }

    /**
     * Add item as service
     * @param service service object
     */
    public void addEdit(Service service) {
        addEdit(service.getId(), service.getName(), service.getIcon());
    }

    /**
     * Deletes menu item and related service
     *
     * @param item
     * @throws IllegalArgumentException if item with <code>id</code> does not exist
     */
    public void delete(MainScreenItem item) {
        for (int i = 0; i < cgServices.getSize(); i++) {
            if (cgServices.getItemAt(i) == item) {
                cgServices.removeItem(i);
                break;
            }
        }
        items.removeElement(item);
        if (item.isService()) {
            ServicesData.delete(item.getId());
        }
        saveXML();
    }

    /**
     * Delete all is used in sync
     */
    public void deleteAllItems() {
        for (int i = cgServices.getSize() - 1; i >=0 ; i--) {
            cgServices.removeItem(i);
        }
        items.removeAllElements();
    }

    /**
     * Gets focused item id.
     *
     * @return focused item id or <code>null</code> if there is not selected item
     */
    public MainScreenItem getFocusedItem() {
        if (items.isEmpty()) {
            return null;
        } else if (cgServices.getSelectedIndex() == -1) {
            return (MainScreenItem) items.firstElement();
        } else {
            return (MainScreenItem) items.elementAt(cgServices.getSelectedIndex());
        }
    }

    /**
     * Moves item and moves all subsequent
     * @param movedItem item to be moved
     * @param moveBefore move before form item
     */
    public void move(MainScreenItem movedItem, MainScreenItem moveBefore) {
        try {
            //change vector
            int newPosition = items.indexOf(moveBefore);
            items.removeElement(movedItem);
            items.insertElementAt(movedItem, newPosition);
            //change list according to vector
            for (int i = 0; i < items.size(); i++) {
                MainScreenItem item = (MainScreenItem) items.elementAt(i);
////                cgServices.set(i, item.getTitle(), IconData.get(item.getIcon()));
            }
            saveXML();
        // setFocus(itemToBeMoved);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.move", null);
        }
    }

    /**
     * Refreshes icon after icon is downloaded
     * @param iconUrl
     * @param data image data
     */
    public void refreshIcon(String iconUrl, byte[] data) {
        try {
            for (int i = 0; i < items.size(); i++) {
                MainScreenItem item = (MainScreenItem) items.elementAt(i);
                if (item.getIconURL().equals(iconUrl)) {
////                    cgServices.getItem(i).setImage(Image.createImage(data, 0, data.length));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.refreshIcon", iconUrl);
        }
    }

    /**
     * Refresh all icons after sync
     */
    public void refreshIcons() {
        try {
            for (int i = 0; i < items.size(); i++) {
                MainScreenItem item = (MainScreenItem) items.elementAt(i);
////                cgServices.set(i, item.getTitle(), IconData.get(item.getIcon()));
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.refreshIcons", null);
        }
    }

    /**
     * Automatically installs all preinstalled services
     */
    public void autoInstall() {
        R.getProgress().view(Locale.get("Installing"), Locale.get("Downloading_services"));
        R.getXmlParser().setAutoInstall(true);
        autoInstallUrls = new Vector();
        for (int i = 0; i < items.size(); i++) {
            MainScreenItem item = (MainScreenItem) items.elementAt(i);
            if (item.isShortcutToDownload()) {
                autoInstallUrls.addElement(item.getId());
            }
        }
        autoInstallNext();
    }

    /**
     * Autoinstalls next element after all elements are installed
     */
    public void autoInstallNext() {
        try {
            if (autoInstallUrls.size() > 0) {
                R.getHttp().start((String) autoInstallUrls.firstElement(), Http.SERVICE);
                autoInstallUrls.removeElement(autoInstallUrls.firstElement());
            } else {
                R.getXmlParser().setAutoInstall(false);
                autoInstallRequest = false;
                refreshIcons();
                view();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.autoInstallNext", null);
        }
    }

//    /**
//     * Returns all main screen items in sync format
//     * @return sync data
//     */
//    public String syncData() {
//        try {
//            String syncData = "<file>\n";
//            syncData += "<id>locify://mainScreen</id>\n";
//            syncData += "<type>view</type>\n";
//            syncData += "<action>allSync</action>\n";
//            syncData += "<ts>" + R.getFileSystem().getTimestamp(FileSystem.MAINSCREEN_FILE) + "</ts>\n";
//            syncData += "<content>\n";
//            syncData += getXML();
//            syncData += "</content>\n";
//            syncData += "</file>\n";
//            return syncData;
//        } catch (Exception e) {
//            R.getErrorScreen().view(e, "MainScreen.syncData", null);
//            return "";
//        }
//    }

    public void loadXML() {
        try {
            items = new Vector();
            String data = R.getFileSystem().loadString(FileSystem.MAINSCREEN_FILE);
            ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(data));
            bais.reset();
            KXmlParser parser = new KXmlParser();
            //parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
            parser.setInput(bais, "utf-8");
            String id = "";
            String name = "";
            String icon = "";
            String timestamp = "";
            while (true) {
                int parserType = parser.next();
                if (parserType == KXmlParser.END_DOCUMENT) {
                    break;
                }
                if (parserType != KXmlParser.START_TAG) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName == null) {
                    // nothing to do ... empty tag
                } else if (tagName.equals("id")) {
                    id = parser.nextText();
                } else if (tagName.equals("name")) {
                    name = parser.nextText();
                } else if (tagName.equals("icon")) {
                    icon = parser.nextText();
                } else if (tagName.equals("ts")) {
                    timestamp = parser.nextText();
                    //zpracovani
                    this.addEdit(id, name, icon, Long.parseLong(timestamp));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.loadXML", null);
        }
    }

    private void saveXML() {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + getXML();
        R.getFileSystem().saveString(FileSystem.MAINSCREEN_FILE, data);
    }

    private String getXML() {
        String data = "<list>\n";
        for (int i = 0; i < items.size(); i++) {
            MainScreenItem item = (MainScreenItem) items.elementAt(i);
            data += "<item>\n";
            data += "<id>" + item.getId() + "</id>\n";
            data += "<name>" + Utils.replaceString(item.getText(), "&", "&amp;") + "</name>\n";
            data += "<icon>" + item.getIconURL() + "</icon>\n";
            data += "<ts>" + item.getTimestamp() + "</ts>\n";
            data += "</item>\n";
        }
        data += "</list>\n";
        return data;
    }

    public void selectionChanged(int oldSelected, int newSelected) {
//        System.out.println("Old: " + oldSelected + ", new: " + newSelected);
        switch (newSelected) {
            case 0:
                this.setTitle(Locale.get("Locify_home"));
                createServiceMenu();
                break;
            case 1:
                this.setTitle(Locale.get("Set_location"));
                if (oldSelected == 0)
                    createOtherMenu();
                break;
            case 2:
                this.setTitle(Locale.get("Saved"));
                if (oldSelected == 0)
                    createOtherMenu();
                break;
            case 3:
                this.setTitle(Locale.get("Maps"));
                if (oldSelected == 0)
                    createOtherMenu();
                break;
            case 4:
                this.setTitle(Locale.get("Navigation"));
                if (oldSelected == 0)
                    createOtherMenu();
                cgNavigation.removeAll();
                loadTab(4);
                break;
            case 5:
                this.setTitle(Locale.get("Dock_more"));
                if (oldSelected == 0)
                    createOtherMenu();
                cgMore.removeAll();
                loadTab(5);
                break;
        }
    }

    public void keyPressed(int keyCode) {
        if (keyCode < 0) {
            ListLocify list;
            int index;

            switch (Display.getInstance().getGameAction(keyCode)) {
                case Display.GAME_DOWN:
                    list = (ListLocify) tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
                    list.setSmoothScrolling(true);
                    index = list.getSelectedIndex();
                    if (index < list.getSize() - 1)
                        list.setSelectedIndex(index + 1, true);
                    else
                        list.setSelectedIndex(0, true);
                    break;
                case Display.GAME_UP:
                    list = (ListLocify) tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
                    index = list.getSelectedIndex();
                    if (index > 0)
                        list.setSelectedIndex(index - 1);
                    else
                        list.setSelectedIndex(list.getSize() - 1);
                    break;
                case Display.GAME_LEFT:
                    index = tabbedPane.getSelectedIndex();
                    if (index > 0)
                        tabbedPane.setSelectedIndex(index - 1);
                    else
                        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                    break;
                case Display.GAME_RIGHT:
                    index = tabbedPane.getSelectedIndex();
                    if (index < tabbedPane.getTabCount() - 1)
                        tabbedPane.setSelectedIndex(index + 1);
                    else
                        tabbedPane.setSelectedIndex(0);
                    break;
                case Display.GAME_FIRE:
                    actionPerformed(new ActionEvent(tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())));
                    break;
            }
        } else {
            super.keyPressed(keyCode);
        }

//        if (Display.getInstance().getGameAction(keyCode) == Display.GAME_LEFT) {
//            int index = tabbedPane.getSelectedIndex();
//            if (index > 0)
//                tabbedPane.setSelectedIndex(index - 1);
//            else
//                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
//        } else if (Display.getInstance().getGameAction(keyCode) == Display.GAME_RIGHT) {
//            int index = tabbedPane.getSelectedIndex();
//            if (index < tabbedPane.getTabCount() - 1)
//                tabbedPane.setSelectedIndex(index + 1);
//            else
//                tabbedPane.setSelectedIndex(0);
//        } else {
//            super.keyPressed(keyCode);
//        }
    }

////    public void itemStateChanged(Item item) {
////        int selected = ((ChoiceGroup) item).getSelectedIndex();
////        if (hasPointerEvents && this.getActiveTab() == 0) {
////            if (selected == lastSelectedIndex) {
////                handleSelect(this.getActiveTab(), selected);
////            }
////            lastSelectedIndex = selected;
////        } else {
////            handleSelect(this.getActiveTab(), selected);
////        }
////        ((ChoiceGroup) item).setSelectedIndex(-1, true);
////    }
////
////    public void pointerPressed(int a, int b) {
////        hasPointerEvents = true;
////        super.pointerPressed(a, b);
////    }
////
////    public void screenStateChanged(Screen screen) {
////        if (this.getActiveTab() == 0) {
////            checkServiceMenu();
////        }
////    }
////
    public void checkLoginLogout() {
        cgMore.removeAll();
        loadTab(5);
    }


////    public void keyPressed(int keyCode) {
////        if (getGameAction(keyCode) == Canvas.UP && !isMenuOpened()) {
////            ChoiceGroup group = (ChoiceGroup) this.getCurrentItem();
////            if (group.getFocusedIndex() == 0) {
////                group.setSelectedIndex(group.getNumberOfInteractiveItems() - 1, true);
////                group.setSelectedIndex(-1, true);
////            } else {
////                super.keyPressed(keyCode);
////            }
////        } else if (getGameAction(keyCode) == Canvas.DOWN && !isMenuOpened()) {
////            ChoiceGroup group = (ChoiceGroup) this.getCurrentItem();
////            if (group.getFocusedIndex() == group.getNumberOfInteractiveItems() - 1) {
////                group.setSelectedIndex(0, true);
////                group.setSelectedIndex(-1, true);
////            } else {
////                super.keyPressed(keyCode);
////            }
////        } else {
////            super.keyPressed(keyCode);
////        }
////    }
}
