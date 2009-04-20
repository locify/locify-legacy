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

package com.locify.client.gui.screen.internal;

/**
 * Object for one main screen item
 * @author David Vavra
 */
public class MainScreenItem{
    private String icon;
    private String id, title;
    private long timestamp;

    public MainScreenItem(String id, String title, String icon, int order, long timestamp) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.timestamp = timestamp;
    }
    
    public String toString()
    {
        return "{id="+id+",title="+title+",icon="+icon+",timestamp="+timestamp+"}";
    }

    public boolean isService() {
        return (id.startsWith("http://") && !isShortcutToDownload());
    }

    public boolean isShortcut() {
        return id.startsWith("locify://");
    }
    
    public boolean isShortcutToDownload()
    {
       return icon.equals("locify://icons/shortcut_to_download_25x25.png");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
