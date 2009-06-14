/*
 * MapOfflineChoose.java
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
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ListLocify;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.maps.fileMaps.StoreManager;
import com.locify.client.maps.FileMapLayer;
import com.locify.client.maps.fileMaps.StoreManagerMapInfo;
import com.locify.client.net.browser.HtmlTextArea;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import java.util.Vector;

/**
 *
 * @author menion
 */
public class MapOfflineChooseScreen implements ActionListener {

    public double lastLat1,  lastLon1,  lastLat2,  lastLon2;
    private ListLocify list;
    private Vector findedData;
    private Button btnInitialize;

    public MapOfflineChooseScreen() {
    }

    public void view(double lat1, double lon1, double lat2, double lon2) {
//Logger.debug("MapOfflineChooseScreen.view(" + lat1 + ", " + lon1 + ", " + lat2 + ", " + lon2 + ")");
        this.lastLat1 = lat1;
        this.lastLon1 = lon1;
        this.lastLat2 = lat2;
        this.lastLon2 = lon2;

        findedData = StoreManager.getMapsAroundScreen(lat1, lon1, lat2, lon2);
        if (findedData.size() == 0) {
            viewNoMapsInfo();
        } else {
            viewAvailable();
        }
    }

    private void viewNoMapsInfo() {
        FormLocify form = new FormLocify(Locale.get("Maps_in_area"));
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        form.setCommandListener(this);

        String path = FileSystem.ROOT + FileSystem.MAP_FOLDER;
        form.addComponent(new HtmlTextArea(Locale.get("No_file_maps_warning")));
        form.addComponent(new HtmlTextArea(path));

        btnInitialize = new Button(Locale.get("Initialize_maps"));
        btnInitialize.addActionListener(this);
        form.addComponent(btnInitialize);

        form.addCommand(Commands.cmdOnlineMaps);
        form.addCommand(Commands.cmdHome);
        form.addCommand(Commands.cmdBack);
        form.show();
    }

    private void viewAvailable() {
        FormLocify form = new FormLocify(Locale.get("Maps_in_area"));
        form.setLayout(new BorderLayout());

        list = new ListLocify();
        form.addComponent(BorderLayout.CENTER, list);

        for (int i = 0; i < findedData.size(); i++) {
            StoreManagerMapInfo smmi = (StoreManagerMapInfo) findedData.elementAt(i);
            Label label = new Label(smmi.mapName);
            label.setIcon(IconData.get("locify://icons/saved.png"));
            list.addItem(label);
        }

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdSelectAndCenter);
        form.addCommand(Commands.cmdOnlineMaps);
        form.addCommand(Commands.cmdHome);
        form.addCommand(Commands.cmdInitialize);
        form.setCommandListener(this);
        form.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdInitialize || evt.getSource() == btnInitialize) {
            //#if !applet
            StoreManager.initializeOfflineMaps(this);
            //#endif
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getMapScreen().view();
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdSearchMaps) {
            view(lastLat1, lastLon1, lastLat2, lastLon2);
        } else if (evt.getCommand() == Commands.cmdOnlineMaps) {
            R.getMapContent().setOnlineMap();
        } else if (evt.getSource() == list || evt.getCommand() == Commands.cmdSelectAndCenter && findedData.size() > 0) {
            StoreManagerMapInfo smmi = (StoreManagerMapInfo) findedData.elementAt(list.getSelectedIndex());
            FileMapManager fmm = StoreManager.getInitializedOfflineMap(smmi.mapName, false);
            if (evt.getSource() == list) {
                if (fmm != null) {
                    R.getMapContent().setFileMap(fmm, new Location4D((lastLat1 + lastLat2) / 2,
                            (lastLon1 + lastLon2) / 2, 0.0f));
                }
            } else if (evt.getCommand() == Commands.cmdSelectAndCenter) {
                if (fmm != null) {
                    Location4D center = fmm.getFileMapConfig().getMapViewPort().getCenter();
                    R.getMapContent().setFileMap(fmm,
                            FileMapLayer.convertMapToGeo(fmm.getFileMapConfig(), center.getLatitude(), center.getLongitude()));
                }
            }
        }
    }
}
