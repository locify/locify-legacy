/*
 * StorageTarInputStream.java
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

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream for reading from TAR
 * @author Menion
 */
public class StorageTarInputStream extends InputStream {

    InputStream inputStream;
    long size;

    public StorageTarInputStream(InputStream a_is, long a_size) {
        super();
        this.size = a_size;
        this.inputStream = a_is;
    }

    public int read() throws IOException {
        if (this.size <= 0) {
            return -1;
        }
        this.size--;
        return this.inputStream.read();
    }
}
