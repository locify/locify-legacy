/*
 * FirstRunManager.java
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
package com.locify.client.gui.manager;

import com.locify.client.data.FileSystem;
import com.locify.client.data.SettingsData;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;

/**
 * This class manages first run and loading of application
 * @author Destil
 */
public class FirstRunManager {

    private Form frmPermissionWarning;
    private Form frmPermissionsHelp;
    private Button btnHowto;
    private Form frmLanguages;
    private List lstLanguages;
    private Form frmRoots;
    private List lstRoots;
    private String chosenLanguage = null;

    public FirstRunManager() {
    }

    /**
     * Determines what to do on the first start
     */
    public void start() {
        try {
            if (chosenLanguage == null) {
                viewLanguageSelection();
            } else {
                //windows mobile has two permission checks
                if (Capabilities.isWindowsMobile()) {
                    permissionsTest();
                }
                if (permissionsTest()) {
                    if (R.getFileSystem().createDefaultRoot()) {
                        R.getMidlet().startMIDlet();
                    } else {
                        viewRootSelection("");
                    }
                } else {
                    viewPermissionsWarning();
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FirstRunManager.start", null);
        }
    }

    /**
     * Perform test if user has set up the file access permissions
     * @return passed
     */
    public boolean permissionsTest() {
        try {
            //try to access filesystem
            long timeBefore = System.currentTimeMillis();
            String defaultRoot = R.getFileSystem().getDefaultRoot();
            R.getFileSystem().writeTestData(defaultRoot);
            long timeAfter = System.currentTimeMillis();
            //decide what to do next
            if ((timeAfter - timeBefore) > 700) { //no permissions set
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FirstRunManager.permissionsText", null);
            return false;
        }
    }

    /**
     * Determines if this is the first time user runs Locify
     * @return if Locify is run for a first time
     */
    public boolean isFirstTime() {
        try {
            boolean firstTime = false;
            RecordStore recordStore = RecordStore.openRecordStore("locify", true);
            byte[] data = null;
            try {
                data = recordStore.getRecord(1);
            } catch (InvalidRecordIDException e) {
                firstTime = true;
            }
            if (!firstTime) {
                try {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                    chosenLanguage = dis.readUTF(); //language
                    dis.readUTF(); //root
                    recordStore.closeRecordStore();
                } catch (EOFException e) {
                    firstTime = true;
                    loadLanguage();
                }
            } else {
                recordStore.closeRecordStore();
            }
            return firstTime;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FirstRunManager.isFirstTime", null);
            return false;
        }
    }

    public void loadLanguage() {
        try {
            RecordStore recordStore = RecordStore.openRecordStore("locify", true);
            byte[] data = recordStore.getRecord(1);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            dis.readUTF();
            chosenLanguage = dis.readUTF();
            Locale.loadTranslations(chosenLanguage);
            dis.close();
            recordStore.closeRecordStore();

            Commands.initializeCommands();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FirstRunManager.loadLanguage", chosenLanguage);
        }
    }

    /**
     * Used just for applet
     */
    public void loadEnglish() {
        try {
            Locale.loadTranslations("en");
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FirstRunManager.loadEnglish", null);
        }
    }

    /**
     * Loads saved preffered language and loads it into UI
     */
    public void loadRoot() {
        try {
            RecordStore recordStore = RecordStore.openRecordStore("locify", true);
            byte[] data = recordStore.getRecord(1);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            String root = dis.readUTF();
            if (!R.getFileSystem().createRoot(root)) {
                R.getFileSystem().createDefaultRoot();
            }
            dis.close();
            recordStore.closeRecordStore();
            SettingsData.setLanguage(chosenLanguage);
            //create map folder
            R.getFileSystem().checkFolders(FileSystem.MAP_FOLDER);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "LoadingScreen.loadLanguage", null);
        }
    }

    /**
     * Saves root and preferred language into database
     * @param root root directory
     */
    public void saveRoot(String root) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(root);
            dos.writeUTF(chosenLanguage);
            byte[] data = baos.toByteArray();
            dos.close();
            RecordStore recordStore = RecordStore.openRecordStore("locify", true);
            recordStore.setRecord(1, data, 0, data.length);
            recordStore.closeRecordStore();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "LoadingScreen.saveRoot", root);
        }
    }

    private void saveLanguage() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(chosenLanguage);
            dos.writeUTF(chosenLanguage);
            byte[] data = baos.toByteArray();
            dos.close();
            RecordStore recordStore = RecordStore.openRecordStore("locify", true);
            recordStore.addRecord(data, 0, data.length);
            recordStore.closeRecordStore();
            loadLanguage();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FirstRunManager.saveLanguage", null);
        }
    }

    /**
     * Views language selection to user
     */
    private void viewLanguageSelection() {
        frmLanguages = new Form("Choose language");
        frmLanguages.setLayout(new BorderLayout());
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                chosenLanguage = R.getSettings().locales[lstLanguages.getSelectedIndex()];
                saveLanguage();
                start();
            }
        };
        Vector items = new Vector();
        for (int i = 0; i < R.getSettings().languageNames.length; i++) {
            items.addElement(R.getSettings().languageNames[i]);
        }
        lstLanguages = new List(items);
        lstLanguages.addActionListener(al);
        frmLanguages.addComponent(BorderLayout.CENTER, lstLanguages);
        frmLanguages.addCommand(new Command("Select"));
        frmLanguages.setCommandListener(al);
        frmLanguages.show();
    }

    /**
     * Views welcome screen with instructions
     */
    public void viewPermissionsWarning() {
        frmPermissionWarning = new Form(Locale.get("Locify"));
        frmPermissionWarning.setLayout(new FlowLayout());
        Label siWarning = new Label(Locale.get("You_need_to_setup_permissions"));
        frmPermissionWarning.addComponent(siWarning);
        btnHowto = new Button(Locale.get("Howto"));
        btnHowto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewPermissionsHelp();
            }
        });
        frmPermissionWarning.addComponent(btnHowto);
        
        frmPermissionWarning.addCommand(Commands.cmdExit);
        frmPermissionWarning.addCommand(Commands.cmdContinue);
        frmPermissionWarning.setCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getCommand() == Commands.cmdExit) {
                    R.getURL().call("locify://exit");
                } else if (evt.getCommand() == Commands.cmdContinue) {
                    start();
                }
            }
        });
        frmPermissionWarning.show();
    }

    /**
     * Views screen for selecting root directory
     */
    private void viewRootSelection(String folder) {
        frmRoots = new Form(Locale.get("Set_root_directory"));
        frmRoots.setLayout(new BorderLayout());

        Vector data = new Vector();
        Enumeration roots = null;
        if (folder.equals("")) {
            roots = R.getFileSystem().getRoots();
        } else {
            roots = R.getFileSystem().getFolders(folder);
        }
        while (roots.hasMoreElements()) {
            data.addElement(folder + (String) roots.nextElement());
        }

        lstRoots = new List(data);
        lstRoots.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setDirectory();
            }
        });

        frmRoots.addComponent(BorderLayout.CENTER, lstRoots);
        frmRoots.addCommand(Commands.cmdSet);
        frmRoots.addCommand(Commands.cmdSelect);
        if (folder.equals("")) {
            frmRoots.addCommand(Commands.cmdExit);
        } else {
            frmRoots.addCommand(Commands.cmdBack);
        }
        frmRoots.setCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String selected = (String) lstRoots.getSelectedItem();
                if (evt.getCommand() == Commands.cmdSet) {
                    setDirectory();
                } else if (evt.getCommand() == Commands.cmdSelect) {
                    if (R.getFileSystem().getFolders(selected).hasMoreElements()) {
                        viewRootSelection(selected);
                    } else {
                        Dialog.show(Locale.get("Warning"), Locale.get("This_folder_does_not_contain_more_folders"), "OK", null);
                    }
                } else if (evt.getCommand() == Commands.cmdBack) {
                    selected = selected.substring(0, selected.lastIndexOf('/'));
                    selected = selected.substring(0, selected.lastIndexOf('/'));
                    selected = selected.substring(0, selected.lastIndexOf('/') + 1);
                    viewRootSelection(selected);
                }
            }
        });

        frmRoots.show();
    }
    
    private void setDirectory() {
//System.out.println("Set");
        String selected = (String) lstRoots.getSelectedItem();
        //#if release
//#     String locifyFolder = "Locify/";
        //#else
        String locifyFolder = "Locify-nightly/";
        //#endif
        if (R.getFileSystem().createRoot(selected + locifyFolder)) {
            saveRoot(selected);
            R.getMidlet().startMIDlet();
        } else {
            Dialog.show(Locale.get("Error"), Locale.get("Access_denied_to_selected_directory"), "OK", null);
        }
    }

    private void viewPermissionsHelp() {
        frmPermissionsHelp = new Form(Locale.get("Howto"));
        frmPermissionsHelp.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        if (Capabilities.isNokia()) {
            frmPermissionsHelp.addComponent(new Label(Locale.get("For_Nokia_S60_3rd_phones")));
            frmPermissionsHelp.addComponent(new Label(Locale.get("Nokia_S60_3rd_text")));
            frmPermissionsHelp.addComponent(new Label(Locale.get("For_Nokia_S40_phones")));
            frmPermissionsHelp.addComponent(new Label(Locale.get("Nokia_S40_text")));
        } else if (Capabilities.isSonyEricsson()) {
            frmPermissionsHelp.addComponent(new Label(Locale.get("For_SonyEricsson_phones")));
            frmPermissionsHelp.addComponent(new Label(Locale.get("SonyEricsson_text")));
            frmPermissionsHelp.addComponent(new Label(Locale.get("For_UIQ_phones")));
            frmPermissionsHelp.addComponent(new Label(Locale.get("UIQ_text")));
        } else {
            frmPermissionsHelp.addComponent(new Label(Locale.get("General_phone_text")));
        }
        frmPermissionsHelp.addComponent(new Label(Locale.get("If_always_allowed_not_possible")));
        frmPermissionsHelp.addComponent(new Label(Locale.get("If_always_allowed_not_possible_text")));
        frmPermissionsHelp.addCommand(Commands.cmdBack);
        frmPermissionsHelp.setCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewPermissionsWarning();
            }
        });
        frmPermissionsHelp.show();
    }
}
