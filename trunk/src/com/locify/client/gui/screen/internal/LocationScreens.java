/*
 * LocationScreens.java
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

import com.locify.client.data.IconData;
import com.locify.client.data.SettingsData;
import com.locify.client.gui.screen.service.GeocodeResult;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.net.Geocoding;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * This class describes all screens for acquiring location context
 * @author Destil
 */
public class LocationScreens implements CommandListener, ItemCommandListener {

    private Form frmCoordinates;
    private Form frmAddress;
    private Form frmGpsRequest;
    private List lstAsk;
    private List lstAddresses;
    private TextField tfLatitude;
    private TextField tfLongitude;
    private TextField tfAddress;
    private StringItem btnCoordinates;
    private StringItem btnAddress;
    private String lastAddress = "";

    public LocationScreens() {
    }

    /**
     * Displays screen to enter coordinates manually
     */
    public void viewCoordinatesScreen() {
        frmCoordinates = new Form(Locale.get("Enter_own_coordinates"));
        tfLatitude = new TextField(Locale.get("Latitude"), GpsUtils.formatLatitude(R.getLocator().getLastLocation().getLatitude(), SettingsData.FORMAT_WGS84_MIN), 1000, TextField.ANY);
        tfLongitude = new TextField(Locale.get("Longitude"), GpsUtils.formatLongitude(R.getLocator().getLastLocation().getLongitude(), SettingsData.FORMAT_WGS84_MIN), 1000, TextField.ANY);
        btnCoordinates = new StringItem("", Locale.get("OK"), StringItem.BUTTON);
        btnCoordinates.setDefaultCommand(Commands.cmdSelect);
        btnCoordinates.setItemCommandListener(this);
        frmCoordinates.append(tfLatitude);
        frmCoordinates.append(tfLongitude);
        frmCoordinates.append(btnCoordinates);
        frmCoordinates.addCommand(Commands.cmdBack);
        //#style imgHome
        frmCoordinates.addCommand(Commands.cmdHome);
        frmCoordinates.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmCoordinates);
    }

    /**
     * Displays screen for entering address
     */
    public void viewAddressScreen() {
        frmAddress = new Form(Locale.get("Enter_address"));
        tfAddress = new TextField("", lastAddress, 100, TextField.ANY);
        btnAddress = new StringItem("", Locale.get("OK"), StringItem.BUTTON);
        btnAddress.setDefaultCommand(Commands.cmdSelect);
        btnAddress.setItemCommandListener(this);
        frmAddress.append(tfAddress);
        frmAddress.append(btnAddress);
        frmAddress.addCommand(Commands.cmdBack);
        //#style imgHome
        frmAddress.addCommand(Commands.cmdHome);
        frmAddress.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmAddress);
    }

    public void viewAddressSelection() {
        Vector results = Geocoding.getResults();
        if (results.size() == 1) {
            GeocodeResult result = (GeocodeResult) results.firstElement();
            R.getConfirmScreen().setAddressLocation(result.getLatitude(), result.getLongitude(), result.getReducedAddress());
            if (result.getWarning().equals("")) {
                R.getConfirmScreen().view(Locale.get("Your_address_has_been_located_here") + ":\n" + result.getFullAddress() + "\n" + Locale.get("Is_it_OK") + "?");
            } else {
                R.getConfirmScreen().view(Locale.get("Warning") + ":\n" + result.getWarning() + "\n" + Locale.get("Is_it_OK") + "?");
            }
        } else {
            lstAddresses = new List(Locale.get("Select_correct_address") + ":", List.IMPLICIT);
            for (int i = 0; i < results.size(); i++) {
                GeocodeResult result = (GeocodeResult) results.elementAt(i);
                lstAddresses.append(result.getFullAddress(), null);
            }
            lstAddresses.addCommand(Commands.cmdBack);
            //#style imgHome
            lstAddresses.addCommand(Commands.cmdHome);
            lstAddresses.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, lstAddresses);
        }
    }

    /**
     * Shows screen for setting up location context
     */
    public void askForContext() {
        lstAsk = new List(Locale.get("Location"), List.IMPLICIT);
        lstAsk.append(Locale.get("Gps"), IconData.get("locify://icons/gps.png"));
        lstAsk.append(Locale.get("Saved_location"), IconData.get("locify://icons/savedLocation.png"));
        lstAsk.append(Locale.get("Address"), IconData.get("locify://icons/address.png"));
        lstAsk.append(Locale.get("Coordinates"), IconData.get("locify://icons/coordinates.png"));
        lstAsk.append(Locale.get("Last_known"), IconData.get("locify://icons/lastKnown.png"));
        lstAsk.addCommand(Commands.cmdBack);
        //#style imgHome
        lstAsk.addCommand(Commands.cmdHome);
        lstAsk.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, lstAsk);
    }

    public void askForContextGPSOnly() {
        frmGpsRequest = new Form(Locale.get("Set_location"));
        StringItem item1 = new StringItem("", Locale.get("Gps_connectRequest"));
        frmGpsRequest.append(item1);
        frmGpsRequest.addCommand(Commands.cmdYes);
        frmGpsRequest.addCommand(Commands.cmdNo);
        frmGpsRequest.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmGpsRequest);
    }
    
    public void commandAction(Command command, Displayable displayable) {
       // if (displayable == lstAsk && command == Commands.cmdBack) {
       //     R.getBack().goBack(2);
       // } else
        if (command == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == List.SELECT_COMMAND && displayable == lstAsk) {
            int selected = lstAsk.getSelectedIndex();
            R.getURL().call(R.getContext().actions[selected]);
        } else if (command == List.SELECT_COMMAND && displayable == lstAddresses) {
            int selected = lstAddresses.getSelectedIndex();
            GeocodeResult result = (GeocodeResult) Geocoding.getResults().elementAt(selected);
            R.getContext().setLocation(new Location4D(result.getLatitude(), result.getLongitude(),0), LocationContext.ADDRESS, result.getReducedAddress());
        } else if (displayable == frmGpsRequest && command == Commands.cmdYes) {
            R.getContext().setBackScreen("locify://recordRoute");
            R.getURL().call("locify://gps");
        } else if (displayable == frmGpsRequest && command == Commands.cmdNo) {
            R.getURL().call("locify://mainScreen");
        }
    }

    public void commandAction(Command command, Item item) {
        if (item == btnCoordinates) { //vlastni souradnice
            R.getContext().setLocation(new Location4D(GpsUtils.parseWGS84Coordinate(tfLatitude.getString()), GpsUtils.parseWGS84Coordinate(tfLongitude.getString()), 0), LocationContext.COORDINATES, tfLatitude.getString()+" "+tfLongitude.getString());
        } else if (item == btnAddress) {
            lastAddress = tfAddress.getString();
            Geocoding.start(tfAddress.getString());
        }
    }
}
