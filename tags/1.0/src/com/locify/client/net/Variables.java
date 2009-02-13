/*
 * Variables.java
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
package com.locify.client.net;

import com.locify.client.data.CookieData;
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;

/**
 * This class manages replacing of Locify variables like $lat, $lon, etc. 
 * @author Destil
 */
public class Variables {

    /**
     * Replaces various variables
     * @param text test to replace
     * @param url if we are replacing url and decimal coordinates should be used
     * @return text with replaced variables
     */
    public static String replace(String text, boolean url) {
        try {
            if (text == null)
                return null;
            if (text.indexOf("$lat") >= 0 || text.indexOf("$lon") >= 0 || text.indexOf("$alt") >= 0 || text.indexOf("$spe") >= 0 || text.indexOf("$hea") >= 0 || text.indexOf("$acc") >= 0) {
                text = replaceGpsVariables(text, !url);
                if (text == null)
                    return null;
            }
            if (text.indexOf("$settings[") >= 0) {
                text = ServiceSettingsData.replaceVariables(text);
            }
            if (text.indexOf("$cookies[") >= 0) {
                text = CookieData.replaceVariables(text);
            }
            if (text.indexOf("$client[time]") >= 0) {
                text = Utils.replaceString(text, "$client[time]", String.valueOf(Utils.timestamp()));
            }
            if (text.indexOf("$client[lang]") >= 0) {
                text = Utils.replaceString(text, "$client[lang]", R.getMidlet().getAppProperty("microedition.locale"));
            }
            if (text.indexOf("$client[version]") >= 0) {
                text = Utils.replaceString(text, "$client[version]", R.getMidlet().getAppProperty("MIDlet-Version"));
            }
            if (text.indexOf("$client[width]") >= 0) {
                text = Utils.replaceString(text, "$client[width]", String.valueOf(R.getMainScreen().availableWidth()));
            }
            if (text.indexOf("$client[height]") >= 0) {
                text = Utils.replaceString(text, "$client[height]", String.valueOf(R.getMainScreen().availableHeight()));
            }
            if (text.indexOf("$client[device]") >=0 )
            {
                text = Utils.replaceString(text, "$client[device]", System.getProperty("microedition.platform"));
            }
            if (text.indexOf("$location[source]") >=0 )
            {
                text = Utils.replaceString(text, "$location[source]", String.valueOf(R.getContext().getSource()));
            }
            if (text.indexOf("$location[text]") >=0 )
            {
                text = Utils.replaceString(text, "$location[text]", R.getContext().getSourceData());
            }
            return text;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Variables.replace", text);
            return text;
        }
    }

    /**
     * Replaces gps-related variables in given string. 
     * Friendly format is used when presenting coordinates to user - according to his settings
     * Not friendly format is WGS84 decimal format and it's used when sending coordinates to web service
     * When user does not have a fix, nothing is preffiled. Howewer he can prefill coordinates by own location.
     * @param text string to be replaced
     * @param friendlyFormat using friendly format
     * @return replaced string
     */
    public static String replaceGpsVariables(String text, boolean friendlyFormat) {
        //nothing preffilled while unknown status or no signal
        if (!R.getLocator().hasValidLocation()) {
            text = Utils.replaceString(text, "$lat", "");
            text = Utils.replaceString(text, "$lon", "");
            text = Utils.replaceString(text, "$alt", "");
            text = Utils.replaceString(text, "$spe", "");
            text = Utils.replaceString(text, "$hea", "");
            text = Utils.replaceString(text, "$acc", "");
        } //has location context
        else  {
            if (friendlyFormat) {
                text = Utils.replaceString(text, "$lat", String.valueOf(GpsUtils.formatLatitude(R.getLocator().getLastLocation().getLatitude(), R.getSettings().getCoordsFormat())));
                text = Utils.replaceString(text, "$lon", String.valueOf(GpsUtils.formatLongitude(R.getLocator().getLastLocation().getLongitude(), R.getSettings().getCoordsFormat())));
            } else {
                text = Utils.replaceString(text, "$lat", String.valueOf(R.getLocator().getLastLocation().getLatitude()));
                text = Utils.replaceString(text, "$lon", String.valueOf(R.getLocator().getLastLocation().getLongitude()));
            }
            text = Utils.replaceString(text, "$alt", String.valueOf(R.getLocator().getLastLocation().getAltitude()));
            text = Utils.replaceString(text, "$spe", String.valueOf(R.getLocator().getSpeed()));
            text = Utils.replaceString(text, "$hea", String.valueOf(R.getLocator().getHeading()));
            text = Utils.replaceString(text, "$acc", String.valueOf(R.getLocator().getAccuracyHorizontal()));
        }
        return text;
    }
}
