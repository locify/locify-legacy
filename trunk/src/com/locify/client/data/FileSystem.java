/*
 * FileSystem.java
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

import com.locify.client.utils.Capabilities;
import de.enough.polish.io.Serializer;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import com.locify.client.utils.R;
import com.locify.client.utils.Sha1;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

/**
 * This class takes care about storing files on memory card or phone's memory<br/>
 * Location in the filesystem can be:<br/>
 * <ul>
 *   <li>ICONS_FOLDER</li>
 *   <li>CACHE_FOLDER</li>
 *   <li>WAYPOINTS_FOLDER</li>
 *   <li>ROUTES_FOLDER</li>
 *   <li>SETTINGS_FOLDER</li>
 * </ul>
 * @author Destil
 */
public class FileSystem {

    //folders definition
    public static final String SETTINGS_FOLDER = "settings/";
    public static final String ICONS_FOLDER = SETTINGS_FOLDER + "icons/";
    public static final String CACHE_FOLDER = SETTINGS_FOLDER + "cache/";
    public static final String AUDIO_FOLDER = SETTINGS_FOLDER + "audio/";
    public static final String FILES_FOLDER = "files/";
    public static final String LOG_FOLDER = "log/";
    public static final String MAP_FOLDER = "maps/";
    //files definition
    public static final String SETTINGS_FILE = SETTINGS_FOLDER + "mainSettings.xml";
    public static final String COOKIES_FILE = SETTINGS_FOLDER + "cookies.xml";
    public static final String DELETED_FILE = SETTINGS_FOLDER + "deleted.xml";
    public static final String SERVICES_FILE = SETTINGS_FOLDER + "services.xml";
    public static final String MAINSCREEN_FILE = SETTINGS_FOLDER + "mainScreen.xml";
    public static final String SERVICE_SETTINGS_FILE = SETTINGS_FOLDER + "serviceSettings.xml";
    /** file for saving running route serializated data before midlet is killed */
    public static final String RUNNING_ROUTE_VARIABLES = CACHE_FOLDER + "runningRoute.lcf";
    public static final String RUNNING_TEMP_ROUTE = FILES_FOLDER + "runningRoute.tmp";
    public static String ROOT = null;

    public FileSystem() {
    }

    /**
     * Try to memory card and gives user best saving for saved files
     * @return default root directory
     */
    public String getDefaultRoot() {
        try {
            //memory card or phone's memory?
            Enumeration roots = FileSystemRegistry.listRoots();
            String directory = "";
            while (roots.hasMoreElements()) {
                directory = (String) roots.nextElement();
                if (directory.equalsIgnoreCase("e:/")) {
                    ROOT = directory;
                    break;
                }
            }
            if (ROOT == null) {
                ROOT = directory;
            }
            return ROOT;
        } catch (SecurityException e) {
            return "";
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.getDefaultRoot", null);
            return "";
        }
    }

    /**
     * Return all root directories divided by \n
     * @return all root directories
     */
    public Enumeration getRoots() {
        try {
            return FileSystemRegistry.listRoots();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.getRoots", null);
            return null;
        }
    }

    public Enumeration getFolders(String folder) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + folder);
            if (!fileConnection.exists()) {
                return null;
            }

            Enumeration files = fileConnection.list();
            Vector folders = new Vector();
            while (files.hasMoreElements()) {
                String file = (String) files.nextElement();
                if (file.endsWith("/")) {
                    folders.addElement(file);
                }
            }
            fileConnection.close();
            return folders.elements();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.getFolders", folder);
            return null;
        }
    }

    public Enumeration getFiles(String folder) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + folder);
            if (!fileConnection.exists()) {
                return null;
            }

            return fileConnection.list();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.getFiles", folder);
            return null;
        }
    }

    public synchronized void writeTestData(String root) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + root);
            fileConnection.close();
        } catch (Exception e) {
        }
    }

    /**
     * Creates default root directory
     * @return saving successfull
     */
    public synchronized boolean createDefaultRoot() {
        try {
            //defining of locify folders for various phones
            //#if release
//#             String locifyFolder = "Locify/";
            //#else
            String locifyFolder = "Locify-nightly/";
            //#endif
            Vector tryOuts = new Vector();
            tryOuts.addElement(locifyFolder);
            //#if polish.Vendor == BlackBerry
            tryOuts.addElement("home/user/" + locifyFolder);
            //#endif
            if (Capabilities.isSonyEricsson()) {
                tryOuts.addElement("other/" + locifyFolder);
            }
            if (Capabilities.isNokia()) {
                tryOuts.addElement("data/Images/" + locifyFolder);
            }

            //ordering roots to prefer memory cards
            Vector firstRoots = new Vector();
            Vector lastRoots = new Vector();
            Enumeration roots = FileSystemRegistry.listRoots();
            while (roots.hasMoreElements()) {
                String root = (String) roots.nextElement();
                if (root.equalsIgnoreCase("e:/") || root.indexOf("Card") != -1 || root.indexOf("card") != -1 || root.indexOf("SD") != -1 || root.indexOf("MMC") != -1) {
                    firstRoots.addElement(root);
                } else {
                    lastRoots.addElement(root);
                }
            }
            Vector orderedRoots = new Vector();
            for (int i = 0; i < firstRoots.size(); i++) {
                orderedRoots.addElement(firstRoots.elementAt(i));
            }
            for (int i = 0; i < lastRoots.size(); i++) {
                orderedRoots.addElement(lastRoots.elementAt(i));
            }

            //trying to create locify folder
            for (int i = 0; i < orderedRoots.size(); i++) {
                String root = (String) orderedRoots.elementAt(i);
                for (int j = 0; j < tryOuts.size(); j++) {
                    String suffix = (String) tryOuts.elementAt(j);
                    try {
                        FileConnection fileConnection = (FileConnection) Connector.open("file:///" + root + suffix);
                        if (!fileConnection.exists()) {
                            fileConnection.mkdir();
                        }
                        fileConnection.close();
                        ROOT = root + suffix;
                        R.getFirstRun().saveRoot(ROOT);
                        return true;
                    } catch (Exception e) {
                    }
                }
            }
            return false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.createDefaultRoot", null);
            return false;
        }
    }

    /**
     * Creates root directory
     * @param root root to save files
     * @return saving successfull
     */
    public synchronized boolean createRoot(String root) {
        try {
            FileSystem.ROOT = root;
            //create Locify folder if not exist
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT);
            if (!fileConnection.exists()) {
                fileConnection.mkdir();
            }
            fileConnection.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns enumeration of files in given folder. If folder does not exists, returns null.
     * @param folder folder to search
     * @param pattern eg. '*.kml' for only kml files
     * @return string enumeration of files
     */
    public synchronized Enumeration listFiles(String folder, String pattern) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT + folder);
            if (!fileConnection.exists()) {
                return null;
            }

            Enumeration files;
            if (pattern != null) {
                files = fileConnection.list(pattern, false);
            } else {
                files = fileConnection.list();
            }
            fileConnection.close();
            return files;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.listFiles", folder);
            return null;
        }
    }

    /**
     * Deletes file from filesystem
     * @param fileName file name
     */
    public synchronized void delete(String fileName) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT + fileName);
            if (fileConnection.exists()) {
                fileConnection.delete();
            }
            fileConnection.close();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.delete", fileName);
        }
    }

    /**
     * Delete all files in specified folder
     * @param folder folder to be cleared out
     */
    public synchronized void deleteAll(String folder) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT + folder);
            if (fileConnection.exists() && fileConnection.isDirectory()) {
                Enumeration files = fileConnection.list();
                fileConnection.close();
                while (files.hasMoreElements()) {
                    delete(folder + (String) files.nextElement());
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.deleteAll", folder);
        }
    }

    /**
     * Check if file exists
     * @param fileName file name including folders
     * @return if file exists
     */
    public synchronized boolean exists(String fileName) {
        try {
            FileConnection fileConnection = null;
            if (fileName.startsWith("file://")) {
                fileConnection = (FileConnection) Connector.open(fileName);
            } else {
                fileConnection = (FileConnection) Connector.open("file:///" + ROOT + fileName);
            }
            boolean exists = fileConnection.exists();
            fileConnection.close();
            return exists;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.exists", ROOT + fileName);
            return false;
        }
    }

    /**
     * Renames files
     * @param oldFile
     * @param newName
     */
    public synchronized void renameFile(String oldFile, String newName) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT + oldFile);
            if (fileConnection.exists()) {
                fileConnection.rename(newName);
            }
            fileConnection.close();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.renameFile()", oldFile + " to " + newName);
        }
    }

    /**
     * Writes binary data into file
     * @param fileName file name
     * @param data binary data
     */
    public void saveBytes(String fileName, byte[] data) {
        try {
            new DataWriter(fileName, data, -1);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.saveBytes", fileName);
        }
    }

    /**
     * Reads binary data from file
     * @param fileName file name
     * @return binary data
     */
    public synchronized byte[] loadBytes(String fileName) {
        try {
            //get the stream
            FileConnection fileConnection = null;
            fileConnection = (FileConnection) Connector.open("file:///" + ROOT + fileName);
            if (!fileConnection.exists()) {
                return null;
            }
            DataInputStream dis = fileConnection.openDataInputStream();
            if (dis != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int ch;
                while ((ch = dis.read()) != -1) {
                    baos.write(ch);
                }
                byte[] data = baos.toByteArray();
                baos.close();
                dis.close();
                fileConnection.close();
                return data;
            } else {
                fileConnection.close();
                return null;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.loadBytes", fileName);
            return null;
        }
    }

    /**
     * Loads object from filesystem and deserializes it
     * @param fileName file name
     * @return deserialized object
     */
    public synchronized Object loadObject(String fileName) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT + fileName);
            if (!fileConnection.exists()) {
                return null;
            }
            DataInputStream dis = fileConnection.openDataInputStream();
            Object object = Serializer.deserialize(dis);
            dis.close();
            fileConnection.close();
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves string into file. It saves string in regular UTF-8 encoding, not J2ME modified
     * @param fileName file name
     * @param string string to be written
     */
    public void saveString(String fileName, String string) {
        try {
            if (!fileName.equals("files/.kml")) {
                byte[] byteArr = UTF8.encode(string);
                new DataWriter(fileName, byteArr, -1);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.saveString()", fileName);
        }
    }

    public void saveStringToBytePos(String fileName, String string, long bytePos) {
        try {
            new DataWriter(fileName, UTF8.encode(string), bytePos);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.saveStringToBytePos()", fileName);
        }
    }
    /**
     * Saves string into end of file.
     * @param fileName file name
     * @param string string to be written
     */
    public void saveStringToEof(String fileName, String string) {
        try {
            new DataWriter(fileName, UTF8.encode(string), -2);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.saveStringToEof()", fileName);
        }
    }

    /**
     * Loads utf-8 string from file and changes it to j2me modified utf-8 string
     * @param fileName file name
     * @return content as string
     */
    public String loadString(String fileName) {
        try {
            byte[] byteArr = loadBytes(fileName);
            if (byteArr == null) {
                return null;
            } else {
                return UTF8.decode(byteArr, 0, byteArr.length);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.loadString", fileName);
            return null;
        }

    }

    /**
     * Get last modified value of file in filesystem. Return -1 if file does not exist
     * @param fileName file name
     * @return unix timestamp
     */
    public synchronized long getTimestamp(String fileName) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT + fileName);
            if (!fileConnection.exists()) {
                return -1;
            }
            long lastModified = fileConnection.lastModified() / 1000;
            fileConnection.close();
            return lastModified;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.getTimestamp", fileName);
            return -1;
        }
    }

    /**
     * Returns file size of some file (root need to be specified)
     * @param file
     * @return
     */
    public synchronized long getFileSize(String file) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + file);
            if (!fileConnection.exists()) {
                return -1;
            }
            long size = fileConnection.fileSize();
            fileConnection.close();
            return size;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.getFileSize", file);
            return -1;
        }
    }

    /**
     * Hashes filename into filesystem-safe form
     * @param fileName file name
     * @return hashed filename
     */
    public static String hashFileName(String fileName) {
        fileName = Sha1.encode(fileName);
        fileName = Utils.replaceString(fileName, "/", "");
        fileName = Utils.replaceString(fileName, "=", "");
        fileName = Utils.replaceString(fileName, "+", "");
        return fileName;
    }

    /**
     * Checks folders in given fileName and creates them if neccessary
     * @param fileName file name
     */
    private void checkFolders(String fileName) {
        try {
            String[] folders = StringTokenizer.getArray(fileName, "/");
            String folder = "";
            for (int i = 0; i <
                    folders.length - 1; i++) {
                folder += folders[i] + "/";
                FileConnection fileConnection =
                        (FileConnection) Connector.open("file:///" + ROOT + folder);
                if (!fileConnection.exists()) {
                    fileConnection.mkdir();
                }

                fileConnection.close();
            }

        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystem.checkFolders", fileName);
        }
    }

    public static String getLastChar(String filename) {
        try {
            //create if not exist
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + filename);
            if (!fileConnection.exists()) {
                return null;
            }

            DataInputStream dis = fileConnection.openDataInputStream();
            dis.skip(dis.available() - 5);
            int d;
            int lastD = 0;
            while (-1 != (d = dis.read())) {
                lastD = d;
            }

            dis.close();
            fileConnection.close();
            return String.valueOf((char) lastD);
        } catch (IOException ex) {
            R.getErrorScreen().view(ex, "FileSystem.getLastChar", filename);
            return null;
        }
    }

    /**
     * Thread for writing all data
     */
    class DataWriter extends Thread {

        private String fileToWrite;
        private byte[] dataToWrite;
        private long bytePos;

        /**
         * Save bytes to file.
         * @param fileToWrite
         * @param dataToWrite
         * @param bytePos position at write data or <br>
         * -1 if write as whole new file<br>
         * -2 if write at the end of file
         */
        public DataWriter(String fileToWrite, byte[] dataToWrite, long bytePos) {
            this.fileToWrite = fileToWrite;
            this.dataToWrite = dataToWrite;
            this.bytePos = bytePos;
            this.start();
        }

        public void run() {
            synchronized (R.getFileSystem()) {
                try {
                    checkFolders(fileToWrite);
                    //create if not exist
                    FileConnection fileConnection = (FileConnection) Connector.open("file:///" + ROOT + fileToWrite);
                    if (!fileConnection.exists()) {
                        fileConnection.create();
                    }

                    if (dataToWrite != null) {
                        OutputStream os;
                        if (bytePos == -1) {
                            fileConnection.truncate(0);
                            os = fileConnection.openOutputStream();
                        } else if (bytePos == -2) {
                            os = fileConnection.openOutputStream(fileConnection.fileSize());
                        } else {
                            os = fileConnection.openOutputStream(bytePos);
                        }

                        if (bytePos == -1)
                            os.write(dataToWrite);
                        else {
                            OutputStreamWriter osw = new OutputStreamWriter(os);
                            String text = new String(dataToWrite);
                            osw.write(text);
                            osw.close();
                            os.close();
                        }
                    }

                    fileConnection.close();
                } catch (Exception e) {
                    R.getErrorScreen().view(e, "FileSystem.DataWriter.run", fileToWrite);
                }
            }
        }
    }

    public String getRoot() {
        return ROOT;
    }
}

