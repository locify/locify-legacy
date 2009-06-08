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
import com.locify.client.utils.Logger;
import com.locify.client.data.items.Waypoint;
import com.locify.client.utils.Commands;
import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Spacer;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.Locale;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.android.lcdui.Graphics;

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
        if (!isFixed(Locale.get(365))) {
            MapItem scale = new ScaleMapItem();
            scale.priority = MapItem.PRIORITY_HIGH;
            scale.setEnabled(true);
            addItemFixed(Locale.get(365), scale);
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
    private Form frmItemManager;
    private ChoiceGroup cgMapItems;
    private ChoiceGroup cgMapItemsFixed;

    public void viewMapSettings() {
        frmItemManager = new Form(Locale.get(252));

        cgMapItemsFixed = new ChoiceGroup("", Choice.MULTIPLE);
        cgMapItems = new ChoiceGroup("", Choice.MULTIPLE);
        for (int i = 0; i < getItemCount(); i++) {
            MapItem mi = getItem(i);
            String miName = getItemName(i);
            if (mi.fixed) {
                cgMapItemsFixed.append(miName, null);
                cgMapItemsFixed.setSelectedIndex(cgMapItemsFixed.size() - 1, mi.enabled);
            } else {
                cgMapItems.append(miName, null);
                cgMapItems.setSelectedIndex(cgMapItems.size() - 1, mi.enabled);
            }
        }

        if (cgMapItemsFixed.size() > 0) {
            frmItemManager.append(new StringItem(Locale.get(514), null));
            frmItemManager.append(cgMapItemsFixed);
            frmItemManager.append(new Spacer(100, 5));
        }
        if (cgMapItems.size() > 0) {
            frmItemManager.append(new StringItem(Locale.get(515), null));
            frmItemManager.append(cgMapItems);
        }

        frmItemManager.addCommand(Commands.cmdOK);
        frmItemManager.addCommand(Commands.cmdBack);
        //#style imgHome
        frmItemManager.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmItemManager.setCommandListener(new CommandListener() {

            public void commandAction(Command c, Displayable d) {
                if (c.equals(Commands.cmdBack)) {
                    R.getMapScreen().view();
                } else if (c.equals(Commands.cmdHome)) {
                    R.getMapScreen().commandAction(Commands.cmdHome, null);
                } else if (c.equals(Commands.cmdOK)) {
                    for (int i = 0; i < cgMapItemsFixed.size(); i++) {
                        setEnabled(cgMapItemsFixed.getString(i), cgMapItemsFixed.isSelected(i));
                    }
                    for (int i = 0; i < cgMapItems.size(); i++) {
                        setEnabled(cgMapItems.getString(i), cgMapItems.isSelected(i));
                    }

                    R.getMapScreen().view();
                }
            }
        });

        R.getMidlet().switchDisplayable(null, frmItemManager);
    }
}
