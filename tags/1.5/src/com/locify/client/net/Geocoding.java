/*
 * Geocode.java
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

import com.locify.client.gui.screen.service.GeocodeResult;
import com.locify.client.utils.Logger;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import java.io.ByteArrayInputStream;
import java.util.Vector;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Class which handles geocoding via yahoo api
 * @author Destil
 */
public class Geocoding {

    private static Vector results;

    public static Vector getResults() {
        return results;
    }
    
    /**
     * Starts geocoding from address via yahoo api
     * @param address address string
     */
    public static void start(String address) {
        R.getHttp().start("http://local.yahooapis.com/MapsService/V1/geocode?appid=9UbHjo7V34GSlshPrG9uURmEguPRUfT3Nh3H0qDKt_fHZtdNjY3gndp8Vszg&location=" + Utils.urlUTF8Encode(address));
    }

    /**
     * Processes response from yahoo geocoding api
     * @param result response from yahoo
     */
    public static void result(String result) {
        try {

            results = new Vector();
            //parse kml
            ByteArrayInputStream bais = new ByteArrayInputStream(UTF8.encode(result));
            bais.reset();
            KXmlParser parser = new KXmlParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
            parser.setInput(bais, "utf-8");
            Logger.log("Parsing geocode xml: ");
            while (parser.nextTag() != XmlPullParser.END_TAG) {
                String tagName = parser.getName();
                Logger.logNoBreak("<" + tagName + ">");
                if (tagName.equalsIgnoreCase("result")) {
                    double latitude = 0;
                    double longitude = 0;
                    String address = "";
                    String state = "";
                    String country = "";
                    String city = "";
                    String warning = "";
                    if (parser.getAttributeValue(null, "warning") != null) {
                        warning = parser.getAttributeValue(null, "warning");
                    }
                    while (parser.nextTag() != XmlPullParser.END_TAG) {
                        tagName = parser.getName();
                        Logger.logNoBreak("<" + tagName + ">");
                        if (tagName.equalsIgnoreCase("latitude")) {
                            latitude = Double.parseDouble(parser.nextText());
                        } else if (tagName.equalsIgnoreCase("longitude")) {
                            longitude = Double.parseDouble(parser.nextText());
                        } else if (tagName.equalsIgnoreCase("address")) {
                            address = parser.nextText();
                        } else if (tagName.equalsIgnoreCase("city")) {
                            city = parser.nextText();
                        } else if (tagName.equalsIgnoreCase("state")) {
                            state = parser.nextText();
                        } else if (tagName.equalsIgnoreCase("zip")) {
                            parser.nextText();
                        } else if (tagName.equalsIgnoreCase("country")) {
                            country = parser.nextText();
                        }
                    }
                    results.addElement(new GeocodeResult(latitude, longitude, warning, address, city, state, country));
                }
            }

            R.getURL().call("locify://addressSelection");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Geocoding.result", result);
        }
    }

    /**
     * Processess error from yahoo geocoding api
     */
    public static void error() {
        R.getCustomAlert().quickView(Locale.get("Invalid_address"), Locale.get("Error"), "locify://refresh");
    }
}