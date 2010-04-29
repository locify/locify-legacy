/*
 * Logger.java
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

import com.locify.client.data.FileSystem;

/**
 * Simple class for logging. It should be used instead of using System.out.println
 * @author Jiri Stepan
 */
public class Logger {

    public static final int SEVERITY_DEBUG = 0;
    public static final int SEVERITY_INFO = 1;
    public static final int SEVERITY_WARNING = 2;
    public static final int SEVERITY_ERROR = 3;
    public static String out = "";
    private static String fileName;

    private static int MAX_SIZE = 3000;
    private static String MAX_SIZE_MESSAGE = "\nLog will truncated ...";

    private static void log(String message, int severity, boolean breaking) {

        //#if !release

        out = message + "\n" + out; //Fist message to top
        if( out.length() > MAX_SIZE) {
            out = out.substring(0, MAX_SIZE) + MAX_SIZE_MESSAGE;
        }

            //#if logToSysOut
//#                 if (breaking) {
//#                     System.out.println();
//#                 }
//# 
//#                 if (severity != SEVERITY_ERROR) {
//#                     System.out.print(message);
//#                 } else {
//#                     System.err.print(message);
//#                 }
            //#endif

            //#if logToFile
//#                 if (fileName == null) {
//#                     fileName = "debug_" + System.currentTimeMillis() + ".txt";
//#                 }
//# 
//#                 if (breaking) {
//#                     message += "\n" + message;
//#                 }
//# 
//#                 // Logging only enabled severity, see on constanse
//#                 switch (severity) {
//#                     case SEVERITY_ERROR:
//#                     case SEVERITY_INFO:
//#                     case SEVERITY_WARNING:
//#                     case SEVERITY_DEBUG:
//#                         R.getFileSystem().saveStringToEof(FileSystem.LOG_FOLDER + fileName, message);
//#                         break;
//#                 }
            //#endif

        //#endif
    }

    public static void log(String message) {
        log(message, SEVERITY_INFO, true);
    }

    public static void logNoBreak(String message) {
        log(message, SEVERITY_INFO, false);
    }

    public static void debug(String message) {
        log(message, SEVERITY_DEBUG, true);
    }

    public static void error(String message) {
        log("Exception: " + message, SEVERITY_ERROR, true);
    }

    public static void warning(String message) {
        log("Warning: " + message, SEVERITY_WARNING, true);
    }

    public static String getOutText() {
        return out;
    }
}
