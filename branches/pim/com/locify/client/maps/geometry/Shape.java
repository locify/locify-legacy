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

import com.locify.client.maps.RectangleViewPort;

/**
 * This interface represents an abstract shape. The shape is described by
 * a PathIterator, and has callbacks for determining bounding box,
 * where points and rectangles lie in relation to the shape, and tracing
 * the trajectory.
 * <p/>
 * <p>A point is inside if it is completely inside, or on the boundary and
 * adjacent points in the increasing x or y direction are completely inside.
 * Unclosed shapes are considered as implicitly closed when performing
 * <code>contains</code> or <code>intersects</code>.
 *
 * @author Aaron M. Renn (arenn@urbanophile.com)
 * @since 1.0
 */
public interface Shape {
    /**
     * Returns a <code>Rectange</code> that bounds the shape. There is no
     * guarantee that this is the minimum bounding box, particularly if
     * the shape overflows the finite integer range of a bound. Generally,
     * <code>getBounds2D</code> returns a tighter bound.
     *
     * @return the shape's bounding box
     * @see #getBounds2D()
     */
    RectangleViewPort getBounds();

    /**
     * Returns a high precision bounding box of the shape. There is no guarantee
     * that this is the minimum bounding box, but at least it never overflows.
     *
     * @return the shape's bounding box
     * @see #getBounds()
     * @since 1.2
     */
    Rectangle2D getBounds2D();

    /**
     * Test if the coordinates lie in the shape.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if (x,y) lies inside the shape
     * @since 1.2
     */
    boolean contains(double x, double y);

    /**
     * Test if the point lie in the shape.
     *
     * @param p the high-precision point
     * @return true if p lies inside the shape
     * @throws NullPointerException if p is null
     * @since 1.2
     */
    boolean contains(Point2D p);

    /**
     * Test if a high-precision rectangle intersects the shape. This is true
     * if any point in the rectangle is in the shape, with the caveat that the
     * operation may include high probability estimates when the actual
     * calculation is prohibitively expensive.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param w the width of the rectangle, undefined results if negative
     * @param h the height of the rectangle, undefined results if negative
     * @return true if the rectangle intersects this shape
     * @since 1.2
     */
    boolean intersects(double x, double y, double w, double h);

    /**
     * Test if a high-precision rectangle intersects the shape. This is true
     * if any point in the rectangle is in the shape, with the caveat that the
     * operation may include high probability estimates when the actual
     * calculation is prohibitively expensive.
     *
     * @param r the rectangle
     * @return true if the rectangle intersects this shape
     * @throws NullPointerException if r is null
     * @see #intersects(double, double, double, double)
     * @since 1.2
     */
    boolean intersects(Rectangle2D r);

    /**
     * Test if a high-precision rectangle lies completely in the shape. This is
     * true if all points in the rectangle are in the shape, with the caveat
     * that the operation may include high probability estimates when the actual
     * calculation is prohibitively expensive.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param w the width of the rectangle, undefined results if negative
     * @param h the height of the rectangle, undefined results if negative
     * @return true if the rectangle is contained in this shape
     * @since 1.2
     */
    boolean contains(double x, double y, double w, double h);

    /**
     * Test if a high-precision rectangle lies completely in the shape. This is
     * true if all points in the rectangle are in the shape, with the caveat
     * that the operation may include high probability estimates when the actual
     * calculation is prohibitively expensive.
     *
     * @param r the rectangle
     * @return true if the rectangle is contained in this shape
     * @throws NullPointerException if r is null
     * @see #contains(double, double, double, double)
     * @since 1.2
     */
    boolean contains(Rectangle2D r);
} // interface Shape
