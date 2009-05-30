/*
 * UploadProgressScreen.java
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

package com.locify.client.gui.screen.service;

import com.locify.client.gui.extension.Progress;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;


/**
 * Shows upload progress
 * @author Destil
 */
public class UploadProgressScreen implements ActionListener {
    
    private FormLocify form;
    private int fileSize; // in kB
    private Progress progress;
    private Label siProgress;

    /**
     * Views the screen with some file size
     * @param fileSize
     */
    public void view(int fileSize) {
        form = new FormLocify(Locale.get("Uploading"));

        this.fileSize = (int)fileSize;
        Label siDummy = new Label("");
        form.addComponent(siDummy);
        progress = new Progress();
        form.addComponent(progress);
        
        siProgress = new Label("0/" + fileSize + " kB");
        form.addComponent(siProgress);
        
        form.addCommand(Commands.cmdStop);
        form.setCommandListener(this);
        form.show();
    }

    public void update(long newSize) {
        progress.setProgress((int)newSize);
        siProgress.setText(newSize + "/" + this.fileSize + " kB");
    }

    public void complete() {
        siProgress.setText(Locale.get("Upload_complete"));
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdStop){
            R.getURL().call("locify://htmlBrowser");
        }

    }
}
