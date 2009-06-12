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

import com.locify.client.gui.extension.Progress;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.sun.lwuit.events.ActionEvent;
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
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.DefaultListModel;

/**
 * This class handles Bluetooth connection, selecting right device and getting bt adress
 * @author David Vavra
 */
public class BluetoothManager implements DiscoveryListener, Runnable, ActionListener {

    private DiscoveryAgent discoveryAgent;
    private LocalDevice localDevice;
    private static String bluetoothAdress = "";
    private Vector devices;
    private Form frmConnectionProgress;
    private Form frmDevices;
    private List lstDevices;

    private Progress progress;

    public BluetoothManager() {
    }

    public void viewConnectionProgress(String status, boolean progressRun) {
        frmConnectionProgress = new Form(Locale.get("Connection_progress"));
        Label siConnectionStatus = new Label(status);
        frmConnectionProgress.addComponent(siConnectionStatus);
        progress = new Progress();
        if (progressRun)
            progress.infiniteRun();
        else
            progress.infiniteStop();
        frmConnectionProgress.addComponent(progress);
        frmConnectionProgress.addCommand(Commands.cmdBack);
        frmConnectionProgress.addCommand(Commands.cmdHome);
        frmConnectionProgress.setCommandListener(this);
        frmConnectionProgress.show();
    }

    public void viewNewDevice(String friendlyName) {
        if (frmDevices == null) {
            frmDevices = new Form(Locale.get("Select_GPS_device"));
            frmDevices.setLayout(new BorderLayout());

            lstDevices = new List(new DefaultListModel());
            lstDevices.addItem(new Label(friendlyName));
            frmDevices.addComponent(BorderLayout.CENTER, lstDevices);

            frmDevices.addCommand(Commands.cmdBack);
            frmDevices.addCommand(Commands.cmdHome);
            frmDevices.addCommand(Commands.cmdSelect);
            frmDevices.setCommandListener(this);
            frmDevices.show();
        } else {
            lstDevices.addItem(new Label(friendlyName));
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
            R.getCustomAlert().quickView(Locale.get("Bluetooth_off"), Dialog.TYPE_WARNING, "locify://back");
            return false;
        }
    }

    /**
     * Starts to search for new bt devices
     */
    public void searchDevices() {
        try {
            Logger.debug("search devices");
            viewConnectionProgress(Locale.get("Bluetooth_searching"), true);
            devices = new Vector();
            lstDevices = null;
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
            Logger.debug("start service search");
            viewConnectionProgress(Locale.get("Connecting_to_device"), true);
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
            Logger.debug("device discovered");
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
        Logger.debug("inquiry completed");
        if (devices.size() == 0) {
            viewConnectionProgress(Locale.get("No_bt_in_range"), false);
        }
    }

    /**
     * Processing of found service, start GPS transfer
     * @param transID
     * @param servRecord 
     */
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        try {
            Logger.debug("service discovered");
            bluetoothAdress = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            Logger.debug("bluetooth address: "+bluetoothAdress);
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
        Logger.debug("service search completed");
        if (bluetoothAdress.equals("")) {
            R.getCustomAlert().quickView(Locale.get("Already_connected"), Dialog.TYPE_ERROR, "locify://back");
        }
    }

    /**
     * This thread takes care about service searching
     */
    public void run() {
        try {
            boolean searching = true;
            int timeout = 0;
            while (searching) {
                try {
                    timeout++;
                    if (timeout > 20) {
                        viewConnectionProgress(Locale.get("Unable_to_search_service"), false);
                        searching = false;
                    }
                    int[] attr = new int[]{
                        0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007,
                        0x0008, 0x0009, 0x000A, 0x000B, 0x000C
                    };
                    Logger.debug("searching");
                    //
                    // search for L2CAP services, most services based on L2CAP
                    //
                    // note: Rococo simulator required a new instance of Listener for
                    // every search. not sure if this is also the case in real devices
                    discoveryAgent.searchServices(null, // attributes to retrieve from remote device
                            new UUID[]{new UUID(0x1101)}, // search criteria, 0x0100 = L2CAP
                            (RemoteDevice) this.devices.elementAt(lstDevices.getSelectedIndex()), this); // direct discovery response to Listener object
                    searching = false;
                } catch (BluetoothStateException ex) {
                    Logger.error("error:"+ex);
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException exc) {
                    }
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BluetoothManager.run()", bluetoothAdress);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdHome) {
            discoveryAgent.cancelInquiry(this);
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            discoveryAgent.cancelInquiry(this);
            R.getBack().goBack();
        } else if (evt.getSource() == frmDevices && evt.getCommand() == Commands.cmdSelect) {
            R.getURL().call("locify://selectBTDevice");
        }
    }
}
