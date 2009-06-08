//#condition polish.usePolishGui
/*
 * Created on Dec 8, 2008 at 7:38:29 AM.
 * 
 * Copyright (c) 2009 Robert Virkus / Enough Software
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
package de.enough.polish.ui.containerviews;

import de.enough.polish.ui.ContainerView;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Dimension;

/**
 * <p>Aligns elements according to the MIDP 2.0 layout directives of the items.</p>
 * <p>Usage:
 * </p>
 * <pre>
 * .myForm {
 * 		view-type: midp2;
 * }
 * </pre>
 * <p>Copyright Enough Software 2008</p>
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class Midp2ContainerView extends ContainerView
{
	
	Dimension contentX;

	/**
	 * Creates a new view type
	 */
	public Midp2ContainerView()
	{
		// use style settings for configuration
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#initContent(de.enough.polish.ui.Item, int, int, int)
	 */
	protected void initContent(Item parentContainerItem, int firstLineWidth, int availWidth, int availHeight)
	{
		super.initContent(parentContainerItem, firstLineWidth, availWidth, availHeight);
		// now just adjust positions, so that the elements are layouted according to their settings:
		Item[] items = this.parentContainer.getItems();
		int x = 0;
		int y = 0;
		int currentRowHeight = 0;
		int currentRowStartIndex = 0;
		int maxRowWidth = 0;
		for (int i = 0; i < items.length; i++)
		{
			Item item = items[i];
			int lo = item.getLayout();
			if (((lo & Item.LAYOUT_NEWLINE_BEFORE) == Item.LAYOUT_NEWLINE_BEFORE) || (x + item.getContentWidth() > availWidth) ) 
			{
				if (currentRowHeight != 0) {
					lineBreak( items, currentRowStartIndex, i - 1, x, currentRowHeight, availWidth );
					y += currentRowHeight + this.paddingVertical;
					currentRowHeight = 0;
				}
				if (x > maxRowWidth) {
					maxRowWidth = x;
				}
				x = 0;
				currentRowStartIndex = i;
			}
			item.relativeX = x;
			item.relativeY = y;
			if (item.itemWidth > availWidth) {
				// item has probably expand layout, as the content width fits within this row:
				item.getItemWidth( availWidth - x, availWidth - x, availHeight );
			}
			x += item.itemWidth;
			if (item.itemHeight > currentRowHeight) {
				currentRowHeight = item.itemHeight;
			}
			if (x >= availWidth || ((lo & Item.LAYOUT_NEWLINE_AFTER) == Item.LAYOUT_NEWLINE_AFTER) || (i == items.length -1)) {
				if (currentRowHeight != 0) {
					lineBreak( items, currentRowStartIndex, i, x, currentRowHeight, availWidth);
					y += currentRowHeight + this.paddingVertical;
					currentRowHeight = 0;
				}
				if (x > maxRowWidth) {
					maxRowWidth = x;
				}
				x = 0;
				currentRowStartIndex = i + 1;
			}
		}
		this.contentHeight = y;
		this.contentWidth = maxRowWidth;
	}

	/**
	 * Adds a linebreak to the current list of items.
	 */
	private void lineBreak(Item[] items, int currentRowStartIndex, int currentRowEndIndex, int currentRowWidth, int currentRowHeight, int availWidth)
	{
		int diff = 0;
		if (this.isLayoutCenter) {
			diff = (availWidth - currentRowWidth) / 2;
		} else if (this.isLayoutRight) {
			diff = (availWidth - currentRowWidth);
		}
		for (int i=currentRowStartIndex; i <= currentRowEndIndex; i++) {
			Item item = items[i];
			int lo = item.getLayout();
			if ((lo & Item.LAYOUT_VCENTER) == Item.LAYOUT_VCENTER ) {
				item.relativeY += (currentRowHeight - item.itemHeight) / 2;
			} else if ((lo & Item.LAYOUT_BOTTOM) == Item.LAYOUT_BOTTOM ) {
				item.relativeY += (currentRowHeight - item.itemHeight);
			}
			item.relativeX += diff;
			if (i == currentRowEndIndex) {
				if  ((lo & Item.LAYOUT_RIGHT) == Item.LAYOUT_RIGHT)
				{
					item.relativeX = availWidth - item.itemWidth;
				}
			}
		}
	}

}
