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
import com.locify.client.locator.Location4D;
import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.maps.fileMaps.StoreManager;
import com.locify.client.maps.FileMapLayer;
import com.locify.client.maps.fileMaps.StoreManagerMapInfo;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

/**
 *
 * @author menion
 */
public class MapOfflineChooseScreen extends Form implements CommandListener, ItemCommandListener {

    private Command cmdSelect;
    private Command cmdSelectAndCenter;
    private Command cmdOnlineMaps;

    private Command cmdInitialize;
    private Command cmdSearchMaps;

    public double lastLat1, lastLon1, lastLat2, lastLon2;

    private Vector findedData;
    
    public MapOfflineChooseScreen() {
        super(Locale.get("Change_map_file"));
        setCommandListener(this);

        cmdSelect = new Command(Locale.get("Select"), Command.SCREEN, 1);
        cmdSelectAndCenter = new Command(Locale.get("Select_and_center"), Command.SCREEN, 2);
        cmdOnlineMaps = new Command(Locale.get("Change_map_tile"), Command.SCREEN, 3);

        cmdInitialize = new Command(Locale.get("Initialize_maps"), Command.SCREEN, 4);
        cmdSearchMaps = new Command(Locale.get("Find_maps"), Command.SCREEN, 5);
    }

    private void view() {
        deleteAll();
        append(Locale.get("Searching"), null);
        if (!this.isShown()) {
            R.getMidlet().switchDisplayable(null, this);
            if (StoreManager.form != null)
                StoreManager.form = null;
        }
    }

    public void view(double lat1, double lon1, double lat2, double lon2) {
//Logger.debug("MapOfflineChooseScreen.view(" + lat1 + ", " + lon1 + ", " + lat2 + ", " + lon2 + ")");
        this.lastLat1 = lat1;
        this.lastLon1 = lon1;
        this.lastLat2 = lat2;
        this.lastLon2 = lon2;

        view();

        findedData = StoreManager.getMapsAroundScreen(lat1, lon1, lat2, lon2);
        deleteAll();
        if (findedData.size() == 0) {
            String path = FileSystem.ROOT + FileSystem.MAP_FOLDER;
            append(Locale.get("No_file_maps_warning", path), null);
        } else {
            for (int i = 0; i < findedData.size(); i++) {
                StoreManagerMapInfo smmi = (StoreManagerMapInfo) findedData.elementAt(i);
                StringItem item = new StringItem("", smmi.mapName, StringItem.BUTTON);
                item.addCommand(cmdSelectAndCenter);
                item.setDefaultCommand(cmdSelect);

                item.setItemCommandListener(this);

                append(item);
            }
        }

        removeAllCommands();
        addCommand(cmdOnlineMaps);
        addCommand(Commands.cmdBack);
        //#style imgHome
        addCommand(Commands.cmdHome);
        
        //#style imgSaved
        addCommand(cmdInitialize);

        if (!this.isShown())
            R.getMidlet().switchDisplayable(null, this);
    }

    public void commandAction(Command c, Displayable d) {
        try {
            if (c.equals(Commands.cmdBack)) {
                R.getMapScreen().view();
            } else if (c.equals(Commands.cmdHome)) {
                R.getURL().call("locify://mainScreen");
            } else if (c.equals(cmdInitialize)) {
                deleteAll();
                StoreManager.initializeOfflineMaps(this);
            } else if (c.equals(cmdSearchMaps))     {
                view(lastLat1, lastLon1, lastLat2, lastLon2);
            } else if (c.equals(cmdOnlineMaps)) {
                R.getMapScreen().setOnlineMaps();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapScreen.commandAction()", null);
        }
    }

    public void commandAction(Command c, Item item) {
        if (item instanceof StringItem) {
            FileMapManager fmm = StoreManager.getInitializedOfflineMap(((StringItem) item).getText(), false);
//Logger.debug("  setMap: " + ((StringItem) item).getText() + " and is ready? " + (fmm == null));
            if (fmm != null) {
                if (c.equals(cmdSelectAndCenter)) {
                    Location4D center = fmm.getFileMapConfig().getMapViewPort().getCenter();
                    R.getMapScreen().setFileMap(fmm,
                            FileMapLayer.convertMapToGeo(fmm.getFileMapConfig(), center.getLatitude(), center.getLongitude()));
                } else {
                    R.getMapScreen().setFileMap(fmm, new Location4D((lastLat1 + lastLat2) / 2,
                            (lastLon1 + lastLon2) / 2, 0.0f));
                }
            } else {
                deleteAll();
                append(Locale.get("Creating_warning"), null);
            }
        }
    }
}
