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
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Form; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Item; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.ItemCommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.List; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.StringItem; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField; import de.enough.polish.ui.StyleSheet;

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
        frmCoordinates = new Form(Locale.get(244));
        tfLatitude = new TextField(Locale.get(73), GpsUtils.formatLatitude(R.getLocator().getLastLocation().getLatitude(), SettingsData.FORMAT_WGS84_MIN), 1000, TextField.ANY);
        tfLongitude = new TextField(Locale.get(87), GpsUtils.formatLongitude(R.getLocator().getLastLocation().getLongitude(), SettingsData.FORMAT_WGS84_MIN), 1000, TextField.ANY);
        btnCoordinates = new StringItem("", Locale.get(248), StringItem.BUTTON);
        btnCoordinates.setDefaultCommand(Commands.cmdSelect);
        btnCoordinates.setItemCommandListener(this);
        frmCoordinates.append(tfLatitude);
        frmCoordinates.append(tfLongitude);
        frmCoordinates.append(btnCoordinates);
        frmCoordinates.addCommand(Commands.cmdBack);
        //#style imgHome
        frmCoordinates.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmCoordinates.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmCoordinates);
    }

    /**
     * Displays screen for entering address
     */
    public void viewAddressScreen() {
        frmAddress = new Form(Locale.get(243));
        tfAddress = new TextField("", lastAddress, 100, TextField.ANY);
        btnAddress = new StringItem("", Locale.get(248), StringItem.BUTTON);
        btnAddress.setDefaultCommand(Commands.cmdSelect);
        btnAddress.setItemCommandListener(this);
        frmAddress.append(tfAddress);
        frmAddress.append(btnAddress);
        frmAddress.addCommand(Commands.cmdBack);
        //#style imgHome
        frmAddress.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        frmAddress.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmAddress);
    }

    public void viewAddressSelection() {
        Vector results = Geocoding.getResults();
        if (results.size() == 1) {
            GeocodeResult result = (GeocodeResult) results.firstElement();
            R.getConfirmScreen().setAddressLocation(result.getLatitude(), result.getLongitude(), result.getReducedAddress());
            if (result.getWarning().equals("")) {
                R.getConfirmScreen().view(Locale.get(286) + ":\n" + result.getFullAddress() + "\n" + Locale.get(284) + "?");
            } else {
                R.getConfirmScreen().view(Locale.get(152) + ":\n" + result.getWarning() + "\n" + Locale.get(284) + "?");
            }
        } else {
            lstAddresses = new List(Locale.get(285) + ":", List.IMPLICIT);
            for (int i = 0; i < results.size(); i++) {
                GeocodeResult result = (GeocodeResult) results.elementAt(i);
                lstAddresses.append(result.getFullAddress(), null);
            }
            lstAddresses.addCommand(Commands.cmdBack);
            //#style imgHome
            lstAddresses.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
            lstAddresses.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, lstAddresses);
        }
    }

    /**
     * Shows screen for setting up location context
     */
    public void askForContext() {
        lstAsk = new List(Locale.get(262), List.IMPLICIT);
        lstAsk.append(Locale.get(245), IconData.get("locify://icons/gps.png"));
        lstAsk.append(Locale.get(128), IconData.get("locify://icons/savedLocation.png"));
        lstAsk.append(Locale.get(241), IconData.get("locify://icons/address.png"));
        lstAsk.append(Locale.get(242), IconData.get("locify://icons/coordinates.png"));
        lstAsk.append(Locale.get(247), IconData.get("locify://icons/lastKnown.png"));
        lstAsk.addCommand(Commands.cmdBack);
        //#style imgHome
        lstAsk.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        lstAsk.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, lstAsk);
    }

    public void askForContextGPSOnly() {
        frmGpsRequest = new Form(Locale.get(249));
        StringItem item1 = new StringItem("", Locale.get(345));
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
