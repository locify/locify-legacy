/*
 * FileBrowser.java
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
package com.locify.client.gui.screen.internal;

import com.locify.client.utils.R;
import com.locify.client.data.IconData;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Utils;
import java.util.Enumeration;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.List; import de.enough.polish.ui.StyleSheet;

/**
 * This class contains phone file browser for input type=file
 * @author Destil
 */
public class FileBrowser implements CommandListener {

    private List list;
    private String currentFolder;
    private String selectedFileName;
    private String selectedFilePath;
    private boolean fileSelected;

    public FileBrowser() {
    }

    /**
     * Starts file system browsing according to file type
     * @param fileType file type
     */
    public void start(String fileType) {
        fileSelected = false;
        if (fileType == null) {
            view("");
        } else if (fileType.equals("image")) {
            String dir = System.getProperty("fileconn.dir.photos");
            //hack pro nokie
            if (Capabilities.isNokia() && R.getFileSystem().exists("file:///E:/Images/")) {
                dir = "file:///E:/Images/";
            }
            if (dir != null) {
                view(Utils.replaceString(dir, "file:///", ""));
            } else {
                view("");
            }
        } else if (fileType.equals("video")) {
            String dir = System.getProperty("fileconn.dir.videos");
            if (dir != null) {
                view(Utils.replaceString(dir, "file:///", ""));
            } else {
                view("");
            }
        } else {
            view("");
        }
    }

    /**
     * Views selected folder - "" for root selection
     * @param folder
     */
    public void view(String folder) {
        // list = new List(Locale.get("Select_file"), List.IMPLICIT);
        list = new List(folder, List.IMPLICIT);
        currentFolder = folder;
        Enumeration files = null;
        if (folder.equals("")) {
            files = R.getFileSystem().getRoots();
        } else {
            files = R.getFileSystem().getFiles(folder);
        }
        while (files.hasMoreElements()) {
            String file = (String) files.nextElement();
            if (file.endsWith("/")) {
                //directory
                list.append(file, IconData.get("locify://icons/browse.png"));
            } else {
                list.append(file, null);
            }
        }
        list.addCommand(Commands.cmdBack);
        list.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, list);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            String selected = list.getString(list.getSelectedIndex());
            if (selected.endsWith("/")) {
                R.getURL().call("locify://filebrowser?folder=" + currentFolder + selected);
            } else {
                selectFile(currentFolder + selected, selected);
            }
        } else if (c == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }

    public String getFilePath() {
        return selectedFilePath;
    }

    public String getFileName() {
        return selectedFileName;
    }

    public boolean isFileSelected() {
        return fileSelected;
    }

    public void selectFile(String path, String name) {
        fileSelected = true;
        selectedFileName = name;
        selectedFilePath = path;
        R.getHTMLScreen().updateFileInfo(name);
    }
}
