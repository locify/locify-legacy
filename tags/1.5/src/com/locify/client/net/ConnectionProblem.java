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
public class ConnectionProblem extends Form implements CommandListener, Runnable {

    private int attempts = 0; //pokusy na pripojeni
    private boolean timeout = false;
    private Command tryAgain = new Command(Locale.get("Try_again"), Command.SCREEN, 1);

    public ConnectionProblem() {
        super(Locale.get("Connection_problem"));
    }

    /**
     * In case of connection error, tries request one more time after second wait
     */
    public void occured() {
        if (attempts == 0) {
            timeout = false;
            (new Thread(this)).start();
        } else {
            attempts = 0;
            view();
        }
    }

    private void view() {
        this.deleteAll();
        this.append(Locale.get("Connection_problem_description"));
        //#style imgAddShortcut
        this.addCommand(tryAgain);
        this.addCommand(Commands.cmdBack);
        //#style imgHome
        this.addCommand(Commands.cmdHome);
        this.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, this);
        timeout = true;
        (new Thread(this)).start();
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

    public void run() {
        if (timeout) //timeout after 10s of inactivity
        {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
            }
        } else //waiting between repeated request
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
            attempts++;
        }
        R.getBack().repeat();
    }
}
