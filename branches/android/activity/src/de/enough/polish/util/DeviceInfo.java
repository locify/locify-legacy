/*
 * Created on Apr 5, 2009 at 8:36:51 PM.
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
package de.enough.polish.util;

//#if polish.midp
//# import de.enough.polish.ui.Canvas; import de.enough.polish.ui.StyleSheet;
//#endif

/**
 * <p>Provides information about the current device</p>
 *
 * <p>Copyright Enough Software 2009</p>
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class DeviceInfo
{

	/** The vendor of this device is not known */
	public static final int VENDOR_UNKNOWN = -1;
	/** The vendor of this device is Nokia */
	public static final int VENDOR_NOKIA = 1;
	/** The vendor of this device is Samsung */
	public static final int VENDOR_SAMSUNG = 2;
	/** The vendor of this device is LG */
	public static final int VENDOR_LG = 3;
	/** The vendor of this device is Sony Ericsson */
	public static final int VENDOR_SONY_ERICSSON = 4;
	/** The vendor of this device is Motorola */
	public static final int VENDOR_MOTOROLA = 5;
	/** The vendor of this device is ZTE */
	public static final int VENDOR_ZTE = 6;
	/** The vendor of this device is Acer */
	public static final int VENDOR_ACER = 7;
	/** The vendor of this device is Alcatel */
	public static final int VENDOR_ALCATEL = 8;
	/** The vendor of this device is BlackBerry */
	public static final int VENDOR_BLACKBERRY = 9;
	/** The vendor of this device is Sagem */
	public static final int VENDOR_SAGEM = 10;
	/** The vendor of this device is  * /
	public static final int VENDOR_ = ;
	*/
	

	private DeviceInfo()
	{
		// disallow external instantiation
	}
	
	/**
	 * Tries to detect the vendor of this device.
	 * 
	 * @return the vendor, e.g. VENDOR_NOKIA or VENDOR_UNKNOWN
	 */
	public static int getVendor() {
		String platform = System.getProperty( "microedition.platform" );
		if (platform == null) {
			platform = "";
		} else {
			platform = platform.toLowerCase();
		}
		if ("j2me".equals(platform)) {
			String model = System.getProperty( "device.model" );
			if (model != null) {
				return VENDOR_MOTOROLA;
			}
		}
		if (platform.startsWith("nokia")) {
			return VENDOR_NOKIA;
		}
		if (platform.startsWith("sonyericsson")) {
			return VENDOR_SONY_ERICSSON;
		}
		if (platform.startsWith("samsung")) {
			return VENDOR_SAMSUNG;
		}
		if (platform.startsWith("lg")) {
			return VENDOR_LG;
		}
		if (platform.startsWith("zte")) {
			return VENDOR_ZTE;
		}
		if (platform.startsWith("blackberry")) {
			return VENDOR_BLACKBERRY;
		}
		return VENDOR_UNKNOWN;
	}
	
	/**
	 * Retrieves the name of the device's vendor or null when it cannot be detected.
	 * @return the vendor name or null
	 */
	public static String getVendorName() {
		int vendor = getVendor();
		switch (vendor) {
		case VENDOR_UNKNOWN: return null;
		case VENDOR_NOKIA: return "Nokia";
		case VENDOR_SAMSUNG: return "Samsung";
		case VENDOR_LG: return "LG";
		case VENDOR_SONY_ERICSSON: return "Sony Ericsson";
		case VENDOR_MOTOROLA: return "Motorola";
		case VENDOR_ZTE: return "ZTE";
		case VENDOR_ACER: return "Acter";
		case VENDOR_ALCATEL: return "Alcatel";
		case VENDOR_BLACKBERRY: return "BlackBerry";
		case VENDOR_SAGEM: return "Sagem";
		}
		return null;
	}
	
	/**
	 * Tries to guess the key for changing the input mode, e.g. from 123 to abc.
	 * 
	 * @return the key, by default Canvas.KEY_POUND 
	 */
	public static int getKeyInputModeSwitch() {
		int key = 35; // == Canvas.KEY_POUND
		//#if polish.midp
			//# if (getVendor() == VENDOR_SONY_ERICSSON) {
				//# key = Canvas.KEY_STAR;
			//# }
		//#endif
		return key;
	}
	
	/**
	 * Tries to guess the key for entering a space character.
	 * 
	 * @return the key, by default Canvas.KEY_NUM0; 
	 */
	public static int getKeySpace() {
		int key = 48; // == Canvas.KEY_NUM0
		//#if polish.midp
			//# if (getVendor() == VENDOR_SONY_ERICSSON) {
				//# key = Canvas.KEY_POUND;
			//# }
		//#endif
		return key;
	}


}
