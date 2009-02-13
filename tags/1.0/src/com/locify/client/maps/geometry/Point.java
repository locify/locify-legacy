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
 * This class represents a point on the screen using cartesian coordinates.
 * Remember that in screen coordinates, increasing x values go from left to
 * right, and increasing y values go from top to bottom.
 * <p/>
 * <p>There are some public fields; if you mess with them in an inconsistent
 * manner, it is your own fault when you get invalid results. Also, this
 * class is not threadsafe.
 *
 */
public class Point extends Point2D {

    /**
     * The x coordinate.
     *
     * @serial the X coordinate of the point
     * @see #getLocation()
     * @see #move(int, int)
     */
    public int x;

    /**
     * The y coordinate.
     *
     * @serial The Y coordinate of the point
     * @see #getLocation()
     * @see #move(int, int)
     */
    public int y;

    /**
     * Initializes a new instance of <code>Point</code> representing the
     * coordiates (0,0).
     *
     * @since 1.1
     */
    public Point() {
    }

    /**
     * Initializes a new instance of <code>Point</code> with coordinates
     * identical to the coordinates of the specified points.
     *
     * @param p the point to copy the coordinates from
     * @throws NullPointerException if p is null
     */
    public Point(Point p) {
        x = p.x;
        y = p.y;
    }

    /**
     * Initializes a new instance of <code>Point</code> with the specified
     * coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get the x coordinate.
     *
     * @return the value of x, as a double
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y coordinate.
     *
     * @return the value of y, as a double
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the location of this point. A pretty useless method, as this
     * is already a point.
     *
     * @return a copy of this point
     * @see #setLocation(Point)
     * @since 1.1
     */
    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * Sets this object's coordinates to match those of the specified point.
     *
     * @param p the point to copy the coordinates from
     * @throws NullPointerException if p is null
     * @since 1.1
     */
    public void setLocation(Point p) {
        x = p.x;
        y = p.y;
    }

    /**
     * Sets this object's coordinates to the specified values.  This method
     * is identical to the <code>move()</code> method.
     *
     * @param x the new X coordinate
     * @param y the new Y coordinate
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets this object's coordinates to the specified values.  This method
     * performs normal casting from double to int, so you may lose precision.
     *
     * @param x the new X coordinate
     * @param y the new Y coordinate
     */
    public void setLocation(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    /**
     * Sets this object's coordinates to the specified values.  This method
     * is identical to the <code>setLocation(int, int)</code> method.
     *
     * @param x the new X coordinate
     * @param y the new Y coordinate
     */
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Changes the coordinates of this point such that the specified
     * <code>dx</code> parameter is added to the existing X coordinate and
     * <code>dy</code> is added to the existing Y coordinate.
     *
     * @param dx the amount to add to the X coordinate
     * @param dy the amount to add to the Y coordinate
     */
    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Tests whether or not this object is equal to the specified object.
     * This will be true if and only if the specified object is an instance
     * of Point2D and has the same X and Y coordinates.
     *
     * @param obj the object to test against for equality
     * @return true if the specified object is equal
     */
    public boolean equals(Object obj) {
        // NOTE: No special hashCode() method is required for this class,
        // as this equals() implementation is functionally equivalent to
        // super.equals(), which does define a proper hashCode().

        if (!(obj instanceof Point2D))
            return false;
        Point2D p = (Point2D) obj;
        return x == p.getX() && y == p.getY();
    }

    /**
     * Returns a string representation of this object. The format is:
     * <code>getClass().getName() + "[x=" + x + ",y=" + y + ']'</code>.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + ']';
    }
} // class Point
