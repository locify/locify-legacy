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

import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 * In case of connection problem, this class shows user warning
 * @author Destil
 */
public class ConnectionProblem extends Form implements CommandListener {
    private Command tryAgain = new Command(Locale.get("Try_again"), Command.SCREEN, 1);

    public ConnectionProblem() {
        super(Locale.get("Connection_problem"));
    }

    public void view() {
        this.deleteAll();
        this.append(Locale.get("Connection_problem_description"));
        this.addCommand(tryAgain);
        this.addCommand(Commands.cmdBack);
        //#style imgHome
        this.addCommand(Commands.cmdHome);
        this.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == tryAgain) {
            R.getBack().repeat();
        } else if (c == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (c == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        }
    }
}
