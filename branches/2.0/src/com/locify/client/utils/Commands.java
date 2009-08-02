/*
 * Commands.java
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

import com.locify.client.data.IconData;
import com.sun.lwuit.Command;


/**
 * Stores some commands which are used everywhere in Locify
 * @author Destil
 */
public class Commands {

    /*************** KEEP SORTED ***************/
    public static Command cmdAnotherLocation;
    public static Command cmdBack;
    public static Command cmdBacklightOff;
    public static Command cmdBacklightOn;
    public static Command cmdCancel;
    public static Command cmdContactEmail;
    public static Command cmdContactTel;
    public static Command cmdContinue;
    public static Command cmdDelete;
    public static Command cmdExit;
    public static Command cmdHome;
    public static Command cmdHowto;
    public static Command cmdNavigate;
    public static Command cmdNo;
    public static Command cmdOK;
    public static Command cmdSave;
    public static Command cmdSelect;
    public static Command cmdSend;
    public static Command cmdSet;
    public static Command cmdStop;
    public static Command cmdView;
    public static Command cmdYes;

    // commands only for mainScreen
    public static Command cmdAdd;
    public static Command cmdAddByLink;
    public static Command cmdAddService;
    public static Command cmdMoreInfo;
    public static Command cmdMove;
    public static Command cmdMoveTo;
    public static Command cmdRename;
    public static Command cmdService;
    public static Command cmdServiceSettings;
    public static Command cmdShortcut;
    public static Command cmdUpdateService;

    // commands only for map screen
    public static Command cmdChangeMapFile;
    public static Command cmdChangeMapTile;
    public static Command cmdItemManager;
    public static Command cmdMapFunction;
    public static Command cmdMyLocation;
    public static Command cmdZoomIn;
    public static Command cmdZoomOut;

    // commands for offline maps
    public static Command cmdSelectAndCenter;
    public static Command cmdOnlineMaps;
    public static Command cmdInitialize;
    public static Command cmdSearchMaps;

    // navigation screen commands
    public static Command cmdMore;
    public static Command cmdLess;

    // route screen commands
    public static Command cmdStyle;
    public static Command cmdStyleSimple;
    public static Command cmdStyleExtended;
    public static Command cmdStyleGraphs;
    public static Command cmdAction;
    public static Command cmdActionStart;
    public static Command cmdActionStop;
    public static Command cmdActionReset;
    public static Command cmdActionPause;
    public static Command cmdActionResume;

    // geoFileBrowser commands
    public static Command cmdMap;
    public static Command cmdMapAll;
    public static Command cmdExport;
    public static Command cmdNavigateToFirst;
    public static Command cmdNavigateToLast;
    public static Command cmdNavigateAlong;
    public static Command cmdNavigateToNearest;
    public static Command cmdExportFirst;
    public static Command cmdExportLast;

    //#if planstudio
//#     public static Command cmdPlanStudio;
    //#endif

    public static void initializeCommands() {
        cmdAnotherLocation = new Command(Locale.get("Another_location"), 10);
        cmdBack = new Command(Locale.get("Back"), IconData.getLocalImage("back"), 3);
        cmdBacklightOff = new Command(Locale.get("Backlight_off"), 20);
        cmdBacklightOn = new Command(Locale.get("Backlight_on"), 20);
        cmdCancel = new Command(Locale.get("Cancel"), 5);
        cmdContactEmail = new Command(Locale.get("Email"), 0);
        cmdContactTel = new Command(Locale.get("Tel"), 0);
        cmdContinue = new Command(Locale.get("Continue"), 2);
        cmdDelete = new Command(Locale.get("Delete"), IconData.getLocalImage("delete"), 5);
        cmdExit = new Command(Locale.get("Exit"), IconData.getLocalImage("exit"), 3);
        cmdHome = new Command(Locale.get("Home"), IconData.getLocalImage("home"), 50);
        cmdHowto = new Command(Locale.get("Howto"), 1);
        cmdNavigate = new Command(Locale.get("Navigate"), 3);
        cmdNo = new Command(Locale.get("No"), 2);
        cmdOK = new Command(Locale.get("OK"), 3);
        cmdSave = new Command(Locale.get("Save"), 4);
        cmdSelect = new Command(Locale.get("Select"), IconData.getLocalImage("select"), 0);
        cmdSend = new Command(Locale.get("Send"), 1);
        cmdSet = new Command(Locale.get("Set"), 1);
        cmdStop = new Command(Locale.get("Stop"));
        cmdView = new Command(Locale.get("View"), 2);
        cmdYes = new Command(Locale.get("Yes"), 1);
        // commands only for mainScreen
        cmdAdd = new Command(Locale.get("Add"), IconData.getLocalImage("add"), 13);
        cmdAddByLink = new Command(Locale.get("Service_by_link"), IconData.getLocalImage("addFromLink"), 11);
        cmdAddService = new Command(Locale.get("Service_from_list"), IconData.getLocalImage("addFromList"), 10);
        cmdMoreInfo = new Command(Locale.get("More_info"), IconData.getLocalImage("moreInfo"), 10);
        cmdMove = new Command(Locale.get("Move"), IconData.getLocalImage("move"), 14);
        cmdMoveTo = new Command(Locale.get("Move"), 1);
        cmdRename = new Command(Locale.get("Rename"), IconData.getLocalImage("renameService"), 13);
        cmdService = new Command(Locale.get("Service"),IconData.getLocalImage("manage"), 11);
        cmdServiceSettings = new Command(Locale.get("Settings"), IconData.getLocalImage("serviceSettings"), 11);
        cmdShortcut = new Command(Locale.get("Shortcut"), 11);
        cmdUpdateService = new Command(Locale.get("Update_service"), IconData.getLocalImage("updateService"), 12);
        // commands only for map screen
        cmdChangeMapFile = new Command(Locale.get("Change_map_file"), 6);
        cmdChangeMapTile = new Command(Locale.get("Change_map_tile"), 5);
        cmdItemManager = new Command(Locale.get("Item_manager"), 7);
        cmdMapFunction = new Command(Locale.get("Map_function"), 1);
        cmdMyLocation = new Command(Locale.get("My_location"), 4);
        cmdZoomIn = new Command(Locale.get("Zoom_in"), 2);
        cmdZoomOut = new Command(Locale.get("Zoom_out"), 3);
        // commands for offline maps
        cmdSelectAndCenter = new Command(Locale.get("Select_and_center"), 2);
        cmdOnlineMaps = new Command(Locale.get("Online_maps"), 3);
        cmdInitialize = new Command(Locale.get("Initialize_maps"), 4);
        cmdSearchMaps = new Command(Locale.get("Find_maps"), 5);
        // navigation screen commands
        cmdMore = new Command(Locale.get("Navi_more"));
        cmdLess = new Command(Locale.get("Navi_less"));
        // route screen commands
        cmdStyle = new Command(Locale.get("Style"), 2);
        cmdStyleSimple = new Command(Locale.get("Simple_style"), 2);
        cmdStyleExtended = new Command(Locale.get("Extended_style"), 2);
        cmdStyleGraphs = new Command(Locale.get("Graphs_style"), 2);
        cmdAction = new Command(Locale.get("Record_route"), 2);
        cmdActionStart = new Command(Locale.get("Start_action"), 2);
        cmdActionStop = new Command(Locale.get("Stop_action"), 2);
        cmdActionReset = new Command(Locale.get("Reset_route"), 2);
        cmdActionPause = new Command(Locale.get("Pause_action"), 2);
        cmdActionResume = new Command(Locale.get("Resume_action"), 2);
        // geoFileBrowser commands
        cmdMap = new Command(Locale.get("Show_on_map"), 10);
        cmdMapAll = new Command(Locale.get("Show_on_map_all"), 11);
        cmdExport = new Command(Locale.get("To_landmarks"), 20);
        cmdNavigateToFirst = new Command(Locale.get("Navigate_to_first_point"), 31);
        cmdNavigateToLast = new Command(Locale.get("Navigate_to_last_point"), 32);
        cmdNavigateAlong = new Command(Locale.get("Navigate_along"), 30);
        cmdNavigateToNearest = new Command(Locale.get("Navigate_to_nearest"), 33);
        cmdExportFirst = new Command(Locale.get("First_point"), 21);
        cmdExportLast = new Command(Locale.get("Last_point"), 22);
        //#if planstudio
//#         cmdPlanStudio = new Command("PlanStudio", 7);
        //#endif

        //#if planstudio
//#         cmdPlanStudio = new Command("PlanStudio", 7);
        //#endif
    }
}
