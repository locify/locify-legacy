/*
 * MultiData.java
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
package com.locify.client.data.items;

import de.enough.polish.util.ArrayList;

/**
 *
 * @author menion
 */
public class MultiData extends GeoData {

    private ArrayList geoData;

    public MultiData() {
        geoData = new ArrayList();
    }

    public void addGeoData(GeoData actualGeoData) {
        geoData.add(actualGeoData);
    }

    public void finalizeData() {
        for (int i = 0; i < geoData.size(); i++) {
            if (geoData.get(i) instanceof Route)
                ((Route) geoData.get(i)).processDescription();
        }
    }

    public String toString() {
        String data = "MultiData: " + super.toString() + "\n";
        for (int i = 0; i < geoData.size(); i++) {
            data += (geoData.toString() + "\n");
        }
        return data;
    }
}
