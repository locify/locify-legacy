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
 * This class holds a width and height value pair. This is used in plenty
 * of windowing classes, but also has geometric meaning.
 * <p/>
 * <p>It is valid for a dimension to have negative width or height; but it
 * is considered to have no area. Therefore, the behavior in various methods
 * is undefined in such a case.
 * <p/>
 * <p>There are some public fields; if you mess with them in an inconsistent
 * manner, it is your own fault when you get invalid results. Also, this
 * class is not threadsafe.
 *
 */
public class Dimension extends Dimension2D {
    /**
     * The width of this object.
     *
     * @serial the width
     * @see #getSize()
     * @see #setSize(double, double)
     */
    public int width;

    /**
     * The height of this object.
     *
     * @serial the height
     * @see #getSize()
     * @see #setSize(double, double)
     */
    public int height;

    /**
     * Create a new Dimension with a width and height of zero.
     */
    public Dimension() {
    }

    /**
     * Create a new Dimension with width and height identical to that of the
     * specified dimension.
     *
     * @param d the Dimension to copy
     * @throws NullPointerException if d is null
     */
    public Dimension(Dimension d) {
        width = d.width;
        height = d.height;
    }

    /**
     * Create a new Dimension with the specified width and height.
     *
     * @param w the width of this object
     * @param h the height of this object
     */
    public Dimension(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * Gets the width of this dimension.
     *
     * @return the width, as a double
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height of this dimension.
     *
     * @return the height, as a double
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the size of this dimension. The values are rounded to int.
     *
     * @param w the new width
     * @param h the new height
     * @since 1.2
     */
    public void setSize(double w, double h) {
        width = (int) w;
        height = (int) h;
    }

    /**
     * Returns the size of this dimension. A pretty useless method, as this is
     * already a dimension.
     *
     * @return a copy of this dimension
     * @see #setSize(Dimension)
     * @since 1.1
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * Sets the width and height of this object to match that of the
     * specified object.
     *
     * @param d the Dimension to get the new width and height from
     * @throws NullPointerException if d is null
     * @see #getSize()
     * @since 1.1
     */
    public void setSize(Dimension d) {
        width = d.width;
        height = d.height;
    }

    /**
     * Sets the width and height of this object to the specified values.
     *
     * @param w the new width value
     * @param h the new height value
     */
    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * Tests this object for equality against the specified object.  This will
     * be true if and only if the specified object is an instance of
     * Dimension2D, and has the same width and height.
     *
     * @param obj the object to test against
     * @return true if the object is equal to this
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Dimension))
            return false;
        Dimension dim = (Dimension) obj;
        return height == dim.height && width == dim.width;
    }

    /**
     * Return the hashcode for this object. It is not documented, but appears
     * to be <code>((width + height) * (width + height + 1) / 2) + width</code>.
     *
     * @return the hashcode
     */
    public int hashCode() {
        // Reverse engineering this was fun!
        return (width + height) * (width + height + 1) / 2 + width;
    }

    /**
     * Returns a string representation of this object. The format is:
     * <code>getClass().getName() + "[width=" + width + ",height=" + height
     * + ']'</code>.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return getClass().getName()
                + "[width=" + width + ",height=" + height + ']';
    }
} // class Dimension
