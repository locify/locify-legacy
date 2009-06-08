/*
 * AudioData.java
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
package com.locify.client.data;

import com.locify.client.utils.R;
import com.locify.client.net.Http;
import java.io.ByteArrayInputStream;
import de.enough.polish.android.media.MediaException;
import de.enough.polish.android.media.Player;
import de.enough.polish.android.media.Manager;

/**
 * Manages downloading, storing and playing audio files in WAV format
 * @author Destil
 */
public class AudioData {

    private String url = null;

    public AudioData() {
    }

    /**
     * Play wav file located on some URL. Caches downloaded audio files into filesystem
     * @param url
     */
    public void play(String url) {
        try {
            if (url == null || url.equals("")) {
                return;
            }
            if (R.getFileSystem().exists(FileSystem.AUDIO_FOLDER + FileSystem.hashFileName(url) + ".wav")) {
                ByteArrayInputStream baos = new ByteArrayInputStream(R.getFileSystem().loadBytes(FileSystem.AUDIO_FOLDER + FileSystem.hashFileName(url) + ".wav"));
                try {
                    Player player = Manager.createPlayer(baos, "audio/x-wav");
                    player.start();
                } catch (MediaException e) {
                    //ignore
                }
            } else {
                R.getHttp().start(url, Http.AUDIO_DOWNLOADER);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "AudioData.play", url);
        }
    }

    /**
     * Saves audio file and plays it
     * @param url
     * @param audioData
     */
    public void save(String url, byte[] audioData) {
        try {
            url = R.getHttp().makeAbsoluteURL(url);
            R.getFileSystem().saveBytes(FileSystem.AUDIO_FOLDER + FileSystem.hashFileName(url) + ".wav", audioData);
            play(url);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "IconData.save", url);
        }
    }

}
