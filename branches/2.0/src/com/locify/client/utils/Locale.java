/*
 * Locale.java
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

import com.sun.lwuit.util.Resources;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 *
 * @author menion
 */
public class Locale {

    private static Hashtable lang;

    public static String get(String string) {
        if (lang != null)
            return (String) lang.get(string);
        return "unknown";
    }

    public static void loadTranslations(DataInputStream dataInputStream) {
        
    }

    public static void loadTranslations(String string) {
        try {
            if (lang != null)
                return;
            
            if (string.length() > 2) {
                string = string.substring(0, 2);
            }
//System.out.println("Load: " + "resource_lang_" + string);
            Resources res = ResourcesLocify.getResource("resource_lang_" + string);
            if (res == null) {
                res = ResourcesLocify.getResource("resource_lang_en");
            }

            if (res != null) {
                lang = res.getL10N("localize", string);
                res = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
