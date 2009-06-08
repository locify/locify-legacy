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
import de.enough.polish.ui.Choice; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Form; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Item; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.List; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.util.Locale;
import de.enough.polish.ui.ImageItem; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.ItemCommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.StringItem; import de.enough.polish.ui.StyleSheet;

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
        titles = new String[6];
        titles[0] = Locale.get(157);
        titles[1] = Locale.get(59);
        titles[2] = Locale.get(61);
        titles[3] = Locale.get(461);
        titles[4] = Locale.get(63);
        titles[5] = Locale.get(281);

        texts = new String[6];
        texts[0] = Locale.get(158);
        texts[1] = Locale.get(60);
        texts[2] = Locale.get(62);
        texts[3] = "";
        texts[4] = Locale.get(64);
        texts[5] = Locale.get(282);
    }

    /**
     * Views menu of help screen
     */
    public void viewMenu() {
        try {
            list = new List(Locale.get(54), Choice.IMPLICIT);
            for (int i = 0; i < titles.length; i++) {
                list.append(titles[i], null);
            }
            list.addCommand(Commands.cmdBack);
            //#style imgHome
            list.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
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
                form.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
                R.getMidlet().switchDisplayable(null, form);
                form.setCommandListener(this);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "HelpScreen.viewText", null);
        }
    }

    public void viewIconsHelp() {
        //#style helpIcons
        frmIcons = new Form(Locale.get(481), de.enough.polish.ui.StyleSheet.helpiconsStyle );
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_connecting.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get(482));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_nosignal.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get(485));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_weak.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get(487));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_normal.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get(484));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/status_strong.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get(486));
        frmIcons.append(new ImageItem("", IconData.get("locify://icons/manual.png"), ImageItem.LAYOUT_NEWLINE_BEFORE, "text"));
        frmIcons.append(Locale.get(483));

        btnDontShow = new StringItem("", Locale.get(488), StringItem.BUTTON);
        btnDontShow.setDefaultCommand(Commands.cmdSelect);
        btnDontShow.setItemCommandListener(this);
        frmIcons.append(btnDontShow);

        frmIcons.addCommand(Commands.cmdBack);
        //#style imgHome
        frmIcons.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
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
