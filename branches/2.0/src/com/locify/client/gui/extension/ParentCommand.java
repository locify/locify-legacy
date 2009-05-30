/*
 * ParentCommand.java
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
package com.locify.client.gui.extension;

import com.sun.lwuit.Command;
import com.sun.lwuit.Image;

/**
 *
 * @author menion
 */
public class ParentCommand extends Command {

    private Command[] children;

    public ParentCommand(String name, Image icon, Command[] children) {
        super(name, icon);
        this.children = children;
    }

    public Command[] getChildren() {
        return children;
    }

    public void setCommand(Command[] children) {
        this.children = children;
    }
}
