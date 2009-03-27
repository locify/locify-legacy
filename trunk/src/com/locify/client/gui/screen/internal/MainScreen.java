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

import com.locify.client.data.CookieData;
import com.locify.client.data.FileSystem;
import com.locify.client.data.IconData;
import com.locify.client.data.Service;
import com.locify.client.data.ServicesData;
import com.locify.client.data.SettingsData;
import com.locify.client.gui.screen.service.ConfirmScreen;
import com.locify.client.net.Http;
import com.locify.client.utils.Commands;
import de.enough.polish.ui.Choice;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Screen;
import de.enough.polish.util.Locale;
import java.util.Vector;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.Utils;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.ScreenStateListener;
import de.enough.polish.ui.TabbedForm;
import de.enough.polish.ui.TabbedFormListener;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import java.io.ByteArrayInputStream;
import com.locify.client.utils.UTF8;
import org.kxml2.io.KXmlParser;

/**
 * Main screen of application with all services, tab menus and context menus
 * @author Destil
 */
public class MainScreen extends TabbedForm implements CommandListener, TabbedFormListener, ItemStateListener, ScreenStateListener {

    private Vector items;
    private MainScreenItem itemToBeMoved = null;
    public Command cmdService = new Command(Locale.get("Service"), Command.SCREEN, 11);
    protected Command cmdShortcut = new Command(Locale.get("Shortcut"), Command.SCREEN, 11);
    protected Command cmdAdd = new Command(Locale.get("Add"), Command.SCREEN, 13);
    protected Command cmdExit = new Command(Locale.get("Exit"), Command.EXIT, 1);
    //service/shortcut
    public Command cmdMoreInfo = new Command(Locale.get("More_info"), Command.SCREEN, 10);
    public Command cmdServiceSettings = new Command(Locale.get("Settings"), Command.SCREEN, 11);
    public Command cmdUpdateService = new Command(Locale.get("Update_service"), Command.SCREEN, 12);
    protected Command cmdRename = new Command(Locale.get("Rename"), Command.SCREEN, 13);
    protected Command cmdMove = new Command(Locale.get("Move"), Command.SCREEN, 14);
    protected Command cmdDelete = new Command(Locale.get("Delete"), Command.SCREEN, 15);
    //add
    protected Command cmdAddService = new Command(Locale.get("Service_from_list"), Command.SCREEN, 10);
    protected Command cmdAddByLink = new Command(Locale.get("Service_by_link"), Command.SCREEN, 11);
    protected Command cmdAddShortcut = new Command(Locale.get("Shortcut"), Command.SCREEN, 12);
    //right button commands
    protected Command cmdMoveTo = new Command(Locale.get("Move"), Command.SCREEN, 1);
    protected Command cmdCancel = new Command(Locale.get("Cancel"), Command.CANCEL, 2);
    private ChoiceGroup cgServices;
    private ChoiceGroup cgWhere;
    private ChoiceGroup cgSaved;
    private ChoiceGroup cgMaps;
    private ChoiceGroup cgNavigation;
    private ChoiceGroup cgMore;
    private boolean autoInstallRequest = false;
    private Vector autoInstallUrls;
    private int lastSelectedIndex = 0;
    public static boolean hasPointerEvents = false;

    /**
     * Contructs a new <code>MainScreen</code> object.
     */
    public MainScreen() {
        //#style mainScreen
        super(Locale.get("Locify_home"), null, new Image[]{IconData.get("locify://icons/home.png"), IconData.get("locify://icons/where.png"), IconData.get("locify://icons/saved.png"), IconData.get("locify://icons/maps.png"), IconData.get("locify://icons/navigation.png"), IconData.get("locify://icons/more.png")});
        this.items = new Vector();
        hasPointerEvents = this.hasPointerEvents();
    }

    /**
     * Creates default values or loads existing main screen items
     */
    public void load() {
        try {
            //#style mainScreenlist
            cgServices = new ChoiceGroup(null, Choice.EXCLUSIVE);
            //#style mainScreenList
            cgWhere = new ChoiceGroup(null, Choice.EXCLUSIVE);
            //#style mainScreenList
            cgSaved = new ChoiceGroup(null, Choice.EXCLUSIVE);
            //#style mainScreenList
            cgMaps = new ChoiceGroup(null, Choice.EXCLUSIVE);
            //#style mainScreenList
            cgNavigation = new ChoiceGroup(null, Choice.EXCLUSIVE);
            //#style mainScreenList
            cgMore = new ChoiceGroup(null, Choice.EXCLUSIVE);
            this.append(0, cgServices);
            this.append(1, cgWhere);
            this.append(2, cgSaved);
            this.append(3, cgMaps);
            this.append(4, cgNavigation);
            this.append(5, cgMore);

            //loading
            R.getLoading().setText(Locale.get("Loading_mainscreen"));

            if (!R.getFileSystem().exists(FileSystem.MAINSCREEN_FILE)) {  //prvni start aplikace
                items = new Vector();
                //nacitani zastupcu na download z JADu
                Logger.log("Loading shortcuts from JAD file");
                int i = 1;
                while (true) {
                    if (R.getMidlet().getAppProperty("Shortcut-" + i) == null) {
                        break;
                    } else {
                        String[] parts = StringTokenizer.getArray(R.getMidlet().getAppProperty("Shortcut-" + i), "|");
                        if (parts[1].startsWith("locify://")) {
                            //shorcut to internal function
                            addEdit(parts[1], parts[0], R.getURL().getIcon(parts[1]));
                        } else {
                            //shorcut to download
                            addEdit(parts[1], parts[0], R.getURL().getIcon("locify://shortcut"));
                        }
                        i++;
                    }
                }
                autoInstallRequest = true;
                saveXML();
            } else {
                loadXML();
            }
            cgServices.setItemStateListener(this);

            //loading other tabs
            loadTab(1);
            cgWhere.setItemStateListener(this);

            loadTab(2);
            cgSaved.setItemStateListener(this);

            loadTab(3);
            cgMaps.setItemStateListener(this);

            loadTab(4);
            cgNavigation.setItemStateListener(this);

            loadTab(5);
            cgMore.setItemStateListener(this);

            this.setCommandListener(this);
            this.setScreenStateListener(this);
            this.setTabbedFormListener(this);
            this.setActiveTab(0);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.load", null);
        }
    }

    private void loadTab(int number) {
        switch (number) {
            case 1:
                //#style mainScreenListItem
                cgWhere.append(Locale.get("Gps"), IconData.get("locify://icons/gps.png"));
                //#style mainScreenListItem
                cgWhere.append(Locale.get("Saved_location"), IconData.get("locify://icons/savedLocation.png"));
                //#style mainScreenListItem
                cgWhere.append(Locale.get("Address"), IconData.get("locify://icons/address.png"));
                //#style mainScreenListItem
                cgWhere.append(Locale.get("Coordinates"), IconData.get("locify://icons/coordinates.png"));
                //#style mainScreenListItem
                cgWhere.append(Locale.get("Last_known"), IconData.get("locify://icons/lastKnown.png"));
                break;
            case 2:
                //#style mainScreenListItem
                cgSaved.append(Locale.get("Save_place"), IconData.get("locify://icons/savePlace.png"));
                //#style mainScreenListItem
                cgSaved.append(Locale.get("Record_route"), IconData.get("locify://icons/recordRoute.png"));
                //#style mainScreenListItem
                cgSaved.append(Locale.get("Browse"), IconData.get("locify://icons/browse.png"));
                //#style mainScreenListItem
                cgSaved.append(Locale.get("Sync"), IconData.get("locify://icons/sync.png"));
                break;
            case 3:
                //#style mainScreenListItem
                cgMaps.append(Locale.get("View_location"), IconData.get("locify://icons/viewLocation.png"));
                //#style mainScreenListItem
                cgMaps.append(Locale.get("View_place"), IconData.get("locify://icons/viewPlace.png"));
                //#style mainScreenListItem
                cgMaps.append(Locale.get("View_route"), IconData.get("locify://icons/viewRoute.png"));
                break;
            case 4:
                if (NavigationScreen.isRunning()) {
                    //#style mainScreenListItem
                    cgNavigation.append(Locale.get("Continue"), IconData.get("locify://icons/select.png"));
                } else {
                    //#style mainScreenListItem
                    cgNavigation.append(Locale.get("Compass"), IconData.get("locify://icons/compass.png"));
                }
                //#style mainScreenListItem
                cgNavigation.append(Locale.get("To_place"), IconData.get("locify://icons/navigateTo.png"));
                //#style mainScreenListItem
                cgNavigation.append(Locale.get("Along_route"), IconData.get("locify://icons/navigateAlong.png"));
                if (R.getLocator().hasSatellites()) {
                    //#style mainScreenListItem
                    cgNavigation.append(Locale.get("Satellites"), IconData.get("locify://icons/gps.png"));
                }
                break;
            case 5:
                //#style mainScreenListItem
                cgMore.append(Locale.get("Settings"), IconData.get("locify://icons/settings.png"));
                if (CookieData.getValue("session", R.getHttp().DEFAULT_URL).equals("")) { //ignore warning!
                    //#style mainScreenListItem
                    cgMore.append(Locale.get("Login"), IconData.get("locify://icons/login.png"));
                } else {
                    //#style mainScreenListItem
                    cgMore.append(Locale.get("Logout"), IconData.get("locify://icons/logout.png"));
                }
                //#style mainScreenListItem
                cgMore.append(Locale.get("Check_version"), IconData.get("locify://icons/checkVersion.png"));
                //#style mainScreenListItem
                cgMore.append(Locale.get("Help"), IconData.get("locify://icons/help.png"));
                //#if !release
                //#style mainScreenListItem
                cgMore.append(Locale.get("Logger"), IconData.get("locify://icons/moreInfo.png"));
                //#endif
                break;
        }
    }

    /**
     * Views the screen
     */
    public void view() {
        if (autoInstallRequest) {
            R.getConfirmScreen().view(Locale.get("Autoinstall_confirmation"), ConfirmScreen.AUTOINSTALL_SERVICES);
        } else {
            R.getMidlet().switchDisplayable(null, this);
        }
        autoInstallRequest = false;
    }

    /**
     * Views mainscreen with services
     */
    private void createServiceMenu() {
        this.removeAllCommands();
        checkServiceMenu();
        //#style imgSelect
        this.addCommand(Commands.cmdSelect);
        //#style imgAdd
        this.addCommand(cmdAdd);
        //#style imgExit
        this.addCommand(cmdExit);

        //#style imgAddFromList
        this.addSubCommand(cmdAddService, cmdAdd);
        //#style imgAddFromLink
        this.addSubCommand(cmdAddByLink, cmdAdd);
        //#style imgAddShortcut
        this.addSubCommand(cmdAddShortcut, cmdAdd);
    }

    private void createOtherMenu() {
        this.removeCommand(cmdService);
        this.removeCommand(cmdShortcut);
        this.removeCommand(cmdAdd);
    }

    /**
     * Show/hide menu options Info and Settings
     */
    public void checkServiceMenu() {
        try {
            MainScreenItem focused = getFocusedItem();
            if (focused != null && itemToBeMoved == null) {
                this.removeCommand(cmdService);
                this.removeCommand(cmdShortcut);
                if (focused.isService()) {
                    //service specific commands
                    //#style imgManage
                    this.addCommand(cmdService);
                    //#style imgInfo
                    this.addSubCommand(cmdMoreInfo, cmdService);
                    Service service = ServicesData.getService(focused.getId());

                    if ((service != null) && (!"".equals(service.getSettingsUrl()))) {
                        //#style imgServiceSettings
                        this.addSubCommand(cmdServiceSettings, cmdService);
                    }

                    //#style imgUpdateService
                    this.addSubCommand(cmdUpdateService, cmdService);
                    //#style imgRename
                    this.addSubCommand(cmdRename, cmdService);
                    //#style imgMove
                    this.addSubCommand(cmdMove, cmdService);
                    //#style imgDelete
                    this.addSubCommand(cmdDelete, cmdService);
                } else {
                    //shortcut specific commands
                    //#style imgManage
                    this.addCommand(cmdShortcut);
                    //#style imgRename
                    this.addSubCommand(cmdRename, cmdShortcut);
                    //#style imgMove
                    this.addSubCommand(cmdMove, cmdShortcut);
                    //#style imgDelete
                    this.addSubCommand(cmdDelete, cmdShortcut);
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
                item.setTitle(title);
                item.setTimestamp(timestamp);
                items.setElementAt(item, i);
                cgServices.set(i, title, IconData.get(iconUrl));
                found = true;
                break;
            }
        }
        // create new item
        if (!found) {
            MainScreenItem item = new MainScreenItem(id, title, iconUrl, items.size(), timestamp);
            items.addElement(item);
            //#style mainScreenListItem
            cgServices.append(title, IconData.get(iconUrl));
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
        cgServices.delete(items.indexOf(item));
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
        items.removeAllElements();
        cgServices.deleteAll();
    }

    /**
     * Gets focused item id.
     *
     * @return focused item id or <code>null</code> if there is not selected item
     */
    public MainScreenItem getFocusedItem() {
        if (items.isEmpty()) {
            return null;
        } else if (cgServices.getFocusedIndex() == -1) {
            return (MainScreenItem) items.firstElement();
        } else {
            return (MainScreenItem) items.elementAt(cgServices.getFocusedIndex());
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
                cgServices.set(i, item.getTitle(), IconData.get(item.getIcon()));
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
                if (item.getIcon().equals(iconUrl)) {
                    cgServices.getItem(i).setImage(Image.createImage(data, 0, data.length));
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
                cgServices.set(i, item.getTitle(), IconData.get(item.getIcon()));
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.refreshIcons", null);
        }
    }

    /**
     * Automatically installs all preinstalled services
     */
    public void autoInstall() {
        R.getSync().viewProgressScreen(Locale.get("Installing"), Locale.get("Downloading_services"));
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
                R.getHttp().start((String) autoInstallUrls.firstElement());
                autoInstallUrls.removeElement(autoInstallUrls.firstElement());
            } else {
                R.getXmlParser().setAutoInstall(false);
                refreshIcons();
                view();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.autoInstallNext", null);
        }
    }

    /**
     * Returns all main screen items in sync format
     * @return sync data
     */
    public String syncData() {
        try {
            String syncData = "<file>\n";
            syncData += "<id>locify://mainScreen</id>\n";
            syncData += "<type>view</type>\n";
            syncData += "<action>allSync</action>\n";
            syncData += "<ts>" + R.getFileSystem().getTimestamp(FileSystem.MAINSCREEN_FILE) + "</ts>\n";
            syncData += "<content>\n";
            syncData += getXML();
            syncData += "</content>\n";
            syncData += "</file>\n";
            return syncData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.syncData", null);
            return "";
        }
    }

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
            data += "<name>" + item.getTitle() + "</name>\n";
            data += "<icon>" + item.getIcon() + "</icon>\n";
            data += "<ts>" + item.getTimestamp() + "</ts>\n";
            data += "</item>\n";
        }
        data += "</list>\n";
        return data;
    }

    public int availableWidth() {
        return this.getScreenContentWidth();
    }

    public int availableHeight() {
        return this.getScreenContentHeight();
    }

    /* 
    ------------- LISTENERS -------------------- 
     */
    public void commandAction(Command c, Displayable d) {
        if (c == Commands.cmdSelect) {
            handleSelect(this.getActiveTab(), ((ChoiceGroup) this.getCurrentItem()).getFocusedIndex());
        } else if (c == cmdExit) {
            R.getURL().call("locify://exit");
        } else if (c == cmdMoreInfo) {
            R.getURL().call("locify://serviceInfo");
        } else if (c == cmdServiceSettings) {
            R.getURL().call("locify://serviceSettings");
        } else if (c == cmdUpdateService) {
            R.getURL().call("locify://updateService");
        } else if (c == cmdRename) {
            R.getURL().call("locify://renameItem");
        } else if (c == cmdMove) {
            this.removeAllCommands();
            this.addCommand(cmdMoveTo);
            this.addCommand(cmdCancel);
            // this.setSelectCommand(cmdMoveTo);
            itemToBeMoved = getFocusedItem();
        } else if (c == cmdMoveTo) {
            move(itemToBeMoved, getFocusedItem());
            this.setActiveTab(0);
            cgServices.focus(items.indexOf(itemToBeMoved));
            checkServiceMenu();
            itemToBeMoved = null;
        } else if (c == cmdCancel) {
            this.setActiveTab(0);
            cgServices.focus(items.indexOf(itemToBeMoved));
            checkServiceMenu();
            itemToBeMoved = null;
        } else if (c == cmdDelete) {
            delete(getFocusedItem());
        } else if (c == cmdAddService) {
            R.getURL().call("locify://addService");
        } else if (c == cmdAddByLink) {
            R.getURL().call("locify://addServiceByLink");
        } else if (c == cmdAddShortcut) {
            R.getURL().call("locify://addShortcut");
        } else {
            for (int i = 0; i < R.getContext().commands.length; i++) {
                if (c == R.getContext().commands[i]) {
                    R.getURL().call(R.getContext().actions[i]);
                }
            }
        }
    }

    public boolean notifyTabChangeRequested(int oldTabIndex, int newTabIndex) {
        switch (newTabIndex) {
            case 0:
                this.setTitle(Locale.get("Locify_home"));
                createServiceMenu();
                break;
            case 1:
                this.setTitle(Locale.get("Set_location"));
                createOtherMenu();
                break;
            case 2:
                this.setTitle(Locale.get("Saved"));
                createOtherMenu();
                break;
            case 3:
                this.setTitle(Locale.get("Maps"));
                createOtherMenu();
                break;
            case 4:
                this.setTitle(Locale.get("Navigation"));
                createOtherMenu();
                break;
            case 5:
                this.setTitle(Locale.get("Dock_more"));
                createOtherMenu();
                break;
        }
        lastSelectedIndex = 0;
        return true;
    }

    public void notifyTabChangeCompleted(int oldTabIndex, int newTabIndex) {
        switch (newTabIndex) {
            case 4:
                cgNavigation.deleteAll();
                loadTab(4);
                break;
            case 5:
                cgMore.deleteAll();
                loadTab(5);
                break;
        }
    }

    /**
     * Handles selecting item on the main screen
     * @param tab
     * @param listItem
     */
    private void handleSelect(int tab, int listItem) {
        try {
            switch (tab) {
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
                        commandAction(cmdMoveTo, this);
                    }
                    break;
                case 1:
                    if (listItem==0 && R.getSettings().getShowIconsHelp())
                    {
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
                            R.getContext().setBackScreen("locify://recordRoute");
                            R.getURL().call("locify://recordRoute");
                            break;
                        case 2:
                            R.getURL().call("locify://files");
                            break;
                        case 3:
                            R.getURL().call("locify://synchronize");
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
                            if (CookieData.getValue("session", Http.DEFAULT_URL).equals("")) { //ignore warning!
                                if (R.getSettings().getAutoLogin() == SettingsData.ON && !R.getSettings().getName().equals("")) {
                                    R.getBack().dontSave();
                                }
                                R.getURL().call("locify://login");
                            } else {
                                R.getURL().call(Http.DEFAULT_URL + "user/logout");
                            }
                            break;
                        case 2:
                            R.getURL().call("locify://checkVersion");
                            break;
                        case 3:
                            R.getURL().call("locify://help");
                            break;
                        case 4:
                            R.getURL().call("locify://logger");
                            break;
                    }
                    break;
            }

        } catch (Exception e) {
            R.getErrorScreen().view(e, "MainScreen.itemStateChanged", null);
        }
    }

    public void itemStateChanged(Item item) {
        int selected = ((ChoiceGroup) item).getSelectedIndex();
        if (hasPointerEvents && this.getActiveTab() == 0) {
            if (selected == lastSelectedIndex) {
                handleSelect(this.getActiveTab(), selected);
            }
            lastSelectedIndex = selected;
        } else {
            handleSelect(this.getActiveTab(), selected);
        }
        ((ChoiceGroup) item).setSelectedIndex(-1, true);
    }

    public void pointerPressed(int a, int b) {
        hasPointerEvents = true;
        super.pointerPressed(a, b);
    }

    public void screenStateChanged(Screen screen) {
        if (this.getActiveTab() == 0) {
            checkServiceMenu();
        }
    }

    public void checkLoginLogout() {
        cgMore.deleteAll();
        loadTab(5);
    }

    public void keyPressed(int keyCode) {
        if (getGameAction(keyCode) == Canvas.UP && !isMenuOpened()) {
            ChoiceGroup group = (ChoiceGroup) this.getCurrentItem();
            if (group.getFocusedIndex() == 0) {
                group.setSelectedIndex(group.getNumberOfInteractiveItems() - 1, true);
                group.setSelectedIndex(-1, true);
            } else {
                super.keyPressed(keyCode);
            }
        } else if (getGameAction(keyCode) == Canvas.DOWN && !isMenuOpened()) {
            ChoiceGroup group = (ChoiceGroup) this.getCurrentItem();
            if (group.getFocusedIndex() == group.getNumberOfInteractiveItems() - 1) {
                group.setSelectedIndex(0, true);
                group.setSelectedIndex(-1, true);
            } else {
                super.keyPressed(keyCode);
            }
        } else {
            super.keyPressed(keyCode);
        }
    }
}
