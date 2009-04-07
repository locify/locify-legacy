/*
 * BTNMEALocationProvider.java
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
package com.locify.client.locator.impl;

import com.locify.client.utils.R;
import com.locify.client.gui.manager.BluetoothManager;
import java.io.IOException;

/**
 * Manages connection with Bluetooth GPS
 * @author Jiri Stepan
 */
public class BTNMEALocationProvider extends NMEALocationProvider implements Runnable {

    private final static int STATUS_FIRST_RUN = 0;
    private final static int STATUS_WAIT_FOR_BTDEVICE_MANAGER = 1;
    private final static int STATUS_CONNECTED = 2;
    private final static int STATUS_DISCONNECTED = 3;
    private static final long LOCATION_UPDATE_TIMEOUT = 8000;
    private static final int SLEEP_INTERVAL = 500;
    private static final int DISCONNECTED_SLEEP_INTERVAL = 8000;
    private Thread btConnectorThread;
    private int btstate;
    private boolean stop;
    private boolean searchDevices;

    public BTNMEALocationProvider() {
        btConnectorThread = new Thread(this);
        btConnectorThread.start();
    }

    public void run() {
        try {
            btstate = STATUS_FIRST_RUN;
            stop = false;
            searchDevices = false;

            while (!stop) {
                String lastDevice = R.getSettings().getLastDevice();
                if (lastDevice.equals("") && btstate == STATUS_FIRST_RUN) //jeste jsme se nikdy nepripojili
                {
                    BluetoothManager bluetooth = R.getBluetoothManager();
                    if (bluetooth.isOn()) {
                        btstate = STATUS_WAIT_FOR_BTDEVICE_MANAGER;
                        searchDevices = true;
                        bluetooth.searchDevices();
                    } else {
                        return;
                    }
                } else if ((!lastDevice.equals("")) && (inputStream == null) && btstate != STATUS_DISCONNECTED) { //uz jsme se nekdy pripojili
                    BluetoothManager bluetooth = R.getBluetoothManager();
                    if (bluetooth.isOn()) {
                        setState(CONNECTING);
                        setConnectionUrl(lastDevice);
                        if (searchDevices) {
                            R.getBack().goBack();
                        }
                        searchDevices = false;
                    } else {
                        return;
                    }
                } else if (inputStream != null) {
                    if (gpsAlive == 0) { //init situation
                        if (state != WAITING) {
                            setState(WAITING);
                        }
                    } else if ((System.currentTimeMillis() - gpsAlive) > LOCATION_UPDATE_TIMEOUT) {
                        //zarizeni vypnuto
                        setState(CONNECTING);
                        btstate = STATUS_DISCONNECTED;
                        if (parser != null) {
                            parser.stop();
                            parser = null;
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ex) {
                            }
                        }
                        inputStream = null;
                    } else {
                        btstate = STATUS_CONNECTED;
                    }
                }
                try {
                    if (btstate == STATUS_DISCONNECTED) {
                        Thread.sleep(DISCONNECTED_SLEEP_INTERVAL);
                        btstate = STATUS_FIRST_RUN;
                    } else {
                        Thread.sleep(SLEEP_INTERVAL);
                    }
                } catch (InterruptedException ex) {
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BTNMEALocationProvider.run", String.valueOf(btstate));
        }
    }

    public void notifyDisconnect() {
        btstate = STATUS_DISCONNECTED;
        setState(CONNECTING);
    }

    public void stopProvider() {
        try {
            super.stopProvider();
            if (btConnectorThread != null) {
                stop = true;
                btConnectorThread = null;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "BTNMEALocationProvider.stopProvider", null);
        }
    }
}
