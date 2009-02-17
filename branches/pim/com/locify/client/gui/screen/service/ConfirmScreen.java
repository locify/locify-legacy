/*
 * ConfirmScreen.java
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
package com.locify.client.gui.screen.service;

import com.locify.client.data.Service;
import com.locify.client.data.ServicesData;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.utils.Commands;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import de.enough.polish.util.Locale;
import com.locify.client.utils.R;

/**
 * Creates confirmation screen from HTML page
 * @author David Vavra
 */
public class ConfirmScreen implements CommandListener {

    private Form form;
    //sluzba, ktera se pozdeji muze pridat
    private Service service;
    private String confirmUrl;
    private double addressLatitude;
    private double addressLongitude;
    private String addressFull;
    //druh akce po yes
    private int confirmAction;
    
    private static final int ADD_SERVICE = 0;
    private static final int OPEN_URL = 1;
    private static final int SET_ADDRESS_LOCATION = 2;
    public static final int AUTOINSTALL_SERVICES = 3;

    public ConfirmScreen() {
    }

    /**
     * Views the screen
     * @param question 
     */
    public void view(String question) {
        //tvorba formulare
        form = new Form(Locale.get("Confirmation"));
        form.append(question);
        form.addCommand(Commands.cmdYes);
        form.addCommand(Commands.cmdNo);
        form.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, form);
    }
    
    public void view(String question, int confirmAction)
    {
        this.confirmAction = confirmAction;
        view(question);
    }

    /**
     * Sets parameters for service adding
     * @param service 
     */
    public void setService(Service service) {
        this.service = service;
        confirmAction = ADD_SERVICE;
    }

    /**
     * Sets parameter for url calling
     * @param url 
     */
    public void setURL(String url) {
        confirmUrl = url;
        confirmAction = OPEN_URL;
    }

    /**
     * Sets latitude and longitude of address in case of Yes
     * @param latitude
     * @param longitude
     * @param address 
     */
    public void setAddressLocation(double latitude, double longitude, String address) {
        this.addressLatitude = latitude;
        this.addressLongitude = longitude;
        this.addressFull = address;
        this.confirmAction = SET_ADDRESS_LOCATION;
    }

    /**
     * Handles reaction to command events
     * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == form) {
            if (command == Commands.cmdYes) {
                switch (confirmAction) {
                    case ADD_SERVICE:
                        ServicesData.add(service);
                        R.getCustomAlert().quickView(Locale.get("Service_added"), "Info", "locify://mainScreen");
                        break;
                    case OPEN_URL:
                        R.getURL().call(confirmUrl);
                        break;
                    case SET_ADDRESS_LOCATION:
                        R.getContext().setLocation(new Location4D(addressLatitude, addressLongitude, 0), LocationContext.ADDRESS, addressFull);
                        break;
                    case AUTOINSTALL_SERVICES:
                        R.getMainScreen().autoInstall();
                        break;
                }

            } else if (command == Commands.cmdNo) {
                R.getBack().goBack();
            }
        }
    }
}
