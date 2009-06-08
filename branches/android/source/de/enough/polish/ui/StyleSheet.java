//#condition polish.usePolishGui
/*
 * Created on 05-Jan-2004 at 20:41:52.
 *
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;

import de.enough.polish.android.lcdui.Font;
import de.enough.polish.android.lcdui.Graphics;
import de.enough.polish.android.lcdui.Image;
import de.enough.polish.android.midlet.MIDlet;

import de.enough.polish.ui.tasks.ImageTask;
import de.enough.polish.util.Locale;

/**
 * <p>Manages all defined styles of a specific project.</p>
 * <p>This class is actually pre-processed to get the styles specific for the project and the device.</p>
 *
 * @author Robert Virkus, robert@enough.de
 * <pre>
 * history
 *        05-Jan-2004 - rob creation
 * </pre>
 */
public final class StyleSheet {
	
	protected static Hashtable imagesByName;
	//#ifdef polish.images.backgroundLoad
		//# private static Hashtable scheduledImagesByName;
		//# //private static final Boolean TRUE = new Boolean( true );
		//# private static Timer timer;
	//#endif
	//#ifdef polish.LibraryBuild
		//# /** default style */
		//# public static Style defaultStyle = new Style( 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0x000000, Font.getDefaultFont(), null, null, null, null );
		//# /** default style for focused/hovered items */
		//# public static Style focusedStyle = new Style( 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0xFF0000, Font.getDefaultFont(), null, null, null, null );
		//# /** default style for labels */
		//# public static Style labelStyle = new Style( 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, Item.LAYOUT_NEWLINE_AFTER, 0x000000, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL ), null, null, null, null );
		//# /** default style for the commands menu */
		//# public static Style menuStyle = new Style( 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, Item.LAYOUT_NEWLINE_AFTER, 0x000000, Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL ), null, null, null, null );
		//# private static Hashtable stylesByName = new Hashtable();
	//#endif


		private StyleSheet() {
			// disallow instantion
			//#if false
				//# // use Graphics so that the import is being kept:
				//# System.out.println(Graphics.LEFT);
			//#endif
		}

	
	
	// do not change the following line!
//$$IncludeStyleSheetDefinitionHere$$//
	public final static Background gradient2Background = new com.locify.client.gui.polish.TopBarBackground(0xffe2c4, 0xffca94, 0, 0, 100, true);
	public final static Background gradientBackground = 
		new de.enough.polish.ui.backgrounds.GradientVerticalBackground(0xffe2c4, 0xffca94,Graphics.SOLID,0,100,true);
	public final static Border originalBottomBorderBorder = 
		new de.enough.polish.ui.borders.BottomBorder( 0xff8000, 1);
	public final static Border topBorderBorder = 
		new de.enough.polish.ui.borders.TopBorder( 0xff8000, 1);
	public final static Border bottomBorderBorder = new com.locify.client.gui.polish.FixedBottomBorder(0xff8000, 1);
	public final static Border inputBorderBorder = 
		new de.enough.polish.ui.borders.DropShadowBorder( 0xffff8000, 0x10ff8000, 1, 1, de.enough.polish.ui.borders.DropShadowBorder.ALL);
	public final static Border mainScreenListBorderBorder = 
		new de.enough.polish.ui.borders.RoundRectBorder( 0x78e239,2, 8, 8);
	public final static Border infoBorderBorder = 
		new de.enough.polish.ui.borders.DropShadowBorder( 0xffa9a9a9, 0x10000000, 2, 1, de.enough.polish.ui.borders.DropShadowBorder.ALL);
	public final static Border focusedInputBorderBorder = 
		new de.enough.polish.ui.borders.DropShadowBorder( 0xff78e239, 0x1078e239, 1, 1, de.enough.polish.ui.borders.DropShadowBorder.ALL);
	public final static Background defaultBackground = null;
	public final static Border defaultBorder = null;
	public static Style defaultStyle;
	//static and referenced styles:
	public static Style contextimgStyle;
	public static Style menu1Style;
	public static Style contexttextStyle;
	public static Style inactivetabStyle;
	public static Style menubarStyle;
	public static Style browsertextitalicStyle;
	public static Style infoStyle;
	public static Style helpiconsStyle;
	public static Style leftcommandStyle;
	public static Style browseroptionitemStyle;
	public static Style fileuploadtextStyle;
	public static Style mainscreenStyle;
	public static Style activetabStyle;
	public static Style menuStyle;
	public static Style browsertextStyle;
	public static Style browserchoicegroupexclusiveStyle;
	public static Style browserradioStyle;
	public static Style browsertextcodeStyle;
	public static Style browserchoicegroupmultipleStyle;
	public static Style browsertextbolditalicStyle;
	public static Style browsertextboldStyle;
	public static Style titleStyle;
	public static Style browseroptionStyle;
	public static Style scrollbarStyle;
	public static Style browserStyle;
	public static Style rssdescriptionalertStyle;
	public static Style tabbarStyle;
	public static Style breaklineStyle;
	public static Style mainscreenlistStyle;
	public static Style contextlabelStyle;
	public static Style rightcommandStyle;
	public static Style browsercheckboxStyle;
	public static Style labelStyle;
	public static Style listStyle;
	public static Style formStyle;
	public static Style choicegroupStyle;
	public static Style pStyle;
	public static Style gaugeStyle;
	public static Style afocusedStyle;
	public static Style imgmorefocusedStyle;
	public static Style imgaddfromlinkfocusedStyle;
	public static Style imgmapfocusedStyle;
	public static Style contextcontainerfocusedStyle;
	public static Style imginfofocusedStyle;
	public static Style contextbuttonfocusedStyle;
	public static Style focusedmenuitemStyle;
	public static Style buttonfocusedStyle;
	public static Style imgaddfocusedStyle;
	public static Style contactstextfieldfocusedStyle;
	public static Style imgsavedfocusedStyle;
	public static Style radioboxfocusedStyle;
	public static Style imgnavigatefocusedStyle;
	public static Style listitemfocusedStyle;
	public static Style browserinputfocStyle;
	public static Style imgrenamefocusedStyle;
	public static Style imgmanagefocusedStyle;
	public static Style imgloggerfocusedStyle;
	public static Style imgonlinemapfocusedStyle;
	public static Style imgselectfocusedStyle;
	public static Style browserlinkfocStyle;
	public static Style imgexitfocusedStyle;
	public static Style imgaddfromlistfocusedStyle;
	public static Style imgdeletefocusedStyle;
	public static Style imgservicesettingsfocusedStyle;
	public static Style imgmovefocusedStyle;
	public static Style imgwherefocusedStyle;
	public static Style selectfocusedStyle;
	public static Style mainscreenlistitemfocusedStyle;
	public static Style imgupdateservicefocusedStyle;
	public static Style imghomefocusedStyle;
	public static Style checkboxfocusedStyle;
	public static Style imggpsfocusedStyle;
	public static Style textfieldfocusedStyle;
	public static Style browserchoicegrouppopupfocusedStyle;
	public static Style alerttitleStyle;
	public static Style imghomeStyle;
	public static Style imggpsStyle;
	public static Style imgdeleteStyle;
	public static Style imgselectStyle;
	public static Style browserchoicegrouppopupStyle;
	public static Style buttonStyle;
	public static Style imgupdateserviceStyle;
	public static Style imgmanageStyle;
	public static Style alertformStyle;
	public static Style mainscreenlistitemStyle;
	public static Style imgaddfromlistStyle;
	public static Style selectStyle;
	public static Style contextbuttonStyle;
	public static Style menuitemStyle;
	public static Style imgmapStyle;
	public static Style imgsavedStyle;
	public static Style imgexitStyle;
	public static Style contactstextfieldStyle;
	public static Style contextcontainerStyle;
	public static Style imgrenameStyle;
	public static Style imgmoveStyle;
	public static Style imgloggerStyle;
	public static Style imgaddfromlinkStyle;
	public static Style imgservicesettingsStyle;
	public static Style aStyle;
	public static Style browserinputStyle;
	public static Style browserlinkStyle;
	public static Style imgwhereStyle;
	public static Style textfieldStyle;
	public static Style imgaddStyle;
	public static Style imgonlinemapStyle;
	public static Style imginfoStyle;
	public static Style imgnavigateStyle;
	public static Style imgmoreStyle;
	public static Style listitemStyle;
	public static Style radioboxStyle;
	public static Style checkboxStyle;
	static final Hashtable stylesByName = new Hashtable(113);
static { // init styles:
	initStyles0();
	initStyles1();
}
private static final void initStyles0(){
	defaultStyle = new Style (
		"default", 
		Item.LAYOUT_DEFAULT,	// default layout
		defaultBackground, 
		defaultBorder, 
		new short[]{ },
		new Object[]{ }
	);
	contextimgStyle = new Style (
		"contextimg", 
		Item.LAYOUT_VCENTER,
		null,	// no background
		null, 	// no border
		new short[]{ 32712},
		new Object[]{ new Dimension(3, false)}
	);
	menu1Style = new Style (
		"menu1", 
		Item.LAYOUT_RIGHT,
		new de.enough.polish.ui.backgrounds.BorderedRoundRectBackground( 0xffeedd,8, 8, 0xff8000, 2),
		null, 	// no border
		new short[]{ 59, 32712, -6, 85},
		new Object[]{ new Dimension(140, false), new Dimension(0, false), new Dimension(0, false), new Integer(0)}
	);
	contexttextStyle = new Style (
		"contexttext", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 59, -15},
		new Object[]{ new Dimension(65, false), new Integer( Font.SIZE_SMALL )}
	);
	inactivetabStyle = new Style (
		"inactivetab", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.RoundTabBackground( 0xffeedd,8, 8),
		null, 	// no border
		new short[]{ -14, -17, -5, -2, -3, -6},
		new Object[]{ new Integer(Font.STYLE_BOLD), new Color( 0x808080, false), new Dimension(1, false), new Dimension(2, false), new Dimension(2, false), new Dimension(3, false)}
	);
	menubarStyle = new Style (
		"menubar", 
		Item.LAYOUT_DEFAULT,	// default layout
		gradientBackground, 
		topBorderBorder, 
		new short[]{ 32712, -10, -7, -8, -9},
		new Object[]{ new Dimension(0, false), new Dimension(3, false), new Dimension(6, false), new Dimension(6, false), new Dimension(1, false)}
	);
	browsertextitalicStyle = new Style (
		"browsertextitalic", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209, -14},
		new Object[]{ Style.FALSE, new Integer(Font.STYLE_ITALIC)}
	);
	infoStyle = new Style (
		"info", 
		Item.LAYOUT_RIGHT,
		null,	// background:none was specified
		new de.enough.polish.ui.borders.SimpleBorder( 0x000000, 1),
		new short[]{ -8, -4, -16, -14, -17, -15},
		new Object[]{ new Dimension(5, false), new Dimension(1, false), new Integer( Font.FACE_PROPORTIONAL ), new Integer(Font.STYLE_PLAIN), new Color( 0x000000, false), new Integer( Font.SIZE_SMALL )}
	);
	helpiconsStyle = new Style (
		"helpicons", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xffeedd, false)),
		null, 	// no border
		new short[]{ 39, 85},
		new Object[]{ new de.enough.polish.ui.containerviews.Midp2ContainerView(), new Integer(0)}
	);
	leftcommandStyle = new Style (
		"leftcommand", 
		Item.LAYOUT_LEFT,
		null,	// no background
		null, 	// no border
		new short[]{ -2, -3, -10, -9, -14, -17},
		new Object[]{ new Dimension(10, false), new Dimension(10, false), new Dimension(0, false), new Dimension(3, false), new Integer(Font.STYLE_BOLD), new Color( 0x000000, false)}
	);
	browseroptionitemStyle = new Style (
		"browseroptionitem", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	fileuploadtextStyle = new Style (
		"fileuploadtext", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 59, -15},
		new Object[]{ new Dimension(85, false), new Integer( Font.SIZE_SMALL )}
	);
	mainscreenStyle = new Style (
		"mainscreen", 
		Item.LAYOUT_CENTER|Item.LAYOUT_TOP,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xffeedd, false)),
		null, 	// no border
		new short[]{ -6, 85},
		new Object[]{ new Dimension(0, false), new Integer(0)}
	);
	activetabStyle = new Style (
		"activetab", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.RoundTabBackground( 0x78e239,8, 8),
		null, 	// no border
		new short[]{ -6, -14, -17},
		new Object[]{ new Dimension(3, false), new Integer(Font.STYLE_BOLD), new Color( 0x000000, false)}
	);
	menuStyle = new Style (
		"menu", 
		Item.LAYOUT_RIGHT,
		new de.enough.polish.ui.backgrounds.BorderedRoundRectBackground( 0xffeedd,8, 8, 0xff8000, 2),
		null, 	// no border
		new short[]{ 32712, -6, 59, 85},
		new Object[]{ new Dimension(0, false), new Dimension(0, false), new Dimension(140, false), new Integer(0)}
	);
	browsertextStyle = new Style (
		"browsertext", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		null, null	// no additional attributes have been defined
	);
	browserchoicegroupexclusiveStyle = new Style (
		"browserchoicegroupexclusive", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	browserradioStyle = new Style (
		"browserradio", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	browsertextcodeStyle = new Style (
		"browsertextcode", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209, -16},
		new Object[]{ Style.FALSE, new Integer( Font.FACE_MONOSPACE )}
	);
	browserchoicegroupmultipleStyle = new Style (
		"browserchoicegroupmultiple", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	browsertextbolditalicStyle = new Style (
		"browsertextbolditalic", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209, -14},
		new Object[]{ Style.FALSE, new Integer(Font.STYLE_BOLD|Font.STYLE_ITALIC)}
	);
	browsertextboldStyle = new Style (
		"browsertextbold", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209, -14, -17},
		new Object[]{ Style.FALSE, new Integer(Font.STYLE_BOLD), new Color( 0x000000, false)}
	);
	titleStyle = new Style (
		"title", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		gradient2Background, 
		bottomBorderBorder, 
		new short[]{ 32712, -7, -6, -8, -16, -14, 150},
		new Object[]{ new Dimension(0, false), new Dimension(20, false), new Dimension(3, false), new Dimension(66, false), new Integer( Font.FACE_PROPORTIONAL ), new Integer(Font.STYLE_BOLD), Style.FALSE}
	);
	browseroptionStyle = new Style (
		"browseroption", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	scrollbarStyle = new Style (
		"scrollbar", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 119, 120},
		new Object[]{ new Integer(3), new Color( 0x333333, false)}
	);
	browserStyle = new Style (
		"browser", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 39, -6, 32712},
		new Object[]{ new de.enough.polish.ui.containerviews.Midp2ContainerView(), new Dimension(0, false), new Dimension(0, false)}
	);
	rssdescriptionalertStyle = new Style (
		"rssdescriptionalert", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	tabbarStyle = new Style (
		"tabbar", 
		Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		null, 	// no border
		new short[]{ -10, 32712, 155, 158, 156},
		new Object[]{ new Dimension(0, false), new Dimension(0, false), "/left.png", Style.TRUE, "/right.png"}
	);
	breaklineStyle = new Style (
		"breakline", 
		Item.LAYOUT_NEWLINE_AFTER,
		null,	// no background
		null, 	// no border
		null, null	// no additional attributes have been defined
	);
	mainscreenlistStyle = new Style (
		"mainscreenlist", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xffeedd, false)),
		mainScreenListBorderBorder, 
		new short[]{ 32712, -6, 85},
		new Object[]{ new Dimension(0, false), new Dimension(0, false), new Integer(0)}
	);
	contextlabelStyle = new Style (
		"contextlabel", 
		Item.LAYOUT_LEFT|Item.LAYOUT_NEWLINE_AFTER,
		null,	// no background
		null, 	// no border
		new short[]{ -14},
		new Object[]{ new Integer(Font.STYLE_BOLD)}
	);
	rightcommandStyle = new Style (
		"rightcommand", 
		Item.LAYOUT_RIGHT,
		null,	// no background
		null, 	// no border
		new short[]{ -2, -3, -10, -9, -14, -17},
		new Object[]{ new Dimension(10, false), new Dimension(10, false), new Dimension(0, false), new Dimension(3, false), new Integer(Font.STYLE_BOLD), new Color( 0x000000, false)}
	);
	browsercheckboxStyle = new Style (
		"browsercheckbox", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	labelStyle = new Style (
		"label", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ -14},
		new Object[]{ new Integer(Font.STYLE_BOLD)}
	);
	listStyle = new Style (
		"list", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xffeedd, false)),
		null, 	// no border
		new short[]{ 32712, -6, 85},
		new Object[]{ new Dimension(0, false), new Dimension(0, false), new Integer(0)}
	);
	formStyle = new Style (
		"form", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xffeedd, false)),
		null, 	// no border
		new short[]{ 85},
		new Object[]{ new Integer(0)}
	);
	choicegroupStyle = new Style (
		"choicegroup", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null,	// border:none was specified
		new short[]{ 32712, -6},
		new Object[]{ new Dimension(0, false), new Dimension(0, false)}
	);
	pStyle = new Style (
		"p", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		null, null	// no additional attributes have been defined
	);
	gaugeStyle = new Style (
		"gauge", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 25},
		new Object[]{ Style.FALSE}
	);
	afocusedStyle = new Style (
		"afocused", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ -14, -17},
		new Object[]{ new Integer(Font.STYLE_BOLD), new Color( 0xFF0000, false)}
	);
	imgmorefocusedStyle = new Style (
		"imgmorefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/more.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgaddfromlinkfocusedStyle = new Style (
		"imgaddfromlinkfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/addFromLink.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgmapfocusedStyle = new Style (
		"imgmapfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/maps.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	contextcontainerfocusedStyle = new Style (
		"contextcontainerfocused", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		focusedInputBorderBorder, 
		new short[]{ 4, 32712, 85, 58},
		new Object[]{ new Integer(3), new Dimension(2, false), new Integer(0), new Dimension(60, false)}
	);
	imginfofocusedStyle = new Style (
		"imginfofocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/moreInfo.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	contextbuttonfocusedStyle = new Style (
		"contextbuttonfocused", 
		Item.LAYOUT_VCENTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		inputBorderBorder, 
		new short[]{ -14, -17, -6, 32712, 58, 85},
		new Object[]{ new Integer(Font.STYLE_BOLD), new Color( 0xFFFFFF, false), new Dimension(3, false), new Dimension(3, false), new Dimension(0, false), new Integer(0)}
	);
	focusedmenuitemStyle = new Style (
		"focusedmenuitem", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	buttonfocusedStyle = new Style (
		"buttonfocused", 
		Item.LAYOUT_NEWLINE_BEFORE|Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		inputBorderBorder, 
		new short[]{ -14, -17, -6, 58, 32712, 85},
		new Object[]{ new Integer(Font.STYLE_BOLD), new Color( 0xFFFFFF, false), new Dimension(3, false), new Dimension(0, false), new Dimension(5, false), new Integer(0)}
	);
	imgaddfocusedStyle = new Style (
		"imgaddfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/add.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	contactstextfieldfocusedStyle = new Style (
		"contactstextfieldfocused", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		focusedInputBorderBorder, 
		new short[]{ 32712, 85, 58},
		new Object[]{ new Dimension(5, false), new Integer(0), new Dimension(60, false)}
	);
	imgsavedfocusedStyle = new Style (
		"imgsavedfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/saved.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	radioboxfocusedStyle = new Style (
		"radioboxfocused", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		null, 	// no border
		new short[]{ 32712, -7, 11, 12},
		new Object[]{ new Dimension(0, false), new Dimension(2, false), "/radio-selected.png", "/radio.png"}
	);
	imgnavigatefocusedStyle = new Style (
		"imgnavigatefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/navigation.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	listitemfocusedStyle = new Style (
		"listitemfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		originalBottomBorderBorder, 
		new short[]{ 32712, -6, 11, 12},
		new Object[]{ new Dimension(0, false), new Dimension(2, false), "none", "none"}
	);
	browserinputfocStyle = new Style (
		"browserinputfoc", 
		Item.LAYOUT_EXPAND|Item.LAYOUT_LEFT,
		null,	// no background
		new de.enough.polish.ui.borders.RoundRectBorder( 0x142850,2, 10, 10),
		new short[]{ -6, 209},
		new Object[]{ new Dimension(1, false), Style.FALSE}
	);
	imgrenamefocusedStyle = new Style (
		"imgrenamefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/renameService.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgmanagefocusedStyle = new Style (
		"imgmanagefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/manage.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgloggerfocusedStyle = new Style (
		"imgloggerfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/logger.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgonlinemapfocusedStyle = new Style (
		"imgonlinemapfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/online.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgselectfocusedStyle = new Style (
		"imgselectfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/select.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	browserlinkfocStyle = new Style (
		"browserlinkfoc", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209, -10, -8, -14, -17},
		new Object[]{ Style.FALSE, new Dimension(1, false), new Dimension(1, false), new Integer(Font.STYLE_BOLD), new Color( 0xFF0000, false)}
	);
	imgexitfocusedStyle = new Style (
		"imgexitfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/exit.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgaddfromlistfocusedStyle = new Style (
		"imgaddfromlistfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/addFromList.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgdeletefocusedStyle = new Style (
		"imgdeletefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/delete.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgservicesettingsfocusedStyle = new Style (
		"imgservicesettingsfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/serviceSettings.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgmovefocusedStyle = new Style (
		"imgmovefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/move.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgwherefocusedStyle = new Style (
		"imgwherefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/where.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	selectfocusedStyle = new Style (
		"selectfocused", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		focusedInputBorderBorder, 
		new short[]{ 58, 59, 13, 310, 85},
		new Object[]{ new Dimension(100, false), new Dimension(124, false), "/popup.png", Style.TRUE, new Integer(0)}
	);
	mainscreenlistitemfocusedStyle = new Style (
		"mainscreenlistitemfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		null,	// border:none was specified
		new short[]{ 32712, -6, 11, 12},
		new Object[]{ new Dimension(0, false), new Dimension(2, false), "none", "none"}
	);
	imgupdateservicefocusedStyle = new Style (
		"imgupdateservicefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/updateService.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imghomefocusedStyle = new Style (
		"imghomefocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/home.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	checkboxfocusedStyle = new Style (
		"checkboxfocused", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		null, 	// no border
		new short[]{ 32712, -7, 9, 10},
		new Object[]{ new Dimension(0, false), new Dimension(2, false), "/checkbox-checked.png", "/checkbox.png"}
	);
	imggpsfocusedStyle = new Style (
		"imggpsfocused", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0x78e239, false)),
		bottomBorderBorder, 
		new short[]{ 190, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/gps.png", new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	textfieldfocusedStyle = new Style (
		"textfieldfocused", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		focusedInputBorderBorder, 
		new short[]{ 85, 58},
		new Object[]{ new Integer(0), new Dimension(60, false)}
	);
	browserchoicegrouppopupfocusedStyle = new Style (
		"browserchoicegrouppopupfocused", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xC0C0C0, false)),
		null, 	// no border
		new short[]{ 209},
		new Object[]{ Style.FALSE}
	);
	alerttitleStyle = new Style (
		"alerttitle", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		gradientBackground, 
		bottomBorderBorder, 
		new short[]{ 32712, -7, -6, -16, -14},
		new Object[]{ new Dimension(0, false), new Dimension(20, false), new Dimension(3, false), new Integer( Font.FACE_PROPORTIONAL ), new Integer(Font.STYLE_BOLD)}
	);
	imghomeStyle = new Style (
		"imghome", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/home.png", imghomefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imggpsStyle = new Style (
		"imggps", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/gps.png", imggpsfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgdeleteStyle = new Style (
		"imgdelete", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/delete.png", imgdeletefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgselectStyle = new Style (
		"imgselect", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/select.png", imgselectfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	browserchoicegrouppopupStyle = new Style (
		"browserchoicegrouppopup", 
		Item.LAYOUT_DEFAULT,	// default layout
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		null, 	// no border
		new short[]{ 209, 1},
		new Object[]{ Style.FALSE, browserchoicegrouppopupfocusedStyle}
	);
	buttonStyle = new Style (
		"button", 
		Item.LAYOUT_NEWLINE_BEFORE|Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xA9A9A9, false)),
		inputBorderBorder, 
		new short[]{ -14, -17, -6, 58, 32712, 1, 85},
		new Object[]{ new Integer(Font.STYLE_BOLD), new Color( 0xFFFFFF, false), new Dimension(3, false), new Dimension(0, false), new Dimension(5, false), buttonfocusedStyle, new Integer(0)}
	);
	imgupdateserviceStyle = new Style (
		"imgupdateservice", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/updateService.png", imgupdateservicefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgmanageStyle = new Style (
		"imgmanage", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/manage.png", imgmanagefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	alertformStyle = new Style (
		"alertform", 
		Item.LAYOUT_CENTER|Item.LAYOUT_BOTTOM|Item.LAYOUT_SHRINK|Item.LAYOUT_VSHRINK,
		null,	// no background
		new de.enough.polish.ui.borders.DropShadowBorder( 0xaaffff00, 0x22000022, 6, 1, de.enough.polish.ui.borders.DropShadowBorder.TOP_RIGHT),
		new short[]{ -2, -3, -10, -6, -9, 116, 62, 2},
		new Object[]{ new Dimension(10, false), new Dimension(10, false), new Dimension(0, false), new Dimension(5, false), new Dimension(15, false), Style.TRUE, new de.enough.polish.ui.screenanimations.FadeScreenChangeAnimation(), alerttitleStyle}
	);
	mainscreenlistitemStyle = new Style (
		"mainscreenlistitem", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		null,	// border:none was specified
		new short[]{ 1, 32712, -6, 11, 12},
		new Object[]{ mainscreenlistitemfocusedStyle, new Dimension(0, false), new Dimension(2, false), "none", "none"}
	);
	imgaddfromlistStyle = new Style (
		"imgaddfromlist", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/addFromList.png", imgaddfromlistfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	selectStyle = new Style (
		"select", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		inputBorderBorder, 
		new short[]{ 58, 59, 13, 310, 1, 85},
		new Object[]{ new Dimension(100, false), new Dimension(124, false), "/popup.png", Style.TRUE, selectfocusedStyle, new Integer(0)}
	);
	contextbuttonStyle = new Style (
		"contextbutton", 
		Item.LAYOUT_VCENTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xA9A9A9, false)),
		inputBorderBorder, 
		new short[]{ -14, -17, -6, 32712, 58, 1, 85},
		new Object[]{ new Integer(Font.STYLE_BOLD), new Color( 0xFFFFFF, false), new Dimension(3, false), new Dimension(3, false), new Dimension(0, false), contextbuttonfocusedStyle, new Integer(0)}
	);
	menuitemStyle = new Style (
		"menuitem", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ -17, -15, 1, 32712, -6, 85, 149, 148, 147},
		new Object[]{ new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), focusedmenuitemStyle, new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgmapStyle = new Style (
		"imgmap", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/maps.png", imgmapfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgsavedStyle = new Style (
		"imgsaved", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/saved.png", imgsavedfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgexitStyle = new Style (
		"imgexit", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/exit.png", imgexitfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	contactstextfieldStyle = new Style (
		"contactstextfield", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		inputBorderBorder, 
		new short[]{ 32712, 1, 85, 58},
		new Object[]{ new Dimension(5, false), contactstextfieldfocusedStyle, new Integer(0), new Dimension(60, false)}
	);
	contextcontainerStyle = new Style (
		"contextcontainer", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		inputBorderBorder, 
		new short[]{ 4, 32712, 1, 85, 58},
		new Object[]{ new Integer(3), new Dimension(2, false), contextcontainerfocusedStyle, new Integer(0), new Dimension(60, false)}
	);
	imgrenameStyle = new Style (
		"imgrename", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/renameService.png", imgrenamefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgmoveStyle = new Style (
		"imgmove", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/move.png", imgmovefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgloggerStyle = new Style (
		"imglogger", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/logger.png", imgloggerfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgaddfromlinkStyle = new Style (
		"imgaddfromlink", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/addFromLink.png", imgaddfromlinkfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgservicesettingsStyle = new Style (
		"imgservicesettings", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/serviceSettings.png", imgservicesettingsfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
}
private static final void initStyles1(){
	aStyle = new Style (
		"a", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ -14, -17, 1},
		new Object[]{ new Integer(Font.STYLE_BOLD), new Color( 0x0000FF, false), afocusedStyle}
	);
	browserinputStyle = new Style (
		"browserinput", 
		Item.LAYOUT_EXPAND|Item.LAYOUT_LEFT,
		null,	// no background
		new de.enough.polish.ui.borders.RoundRectBorder( 0x1e5556,1, 10, 10),
		new short[]{ 209, -6, 1},
		new Object[]{ Style.FALSE, new Dimension(2, false), browserinputfocStyle}
	);
	browserlinkStyle = new Style (
		"browserlink", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 209, -10, -8, -14, -17, 1},
		new Object[]{ Style.FALSE, new Dimension(1, false), new Dimension(1, false), new Integer(Font.STYLE_BOLD), new Color( 0x0000FF, false), browserlinkfocStyle}
	);
	imgwhereStyle = new Style (
		"imgwhere", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/where.png", imgwherefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	textfieldStyle = new Style (
		"textfield", 
		Item.LAYOUT_NEWLINE_AFTER,
		new de.enough.polish.ui.backgrounds.SimpleBackground( new Color( 0xFFFFFF, false)),
		inputBorderBorder, 
		new short[]{ 1, 85, 58},
		new Object[]{ textfieldfocusedStyle, new Integer(0), new Dimension(60, false)}
	);
	imgaddStyle = new Style (
		"imgadd", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/add.png", imgaddfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgonlinemapStyle = new Style (
		"imgonlinemap", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/online.png", imgonlinemapfocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imginfoStyle = new Style (
		"imginfo", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/moreInfo.png", imginfofocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgnavigateStyle = new Style (
		"imgnavigate", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/navigation.png", imgnavigatefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	imgmoreStyle = new Style (
		"imgmore", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		bottomBorderBorder, 
		new short[]{ 190, 1, -17, -15, 32712, -6, 85, 149, 148, 147},
		new Object[]{ "/more.png", imgmorefocusedStyle, new Color( 0x000000, false), new Integer( Font.SIZE_MEDIUM ), new Dimension(0, false), new Dimension(3, false), new Integer(0), new Integer(10), new Integer(8), new Color( 0x000000, false)}
	);
	listitemStyle = new Style (
		"listitem", 
		Item.LAYOUT_LEFT|Item.LAYOUT_EXPAND,
		null,	// no background
		originalBottomBorderBorder, 
		new short[]{ 32712, -6, 1, 11, 12},
		new Object[]{ new Dimension(0, false), new Dimension(2, false), listitemfocusedStyle, "none", "none"}
	);
	radioboxStyle = new Style (
		"radiobox", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 32712, -7, 11, 12, 1},
		new Object[]{ new Dimension(0, false), new Dimension(2, false), "/radio-selected.png", "/radio.png", radioboxfocusedStyle}
	);
	checkboxStyle = new Style (
		"checkbox", 
		Item.LAYOUT_DEFAULT,	// default layout
		null,	// no background
		null, 	// no border
		new short[]{ 32712, -7, 9, 10, 1},
		new Object[]{ new Dimension(0, false), new Dimension(2, false), "/checkbox-checked.png", "/checkbox.png", checkboxfocusedStyle}
	);

	//register referenced and dynamic styles:
	stylesByName.put( "contextimg", contextimgStyle );
	stylesByName.put( "menu1", menu1Style );
	stylesByName.put( "contexttext", contexttextStyle );
	stylesByName.put( "inactivetab", inactivetabStyle );
	stylesByName.put( "menubar", menubarStyle );
	stylesByName.put( "browsertextitalic", browsertextitalicStyle );
	stylesByName.put( "info", infoStyle );
	stylesByName.put( "helpicons", helpiconsStyle );
	stylesByName.put( "leftcommand", leftcommandStyle );
	stylesByName.put( "browseroptionitem", browseroptionitemStyle );
	stylesByName.put( "fileuploadtext", fileuploadtextStyle );
	stylesByName.put( "mainscreen", mainscreenStyle );
	stylesByName.put( "activetab", activetabStyle );
	stylesByName.put( "menu", menuStyle );
	stylesByName.put( "browsertext", browsertextStyle );
	stylesByName.put( "browserchoicegroupexclusive", browserchoicegroupexclusiveStyle );
	stylesByName.put( "browserradio", browserradioStyle );
	stylesByName.put( "default", defaultStyle );
	stylesByName.put( "browsertextcode", browsertextcodeStyle );
	stylesByName.put( "browserchoicegroupmultiple", browserchoicegroupmultipleStyle );
	stylesByName.put( "browsertextbolditalic", browsertextbolditalicStyle );
	stylesByName.put( "browsertextbold", browsertextboldStyle );
	stylesByName.put( "title", titleStyle );
	stylesByName.put( "browseroption", browseroptionStyle );
	stylesByName.put( "scrollbar", scrollbarStyle );
	stylesByName.put( "browser", browserStyle );
	stylesByName.put( "rssdescriptionalert", rssdescriptionalertStyle );
	stylesByName.put( "tabbar", tabbarStyle );
	stylesByName.put( "breakline", breaklineStyle );
	stylesByName.put( "mainscreenlist", mainscreenlistStyle );
	stylesByName.put( "contextlabel", contextlabelStyle );
	stylesByName.put( "rightcommand", rightcommandStyle );
	stylesByName.put( "browsercheckbox", browsercheckboxStyle );
	stylesByName.put( "label", labelStyle );
	stylesByName.put( "list", listStyle );
	stylesByName.put( "form", formStyle );
	stylesByName.put( "choicegroup", choicegroupStyle );
	stylesByName.put( "p", pStyle );
	stylesByName.put( "gauge", gaugeStyle );
	stylesByName.put( "afocused", afocusedStyle );
	stylesByName.put( "imgmorefocused", imgmorefocusedStyle );
	stylesByName.put( "imgaddfromlinkfocused", imgaddfromlinkfocusedStyle );
	stylesByName.put( "imgmapfocused", imgmapfocusedStyle );
	stylesByName.put( "contextcontainerfocused", contextcontainerfocusedStyle );
	stylesByName.put( "imginfofocused", imginfofocusedStyle );
	stylesByName.put( "contextbuttonfocused", contextbuttonfocusedStyle );
	stylesByName.put( "focusedmenuitem", focusedmenuitemStyle );
	stylesByName.put( "buttonfocused", buttonfocusedStyle );
	stylesByName.put( "imgaddfocused", imgaddfocusedStyle );
	stylesByName.put( "contactstextfieldfocused", contactstextfieldfocusedStyle );
	stylesByName.put( "imgsavedfocused", imgsavedfocusedStyle );
	stylesByName.put( "radioboxfocused", radioboxfocusedStyle );
	stylesByName.put( "imgnavigatefocused", imgnavigatefocusedStyle );
	stylesByName.put( "listitemfocused", listitemfocusedStyle );
	stylesByName.put( "browserinputfoc", browserinputfocStyle );
	stylesByName.put( "imgrenamefocused", imgrenamefocusedStyle );
	stylesByName.put( "imgmanagefocused", imgmanagefocusedStyle );
	stylesByName.put( "imgloggerfocused", imgloggerfocusedStyle );
	stylesByName.put( "imgonlinemapfocused", imgonlinemapfocusedStyle );
	stylesByName.put( "imgselectfocused", imgselectfocusedStyle );
	stylesByName.put( "browserlinkfoc", browserlinkfocStyle );
	stylesByName.put( "imgexitfocused", imgexitfocusedStyle );
	stylesByName.put( "imgaddfromlistfocused", imgaddfromlistfocusedStyle );
	stylesByName.put( "imgdeletefocused", imgdeletefocusedStyle );
	stylesByName.put( "imgservicesettingsfocused", imgservicesettingsfocusedStyle );
	stylesByName.put( "imgmovefocused", imgmovefocusedStyle );
	stylesByName.put( "imgwherefocused", imgwherefocusedStyle );
	stylesByName.put( "selectfocused", selectfocusedStyle );
	stylesByName.put( "mainscreenlistitemfocused", mainscreenlistitemfocusedStyle );
	stylesByName.put( "imgupdateservicefocused", imgupdateservicefocusedStyle );
	stylesByName.put( "imghomefocused", imghomefocusedStyle );
	stylesByName.put( "checkboxfocused", checkboxfocusedStyle );
	stylesByName.put( "imggpsfocused", imggpsfocusedStyle );
	stylesByName.put( "textfieldfocused", textfieldfocusedStyle );
	stylesByName.put( "browserchoicegrouppopupfocused", browserchoicegrouppopupfocusedStyle );
	stylesByName.put( "alerttitle", alerttitleStyle );
	stylesByName.put( "imghome", imghomeStyle );
	stylesByName.put( "imggps", imggpsStyle );
	stylesByName.put( "imgdelete", imgdeleteStyle );
	stylesByName.put( "imgselect", imgselectStyle );
	stylesByName.put( "browserchoicegrouppopup", browserchoicegrouppopupStyle );
	stylesByName.put( "button", buttonStyle );
	stylesByName.put( "imgupdateservice", imgupdateserviceStyle );
	stylesByName.put( "imgmanage", imgmanageStyle );
	stylesByName.put( "alertform", alertformStyle );
	stylesByName.put( "mainscreenlistitem", mainscreenlistitemStyle );
	stylesByName.put( "imgaddfromlist", imgaddfromlistStyle );
	stylesByName.put( "select", selectStyle );
	stylesByName.put( "contextbutton", contextbuttonStyle );
	stylesByName.put( "menuitem", menuitemStyle );
	stylesByName.put( "imgmap", imgmapStyle );
	stylesByName.put( "imgsaved", imgsavedStyle );
	stylesByName.put( "imgexit", imgexitStyle );
	stylesByName.put( "contactstextfield", contactstextfieldStyle );
	stylesByName.put( "contextcontainer", contextcontainerStyle );
	stylesByName.put( "imgrename", imgrenameStyle );
	stylesByName.put( "imgmove", imgmoveStyle );
	stylesByName.put( "imglogger", imgloggerStyle );
	stylesByName.put( "imgaddfromlink", imgaddfromlinkStyle );
	stylesByName.put( "imgservicesettings", imgservicesettingsStyle );
	stylesByName.put( "a", aStyle );
	stylesByName.put( "browserinput", browserinputStyle );
	stylesByName.put( "browserlink", browserlinkStyle );
	stylesByName.put( "imgwhere", imgwhereStyle );
	stylesByName.put( "textfield", textfieldStyle );
	stylesByName.put( "imgadd", imgaddStyle );
	stylesByName.put( "imgonlinemap", imgonlinemapStyle );
	stylesByName.put( "imginfo", imginfoStyle );
	stylesByName.put( "imgnavigate", imgnavigateStyle );
	stylesByName.put( "imgmore", imgmoreStyle );
	stylesByName.put( "listitem", listitemStyle );
	stylesByName.put( "radiobox", radioboxStyle );
	stylesByName.put( "checkbox", checkboxStyle );
	stylesByName.put( "list", listStyle );
	stylesByName.put( "listitem", listitemStyle );
	stylesByName.put( "form", formStyle );
	stylesByName.put( "textfield", textfieldStyle );
	stylesByName.put( "choicegroup", choicegroupStyle );
	stylesByName.put( "radiobox", radioboxStyle );
	stylesByName.put( "checkbox", checkboxStyle );
	stylesByName.put( "button", buttonStyle );
	stylesByName.put( "a", aStyle );
	stylesByName.put( "p", pStyle );
	stylesByName.put( "gauge", gaugeStyle );
}
	public static Style focusedStyle = new Style("focused", 0, new de.enough.polish.ui.backgrounds.SimpleBackground(0), null, new short[]{-17}, new Object[]{new Color(0xffffff, false)} );	// the focused-style is not defined.
	/** Access to the currently shown J2ME Polish screen, if any */
	public static Screen currentScreen;	
	/** Access to the application's Display */
	public static Display display;
	/** Access to the currently running MIDlet */
	public static MIDlet midlet;
	/** Access to the AnimationThread responsible for animating all user interface components */
	public static AnimationThread animationThread;
	/** default OK command */
	//#ifdef polish.i18n.useDynamicTranslations
		public static Command OK_CMD = new Command( Locale.get(182), Command.OK, 2 );
	//#elifdef polish.command.ok:defined
		//#= public static final Command OK_CMD = new Command("${polish.command.ok}", Command.OK, 2 );
	//#else
		//# public static final Command OK_CMD = new Command("OK", Command.OK, 2 );
	//#endif
	/** default CANCEL command */
	//#ifdef polish.i18n.useDynamicTranslations
		public static Command CANCEL_CMD = new Command(Locale.get(175), Command.CANCEL, 2 );
	//#elifdef polish.command.cancel:defined
		//#= public static final Command CANCEL_CMD = new Command("${polish.command.cancel}", Command.CANCEL, 3 );
	//#else
		//# public static final Command CANCEL_CMD = new Command("Cancel", Command.CANCEL, 3 );
	//#endif

	/**
	 * Retrieves the image with the given name.
	 * When the image has been cached before, it will be returned immediately.
	 * When it has not been cached before, it either will be loaded directly
	 * or in a background thread. This behaviour is set in the 
	 * <a href="../../../../definitions/polish_xml.html">polish.xml</a> file.
	 * 
	 * @param url the URL of the Image, e.g. "/background.png"
	 * @param parent the object which needs the image, when the image should be loaded
	 * 		   		in the background, the parent need to implement
	 * 				the ImageConsumer interface when it wants to be notified when
	 * 				the picture has been loaded.
	 * @param cache true when the image should be cached for later retrieval.
	 *              This costs RAM obviously, so you should decide carefully if
	 *              large images should be cached.
	 * @return the image when it either was cached or is loaded directly.
 	 *              When the should be loaded in the background, it will be later
	 *              set via the ImageConsumer.setImage()-method.
	 * @throws IOException when the image could not be loaded directly
	 * @see ImageConsumer#setImage(String, Image)
	 */
	public static Image getImage( String url, Object parent, boolean cache )
	throws IOException 
	{
		// check if the image has been cached before:
		//#if polish.allowImageCaching != false
			if ( imagesByName != null ) {
				Image image = (Image) imagesByName.get( url );
				if (image != null) {
					return image;
				}
			}
		//#endif
		//#if ! polish.images.backgroundLoad
			// when images should be loaded directly, try to do so now:
			//#ifdef polish.classes.ImageLoader:defined
				//#= Image image = ${ classname( polish.classes.ImageLoader ) }.loadImage( url );
			//#else
				Image image = null; 
				//#if polish.i18n.loadResources
					//# try {
				//#endif
						image = Image.createImage( url );
				//#if polish.i18n.loadResources
					//# } catch (IOException e) {
						//# if (Locale.LANGUAGE == null || Locale.LANGUAGE.length() == 0) {
							//# throw e;
						//# }
						//# String localeUrl = "/" + Locale.LANGUAGE + url;
						//# image = Image.createImage( localeUrl );
					//# }
				//#endif
			//#endif
			//#if polish.allowImageCaching != false
				if (cache) {
					if (imagesByName == null ) {
						imagesByName = new Hashtable();
					}
					imagesByName.put( url, image );
				}
			//#endif
			return image;
		//#else
			//# // when images should be loaded in the background, 
			//# // tell the background-thread to do so now:		
			//# if ( ! (parent instanceof ImageConsumer)) {
				//#debug error
				//# System.out.println("StyleSheet.getImage(..) needs an ImageConsumer when images are loaded in the background!");
				//# return null;
			//# }
			//# if (scheduledImagesByName == null ) {
				//# scheduledImagesByName = new Hashtable();
			//# }
			//# ImageQueue queue = (ImageQueue) scheduledImagesByName.get(url);
			//# if (queue != null) {
				//# // this image is already scheduled to load:
				//# queue.addConsumer((ImageConsumer) parent);
				//# return null;
			//# }
			//# scheduledImagesByName.put( url, new ImageQueue( (ImageConsumer) parent, cache ) );
			//# if (imagesByName == null ) {
				//# imagesByName = new Hashtable();
			//# }
			//# if (timer == null) {
				//# timer = new Timer();
			//# }
			//# ImageTask task = new ImageTask( url );
			//# timer.schedule( task, 10 );
			//# return null;
		//#endif
	}
	
	//#ifdef polish.images.backgroundLoad
	//# /**
	 //# * Notifies the GUI items which requested images about the successful loading of thoses images.
	 //# * 
	 //# * @param name the URL of the image
	 //# * @param image the image 
	 //# */
	//# public static void notifyImageConsumers( String name, Image image ) {
		//# ImageQueue queue = (ImageQueue) scheduledImagesByName.remove(name);
		//# if (queue != null) {
			//# if (queue.cache) {
				//# imagesByName.put( name, image );
			//# }
			//# queue.notifyConsumers(name, image);
			//# if (true) {
				//# return;
			//# }
			//# if (currentScreen != null) {
				//# currentScreen.repaint();
			//# }
		//# }
	//# }
	//#endif
	
	/**
	 * Gets the style with the specified name.
	 * 
	 * @param name the name of the style
	 * @return the specified style or null when no style with the given 
	 * 	       name has been defined.
	 */
	public static Style getStyle( String name ) {
		Style style =  (Style) stylesByName.get( name );
		if (style == null) {
			style =  (Style) stylesByName.get( name.toLowerCase() );
		}
		return style;
	}
	
	/**
	 * Retrieves all registered styles in a Hashtable.
	 * 
	 * @return all registered styles in a Hashtable.
	 */
	public static Hashtable getStyles()
	{
		return stylesByName;
	}
	
	//#ifdef polish.useDynamicStyles
	/**
	 * Retrieves the style for the given item.
	 * This function is only available when the &lt;buildSetting&gt;-attribute
	 * [useDynamicStyles] is enabled.
	 * This function allows to set styles without actually using the preprocessing-
	 * directive //#style. Beware that this dynamic style retrieval is not as performant
	 * as the direct-style-setting with the //#style preprocessing directive.
	 *  
	 * @param item the item for which the style should be retrieved
	 * @return the appropriate style. When no specific style is found,
	 *         the default style is returned.
	 */
	public static Style getStyle( Item item ) {
		if (item.screen == null) {
			//#debug error
			//# System.out.println("unable to retrieve style for item [" + item.getClass().getName() + "] without screen.");
			return defaultStyle;
		}
		String itemCssSelector = item.cssSelector;
		String screenCssSelector = item.screen.cssSelector;
		Style style = null;
		String fullStyleName;
		StringBuffer buffer = new StringBuffer();
		buffer.append( screenCssSelector );
		if (item.parent == null) {
			//#debug
			//# System.out.println("item.parent == null");
			buffer.append('>').append( itemCssSelector );
			fullStyleName = buffer.toString();
			style = (Style) stylesByName.get( fullStyleName );
			if (style != null) {
				return style;
			}
			style = (Style) stylesByName.get( screenCssSelector + " " + itemCssSelector );
		} else if (item.parent.parent == null) {
			//#debug
			//# System.out.println("Item has one parent.");
			// this item is propably in a form or list,
			// typical hierarchy is for example "form>container>p"                                                 
			Item parent = item.parent;
			String parentCssSelector = parent.cssSelector;
			if (parentCssSelector == null) {
				parentCssSelector = parent.createCssSelector();
			}
			//#debug
			//# System.out.println( parent.getClass().getName() + "-css-selector: " + parentCssSelector );
			buffer.append('>').append( parentCssSelector )
				  .append('>').append( itemCssSelector );
			fullStyleName = buffer.toString();
			//#debug
			//# System.out.println("trying " + fullStyleName);
			style = (Style) stylesByName.get( fullStyleName );
			if (style != null) {
				return style;
			}
			// 1. try: "screen item":
			String styleName = screenCssSelector + " " + itemCssSelector;
			//#debug
			//# System.out.println("trying " + styleName);
			style = (Style) stylesByName.get( styleName );
			if (style == null) {
				// 2. try: "screen*item":
				styleName = screenCssSelector + "*" + itemCssSelector;
				//#debug
				//# System.out.println("trying " + styleName);
				style = (Style) stylesByName.get( styleName );
				if (style == null) {
					// 3. try: "parent>item"
					styleName = parentCssSelector + ">" + itemCssSelector;
					//#debug
					//# System.out.println("trying " + styleName);
					style = (Style) stylesByName.get( styleName );
					if (style == null) {
						// 4. try: "parent item"
						styleName = parentCssSelector + " " + itemCssSelector;
						//#debug
						//# System.out.println("trying " + styleName);
						style = (Style) stylesByName.get( styleName );
					}
				}
			}
			//#debug
			//# System.out.println("found style: " + (style != null));
		} else {
			//#debug
			//# System.out.println("so far unable to set style: complex item setup");
			// this is a tiny bit more complicated....
			fullStyleName = null;
		}
		if (style == null) {
			// try just the item:
			//#debug
			//# System.out.println("trying " + itemCssSelector);
			style = (Style) stylesByName.get( itemCssSelector );
			if (style == null) {
				//#debug
				//# System.out.println("Using default style for item " + item.getClass().getName() );
				style = defaultStyle;
			}
			//#ifdef polish.debug.debug
				//# else {
					//#debug
					//# System.out.println("Found style " + itemCssSelector );
				//# }
			//#endif
		}
		if ( fullStyleName != null && style != null ) {
			stylesByName.put( fullStyleName, style );
		}
		return style;
	}
	//#endif

	//#ifdef polish.useDynamicStyles
	/**
	 * Retrieves a dynamic style for the given screen.
	 * 
	 * @param screen the screen for which a style should be retrieved
	 * @return the style for the given screen.
	 */
	public static Style getStyle(Screen screen) {
		Style style = (Style) stylesByName.get( screen.cssSelector );
		if (style == null) {
			return defaultStyle;
		}
		return style;
	}		
	//#endif
	
	
	/**
	 * Releases all (memory intensive) resources such as images or RGB arrays of this style sheet.
	 */
	public static void releaseResources() {
		//#if polish.allowImageCaching != false
		if (imagesByName != null) {
			imagesByName.clear();
		}
		//#endif
		//#ifdef polish.useDynamicStyles
			Enumeration enumeration = stylesByName.elements();
			while (enumeration.hasMoreElements()) {
				Style style = (Style) enumeration.nextElement();
				style.releaseResources();
			}
		//#endif
		//#ifdef polish.StyleSheet.releaseResources:defined
			//#include ${polish.StyleSheet.releaseResources}
		//#endif
	}


	public static Style[] getDynamicStyles() {
		//#if polish.inSkinEditor == true
			//# return (Style[]) dynamicStylesList.toArray( new Style[ dynamicStylesList.size() ] );
			//# }
		//#else
//			java.util.Enumeration enumeration = dynamicStylesByName.elements();
//			Style[] styles = new Style[ dynamicStylesByName.size() ];
//			for (int i=0; i<styles.length; i++) {
//				styles[i] = (Style) enumeration.nextElement();
//			}
//			return styles;
			return new Style[]{ defaultStyle, focusedStyle };
		//#endif
	}
	
	
//#ifdef polish.StyleSheet.additionalMethods:defined
	//#include ${polish.StyleSheet.additionalMethods}
//#endif

}
