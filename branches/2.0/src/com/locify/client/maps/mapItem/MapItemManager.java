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

import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.utils.R;
import com.locify.client.data.items.Waypoint;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.sun.lwuit.CheckBox;
import com.sun.lwuit.Container;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


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
        if (!isFixed(Locale.get("Scale"))) {
            MapItem scale = new ScaleMapItem();
            scale.priority = MapItem.PRIORITY_HIGH;
            scale.setEnabled(true);
            addItemFixed(Locale.get("Scale"), scale);
        }
    }

    /**
     * Add item to manager as fixed. Fixed item cannot be overwritten, cannot be selected.
     * @param itemName name of item
     * @param item MapItem to show
     * @return true if added, falsee if same name already exist
     */
    public boolean addItemFixed(String itemName, MapItem item) {
        if (!items.containsKey(itemName)) {
            item.fixed = true;
            items.put(itemName, item);
            return true;
        }
        return false;
    }

    private boolean isFixed(String name) {
        MapItem item = (MapItem) items.get(name);
        if (item == null) {
            return false;
        } else {
            return item.fixed;
        }
    }

    /**
     * Add item to manager, if item of same name exist, overwrite it !!!
     * @param itemName name of item
     * @param item MapItem to show
     * @param priority
     */
    public void addItem(String itemName, MapItem item, int priority) {
        try {
            /* change navigation along the map while new waypoint is coming from network link */
            if (MapScreen.isNowDirectly() && item instanceof PointMapItem) {
                //update map navigation
                if (R.getMapScreen().isMapNavigationRunning()) {
                    MapNavigationItem navItem = (MapNavigationItem) getItemTemp(MapScreen.tempMapNavigationItem);
                    Waypoint target = navItem.getTargetWaypoint();
                    if (target != null) {
                        navItem.setTargetWaypoint(((PointMapItem) item).getWaypointById(target.id));
                    }
                }
                //update compass navigation
                if (R.getMapScreen().getDifferentScreenLock()) {
                    String navId = R.getNavigationScreen().getWaypointId();
                    if (navId != null) {
                        Waypoint waypoint = ((PointMapItem) item).getWaypointById(navId);
                        if (waypoint == null) {
                            R.getNavigationScreen().removeNavigator();
                        } else {
                            R.getNavigationScreen().updateWaypoint(waypoint);
                        }
                    }
                }
                //update selected item
                if (getItemTemp(MapScreen.tempWaypointDescriptionItemName) != null) {
                    DescriptionMapItem navItem = (DescriptionMapItem) getItemTemp(MapScreen.tempWaypointDescriptionItemName);
                    String navId = navItem.getWaypoint().id;
                    if (navId != null) {
                        navItem.updateWaypoint(((PointMapItem) item).getWaypointById(navId));
                    }
                }
            }
            item.priority = priority;
            items.put(itemName, item);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItemManager.addItem()", itemName);
        }
    }

    public MapItem getItem(String itemName) {
        return (MapItem) items.get(itemName);
    }

    public MapItem getItem(int index) {
        if (index < items.size()) {
            return (MapItem) items.get(getItemName(index));
        } else {
            return null;
        }
    }

    public String getItemName(int i) {
        if (i < items.size()) {
            Enumeration enu = items.keys();
            int counter = 0;
            while (enu.hasMoreElements()) {
                if (counter == i) {
                    return (String) enu.nextElement();
                }

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
                if (item != null && item.priority == priority) {
                    item.drawItem(g);
                }
            }

            enu = itemsTemp.keys();
            while (enu.hasMoreElements()) {
                item = (MapItem) itemsTemp.get(enu.nextElement());
                if (item != null && item.priority == priority) {
                    item.drawItem(g);
                }
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
        try {
            Enumeration enu = items.elements();
            MapItem item;
            while (enu.hasMoreElements()) {
                item = (MapItem) enu.nextElement();
                if (item != null) {
                    item.panItem(x, y);
                }
            }
            enu = itemsTemp.elements();
            while (enu.hasMoreElements()) {
                item = (MapItem) enu.nextElement();
                if (item != null) {
                    item.panItem(x, y);
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapItemManager.panItem", null);
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
                item.getWaypointsAtPositionByPoint(wayPoints, x, y, radiusSquare);
            }
        }

        // no waypoints founded ... try to find any waypoints icons
        if (wayPoints.size() == 0) {
            enu = items.keys();
            while (enu.hasMoreElements()) {
                name = (String) enu.nextElement();
                if (!isFixed(name)) {
                    item = (MapItem) items.get(name);
                    item.getWaypointsAtPositionByIcon(wayPoints, x, y);
                }
            }
        }
        return wayPoints;
    }

    public Waypoint getWaypointById(String id) {
        Enumeration enu = items.elements();
        MapItem item;
        while (enu.hasMoreElements()) {
            item = (MapItem) enu.nextElement();
            if (item instanceof PointMapItem) {
                return ((PointMapItem) item).getWaypointById(id);
            }
        }
        return null;
    }
    private FormLocify form;
    private Container cgMapItems;
    private Container cgMapItemsFixed;

    public void viewMapSettings() {
        form = new FormLocify(Locale.get("Maps"));
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));

        cgMapItemsFixed = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        cgMapItems = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        for (int i = 0; i < getItemCount(); i++) {
            MapItem mi = getItem(i);
            String miName = getItemName(i);
            CheckBox chb = new CheckBox(miName);
            chb.setSelected(mi.enabled);

            if (mi.fixed) {
                cgMapItemsFixed.addComponent(chb);
            } else {
                cgMapItems.addComponent(chb);
            }
        }

        if (cgMapItemsFixed.getComponentCount() > 0) {
            form.addComponent(new Label(Locale.get("Permanent_map_items")));
            form.addComponent(cgMapItemsFixed);
            form.addComponent(new Label(""));
        }
        if (cgMapItems.getComponentCount() > 0) {
            form.addComponent(new Label(Locale.get("Temporary_map_items")));
            form.addComponent(cgMapItems);
        }

        form.addCommand(Commands.cmdOK);
        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.setCommandListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (evt.getCommand() == Commands.cmdBack) {
                    R.getMapScreen().view();
                } else if (evt.getCommand() == Commands.cmdHome) {
                    R.getMapScreen().actionCommand(Commands.cmdHome);
                } else if (evt.getCommand() == Commands.cmdOK) {
                    for (int i = 0; i < cgMapItemsFixed.getComponentCount(); i++) {
                        CheckBox chb = (CheckBox) cgMapItemsFixed.getComponentAt(i);
                        setEnabled(chb.getText(), chb.isSelected());
                    }
                    for (int i = 0; i < cgMapItems.getComponentCount(); i++) {
                        CheckBox chb = (CheckBox) cgMapItems.getComponentAt(i);
                        setEnabled(chb.getText(), chb.isSelected());
                    }

                    R.getMapScreen().view();
                }
            }
        });

        form.show();
    }
}
