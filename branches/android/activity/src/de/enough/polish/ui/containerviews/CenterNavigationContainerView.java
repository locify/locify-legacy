//#condition polish.usePolishGui && polish.midp2

package de.enough.polish.ui.containerviews;

import java.io.IOException;

import de.enough.polish.android.lcdui.Graphics;
import de.enough.polish.android.lcdui.Image;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.ContainerView;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.DrawUtil;


/**
 * <p>Shows  the available items of a Container in a horizontal list.</p>
 * <p>Apply this view by specifying "view-type: horizontal;" in your polish.css file.</p>
 *
 * <p>Copyright Enough Software 2007 - 2009</p>
 * @author Andre Schmidt
 */
public class CenterNavigationContainerView extends ContainerView {
	transient static ImageItem leftItem;
	transient static ImageItem rightItem;
	
	int grayOffset = 0;
	
	static
	{
		try {
			//#style leftArrow?
			leftItem = new ImageItem(null,Image.createImage("/arrow_left.png"),Item.LAYOUT_LEFT,null);
			
			//#style rightArrow?
			rightItem = new ImageItem(null,Image.createImage("/arrow_right.png"),Item.LAYOUT_RIGHT,null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private transient int[][] inactiveIcons = null;
	
	/**
	 * Creates a new view
	 */
	public CenterNavigationContainerView() {
		super();
		this.allowsAutoTraversal = false;
		this.isHorizontal = true;
		this.isVertical = false;
		
	}
	
	
	protected void setStyle(Style style) {
		super.setStyle(style);
		
		Integer grayOffsetObj= style.getIntProperty(347);
		if (grayOffsetObj != null) {
			this.grayOffset = grayOffsetObj.intValue();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#initContent(de.enough.polish.ui.Container, int, int)
	 */
	protected void initContent(Item parentItm, int firstLineWidth,
			int availWidth, int availHeight) 
	{
		int height = 0;
		Item[] items = this.parentContainer.getItems();
		this.inactiveIcons = new int[items.length][];
		
		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			
			int itemHeight = item.getItemHeight(availWidth, availWidth, availHeight);
			int itemWidth = item.itemWidth;
			
			if (itemWidth == 0)  {
				this.inactiveIcons[i] = new int[0];
				continue;
			}
			
			if (itemHeight > height ) {
				height = itemHeight;
			}
			
			if(item.isFocused)
			{
				//set to normal style
				item.setStyle(this.originalStyle);
			}
			
			int rgbData[] = UiAccess.getRgbData(item);
			
			if(item.isFocused)
			{
				item.setStyle(item.getFocusedStyle());
			}
			
			convertToGrayScale(rgbData,this.grayOffset);
			this.inactiveIcons[i] = rgbData;
		}
		
		this.contentHeight = height;
		this.contentWidth = availWidth;
	}
	
	boolean animateItems = false;
	Style originalStyle;

	public Style focusItem(int index, Item item, int direction, Style focusedStyle) {
		this.originalStyle = super.focusItem(index, item, direction, focusedStyle); 
		return this.originalStyle;
	}
	
	
	protected void convertToGrayScale(int[] rgbData, int grayOffset)
	{
		int color,red,green,blue,alpha;
		for(int i = 0;i < rgbData.length;i++){
			color = rgbData[i];			
			
			alpha = (0xFF000000 & color);
			red = (0x00FF & (color >>> 16));	
			green = (0x0000FF & (color >>> 8));
			blue = color & (0x000000FF );
			
			int brightness = ((red + green + blue) / 3 ) & 0x000000FF;
            brightness += grayOffset;
            if (brightness>255)
            {
                  brightness = 255;
            }
            color = (brightness << 0)
                  |   (brightness << 8)
                  |   (brightness << 16);
            color |= alpha;
            rgbData[i] = color;
		}
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#paintContent(de.enough.polish.ui.Container, de.enough.polish.ui.Item[], int, int, int, int, int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	protected void paintContent(Container container, Item[] myItems, int x, int y, int leftBorder, int rightBorder, int clipX, int clipY, int clipWidth, int clipHeight, Graphics g) {
		int originalLeftBorder = leftBorder;
		int originalRightBorder = rightBorder;
		
		int width = rightBorder - leftBorder;
		int availHeight = this.availableHeight;
		int leftWidth = leftItem.getItemWidth(width, width, availHeight);
		int rightWidth = rightItem.getItemWidth(width, width, availHeight);
		
		leftBorder += leftWidth;
		rightBorder -= rightWidth;
		
		int center = width/2;
		
		int itemWidth = myItems[0].getItemWidth(width, width, availHeight);
		
		int offset = (center - (this.focusedIndex * itemWidth)) - (itemWidth / 2);  
		
		for(int i = 0; i < myItems.length; i++)
		{
			Item item = myItems[i];
			
			int xOffset = x + offset;
			
			if(xOffset > leftBorder && (xOffset + itemWidth) < rightBorder)
			{
				if(item == this.focusedItem)
				{
					item.paint(xOffset, y, leftBorder, rightBorder, g);
				}
				else
				{
					int[] rgbData = this.inactiveIcons[i];
					DrawUtil.drawRgb( rgbData, xOffset, y, item.itemWidth, item.itemHeight, true, g );
				}
			}
			
			offset += itemWidth;
		}
		
		leftBorder = originalLeftBorder;
		rightBorder = originalRightBorder;
		
		if(myItems.length != 0)
		{
			if(this.focusedIndex != 0)
			{
				leftItem.paint(leftBorder, y, leftBorder, rightBorder, g);
			}
			
			if(this.focusedIndex != (myItems.length - 1))
			{
				rightItem.paint(rightBorder - rightWidth, y, leftBorder, rightBorder, g);	
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#isValid(de.enough.polish.ui.Item, de.enough.polish.ui.Style)
	 */
	protected boolean isValid(Item parent, Style style)
	{
		//#if polish.midp1
			//# return false;
		//#else
			return super.isValid(parent, style);
		//#endif
	}	
	
	
}


