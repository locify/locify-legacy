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

import com.locify.client.data.IconData;
import com.locify.client.data.SettingsData;
import com.locify.client.maps.FileMapLayer;
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
import javax.microedition.lcdui.TextField;

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
    private StringItem btnSaveMap;
    private StringItem siAdvancedMaps;
    private Form frmAdvancedMaps;
    private ChoiceGroup cgPanning;
    private TextField tfCacheSize;
    private StringItem btnSaveAdvancedMaps;
    // maps variables
    private Form frmMaps;
    private ChoiceGroup cgMapProvider;

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
        if (R.getSettings().getPrefferedGps() > names.length) {
            R.getSettings().saveLocationSettings(SettingsData.AUTODETECT, R.getSettings().getCoordsFormat());
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

    public void viewMapSettings() {
        frmMaps = new Form(Locale.get("Maps"));

        cgMapProvider = new ChoiceGroup(Locale.get("Default_map_provider"), Choice.EXCLUSIVE);
        Vector providers = (new TileMapLayer(null)).getProvidersAndModes();
        int onlineProviders = providers.size();
        for (int i = 0; i < providers.size(); i++) {
            cgMapProvider.append((String) providers.elementAt(i), IconData.get("locify://icons/online.png"));
        }
        providers = (new FileMapLayer(null)).getProvidersAndModes();
        for (int i = 0; i < providers.size(); i++) {
            cgMapProvider.append((String) providers.elementAt(i), IconData.get("locify://icons/saved.png"));
        }
        if (R.getSettings().isDefaultMapProviderOnline()) {
            cgMapProvider.setSelectedIndex(R.getSettings().getDefaultMapProvider(), true);
        } else {
            cgMapProvider.setSelectedIndex(onlineProviders + R.getSettings().getDefaultMapProvider(), true);
        }
        frmMaps.append(cgMapProvider);

        frmMaps.addCommand(Commands.cmdBack);
        //#style imgHome
        frmMaps.addCommand(Commands.cmdHome);
        frmMaps.setCommandListener(this);

        btnSaveMap = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
        btnSaveMap.setDefaultCommand(Commands.cmdSave);
        btnSaveMap.setItemCommandListener(this);
        frmMaps.append(btnSaveMap);

        siAdvancedMaps = new StringItem("",Locale.get("Advanced_settings"),StringItem.HYPERLINK);
        siAdvancedMaps.setDefaultCommand(Commands.cmdView);
        siAdvancedMaps.setItemCommandListener(this);
        frmMaps.append(siAdvancedMaps);

        R.getMidlet().switchDisplayable(null, frmMaps);
    }

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

    public void viewAdvancedMapSettings() {
        frmAdvancedMaps = new Form(Locale.get("Advanced_settings"));

        tfCacheSize = new TextField(Locale.get("Cache_size"),String.valueOf(R.getSettings().getCacheSize()),10,TextField.NUMERIC);
        frmAdvancedMaps.append(tfCacheSize);

        cgPanning = new ChoiceGroup(Locale.get("Panning_behaviour"), Choice.EXCLUSIVE);
        cgPanning.append(Locale.get("Repaint_during"), null);
        cgPanning.append(Locale.get("Wait_until_end_of_panning"), null);
        cgPanning.setSelectedIndex(R.getSettings().getPanning(), true);
        frmAdvancedMaps.append(cgPanning);

        btnSaveAdvancedMaps = new StringItem("", Locale.get("Save"), StringItem.BUTTON);
        btnSaveAdvancedMaps.setDefaultCommand(Commands.cmdSave);
        btnSaveAdvancedMaps.setItemCommandListener(this);
        frmAdvancedMaps.append(btnSaveAdvancedMaps);

        frmAdvancedMaps.addCommand(Commands.cmdBack);
        //#style imgHome
        frmAdvancedMaps.addCommand(Commands.cmdHome);
        frmAdvancedMaps.setCommandListener(this);

        R.getMidlet().switchDisplayable(null, frmAdvancedMaps);
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
        }
    }

    public void commandAction(Command command, Item item) {
        if (item.equals(btnSaveLocation)) {
            R.getSettings().saveLocationSettings(cgPrefferedGps.getSelectedIndex(), cgCoordinatesFormat.getSelectedIndex());
        } else if (item.equals(btnSaveInterface)) {
            R.getSettings().saveInterfaceSettings(cgLanguage.getSelectedIndex());
        } else if (item.equals(btnSaveOther)) {
            R.getSettings().saveOtherSettings(cgAutoLogin.getSelectedIndex(), cgExternalClose.getSelectedIndex());
        } else if (item.equals(btnSaveMap)) {
            int onlineProviders = ((new TileMapLayer(null)).getProvidersAndModes()).size();
            if (cgMapProvider.getSelectedIndex() < onlineProviders) {
                R.getSettings().saveMapsSettings(cgMapProvider.getSelectedIndex(), false);
            } else {
                R.getSettings().saveMapsSettings(cgMapProvider.getSelectedIndex() - onlineProviders, true);
            }
        } else if (item.equals(siAdvancedMaps))
        {
            viewAdvancedMapSettings();
        } else if (item.equals(btnSaveAdvancedMaps))
        {
            R.getSettings().saveAdvancedMaps(Integer.parseInt(tfCacheSize.getString()),cgPanning.getSelectedIndex());
        }
    }
}
