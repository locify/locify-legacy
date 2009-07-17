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

import com.locify.client.net.browser.HtmlForm;
import com.locify.client.data.ServicesData;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.locator.LocationEventGenerator;
import com.locify.client.locator.LocationEventListener;
import com.locify.client.net.browser.HtmlButton;
import com.locify.client.net.browser.HtmlTextArea;
import com.locify.client.net.browser.XHtmlBrowser;
import com.locify.client.net.browser.XHtmlTagHandler;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Command;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * This class uses Polish HTML Browser and extends its functionality
 * @author David Vavra
 */
public class HtmlScreen implements ActionListener, LocationEventListener {

    private FormLocify form;
    private XHtmlBrowser htmlBrowser;
    private Command cmdRefresh = new Command(Locale.get("Refresh_gps"), 2);
    private HtmlForm currentForm;

    public HtmlScreen() {
        form = new FormLocify("");
//        form.setLayout(new BorderLayout());

        htmlBrowser = new XHtmlBrowser(form.getContentPane());
//        form.addComponent(BorderLayout.CENTER, htmlBrowser.);
        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        //another location commands
        // i know about added gps ... actualy i'm lazy :)
        form.addCommand(new ParentCommand(Locale.get("Another_location"), null, R.getContext().commands));
        form.setCommandListener(this);
        R.getLocator().addLocationChangeListener(this);
    }

    /**
     * Renders HTML page
     * @param data html response
     */
    public void view(String data) {
        htmlBrowser.loadPage(data);
        if (R.getContext().getSource() == LocationContext.GPS) {
            form.addCommand(cmdRefresh);
            R.getLocator().startWaitForLocation();
        } else {
            form.removeCommand(cmdRefresh);
        }
        //service commands
        if (ServicesData.getCurrent().equals("Locify")) {
            form.removeCommand(Commands.cmdService);
        } else {
            form.addCommand(Commands.cmdService);
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
        }
        if (updateItemsWithVariables()) {
            form.show();
        }
    }

    public void reset() {
        htmlBrowser.getContainer().removeAll();
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
        currentForm = new HtmlForm("", action, HtmlForm.POST);
    }

    /**
     * Adds textfield to the form
     * @param label
     * @param name
     * @param value
     */
    public void addTextField(String label, String name, String value) {
        htmlBrowser.addComponent(new Label(label));
        HtmlTextArea textField = new HtmlTextArea(value, true);
        htmlBrowser.addComponent(textField);
        this.currentForm.addItem(textField);
        textField.setAttributeForm(this.currentForm);
        textField.setAttributeName(name);
        textField.setAttributeValue(value);
    }

    /**
     * Adds button to the form
     * @param label
     * @param name
     * @param value
     */
    public void addButton(String label, String name, String value) {
        HtmlButton buttonItem = new HtmlButton(label);
        buttonItem.setAttributeType(XHtmlTagHandler.CMD_SUBMIT);
        buttonItem.addActionListener(htmlBrowser.getXHtmlTagHandler());
        htmlBrowser.getXHtmlTagHandler().addCommands("input", "type", XHtmlTagHandler.CMD_SUBMIT, buttonItem);
        htmlBrowser.addComponent(buttonItem);
        this.currentForm.addItem(buttonItem);
        buttonItem.setAttributeForm(this.currentForm);
        buttonItem.setAttributeType(XHtmlTagHandler.CMD_SUBMIT);
        buttonItem.setAttributeName(name);
        buttonItem.setAttributeValue(value);
    }

    /**
     * Adds new line to the page
     */
    public void addNewLine() {
        htmlBrowser.addNewLine();
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
//System.out.println("X1");
        if (R.getContext().getSource() == LocationContext.GPS) {
//System.out.println("X2");
            form.addCommand(cmdRefresh);
//System.out.println("X3");
            R.getLocator().startWaitForLocation();
        } else {
//System.out.println("X4");
            form.removeCommand(cmdRefresh);
//System.out.println("X5");
        }
//System.out.println("X6");
        updateItemsWithVariables();
//System.out.println("X7");
        htmlBrowser.updateContextItem();
//System.out.println("X8");
        form.revalidate();
        form.show();
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
    
    /**
     * Goes through the page and replaces values in form items with variables for coordinates and other Locify stuff
     * @return if update was successfull
     */
    public boolean updateItemsWithVariables() {
        try {
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
            return true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "HTMLScreen.updateItemsWithVariables", null);
            return false;
        }
    }

    public void updateFileInfo(String fileName) {
        htmlBrowser.updateFileItem(fileName);
        R.getURL().call("locify://htmlBrowser");
    }

    public void updateContactTelInfo(String tel) {
        htmlBrowser.updateContactTelItem(tel);
        R.getURL().call("locify://htmlBrowser");
    }

    public void updateContactEmailInfo(String email) {
        htmlBrowser.updateContactEmailItem(email);
        R.getURL().call("locify://htmlBrowser");
    }

    public XHtmlBrowser getHtmlBrowser() {
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
        quit();
        if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == cmdRefresh) {
            updateItemsWithVariables();
        } else if (evt.getCommand() == Commands.cmdMoreInfo) {
            R.getMainScreen().actionCommand(Commands.cmdMoreInfo);
        } else if (evt.getCommand() == Commands.cmdServiceSettings) {
            R.getMainScreen().actionCommand(Commands.cmdServiceSettings);
        } else if (evt.getCommand() == Commands.cmdUpdateService) {
            R.getMainScreen().actionCommand(Commands.cmdUpdateService);
        } else {
            for (int i = 0; i < R.getContext().commands.length; i++) {
                if (evt.getCommand() == R.getContext().commands[i]) {
                    R.getContext().setTemporaryScreen("locify://htmlBrowser");
                    R.getURL().call(R.getContext().actions[i]);
                    return;
                }
            }
        }
    }
}
