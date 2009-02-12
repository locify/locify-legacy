/*
 * SlowLineInputStream.java
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
package com.locify.client.locator.impl;

import java.io.IOException;
import java.io.InputStream;


/**
 * Class reads from InputStream and make pause after each line
 * @author Jiri Stepan
 */
public class SlowLineInputStream extends InputStream {

    private InputStream input;
    private long pause;
    private String pattern;
    private String line;

    /** 
     * @param original - original input stream
     * @param pattern 
     * @param pause - pause in miliseconds after each line which contains some string pattern
     */
    public SlowLineInputStream(InputStream original, String pattern, long pause) {
        this.pattern = pattern;
        this.input = original;
        this.pause = pause;
        this.line = "";
        this.input.mark(0);
    }

    public int read() throws IOException {
        int ch;
        ch = input.read();
        line += (char) ch;
        if (ch == '\n') {
            if (line.indexOf(pattern) != -1) {
                try {
                    //GpsUtils.makePause(pause);
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            line = ""; //reset line
        }
        if (ch == -1) { //EOF
            input.reset();
        }
        //System.out.println("'" + ((char) ch) + "'");
        return ch;
    }
}
