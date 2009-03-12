/*
 * FileMapProviders.java
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

package com.locify.client.maps.fileMaps;

import com.locify.client.data.FileSystem;
import com.locify.client.utils.R;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Lists all offline map providers
 * @author menion
 */
public class FileMapProviders {

    private Vector providersName;
    private Vector providersPath;
    
    public FileMapProviders() {
        //addProvider("Invex PLANstudio", "http://services.locify.com/invexgame/maps/invex_digitex.xml", true);
        //addProvider("Brno SHOCard", "http://services.locify.com/invexgame/maps/brno_shocart.xml", false);
        //addProvider("Brno SHOCard (tar)", "brno_shocard.tar", false);
        //addProvider("Invex PLANstudio (tar)", "invex_digitex.tar", true);
        //addProvider("praha trekBuddy (tar)", "praha.tar", true);
        //addProvider("Brno SHOCard", "brno_shocard.xml", false);
        //addProvider("Invex PLANstudio", "invex_digitex.xml", false);
        //addProvider("SHOCard", "shocard.xml", true);
    }

    private void createProviderList() {
        providersName = new Vector();
        providersPath = new Vector();
        Enumeration files = R.getFileSystem().listFiles(FileSystem.MAP_FOLDER, null);
        //reading files
        if (files != null) {
            while (files.hasMoreElements()) {
                String filename = (String) files.nextElement();
                if (filename.endsWith(".tar") || filename.endsWith(".xml")) {
                    addProvider(createProviderName(filename), filename);
                }
            }
        }
    }

    private String createProviderName(String fileName) {
        String name = fileName.substring(0, fileName.lastIndexOf('.'));
        name = name.replace('_', ' ');
        return name;
    }
    
    public boolean addProvider(String providerName, String providerPath) {
        if (!providersName.contains(providerName)) {
            providersName.addElement(providerName);
            providersPath.addElement(providerPath);
            return true;
        } else
            return false;
    }
      
    public String getProviderName(int i) {
        if (i < providersName.size())
            return (String) providersName.elementAt(i);
        else
            return null;
    }

    public String getProviderPath(String providerName) {
        for (int i = 0; i < getNumOfProviders(); i++) {
            if (((String) providersName.elementAt(i)).equals(providerName))
                return (String) providersPath.elementAt(i);
        }
        return null;
    }

    public String getProviderPath(int i) {
        if (i < providersName.size())
            return (String) providersPath.elementAt(i);
        else
            return null;
    }
    
    public int getNumOfProviders() {
        createProviderList();
        return providersName.size();
    }
   
    public void removeProvider(int i) {
        if (i < providersName.size()) {
            providersName.removeElementAt(i);
            providersPath.removeElementAt(i);
        }
    }
}
