/*
 * ConnectionProblem.java
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
package com.locify.client.net;

import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Command;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * In case of connection problem, this class shows user warning
 * @author Destil
 */
public class ConnectionProblem extends FormLocify implements ActionListener {
    private Command tryAgain = new Command(Locale.get("Try_again"), 1);

    public ConnectionProblem() {
        super();
    }

    public void view() {
        this.setAsNew(Locale.get("Connection_problem"));
        this.addComponent(new Label(Locale.get("Connection_problem_description")));

        this.addCommand(tryAgain);
        this.addCommand(Commands.cmdBack);
        this.addCommand(Commands.cmdHome);
        this.setCommandListener(this);
        this.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == tryAgain) {
            R.getBack().repeat();
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        }
    }
}
