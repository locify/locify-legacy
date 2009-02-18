/*
 * Commands.java
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
package com.locify.client.utils;

import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;

/**
 * Stores some commands which are used everywhere in Locify
 * @author Destil
 */
public class Commands {

    public static Command cmdHome = new Command(Locale.get("Home"), Command.SCREEN, 50);
    public static Command cmdBack = new Command(Locale.get("Back"), Command.BACK, 3);
    public static Command cmdSelect = new Command(Locale.get("Select"), Command.ITEM, 0);
    public static Command cmdContactTel = new Command(Locale.get("Tel"), Command.ITEM, 0);
    public static Command cmdContactEmail = new Command(Locale.get("Email"), Command.ITEM, 0);
    public static Command cmdOK = new Command(Locale.get("OK"), Command.BACK, 3);
    public static Command cmdSend = new Command(Locale.get("Send"), Command.SCREEN, 1);
    public static Command cmdView = new Command(Locale.get("View"), Command.SCREEN, 2);
    public static Command cmdDelete = new Command(Locale.get("Delete"), Command.SCREEN, 5);
    public static Command cmdNavigate = new Command(Locale.get("Navigate"), Command.SCREEN, 3);
    public static Command cmdSave = new Command(Locale.get("Save"), Command.SCREEN, 4);
    public static Command cmdAnotherLocation = new Command(Locale.get("Another_location"), Command.SCREEN, 10);
    public static Command cmdYes = new Command(Locale.get("Yes"), Command.OK, 1);
    public static Command cmdNo = new Command(Locale.get("No"), Command.CANCEL, 2);
}
