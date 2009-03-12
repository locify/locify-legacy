/*
 * MapItemManager.java
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
package com.locify.client.maps.mapItem;

import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Manages visible map itemps on map
 * @author MenKat
 */
public class MapItemManager {

    /** hashmap contains itemName and MapItems */
    private Hashtable items;
    /** hashmap contains itemName and MapItem, always visible */
    private Hashtable itemsTemp;


    public MapItemManager() {
        this.items = new Hashtable();
        this.itemsTemp = new Hashtable();
    }

    public void init() {
        //default settings
        MapItem scale = new ScaleMapItem();
        scale.priority = MapItem.PRIORITY_HIGH;
        scale.setEnabled(true);
        addItemFixed(Locale.get("Scale"), scale);

//        Object[] names = itemsState.keys();
//        for (int i = 0; i < names.length; i++) {
//            if (items.containsKey(names[i]))
//                ((MapItem) items.get(names[i])).setEnabled(isEnabled((String) names[i]));
//        }
    }    


    /**
     * Add item to manager as fixed. Fixed item cannot be overwritten, cannot be selected.
     * @param itemName name of item
     * @param item MapItem to show
     * @return true if added, falsee if same name already exist
     */
    private boolean addItemFixed(String itemName, MapItem item) {
        if (!items.containsKey(itemName)) {
            item.fixed = true;
            items.put(itemName, item);
            return true;
        }
        return false;
    }

    private boolean isFixed(String name) {
        MapItem item = (MapItem) items.get(name);
        if (item == null)
            return false;
        else
            return item.fixed;
    }
    
    /**
     * Add item to manager, if item of same name exist, overwrite it !!!
     * @param itemName name of item
     * @param item MapItem to show
     * @return true if added, falsee if same name already exist
     */
    public void addItem(String itemName, MapItem item, int priority) {
        item.priority = priority;
        items.put(itemName, item);
    }

    public MapItem getItem(String itemName) {
        return (MapItem) items.get(itemName);
    }

    public MapItem getItem(int index) {
        if (index < items.size()) {
            return (MapItem) items.get(getItemName(index));
        } else
            return null;
    }

    public String getItemName(int i) {
        if (i < items.size()) {
            Enumeration enu = items.keys();
            int counter = 0;
            while (enu.hasMoreElements()) {
                if (counter == i)
                    return (String) enu.nextElement();

                enu.nextElement();
                counter++;
            }
            return null;
        } else {
            return null;
        }
    }

    public void removeItem(String itemName) {
        items.remove(itemName);
    }

    public boolean existItem(String itemName) {
        return items.containsKey(itemName);
    }

    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Add temp items do mapItemManager. Items are always visible and when new item
     * with same name comes, it automatically overwrite old one.
     * @param itemName name of item
     * @param item MapItem
     */
    public void addItemTemp(String itemName, MapItem item, int priority) {
        item.priority = priority;
        itemsTemp.put(itemName, item);
    }

    public MapItem getItemTemp(String itemName) {
        return (MapItem) itemsTemp.get(itemName);
    }

    public void removeItemTemp(String itemName) {
        itemsTemp.remove(itemName);
    }

    public boolean existItemTemp(String itemName) {
        return itemsTemp.containsKey(itemName);
    }
    
    /**
     * Removes all elements excluding scale
     */
    public void removeAll() {
        items.clear();
        itemsTemp.clear();
        init();
    }

    public void setEnabled(String itemName, boolean enabled) {
        if (items.containsKey(itemName)) {
            MapItem item = (MapItem) items.get(itemName);
            item.setEnabled(enabled);
        } else if (itemsTemp.contains(itemName)) {
            MapItem item = (MapItem) itemsTemp.get(itemName);
            item.setEnabled(enabled);
        }
    }

    public boolean isEnabled(String itemName) {
        if (items.containsKey(itemName)) {
            MapItem item = (MapItem) items.get(itemName);
            return item.enabled;
        } else if (itemsTemp.contains(itemName)) {
            MapItem item = (MapItem) itemsTemp.get(itemName);
            return item.enabled;
        }
        return false;
    }


    
    public void drawItems(Graphics g, int priority) {
        try {
            Enumeration enu = items.keys();
            MapItem item;
            while (enu.hasMoreElements()) {
                item = (MapItem) items.get(enu.nextElement());
                if (item.priority == priority)
                    item.drawItem(g);
            }

            enu = itemsTemp.keys();
            while (enu.hasMoreElements()) {
                item = (MapItem) itemsTemp.get(enu.nextElement());
                if (item.priority == priority)
                    item.drawItem(g);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItemManager.drawItems()", "priority: " + priority);
        }
    }

    public void disableInitializeState() {
        Enumeration enu = items.elements();
        MapItem item;
        while (enu.hasMoreElements()) {
            item = (MapItem) enu.nextElement();
            item.disableInitializeState();
        }
        enu = itemsTemp.elements();
        while (enu.hasMoreElements()) {
            item = (MapItem) enu.nextElement();
            item.disableInitializeState();
        }
    }
    
    public void panItem(int x, int y) {
        Enumeration enu = items.elements();
        MapItem item;
        while (enu.hasMoreElements()) {
            item = (MapItem) enu.nextElement();
            item.panItem(x, y);
        }
        enu = itemsTemp.elements();
        while (enu.hasMoreElements()) {
            item = (MapItem) enu.nextElement();
            item.panItem(x, y);
        }
    }
    
    public String printItems() {
//        String data = "";
//
//        for (int i = 0; i < itemsState.size(); i++) {
//            data += i + ". - " + getItemName(i) + "\n";
//        }
//        return data;
        return "";
    }
    
    public Vector getWaypointsAtPosition(int x, int y, int radiusSquare) {
        Vector wayPoints = new Vector();
        Enumeration enu = items.keys();
        MapItem item;
        String name;

        while (enu.hasMoreElements()) {
            name = (String) enu.nextElement();
            if (!isFixed(name)) {
                item = (MapItem) items.get(name);
                item.getWaypointsAtPosition(wayPoints, x, y, radiusSquare);
            }
        }
        return wayPoints;
    }

//    public MapItem touchAtPosition(int x, int y) {
//        for (int i = 0; i < itemsState.size(); i++) {
//            MapItem item = (MapItem) items.get(getItemName(i));
//            if (item.touchInside(x, y))
//                return item;
//        }
//        return null;
//    }

}
