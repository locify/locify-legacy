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
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;

/**
 * Stores some commands which are used everywhere in Locify
 * @author Destil
 */
public class Commands {

    public static Command cmdHome = new Command(Locale.get(401), Command.SCREEN, 50);
    public static Command cmdBack = new Command(Locale.get(16), Command.BACK, 3);
    public static Command cmdSelect = new Command(Locale.get(130), Command.ITEM, 0);
    public static Command cmdContactTel = new Command(Locale.get(464), Command.ITEM, 0);
    public static Command cmdContactEmail = new Command(Locale.get(46), Command.ITEM, 0);
    public static Command cmdOK = new Command(Locale.get(248), Command.BACK, 3);
    public static Command cmdSend = new Command(Locale.get(133), Command.SCREEN, 1);
    public static Command cmdView = new Command(Locale.get(151), Command.SCREEN, 2);
    public static Command cmdDelete = new Command(Locale.get(39), Command.SCREEN, 5);
    public static Command cmdNavigate = new Command(Locale.get(106), Command.SCREEN, 3);
    public static Command cmdSave = new Command(Locale.get(126), Command.SCREEN, 4);
    public static Command cmdAnotherLocation = new Command(Locale.get(267), Command.SCREEN, 10);
    public static Command cmdYes = new Command(Locale.get(167), Command.OK, 1);
    public static Command cmdNo = new Command(Locale.get(110), Command.CANCEL, 2);
    public static Command cmdCancel = new Command(Locale.get(226), Command.CANCEL, 5);
    public static Command cmdBacklightOn = new Command(Locale.get(456),Command.SCREEN,20);
    public static Command cmdBacklightOff = new Command(Locale.get(455),Command.SCREEN,20);
    public static Command cmdStop = new Command(Locale.get(141),Command.STOP,20);
}
