/*
 * InternalURL.java
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

package com.locify.client.gui.manager;

/**
 * Object for one internal url
 * @author Destil
 */
public class InternalURL {

    private String url;
    private String title;
    private String icon;
    private boolean shortcut;

    public InternalURL(String url, String title, String icon, boolean shortcut) {
        this.url = url;
        this.title = title;
        this.icon = icon;
        this.shortcut = shortcut;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isShortcut() {
        return shortcut;
    }
}
