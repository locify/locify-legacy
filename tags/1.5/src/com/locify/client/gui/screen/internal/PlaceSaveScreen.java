/*
 * PlaceSaveScreen.java
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

import de.enough.polish.util.Locale;
import com.locify.client.utils.R;


/**
 * Manages saving new places
 * @author Destil
 */
public class PlaceSaveScreen {

    public PlaceSaveScreen() {
    }

    public void view() {
        R.getHTMLScreen().reset();
        R.getHTMLScreen().setTitle(Locale.get("Save_place"));
        R.getHTMLScreen().addForm("locify://filesystem/waypoint");
        R.getHTMLScreen().getHtmlBrowser().addContextItem();
        R.getHTMLScreen().addHidden("latitude","$lat");
        R.getHTMLScreen().addHidden("longitude","$lon");
        R.getHTMLScreen().addNewLine();
        R.getHTMLScreen().addTextField(Locale.get("Name"), "name", "");
        R.getHTMLScreen().addNewLine();
        R.getHTMLScreen().addTextField(Locale.get("Description"), "description", "");
        R.getHTMLScreen().addButton(Locale.get("Save"), "save", "");
        R.getHTMLScreen().view();
    }
}
