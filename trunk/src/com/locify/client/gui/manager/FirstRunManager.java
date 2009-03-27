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
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.Enumeration;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;

/**
 * This class manages first run and loading of application
 * @author Destil
 */
public class FirstRunManager implements CommandListener, ItemCommandListener {

    private Form frmPermissionWarning;
    private Form frmPermissionsHelp;
    private StringItem btnHowto;
    private Command cmdContinue;
    private Command cmdSet;
    private Command cmdExit;
    private Command cmdHowto;
    private List lstLanguages;
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
            Locale.loadTranslations("/" + chosenLanguage + ".loc");
            cmdContinue = new Command(Locale.get("Continue"), Command.SCREEN, 2);
            cmdSet = new Command(Locale.get("Set"), Command.SCREEN, 1);
            cmdExit = new Command(Locale.get("Exit"), Command.EXIT, 3);
            cmdHowto = new Command(Locale.get("Howto"), Command.ITEM, 1);
            dis.close();
            recordStore.closeRecordStore();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FirstRunManager.loadLanguage", chosenLanguage);
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
        lstLanguages = new List(Locale.get("Choose_language"), List.IMPLICIT);
        for (int i = 0; i < R.getSettings().locales.length; i++) {
            lstLanguages.append(R.getSettings().languageNames[i], null);
        }
        lstLanguages.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, lstLanguages);
    }

    /**
     * Views welcome screen with instructions
     */
    public void viewPermissionsWarning() {
        frmPermissionWarning = new Form(Locale.get("Locify"));
        //#style browserTextBold
        StringItem siWarning = new StringItem(null, Locale.get("You_need_to_setup_permissions"));
        frmPermissionWarning.append(siWarning);
        btnHowto = new StringItem(null, Locale.get("Howto"), StringItem.BUTTON);
        btnHowto.setDefaultCommand(cmdHowto);
        btnHowto.setItemCommandListener(this);
        frmPermissionWarning.append(btnHowto);
        frmPermissionWarning.addCommand(cmdContinue);
        frmPermissionWarning.addCommand(cmdExit);
        frmPermissionWarning.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmPermissionWarning);
    }

    /**
     * Views screen for selecting root directory
     */
    private void viewRootSelection(String folder) {
        lstRoots = new List(Locale.get("Set_root_directory"), List.IMPLICIT);
        Enumeration roots = null;
        if (folder.equals("")) {
            roots = R.getFileSystem().getRoots();
        } else {
            roots = R.getFileSystem().getFolders(folder);
        }
        while (roots.hasMoreElements()) {
            lstRoots.append(folder + (String) roots.nextElement(), null);
        }
        lstRoots.addCommand(cmdSet);
        if (folder.equals("")) {
            lstRoots.addCommand(cmdExit);
        } else {
            lstRoots.addCommand(Commands.cmdBack);
        }
        lstRoots.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, lstRoots);
    }

    private void viewPermissionsHelp() {
        frmPermissionsHelp = new Form(Locale.get("Howto"));
        if (Capabilities.isNokia()) {
            StringItem siNokiaS60 = new StringItem(Locale.get("For_Nokia_S60_3rd_phones"), Locale.get("Nokia_S60_3rd_text"));
            frmPermissionsHelp.append(siNokiaS60);
            StringItem siNokiaS40 = new StringItem(Locale.get("For_Nokia_S40_phones"), Locale.get("Nokia_S40_text"));
            frmPermissionsHelp.append(siNokiaS40);
        } else if (Capabilities.isSonyEricsson()) {
            StringItem siSE = new StringItem(Locale.get("For_SonyEricsson_phones"), Locale.get("SonyEricsson_text"));
            frmPermissionsHelp.append(siSE);
            StringItem siUIQ = new StringItem(Locale.get("For_UIQ_phones"), Locale.get("UIQ_text"));
            frmPermissionsHelp.append(siUIQ);
        } else {
            StringItem siGeneral = new StringItem(null, Locale.get("General_phone_text"));
            frmPermissionsHelp.append(siGeneral);
        }
        StringItem siNotPossible = new StringItem(Locale.get("If_always_allowed_not_possible"), Locale.get("If_always_allowed_not_possible_text"));
        frmPermissionsHelp.append(siNotPossible);
        frmPermissionsHelp.addCommand(Commands.cmdBack);
        frmPermissionsHelp.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmPermissionsHelp);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdExit) {
            R.getMidlet().switchDisplayable(null, null);
            R.getMidlet().destroyApp(true);
            R.getMidlet().notifyDestroyed();
        } else if (d == lstLanguages) {
            if (c == List.SELECT_COMMAND) {
                chosenLanguage = R.getSettings().locales[lstLanguages.getSelectedIndex()];
                saveLanguage();
                start();
            }
        } else if (d == frmPermissionWarning) {
            if (c == cmdContinue) {
                start();
            }
        } else if (d == lstRoots) {
            String selected = lstRoots.getString(lstRoots.getSelectedIndex());
            if (c == cmdSet) {
                //#if release
//#             String locifyFolder = "Locify/";
                //#else
                String locifyFolder = "Locify-nightly/";
                //#endif
                if (R.getFileSystem().createRoot(selected + locifyFolder)) {
                    saveRoot(selected);
                    R.getMidlet().startMIDlet();
                } else {
                    Alert alert = new Alert(Locale.get("Error"), Locale.get("Access_denied_to_selected_directory"), null, AlertType.ERROR);
                    R.getMidlet().switchDisplayable(alert, lstRoots);
                }
            } else if (c == List.SELECT_COMMAND) {
                if (R.getFileSystem().getFolders(selected).hasMoreElements()) {
                    viewRootSelection(selected);
                } else {
                    Alert alert = new Alert(Locale.get("Warning"), Locale.get("This_folder_does_not_contain_more_folders"), null, AlertType.WARNING);
                    R.getMidlet().switchDisplayable(alert, lstRoots);
                }
            } else if (c == Commands.cmdBack) {
                selected = selected.substring(0, selected.lastIndexOf('/'));
                selected = selected.substring(0, selected.lastIndexOf('/'));
                selected = selected.substring(0, selected.lastIndexOf('/') + 1);
                viewRootSelection(selected);
            }
        } else if (d == frmPermissionsHelp) {
            if (c == Commands.cmdBack) {
                viewPermissionsWarning();
            }
        }
    }

    public void commandAction(Command c, Item i) {
        if (c == cmdHowto) {
            viewPermissionsHelp();
        }
    }
}
