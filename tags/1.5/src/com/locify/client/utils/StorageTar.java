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

import com.locify.client.maps.fileMaps.FileMapManager;
import com.locify.client.utils.math.LMath;
import de.enough.polish.util.Arrays;
import de.enough.polish.util.Comparator;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordComparator;

/**
 * Manages loading and writing files into one TAR file
 * @author Menion
 */
public class StorageTar {

    // temp variables
    private static FileConnection fileConnection;
    private static InputStream inputStream;
    private static int fileSize;
    private static int dataPosition;
    private static String actualFile;
    private static String lastFile;
    private static int type;

    /** size of skip-buffer (in bytes) */
    private static int bufferSize = 1024 * 50;
    private static byte[] buffer = new byte[bufferSize];
    private String tarPath;
    private String imageDir;
    private String imagePrefix;
    private int imageSuffixLength;

    // main config file
    private int configFileStart;
    private int configFileLength;
    private int configFileType;

    private byte[] index;

    /**
     * Cretae default storage that manage tar maps.
     * @param tarPath <b>Absolute</b> path to file.
     * @param imageDir Just a name of image containing directory.
     */
    public StorageTar() {
        this(null);
    }

    public StorageTar(String tarPath) {
        try {
            this.tarPath = tarPath;

            fileConnection = null;
            inputStream = null;
            fileSize = 0;
            dataPosition = 0;
            actualFile = "";
            lastFile = "";
            type = '5';

            configFileStart = -1;
            configFileLength = -1;
            configFileType = -1;

            if (this.tarPath != null) {
                indexFile();
            }
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "StorageTar constructor", tarPath);
        }
    }

    public String getTarFile() {
        return tarPath;
    }

    public TarRecord getConfigFile() {
        if (configFileStart > -1 && configFileLength > -1) {
            return new TarRecord(configFileStart, configFileLength);
        } else {
            return null;
        }
    }

    public int getConfigFileType() {
        return configFileType;
    }

    public String getImageDir() {
        return imageDir;
    }
    
    /**
     * Get TarRecord from choosed file position.
     * @param filePosition Position of file in map directory.
     * @return TarRecord or null if this TarRecord doesn't exist.
     */
    public TarRecord getTarRecord(int filePosition) {
        if (index != null && index.length >= ((filePosition + 1) * 8)) {
//System.out.println("\n  StorageTar.getTarRecord(): " + filePosition);
            return new TarRecord(byteArrayToInt(index, filePosition * 8),
                    byteArrayToInt(index, filePosition * 8 + 4));
        }
        return null;
    }

    private static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    private static void skipBytes(InputStream is, int numOfBytes) {
        try {
//Logger.log("  StorageTar.skipBytes() " + numOfBytes);
            int actualPos = 0;
            if (!Capabilities.isWindowsMobile()) {
                while (true) {
                    if ((actualPos + bufferSize) < numOfBytes) {
                            is.read(buffer);
//Logger.log("Y");
//printByteArray(buffer);
                    } else {
                        is.read(buffer, 0, numOfBytes - actualPos);
//Logger.log("X");
//printByteArray(buffer);
                        break;
                    }
//Logger.log("    skipBytes() ap: " + actualPos);
                    actualPos += bufferSize;
                }
            } else {
                is.skip(numOfBytes);
            }
        } catch (IOException ex) {
            Logger.error("StorageTar.skipBytes() error: " + ex.toString());
        }
    }

    public void indexFile() {
        try {            
//Logger.debug("  StorageTar.indexFile() indexing... (" + tarPath + ") ");
            fileConnection = (FileConnection) Connector.open(tarPath, Connector.READ);
            inputStream = fileConnection.openInputStream();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            Vector imageData = new Vector();
            
            int l_posunuti = 0;
            int readedBytes = 0;
            byte l_filenamebytes[] = new byte[256];
            while (true) {
                l_posunuti += 512 * (long) Math.ceil((float) fileSize / 512);
                skipBytes(inputStream, l_posunuti);
                dataPosition += 512 * (long) Math.ceil((float) fileSize / 512);
                // 0 100 File name
                readedBytes = inputStream.read(l_filenamebytes, 0, 100);
//System.out.println("A " + readedBytes + " " + printByteArray(l_filenamebytes));
                actualFile = new String(l_filenamebytes, 0, 100).trim();
                if (actualFile.equals("") || readedBytes == -1) {
                    break;
                }

                // 100 8 File mode
                readedBytes = inputStream.read(l_filenamebytes, 0, 8);
//printByteArray(l_filenamebytes);
                // 108 8 Owner user ID
                readedBytes = inputStream.read(l_filenamebytes, 0, 8);
//printByteArray(l_filenamebytes);
                // 116 8 Group user ID
                readedBytes = inputStream.read(l_filenamebytes, 0, 8);
//printByteArray(l_filenamebytes);
                // 124 12 File size in bytes
                readedBytes = inputStream.read(l_filenamebytes, 0, 12);
                fileSize = Oct2Int(l_filenamebytes, 11);
//System.out.println("C " + readedBytes + " " + fileSize + " " + printByteArray(l_filenamebytes));
                // 136 12 Last modification time
                readedBytes = inputStream.read(l_filenamebytes, 0, 12);
//printByteArray(l_filenamebytes);
                // 148 8 Check sum for header block
                readedBytes = inputStream.read(l_filenamebytes, 0, 8);
//printByteArray(l_filenamebytes);
                // 156 1 Link indicator
                readedBytes = inputStream.read(l_filenamebytes, 0, 1);
//printByteArray(l_filenamebytes);
                type = (char) l_filenamebytes[0];
                // 157 100 Name of linked file
                readedBytes = inputStream.read(l_filenamebytes, 0, 100);
//printByteArray(l_filenamebytes);
                dataPosition += 256;

                // Test, zda-li nema rozsirenou hlavicku
                readedBytes = inputStream.read(l_filenamebytes, 0, 6);
//System.out.println("B " + readedBytes + " " + printByteArray(l_filenamebytes));
//                if (l_filenamebytes[0] == 'u' && l_filenamebytes[1] == 's' &&
//                        l_filenamebytes[2] == 't' && l_filenamebytes[3] == 'a' && l_filenamebytes[4] == 'r') {
                    dataPosition += 256;
                    skipBytes(inputStream, 249);
                    //inputStream.skip(249);
                    l_posunuti = 0;
//                } else {
//                    dataPosition -= 6;
//                    l_posunuti = -6;
//                }

                actualFile = actualFile.replace('\\', '/');
//System.out.println("  StorageTar.indexFile(): try to index: " + actualFile);
                if (!lastFile.equals(actualFile)) {
                    if (imageDir == null && actualFile.indexOf("/") != -1) {
                        imageDir = actualFile.substring(0, actualFile.indexOf("/") + 1);
                    }
//Logger.log("1");
                    if (imagePrefix == null && imageDir != null && actualFile.startsWith(imageDir) &&
                            actualFile.length() > imageDir.length()) {
                        String imageName = actualFile.substring(imageDir.length());
                        int count = 0;
                        for (int i = 0; i < imageName.length(); i++) {
                            if (imageName.charAt(i) == '_')
                                count++;
                        }

                        if (count == 1) {
                            imagePrefix = imageDir;
                        } else if (count == 2) {
                            imagePrefix = imageDir + imageName.substring(0, imageName.indexOf('_') + 1);
                        } else {
                            count--;
                            int pos = 0;
                            for (int i = 0; i < imageName.length(); i++) {
                                if (imageName.charAt(i) == '_')
                                    pos++;
                                if (count == pos) {
                                    imagePrefix = imageDir + imageName.substring(0, i + 1);
                                    break;
                                }
                            }
                        }
                        imageSuffixLength = imageName.substring(imageName.lastIndexOf('.')).length();
                    }
//Logger.log("2");
                    // is image and not other file or image directory
                    if (imagePrefix != null && actualFile.startsWith(imagePrefix)) {
//Logger.log("3");
                        imageData.addElement(
                                new TempRecord(actualFile.substring(imagePrefix.length(), actualFile.length() - imageSuffixLength),
                                dataPosition, fileSize));
                    // config file
                    } else if (actualFile.indexOf("/") == -1 && 
                            (actualFile.endsWith(".xml") || actualFile.endsWith(".map"))) {
//Logger.log("4");
                        if (actualFile.endsWith(".xml"))
                            configFileType = FileMapManager.CATEGORY_XML;
                        else if (actualFile.endsWith(".map"))
                            configFileType = FileMapManager.CATEGORY_MAP;
                        configFileStart = dataPosition;
                        configFileLength = fileSize;
                    }
                    lastFile = actualFile;
                } else {
                    return;
                }
//Logger.log("5 " + imageData.size());
            }

            TempRecord[] imageArray = new TempRecord[imageData.size()];
            for (int i = 0; i < imageData.size(); i++) {
                imageArray[i] = (TempRecord) imageData.elementAt(i);
            }
            imageData = null;

            Arrays.shellSort(imageArray, new Comparator() {
                public int compare(Object o1, Object o2) {
                    TempRecord tr1 = (TempRecord) o1;
                    TempRecord tr2 = (TempRecord) o2;

                    if (tr1.x < tr2.x) {
                        return RecordComparator.PRECEDES;
                    } else if (tr1.x > tr2.x) {
                        return RecordComparator.FOLLOWS;
                    } else {
                        if (tr1.y < tr2.y) {
                            return RecordComparator.PRECEDES;
                        } else {
                            return RecordComparator.FOLLOWS;
                        }
                    }
                }
            });

            for (int i = 0; i < imageArray.length; i++) {
                TempRecord tr = imageArray[i];
                dos.writeInt(tr.recordStart);
                dos.writeInt(tr.recordLength);
            }
            imageArray = null;

            dos.flush();
            index = baos.toByteArray();

            baos.close();
            baos = null;
            dos.close();
            dos = null;
            inputStream.close();
            inputStream = null;
            fileConnection.close();
            fileConnection = null;
//Logger.debug("  StorageTar.indexFile(): stats: " + index.length);
        } catch (IOException ex) {
            Logger.error(ex.toString());
        } catch (NumberFormatException ex) {
            Logger.error(ex.toString());
        }
    }

    private static String printByteArray(byte[] array) {
        String text = "";
        for (int i = 0; i < array.length; i++) {
            text += "'" + ((char) array[i]) + "' ";
        }
        return text;
    }

    public byte[] loadFile(TarRecord record) {
//long time = System.currentTimeMillis();
//Logger.warning("\nStorageTar.loadFile() tarArchive: " + tarArchive + " fileSizeFrom: " + record.recordStart + " length: " + record.recordLength);
        try {
            fileConnection = (FileConnection) Connector.open(tarPath, Connector.READ);
            inputStream = fileConnection.openInputStream();
            
            byte[] data = new byte[256];
//Logger.warning(" step 1 " + (System.currentTimeMillis() - time) + "ms");
            skipBytes(inputStream, record.recordStart);
            //inputStream.skip(fileSizeFrom);
//Logger.warning(" step 2 " + (System.currentTimeMillis() - time) + "ms");
            data = new byte[(int) record.recordLength];
            inputStream.read(data, 0, record.recordLength);

            inputStream.close();
            inputStream = null;
            fileConnection.close();
            fileConnection = null;
//Logger.warning("End ... by " + (System.currentTimeMillis() - time) + "ms");
            return data;
        } catch (IOException ex) {
            R.getErrorScreen().view(ex, "StorageTar.loadFile(tarArchive, fileSize, filePosition) - IOEx", null);
            return null;
        }
    }

    private static int Oct2Int(byte[] a_data, int a_len) {
        int l_cislo = 0;
        for (int l_idx = 0; l_idx < a_len; l_idx++) {
            if (((char) a_data[l_idx]) == ' ')
                a_data[l_idx] = '0';
            l_cislo = 8 * l_cislo + a_data[l_idx] - '0';
        }
        return l_cislo;
    }

//    private static int Oct2Int(byte[] a_data, int a_len) {
//        int l_cislo = 0;
//        int max = (int) LMath.pow(8, a_len) * 8;
//        for (int l_idx = 0; l_idx < a_len; l_idx++) {
//            l_cislo += ((max / 8) * a_data[l_idx]);
//            l_cislo = 8 * l_cislo + a_data[l_idx] - '0';
//        }
//        return l_cislo;
//    }

    public static StorageTar loadStorageTar(DataInputStream dis) {
        StorageTar storageTar = new StorageTar();
        try {
            storageTar.tarPath = dis.readUTF();
            storageTar.configFileType = dis.readInt();
            storageTar.configFileStart = dis.readInt();
            storageTar.configFileLength = dis.readInt();
            storageTar.imageDir = dis.readUTF();
            int length = dis.readInt();
            storageTar.index = new byte[length];
            dis.read(storageTar.index);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return storageTar;
    }

    public void saveStorageTar(DataOutputStream dos) {
        try {
            dos.writeUTF(tarPath);
            dos.writeInt(configFileType);
            dos.writeInt(configFileStart);
            dos.writeInt(configFileLength);
            dos.writeUTF(imageDir);
            dos.writeInt(index.length);
            dos.write(index, 0, index.length);
        } catch (Exception ex) {
            Logger.error(ex.toString());
        }
    }

    public String toString() {
        return "StorageTar [tarPath: " + tarPath + ", imageDir: " + imageDir + "]";
    }

    public class TarRecord {

        public int recordStart;
        private int recordLength;

        public TarRecord(int recordStart, int recordLength) {
            this.recordStart = recordStart;
            this.recordLength = recordLength;
        }

        public String toString() {
            return "TarRecord start: " + recordStart + " length: " + recordLength;
        }
    }

    private class TempRecord {

        private int x;
        private int y;
        private int recordStart;
        private int recordLength;

        public TempRecord(String name, int recordStart, int recordLength) {
            this.x = GpsUtils.parseInt(name.substring(0, name.indexOf("_")));
            this.y = GpsUtils.parseInt(name.substring(name.indexOf("_") + 1));
            this.recordStart = recordStart;
            this.recordLength = recordLength;
        }

        public String toString() {
            return "TempRecord [" + recordStart + " " + recordLength + " " + x + " " + y + "]";
        }
    }
}
