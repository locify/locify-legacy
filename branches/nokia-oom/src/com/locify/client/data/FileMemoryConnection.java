/*
 * FileMemoryConnection.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.file.FileConnection;

/**
 * No permanent storage implements FileConnection (JSR 75) for testing
 * on phones without a certificate.
 * @author Libor Tvrdik
 */
public class FileMemoryConnection implements FileConnection {

    private boolean directory;
    private boolean create;

    private byte [] data;
    private String name;
    private Vector contentFiles;

    private long lastModify;
    private boolean readable;
    private boolean writeable;
    private boolean hidden;
    private boolean open;
    private boolean unclosable;

    public FileMemoryConnection(String name, boolean unclosable) {

        open = true;
        create = false;
        directory = false;
        
        readable = true;
        writeable = true;

        data = new byte[0];
        lastModify = System.currentTimeMillis();
        contentFiles = new Vector();

        this.name = name;
        this.unclosable = unclosable;
    }

    public boolean isOpen() {
        return open;
    }

    public InputStream openInputStream() throws IOException {

        if( !create || directory || !readable ) {
            throw new IllegalStateException("Cannot read from '" + getName() + "'. Created=" + create + " isDir=" + directory + " canRead=" + readable);
        }

        return new ByteArrayInputStream(data);
    }

    public DataInputStream openDataInputStream() throws IOException {

        if( !create || directory || !readable ) {
            throw new IllegalStateException("Cannot read from '" + getName() + "'. Created=" + create + " isDir=" + directory + " canRead=" + readable);
        }

        return new DataInputStream(this.openDataInputStream());
    }

    public OutputStream openOutputStream() throws IOException {

        if( !create || directory || !writeable ) {
            throw new IllegalStateException("Cannot write to '" + getName() + "'. Created=" + create + " isDir=" + directory + " canWrite=" + writeable);
        }

        return new InternalByteArrayOutputStream(data, data.length);
    }

    public DataOutputStream openDataOutputStream() throws IOException {

        if( !create || directory || !writeable ) {
            throw new IllegalStateException("Cannot write to '" + getName() + "'. Created=" + create + " isDir=" + directory + " canWrite=" + writeable);
        }

        return new DataOutputStream(this.openOutputStream());
    }

    public OutputStream openOutputStream(long offset) throws IOException {

        if( !create || directory || !writeable ) {
            throw new IllegalStateException("Cannot write to '" + getName() + "'. Created=" + create + " isDir=" + directory + " canWrite=" + writeable);
        }

        return new InternalByteArrayOutputStream(data, (int)offset);
    }

    public long totalSize() {
        return data.length;
    }

    public long availableSize() {
        return Runtime.getRuntime().freeMemory();
    }

    public long usedSize() {
        return totalSize();
    }

    public long directorySize(boolean includeSubDirs) throws IOException {
        return totalSize();
    }

    public long fileSize() throws IOException {
        return totalSize();
    }

    public boolean canRead() {
        return readable;
    }

    public boolean canWrite() {
        return writeable;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setReadable(boolean readable) throws IOException {
        this.readable = readable;
    }

    public void setWritable(boolean writeable) throws IOException {
        this.writeable = writeable;
    }

    public void setHidden(boolean hidden) throws IOException {
        this.hidden = hidden;
    }

    public Enumeration list() throws IOException {
        return contentFiles.elements();
    }

    public void addFile(FileMemoryConnection file) throws IOException {
        if( !isDirectory() ) {
            throw new IOException("Can not add file to '" + getURL() + "'; It's not a directory!");
        }
        contentFiles.addElement(file);
    }

    public Enumeration list(String filter, boolean includeHidden) throws IOException {        
        throw new IOException("Not implement list by filter=" + filter);

    }

    public void create() throws IOException {
        create = true;
        directory = false;
        data = new byte[0];
    }

    public void mkdir() throws IOException {
        create = true;
        directory = true;
    }

    public boolean exists() {
        return create;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void delete() throws IOException {
        data = new byte[0];
        create = false;
    }

    public void rename(String newName) throws IOException {
        this.name = newName;
    }

    public void truncate(long len) throws IOException {

        if(len <= 0 ) data = new byte[0];
        if(len < data.length) {
            byte[] oldData = data;
            data = new byte[(int)len];
            System.arraycopy(oldData, 0, data, 0, (int)len);
        }
    }

    public void setFileConnection(String fileName) throws IOException {
        throw new IllegalStateException("Can not switch to '" + fileName + "'");
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return name;
    }

    public String getURL() {
        return name;
    }

    public long lastModified() {
        return lastModify;
    }

    public void close() throws IOException {
        if( !unclosable ) open = false;
    }

    private class InternalByteArrayOutputStream extends ByteArrayOutputStream {

        public InternalByteArrayOutputStream(byte [] originalData, int offset) throws IOException {
            super(offset);
            write(originalData, 0, offset);
        }

        public synchronized void close() throws IOException {
            super.close();
            lastModify = System.currentTimeMillis();
            data = toByteArray();
        }
    }
    
}
