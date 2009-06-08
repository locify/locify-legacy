/*
 * ServiceManager.java
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

import com.locify.client.data.Service;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Form; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Item; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.ItemCommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.StringItem; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField; import de.enough.polish.ui.StyleSheet;

/**
 * Manages user interface for service adding, renaming, serviceInfo etc.
 * @author Destil
 */
public class ServiceManager implements CommandListener, ItemCommandListener {

    private Form frmServiceInfo;
    private Form frmRenameService;
    private Form frmAddByLink;
    private TextField tfNewName;
    private TextField tfServiceUrl;
    private StringItem btnRename;
    private StringItem btnAdd;
    private String previousUrl = "http://";

    public ServiceManager() {
    }

    public void viewServiceInfo(Service service) {
        frmServiceInfo = new Form(service.getName());       
        StringItem siServiceDescription = new StringItem(Locale.get(136), service.getDescription());
        frmServiceInfo.append(siServiceDescription);
        frmServiceInfo.addCommand(Commands.cmdBack);
        //#style imgHome
        frmServiceInfo.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmServiceInfo.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmServiceInfo);
    }

    public void viewRenameService(String original) {
        frmRenameService = new Form(Locale.get(123));
        tfNewName = new TextField(Locale.get(109), original, 100, TextField.ANY);
        frmRenameService.append(tfNewName);
        btnRename = new StringItem("", Locale.get(123), StringItem.BUTTON);
        btnRename.setDefaultCommand(Commands.cmdSend);
        btnRename.setItemCommandListener(this);
        frmRenameService.append(btnRename);
        frmRenameService.addCommand(Commands.cmdBack);
        //#style imgHome
        frmRenameService.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmRenameService.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmRenameService);
    }

    public void viewAddByLink() {
        frmAddByLink = new Form(Locale.get(257));
        tfServiceUrl = new TextField(Locale.get(137), previousUrl, 500, TextField.URL);
        frmAddByLink.append(tfServiceUrl);
        btnAdd = new StringItem("", Locale.get(5), StringItem.BUTTON);
        btnAdd.setDefaultCommand(Commands.cmdSend);
        btnAdd.setItemCommandListener(this);
        frmAddByLink.append(btnAdd);
        frmAddByLink.addCommand(Commands.cmdBack);
        //#style imgHome
        frmAddByLink.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmAddByLink.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmAddByLink);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }

    public void commandAction(Command command, Item item) {
        if (item == btnRename) {
            R.getMainScreen().addEdit(R.getMainScreen().getFocusedItem().getId(), tfNewName.getString(), R.getMainScreen().getFocusedItem().getIcon());
            R.getURL().call("locify://mainScreen");
        } else if (item == btnAdd) {
            previousUrl = tfServiceUrl.getString();
            R.getURL().call(previousUrl);
        }
    }
}
