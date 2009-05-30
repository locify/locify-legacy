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
import com.locify.client.gui.extension.ListLabelItem;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ListLocify;
import com.locify.client.gui.screen.service.GeocodeResult;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.net.Geocoding;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import java.util.Vector;

/**
 * This class describes all screens for acquiring location context
 * @author Destil
 */
public class LocationScreens implements ActionListener {

    private FormLocify form;

    private ListLocify lstAsk;
    private ListLocify lstAddresses;
    private TextArea tfLatitude;
    private TextArea tfLongitude;
    private TextArea tfAddress;
    private Button btnCoordinates;
    private Button btnAddress;
    private String lastAddress = "";

    public LocationScreens() {
        form = new FormLocify();
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    }

    /**
     * Displays screen to enter coordinates manually
     */
    public void viewCoordinatesScreen() {
        form.setAsNew(Locale.get("Enter_own_coordinates"));

        Label labelLat = new Label(Locale.get("Latitude"));
        form.addComponent(labelLat);
        tfLatitude = new TextArea(GpsUtils.formatLatitude(R.getLocator().getLastLocation().getLatitude(), SettingsData.FORMAT_WGS84_MIN), 1, 100, TextArea.ANY);
        form.addComponent(tfLatitude);

        Label labelLon = new Label(Locale.get("Longitude"));
        form.addComponent(labelLon);
        tfLongitude = new TextArea(GpsUtils.formatLongitude(R.getLocator().getLastLocation().getLongitude(), SettingsData.FORMAT_WGS84_MIN), 1, 100, TextField.ANY);
        form.addComponent(tfLongitude);

        btnCoordinates = new Button(Locale.get("OK"));
        btnCoordinates.addActionListener(this);
        form.addComponent(btnCoordinates);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.setCommandListener(this);

        form.show();
    }

    /**
     * Displays screen for entering address
     */
    public void viewAddressScreen() {
        form.setAsNew(Locale.get("Enter_address"));

        tfAddress = new TextArea(lastAddress, 1, 100, TextField.ANY);
        form.addComponent(tfAddress);

        btnAddress = new Button(Locale.get("OK"));
        btnAddress.addActionListener(this);
        form.addComponent(btnAddress);

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.setCommandListener(this);
        form.show();
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
            form.setAsNew(Locale.get("Select_correct_address"));
            lstAddresses = new ListLocify();
            for (int i = 0; i < results.size(); i++) {
                GeocodeResult result = (GeocodeResult) results.elementAt(i);
                lstAddresses.addItem(result.getFullAddress());
            }
            lstAddresses.addActionListener(this);
            
            form.addCommand(Commands.cmdBack);
            form.addCommand(Commands.cmdHome);
            form.setCommandListener(this);
            form.show();
        }
    }

    /**
     * Shows screen for setting up location context
     */
    public void askForContext() {
        form.setAsNew(Locale.get("Location"));
        lstAsk = new ListLocify();
        lstAsk.addItem(new ListLabelItem(Locale.get("Gps"), IconData.get("locify://icons/gps.png")));
        lstAsk.addItem(new ListLabelItem(Locale.get("Saved_location"), IconData.get("locify://icons/savedLocation.png")));
        lstAsk.addItem(new ListLabelItem(Locale.get("Address"), IconData.get("locify://icons/address.png")));
        lstAsk.addItem(new ListLabelItem(Locale.get("Coordinates"), IconData.get("locify://icons/coordinates.png")));
        lstAsk.addItem(new ListLabelItem(Locale.get("Last_known"), IconData.get("locify://icons/lastKnown.png")));

        form.addCommand(Commands.cmdBack);
        form.addCommand(Commands.cmdHome);
        form.setCommandListener(this);
        form.show();
    }

    public void askForContextGPSOnly() {
        form.setAsNew(Locale.get("Set_location"));
        Label item1 = new Label(Locale.get("Gps_connectRequest"));
        form.addComponent(item1);
        form.addCommand(Commands.cmdYes);
        form.addCommand(Commands.cmdNo);
        form.setCommandListener(this);
        form.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getSource() == lstAsk) {
            int selected = lstAsk.getSelectedIndex();
            R.getURL().call(R.getContext().actions[selected]);
        } else if (evt.getSource() == lstAddresses) {
            int selected = lstAddresses.getSelectedIndex();
            GeocodeResult result = (GeocodeResult) Geocoding.getResults().elementAt(selected);
            R.getContext().setLocation(new Location4D(result.getLatitude(), result.getLongitude(),0), LocationContext.ADDRESS, result.getReducedAddress());
        } else if (evt.getCommand() == Commands.cmdYes) {
            R.getContext().setBackScreen("locify://recordRoute");
            R.getURL().call("locify://gps");
        } else if (evt.getCommand() == Commands.cmdNo) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getSource() == btnCoordinates) { //vlastni souradnice
            R.getContext().setLocation(
                    new Location4D(GpsUtils.parseWGS84Coordinate(tfLatitude.getText()),
                    GpsUtils.parseWGS84Coordinate(tfLongitude.getText()), 0),
                    LocationContext.COORDINATES, tfLatitude.getText() + " " + tfLongitude.getText());
        } else if (evt.getSource() == btnAddress) {
            lastAddress = tfAddress.getText();
            Geocoding.start(tfAddress.getText());
        }
    }
}
