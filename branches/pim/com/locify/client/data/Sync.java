/*
 * Sync.java
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
package com.locify.client.data;

import com.locify.client.data.items.GeoFiles;
import com.locify.client.net.Http;
import com.locify.client.net.XmlParser;
import de.enough.polish.util.Locale;
import com.locify.client.utils.R;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

/**
 * This class creates sync data and handles sending and receiving of them
 * @author David Vavra
 */
public class Sync implements CommandListener {

    public Sync() {
    }
    private static boolean running = false;
    private Command cmdStop = new Command(Locale.get("Stop"), Command.STOP, 1);
    private Form frmSync;

    public static boolean isRunning() {
        return running;
    }

    public void viewProgressScreen(String caption, String status) {
        frmSync = new Form(caption);
        StringItem siSyncStatus = new StringItem("", status);
        frmSync.append(siSyncStatus);
        Gauge gaSync = new Gauge("", false, 60, 0);
        gaSync.setMaxValue(Gauge.INDEFINITE);
        gaSync.setValue(Gauge.CONTINUOUS_RUNNING);
        frmSync.append(gaSync);
        frmSync.addCommand(cmdStop);
        frmSync.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmSync);
    }

    /**
     * This method sends all data to be synced
     */
    public void sendSync() {
        try {
            running = true;
            viewProgressScreen(Locale.get("Sync_in_progress"),Locale.get("Sending_sync_data"));
            //build sync data
            String syncData = "<sync>\n";
            syncData += R.getSettings().syncData();
            syncData += ServicesData.syncData();
            syncData += ServiceSettingsData.getXML();
            syncData += R.getMainScreen().syncData();
            syncData += DeletedData.syncData();
            syncData += CookieData.syncData();
            syncData += GeoFiles.syncData();
            syncData += "</sync>\n";

            Http http = R.getHttp();
            R.getPostData().setRaw(syncData, false);
            http.start(Http.DEFAULT_URL + "sync/sync");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Sync.sendSync", null);
        }
    }

    /**
     * Before sync, data on client is erased
     */
    public void resetDatabase() {
        viewProgressScreen(Locale.get("Sync_in_progress"),Locale.get("Receiving_sync_data"));
        DeletedData.deleteAll();
        R.getMainScreen().deleteAllItems();
        ServicesData.deleteAll();
        R.getFileSystem().deleteAll(FileSystem.FILES_FOLDER);
    }

    /**
     * This method receives one synced data item and takes apropriate action
     * @param id file id
     * @param type file type
     * @param action file action
     * @param timestamp file timestamp
     * @param content  file content
     */
    public static void receiveSyncItem(String id, String type, String action, String timestamp, String content) {
        try {
            if (type.equals("waypoint") || type.equals("route")) //waypoints is saved as kml, they are not parsed
            {
                // file id is "locify://filesystem/filename.kml"
                int startLength = "locify://filesystem/".length(), endLength = ".kml".length();
                String filename = id.substring(startLength, id.length() - endLength);
                GeoFiles.save("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + content, filename);
            } else //other data are parsed one more time
            {
                new XmlParser().parseSyncItem(content, id, type, Long.parseLong(timestamp));
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Sync.receiveSyncItem", id);
        }
    }

    /**
     * This method is called when sync is completed
     */
    public static void syncComplete() {
        R.getPostData().reset();
        R.getCustomAlert().quickView(Locale.get("Sync_complete"), "Info", "locify://mainScreen");
        running = false;
        R.getMainScreen().refreshIcons();
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == cmdStop) {
            running = false;
            R.getURL().call("locify://mainScreen");
        }
    }
}
