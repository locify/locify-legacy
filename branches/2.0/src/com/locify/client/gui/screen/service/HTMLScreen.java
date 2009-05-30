/*
 * HTMLScreen.java
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
package com.locify.client.gui.screen.service;

import com.locify.client.data.ServicesData;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.net.Variables;
import com.locify.client.net.XHTMLBrowser;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.locify.client.net.XHTMLTagHandler;
import com.locify.client.utils.Locale;
import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * This class uses Polish HTML Browser and extends its functionality
 * @author David Vavra
 */
public class HTMLScreen implements ActionListener, LocationEventListener {

    private Form form;
    private XHTMLBrowser htmlBrowser;
    private Command cmdRefresh = new Command(Locale.get("Refresh_gps"), 2);
    private HtmlForm currentForm;

    public HTMLScreen() {
////
////        form = new Form("");
////
////        this.htmlBrowser = new XHTMLBrowser();
////        form.append(this.htmlBrowser);
////        form.addCommand(Commands.cmdBack);
////        //#style imgHome
////        form.addCommand(Commands.cmdHome);
////        //another location commands
////        //#style imgWhere
////        form.addCommand(Commands.cmdAnotherLocation);
////        for (int i = 0; i < R.getContext().commands.length; i++) {
////            if (i != LocationContext.GPS) {
////                UiAccess.addSubCommand(R.getContext().commands[i], Commands.cmdAnotherLocation, form);
////            }
////        }
////        form.setCommandListener(this);
////        R.getLocator().addLocationChangeListener(this);
    }

    /**
     * Renders HTML page
     * @param data html response
     */
    public void view(String data) {
////        htmlBrowser.loadPage(data);
////        if (R.getContext().getSource() == LocationContext.GPS) {
////            //#style imgGps
////            form.addCommand(cmdRefresh);
////            R.getLocator().startWaitForLocation();
////        } else {
////            form.removeCommand(cmdRefresh);
////        }
////        //service commands
////        if (ServicesData.getCurrent().equals("Locify")) {
////            form.removeCommand(R.getMainScreen().cmdService);
////        } else {
////            //#style imgManage
////            form.addCommand(R.getMainScreen().cmdService);
////            Utils.removeSubCommand(R.getMainScreen().cmdMoreInfo, R.getMainScreen().cmdService, form);
////            Utils.removeSubCommand(R.getMainScreen().cmdServiceSettings, R.getMainScreen().cmdService, form);
////            Utils.removeSubCommand(R.getMainScreen().cmdUpdateService, R.getMainScreen().cmdService, form);
////            //#style imgInfo
////            UiAccess.addSubCommand(R.getMainScreen().cmdMoreInfo, R.getMainScreen().cmdService, form);
////            if (!"".equals(ServicesData.getService(ServicesData.getCurrent()).getSettingsUrl())) {
////                //#style imgServiceSettings
////                UiAccess.addSubCommand(R.getMainScreen().cmdServiceSettings, R.getMainScreen().cmdService, form);
////            }
////            //#style imgUpdateService
////            UiAccess.addSubCommand(R.getMainScreen().cmdUpdateService, R.getMainScreen().cmdService, form);
////        }
////        if (updateItemsWithVariables()) {
////            form.show();
////        }
////        htmlBrowser.focus(0);
    }

    public void reset() {
////        htmlBrowser.clear();
    }

    /**
     * Sets screen title
     * @param title
     */
    public void setTitle(String title) {
        form.setTitle(title);
    }

    /**
     * Adds form to the screen
     * @param action URL of  form's action
     */
    public void addForm(String action) {
        currentForm = new HtmlForm("", action, "POST");
    }

    /**
     * Adds textfield to the form
     * @param label
     * @param name
     * @param value
     */
    public void addTextField(String label, String name, String value) {
////        //#style textfield
////        TextField textField = new TextField(label, value, 100, TextField.ANY);
////        htmlBrowser.add(textField);
////        this.currentForm.addItem(textField);
////        textField.setAttribute("polish_form", this.currentForm);
////        textField.setAttribute("name", name);
////        textField.setAttribute("value", value);
    }

    /**
     * Adds button to the form
     * @param label
     * @param name
     * @param value
     */
    public void addButton(String label, String name, String value) {
////        //#style button
////        StringItem buttonItem = new StringItem(null, label);
////        buttonItem.setDefaultCommand(XHTMLTagHandler.CMD_SUBMIT);
////        buttonItem.setItemCommandListener(htmlBrowser.getTagHandler());
////        htmlBrowser.getTagHandler().addCommands("input", "type", "submit", buttonItem);
////        htmlBrowser.add(buttonItem);
////        this.currentForm.addItem(buttonItem);
////        buttonItem.setAttribute("polish_form", this.currentForm);
////        buttonItem.setAttribute("type", "submit");
////        buttonItem.setAttribute("name", name);
////        buttonItem.setAttribute("value", value);
    }

    /**
     * Adds new line to the page
     */
    public void addNewLine() {
////        StringItem stringItem = new StringItem(null, null);
////        stringItem.setLayout(Item.LAYOUT_NEWLINE_AFTER);
////        htmlBrowser.add(stringItem);
    }


    /**
     * Adds hidden element into current form
     * @param name
     * @param value
     */
    public void addHidden(String name, String value) {
        this.currentForm.addHiddenElement(name, value);
    }

    /**
     * Views current browser without reloading
     */
    public void view() {
////        if (R.getContext().getSource() == LocationContext.GPS) {
////            //#style imgGps
////            form.addCommand(cmdRefresh);
////            R.getLocator().startWaitForLocation();
////        } else {
////            form.removeCommand(cmdRefresh);
////        }
////        updateItemsWithVariables();
////        htmlBrowser.updateContextItem();
////        R.getMidlet().switchDisplayable(null, form);
    }

    /**
     * Performs additional task when moving to different screen
     */
    public void quit() {
        R.getLocator().stopWaitForLocator();
        R.getAutoSend().stop();
        if (R.getContext().isTemporary()) {
            R.getContext().removeTemporaryLocation();
        }
    }

////    public void commandAction(Command c, Displayable d) {
////        quit();
////        if (c == Commands.cmdBack) {
////            R.getBack().goBack();
////        } else if (c == Commands.cmdHome) {
////            R.getURL().call("locify://mainScreen");
////        } else if (c == cmdRefresh) {
////            updateItemsWithVariables();
////        } else if (c == R.getMainScreen().cmdMoreInfo) {
////            R.getMainScreen().commandAction(R.getMainScreen().cmdMoreInfo, form);
////        } else if (c == R.getMainScreen().cmdServiceSettings) {
////            R.getMainScreen().commandAction(R.getMainScreen().cmdServiceSettings, form);
////        } else if (c == R.getMainScreen().cmdUpdateService) {
////            R.getMainScreen().commandAction(R.getMainScreen().cmdUpdateService, form);
////        } else {
////            for (int i = 0; i < R.getContext().commands.length; i++) {
////                if (c == R.getContext().commands[i]) {
////                    R.getContext().setTemporaryScreen("locify://htmlBrowser");
////                    R.getURL().call(R.getContext().actions[i]);
////                    return;
////                }
////            }
////        }
////    }

    /**
     * Goes through the page and replaces values in form items with variables for coordinates and other Locify stuff
     * @return if update was successfull
     */
    public boolean updateItemsWithVariables() {
////        try {
////            Item[] items = htmlBrowser.getItems();
////            for (int i = 0; i < items.length; i++) {
////                Item item = items[i];
////                String originalValue = (String) item.getAttribute("value");
////                if (originalValue != null && originalValue.indexOf("$") == 0) {
////                    if (item instanceof TextField) {
////                        String newValue = Variables.replace(originalValue, false);
////                        if (newValue == null) {
////                            return false; //gps context not selected
////                        }
////                        TextField textField = (TextField) item;
////                        textField.setString(newValue);
////                    }
////                }
////            }
////            return true;
////        } catch (Exception e) {
////            R.getErrorScreen().view(e, "HTMLScreen.updateItemsWithVariables", null);
            return false;
////        }
    }

    public void updateFileInfo(String fileName) {
        htmlBrowser.updateFileItem(fileName);
        R.getURL().call("locify://htmlBrowser");
    }

    public void updateContactTelInfo(String tel) {
////        htmlBrowser.updateContactTelItem(tel);
        R.getURL().call("locify://htmlBrowser");
    }

    public void updateContactEmailInfo(String email) {
////        htmlBrowser.updateContactEmailItem(email);
        R.getURL().call("locify://htmlBrowser");
    }

    public XHTMLBrowser getHtmlBrowser() {
        return htmlBrowser;
    }

    public void locationChanged(LocationEventGenerator sender, Location4D location) {
    }

    public void stateChanged(LocationEventGenerator sender, int state) {
    }

    public void errorMessage(LocationEventGenerator sender, String message) {
    }

    public void message(LocationEventGenerator sender, String message) {
        if ("Has_fix".equals(message)) {
            updateItemsWithVariables();
            htmlBrowser.updateContextItem();
        }
    }

    public void actionPerformed(ActionEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
