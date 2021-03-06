/**
 * StringTokenizer.java
 * J2ME GPS Track, Copyright (C) 2006 Dana Peters, http://www.qcontinuum.org/gpstrack
 */
package com.locify.client.utils;

import java.util.Vector;

/**
 * This utility cuts string with separator and returns as Vector or Array
 */
public class StringTokenizer {

    public static Vector getVector(String tokenList, String separator) {
        Vector tokens = new Vector();
        int commaPos = 0;
        String token = "";
        int cnt = 0;
        commaPos = tokenList.indexOf(separator);
//System.out.println("CommPos: " + commaPos);
        while (commaPos > 0) {
            commaPos = tokenList.indexOf(separator);
            if (commaPos > 0) {
                token = tokenList.substring(0, commaPos);
//System.out.println("Token: '" + token + "'");
                tokenList = tokenList.substring(commaPos,tokenList.length());
            }
            if (!token.startsWith(separator))
                tokens.addElement(token);
            while (tokenList.startsWith(separator)) {
//                cnt++;
//                if (cnt >= 2)
//                    tokens.addElement("");
                tokenList = tokenList.substring(1,tokenList.length());
                commaPos = tokenList.indexOf(separator);
            }
            cnt = 0;
        }
        if (commaPos < 0) {
            token = tokenList;
            tokens.addElement(token);
        }
        return tokens;
    }

    public static String[] getArray(String tokenList, String separator) {
        Vector tokens = getVector(tokenList,separator);
        String[] st = new String[tokens.size()];
        for (int i = 0; i <= tokens.size() - 1; i++)
            st[i] = (String)tokens.elementAt(i);
        return st;
    }

}
