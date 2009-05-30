/*
 * ErrorScreen.java
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

import com.locify.client.data.FileSystem;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * Displays exceptions in application
 * @author Destil
 */
public class ErrorScreen implements ActionListener {

    private Form frmCalmDown;
    private Form frmDetail;
    private Command cmdDetail = new Command(Locale.get("Detail"), 3);
    private Command cmdSave = new Command(Locale.get("Save"), 4);
    private Command cmdExit = new Command(Locale.get("Exit"), 5);
    private Command cmdLogger = new Command(Locale.get("Logger"), 6);
    private Exception exception;
    private String errorPlace;
    private String errorData;
    private Button btnDetail;
    private boolean firstError = false;

    public ErrorScreen() {
    }

    /**
     * Displays one exception
     * @param exception exception full object
     * @param errorPlace description of function, piece of code etc.
     * @param errorData optional data
     */
    public void view(Exception exception, String errorPlace, String errorData) {
        Logger.error(exception.toString()+" place: "+errorPlace+", data: "+errorData);
        exception.printStackTrace();
        if (firstError == false) {
            this.exception = exception;
            this.errorPlace = errorPlace;
            this.errorData = errorData;
            viewCalmDownScreen();
            firstError = true;
        }
    }

    private void viewCalmDownScreen() {
        frmCalmDown = new Form(Locale.get("Error_occured"));
        frmCalmDown.addComponent(new Label(Locale.get("Error_occured_description")));
        btnDetail =  new Button(Locale.get("Detail"));
        btnDetail.addActionListener(this);
        frmCalmDown.addComponent(btnDetail);
        frmCalmDown.addCommand(Commands.cmdBack);
        frmCalmDown.addCommand(Commands.cmdHome);
        frmCalmDown.addCommand(cmdExit);
        frmCalmDown.setCommandListener(this);
        frmCalmDown.show();
    }

    /**
     * Views detail of last exception
     */
    public void viewDetail() {
        frmDetail = new Form(Locale.get("Error_detail"));
        Label siDescription01 = new Label(Locale.get("Description"));
        Label siDescription02 = new Label(exception.toString());
        frmDetail.addComponent(siDescription01);
        frmDetail.addComponent(siDescription02);
        if (errorPlace != null) {
            Label siPlace01 = new Label(Locale.get("Place"));
            Label siPlace02 = new Label(errorPlace);
            frmDetail.addComponent(siPlace01);
            frmDetail.addComponent(siPlace02);
        }
        if (errorData != null) {
            Label siData01 = new Label(Locale.get("Data"));
            Label siData02 = new Label(errorData);
            frmDetail.addComponent(siData01);
            frmDetail.addComponent(siData02);
        }
        frmDetail.addCommand(cmdSave);
        frmDetail.addCommand(Commands.cmdBack);
        frmDetail.addCommand(Commands.cmdHome);
        //#if !release
        frmDetail.addCommand(cmdLogger);
        //#endif
        frmDetail.setCommandListener(this);
        frmDetail.show();
    }

    private void saveError() {
        String errorReport = "Description:\n" + exception.toString() + "\n\n" + "Place:\n" + errorPlace + "\n\n" + "Additional data:\n" + errorData + "\n";
        R.getFileSystem().saveString(FileSystem.LOG_FOLDER + "error" + String.valueOf(Utils.timestamp()) + ".txt", errorReport);
        R.getCustomAlert().quickView(Locale.get("Error_saved"), Locale.get("Info"), "locify://refresh");
    }

    /**
     * Return true if there is no error or error was already revised by user
     * @return
     */
    public boolean isNoErrorOrViewed() {
        return !firstError;
    }

    public void actionPerformed(ActionEvent evt) {
        firstError = false;
        if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == cmdSave) {
            saveError();
        } else if (evt.getCommand() == cmdExit) {
            R.getURL().call("locify://exit");
        } else if (evt.getCommand() == cmdDetail || evt.getSource() == btnDetail) {
            R.getURL().call("locify://errorDetail");
        } else if (evt.getCommand() == cmdLogger) {
            R.getURL().call("locify://logger");
        }
    }
}
