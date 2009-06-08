//#condition polish.usePolishGui && polish.android
package de.enough.polish.android.lcdui;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;

public class DisplayUtil {
	
	KeyCharacterMap characterMap;
	
//	KeyEvent event;
	
	public DisplayUtil(int deviceId)
	{
		this.characterMap = KeyCharacterMap.load(deviceId);
	}
	
//	public KeyEvent getEvent() {
//		return this.event;
//	}
//
//	public void setEvent(KeyEvent event) {
//		this.event = event;
//	}
	
	public int handleKey(int keyCode, KeyEvent event, Canvas canvas)
	{
//		setEvent(event);
		
		int key = event.getKeyCode();
		int meta = event.getMetaState();
		
		int ascii = this.characterMap.get(key, meta);
		
		if( ascii == 0 && 
			(canvas.getGameAction(keyCode) != 0 ||
			isClear(keyCode) || 
			isReturn(keyCode) ||
			isMenu(keyCode))
		)
		{
			if(isClear(keyCode))
			{
				return -8;
			}
			else
			{
				return keyCode;
			}
		}
		else
		{
			return ascii;
		}
	}
	
	public boolean isClear(int keyCode)
	{
		//#ifdef polish.android.key.ClearKey:defined
		//#= if (keyCode == ${polish.android.key.ClearKey})
		//# {
		//#		return true; 
		//# }
		//#endif
		
		return false;
	}
	
	public boolean isReturn(int keyCode)
	{
		//#if polish.key.ReturnKey:defined
if (keyCode == 4)
		{
			return true; 
		}
		//#endif
			
		return false;
	}
	
	public boolean isMenu(int keyCode)
	{
		//#if polish.key.Menu:defined
if (keyCode == 82)
		{
			return true; 
		}
		//#endif
			
		return false;
	}
}
