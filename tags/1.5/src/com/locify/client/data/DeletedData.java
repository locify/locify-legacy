/*
 * Deleted.java
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

import com.locify.client.utils.R;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import de.enough.polish.util.Locale;
import java.io.ByteArrayInputStream;
import java.util.Vector;
import org.kxml2.io.KXmlParser;

/**
 * Stores deleted items
 * 
 * @author David Vavra
 */
public class DeletedData {

    private static Vector deleted;

    /**
     * Creates default values or loads existing deleted items
     */
    public static void load() {
        try {
            R.getLoading().setText(Locale.get("Loading_deleted"));
            if (!R.getFileSystem().exists(FileSystem.DELETED_FILE)) {  //prvni start aplikace
                deleted = new Vector();
                //0.8.1 compatibility
                if (R.getFileSystem().exists(FileSystem.SETTINGS_FOLDER + "deleted.lcf")) {
                    R.getFileSystem().delete(FileSystem.SETTINGS_FOLDER + "deleted.lcf");
                }
                saveXML();
            } else {
                //ostatni starty aplikace, nacitam data
                loadXML();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "DeletedData.load", null);
        }
    }

    /**
     * Adds new deleted item
     * @param id item id
     * @param type 
     */
    public static void add(String id, String type) {
        try {
            deleted.addElement(new Deleted(id, type, Utils.timestamp()));
            saveXML();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "DeletedData.add", id);
        }
    }

    /**
     * Delete all is used in sync
     */
    public static void deleteAll() {
        deleted = new Vector();
        R.getFileSystem().delete(FileSystem.DELETED_FILE);
    }

    /**
     * Sync data of deleted items
     * @return sync data
     */
    public static String syncData() {
        return getXML();
    }

    private static void loadXML() {
        try {
            deleted = new Vector();
            String deletedData = R.getFileSystem().loadString(FileSystem.DELETED_FILE);
            ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(deletedData));
            bais.reset();
            KXmlParser parser = new KXmlParser();
            //parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
            parser.setInput(bais, "utf-8");
            String id = "";
            String type = "";
            String timestamp = "";
            while (true) {
                int parserType = parser.next();
                if (parserType == KXmlParser.END_DOCUMENT) {
                    break;
                }
                if (parserType != KXmlParser.START_TAG) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName == null) {
                    // nothing to do ... empty tag
                } else if (tagName.equals("id")) {
                    id = parser.nextText();
                } else if (tagName.equals("type")) {
                    type = parser.nextText();
                } else if (tagName.equals("ts")) {
                    timestamp = parser.nextText();
                    //zpracovani
                    deleted.addElement(new Deleted(id, type, Long.parseLong(timestamp)));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "DeletedData.loadXML", null);
        }
    }

    private static void saveXML() {
        String deletedData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + getXML();
        R.getFileSystem().saveString(FileSystem.DELETED_FILE, deletedData);
    }

    private static String getXML() {
        String deletedData = "";
        for (int i = 0; i < deleted.size(); i++) {
            Deleted delete = (Deleted) deleted.elementAt(i);
            deletedData += "<file>\n";
            deletedData += "<id>" + delete.getId() + "</id>\n";
            deletedData += "<type>" + delete.getType() + "</type>\n";
            deletedData += "<action>delete</action>\n";
            deletedData += "<ts>" + delete.getTimestamp() + "</ts>\n";
            deletedData += "</file>\n";
        }
        return deletedData;
    }
}

/**
 * This is object for representing deleted items
 * @author Destil
 */
class Deleted {

    private String id;
    private String type;
    private long timestamp;

    public Deleted(String id, String type, long timestamp) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

