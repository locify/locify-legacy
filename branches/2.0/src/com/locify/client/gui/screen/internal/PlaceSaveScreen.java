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

import com.locify.client.utils.Locale;
import com.locify.client.utils.R;


/**
 * Manages saving new places
 * @author Destil
 */
public class PlaceSaveScreen {

    public PlaceSaveScreen() {
    }

    public void view() {
        R.getHtmlScreen().reset();
        R.getHtmlScreen().setTitle(Locale.get("Save_place"));
        R.getHtmlScreen().addForm("locify://filesystem/waypoint");
        R.getHtmlScreen().getHtmlBrowser().addContextItem();
        R.getHtmlScreen().addHidden("latitude","$lat");
        R.getHtmlScreen().addHidden("longitude","$lon");
        R.getHtmlScreen().addNewLine();
        R.getHtmlScreen().addTextField(Locale.get("Name"), "name", "");
        R.getHtmlScreen().addNewLine();
        R.getHtmlScreen().addTextField(Locale.get("Description"), "description", " ");
        R.getHtmlScreen().addNewLine();
        R.getHtmlScreen().addButton(Locale.get("Save"), "save", "");
        R.getHtmlScreen().view();
    }
}
