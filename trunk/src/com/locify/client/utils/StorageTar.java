/*
 * StorageTar.java
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
package com.locify.client.utils;

import com.locify.client.data.FileSystem;
import de.enough.polish.util.HashMap;
import de.enough.polish.util.Iterator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * Manages loading and writing files into one TAR file
 * @author Menion
 */
public class StorageTar {

    //String path = "";
    private static FileConnection fileConnection;
    private static InputStream inputStream;
    private static long fileSize;
    private static int dataPosition;
    private static String actualFile;
    private static String lastFile;
    private static int type;

    private static int bufferSize = 25600;
    private static byte[] buffer = new byte[bufferSize];
    private String tarPath;
    private HashMap indexes;
    long modifydate;

    // map variables
    private int mapTileSizeX = 0;
    private int mapTileSizeY = 0;

    private boolean makeIndexes;
    

    /**
     * Cretae default storage that manage tar maps ie.
     * @param tarPath <b>absolute </b> path to file
     */
    public StorageTar(String tarPath) {
        try {
            this.tarPath = tarPath;
            this.indexes = new HashMap();
            this.makeIndexes = true;
            //loadMapTarVariables();
            if (makeIndexes) {
                indexFile();
                getMapTileSize();
                //saveMapTarVariables();
            }
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, ex.getMessage(), "StorageTar constructor");
        }
    }

    public String getTarFile() {
        return tarPath;
    }

    public int getFilePosition(String fileName) {
        if (indexes != null) {
            Integer index = (Integer) indexes.get(fileName);
//System.out.println("\n  StorageTar.getFilePosition(): " + fileName + " index: " + index);
            if (index != null)
                return index.intValue();
            else
                return -1;
        }
        return -1;
    }
    
    private void resetData() {
        inputStream = null;
        fileConnection = null;
        fileSize = 0;
        dataPosition = 0;
        actualFile = "";
        lastFile = "";
        type = '5';
    }

    public void indexFile() {
        try {
            resetData();
long time = System.currentTimeMillis();
Logger.debug("StorageTar.indexFile() indexing... (" + tarPath + ")");
            fileConnection = (FileConnection) Connector.open(tarPath, Connector.READ);
            inputStream = fileConnection.openInputStream();

            long l_posunuti = 0;
            byte l_filenamebytes[] = new byte[256];
            
            while (true) {
                l_posunuti += 512 * (long) Math.ceil((float) fileSize / 512);
                inputStream.skip(l_posunuti);
                dataPosition += 512 * (long) Math.ceil((float) fileSize / 512);

                // 0 100 File name
                inputStream.read(l_filenamebytes, 0, 100);
                actualFile = new String(l_filenamebytes, 0, 100).trim();
//System.out.println("!!! actualFile: " + actualfile);
                if (actualFile.equals("")) {
                    break;
                }

                if (!lastFile.equals(actualFile)) {
//System.out.println("\nIndexed: " + actualFile + " pos: " + dataPosition);
                    indexes.put(actualFile, new Integer(dataPosition));
                    lastFile = actualFile;
                } else {
                    indexes.clear();
                    return;
                }

                // 100 8 File mode
                inputStream.read(l_filenamebytes, 0, 8);
                // 108 8 Owner user ID
                inputStream.read(l_filenamebytes, 0, 8);
                // 116 8 Group user ID
                inputStream.read(l_filenamebytes, 0, 8);
                // 124 12 File size in bytes
                inputStream.read(l_filenamebytes, 0, 12);
                fileSize = Oct2Long(l_filenamebytes, 11);
                // 136 12 Last modification time
                inputStream.read(l_filenamebytes, 0, 12);
                // 148 8 Check sum for header block
                inputStream.read(l_filenamebytes, 0, 8);
                // 156 1 Link indicator
                inputStream.read(l_filenamebytes, 0, 1);
                type = (char) l_filenamebytes[0];
                // 157 100 Name of linked file
                inputStream.read(l_filenamebytes, 0, 100);
                dataPosition += 256;
                // Test, zda-li nema rozsirenou hlavicku
                inputStream.read(l_filenamebytes, 0, 6);
                if (l_filenamebytes[0] == 'u' && l_filenamebytes[1] == 's' && l_filenamebytes[2] == 't' && l_filenamebytes[3] == 'a' && l_filenamebytes[4] == 'r') {
                    dataPosition += 256;
                    inputStream.skip(249);
                    l_posunuti = 0;
                } else {
                    dataPosition -= 6;
                    l_posunuti = -6;
                }
            }

            inputStream.close();
            inputStream = null;
            fileConnection = null;
Logger.debug("StorageTar.indexFile() end after " + (System.currentTimeMillis() - time) + "ms");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } finally {
            makeIndexes = false;
        }
    }

    // unimproved function for loading file from not-indexed file !!!!
//    public String indexFile(String fileName) {
//        try {
//            resetData();
////Logger.log("FileName: " + tarPath);
//            fileConnection = (FileConnection) Connector.open(tarPath, Connector.READ);
//            inputStream = fileConnection.openInputStream();
//
//            long l_posunuti = 0;
//            byte l_filenamebytes[] = new byte[256];
//
//            while (!actualFile.equals(fileName) || makeIndexes) {
//                l_posunuti += 512 * (long) Math.ceil((float) fileSize / 512);
//                inputStream.skip(l_posunuti);
//                dataPosition += 512 * (long) Math.ceil((float) fileSize / 512);
//
//                // 0 100 File name
//                inputStream.read(l_filenamebytes, 0, 100);
//                actualFile = new String(l_filenamebytes, 0, 100).trim();
////System.out.println("!!! actualFile: " + actualfile);
//                if (actualFile.equals("")) {
//                    break;
//                } else if (actualFile.equals(fileName) && !makeIndexes) {
//                    return new String(loadFile(tarPath, (int) dataPosition));
//                }
//
//                if (makeIndexes) {
//                    if (!lastFile.equals(actualFile)) {
////System.out.println("\nIndexed: " + actualFile + " pos: " + dataPosition);
//                        indexes.put(actualFile, new Integer(dataPosition));
//                        lastFile = actualFile;
//                    } else {
//
//                    }
//                }
//
//                // 100 8 File mode
//                inputStream.read(l_filenamebytes, 0, 8);
//                // 108 8 Owner user ID
//                inputStream.read(l_filenamebytes, 0, 8);
//                // 116 8 Group user ID
//                inputStream.read(l_filenamebytes, 0, 8);
//                // 124 12 File size in bytes
//                inputStream.read(l_filenamebytes, 0, 12);
//                fileSize = Oct2Long(l_filenamebytes, 11);
////System.out.println("FileSize: " + fileSize);
//                // 136 12 Last modification time
//                inputStream.read(l_filenamebytes, 0, 12);
//                // 148 8 Check sum for header block
//                inputStream.read(l_filenamebytes, 0, 8);
//                // 156 1 Link indicator
//                inputStream.read(l_filenamebytes, 0, 1);
//                type = (char) l_filenamebytes[0];
//                // 157 100 Name of linked file
//                inputStream.read(l_filenamebytes, 0, 100);
//                dataPosition += 256;
//                // Test, zda-li nema rozsirenou hlavicku
//                inputStream.read(l_filenamebytes, 0, 6);
//                if (l_filenamebytes[0] == 'u' && l_filenamebytes[1] == 's' && l_filenamebytes[2] == 't' && l_filenamebytes[3] == 'a' && l_filenamebytes[4] == 'r') {
//                    dataPosition += 256;
//                    inputStream.skip(249);
//                    l_posunuti = 0;
//                } else {
//                    dataPosition -= 6;
//                    l_posunuti = -6;
//                }
//            }
//
//            inputStream.close();
//            inputStream = null;
//            fileConnection = null;
//
//            return null;
//        } catch (NumberFormatException e) {
//            R.getErrorScreen().view(e, "StorageTar.loadFile(tarArchive, fileName) - nfEx", "tarArchive: " + tarPath + " fileName: " + fileName);
//            return null;
//        } catch (IOException e) {
//            R.getErrorScreen().view(e, "StorageTar.loadFile(tarArchive, fileName) - IOEx", "tarArchive: " + tarPath + " fileName: " + fileName);
//            return null;
//        } finally {
//            makeIndexes = false;
//        }
//    }

    public static byte[] loadFile(String tarArchive, int fileSizeFrom) {
long time = System.currentTimeMillis();
Logger.warning("\nStorageTar.loadFile() tarArchive: " + tarArchive + " fileSizeFrom: " + fileSizeFrom);
        try {
            fileConnection = (FileConnection) Connector.open(tarArchive, Connector.READ);
            inputStream = fileConnection.openInputStream();
            
            byte[] data = new byte[256];
Logger.warning(" step 1 " + (System.currentTimeMillis() - time) + "ms");
            int actualPos = 0;
            while (true) {
                if ((actualPos + bufferSize) < fileSizeFrom) {
                    inputStream.read(buffer);
                } else {
                    inputStream.read(buffer, 0, fileSizeFrom - actualPos);
                    break;
                }
                actualPos += bufferSize;
            }
            //inputStream.skip(fileSizeFrom);
Logger.warning(" step 2 " + (System.currentTimeMillis() - time) + "ms");
            // trash
            inputStream.read(data, 0, 124);
            // file size
            inputStream.read(data, 0, 12);
            fileSize = Oct2Long(data, 11);

            // trash
            inputStream.read(data, 0, 121);
            
            dataPosition += 256;
            // Test, zda-li nema rozsirenou hlavicku
            inputStream.read(data, 0, 6);
            if (data[0] == 'u' && data[1] == 's' && data[2] == 't' && data[3] == 'a' && data[4] == 'r') {
                dataPosition += 256;
                inputStream.skip(249);
            } else {
                dataPosition -= 6;
            }
Logger.warning(" step 3 " + (System.currentTimeMillis() - time) + "ms");
            data = new byte[(int) fileSize];
            inputStream.read(data, 0,(int) fileSize);

            inputStream.close();
            inputStream = null;
            fileConnection = null;
Logger.warning("End ... by " + (System.currentTimeMillis() - time) + "ms");
            return data;
        } catch (IOException ex) {
            R.getErrorScreen().view(ex, "StorageTar.loadFile(tarArchive, fileSize, filePosition) - IOEx", null);
            return null;
        }
    }

    public Object[] getKeys() {
        if (!indexes.isEmpty()) {
            return indexes.keys();
        }
        return null;
    }

    private static long Oct2Long(byte[] a_data, int a_len) {
        long l_cislo = 0;
        for (int l_idx = 0; l_idx < a_len; l_idx++) {
            l_cislo = 8 * l_cislo + a_data[l_idx] - '0';
        }
        return l_cislo;
    }


    // functions for managings cached storageTar configs

    public int[] getMapTileSize() {
        if (mapTileSizeX == 0 || mapTileSizeY == 0) {
            Object[] tarKeys = getKeys();
            //Arrays.sort(tarKeys);
            if (tarKeys != null) {
                String key;
                int startLength = 0;
                mapTileSizeX = Integer.MAX_VALUE;
                mapTileSizeY = Integer.MAX_VALUE;
//System.out.println("tarKeys.size() " + tarKeys.length);
                for (int i = 0; i < tarKeys.length; i++) {
                    key = (String) tarKeys[i];
//System.out.println("KEY: " + key);
                    if (!(key.startsWith("set/") && key.lastIndexOf('_') != -1))
                        continue;

                    if (startLength == 0) {
                        String temp = key.substring(0, key.lastIndexOf('_'));
                        int l = temp.lastIndexOf('_');
                        if (l != -1) {
                            temp = temp.substring(0, l + 1);
                            startLength = temp.length();
                        }
                    }
                    key = key.substring(startLength);
//System.out.println("KEY: " + key);
                    int x = Integer.parseInt(key.substring(0, key.lastIndexOf('_')));
                    int y = Integer.parseInt(key.substring(key.lastIndexOf('_') + 1, key.lastIndexOf('.')));
//System.out.println("x " + x + " y " + y);
                    if (x < mapTileSizeX && x != 0)
                        mapTileSizeX = x;
                    if (y < mapTileSizeY && y != 0)
                        mapTileSizeY = y;
                }
            }
        }
        return new int[]{mapTileSizeX, mapTileSizeY};
    }

    private String getFileName() {
        // locify map tar
        return tarPath.substring( tarPath.lastIndexOf('/') + 1, tarPath.lastIndexOf('.')) + ".lmt";
    }

    public void saveMapTarVariables() throws IOException {
        String data = "";
        
        fileConnection = (FileConnection) Connector.open(tarPath, Connector.READ);
        data += fileConnection.fileSize() + ";" + mapTileSizeX + ";" + mapTileSizeY + "\n";
        
        Object key, value;
        Iterator iter = indexes.keysIterator();
        while (iter.hasNext()) {
            key = iter.next();
            value = indexes.get(key);

            data += String.valueOf(key) + ";" + String.valueOf(value) + "\n";
        }

        R.getFileSystem().saveString(FileSystem.CACHE_FOLDER + getFileName(), data);
        //System.out.println("SaveMapTarVariables() completed");
    }

    public void loadMapTarVariables() throws IOException {
        if (R.getFileSystem().exists(FileSystem.CACHE_FOLDER + getFileName())) {
            String data = R.getFileSystem().loadString(FileSystem.CACHE_FOLDER + getFileName());

            Vector lines = StringTokenizer.getVector(data, "\n");
            Vector token = StringTokenizer.getVector(String.valueOf(lines.elementAt(0)), ";");
            long size = Long.parseLong(String.valueOf(token.elementAt(0)));
            
            fileConnection = (FileConnection) Connector.open(tarPath, Connector.READ);
            if (fileConnection.fileSize() == size) {
                mapTileSizeX = Integer.parseInt(String.valueOf(token.elementAt(1)));
                mapTileSizeY = Integer.parseInt(String.valueOf(token.elementAt(2)));
                indexes.clear();
                for (int i = 1; i < lines.size(); i++) {
                    token = StringTokenizer.getVector(String.valueOf(lines.elementAt(i)), ";");
                    if (token.size() == 2)
                        indexes.put(String.valueOf(token.elementAt(0)),
                                Integer.valueOf(String.valueOf(token.elementAt(1))));
                }
                makeIndexes = false;
            }
        }
        //System.out.println("LoadMapTarVariables() " + mapTileSizeX + " " + mapTileSizeY + " " + indexes.size());
    }
}
