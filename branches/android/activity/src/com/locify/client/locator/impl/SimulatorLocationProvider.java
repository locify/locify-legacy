/*
 * SimulatorLocationProvider.java
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
import java.io.DataInputStream;
import java.io.InputStream;

/** 
 * This LocationProvider simulates GPS device. I reads slowly NMEA messages from resources file nmeatest1.txt and using 
 * NMEALocationProvider generates GPS events.
 *  
 * @author Jiri Stepan
 */
public class SimulatorLocationProvider extends NMEALocationProvider {

    private static final String file = "/nmeatest1.txt";

    public SimulatorLocationProvider() {
        InputStream in = new DataInputStream(this.getClass().getResourceAsStream(file));
        SlowLineInputStream slowInputStream = new SlowLineInputStream(in, "$GPRMC", 1000);
        setInputStream(slowInputStream);
    }
}
