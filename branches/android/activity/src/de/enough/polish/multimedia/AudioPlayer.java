//#condition polish.api.mmapi || polish.midp2
/*
 * Created on Nov 21, 2006 at 6:16:24 PM.
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
package de.enough.polish.multimedia;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import de.enough.polish.android.media.Manager;
import de.enough.polish.android.media.MediaException;
import de.enough.polish.android.media.Player;
import de.enough.polish.android.media.PlayerListener;
import de.enough.polish.android.media.control.VolumeControl;

//#if polish.android
import de.enough.polish.android.helper.ResourceInputStream;
import de.enough.polish.android.helper.ResourcesHelper;
import de.enough.polish.android.midlet.MIDlet;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
//#endif
/**
 * <p>
 * Plays back audio files - at the moment this is only supported for MIDP 2.0 and devices that support the MMAPI and for Android devices.
 * </p>
 * 
 * <p>
 * Copyright Enough Software 2006 - 2009
 * </p>
 * 
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class AudioPlayer implements PlayerListener
//#if polish.android
	, MediaPlayer.OnCompletionListener 
//#endif
{

	private final static Hashtable AUDIO_TYPES = new Hashtable();

	private final boolean doCachePlayer;

	private Player player;
	private PlayerListener listener;
	//#if polish.android
	private MediaPlayer androidPlayer;
	//#endif

	private final String defaultContentType;

	private int volumeLevel = -1;

	private int previousVolumeLevel;

	private int androidMaxVolume;

	/**
	 * Creates a new audio player with no default content type and no caching.
	 */
	public AudioPlayer() {
		this(false, null, null);
	}

	/**
	 * Creates a new audio player with no default content type.
	 * 
	 * @param doCachePlayer caches the player even though the end of the media is reached
	 */
	public AudioPlayer(boolean doCachePlayer) {
		this(doCachePlayer, null, null);
	}

	/**
	 * Creates a new audio player without caching and with no listener.
	 * @param contentType the type of the referenced media, this is being resolved to the phone's expected type automatically.
	 *        You can, for example, use the type "audio/mp3" and this method resolves the type to "audio/mpeg3", if this
	 *        is expected by the device.
	 */
	public AudioPlayer(String contentType) {
		this(false, contentType, null);
	}

	/**
	 * Creates a new audio player with no listener
	 * @param doCachePlayer caches the player even though the end of the media is reached
	 * @param contentType the type of the referenced media, this is being resolved to the phone's expected type automatically.
	 *        You can, for example, use the type "audio/mp3" and this method resolves the type to "audio/mpeg3", if this
	 *        is expected by the device.
	 */
	public AudioPlayer(boolean doCachePlayer, String contentType) {
		this(doCachePlayer, contentType, null);
	}

	/**
	 * Creates a new audio player
	 * @param doCachePlayer caches the player even though the end of the media is reached
	 * @param contentType the type of the referenced media, this is being resolved to the phone's expected type automatically.
	 *        You can, for example, use the type "audio/mp3" and this method resolves the type to "audio/mpeg3", if this
	 *        is expected by the device.
	 * @param listener an optional PlayerListener
	 */
	public AudioPlayer(boolean doCachePlayer, String contentType, PlayerListener listener)
	{
		this.listener = listener;
		this.doCachePlayer = doCachePlayer;
		if (contentType != null) {
			if (!contentType.startsWith("audio/")) {
				contentType = "audio/" + contentType;
			}
			String correctContentType = getAudioType(contentType, null);
			if (correctContentType != null) {
				contentType = correctContentType;
			}
		}
		this.defaultContentType = contentType;
	}
	
	/**
	 * Sets a player listener, replacing any previously registered listener.
	 * 
	 * @param listener the new listener or null
	 */
	public void setPlayerListener( PlayerListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Retrieves the currently registered player listener.
	 * @return the current player listener
	 */
	public PlayerListener getPlayerListener() {
		return this.listener;
	}

	/**
	 * Plays the media taken from the specified URL.
	 * @param url the URL of the media 
	 * @param type the type of the referenced media, this is being resolved to the phone's expected type automatically.
	 *        You can, for example, use the type "audio/mp3" and this method resolves the type to "audio/mpeg3", if this
	 *        is expected by the device.
	 * @throws MediaException when the media is not supported
	 * @throws IOException when the URL cannot be resolved
	 */
	public void play(String url, String type) throws MediaException, IOException
	{
		//#if polish.android
			if(url.startsWith("file://")) {
				this.androidPlayer = new MediaPlayer();
				this.androidPlayer.setDataSource(url);
			} else {
				int resourceID = ResourcesHelper.getResourceID(url);
				this.androidPlayer = MediaPlayer.create(MIDlet.midletInstance, resourceID);
			}
			this.androidPlayer.setOnCompletionListener(this);
			this.androidPlayer.start();
		//#else
			//# InputStream in = getClass().getResourceAsStream(url);
			//# if (in == null) {
				//# throw new IOException("not found: " + url);
			//# }
			//# play(in, type);
		//#endif
	}

	/**
	 * Plays the media taken from the specified input stream.
	 * @param in the media input 
	 * @param type the type of the referenced media, this is being resolved to the phone's expected type automatically.
	 *        You can, for example, use the type "audio/mp3" and this method resolves the type to "audio/mpeg3", if this
	 *        is expected by the device.
	 * @throws MediaException when the media is not supported
	 * @throws IOException when the input cannot be read
	 */
	public void play(InputStream in, String type) throws MediaException, IOException 
	{
		String correctType = getAudioType(type, "file");
		if (correctType == null) {
			//#debug warn
			//# System.out.println("Unable to find correct type for " + type + " with the file protocol");
			correctType = getAudioType(type, null);
			if (correctType == null) {
				//#debug warn
				//# System.out.println("Unable to find correct type for " + type);
				correctType = type;
			}
		}
		//#if polish.android
			if (in instanceof ResourceInputStream) {
				this.androidPlayer = MediaPlayer.create(MIDlet.midletInstance, ((ResourceInputStream)in).getResourceId());
				this.androidPlayer.setOnCompletionListener(this);
				this.androidPlayer.start();
			} 
			//#if polish.debug.warn
				//# else {
					//#debug warn
					//# System.out.println("Unable to play input stream: input stream does not originate from a resource.");
				//# }
			//#endif
		//#else
			//# this.player = Manager.createPlayer(in, correctType);
			//# this.player.addPlayerListener(this);
			//# this.player.start();
		//#endif
		if (this.volumeLevel != -1) {
			setVolumeLevel(this.volumeLevel);
		}
	}

	/**
	 * Plays the media taken from the specified URL  with the content type specified in the constructor.
	 * @param url the URL of the media 
	 * @throws MediaException when the media is not supported
	 * @throws IOException when the URL cannot be resolved
	 */
	public void play(String url) throws MediaException, IOException 
	{
		//#if polish.android
			if(url.startsWith("file://")) {
				this.androidPlayer = new MediaPlayer();
				this.androidPlayer.setDataSource(url);
			} else {
				int resourceID = ResourcesHelper.getResourceID(url);
				this.androidPlayer = MediaPlayer.create(MIDlet.midletInstance, resourceID);
				
			}
			this.androidPlayer.setOnCompletionListener(this);
			this.androidPlayer.start();
		//#else
			//# InputStream in = getClass().getResourceAsStream(url);
			//# if (in == null) {
				//# throw new IOException("not found: " + url);
			//# }
			//# play(in);
		//#endif
		if (this.volumeLevel != -1) {
			setVolumeLevel(this.volumeLevel);
//		} else {
//			this.volumeLevel = getVolumeLevel();
		}
	}

	/**
	 * Plays the media taken from the specified input stream with the content type specified in the constructor.
	 * @param in the media input 
	 * @throws MediaException when the media is not supported
	 * @throws IOException when the input cannot be read
	 */
	public void play(InputStream in)
	throws MediaException, IOException 
	{
		//#if polish.android
			if (in instanceof ResourceInputStream) {
				this.androidPlayer = MediaPlayer.create(MIDlet.midletInstance, ((ResourceInputStream)in).getResourceId());
				this.androidPlayer.setOnCompletionListener(this);
				this.androidPlayer.start();
			} 
			//#if polish.debug.warn
				//# else {
					//#debug warn
					//# System.out.println("Unable to play input stream: input stream does not originate from a resource.");
				//# }
			//#endif
		//#else
			//# String correctType = this.defaultContentType;
			//# this.player = Manager.createPlayer(in, correctType);
			//# this.player.addPlayerListener(this);
			//# this.player.start();
		//#endif
	}

	/**
	 * Plays back the last media again. This can only be used when doCachePlayer
	 * is set to true in the constructor
	 * 
	 * @throws MediaException
	 *             when the player cannot be started
	 * @see #AudioPlayer(boolean)
	 * @see #AudioPlayer(boolean, String)
	 * @see #AudioPlayer(boolean, String, PlayerListener)
	 * @see #AudioPlayer(String)
	 */
	public void play() throws MediaException
	{
		//#if polish.android
			if (this.androidPlayer != null) {
				this.androidPlayer.start();
			}
		//#else
			//# if (this.player != null) {
				//# this.player.start();
			//# }
		//#endif
	}

	/**
	 * Returns the original player.
	 * 
	 * @return the original player, this can be null when no audio has been
	 *         played back so far.
	 */
	//#if !polish.android
	//# public Player getPlayer() {
		//# return this.player;
	//# }
	//#endif

	/**
	 * Helper function for getting a supported media type.
	 * 
	 * @param type the type like "audio/mp3"
	 * @param protocol
	 *            the protocol, when null is given the content type will be
	 *            returned for any protocol
	 * @return the type supported by the device, for example "audio/mpeg3" -
	 *         null when the given type is not supported by the device.
	 */
	public static String getAudioType(String type, String protocol) {		
		//#if !polish.android
		//# if (AUDIO_TYPES.size() == 0) {
			//# addTypes(new String[] { "audio/3gpp", "audio/3gp" });
			//# addTypes(new String[] { "audio/x-mp3", "audio/mp3", "audio/x-mp3",
					//# "audio/mpeg3", "audio/x-mpeg3", "audio/mpeg-3" });
			//# addTypes(new String[] { "audio/midi", "audio/x-midi", "audio/mid",
					//# "audio/x-mid", "audio/sp-midi" });
			//# addTypes(new String[] { "audio/wav", "audio/x-wav" });
			//# addTypes(new String[] { "audio/amr", "audio/x-amr" });
			//# addTypes(new String[] { "audio/mpeg4", "audio/mpeg-4", "audio/mp4",
					//# "audio/mp4a-latm" });
			//# addTypes(new String[] { "audio/imelody", "audio/x-imelody",
					//# "audio/imy", "audio/x-imy" });
		//# }
		//# String[] supportedContentTypes = Manager
				//# .getSupportedContentTypes(protocol);
		//# if (supportedContentTypes == null || supportedContentTypes.length == 0) {
			//# return null;
		//# }
		//# Hashtable mappings = (Hashtable) AUDIO_TYPES.get(type);
		//# if (mappings == null) {
			//#debug warn
			//# System.out.println("The audio content type " + type
					//# + " has no known synonyms.");
			//# for (int i = 0; i < supportedContentTypes.length; i++) {
				//# String contentType = supportedContentTypes[i];
				//# if (contentType.equals(type)) {
					//# return type;
				//# }
			//# }
		//# } else {
			//# for (int i = 0; i < supportedContentTypes.length; i++) {
				//# String contentType = supportedContentTypes[i];
				//# if (mappings.containsKey(contentType)) {
					//# return contentType;
				//# }
			//# }
		//# }
		//#endif
		return null;
	}
	
	/**
	 * Determines whether the given audio format is supported by this device for the specified protocol.
	 * @param type the type like "audio/mp3"
	 * @param protocol
	 *            the protocol, when null is given the content type will be
	 *            returned for any protocol
	 * @return true when the given audio type is supported
	 */
	public static boolean isSupportedAudioType( String type, String protocol ) {
		return getAudioType(type, protocol) != null;
	}

	/**
	 * Determines if the audio player is currently playing music
	 * @return true when audio is played back
	 */
	public boolean isPlaying() {
		//#if polish.android
			if (this.androidPlayer != null) {
				return this.androidPlayer.isPlaying();
			}
		//#else
			//# if (this.player != null) {
				//# return this.player.getState() == Player.STARTED;
			//# }
		//#endif
		return false;
	}

	private static final void addTypes(String[] types) {
		Hashtable nestedMap = new Hashtable();
		for (int i = 0; i < types.length; i++) {
			String type = types[i];
			nestedMap.put(type, type);
			AUDIO_TYPES.put(type, nestedMap);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.media.PlayerListener#playerUpdate(javax.microedition.media.Player,
	 *      java.lang.String, java.lang.Object)
	 */
	public void playerUpdate(Player p, String event, Object data) {

		if (this.listener != null) {
			this.listener.playerUpdate(p, event, data);
		}
		if (!this.doCachePlayer && PlayerListener.END_OF_MEDIA.equals(event)) {
			//#if !polish.android
				//# p.removePlayerListener(this);
			//#endif
			cleanUpPlayer();
		}
	}

	/**
	 * Closes and deallocates the player.
	 */
	public void cleanUpPlayer() {
		//TODO: rickyn: do we need to reset the volume?
		this.volumeLevel = -1;
		//#if !polish.android
			//# if (this.player != null) {
				//# this.player.deallocate();
				//# this.player.close(); // necessary for some Motorola devices
				//# this.player = null;
			//# }
		//#else
			if (this.androidPlayer != null) {
				this.androidPlayer.release();
		 		this.androidPlayer = null;
			}
		//#endif
	}
	/**
	 * Gets the volume using a linear point scale with values between 0 and 100.
	 * 0 is silence; 100 is the loudest useful level that this VolumeControl supports. If the given level is less than 0 or greater than 100, the level will be set to 0 or 100 respectively.
	 * When setLevel results in a change in the volume level, a VOLUME_CHANGED event will be delivered through the PlayerListener. 
	 * @return the volume level between 0 and 100 or -1 when the player is not initialized
	 */
	public int getVolumeLevel() {
		int volume;
		//#if polish.android
			AudioManager audioManager = (AudioManager) MIDlet.midletInstance.getSystemService(Context.AUDIO_SERVICE);			
			if (this.androidMaxVolume == -1) {
				this.androidMaxVolume = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC);
			}
			int current = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC);
			volume = (int) (((float)current * 100f) / this.androidMaxVolume);
		//#else
			//# Player pl = this.player;
			//# if (pl != null) {
				//# VolumeControl volumeControl = (VolumeControl) pl.getControl("VolumeControl");
				//# if (volumeControl != null) {
					//# return volumeControl.getLevel();
				//# }
			//# }
			//# volume = this.volumeLevel;
		//#endif
		return volume;
	}
	
	/**
	 * Sets the volume using a linear point scale with values between 0 and 100.
	 * 0 is silence; 100 is the loudest useful level that this VolumeControl supports. If the given level is less than 0 or greater than 100, the level will be set to 0 or 100 respectively.
	 * When setLevel results in a change in the volume level, a VOLUME_CHANGED event will be delivered through the PlayerListener.
	 *  
	 * @param level the volume level between 0 and 100
	 */
	public void setVolumeLevel(int level) {
		//#if polish.android
			if (this.androidPlayer != null) {
				if (this.androidMaxVolume == -1) {
					AudioManager audioManager = (AudioManager) MIDlet.midletInstance.getSystemService(Context.AUDIO_SERVICE);			
					this.androidMaxVolume = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC);
				}
				if (level < 0) {
					level = 0;
				} else if (level > 100) {
					level = 100;
				}
				float levelF = ((float)level * this.androidMaxVolume) / 100f;
				this.androidPlayer.setVolume(levelF,levelF);
			}
		//#else
			//# Player pl = this.player;
			//# if (pl != null) {
				//# VolumeControl volumeControl = (VolumeControl) pl.getControl("VolumeControl");
				//# if (volumeControl != null) {
					//# volumeControl.setLevel(100);
					//# return;
				//# }
			//# }
		//#endif
		this.volumeLevel = level;
	}
	
	/**
	 * Detects the the player is currently muted
	 * @return true when the player is muted
	 */
	public boolean isMuted() {
		int level = getVolumeLevel();
		return level == 0;
	}
	
	/**
	 * Mutes the player or restores the previous volume level
	 * @param mute true when the player should be muted, false when the previous volume level should be restored
	 */
	public void setMute( boolean mute ) {
		if (mute) {
			this.previousVolumeLevel = getVolumeLevel();
			setVolumeLevel(0);
		} else if (this.previousVolumeLevel != -1){
			setVolumeLevel(this.previousVolumeLevel);
		}
	}

	//#if polish.android
	/**
	 * Informs the audio player about a finished media on Android devices.
	 * @param mp the media player (should be the same as this.mediaPlayer)
	 */
	public void onCompletion(MediaPlayer mp) {
		playerUpdate( this.player, PlayerListener.END_OF_MEDIA, null );
	}
	//#endif

}
