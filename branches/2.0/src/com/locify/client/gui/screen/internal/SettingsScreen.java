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
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ListLocify;
import com.locify.client.maps.TileMapLayer;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Button;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.Label;
import com.sun.lwuit.RadioButton;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import java.util.Vector;

/**
 * Views Locify settings
 * @author Destil
 */
public class SettingsScreen implements ActionListener {

    private ListLocify list;

    private ButtonGroup bgPrefferedGps;
    private ButtonGroup bgCoordinatesFormat;
    private Button btnSaveLocation;

    private ButtonGroup bgLanguage;
    private ButtonGroup bgUnits;
    private ButtonGroup bgBacklight;
    private TextArea taBacklightFrequency;
    private Button btnSaveInterface;

    private ButtonGroup bgMapProvider;
    private Button btnSaveMap;
    private Button btnAdvancedMaps;

    private ButtonGroup bgAutoLogin;
    private ButtonGroup bgExternalClose;
    private Button btnSaveOther;

    private ButtonGroup bgAutoload;
    private TextArea taCacheSize;
    private ButtonGroup bgFilecache;
    private ButtonGroup bgPanning;
    private ButtonGroup bgMapLoading;
    private Button btnSaveAdvancedMaps;

    public SettingsScreen() {
    }

    public void view() {
        FormLocify form = new FormLocify(Locale.get("Settings"));
        form.setLayout(new BorderLayout());

        list = new ListLocify();
        list.addItem(new Label(Locale.get("Location")));
        list.addItem(new Label(Locale.get("Interface")));
        list.addItem(new Label(Locale.get("Maps")));
        list.addItem(new Label(Locale.get("Other")));
        list.addActionListener(this);
        form.addComponent(BorderLayout.CENTER, list);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.addCommandListener(this);
        form.show();
    }

    public void viewLocationSettings() {
        FormLocify form = new FormLocify(Locale.get("Location"));
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));

        form.addComponent(new Label(Locale.get("Preffered_gps")));
        bgPrefferedGps = new ButtonGroup();
        bgPrefferedGps.add(new RadioButton(Locale.get("Autodetect"), null));
        String[] names = R.getLocator().getProviderNames();
        for (int i = 0; i < names.length; i++) {
            bgPrefferedGps.add(new RadioButton(names[i], null));
        }
        if (R.getSettings().getPrefferedGps() > names.length) {
            R.getSettings().saveLocationSettings(SettingsData.AUTODETECT, R.getSettings().getCoordsFormat());
        }
        bgPrefferedGps.setSelected(R.getSettings().getPrefferedGps());
        for (int i = 0; i < bgPrefferedGps.getButtonCount(); i++) {
            form.addComponent(bgPrefferedGps.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("Coordinates_format")));
        bgCoordinatesFormat = new ButtonGroup();
        bgCoordinatesFormat.add(new RadioButton(Locale.get("Decimal_degrees"), null));
        bgCoordinatesFormat.add(new RadioButton(Locale.get("Degrees_and_minutes"), null));
        bgCoordinatesFormat.add(new RadioButton(Locale.get("Degrees_minutes_seconds"), null));
        bgCoordinatesFormat.setSelected(R.getSettings().getCoordsFormat());
        for (int i = 0; i < bgCoordinatesFormat.getButtonCount(); i++) {
            form.addComponent(bgCoordinatesFormat.getRadioButton(i));
        }

        btnSaveLocation = new Button(Locale.get("Save"));
        btnSaveLocation.addActionListener(this);
        form.addComponent(btnSaveLocation);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.addCommandListener(this);
        form.show();
    }

    public void viewInterfaceSettings() {
        FormLocify form = new FormLocify(Locale.get("Interface"));
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));

        form.addComponent(new Label(Locale.get("Language")));
        bgLanguage = new ButtonGroup();
        for (int i = 0; i < R.getSettings().locales.length; i++) {
            bgLanguage.add(new RadioButton(R.getSettings().languageNames[i]));
        }
        bgLanguage.setSelected(R.getSettings().getSelectedLanguage());
        for (int i = 0; i < bgLanguage.getButtonCount(); i++) {
            form.addComponent(bgLanguage.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("Units")));
        bgUnits = new ButtonGroup();
        bgUnits.add(new RadioButton(Locale.get("Metric")));
        bgUnits.add(new RadioButton(Locale.get("Imperial")));
        bgUnits.setSelected(R.getSettings().getUnits());
        for (int i = 0; i < bgUnits.getButtonCount(); i++) {
            form.addComponent(bgUnits.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("Turn_on_backlight")));
        bgBacklight = new ButtonGroup();
        bgBacklight.add(new RadioButton(Locale.get("Nowhere")));
        bgBacklight.add(new RadioButton(Locale.get("Maps")));
        bgBacklight.add(new RadioButton(Locale.get("Navigation")));
        bgBacklight.add(new RadioButton(Locale.get("Maps_and_navigation")));
        bgBacklight.add(new RadioButton(Locale.get("Whole_application")));
        bgBacklight.setSelected(R.getSettings().getBacklight());
        for (int i = 0; i < bgBacklight.getButtonCount(); i++) {
            form.addComponent(bgBacklight.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("Backlight_blink_frequency")));
        taBacklightFrequency = new TextArea(String.valueOf(R.getSettings().getBacklightFrequency()),
                1, 10, TextField.NUMERIC);
        form.addComponent(taBacklightFrequency);

        btnSaveInterface = new Button(Locale.get("Save"));
        btnSaveInterface.addActionListener(this);
        form.addComponent(btnSaveInterface);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.addCommandListener(this);
        form.show();
    }

    public void viewMapSettings() {
        FormLocify form = new FormLocify(Locale.get("Maps"));
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));

        form.addComponent(new Label(Locale.get("Default_map_provider")));
        bgMapProvider = new ButtonGroup();
        Vector providers = (new TileMapLayer()).getProvidersAndModes();
        int onlineProviders = providers.size();
        for (int i = 0; i < providers.size(); i++) {
            bgMapProvider.add(new RadioButton((String) providers.elementAt(i), IconData.get("locify://icons/online.png")));
        }
        bgMapProvider.add(new RadioButton(Locale.get("Offline_maps"), IconData.get("locify://icons/saved.png")));

        if (R.getSettings().isDefaultMapProviderOnline()) {
            bgMapProvider.setSelected(R.getSettings().getDefaultMapProvider());
        } else {
            bgMapProvider.setSelected(onlineProviders);
        }
        for (int i = 0; i < bgMapProvider.getButtonCount(); i++) {
            form.addComponent(bgMapProvider.getRadioButton(i));
        }
        
        btnSaveMap = new Button(Locale.get("Save"));
        btnSaveMap.addActionListener(this);
        form.addComponent(btnSaveMap);

        btnAdvancedMaps = new Button(Locale.get("Advanced_settings"));
        btnAdvancedMaps.addActionListener(this);
        form.addComponent(btnAdvancedMaps);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.addCommandListener(this);
        form.show();
    }

    public void viewOtherSettings() {
        FormLocify form = new FormLocify(Locale.get("Other"));
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
        form.addComponent(new Label(Locale.get("Autologin")));
        bgAutoLogin = new ButtonGroup();
        bgAutoLogin.add(new RadioButton(Locale.get("On")));
        bgAutoLogin.add(new RadioButton(Locale.get("Off")));
        bgAutoLogin.setSelected(R.getSettings().getAutoLogin());
        for (int i = 0; i < bgAutoLogin.getButtonCount(); i++) {
            form.addComponent(bgAutoLogin.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("Close_app_on_external_browser")));
        bgExternalClose = new ButtonGroup();
        bgExternalClose.add(new RadioButton(Locale.get("Always")));
        bgExternalClose.add(new RadioButton(Locale.get("Never")));
        bgExternalClose.add(new RadioButton(Locale.get("Ask_every_time")));
        bgExternalClose.setSelected(R.getSettings().getExternalClose());
        for (int i = 0; i < bgExternalClose.getButtonCount(); i++) {
            form.addComponent(bgExternalClose.getRadioButton(i));
        }

        btnSaveOther = new Button(Locale.get("Save"));
        btnSaveOther.addActionListener(this);
        form.addComponent(btnSaveOther);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.addCommandListener(this);
        form.show();
    }

    public void viewAdvancedMapSettings() {
        FormLocify form = new FormLocify(Locale.get("Advanced_settings"));
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
        form.addComponent(new Label(Locale.get("Autoload")));
        bgAutoload = new ButtonGroup();
        bgAutoload.add(new RadioButton(Locale.get("On")));
        bgAutoload.add(new RadioButton(Locale.get("Off")));
        bgAutoload.setSelected(R.getSettings().getAutoload());
        for (int i = 0; i < bgAutoload.getButtonCount(); i++) {
            form.addComponent(bgAutoload.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("File_cache")));
        bgFilecache = new ButtonGroup();
        bgFilecache.add(new RadioButton(Locale.get("On")));
        bgFilecache.add(new RadioButton(Locale.get("Off")));
        bgFilecache.setSelected(R.getSettings().getFilecache());
        for (int i = 0; i < bgFilecache.getButtonCount(); i++) {
            form.addComponent(bgFilecache.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("Cache_size")));
        taCacheSize = new TextArea(String.valueOf(R.getSettings().getFilecacheSize()), 1, 10, TextField.NUMERIC);
        form.addComponent(taCacheSize);

        form.addComponent(new Label(Locale.get("Panning_behaviour")));
        bgPanning = new ButtonGroup();
        bgPanning.add(new RadioButton(Locale.get("Repaint_during")));
        bgPanning.add(new RadioButton(Locale.get("Wait_until_end_of_panning")));
        bgPanning.setSelected(R.getSettings().getPanning());
        for (int i = 0; i < bgPanning.getButtonCount(); i++) {
            form.addComponent(bgPanning.getRadioButton(i));
        }

        form.addComponent(new Label(Locale.get("Offline_map_loading")));
        bgMapLoading = new ButtonGroup();
        bgMapLoading.add(new RadioButton(Locale.get("Regular")));
        bgMapLoading.add(new RadioButton(Locale.get("S60_fix")));
        bgMapLoading.setSelected(R.getSettings().getMapLoading());
        for (int i = 0; i < bgMapLoading.getButtonCount(); i++) {
            form.addComponent(bgMapLoading.getRadioButton(i));
        }

        btnSaveAdvancedMaps = new Button(Locale.get("Save"));
        btnSaveAdvancedMaps.addActionListener(this);
        form.addComponent(btnSaveAdvancedMaps);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.addCommandListener(this);
        form.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getSource() == list) {
            int selected = list.getSelectedIndex();
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
        } else if (evt.getSource() == btnSaveLocation) {
            R.getSettings().saveLocationSettings(bgPrefferedGps.getSelectedIndex(), bgCoordinatesFormat.getSelectedIndex());
        } else if (evt.getSource() == btnSaveInterface) {
            R.getSettings().saveInterfaceSettings(bgLanguage.getSelectedIndex(), bgUnits.getSelectedIndex(),
                    bgBacklight.getSelectedIndex(), Integer.parseInt(taBacklightFrequency.getText()));
        } else if (evt.getSource() == btnSaveOther) {
            R.getSettings().saveOtherSettings(bgAutoLogin.getSelectedIndex(), bgExternalClose.getSelectedIndex());
        } else if (evt.getSource() == btnSaveMap) {
            int onlineProviders = ((new TileMapLayer()).getProvidersAndModes()).size();
            if (bgMapProvider.getSelectedIndex() < onlineProviders) {
                R.getSettings().saveMapsSettings(bgMapProvider.getSelectedIndex(), false);
            } else {
                R.getSettings().saveMapsSettings(bgMapProvider.getSelectedIndex() - onlineProviders, true);
            }
        } else if (evt.getSource() == btnAdvancedMaps) {
            viewAdvancedMapSettings();
        } else if (evt.getSource() == btnSaveAdvancedMaps) {
            R.getSettings().saveAdvancedMaps(bgAutoload.getSelectedIndex(), bgFilecache.getSelectedIndex(), 
                    Integer.parseInt(taCacheSize.getText()), bgPanning.getSelectedIndex(),bgMapLoading.getSelectedIndex());
        }
    }
}
