/*
 * VectorSortable.java
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

import java.util.Vector;

/**
 *
 * @author menion
 */
public class VectorSortable extends Vector {

    public static int ASC = 1;
    public static int DESC = 2;

    public void sort(int direction) {
        for (int i = 1; i < size(); i++) {
            if (direction == ASC) {
                for (int j = 0; j < size() - i; j++) {
                    if (((Comparable) elementAt(j)).compareTo((Comparable) elementAt(j + 1)) < 0) {
                        Comparable temp1 = (Comparable) elementAt(j);
                        Comparable temp2 = (Comparable) elementAt(j + 1);
                        setElementAt(temp2, j);
                        setElementAt(temp1, j + 1);
                    }
                }
            } else {
                for (int j = 0; j < size() - i; j++) {
                    if (((Comparable) elementAt(j)).compareTo((Comparable) elementAt(j + 1)) > 0) {
                        Comparable temp1 = (Comparable) elementAt(j);
                        Comparable temp2 = (Comparable) elementAt(j + 1);
                        setElementAt(temp2, j);
                        setElementAt(temp1, j + 1);
                    }
                }
            }
        }
    }
}

