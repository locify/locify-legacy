/*
 * Help.java
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

import com.locify.client.data.IconData;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

/**
 * This class is used for showing help screens
 * @author David Vavra
 */
public class HelpScreen implements CommandListener, ItemCommandListener {

    private String[] titles;
    private String[] texts;
    private List list;
    private Form form;
    private Form frmIcons;
    private StringItem btnDontShow;

    public HelpScreen() {
        titles = new String[8];
        titles[0] = Locale.get("What_is_locify_for");
        titles[1] = Locale.get("How_to_add_service");
        titles[2] = Locale.get("How_to_connect_gps");
        titles[3] = Locale.get("What_coloured_satellites_mean");
        titles[4] = Locale.get("What_is_login_for");
        titles[5] = Locale.get("What_is_synchronized");
        titles[6] = Locale.get("How_to_manage_services");
        titles[7] = Locale.get("Credits");

        texts = new String[8];
        texts[0] = Locale.get("What_is_locify_for_text");
        texts[1] = Locale.get("How_to_add_service_text");
        texts[2] = Locale.get("How_to_connect_gps_text");
        texts[3] = "";
        texts[4] = Locale.get("What_is_login_for_text");
        texts[5] = Locale.get("What_is_synchronized_text");
        texts[6] = Locale.get("How_to_manage_services_text");
        texts[7] = Locale.get("Credits_text");
    }

    /**
     * Views menu of help screen
     */
    public void viewMenu() {
        try {
            list = new List(Locale.get("Help"), Choice.IMPLICIT);
            for (int i = 0; i < titles.length; i++) {
                list.append(titles[i], null);
            }
            list.addCommand(Commands.cmdBack);
            //#style imgHome
            list.addCommand(Commands.cmdHome);
            R.getMidlet().switchDisplayable(null, list);
            list.setCommandListener(this);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "HelpScreen.viewMenu", null);
        }
    }

    /**
     * Views one selected help text
     */
    public void viewText(int selected) {
        try {
            if (selected == 3) {
                viewIconsHelp();
            } else {
                form = new Form(titles[selected]);
                form.append(texts[selected]);
                form.addCommand(Commands.cmdBack);
                //#style imgHome
                form.addCommand(Commands.cmdHome);
                R.getMidlet().switchDisplayable(null, form);
                form.setCommandListener(this);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "HelpScreen.viewText", null);
        }
    }

    public void viewIconsHelp() {
        //#style helpIcons
        frmIcons = new Form(Locale.get("Icons_help"));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_connecting.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get("Connecting_help"));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_nosignal.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get("Nosignal_help"));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_weak.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get("Weak_help"));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_normal.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get("Normal_help"));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_strong.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get("Strong_help"));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/manual.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get("Manual_help"));

        btnDontShow = new StringItem("", Locale.get("Dont_show_next_time"), StringItem.BUTTON);
        btnDontShow.setDefaultCommand(Commands.cmdSelect);
        btnDontShow.setItemCommandListener(this);
        frmIcons.append(btnDontShow);

        frmIcons.addCommand(Commands.cmdBack);
        //#style imgHome
        frmIcons.addCommand(Commands.cmdHome);
        R.getMidlet().switchDisplayable(null, frmIcons);
        frmIcons.setCommandListener(this);
    }

    /**
     * Handles reaction to command events
     * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == List.SELECT_COMMAND) {
            R.getURL().call("locify://help?text="+list.getSelectedIndex());
        } else if (command == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }

    public void commandAction(Command c, Item item) {
        if (item == btnDontShow) {
            R.getSettings().setShowIconsHelp(false);
            R.getURL().call("locify://back");
        }
    }
}
