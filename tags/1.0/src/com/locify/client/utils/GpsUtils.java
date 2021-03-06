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
    
    public static final double RHO = 180/Math.PI;

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

    /* formatuje vzdalenost v m */
    public static String formatDistance(double dist) {
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
        return deg / RHO;
    }

    public static double radToDeg(double rad) {
        return rad * RHO;
    }

    public static String formatLatitude(double latitude, int format, int precisions) {
        StringBuffer out = new StringBuffer();
        String degree = Locale.get("Degree");
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
        String degree = Locale.get("Degree");
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

    public static int parseInt(String data) {
        try {
            return Integer.parseInt(data);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Integer parseInteger(String data) {
        try {
            return Integer.valueOf(data);
        } catch (NumberFormatException e) {
            return new Integer(0);
        }
    }

    public static float parseFloat(String data) {
        try {
            return Float.parseFloat(data);
        } catch (NumberFormatException e) {
            return 0.0f;
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
        coor = Utils.replaceString(coor, Locale.get("Degree"), " ");
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
