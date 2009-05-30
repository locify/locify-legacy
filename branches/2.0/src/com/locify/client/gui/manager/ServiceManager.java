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
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Button;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * Manages user interface for service adding, renaming, serviceInfo etc.
 * @author Destil
 */
public class ServiceManager implements ActionListener {

    private Form frmServiceInfo;
    private Form frmRenameService;
    private Form frmAddByLink;
    private TextArea tfNewName;
    private TextArea tfServiceUrl;
    private Button btnRename;
    private Button btnAdd;
    private String previousUrl = "http://";

    public ServiceManager() {
    }

    public void viewServiceInfo(Service service) {
        frmServiceInfo = new Form(service.getName());       
        Label siServiceDescription01 = new Label(Locale.get("Service_description"));
        Label siServiceDescription02 = new Label(service.getDescription());
        frmServiceInfo.addComponent(siServiceDescription01);
        frmServiceInfo.addComponent(siServiceDescription02);
        frmServiceInfo.addCommand(Commands.cmdBack);
        frmServiceInfo.addCommand(Commands.cmdHome);
        frmServiceInfo.setCommandListener(this);
        frmServiceInfo.show();
    }

    public void viewRenameService(String original) {
        frmRenameService = new Form(Locale.get("Rename"));
        Label label = new Label(Locale.get("New_name"));
        tfNewName = new TextField(original, 100);
        frmRenameService.addComponent(label);
        frmRenameService.addComponent(tfNewName);

        btnRename = new Button(Locale.get("Rename"));
        btnRename.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                R.getMainScreen().addEdit(R.getMainScreen().getFocusedItem().getId(),
                        tfNewName.getText(), R.getMainScreen().getFocusedItem().getIconURL());
                R.getURL().call("locify://mainScreen");
            }
        });
        frmRenameService.addComponent(btnRename);
        frmRenameService.addCommand(Commands.cmdBack);
        frmRenameService.addCommand(Commands.cmdHome);
        frmRenameService.setCommandListener(this);
        frmRenameService.show();
    }

    public void viewAddByLink() {
        frmAddByLink = new Form(Locale.get("Service_by_link"));
        Label label = new Label(Locale.get("Service_url"));
        frmAddByLink.addComponent(label);
        tfServiceUrl = new TextArea(previousUrl, 500);
        tfServiceUrl.setConstraint(TextArea.URL);
        frmAddByLink.addComponent(tfServiceUrl);
        btnAdd = new Button(Locale.get("Add"));
        btnAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                previousUrl = tfServiceUrl.getText();
                R.getURL().call(previousUrl);
            }
        });
        frmAddByLink.addComponent(btnAdd);
        frmAddByLink.addCommand(Commands.cmdBack);
        frmAddByLink.addCommand(Commands.cmdHome);
        frmAddByLink.setCommandListener(this);
        frmAddByLink.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }
}
