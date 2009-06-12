/*
 * LandmarksExport.java
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

package com.locify.client.data;

import com.locify.client.data.items.Waypoint;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Dialog;
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkStore;
import javax.microedition.location.QualifiedCoordinates;

/**
 * This class manages export to JSR179 Landmarks
 * @author Destil
 */
public class LandmarksExport {
/**
     * Export waypoint to Nokia Landmarks
     * @param waypoint
     * @param callNext 
     */
    public static void export(Waypoint waypoint, String callNext) {
        try {
            LandmarkStore defaultStore = LandmarkStore.getInstance(null);
            try {
                defaultStore.addCategory("Locify");
            } catch (IllegalArgumentException e) {
            }
            QualifiedCoordinates coordinates = new QualifiedCoordinates(waypoint.getLatitude(), waypoint.getLongitude(), waypoint.getLocation().getAltitude(), Float.NaN, Float.NaN);
            Landmark landMark = new Landmark(waypoint.getName(), waypoint.getDescription(), coordinates, null);
            defaultStore.addLandmark(landMark, "Locify");
            R.getCustomAlert().quickView(Locale.get("Waypoint_added_to_landmarks"), Dialog.TYPE_INFO, callNext);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoDataBrowser.exportToLandmarks()", null);
        }
    }
}
