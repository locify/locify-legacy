/**
 * Utils.java
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
package com.locify.client.utils;

import com.locify.client.data.SettingsData;
import com.locify.client.locator.Location4D;
import com.locify.client.utils.math.LMath;
import de.enough.polish.util.Locale;
import java.util.Vector;

/***
 * Usefull functions for manipulating with GPS data
 * @author Destil
 */
public class GpsUtils {

    public static final int FORMAT_WGS84 = 0;   //14,94323
    public static final int FORMAT_WGS84_MIN = 1; //14°52.123
    public static final int FORMAT_WGS84_SEC = 2; //14°52'12.34

    /**
     * compute distance beetwen two points on sphere (WGS-84 diametr)
     * @param lat1 lattitude of first point
     * @param lon1 longitude of first point
     * @param lat2 lattitude of second point
     * @param lon2 longitude of second point
     * @return distance in metres
     */
    public static float computeDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = GpsUtils.degToRad(lat2 - lat1);
        double dLon = GpsUtils.degToRad(lon2 - lon1);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
                Math.cos(GpsUtils.degToRad(lat1)) * Math.cos(GpsUtils.degToRad(lat2)) *
                Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        //return (float) (Location4D.R * 2.0 * Utils.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return (float) (Location4D.R * 2.0 * LMath.asin(Math.sqrt(a)));
    }

    /**
     * Compute azimut from two points on sphere
     * @param lat1 lattitude of the first point
     * @param lon1 longitude of the first point
     * @param lat2 lattitude of the second point
     * @param lon2 longitude of the second point
     * @return Azimut from first to second [degree]
     */
    public static float computeAzimut(double lat1, double lon1, double lat2, double lon2) {

        //inspired by skript vyssi geodesie
        lat1 = GpsUtils.degToRad(lat1);
        lon1 = GpsUtils.degToRad(lon1);
        lat2 = GpsUtils.degToRad(lat2);
        lon2 = GpsUtils.degToRad(lon2);

        double dLon = (lon2 - lon1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        return (float) ((GpsUtils.radToDeg(LMath.atan2(y, x)) + 360) % 360);
    }

    /** formatuje double na dany pocet desetinych mist */
    public static String formatDouble(double value, int precision) {
        StringBuffer out = new StringBuffer();
        double tail = value;

        if (tail < 0) {
            out.append('-');
            tail = -tail;
        }
        long cel = (long) tail;
        tail -= cel;
        out.append(cel);
        if (precision > 0) {
            out.append('.');
        }
        for (int i = 0; i < precision; i++) {
            tail *= 10;
            cel = (long) tail;
            tail -= cel;
            out.append(cel);
        }
        return out.toString();
    }

    public static String formatFloat(float value, int precision) {
        double d = (double) value;
        return formatDouble(d, precision);
    }

    /**
     * Format speed to correct format.
     * @param Speed Speed in m/s.
     * @return Formated speed in apropriate units.
     */
    public static String formatSpeed(double speed) {
        if (R.getSettings().getUnits() == SettingsData.METRIC) {
            return formatDouble(speed * 3.6, 1) + " kph";
        } else {
            return formatDouble(speed * 2.237, 1) + " mph";
        }
    }

    /**
     * Format distance in metres.
     * @param dist Distance in metres.
     * @return Formated distance in apropriate units.
     */
    public static String formatDistance(double dist) {
        if (R.getSettings().getUnits() == SettingsData.METRIC) {
            if (dist > 1000) {
                double km = dist / 1000.0;
                if (km > 100) {
                    return formatDouble(km, 0) + " km";
                } else {
                    return formatDouble(km, 1) + " km";
                }
            } else {
                return formatDouble(dist, 0) + " m";
            }
        } else {
            if (dist > 1609.3) {
                double mi = dist / 1609.3;
                if (mi > 100) {
                    return formatDouble(mi, 0) + " mi";
                } else {
                    return formatDouble(mi, 1) + " mi";
                }
            } else {
                return formatDouble(dist / 0.9144, 0) + " yd";
            }
        }
    }
    //doplni na minlen nulami zleva. TODO: nebude to blbnout u zapornych cisel.
    public static String formatDouble(double f, int precision, int minlen) {
        double intPart = Math.floor(f);
        double partPart = f - intPart;
        StringBuffer outInt = new StringBuffer(formatDouble(intPart, 0));
        if (outInt.length() < minlen) {
            for (int i = 0; i < minlen - outInt.length(); i++) {
                outInt.insert(0, 0);
            }
        }
        if (partPart != 0) {
            String partStr = Double.toString(partPart);
            outInt.append(".").append(partStr.substring(2, partStr.length()));
        }
        return outInt.toString();
    }

    public static double degToRad(double deg) {
        return deg / LMath.RHO;
    }

    public static double radToDeg(double rad) {
        return rad * LMath.RHO;
    }

    public static String formatLatitude(double latitude, int format, int precisions) {
        StringBuffer out = new StringBuffer();
        String degree = Locale.get(36);
        if (latitude < 0) {
            out.append("S ");
        } else {
            out.append("N ");
        }
        latitude = Math.abs(latitude);
        switch (format) {
            case FORMAT_WGS84:
                out.append(formatDouble(latitude, precisions, 2));
                break;
            case FORMAT_WGS84_MIN:
                double deg = Math.floor(latitude);
                double min = (latitude - deg) * 60;
                out.append(formatDouble(deg, 0, 2) + degree + formatDouble(min, precisions));
                break;
            case FORMAT_WGS84_SEC:
                deg = Math.floor(latitude);
                min = Math.floor((latitude - deg) * 60);
                double sec = (latitude - deg - min / 60) * 3600;
                out.append(formatDouble(deg, 0, 2) + degree + formatDouble(min, 0) + "'" + formatDouble(sec, precisions));
                break;
        }
        return out.toString();
    }

    public static String formatLongitude(double longitude, int format, int precisions) {
        StringBuffer out = new StringBuffer();
        String degree = Locale.get(36);
        if (longitude < 0) {
            out.append("W ");
        } else {
            out.append("E ");
        }
        longitude = Math.abs(longitude);
        switch (format) {
            case FORMAT_WGS84:
                out.append(formatDouble(longitude, precisions, 3));
                break;
            case FORMAT_WGS84_MIN:
                double deg = Math.floor(longitude);
                double min = (longitude - deg) * 60;
                out.append(formatDouble(deg, 0, 2) + degree + formatDouble(min, precisions));
                break;
            case FORMAT_WGS84_SEC:
                deg = Math.floor(longitude);
                min = Math.floor((longitude - deg) * 60);
                double sec = (longitude - deg - min / 60) * 3600;
                out.append(formatDouble(deg, 0, 2) + degree + formatDouble(min, 0) + "'" + formatDouble(sec, precisions));
                break;
        }
        return out.toString();
    }

    /**
     * This method parses separated values from record.
     *
     * @return String values from record as Enumeration
     * @param str  record containing separated values
     * @param delimiter values delimiter char
     */
    public static final Vector parseString(String str, char delimiter) {
        int length = str.length();
        int beginindex = 0;
        Vector v = new Vector();
        for (int i = 0; i < length; i++) {
            if (str.charAt(i) == delimiter) {
                v.addElement(str.substring(beginindex, i).trim());
                beginindex = i + 1;
            }
            if (i == (length - 1)) {
                v.addElement(str.substring(beginindex, length).trim());
            }
        }
        return v;
    }

    public static int parseInt(Object data) {
        return parseInt(String.valueOf(data));
    }

    public static int parseInt(String data) {
        try {
            return Integer.parseInt(data.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Integer parseInteger(String data) {
        try {
            return Integer.valueOf(data.trim());
        } catch (NumberFormatException e) {
            return new Integer(0);
        }
    }

    public static float parseFloat(String data) {
        try {
            return Float.parseFloat(data.trim());
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    public static long parseLong(String data) {
        try {
            return Long.parseLong(data.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double parseDouble(Object data) {
        return parseDouble(String.valueOf(data));
    }

    public static double parseDouble(String data) {
        try {
            return Double.parseDouble(data.trim());
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public static String formatLatitude(double d, int coordsFormat) {
        if (coordsFormat == FORMAT_WGS84) {
            return formatLatitude(d, coordsFormat, 5);
        } else {
            return formatLatitude(d, coordsFormat, 3);
        }
    }

    public static String formatLongitude(double longitude, int coordsFormat) {
        if (coordsFormat == FORMAT_WGS84) {
            return formatLongitude(longitude, coordsFormat, 5);
        } else {
            return formatLongitude(longitude, coordsFormat, 3);
        }
    }

    /** parsruje latitude nebo longitude v nekterem z WGS84 formatu
     *  (+|-|N|W|E|S)dd.dddd
     *  (N|W|E|S)dd mm ss.ss
     *  (N|W|E|S)dd mm.mm
     *  (N|W|E|S)dd°mm.mm
     * TODO: vyhodit ilegal argument na zaklade regexpu
     * 
     * @param coor textova reporezentace
     * @return double hodnota souradnice v uhlovych jednotkach (stupne)
     * @throws IllegalArgumentException if parsing failed
     */
    public static double parseWGS84Coordinate(String coor) throws IllegalArgumentException {
        if (coor.equals("") || coor == null) {
            throw new IllegalArgumentException();
        }
        coor = coor.replace(',', '.').toUpperCase().trim();
        char negativeChars[] = {};
        char startChar = coor.charAt(0);
        int negative;
        coor = Utils.replaceString(coor, Locale.get(36), " ");
        coor = Utils.replaceString(coor, "'", " ");
        //zacina necim co indikuje zaporne hodnoty?
        if ((startChar == '-') || (startChar == 'S') || (startChar == 'W')) {
            negative = -1;
            coor = coor.substring(1, coor.length()).trim();
        } else if ((startChar == '+') || (startChar == 'E') || (startChar == 'N')) {
            coor = coor.substring(1, coor.length()).trim();
            negative = 1;
        } else {
            negative = 1;
        }
        double value = 0;
        Vector tokens = parseString(coor, ' ');
        switch (tokens.size()) {
            case 3: //dd mm ss.sss - zpracuji skundy
                value += Double.parseDouble((String) tokens.elementAt(2)) / 3600;
            case 2: // dd mm.ss
                value += Double.parseDouble(((String) tokens.elementAt(1)).replace('\'', ' ')) / 60;
            case 1: //dd.dddd
                value += Double.parseDouble((String) tokens.elementAt(0));
                break;
        }

        return value * negative;
    }

    /** updated function for time formating as in stop watch */
    public static String formatTime(long tripTime) {
        long hours = tripTime / 3600000;
        long mins = (tripTime - (hours * 3600000)) / 60000;
        double sec = (tripTime - (hours * 3600000) - mins * 60000) / 1000.0;
        if ((hours * 60 + mins) < 10) {
            return mins + "m : " + GpsUtils.formatDouble(sec, 2) + "s";
        } else {
            return hours + "h : " + mins + "m : " + GpsUtils.formatDouble(sec, 1) + "s";
        }
    }

    /** updated function for time formating as in stop watch */
    public static String formatTimeShort(long tripTime) {
        long hours = tripTime / 3600000;
        long mins = (tripTime - (hours * 3600000)) / 60000;
        double sec = (tripTime - (hours * 3600000) - mins * 60000) / 1000.0;
        if ((hours * 60 + mins) < 10) {
            return mins + ":" + GpsUtils.formatDouble(sec, 2);
        } else {
            return hours + ":" + mins + ":" + GpsUtils.formatDouble(sec, 1);
        }
    }
}
