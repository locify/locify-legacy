/*
 * TempRecord.java
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

/**
 *
 * @author menion
 */
public class TempRecord {

    public int x;
    public int y;
    public int recordStart;
    public int recordLength;

    public TempRecord(int recordStart, int recordLength) {
        this.recordStart = recordStart;
        this.recordLength = recordLength;
    }

    public TempRecord(String name, int recordStart, int recordLength) {
        this(recordStart, recordLength);
        this.x = GpsUtils.parseInt(name.substring(0, name.indexOf("_")));
        this.y = GpsUtils.parseInt(name.substring(name.indexOf("_") + 1));
    }

    public String toString() {
        return "TempRecord [recordStart: " + recordStart + " recordLength: " + recordLength + " sortX: " + x + " sortY: " + y + "]";
    }
}
