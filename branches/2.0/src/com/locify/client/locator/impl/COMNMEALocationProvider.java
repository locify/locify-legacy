/*
 * COMNEMALocationProvider.java
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

import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Dialog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

/**
 * Location listener for SE HGE-100 GPS
 * @author Jiri Stepan
 */
public class COMNMEALocationProvider extends NMEALocationProvider {

    private OutputStream oStream = null;
    private CommConnection commConn = null;

    public COMNMEALocationProvider() {
        //Open connection to HGE-100
        try {
            commConn = (CommConnection) Connector.open("comm:AT5;baudrate=9600");
        } catch (IOException e) {
            commConn = null;
            R.getCustomAlert().quickView(Locale.get("HGE-100_not_connected"), Dialog.TYPE_WARNING, "locify://back");
            super.stopProvider();
            return;
        }
        if (commConn != null) {
            //Get the input stream
            InputStream iStream;
            try {
                iStream = commConn.openInputStream();
                this.setInputStream(iStream);
            } catch (IOException e) {
                iStream = null;
                try {
                    commConn.close();
                } catch (IOException ex) {
                } finally {
                    commConn = null;
                }
                R.getErrorScreen().view(e, "COMNMEALocationProvider.constructor.openStream", null);
            }

            //Get the output stream
            try {
                oStream = commConn.openOutputStream();
            } catch (IOException e) {
                oStream = null;
                try {
                    iStream.close();
                } catch (IOException ex) {
                } finally {
                    iStream = null;
                }
                try {
                    commConn.close();
                } catch (IOException ex) {
                } finally {
                    commConn = null;
                }
                R.getErrorScreen().view(e, "COMNMEALocationProvider.constructor.closeStream", null);
            }

            this.sendStart();
        }
    }

    public void stopProvider() {
        sendStop();
        super.stopProvider();

        try {
            oStream.close();
        } catch (Exception e) {
            //noop
        }

    }

    /**
     * Sends the start command to HGE-100 to start transmitting data
     * @return true if successfull
     */
    private boolean sendStart() {
        String cmd = "$STA\r\n";
        try {
            oStream.write(cmd.getBytes());
        } catch (IOException e) {
            R.getErrorScreen().view(e, "COMNMEALocationProvider.sendStart", null);
            return false;
        }
        return true;
    }

    /**
     * Sends the stop command to HGE-100 to stop transmitting data
     * @return
     */
    private boolean sendStop() {
        String cmd = "$STO\r\n";
        try {
            oStream.write(cmd.getBytes());
        } catch (IOException e) {
            R.getErrorScreen().view(e, "COMNMEALocationProvider.sendStop", null);
            return false;
        }
        return true;
    }
}
