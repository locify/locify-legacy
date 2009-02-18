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
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.HashMap;
import de.enough.polish.util.Iterator;
import de.enough.polish.util.Locale;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Manages visible map itemps on map
 * @author MenKat
 */
public class MapItemManager {

    /** hashmap contains itemName and MapItems */
    private HashMap items;
    /** hashmap contains itemName and true if set to visible otherwise false (1, 0) */
    private HashMap itemsState;
    /** hashmap contains itemName and MapItem, always visible */
    private HashMap itemsTemp;
    /** number of fixed items */
    private ArrayList fixedItemNames;

    public MapItemManager() {
        this.items = new HashMap();
        this.itemsState = new HashMap();
        this.itemsTemp = new HashMap();
        this.fixedItemNames = new ArrayList();        
    }

    public void init() {
        //default settings
        itemsState.put(Locale.get("Scale"), String.valueOf(true));
        fixedItemNames.add(Locale.get("Scale"));

        Object[] names = itemsState.keys();
        for (int i = 0; i < names.length; i++) {
            if (items.containsKey(names[i]))
                ((MapItem) items.get(names[i])).setEnabled(isEnabled((String) names[i]));
        }

        addItemFixed(Locale.get("Scale"), new ScaleMapItem());
        ((MapItem) items.get(Locale.get("Scale"))).setEnabled(R.getSettings().getShowScale());
        //addItemFixed(Locale.get("Touch"), new TouchScreenMapItem());
        //((MapItem) items.get(Locale.get("Touch"))).setEnabled(R.getSettings().getShowScale());
    }    

    /**
     * Add item to manager, if item of same name exist, overwrite it !!!
     * @param itemName name of item
     * @param item MapItem to show
     * @return true if added, falsee if same name already exist
     */
    public boolean addItem(String itemName, MapItem item) {
        //System.out.println("\n add1 - " + printItems());
        if (itemsState.containsKey(itemName)) {
            removeItem(itemName);
        }

        items.put(itemName, item);
        itemsState.put(itemName, String.valueOf(true));
        //System.out.println("\n add2 - " + printItems());
        return true;
    }

    /**
     * Removes all elements excluding scale
     */
    public void removeAll()
    {
        items.clear();
        itemsState.clear();
        itemsTemp.clear();
        fixedItemNames.clear();
        init();
    }
    
    private boolean addItemFixed(String itemName, MapItem item) {
        if (!items.containsKey(itemName)) {
            items.put(itemName, item);
            if (!isFixed(itemName))
                fixedItemNames.add(itemName);
            return true;
        }
        return false;
    }

    private boolean isFixed(String name) {
        for (int i = 0; i < fixedItemNames.size(); i++) {
            if (fixedItemNames.get(i).equals(name))
                return true;
        }
        return false;
    }
    /**
     * Add temp items do mapItemManager. Items are always visible and when new item 
     * with same name comes, it automatically overwrite old one.
     * @param itemName name of item
     * @param item MapItem
     */
    public void addItemTemp(String itemName, MapItem item) {
        itemsTemp.put(itemName, item);
    }
    
    public MapItem getItemTemp(String itemName) {
        return (MapItem) itemsTemp.get(itemName);
    }
    
    public void removeItemTemp(String itemName) {
        itemsTemp.remove(itemName);
    }

    public boolean existItemTemp(String itemName) {
        //return false;
        return itemsTemp.containsKey(itemName);
    }
    
    public void removeItem(String itemName) {
        items.remove(itemName);
        itemsState.remove(itemName);
    }

    public int getItemCount() {
        return itemsState.size();
    }

    public String getItemName(int i) {
        if (i < itemsState.size()) {
            return (String) itemsState.keys()[i];
        } else {
            return null;
        }
    }

    public MapItem getMapItem(int index) {
        if (index < itemsState.size()) {
            return (MapItem) items.get(getItemName(index));
        } else
            return null;
    }
    
    public void setEnabled(String itemName, boolean enabled) {
        if (itemsState.containsKey(itemName))
            itemsState.put(itemName, String.valueOf(enabled));
        if (items.containsKey(itemName))
            ((MapItem) items.get(itemName)).setEnabled(enabled);
//        MapItem mapItem = (MapItem) items.get(itemName);
//        if (mapItem != null) {
//            mapItem.setEnabled(enabled);
//        }
    }

    public boolean isEnabled(String itemName) {
        return (String.valueOf(true).equals(itemsState.get(itemName)));
    }
    
    
    public void drawItems(Graphics g) {
        Iterator iter = items.keysIterator();
        while (iter.hasNext()) {
            MapItem item = (MapItem) items.get(iter.next());
            item.drawItem(g);
        }
        
        iter = itemsTemp.keysIterator();
        while (iter.hasNext()) {
            MapItem item = (MapItem) itemsTemp.get(iter.next());
            item.drawItem(g);
        }
    }
    
    public void disableInitializeState() {
        Iterator iter = items.keysIterator();
        while (iter.hasNext()) {
            MapItem item = (MapItem) items.get(iter.next());
            item.disableInitializeState();
        }
        
        iter = itemsTemp.keysIterator();
        while (iter.hasNext()) {
            MapItem item = (MapItem) itemsTemp.get(iter.next());
            item.disableInitializeState();
        }
    }
    
    public void panItem(int x, int y) {
        Iterator iter = items.keysIterator();
        while (iter.hasNext()) {
            MapItem item = (MapItem) items.get(iter.next());
            item.panItem(x, y);
        }
        
        iter = itemsTemp.keysIterator();
        while (iter.hasNext()) {
            MapItem item = (MapItem) itemsTemp.get(iter.next());
            item.panItem(x, y);
        }
    }
    
    public String printItems() {
        String data = "";
        
        for (int i = 0; i < itemsState.size(); i++) {
            data += i + ". - " + getItemName(i) + "\n";
        }
        return data;
    }
    
    public Vector getWaypointsAtPosition(int x, int y, int radius) {
        Vector wayPoints = new Vector();
        
        for (int i = 0; i < itemsState.size(); i++) {
            if (!isFixed(getItemName(i))) {
//System.out.println("check near waypoints: " + getItemName(i));
                MapItem item = (MapItem) items.get(getItemName(i));
                item.getWaypointsAtPosition(wayPoints, x, y, radius);
            }
        }
        return wayPoints;
    }

    public MapItem touchAtPosition(int x, int y) {
        for (int i = 0; i < itemsState.size(); i++) {
            MapItem item = (MapItem) items.get(getItemName(i));
            if (item.touchInside(x, y))
                return item;
        }
        return null;
    }

}
