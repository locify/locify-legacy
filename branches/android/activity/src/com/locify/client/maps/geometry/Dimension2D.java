/*
 * Copyright (c) 2008, Epseelon. All Rights Reserved.
 *
 * This file is part of MobiMap for Java ME.
 *
 * MobiMap for Java ME is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobiMap for Java ME is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please go to http://epseelon.com/mobimap
 * or email us at mobimap@epseelon.com
 */
package com.locify.client.maps.geometry;

/**
 * This stores a dimension in 2-dimensional space - a width (along the x-axis)
 * and height (along the y-axis). The storage is left to subclasses.
 *
 * @author Per Bothner (bothner@cygnus.com)
 * @author Eric Blake (ebb9@email.byu.edu)
 * @since 1.2
 */
public abstract class Dimension2D {
    /**
     * The default constructor.
     */
    protected Dimension2D() {
    }

    /**
     * Get the width of this dimension. A negative result, while legal, is
     * undefined in meaning.
     *
     * @return the width
     */
    public abstract double getWidth();

    /**
     * Get the height of this dimension. A negative result, while legal, is
     * undefined in meaning.
     *
     * @return the height
     */
    public abstract double getHeight();

    /**
     * Set the size of this dimension to the requested values. Loss of precision
     * may occur.
     *
     * @param w the new width
     * @param h the new height
     */
    public abstract void setSize(double w, double h);

    /**
     * Set the size of this dimension to the requested value. Loss of precision
     * may occur.
     *
     * @param d the dimension containing the new values
     * @throws NullPointerException if d is null
     */
    public void setSize(Dimension2D d) {
        setSize(d.getWidth(), d.getHeight());
    }
} // class Dimension2D
