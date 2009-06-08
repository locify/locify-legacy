//#condition polish.usePolishGui
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Sat Dec 06 15:06:43 CET 2003
/*
 * Copyright (c) 2004-2005 Robert Virkus / Enough Software
 *
 * This file is part of J2ME Polish.
 *
 * J2ME Polish is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * J2ME Polish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with J2ME Polish; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Commercial licenses are also available, please
 * refer to the accompanying LICENSE.txt or visit
 * http://www.j2mepolish.org for details.
 */
package de.enough.polish.ui;

import de.enough.polish.android.lcdui.Graphics;

/**
 * Implements a &quot;ticker-tape&quot;, a piece of text that runs
 * continuously across the display. The direction and speed of scrolling are
 * determined by the implementation. While animating, the ticker string
 * scrolls continuously. That is, when the string finishes scrolling off the
 * display, the ticker starts over at the beginning of the string.
 * 
 * <p> There is no API provided for starting and stopping the ticker. The
 * application model is that the ticker is always scrolling continuously.
 * However, the implementation is allowed to pause the scrolling for power
 * consumption purposes, for example, if the user doesn't interact with the
 * device for a certain period of time. The implementation should resume
 * scrolling the ticker when the user interacts with the device again. </p>
 * 
 * <p>The text of the ticker may contain
 * <A HREF="Form.html#linebreak">line breaks</A>.
 * The complete text MUST be displayed in the ticker;
 * line break characters should not be displayed but may be used
 * as separators. </p>
 * 
 * <p> The same ticker may be shared by several <code>Displayable</code>
 * objects (&quot;screens&quot;). This can be accomplished by calling
 * <A HREF="../../../javax/microedition/lcdui/Displayable.html#setTicker(javax.microedition.lcdui.Ticker)"><CODE>setTicker()</CODE></A> on each of them.
 * Typical usage is for an application to place the same ticker on
 * all of its screens. When the application switches between two screens that
 * have the same ticker, a desirable effect is for the ticker to be displayed
 * at the same location on the display and to continue scrolling its contents
 * at the same position. This gives the illusion of the ticker being attached
 * to the display instead of to each screen. </p>
 * 
 * <p> An alternative usage model is for the application to use different
 * tickers on different sets of screens or even a different one on each
 * screen. The ticker is an attribute of the <code>Displayable</code> class
 * so that
 * applications may implement this model without having to update the ticker
 * to be displayed as the user switches among screens. </p>
 * <HR>
 * 
 * Supported CSS Attributes, next to any IconItem and StringItem attributes:
 * <ul>
 * 	<li><b>ticker-step</b>: the number of pixels by which the ticker is moved in every animation step, defaults to 2.</li>
 * 	<li><b>ticker-position</b>: the position of the ticker relative to the screen - either top or bottom</li>
 * 	<li><b>ticker-direction</b>: the direction of the ticker, defaults to 'left' (meaning going from right to left).</li>
 * </ul>
 * @see StringItem
 * @see IconItem
 * @since MIDP 1.0
 */
public class Ticker extends IconItem
{
	/**
	 * The ticker moves from right to left.
	 */
	public static final int DIRECTION_RIGHT_TO_LEFT = 0;
	/**
	 * The ticker moves from left to right.
	 */
	public static final int DIRECTION_LEFT_TO_RIGHT = 1;
	/** the x offset - if it increased, then the text will be painted more to the left side */ 
	private int tickerXOffset;
	private int step = 2;
	private int tickerWidth;
	private int direction;

	/**
	 * Constructs a new <code>Ticker</code> object, given its initial
	 * contents string.
	 * 
	 * @param str string to be set for the Ticker
	 * @throws NullPointerException if str is null
	 */
	public Ticker( String str)
	{
		this( str, null );
	}

	/**
	 * Constructs a new <code>Ticker</code> object, given its initial
	 * contents string.
	 * 
	 * @param str string to be set for the Ticker
	 * @param style the CSS style for this item
	 * @throws NullPointerException if str is null
	 */
	public Ticker( String str, Style style )
	{
		super( null, null, style );
		setString( str );
		setAppearanceMode( Item.PLAIN );
		//#if polish.i18n.rightToLeft
			//# this.direction = DIRECTION_LEFT_TO_RIGHT;
		//#endif
	}
	
	/**
	 * Retrieves the shown text of this ticker.
	 * 
	 * @return the ticker text
	 */
	public String getString() {
		return this.text;
	}
	
	/**
	 * Sets the ticker text
	 * 
	 * @param text the text that is being scrolled
	 */
	public void setString( String text ) {
		if (text != null) {
			text = text.replace('\n', ' ');
		}
		super.setText(text);
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#initContent(int, int)
	 */
	protected void initContent(int firstLineWidth, int availWidth, int availHeight) {
		super.initContent( Integer.MAX_VALUE, Integer.MAX_VALUE, availHeight );
		this.tickerWidth = this.contentWidth;
		//#debug
		//# System.out.println("init content of ticker " + this.toString() + ", tickerWidth=" + this.tickerWidth + ", textVisible=" + this.isTextVisible) ;
		this.contentWidth = availWidth;
		if (this.direction == DIRECTION_LEFT_TO_RIGHT) {
			this.tickerXOffset = this.tickerWidth;
		} else {
			this.tickerXOffset = - availWidth;
		}
		
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#paintContent(int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	public void paintContent(int x, int y, int leftBorder, int rightBorder, Graphics g) {
		//System.out.println("painting ticker at " + x + " width itemWidth=" + this.itemWidth);
		//System.out.println("rightBorder=" + rightBorder + ", screenWidth=" + this.screen.screenWidth);
		int clipX = g.getClipX();
		int clipY = g.getClipY();
		int clipHeight = g.getClipHeight();
		int clipWidth = g.getClipWidth();
		int width = (rightBorder - leftBorder);
		g.clipRect( x, clipY, width, clipHeight);
		x -= this.tickerXOffset;
		
		super.paintContent(x, y, leftBorder, rightBorder, g);
		if (this.direction == DIRECTION_RIGHT_TO_LEFT) {
			if (x < leftBorder &&  x + this.tickerWidth + this.paddingHorizontal < rightBorder) {
				// the item can be wrapped to the other side again:
				if (this.tickerWidth > width) {
					x += this.tickerWidth + this.paddingHorizontal;
				} else {
					x = rightBorder + (x - leftBorder);
				}
				super.paintContent(x, y, leftBorder, rightBorder, g);
			}
		} else {
//			if (this.tickerXOffset < 0) {
//				x += this.tickerXOffset; 
//				x -= this.tickerWidth + this.tickerXOffset - this.paddingHorizontal;
////				if (this.tickerWidth > width) {
////					
////				} else {
////				}
//				super.paintContent(x, y, leftBorder, rightBorder, g);
//			}
		}
		
		g.setClip(clipX, clipY, clipWidth, clipHeight);
		
	}

	//#ifdef polish.useDynamicStyles
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#getCssSelector()
	 */
	protected String createCssSelector() {
		return "ticker";
	}
	//#endif

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#setStyle(de.enough.polish.ui.Style)
	 */
	public void setStyle(Style style, boolean resetStyle) {
		super.setStyle(style, resetStyle);
		if (resetStyle) {
			this.font = style.getFont();
			this.textColor = style.getFontColor();
		}
		//#ifdef polish.css.ticker-step
			//# Integer stepInt = style.getIntProperty(35);
			//# if (stepInt != null) {
				//# this.step = stepInt.intValue();
			//# }
		//#endif
		//#ifdef polish.css.ticker-direction
			//# Integer directionInt = style.getIntProperty(390);
			//# if (directionInt != null) {
				//# this.direction = directionInt.intValue();
			//# }
		//#endif
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#animate()
	 */
	public boolean animate() {
		if (this.tickerWidth == 0) {
			return false;
		}
		if (this.direction == DIRECTION_RIGHT_TO_LEFT) {
			if (this.tickerXOffset < this.tickerWidth) {
				this.tickerXOffset += this.step;
			} else {
				if (this.tickerWidth > this.contentWidth) {
					this.tickerXOffset -= (this.tickerWidth + this.paddingHorizontal) - this.step;
				} else {
					this.tickerXOffset = (this.tickerXOffset - this.contentWidth) + this.step;
				}
			}
		} else {
			// direction is left to right:
			if (this.tickerXOffset > -this.contentWidth) {
				this.tickerXOffset -= this.step;
			} else {
				this.tickerXOffset = this.tickerWidth;
			}
		}
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.FakeStringCustomItem#hideNotify()
	 */
	protected void hideNotify() {
		super.hideNotify();
		AnimationThread.removeAnimationItem( this );
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.FakeStringCustomItem#showNotify()
	 */
	protected void showNotify() {
		super.showNotify();
		AnimationThread.addAnimationItem( this );
	}
}
