//#condition polish.usePolishGui
// generated by de.enough.doc2java.Doc2Java (www.enough.de) on Sat Dec 06 15:06:44 CET 2003
/* 
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
 * A blank, non-interactive item that has a settable minimum size.
 * 
 * <p>
 * The usage of the Spacer item is discouraged for applications which
 * use J2ME Polish, since margins and paddings can be set for each item
 * anyhow.
 * </p>
 * 
 * <p>The
 * minimum width is useful for allocating flexible amounts of space between
 * <code>Items</code> within the same row of a
 * <code>Form</code>.  The minimum height is useful for
 * enforcing a particular minimum height of a row.  The application can set
 * the minimum width or height to any non-negative value.  The implementation
 * may enforce implementation-defined maximum values for the minimum width and
 * height.</p>
 * 
 * <p>The unlocked preferred width of a <code>Spacer</code> is the same as its
 * current minimum width.  Its unlocked preferred height is the same as its
 * current minimum height.</p>
 * 
 * <p>Since a <code>Spacer's</code> primary purpose is to position
 * other items, it is
 * restricted to be non-interactive, and the application is not allowed to add
 * <code>Commands</code> to a <code>Spacer</code>.  Since the
 * presence of a label on an <code>Item</code> may affect
 * layout in device-specific ways, the label of a
 * <code>Spacer</code> is restricted to
 * always be <code>null</code>, and the application is not allowed
 * to change it.</p>
 * 
 * @author Robert Virkus, robert@enough.de
 * @since MIDP 2.0
 */
public class Spacer extends Item
{

	/**
	 * Creates a new <code>Spacer</code> with the given minimum
	 * size.  The <code>Spacer's</code> label
	 * is <code>null</code>.
	 * The minimum size must be zero or greater.
	 * If <code>minWidth</code> is greater than the
	 * implementation-defined maximum width, the maximum
	 * width will be used instead.
	 * If <code>minHeight</code> is greater than the
	 * implementation-defined maximum height, the maximum
	 * height will be used instead.
	 * 
	 * @param minWidth - the minimum width in pixels
	 * @param minHeight - the minimum height in pixels
	 * @throws IllegalArgumentException - if either minWidth or minHeight is less than zero
	 */
	public Spacer(int minWidth, int minHeight)
	{
		this.contentWidth = minWidth;
		this.contentHeight = minHeight;
	}

	/**
	 * Sets the minimum size for this spacer.  The
	 * <code>Form</code> will not
	 * be allowed to make the item smaller than this size.
	 * The minimum size must be zero or greater.
	 * If <code>minWidth</code> is greater than the
	 * implementation-defined maximum width, the maximum
	 * width will be used instead.
	 * If <code>minHeight</code> is greater than the
	 * implementation-defined maximum height, the maximum
	 * height will be used instead.
	 * 
	 * @param minWidth - the minimum width in pixels
	 * @param minHeight - the minimum height in pixels
	 * @throws IllegalArgumentException - if either minWidth or minHeight is less than zero
	 */
	public void setMinimumSize(int minWidth, int minHeight)
	{
		this.contentWidth = minWidth;
		this.contentHeight = minHeight;
		this.isInitialized = false;
	}

	/**
	 * <code>Spacers</code> are restricted from having
	 * <code>Commands</code>, so this method will always
	 * throw <code>IllegalStateException</code> whenever it is called.
	 * 
	 * @param cmd - the Command
	 * @throws IllegalStateException - always
	 * @see Item#addCommand(Command) in class Item
	 */
	public void addCommand( Command cmd)
	{
		throw new IllegalStateException("Spacer cannot have a command.");
	}

	/**
	 * Spacers are restricted from having <code>Commands</code>,
	 * so this method will always
	 * throw <code>IllegalStateException</code> whenever it is called.
	 * 
	 * @param cmd - the Command
	 * @throws IllegalStateException - always
	 * @see Item#setDefaultCommand(Command) in class Item
	 */
	public void setDefaultCommand( Command cmd)
	{
		throw new IllegalStateException("Spacer cannot have a command.");
	}

	/**
	 * <code>Spacers</code> are restricted to having
	 * <code>null</code> labels, so this method will
	 * always throw
	 * <code>IllegalStateException</code> whenever it is called.
	 * 
	 * @param label - the label string
	 * @throws IllegalStateException - always
	 * @see Item#setLabel(String) in class Item
	 */
	public void setLabel( String label)
	{
		throw new IllegalStateException("Spacer cannot have a label.");
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#paint(int, int, javax.microedition.lcdui.Graphics)
	 */
	public void paintContent(int x, int y, int leftBorder, int rightBorder, Graphics g) {
		// nothing to paint
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#initItem()
	 */
	protected void initContent(int firstLineWidth, int availWidth, int availHeight) {
		// nothing to do. Content width and height are set in 
		// the setMinimumSize(int, int) method. 
	}

	//#ifdef polish.useDynamicStyles
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#getCssSelector()
	 */
	protected String createCssSelector() {
		return "spacer";
	}
	//#endif
}
