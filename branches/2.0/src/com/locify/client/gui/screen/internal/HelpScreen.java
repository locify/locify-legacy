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
import com.locify.client.gui.extension.FlowLayoutYScroll;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ListLocify;
import com.locify.client.net.browser.HtmlTextArea;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;

/**
 * This class is used for showing help screens
 * @author David Vavra
 */
public class HelpScreen implements ActionListener {

    private String[] titles;
    private String[] texts;
    private ListLocify list;
    private FormLocify form;
    private FormLocify frmIcons;
    private Button btnDontShow;

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
            form = new FormLocify(Locale.get("Help"));
            list = new ListLocify();

            for (int i = 0; i < titles.length; i++) {
                list.addItem(titles[i]);
            }

            list.addActionListener(this);
            form.addComponent(list);
            form.addCommand(Commands.cmdHome);
            form.setCommandListener(this);
            form.show();
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
                form = new FormLocify(titles[selected]);
                form.addComponent(new HtmlTextArea(texts[selected], false));

                /*form.setLayout(new FlowLayout());
                TextArea ta = new TextArea(texts[selected]);
                ta.setEditable(false);
                ta.setSingleLineTextArea(false);
                ta.setGrowByContent(true);
                ta.setFocusable(false);
                TextArea tb = new TextArea(texts[selected]);
                tb.setEditable(false);
                tb.setSingleLineTextArea(false);
                tb.setGrowByContent(true);
                tb.setFocusable(false);
                form.addComponent(ta);
                form.addComponent(tb);*/

                form.addCommand(Commands.cmdBack);
                form.addCommand(Commands.cmdHome);
                form.setCommandListener(this);
                form.show();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "HelpScreen.viewText", null);
        }
    }

    public void viewIconsHelp() {
        frmIcons = new FormLocify(Locale.get("Icons_help"));
        frmIcons.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Label label01 = new Label(IconData.get("locify://icons/status_connecting.png"));
        label01.setText(Locale.get("Connecting_help"));
        frmIcons.addComponent(label01);
        Label label02 = new Label(IconData.get("locify://icons/status_nosignal.png"));
        label02.setText(Locale.get("Nosignal_help"));
        frmIcons.addComponent(label02);
        Label label03 = new Label(IconData.get("locify://icons/status_weak.png"));
        label03.setText(Locale.get("Weak_help"));
        frmIcons.addComponent(label03);
        Label label04 = new Label(IconData.get("locify://icons/status_normal.png"));
        label04.setText(Locale.get("Normal_help"));
        frmIcons.addComponent(label04);
        Label label05 = new Label(IconData.get("locify://icons/status_strong.png"));
        label05.setText(Locale.get("Strong_help"));
        frmIcons.addComponent(label05);
        Label label06 = new Label(IconData.get("locify://icons/manual.png"));
        label06.setText(Locale.get("Manual_help"));
        frmIcons.addComponent(label06);

        btnDontShow = new Button(Locale.get("Dont_show_next_time"));
        btnDontShow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                R.getSettings().setShowIconsHelp(false);
                R.getURL().call("locify://back");
            }
        });
        frmIcons.addComponent(btnDontShow);

        frmIcons.addCommand(Commands.cmdBack);
        frmIcons.addCommand(Commands.cmdHome);
        frmIcons.setCommandListener(this);
        frmIcons.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getSource() == list) {
            R.getURL().call("locify://help?text=" + list.getSelectedIndex());
        }
    }
}
