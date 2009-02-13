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
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

/**
 * Displays exceptions in application
 * @author Destil
 */
public class ErrorScreen implements CommandListener, ItemCommandListener {

    private Form frmCalmDown;
    private Form frmDetail;
    private Command cmdDetail = new Command(Locale.get("Detail"), Command.SCREEN, 3);
    private Command cmdSave = new Command(Locale.get("Save"), Command.SCREEN, 4);
    private Command cmdExit = new Command(Locale.get("Exit"), Command.SCREEN, 5);
    private Command cmdLogger = new Command(Locale.get("Logger"), Command.SCREEN, 6);
    private Exception exception;
    private String errorPlace;
    private String errorData;
    private StringItem btnDetail;
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
        Logger.error(exception.toString());
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
        frmCalmDown.append(Locale.get("Error_occured_description"));
        btnDetail =  new StringItem("", Locale.get("Detail"), StringItem.BUTTON);
        btnDetail.setDefaultCommand(Commands.cmdSelect);
        btnDetail.setItemCommandListener(this);
        frmCalmDown.append(btnDetail);
        frmCalmDown.addCommand(Commands.cmdBack);
        //#style imgHome
        frmCalmDown.addCommand(Commands.cmdHome);
        frmCalmDown.addCommand(cmdExit);
        frmCalmDown.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmCalmDown);
    }

    /**
     * Views detail of last exception
     */
    public void viewDetail() {
        frmDetail = new Form(Locale.get("Error_detail"));
        StringItem siDescription = new StringItem(Locale.get("Description"), exception.toString());
        frmDetail.append(siDescription);
        if (errorPlace != null) {
            StringItem siPlace = new StringItem(Locale.get("Place"), errorPlace);
            frmDetail.append(siPlace);
        }
        if (errorData != null) {
            StringItem siData = new StringItem(Locale.get("Data"), errorData);
            frmDetail.append(siData);
        }
        //#style imgSaved
        frmDetail.addCommand(cmdSave);
        frmDetail.addCommand(Commands.cmdBack);
        //#style imgHome
        frmDetail.addCommand(Commands.cmdHome);
        //#if !release
        //#style imgLogger
        frmDetail.addCommand(cmdLogger);
        //#endif
        frmDetail.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmDetail);
        R.getMidlet().switchDisplayable(null, frmDetail);
    }

    private void saveError() {
        String errorReport = "Description:\n" + exception.toString() + "\n\n" + "Place:\n" + errorPlace + "\n\n" + "Additional data:\n" + errorData + "\n";
        R.getFileSystem().saveString(FileSystem.LOG_FOLDER + "error" + String.valueOf(Utils.timestamp()) + ".txt", errorReport);
        R.getCustomAlert().quickView(Locale.get("Error_saved"), Locale.get("Info"), "locify://refresh");
    }

    public void commandAction(Command command, Displayable displayable) {
        firstError = false;
        if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (command == cmdSave) {
            saveError();
        } else if (command == cmdExit) {
            R.getMidlet().switchDisplayable(null, null);
            R.getMidlet().destroyApp(true);
            R.getMidlet().notifyDestroyed();
        } else if (command == cmdDetail)
        {
            R.getURL().call("locify://errorDetail");
        } else if (command == cmdLogger)
        {
            R.getURL().call("locify://logger");
        }
    }

    /**
     * Return true if there is no error or error was already revised by user
     * @return
     */
    public boolean isNoErrorOrViewed() {
        return !firstError;
    }

    public void commandAction(Command c, Item i) {
        commandAction(cmdDetail, frmCalmDown);
    }
}
