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

import de.enough.polish.ui.CommandItem;
import de.enough.polish.ui.Screen;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import de.enough.polish.util.base64.Base64;

/***
 * This class contains methods usefull in whole app
 * @author Destil
 */
public class Utils {

    final static String[] hex = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
        "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
        "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
        "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
        "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
        "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
        "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
        "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
        "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
        "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
        "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
        "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
        "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
        "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
        "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
        "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
        "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
        "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
        "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
        "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
        "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
        "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
        "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
        "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
        "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
        "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
        "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
    };

    /***
     * Encode a string to the "x-www-form-urlencoded" form, enhanced
     * with the UTF-8-in-URL proposal. This is what happens:
     *
     * <ul>
     * <li><p>The ASCII characters 'a' through 'z', 'A' through 'Z',
     *        and '0' through '9' remain the same.
     *
     * <li><p>The unreserved characters - _ . ! ~ * ' ( ) remain the same.
     *
     * <li><p>The space character ' ' is converted into a plus sign '+'.
     *
     * <li><p>All other ASCII characters are converted into the
     *        3-character string "%xy", where xy is
     *        the two-digit hexadecimal representation of the character
     *        code
     *
     * <li><p>All non-ASCII characters are encoded in two steps: first
     *        to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
     *        secondly each of these bytes is encoded as "%xx".
     * </ul>
     *
     * @param s The string to be encoded
     * @return The encoded string
     */
    public static String urlUTF8Encode(String s) {
        StringBuffer sbuf = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int ch = s.charAt(i);
            if ('A' <= ch && ch <= 'Z') {		// 'A'..'Z'

                sbuf.append((char) ch);
            } else if ('a' <= ch && ch <= 'z') {	// 'a'..'z'

                sbuf.append((char) ch);
            } else if ('0' <= ch && ch <= '9') {	// '0'..'9'

                sbuf.append((char) ch);
            } else if (ch == ' ') {			// space

                sbuf.append('+');
            } else if (ch == '-' || ch == '_' // unreserved
                    || ch == '.' || ch == '!' || ch == '~' || ch == '*' || ch == '\'' || ch == '(' || ch == ')') {
                sbuf.append((char) ch);
            } else if (ch <= 0x007f) {		// other ASCII

                sbuf.append(hex[ch]);
            } else if (ch <= 0x07FF) {		// non-ASCII <= 0x7FF

                sbuf.append(hex[0xc0 | (ch >> 6)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            } else {					// 0x7FF < ch <= 0xFFFF

                sbuf.append(hex[0xe0 | (ch >> 12)]);
                sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            }
        }
        return sbuf.toString();
    }

    /*
     * Decode a url-encoded string.
     */
    public static String urlUTF8decode(String str) {
        StringBuffer result = new StringBuffer();
        int l = str.length();
        for (int i = 0; i < l; ++i) {
            char c = str.charAt(i);
            if (c == '%' && i + 2 < l) {
                char c1 = str.charAt(i + 1);
                char c2 = str.charAt(i + 2);
                if (isHexit(c1) && isHexit(c2)) {
                    result.append((char) (hexit(c1) * 16 + hexit(c2)));
                    i += 2;
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static boolean isHexit(char c) {
        String legalChars = "0123456789abcdefABCDEF";
        return (legalChars.indexOf(c) != -1);
    }

    private static int hexit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return 0;	// shouldn't happen, we're guarded by isHexit()
    }

    /**
     * Implementation of str_replace
     * @param haystack Text to replace
     * @param needle String to search
     * @param replaceWith String to replace
     * @return Replaced text
     */
    public static String replaceString(String haystack, String needle, String replaceWith) {
        // String buffer to store str
        StringBuffer sb = new StringBuffer();

        // Search for search
        int searchStringPos = haystack.indexOf(needle);
        int startPos = 0;
        int searchStringLength = needle.length();

        // Iterate to add string
        while (searchStringPos != -1) {
            sb.append(haystack.substring(startPos, searchStringPos)).append(replaceWith);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = haystack.indexOf(needle, startPos);
        }

        // Create string
        sb.append(haystack.substring(startPos, haystack.length()));

        return sb.toString();
    }

    /**
     * Adds '0' chars at the beginning of string of specified length
     * Menion: this is one of most memory eating function !!!
     * @param s original string
     * @param length desired length
     * @return string with zeros
     */
    public static String addZerosBefore(String s, int length) {
        String prefix = "";
        for (int i = 0; i < (length - s.length()); i++) {
            prefix = prefix.concat("0");
        }
        return prefix + s;
    }

    /**
     * Adds '0' chars at the end of string of specified length
     * @param s original string
     * @param length desired length
     * @return string with zeros
     */
    public static String addZerosAfter(String s, int length) {
        String postfix = "";
        for (int i = 0; i < (length - s.length()); i++) {
            postfix = postfix.concat("0");
        }
        return s + postfix;
    }

    /**
     * Returns standart unix timestamp
     * @return unix timestamp
     */
    public static long timestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Converts expire date from cookies to unix timestamp
     * Sample cookie date: expires=Fri, 31 Dec 2010 23:59:59 GMT;
     *                 or: expires=Fri, 31-Dec-2010 23:59:59 GMT;
     *                 or: Sat04 Jul 2009 12:24:30 GMT
     * @param expires cookie expire string
     * @return unix timestamp
     */
    public static long expiresToTimestamp(String expires) {
        try {
            String[] parts = StringTokenizer.getArray(expires, " ");
            int day, month, year;
            String[] time;
            if (expires.indexOf("-") > 0) {
                String[] date = StringTokenizer.getArray(parts[1], "-");
                day = Integer.parseInt(date[0]);
                month = numericMonth(date[1]);
                year = Integer.parseInt(date[2]);
                time = StringTokenizer.getArray(parts[2], ":");
            } else {
                if (parts[0].length() == 5) {
                    day = Integer.parseInt(parts[0].substring(3, 5));
                    month = numericMonth(parts[1]);
                    year = Integer.parseInt(parts[2]);
                    time = StringTokenizer.getArray(parts[3], ":");
                } else {
                    day = Integer.parseInt(parts[1]);
                    month = numericMonth(parts[2]);
                    year = Integer.parseInt(parts[3]);
                    time = StringTokenizer.getArray(parts[4], ":");
                }
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
            calendar.set(Calendar.SECOND, Integer.parseInt(time[2]));
            Date timestamp = calendar.getTime();
            return timestamp.getTime();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Utils.expiresToTimestamp", expires);
            return -1;
        }
    }


    /**
     * Returns numeric month from three-letter name
     * @param month three-letter name
     * @return number of month
     */
    public static int numericMonth(String month) {
        if (month.equals("Jan")) {
            return Calendar.JANUARY;
        }
        if (month.equals("Feb")) {
            return Calendar.FEBRUARY;
        }
        if (month.equals("Mar")) {
            return Calendar.MARCH;
        }
        if (month.equals("Apr")) {
            return Calendar.APRIL;
        }
        if (month.equals("May")) {
            return Calendar.MAY;
        }
        if (month.equals("Jun")) {
            return Calendar.JUNE;
        }
        if (month.equals("Jul")) {
            return Calendar.JULY;
        }
        if (month.equals("Aug")) {
            return Calendar.AUGUST;
        }
        if (month.equals("Sep")) {
            return Calendar.SEPTEMBER;
        }
        if (month.equals("Oct")) {
            return Calendar.OCTOBER;
        }
        if (month.equals("Nov")) {
            return Calendar.NOVEMBER;
        }
        if (month.equals("Dec")) {
            return Calendar.DECEMBER;
        }
        return 0;
    }

    /**
     * Removes last occurence of selected char in string
     * @param s original string
     * @param ch char to be replaced
     * @return altered string
     */
    public static String removeLast(String s, char ch) {
        s = s.trim();
        if (s.lastIndexOf(ch) == s.length() - 1) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Removes <!xml and <!DOCTYPE headers
     * @param xml xml with headers
     * @return xml without headers
     */
    public static String removeXmlHeaders(String xml) {
        // <?xml .. 
        if (xml.startsWith("<?xml version") || xml.indexOf("<?xml version") > 0) {
            String[] cutted = StringTokenizer.getArray(xml, "?>");
            xml = cutted[1].substring(1);
        }
        //<!DOCTYPE .. 
        if (xml.startsWith("<!DOCTYPE") || xml.indexOf("<!DOCTYPE") > 0) {
            String[] cutted = StringTokenizer.getArray(xml, ".dtd\">");
            xml = cutted[1].substring(5);
        }
        return xml;
    }

    /**
     * Get actual date at format
     * @return Time at format for example -  "23. 12. 1987  -  21:12:25"
     */
    public static String getActualDate() {
        String time = (new Date()).toString();
        Vector vec = StringTokenizer.getVector(time, " ");
        return vec.elementAt(2) + ". " + vec.elementAt(1) + " " +
                vec.elementAt(5) + "  -  " + vec.elementAt(3);
    }

    /**
     * Removes subcommand from mainscreen
     * @param child child command
     * @param parent parent command
     */
    public static void removeSubCommand(Command child, Command parent, Screen screen) {
        CommandItem ci = screen.getCommandItem(parent);
        ci.removeChild(child);
    }

    public static int readInt(byte[] buffer, int start, int len) {
        int result = 0;
		for (int i = 0; i < len; i++) {
			int n = (buffer[start + i] < 0 ? (int) buffer[start + i] + 256 : (int) buffer[start + i]) << (8 * i);
			result += n;
		}

        return result;
    }

	public static double readDouble(byte[] buffer, int start, int len) {
		long result = 0;
		for (int i = 0; i < len; i++) {
			result |= ((long)(buffer[start + i] & 0xff)) << (i * 8);
		}
		return Double.longBitsToDouble(result);
	}

    /**
     * Print actual memory values to System.out
     */
    public static void printMemoryState() {
        Runtime rt = Runtime.getRuntime();
        System.out.println("\n*************** MemoryStatistics ***********");
        System.out.println("  free     = " + rt.freeMemory());
        System.out.println("  total    = " + rt.totalMemory());
        System.out.println("  consumed = " + (rt.totalMemory() - rt.freeMemory()));
        System.out.println("*******************************************");
    }

}
