/**
 * Bluetooth.java
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
package com.locify.client.gui.manager;

import com.locify.client.utils.Commands;
import de.enough.polish.util.Locale;
import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import com.locify.client.utils.R;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;

/**
 * This class handles Bluetooth connection, selecting right device and getting bt adress
 * @author David Vavra
 */
public class BluetoothManager implements DiscoveryListener, Runnable, CommandListener {

    private DiscoveryAgent discoveryAgent;
    private LocalDevice localDevice;
    private static String bluetoothAdress = "";
    private Vector devices;
    private Form frmConnectionProgress;
    private List lstDevices;

    public BluetoothManager() {
    }

    public void viewConnectionProgress(String status, int gaugeStatus) {
        frmConnectionProgress = new Form(Locale.get("Connection_progress"));
        StringItem siConnectionStatus = new StringItem("", status);
        frmConnectionProgress.append(siConnectionStatus);
        Gauge gaSync = new Gauge("", false, 60, 0);
        gaSync.setMaxValue(Gauge.INDEFINITE);
        gaSync.setValue(gaugeStatus);
        frmConnectionProgress.append(gaSync);
        frmConnectionProgress.addCommand(Commands.cmdBack);
        //#style imgHome
        frmConnectionProgress.addCommand(Commands.cmdHome);
        frmConnectionProgress.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmConnectionProgress);
    }

    public void viewNewDevice(String friendlyName) {
        if (lstDevices == null) {
            lstDevices = new List(Locale.get("Select_GPS_device"), Choice.IMPLICIT);
            lstDevices.append(friendlyName, null);
            lstDevices.addCommand(Commands.cmdBack);
            //#style imgHome
            lstDevices.addCommand(Commands.cmdHome);
            lstDevices.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, lstDevices);
        } else {
            lstDevices.append(friendlyName, null);
        }
    }

    /**
     * Is Bluetooth on?
     * @return if bluetooth is on
     */
    public boolean isOn() {
        try {
            localDevice = LocalDevice.getLocalDevice();
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);
            return true;
        } catch (BluetoothStateException ex) {
            R.getCustomAlert().quickView(Locale.get("Bluetooth_off"), "Warning", "locify://back");
            return false;
        }
    }

    /**
     * Starts to search for new bt devices
     */
    public void searchDevices() {
        try {
            viewConnectionProgress(Locale.get("Bluetooth_searching"), Gauge.CONTINUOUS_RUNNING);
            devices = new Vector();
            discoveryAgent = localDevice.getDiscoveryAgent();
            discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BluetoothManager.searchDevices", null);
        }
    }

    /**
     * Start to search for services of selected device in new thread
     */
    public void searchForServices() {
        try {
            viewConnectionProgress(Locale.get("Connecting_to_device"), Gauge.CONTINUOUS_RUNNING);
            discoveryAgent.cancelInquiry(this);
            Thread thread = new Thread(this);
            thread.start();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BluetoothManager.searchForServices", null);
        }
    }

    /**
     * Device was found
     * @param btDevice
     * @param cod 
     */
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        try {
            devices.addElement(btDevice);
            String name;
            //series 40 bug
            try {
                name = btDevice.getFriendlyName(false);
            } catch (IOException ex) {
                name = Locale.get("Unknown_bt_device");
            }
            viewNewDevice(name);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BluetoothManager.deviceDiscovered", null);
        }
    }

    /**
     * Searching for devices completed
     * @param discType 
     */
    public void inquiryCompleted(int discType) {
            if (devices.size() == 0) {
                viewConnectionProgress(Locale.get("No_bt_in_range"), Gauge.CONTINUOUS_IDLE);
            }
    }

    /**
     * Processing of found service, start GPS transfer
     * @param transID
     * @param servRecord 
     */
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        try {
            bluetoothAdress = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            discoveryAgent.cancelServiceSearch(transID);
            R.getSettings().setLastDevice(bluetoothAdress);
        } catch (Exception e) {
            R.getSettings().setLastDevice("");
            R.getErrorScreen().view(e, "BluetoothManager.servicesDiscovered", null);
        }
    }

    /**
     * If service not found, there is an error
     * @param transID
     * @param responseCode 
     */
    public void serviceSearchCompleted(int transID, int responseCode) {
        if (bluetoothAdress.equals("")) {
            R.getCustomAlert().quickView(Locale.get("Already_connected"), "Error", "locify://back");
        }
    }

    /**
     * This thread takes care about service searching
     */
    public void run() {
        boolean searching = true;
        int timeout = 0;
        while (searching) {
            try {
                timeout++;
                if (timeout > 20) {
                    viewConnectionProgress(Locale.get("Unable_to_search_service"), Gauge.CONTINUOUS_IDLE);
                    searching = false;
                }
                this.discoveryAgent.searchServices(null, new UUID[]{new UUID(0x1101)}, (RemoteDevice) this.devices.elementAt(lstDevices.getSelectedIndex()), this);

                searching = false;
            } catch (BluetoothStateException ex) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException exc) {
                }
            }
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == Commands.cmdHome) {
            discoveryAgent.cancelInquiry(this);
            R.getURL().call("locify://mainScreen");
        } else if (command == Commands.cmdBack) {
            discoveryAgent.cancelInquiry(this);
            R.getBack().goBack();
        } else if (displayable == lstDevices && command == List.SELECT_COMMAND)
        {
            R.getURL().call("locify://selectBTDevice");
        }
    }
}
