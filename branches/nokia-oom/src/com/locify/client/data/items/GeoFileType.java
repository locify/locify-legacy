/*
 * GeoFileType.java
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
package com.locify.client.data.items;

import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.StringTokenizer;

/**
 *
 * @author menion
 */
public class GeoFileType {
    private String name;
    private long size;
    private int type;
    private boolean used;

    public GeoFileType(String rsData) {
        String[] data = StringTokenizer.getArray(rsData, ";");
        this.name = data[0];
        this.size = GpsUtils.parseLong(data[1]);
        this.type = GpsUtils.parseInt(data[2]);
        //System.out.println("Created fileType: " + name + " " + size + " " + type);
    }

    public GeoFileType(String name, long size, int type) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.used = false;
    }

    public byte[] getBytesToWrite() {
        return (new String(name + ";" + size + ";" + type)).getBytes();
    }

    public boolean isUsed() {
        return used;
    }

    public boolean compare(String name, long size) {
//System.out.println("Compare: " + this.name + " " + name + " " + this.size + " " + size);
        boolean compare = this.name.equals(name) && this.size == size;
        if (compare) {
            used = true;
            return true;
        } else {
            return false;
        }
    }

    public int getType() {
        return type;
    }
}
