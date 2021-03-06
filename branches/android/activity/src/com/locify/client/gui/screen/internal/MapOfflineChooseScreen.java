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
import com.locify.client.locator.Location4D;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.maps.fileMaps.StoreManager;
import com.locify.client.maps.FileMapLayer;
import com.locify.client.maps.fileMaps.StoreManagerMapInfo;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import de.enough.polish.ui.List;
import de.enough.polish.util.Locale;
import java.util.Vector;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Form; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Item; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.ItemCommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.StringItem; import de.enough.polish.ui.StyleSheet;

/**
 *
 * @author menion
 */
public class MapOfflineChooseScreen implements CommandListener, ItemCommandListener {

    private Command cmdSelectAndCenter;
    private Command cmdOnlineMaps;
    private Command cmdInitialize;
    private Command cmdSearchMaps;
    private List lstAvailableMaps;
    private Form frmMapsInfo;
    public double lastLat1,  lastLon1,  lastLat2,  lastLon2;
    private Vector findedData;

    public MapOfflineChooseScreen() {
        cmdSelectAndCenter = new Command(Locale.get(498), Command.SCREEN, 2);
        cmdOnlineMaps = new Command(Locale.get(360), Command.SCREEN, 3);
        cmdInitialize = new Command(Locale.get(492), Command.SCREEN, 4);
        cmdSearchMaps = new Command(Locale.get(491), Command.SCREEN, 5);
    }

    public void view(double lat1, double lon1, double lat2, double lon2) {
//Logger.debug("MapOfflineChooseScreen.view(" + lat1 + ", " + lon1 + ", " + lat2 + ", " + lon2 + ")");
        this.lastLat1 = lat1;
        this.lastLon1 = lon1;
        this.lastLat2 = lat2;
        this.lastLon2 = lon2;

        view();

        findedData = StoreManager.getMapsAroundScreen(lat1, lon1, lat2, lon2);
        if (findedData.size() == 0) {
            viewNoMapsInfo();
        } else {
            viewAvailable();
        }
    }

    private void view() {
        lstAvailableMaps = new List(Locale.get(504), List.IMPLICIT);
        lstAvailableMaps.setCommandListener(this);
        lstAvailableMaps.deleteAll();
        lstAvailableMaps.append(Locale.get(497), null);
        if (!lstAvailableMaps.isShown()) {
            R.getMidlet().switchDisplayable(null, lstAvailableMaps);
            if (StoreManager.form != null) {
                StoreManager.form = null;
            }
        }
        lstAvailableMaps.addCommand(Commands.cmdBack);
    }

    private void viewNoMapsInfo() {
        frmMapsInfo = new Form(Locale.get(504));
        frmMapsInfo.setCommandListener(this);
        String path = FileSystem.ROOT + FileSystem.MAP_FOLDER;
        frmMapsInfo.append(Locale.get(0, path));
        StringItem btnInitialize = new StringItem("", Locale.get(492), StringItem.BUTTON);
        btnInitialize.setDefaultCommand(cmdInitialize);
        btnInitialize.setItemCommandListener(this);
        frmMapsInfo.append(btnInitialize);
        //#style imgOnlineMap
        frmMapsInfo.addCommand(cmdOnlineMaps, de.enough.polish.ui.StyleSheet.imgonlinemapStyle );
        //#style imgHome
        frmMapsInfo.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmMapsInfo.addCommand(Commands.cmdBack);
        R.getMidlet().switchDisplayable(null, frmMapsInfo);
    }

    private void viewAvailable() {
        lstAvailableMaps.deleteAll();
        for (int i = 0; i < findedData.size(); i++) {
            StoreManagerMapInfo smmi = (StoreManagerMapInfo) findedData.elementAt(i);
            lstAvailableMaps.append(smmi.mapName, IconData.get("locify://icons/saved.png"));
        }

        lstAvailableMaps.addCommand(cmdSelectAndCenter);
        //#style imgOnlineMap
        lstAvailableMaps.addCommand(cmdOnlineMaps, de.enough.polish.ui.StyleSheet.imgonlinemapStyle );
        //#style imgHome
        lstAvailableMaps.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        //#style imgSaved
        lstAvailableMaps.addCommand(cmdInitialize, de.enough.polish.ui.StyleSheet.imgsavedStyle );
    }

    public void commandAction(Command c, Displayable d) {
        try {
            if (c.equals(Commands.cmdBack)) {
                R.getMapScreen().view();
            } else if (c.equals(Commands.cmdHome)) {
                R.getURL().call("locify://mainScreen");
            } else if (c.equals(cmdInitialize)) {
                //#if !applet
                //# StoreManager.initializeOfflineMaps(this);
            //#endif
            } else if (c.equals(cmdSearchMaps)) {
                view(lastLat1, lastLon1, lastLat2, lastLon2);
            } else if (c.equals(cmdOnlineMaps)) {
                R.getMapScreen().setOnlineMaps();
            } else if (c == List.SELECT_COMMAND || c.equals(cmdSelectAndCenter) && findedData.size() > 0) {
                StoreManagerMapInfo smmi = (StoreManagerMapInfo) findedData.elementAt(lstAvailableMaps.getSelectedIndex());
                FileMapManager fmm = StoreManager.getInitializedOfflineMap(smmi.mapName, false);
                if (c == List.SELECT_COMMAND) {
                    if (fmm != null) {
                        R.getMapScreen().setFileMap(fmm, new Location4D((lastLat1 + lastLat2) / 2,
                                (lastLon1 + lastLon2) / 2, 0.0f));
                    }
                } else if (c.equals(cmdSelectAndCenter)) {
                    if (fmm != null) {
                        Location4D center = fmm.getFileMapConfig().getMapViewPort().getCenter();
                        R.getMapScreen().setFileMap(fmm,
                                FileMapLayer.convertMapToGeo(fmm.getFileMapConfig(), center.getLatitude(), center.getLongitude()));
                    }
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.commandAction()", null);
        }
    }

    public void commandAction(Command c, Item item) {
        if (c == cmdInitialize) {
            //#if !applet
            //# StoreManager.initializeOfflineMaps(this);
            //#endif
        }
    }
}
