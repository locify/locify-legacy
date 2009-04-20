/*
 * SatelliteManager.java
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

package com.locify.client.locator;

import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.R;
import java.util.Hashtable;

/**
 * Manages info about satellites
 * @author menion
 */
public class SatelliteManager {

    private Hashtable satInView = new Hashtable();
    private int actualSatInViewCount = 0;

    private int allSatellites;
    private Integer prn;
    private float elevation;
    private float azimuth;
    private int snr;

    public SatelliteManager() {
    }

    public void parseNMEASatellites(String nmea) {
        try {
            if (R.getLocator().isSatScreenActive()) {
                int starIndex = nmea.indexOf('*');
                if (starIndex == -1) {
                    return;
                }
                String[] param = StringTokenizer.getArray(nmea.substring(0, starIndex), ",");

                // wrong message - $GPGSV,3,*7F
                if (!param[0].equals("$GPGSV") || param.length < 7)
                    return;

                allSatellites = GpsUtils.parseInt(param[3]);
                // wrong message - $GPGSV,4,3,1329,,19,10,3,*7F
                if (allSatellites > 12)
                    return;

                if (actualSatInViewCount != allSatellites) {
                    satInView.clear();
                    actualSatInViewCount = allSatellites;
                }

                int numOfSat = Math.min(allSatellites - (GpsUtils.parseInt(param[2]) - 1) * 4, 4);

                for (int k = 0; k < numOfSat; k++) {
                    if (param[k * 4 + 4].length() > 0)
                        prn =  GpsUtils.parseInteger(param[k * 4 + 4]);
                    else
                        continue;

                    elevation = param[k * 4 + 5].length() > 0 ? GpsUtils.parseFloat(param[k * 4 + 5]) : 0.0f;
                    azimuth = param[k * 4 + 6].length() > 0 ? GpsUtils.parseFloat(param[k * 4 + 6]) : 0.0f;
                    snr = param[k * 4 + 7].length() > 0 ? GpsUtils.parseInt(param[k * 4 + 7]) : 0;

                    // refresh satellites
                    SatellitePosition sat = (SatellitePosition) satInView.get(prn);
                    if (sat == null) {
                        sat = new SatellitePosition();
                        satInView.put(prn, sat);
                    }

                    sat.prn = prn;
                    sat.elevation = elevation;
                    sat.azimuth = azimuth;
                    sat.snr = snr;
                }
            }
        } catch (Exception e) {
            Logger.error("SatelliteManager.parseNmeaSatellites(String[] param), NMEA: " + nmea + " Ex: " + e.toString());
        }
        return;
    }

    public Hashtable getSatInView() {
        return satInView;
    }
}
