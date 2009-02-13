/*
 * SettingsScreen.java
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

import com.locify.client.maps.TileMapLayer;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import java.util.Vector;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;

/**
 * Views Locify settings
 * @author Destil
 */
public class SettingsScreen implements CommandListener, ItemCommandListener {

    private List lstSettings;
    private Form frmLocation;
    private ChoiceGroup cgPrefferedGps;
    private ChoiceGroup cgCoordinatesFormat;
    private Form frmInterface;
    private ChoiceGroup cgLanguage;
    private Form frmOther;
    private ChoiceGroup cgAutoLogin;
    private ChoiceGroup cgExternalClose;
    private StringItem btnSaveOther;
    private StringItem btnSaveInterface;
    private StringItem btnSaveLocation;
    private StringItem btnSaveMapOnline;
    private StringItem btnSaveMapFile;
    private StringItem btnSaveMapSettings;
    // maps variables
    //private List lstMaps;
    //private Form frmMaps;
    private Form frmMapsOnline;
    //private Form frmMapsFile;
    //private Form frmMapsSettings;
    private ChoiceGroup cgMapProvider;
    private ChoiceGroup cgFileMapProvider;
    private ChoiceGroup cgMapItems;
    
    public SettingsScreen() {
    }

    public void view() {
        lstSettings = new List(Locale.get("Settings"), List.IMPLICIT);
        lstSettings.append(Locale.get("Location"), null);
        lstSettings.append(Locale.get("Interface"), null);
        lstSettings.append(Locale.get("Maps"), null);
        lstSettings.append(Locale.get("Other"), null);
        lstSettings.addCommand(Commands.cmdBack);
        //#style imgHome
        lstSettings.addCommand(Commands.cmdHome);
        lstSettings.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, lstSettings);
    }

    public void viewLocationSettings() {
        frmLocation = new Form(Locale.get("Location"));

        cgPrefferedGps = new ChoiceGroup(Locale.get("Preffered_gps"), Choice.EXCLUSIVE);
        cgPrefferedGps.append(Locale.get("Autodetect"), null);
        String[] names = R.getLocator().getProviderNames();
        for (int i = 0; i < names.length; i++) {
            cgPrefferedGps.append(names[i], null);
        }
        cgPrefferedGps.setSelectedIndex(R.getSettings().getPrefferedGps(), true);
        frmLocation.append(cgPrefferedGps);

        cgCoordinatesFormat = new ChoiceGroup(Locale.get("Coordinates_format"), Choice.EXCLUSIVE);
        cgCoordinatesFormat.append(Locale.get("Decimal_degrees"), null);
        cgCoordinatesFormat.append(Locale.get("Degrees_and_minutes"), null);
        cgCoordinatesFormat.append(Locale.get("Degrees_minutes_seconds"), null);
        cgCoordinatesFormat.setSelectedIndex(R.getSettings().getCoordsFormat(), true);
        frmLocation.append(cgCoordinatesFormat);

        btnSaveLocation = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
        btnSaveLocation.setDefaultCommand(Commands.cmdSave);
        btnSaveLocation.setItemCommandListener(this);
        frmLocation.append(btnSaveLocation);

        frmLocation.addCommand(Commands.cmdBack);
        //#style imgHome
        frmLocation.addCommand(Commands.cmdHome);
        frmLocation.setCommandListener(this);

        R.getMidlet().switchDisplayable(null, frmLocation);
    }

    public void viewInterfaceSettings() {
        frmInterface = new Form(Locale.get("Interface"));

        cgLanguage = new ChoiceGroup(Locale.get("Language"), Choice.EXCLUSIVE);
        for (int i = 0; i < R.getSettings().locales.length; i++) {
            cgLanguage.append(R.getSettings().languageNames[i], null);
        }
        cgLanguage.setSelectedIndex(R.getSettings().getSelectedLanguage(), true);
        frmInterface.append(cgLanguage);

        btnSaveInterface = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
        btnSaveInterface.setDefaultCommand(Commands.cmdSave);
        btnSaveInterface.setItemCommandListener(this);
        frmInterface.append(btnSaveInterface);

        frmInterface.addCommand(Commands.cmdBack);
        //#style imgHome
        frmInterface.addCommand(Commands.cmdHome);
        frmInterface.setCommandListener(this);

        R.getMidlet().switchDisplayable(null, frmInterface);
    }

//    public void viewMapSettings() {
//        frmMaps = new Form(Locale.get("Maps"));
//
//        cgMapProvider = new ChoiceGroup(Locale.get("Default_map_provider"), Choice.EXCLUSIVE);
//        Vector providers = (new TileMapLayer(null)).getProvidersAndModes();
//        for (int i = 0; i < providers.size(); i++) {
//            cgMapProvider.append((String)providers.elementAt(i), null);
//        }
//        cgMapProvider.setSelectedIndex(R.getSettings().getDefaultOnlineMapProvider(), true);
//        frmMaps.append(cgMapProvider);
//
//        cgMapItems = new ChoiceGroup(Locale.get("Map_items"), Choice.MULTIPLE);
//        MapItemManager manager = R.getMapItemManager();
//        for (int i = 0; i < manager.getItemCount(); i++) {
//            String itemName = manager.getItemName(i);
//            cgMapItems.append(itemName, null);
//            cgMapItems.setSelectedIndex(i, R.getSettings().getShowScale());
//        }
//        if (manager.getItemCount() > 0)
//            frmMaps.append(cgMapItems);
//
//        frmMaps.addCommand(Commands.cmdBack);
//        //#style imgHome
//        frmMaps.addCommand(Commands.cmdHome);
//        frmMaps.setCommandListener(this);
//
//        btnSaveMap = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
//        btnSaveMap.setDefaultCommand(Commands.cmdSave);
//        btnSaveMap.setItemCommandListener(this);
//        frmMaps.append(btnSaveMap);
//
//        R.getMidlet().switchDisplayable(null, frmMaps);
//    }
    
    
    /**************************************/
    /*            MAP SETTINGS            */
    /**************************************/

//    public void viewMapSettings() {
//        lstMaps = new List(Locale.get("Maps"), List.IMPLICIT);
//        lstMaps.append(Locale.get("Change_map_tile"), null);
//        lstMaps.append(Locale.get("Change_map_file"), null);
//        lstMaps.append(Locale.get("Settings"), null);
//        lstMaps.addCommand(Commands.cmdBack);
//        lstMaps.addCommand(Commands.cmdHome);
//        lstMaps.setCommandListener(this);
//        R.getMidlet().switchDisplayable(null, lstMaps);
//    }

    public void viewMapSettings() {
        frmMapsOnline = new Form(Locale.get("Change_map_tile"));

        cgMapProvider = new ChoiceGroup(Locale.get("Default_map_provider"), Choice.EXCLUSIVE);
        Vector providers = (new TileMapLayer(null)).getProvidersAndModes();
        for (int i = 0; i < providers.size(); i++) {
            cgMapProvider.append((String)providers.elementAt(i), null);
        }
        cgMapProvider.setSelectedIndex(R.getSettings().getDefaultOnlineMapProvider(), true);
        frmMapsOnline.append(cgMapProvider);

        frmMapsOnline.addCommand(Commands.cmdBack);
        //#style imgHome
        frmMapsOnline.addCommand(Commands.cmdHome);
        frmMapsOnline.setCommandListener(this);

        btnSaveMapOnline = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
        btnSaveMapOnline.setDefaultCommand(Commands.cmdSave);
        btnSaveMapOnline.setItemCommandListener(this);
        frmMapsOnline.append(btnSaveMapOnline);

        R.getMidlet().switchDisplayable(null, frmMapsOnline);
    }

//    public void viewMapSettingsFileMaps() {
//        frmMapsFile = new Form(Locale.get("Change_map_file"));
//
//        cgFileMapProvider = new ChoiceGroup(Locale.get("Change"), Choice.MULTIPLE);
//        for (int i = 0; i < R.getSettings().getFileMapProviders().getNumOfProviders(); i++) {
//            String name = R.getSettings().getFileMapProviders().getProviderName(i);
//            cgFileMapProvider.append(name, null);
//            cgFileMapProvider.setSelectedIndex(i, R.getSettings().getFileMapEnable(name));
//        }
//
//        if (cgFileMapProvider.size() > 0)
//            frmMapsFile.append(cgFileMapProvider);
//
//        frmMapsFile.addCommand(Commands.cmdBack);
//        //#style imgHome
//        frmMapsFile.addCommand(Commands.cmdHome);
//        frmMapsFile.setCommandListener(this);
//
//        btnSaveMapFile = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
//        btnSaveMapFile.setDefaultCommand(Commands.cmdSave);
//        btnSaveMapFile.setItemCommandListener(this);
//        frmMapsFile.append(btnSaveMapFile);
//
//        R.getMidlet().switchDisplayable(null, frmMapsFile);
//    }
//
//    public void viewMapSettingsSettings() {
//        frmMapsSettings = new Form(Locale.get("Settings"));
//
//        cgMapItems = new ChoiceGroup(Locale.get("Map_items"), Choice.MULTIPLE);
//        MapItemManager manager = R.getMapItemManager();
//        for (int i = 0; i < manager.getItemCount(); i++) {
//            String itemName = manager.getItemName(i);
//            cgMapItems.append(itemName, null);
//            if (i == 0)
//                cgMapItems.setSelectedIndex(i, R.getSettings().getShowScale());
//            else
//                cgMapItems.setSelectedIndex(i, R.getMapItemManager().isEnabled(itemName));
//        }
//        if (manager.getItemCount() > 0)
//            frmMapsSettings.append(cgMapItems);
//
//        frmMapsSettings.addCommand(Commands.cmdBack);
//
//        ChoiceTextField cft = new ChoiceTextField("po", "text", 20, ChoiceTextField.ANY, (new String[]{"aaa","bbb","ccc"}), false);
//        ChoiceGroup cg = new ChoiceGroup("p",Choice.POPUP);
//        cg.append("sss", null);
//        cg.append("sss", null);
//        cg.append("sss", null);
//
//        frmMaps.append(cg);
//
//        frmMaps.addCommand(Commands.cmdBack);
//        //#style imgHome
//        frmMapsSettings.addCommand(Commands.cmdHome);
//        frmMapsSettings.setCommandListener(this);
//
//        btnSaveMapSettings = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
//        btnSaveMapSettings.setDefaultCommand(Commands.cmdSave);
//        btnSaveMapSettings.setItemCommandListener(this);
//        frmMapsSettings.append(btnSaveMapSettings);
//
//        R.getMidlet().switchDisplayable(null, frmMapsSettings);
//    }

//    /**
//     *
//     * @param label
//     * @param item index of item in FileMapList, or negative value if add item selected
//
//    private void viewMapSettingsEdit(String label, int item) {
//        if (item >= R.getSettings().getFileMapProviders().getNumOfProviders()) {
//            selectedItem = -1;
//            return;
//        }
//
//        selectedItem = item;
//
//        frmMapsFileEdit = new Form(label);
//        cmdRemoveItem = new Command(Locale.get("Delete"), Command.ITEM, 5);
//
//        tfMapsFileEditName = new TextField(Locale.get("Name"), null, 100, TextField.ANY);
//        tfMapsFileEditPath = new TextField(Locale.get("Path"), null, 250, TextField.ANY);
//        cgDefaultFileMap = new ChoiceGroup("", Choice.MULTIPLE);
//        //cgDefaultFileMap.append(" " + Locale.get("Default"), null);
//
//        if (selectedItem >= 0) {
//            tfMapsFileEditName.setString(R.getSettings().getFileMapProviders().getProviderName(selectedItem));
//            tfMapsFileEditPath.setString(R.getSettings().getFileMapProviders().getProviderPath(selectedItem));
//            cgDefaultFileMap.setSelectedIndex(0, R.getSettings().getFileMapProviders().isDefaultProvider(item));
//            frmMapsFileEdit.addCommand(cmdRemoveItem);
//        }
//
//        frmMapsFileEdit.insert(0, tfMapsFileEditName);
//        frmMapsFileEdit.insert(1, tfMapsFileEditPath);
//        frmMapsFileEdit.insert(2, cgDefaultFileMap);
//
//        btnSaveProviderEdit = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
//        btnSaveProviderEdit.setDefaultCommand(Commands.cmdSave);
//        btnSaveProviderEdit.setItemCommandListener(this);
//        frmMapsFileEdit.append(btnSaveProviderEdit);
//
//        frmMapsFileEdit.addCommand(Commands.cmdBack);
//        frmMapsFileEdit.addCommand(Commands.cmdHome);
//        frmMapsFileEdit.setCommandListener(this);
//
//        R.getMidlet().switchDisplayable(null, frmMapsFileEdit);
//    }*/
    
    /****************************************/
    /*           END MAP SETTINGS           */
    /****************************************/
    
    
    public void viewOtherSettings() {
        frmOther = new Form(Locale.get("Other"));

        cgAutoLogin = new ChoiceGroup(Locale.get("Autologin"), Choice.EXCLUSIVE);
        cgAutoLogin.append(Locale.get("On"), null);
        cgAutoLogin.append(Locale.get("Off"), null);
        cgAutoLogin.setSelectedIndex(R.getSettings().getAutoLogin(), true);
        frmOther.append(cgAutoLogin);

        cgExternalClose = new ChoiceGroup(Locale.get("Close_app_on_external_browser"), Choice.EXCLUSIVE);
        cgExternalClose.append(Locale.get("Always"), null);
        cgExternalClose.append(Locale.get("Never"), null);
        cgExternalClose.append(Locale.get("Ask_every_time"), null);
        cgExternalClose.setSelectedIndex(R.getSettings().getExternalClose(), true);
        frmOther.append(cgExternalClose);

        btnSaveOther = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
        btnSaveOther.setDefaultCommand(Commands.cmdSave);
        btnSaveOther.setItemCommandListener(this);
        frmOther.append(btnSaveOther);

        frmOther.addCommand(Commands.cmdBack);
        //#style imgHome
        frmOther.addCommand(Commands.cmdHome);
        frmOther.setCommandListener(this);

        R.getMidlet().switchDisplayable(null, frmOther);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (displayable == lstSettings && command == List.SELECT_COMMAND) {
            int selected = lstSettings.getSelectedIndex();
            switch (selected) {
                case 0:
                    R.getURL().call("locify://settings/location");
                    break;
                case 1:
                    R.getURL().call("locify://settings/interface");
                    break;
               case 2:
                    R.getURL().call("locify://settings/maps");
                    break;
                case 3:
                    R.getURL().call("locify://settings/other");
                    break;
            }
//        } else if (displayable == lstMaps && command == List.SELECT_COMMAND) {
//            int selected = lstMaps.getSelectedIndex();
//            switch (selected) {
//                case 0:
//                    viewMapSettingsOnlineMaps();
//                    break;
//                case 1:
//                    viewMapSettingsFileMaps();
//                    break;
//                case 2:
//                    viewMapSettingsSettings();
//                    break;
//            }
        }
    }

    public void commandAction(Command command, Item item) {
        if (item.equals(btnSaveLocation)) {
            R.getSettings().saveLocationSettings(cgPrefferedGps.getSelectedIndex(), cgCoordinatesFormat.getSelectedIndex());
        } else if (item.equals(btnSaveInterface)) {
            R.getSettings().saveInterfaceSettings(cgLanguage.getSelectedIndex());
        } else if (item.equals(btnSaveOther)) {
            R.getSettings().saveOtherSettings(cgAutoLogin.getSelectedIndex(), cgExternalClose.getSelectedIndex());
        /*} else if (command.equals(cmdEditProvider)) {
            for (int i = 0; i < btnProviders.length; i++)
                if (btnProviders[i] == item) {
                    viewMapSettingsEdit(Locale.get("Edit"), i);
                    break;
                }
        } else if (item.equals(btnSaveProviderEdit)) {
            if (tfMapsFileEditName.getString() != null && tfMapsFileEditPath.getString() != null) {
                if (selectedItem >= 0) {
                    R.getSettings().getFileMapProviders().modifyProvider(selectedItem,
                            tfMapsFileEditName.getString(),
                            tfMapsFileEditPath.getString(),
                            cgDefaultFileMap.isSelected(0));
                } else {
                    R.getSettings().getFileMapProviders().addProvider(
                        tfMapsFileEditName.getString(),
                        tfMapsFileEditPath.getString(),
                        cgDefaultFileMap.isSelected(0));
                }
                R.getSettings().saveMapFileSettings();
            }
            R.getURL().call("locify://mapSettingsFile");*/
        } else if (item.equals(btnSaveMapOnline)) {
            R.getSettings().saveMapsOnline(cgMapProvider.getSelectedIndex());
        } else if (item.equals(btnSaveMapFile)) {
            R.getSettings().saveMapsFile(cgFileMapProvider);
        } else if (item.equals(btnSaveMapSettings)) {
            for (int i = 0; i < cgMapItems.size(); i++) {
                R.getMapItemManager().setEnabled(
                        R.getMapItemManager().getItemName(i),
                        cgMapItems.isSelected(i));
            }
            // first always "Scale"
            R.getSettings().saveMapsSettings(cgMapItems.isSelected(0));
        }
    }
}
