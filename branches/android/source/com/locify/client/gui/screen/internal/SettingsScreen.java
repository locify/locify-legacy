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
import de.enough.polish.ui.Choice; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.ChoiceGroup; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Form; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Item; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.ItemCommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.List; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.StringItem; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField; import de.enough.polish.ui.StyleSheet;

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
    private ChoiceGroup cgUnits;
    private ChoiceGroup cgBacklight;
    private TextField tfBacklightFrequency;
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
    private ChoiceGroup cgAutoload;
    private ChoiceGroup cgMapLoading;
    private ChoiceGroup cgFilecache;
    private TextField tfCacheSize;
    private StringItem btnSaveAdvancedMaps;
    // maps variables
    private Form frmMaps;
    private ChoiceGroup cgMapProvider;

    public SettingsScreen() {
    }

    public void view() {
        lstSettings = new List(Locale.get(138), List.IMPLICIT);
        lstSettings.append(Locale.get(262), null);
        lstSettings.append(Locale.get(261), null);
        lstSettings.append(Locale.get(252), null);
        lstSettings.append(Locale.get(264), null);
        lstSettings.addCommand(Commands.cmdBack);
        //#style imgHome
        lstSettings.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        lstSettings.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, lstSettings);
    }

    public void viewLocationSettings() {
        frmLocation = new Form(Locale.get(262));

        cgPrefferedGps = new ChoiceGroup(Locale.get(265), Choice.EXCLUSIVE);
        cgPrefferedGps.append(Locale.get(260), null);
        String[] names = R.getLocator().getProviderNames();
        for (int i = 0; i < names.length; i++) {
            cgPrefferedGps.append(names[i], null);
        }
        if (R.getSettings().getPrefferedGps() > names.length) {
            R.getSettings().saveLocationSettings(SettingsData.AUTODETECT, R.getSettings().getCoordsFormat());
        }
        cgPrefferedGps.setSelectedIndex(R.getSettings().getPrefferedGps(), true);
        frmLocation.append(cgPrefferedGps);

        cgCoordinatesFormat = new ChoiceGroup(Locale.get(31), Choice.EXCLUSIVE);
        cgCoordinatesFormat.append(Locale.get(35), null);
        cgCoordinatesFormat.append(Locale.get(37), null);
        cgCoordinatesFormat.append(Locale.get(38), null);
        cgCoordinatesFormat.setSelectedIndex(R.getSettings().getCoordsFormat(), true);
        frmLocation.append(cgCoordinatesFormat);

        btnSaveLocation = new StringItem("", Locale.get(126), StringItem.BUTTON);
        btnSaveLocation.setDefaultCommand(Commands.cmdSave);
        btnSaveLocation.setItemCommandListener(this);
        frmLocation.append(btnSaveLocation);

        frmLocation.addCommand(Commands.cmdBack);
        //#style imgHome
        frmLocation.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmLocation.setCommandListener(this);

        R.getMidlet().switchDisplayable(null, frmLocation);
    }

    public void viewInterfaceSettings() {
        frmInterface = new Form(Locale.get(261));

        cgLanguage = new ChoiceGroup(Locale.get(238), Choice.EXCLUSIVE);
        for (int i = 0; i < R.getSettings().locales.length; i++) {
            cgLanguage.append(R.getSettings().languageNames[i], null);
        }
        cgLanguage.setSelectedIndex(R.getSettings().getSelectedLanguage(), true);
        frmInterface.append(cgLanguage);

        cgUnits = new ChoiceGroup(Locale.get(503), Choice.EXCLUSIVE);
        cgUnits.append(Locale.get(502), null);
        cgUnits.append(Locale.get(501), null);
        cgUnits.setSelectedIndex(R.getSettings().getUnits(), true);
        frmInterface.append(cgUnits);

        cgBacklight = new ChoiceGroup(Locale.get(519), Choice.EXCLUSIVE);
        cgBacklight.append(Locale.get(518), null);
        cgBacklight.append(Locale.get(252), null);
        cgBacklight.append(Locale.get(107), null);
        cgBacklight.append(Locale.get(517), null);
        cgBacklight.append(Locale.get(520), null);
        cgBacklight.setSelectedIndex(R.getSettings().getBacklight(), true);
        frmInterface.append(cgBacklight);

        tfBacklightFrequency = new TextField(Locale.get(516),String.valueOf(R.getSettings().getBacklightFrequency()),10,TextField.NUMERIC);
        frmInterface.append(tfBacklightFrequency);

        btnSaveInterface = new StringItem("", Locale.get(126), StringItem.BUTTON);
        btnSaveInterface.setDefaultCommand(Commands.cmdSave);
        btnSaveInterface.setItemCommandListener(this);
        frmInterface.append(btnSaveInterface);

        frmInterface.addCommand(Commands.cmdBack);
        //#style imgHome
        frmInterface.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmInterface.setCommandListener(this);

        R.getMidlet().switchDisplayable(null, frmInterface);
    }

    public void viewMapSettings() {
        frmMaps = new Form(Locale.get(252));

        cgMapProvider = new ChoiceGroup(Locale.get(367), Choice.EXCLUSIVE);
        Vector providers = (new TileMapLayer(null)).getProvidersAndModes();
        int onlineProviders = providers.size();
        for (int i = 0; i < providers.size(); i++) {
            cgMapProvider.append((String) providers.elementAt(i), IconData.get("locify://icons/online.png"));
        }

        cgMapProvider.append(Locale.get(505), IconData.get("locify://icons/saved.png"));

        if (R.getSettings().isDefaultMapProviderOnline()) {
            cgMapProvider.setSelectedIndex(R.getSettings().getDefaultMapProvider(), true);
        } else {
            cgMapProvider.setSelectedIndex(onlineProviders, true);
        }
        frmMaps.append(cgMapProvider);

        frmMaps.addCommand(Commands.cmdBack);
        //#style imgHome
        frmMaps.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmMaps.setCommandListener(this);

        btnSaveMap = new StringItem("", Locale.get(126), StringItem.BUTTON);
        btnSaveMap.setDefaultCommand(Commands.cmdSave);
        btnSaveMap.setItemCommandListener(this);
        frmMaps.append(btnSaveMap);

        siAdvancedMaps = new StringItem("",Locale.get(475),StringItem.HYPERLINK);
        siAdvancedMaps.setDefaultCommand(Commands.cmdView);
        siAdvancedMaps.setItemCommandListener(this);
        //#style a
        frmMaps.append(siAdvancedMaps, de.enough.polish.ui.StyleSheet.aStyle );

        R.getMidlet().switchDisplayable(null, frmMaps);
    }

    public void viewOtherSettings() {
        frmOther = new Form(Locale.get(264));

        cgAutoLogin = new ChoiceGroup(Locale.get(219), Choice.EXCLUSIVE);
        cgAutoLogin.append(Locale.get(221), null);
        cgAutoLogin.append(Locale.get(220), null);
        cgAutoLogin.setSelectedIndex(R.getSettings().getAutoLogin(), true);
        frmOther.append(cgAutoLogin);

        cgExternalClose = new ChoiceGroup(Locale.get(20), Choice.EXCLUSIVE);
        cgExternalClose.append(Locale.get(12), null);
        cgExternalClose.append(Locale.get(108), null);
        cgExternalClose.append(Locale.get(13), null);
        cgExternalClose.setSelectedIndex(R.getSettings().getExternalClose(), true);
        frmOther.append(cgExternalClose);

        btnSaveOther = new StringItem("", Locale.get(126), StringItem.BUTTON);
        btnSaveOther.setDefaultCommand(Commands.cmdSave);
        btnSaveOther.setItemCommandListener(this);
        frmOther.append(btnSaveOther);

        frmOther.addCommand(Commands.cmdBack);
        //#style imgHome
        frmOther.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmOther.setCommandListener(this);

        R.getMidlet().switchDisplayable(null, frmOther);
    }

    public void viewAdvancedMapSettings() {
        frmAdvancedMaps = new Form(Locale.get(475));

        cgAutoload = new ChoiceGroup(Locale.get(500), Choice.EXCLUSIVE);
        cgAutoload.append(Locale.get(221), null);
        cgAutoload.append(Locale.get(220), null);
        cgAutoload.setSelectedIndex(R.getSettings().getAutoload(), true);
        frmAdvancedMaps.append(cgAutoload);

        cgFilecache = new ChoiceGroup(Locale.get(527), Choice.EXCLUSIVE);
        cgFilecache.append(Locale.get(221), null);
        cgFilecache.append(Locale.get(220), null);
        cgFilecache.setSelectedIndex(R.getSettings().getFilecache(), true);
        frmAdvancedMaps.append(cgFilecache);

        tfCacheSize = new TextField(Locale.get(476),String.valueOf(R.getSettings().getFilecacheSize()),10,TextField.NUMERIC);
        frmAdvancedMaps.append(tfCacheSize);

        cgPanning = new ChoiceGroup(Locale.get(477), Choice.EXCLUSIVE);
        cgPanning.append(Locale.get(478), null);
        cgPanning.append(Locale.get(480), null);
        cgPanning.setSelectedIndex(R.getSettings().getPanning(), true);
        frmAdvancedMaps.append(cgPanning);

        cgMapLoading = new ChoiceGroup(Locale.get(509), Choice.EXCLUSIVE);
        cgMapLoading.append(Locale.get(510), null);
        cgMapLoading.append(Locale.get(511), null);
        cgMapLoading.setSelectedIndex(R.getSettings().getMapLoading(), true);
        frmAdvancedMaps.append(cgMapLoading);

        btnSaveAdvancedMaps = new StringItem("", Locale.get(126), StringItem.BUTTON);
        btnSaveAdvancedMaps.setDefaultCommand(Commands.cmdSave);
        btnSaveAdvancedMaps.setItemCommandListener(this);
        frmAdvancedMaps.append(btnSaveAdvancedMaps);

        frmAdvancedMaps.addCommand(Commands.cmdBack);
        //#style imgHome
        frmAdvancedMaps.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
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
            R.getSettings().saveInterfaceSettings(cgLanguage.getSelectedIndex(), cgUnits.getSelectedIndex(), cgBacklight.getSelectedIndex(), Integer.parseInt(tfBacklightFrequency.getString()));
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
            R.getSettings().saveAdvancedMaps(cgAutoload.getSelectedIndex(),cgFilecache.getSelectedIndex(),Integer.parseInt(tfCacheSize.getString()),cgPanning.getSelectedIndex(),cgMapLoading.getSelectedIndex());
        }
    }
}
