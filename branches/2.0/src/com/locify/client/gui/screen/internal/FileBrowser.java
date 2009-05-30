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
import com.sun.lwuit.Form;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import java.util.Enumeration;

/**
 * This class contains phone file browser for input type=file
 * @author Destil
 */
public class FileBrowser implements ActionListener {

    private Form form;
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
        form = new Form(folder);
        form.setLayout(new BorderLayout());

        list = new List();
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
                //list.addItem(file, IconData.get("locify://icons/browse.png"));
                list.addItem(file);
            } else {
                list.addItem(file);
            }
        }
        list.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                String selected = (String) list.getSelectedItem();
                if (selected.endsWith("/")) {
                    R.getURL().call("locify://filebrowser?folder=" + currentFolder + selected);
                } else {
                    selectFile(currentFolder + selected, selected);
                }
            }
        });

        form.addCommand(Commands.cmdBack);
        form.setCommandListener(this);
        form.show();
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

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }
}
