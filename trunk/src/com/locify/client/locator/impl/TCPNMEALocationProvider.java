/*
 * TCPNMEALocationProvider.java
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
import de.enough.polish.util.Locale;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 * Manages with GPS over HTTP (on windows mobile - Locify Porter or GPS Gate)
 * @author Jiri
 */
public class TCPNMEALocationProvider extends NMEALocationProvider {

    public TCPNMEALocationProvider() {
        try {
            SocketConnection sc = null;
            try {
                sc = (SocketConnection) Connector.open("socket://127.0.0.1:" + R.getSettings().getTcpPort());
            } catch (ConnectionNotFoundException e) {
                sc = null;
                R.getCustomAlert().quickView(Locale.get("Pda_porter_not_connected"), Locale.get("Warning"), "locify://back");
                super.stopProvider();
                return;
            }
            if (sc != null) {
                sc.setSocketOption(SocketConnection.LINGER, 5);

                InputStream is = sc.openInputStream();
                setInputStream(is);
            }

        } catch (IOException e) {
            R.getErrorScreen().view(e, "TCPNMEALocationProvider.constructor", null);
        }
    }
}
