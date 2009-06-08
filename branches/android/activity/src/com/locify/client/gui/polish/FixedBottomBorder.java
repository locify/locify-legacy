 /*
  * FixedBotomBorder.java
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
 package com.locify.client.gui.polish;
 
 import de.enough.polish.ui.borders.BottomBorder;
 
import de.enough.polish.android.lcdui.Graphics;
 
 /**
  * This class overrides standart Polish BottomBorder and fix one-pixel space between border and background
  * @author David Vavra
  */
 public class FixedBottomBorder extends BottomBorder {
 
 	private final int color;
 
 	/**
 	 * Creates a new simple border.
 	 * 
         * @param color the color of this border in RGB, e.g. 0xFFDD12
         * @param width 
 	 */
 	public FixedBottomBorder( int color, int width ) {
 		super(color, width);
 		this.color = color;
        this.borderWidthBottom = width;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.enough.polish.ui.Borderpaint(int, int, int, int, javax.microedition.lcdui.Graphics)
 	 */
 	public void paint(int x, int y, int width, int height, Graphics g) {
 		g.setColor( this.color );
 		y += height-1;
 		int endX = x + width;
 		g.drawLine( x, y, endX, y );
 		if (this.borderWidthBottom > 1) {
 			int border = this.borderWidthBottom - 1;
 			while ( border > 0) {
 				g.drawLine( x, y - border, endX, y - border );
 				border--;
 			}
 		}
 	}
 
 }
