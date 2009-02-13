/*
 * R.java
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
package com.locify.client.utils;

import com.locify.client.gui.manager.BackScreenManager;
import com.locify.client.gui.manager.BluetoothManager;
import com.locify.client.gui.screen.service.ConfirmScreen;
import com.locify.client.gui.screen.service.AlertScreen;
import com.locify.client.gui.Midlet;
import com.locify.client.gui.screen.internal.HelpScreen;
import com.locify.client.net.Redirection;
import com.locify.client.net.Http;
import com.locify.client.gui.screen.service.HTMLScreen;
import com.locify.client.net.XmlParser;
import com.locify.client.gui.screen.internal.NavigationScreen;
import com.locify.client.gui.screen.internal.MainScreen;
import com.locify.client.gui.screen.internal.ShortcutsScreen;
import com.locify.client.locator.LocatorModel;
import com.locify.client.data.FileSystem;
import com.locify.client.data.SettingsData;
import com.locify.client.data.Sync;
import com.locify.client.gui.manager.FirstRunManager;
import com.locify.client.gui.manager.InternalURLManager;
import com.locify.client.gui.manager.ServiceManager;
import com.locify.client.gui.polish.TopBarBackground;
import com.locify.client.gui.screen.internal.ErrorScreen;
import com.locify.client.gui.screen.internal.FileBrowser;
import com.locify.client.gui.screen.internal.FileSystemScreen;
import com.locify.client.gui.screen.internal.LoadingScreen;
import com.locify.client.gui.screen.internal.LocationScreens;
import com.locify.client.gui.screen.internal.LogScreen;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.gui.screen.internal.PlaceSaveScreen;
import com.locify.client.gui.screen.internal.RouteSaveScreen;
import com.locify.client.gui.screen.internal.RouteScreen;
import com.locify.client.gui.screen.internal.SatelliteScreen;
import com.locify.client.gui.screen.internal.SettingsScreen;
import com.locify.client.gui.screen.internal.UpdateScreen;
import com.locify.client.gui.screen.service.AuthenticationScreens;
import com.locify.client.gui.screen.service.GeoFileBrowser;
import com.locify.client.gui.screen.service.ListScreen;
import com.locify.client.gui.screen.service.UploadProgressScreen;
import com.locify.client.locator.LocationContext;
import com.locify.client.maps.mapItem.MapItemManager;
import com.locify.client.net.ConnectionProblem;
import com.locify.client.net.ExternalBrowser;
import com.locify.client.net.PostData;

/**
 * This class store all references to other classes in this project. It quaranties the singleton structure of classes.
 * @author David Vavra
 */
public class R {

    private static Midlet midlet;
    private static SettingsData settings;
    private static Http http;
    private static BluetoothManager bluetoothManager;
    private static XmlParser xmlParser;
    private static ConfirmScreen confirmScreen;
    private static AlertScreen customAlert;
    private static MainScreen mainScreen;
    private static AuthenticationScreens authentication;
    private static HelpScreen help;
    private static NavigationScreen navigationScreen;
    private static RouteScreen routeScreen;
    private static GeoFileBrowser geoDataBrowser;
    private static RouteSaveScreen routeSaveScreen;
    private static MapScreen mapScreen;
    private static MapItemManager mapItemManager;
    private static BackScreenManager backScreens;
    private static ShortcutsScreen shortcuts;
    private static HTMLScreen htmlScreen;
    private static Redirection autoSend;
    private static Logger logger;
    private static FileSystem fileSystem;
    private static LocatorModel locator;
    private static PostData postData;
    private static FileSystemScreen fileSystemScreen;
    private static InternalURLManager internalUrlManager;
    private static LoadingScreen loadingScreen;
    private static TopBarBackground topBar;
    private static LogScreen loggerScreen;
    private static ConnectionProblem connectionProblem;
    private static LocationScreens locationScreens;
    private static LocationContext locationContext;
    private static SettingsScreen settingsScreen;
    private static ServiceManager serviceManager;
    private static Sync sync;
    private static UpdateScreen update;
    private static ExternalBrowser external;
    private static ErrorScreen errorScreen;
    private static FirstRunManager firstRun;
    private static ListScreen customList;
    private static PlaceSaveScreen savePlace;
    private static FileBrowser fileBrowser;
    private static UploadProgressScreen progress;
    private static SatelliteScreen satelliteScreen;

    public R(Midlet ref) {
        midlet = ref;
    }

    public static Midlet getMidlet() {
        return midlet;
    }

    public static SettingsData getSettings() {
        if (settings == null) {
            settings = new SettingsData();
        }
        return settings;
    }

    public static Http getHttp() {
        if (http == null) {
            http = new Http();
        }
        return http;
    }

    public static XmlParser getXmlParser() {
        if (xmlParser == null) {
            xmlParser = new XmlParser();
        }
        return xmlParser;
    }

    public static ConfirmScreen getConfirmScreen() {
        if (confirmScreen == null) {
            confirmScreen = new ConfirmScreen();
        }
        return confirmScreen;
    }

    public static AlertScreen getCustomAlert() {
        if (customAlert == null) {
            customAlert = new AlertScreen();
        }
        return customAlert;
    }

    public static MainScreen getMainScreen() {
        if (mainScreen == null) {
            mainScreen = new MainScreen();
        }
        return mainScreen;
    }

    public static AuthenticationScreens getAuthentication() {
        if (authentication == null) {
            authentication = new AuthenticationScreens();
        }
        return authentication;
    }

    public static HelpScreen getHelp() {
        if (help == null) {
            help = new HelpScreen();
        }
        return help;
    }

    public static NavigationScreen getNavigationScreen() {
        if (navigationScreen == null) {
            navigationScreen = new NavigationScreen();
        }
        return navigationScreen;
    }

    public static MapScreen getMapScreen() {
        if (mapScreen == null) {
            mapScreen = new MapScreen();
            R.getLocator().addLocationChangeListener(mapScreen);
        }
        return mapScreen;
    }

    public static boolean isMapScreenInitialized() {
        return mapScreen != null;
    }

    public static MapItemManager getMapItemManager() {
        if (mapItemManager == null) {
            mapItemManager = new MapItemManager();
        }
        return mapItemManager;
    }

    public static RouteScreen getRouteScreen() {
        if (routeScreen == null) {
            routeScreen = new RouteScreen();
        }
        return routeScreen;
    }

    public static GeoFileBrowser getGeoDataBrowser() {
        if (geoDataBrowser == null) {
            geoDataBrowser = new GeoFileBrowser();
        }
        return geoDataBrowser;
    }

    public static RouteSaveScreen getRouteSaveScreen() {
        if (routeSaveScreen == null) {
            routeSaveScreen = new RouteSaveScreen();
        }
        return routeSaveScreen;
    }

    public static BackScreenManager getBack() {
        if (backScreens == null) {
            backScreens = new BackScreenManager();
        }
        return backScreens;
    }

    public static ShortcutsScreen getShortcuts() {
        if (shortcuts == null) {
            shortcuts = new ShortcutsScreen();
        }
        return shortcuts;
    }

    public static HTMLScreen getHTMLScreen() {
        if (htmlScreen == null) {
            htmlScreen = new HTMLScreen();
        }
        return htmlScreen;
    }

    public static Redirection getAutoSend() {
        if (autoSend == null) {
            autoSend = new Redirection();
        }
        return autoSend;
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    public static FileSystem getFileSystem() {
        if (fileSystem == null) {
            fileSystem = new FileSystem();
        }
        return fileSystem;
    }

    public static LocatorModel getLocator() {
        if (locator == null) {
            locator = new LocatorModel();
        }
        return locator;
    }

    public static BluetoothManager getBluetoothManager() {
        if (bluetoothManager == null) {
            bluetoothManager = new BluetoothManager();
        }
        return bluetoothManager;
    }

    public static PostData getPostData() {
        if (postData == null) {
            postData = new PostData();
        }
        return postData;
    }

    public static FileSystemScreen getFileSystemScreen() {
        if (fileSystemScreen == null) {
            fileSystemScreen = new FileSystemScreen();
        }
        return fileSystemScreen;
    }

    public static InternalURLManager getURL() {
        if (internalUrlManager == null) {
            internalUrlManager = new InternalURLManager();
        }
        return internalUrlManager;
    }

    public static LoadingScreen getLoading() {
        if (loadingScreen == null) {
            loadingScreen = new LoadingScreen();
        }
        return loadingScreen;
    }

    public static void destroyLoading() {
        loadingScreen = null;
    }

    public static TopBarBackground getTopBar() {
        return topBar;
    }

    public static void setTopBar(TopBarBackground tb) {
        topBar = tb;
        R.getLocator().addLocationChangeListener(topBar);
    }

    public static LogScreen getLoggerScreen() {
        if (loggerScreen == null) {
            loggerScreen = new LogScreen();
        }
        return loggerScreen;
    }

    public static ConnectionProblem getConnectionProblem() {
        if (connectionProblem == null) {
            connectionProblem = new ConnectionProblem();
        }
        return connectionProblem;
    }

    public static LocationScreens getLocationScreens() {
        if (locationScreens == null) {
            locationScreens = new LocationScreens();
        }
        return locationScreens;
    }

    public static LocationContext getContext() {
        if (locationContext == null) {
            locationContext = new LocationContext();
        }
        return locationContext;
    }

    public static SettingsScreen getSettingsScreen() {
        if (settingsScreen == null) {
            settingsScreen = new SettingsScreen();
        }
        return settingsScreen;
    }

    public static ServiceManager getServiceManager() {
        if (serviceManager == null) {
            serviceManager = new ServiceManager();
        }
        return serviceManager;
    }

    public static Sync getSync() {
        if (sync == null) {
            sync = new Sync();
        }
        return sync;
    }

    public static UpdateScreen getUpdate() {
        if (update == null) {
            update = new UpdateScreen();
        }
        return update;
    }

    public static ExternalBrowser getExternalBrowser() {
        if (external == null) {
            external = new ExternalBrowser();
        }
        return external;
    }

    public static ErrorScreen getErrorScreen() {
        if (errorScreen == null) {
            errorScreen = new ErrorScreen();
        }
        return errorScreen;
    }

    public static FirstRunManager getFirstRun() {
        if (firstRun == null) {
            firstRun = new FirstRunManager();
        }
        return firstRun;
    }

    public static ListScreen getCustomList() {
        if (customList == null) {
            customList = new ListScreen();
        }
        return customList;
    }

    public static PlaceSaveScreen getSavePlace() {
        if (savePlace == null) {
            savePlace = new PlaceSaveScreen();
        }
        return savePlace;
    }

    public static FileBrowser getFileBrowser() {
        if (fileBrowser == null) {
            fileBrowser = new FileBrowser();
        }
        return fileBrowser;
    }

    public static UploadProgressScreen getUploadProgress() {
        if (progress == null) {
            progress = new UploadProgressScreen();
        }
        return progress;
    }

    public static SatelliteScreen getSatelliteScreen() {
        if (satelliteScreen == null) {
            satelliteScreen = new SatelliteScreen();
        }
        return satelliteScreen;
    }
}
