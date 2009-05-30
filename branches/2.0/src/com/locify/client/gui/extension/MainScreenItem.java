/**
 * MainScreenItem.java
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

package com.locify.client.gui.extension;

import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.list.ListCellRenderer;

/**
 * Object for one main screen item
 * @author David Vavra
 */
public class MainScreenItem extends Button implements ListCellRenderer {
    private String iconUrl;
    private String id;
    private long timestamp;

    public MainScreenItem(String id, String title, String iconUrl, int order, long timestamp) {
        this.id = id;
        setText(title);
        this.iconUrl = iconUrl;
        this.timestamp = timestamp;
    }
    
    public String toString() {
        return "{id = " + id + ", title = " + getText() + ", icon = " + iconUrl + ", timestamp = " + timestamp + "}";
    }

    public boolean isService() {
        return (id.startsWith("http://") && !isShortcutToDownload());
    }

    public boolean isShortcut() {
        return id.startsWith("locify://");
    }
    
    public boolean isShortcutToDownload() {
       return iconUrl.equals("locify://icons/shortcut_to_download_25x25.png");
    }

    public String getId() {
        return id;
    }

    public String getIconURL() {
        return iconUrl;
    }

    public void setIcon(String icon) {
        this.iconUrl = icon;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        return this;
    }

    public Component getListFocusComponent(List list) {
        Label label = new Label();
        label.setText(getText());
        label.setIcon(getIcon());
        return label;
    }
}
