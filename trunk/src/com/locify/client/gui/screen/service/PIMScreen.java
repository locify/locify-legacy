/*
 * PIMscreen.java
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

import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import de.enough.polish.ui.FilteredList;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;
import de.enough.polish.util.Locale;
import java.util.Enumeration;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

/**
 *
 * @author menion
 */
public class PIMScreen implements CommandListener, ItemCommandListener {

    private PIM pim;
    private ContactList contactList;

    private boolean initialized;

    private Form frmSearch;
    private Form frmViewAll;

    // components
    private TextField txtField;
    private StringItem btnOK;

    public PIMScreen() {
    }

//    public void load() {
//        initialized = false;
//        Thread thread = new Thread(new Runnable() {
//
//            public void run() {
//                try {
//                    pim = PIM.getInstance();
//                    contacts = new ArrayList();
//                    ContactList contactList = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
//                    Enumeration cont = contactList.items();
//                    Contact contact;
//                    String[] nameValues;
//                    while (cont.hasMoreElements()) {
//                        contact = (Contact) cont.nextElement();
//                        nameValues = contact.getStringArray(Contact.NAME, 0);
//                        contacts.add(nameValues[Contact.NAME_FAMILY] + ", " + nameValues[Contact.NAME_GIVEN]);
//                    }
//                    contactList.close();
//                    initialized = true;
//                } catch (PIMException ex) {
//                    initialized = true;
//                }
//            }
//        });
//        thread.start();
//        thread.setPriority(Thread.MIN_PRIORITY);
//    }
//
//    public void view() {
//        try {
//            list = new FilteredList("Contact list", FilteredList.IMPLICIT);
//            list.setCommandListener(this);
//            list.addCommand(Commands.cmdBack);
//
//            if (initialized && Capabilities.hasPIMSupport()) {
////                Contact contact;
//                for (int i = 0; i < contacts.size(); i++) {
////                    contact = (Contact) contacts.get(i);
////                    String[] nameValues = contact.getStringArray(Contact.NAME, 0);
////                    String firstName = nameValues[Contact.NAME_GIVEN];
////                    String lastName = nameValues[Contact.NAME_FAMILY];
////                    list.append(lastName + ", " + firstName, null);
//                    list.append((String) contacts.get(i), null);
//                }
//
//                if (contacts.size() > 0) {
//                    list.setSelectedIndex(0, true);
//                }
//            } else {
//                list.append("Not supported or not yet initialized :)!!!", null);
//            }
//
//            R.getMidlet().switchDisplayable(null, list);
//        } catch (Exception ex) {
//            Logger.log("PIMScreen.view(): " + ex.toString());
//        }
//    }


    public void load() {
        initialized = false;
        try {
            pim = PIM.getInstance();
            contactList = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
            initialized = true;
        } catch (PIMException ex) {
            ex.printStackTrace();
        }

    }

    public void view() {
        try {
            frmSearch = new Form("Contact list");
            frmSearch.setCommandListener(this);
            frmSearch.addCommand(Commands.cmdBack);

            if (initialized && Capabilities.hasPIMSupport()) {
                txtField = new TextField("Name", "", 25, TextField.ANY);
                btnOK = new StringItem("", Locale.get("OK"), StringItem.BUTTON);
                btnOK.setDefaultCommand(Commands.cmdSelect);
                btnOK.setItemCommandListener(this);
                
                frmSearch.append("Write first letters!!!", null);
                frmSearch.append(txtField);
                frmSearch.append(btnOK);
            } else {
                frmSearch.append("Not supported!!!", null);
            }

            R.getMidlet().switchDisplayable(null, frmSearch);
        } catch (Exception ex) {
            Logger.log("PIMScreen.view(): " + ex.toString());
        }
    }

    private void viewFiltered(String filterName) {
//        firstLetter = new String(UTF8.encode(firstLetter));
//System.out.println("\n'" + firstLetter + "'");
        try {
            frmViewAll = new Form("Filtered contacts");
            frmViewAll.setCommandListener(this);
            frmViewAll.addCommand(Commands.cmdBack);
            

            if (initialized && Capabilities.hasPIMSupport()) {
                if (filterName.length() > 0) {
                    Contact contact = contactList.createContact();
                    String[] name = new String[contactList.stringArraySize(Contact.NAME)];
                    name[Contact.NAME_FAMILY] = filterName;
                    contact.addStringArray(Contact.NAME,Contact.ATTR_NONE , name);

                    Enumeration items = contactList.items(contact);
                    while(items.hasMoreElements()) {
                        contact = (Contact) items.nextElement();
                        String[] nameValues = contact.getStringArray(Contact.NAME, 0);
                        String firstName = nameValues[Contact.NAME_GIVEN];
                        String lastName = nameValues[Contact.NAME_FAMILY];
                        frmViewAll.append(lastName + ", " + firstName, null);
                    }
                } else {
                    Enumeration items = contactList.items();
                    while(items.hasMoreElements()) {
                        Contact contact = (Contact) items.nextElement();
                        String[] nameValues = contact.getStringArray(Contact.NAME, 0);
                        String firstName = nameValues[Contact.NAME_GIVEN];
                        String lastName = nameValues[Contact.NAME_FAMILY];
                        Logger.log(lastName + ", " + firstName);
                    }
                    frmViewAll.append("Please write letter!!!", null);
                }
            }
            R.getMidlet().switchDisplayable(null, frmViewAll);
        } catch (Exception ex) {
            Logger.log("PIMScreen.view(): " + ex.toString());
        }
    }

    public void viewContact(Contact contact) {
        //R.getMidlet().switchDisplayable(null, frmOther);
    }

    private void addContact(ContactList list, String firstName, String lastName,
            String street, String city, String country, String postalcode) throws PIMException {

        Contact contact = list.createContact();
        String[] name = new String[list.stringArraySize(Contact.NAME)];
        name[Contact.NAME_GIVEN] = firstName;
        name[Contact.NAME_FAMILY] = lastName;
        contact.addStringArray(Contact.NAME, Contact.ATTR_NONE, name);
        String[] addr = new String[list.stringArraySize(Contact.ADDR)];
        addr[Contact.ADDR_STREET] = street;
        addr[Contact.ADDR_LOCALITY] = city;
        addr[Contact.ADDR_COUNTRY] = country;
        addr[Contact.ADDR_POSTALCODE] = street;
        contact.addStringArray(Contact.ADDR, Contact.ATTR_NONE, addr);
        contact.commit();
    }

//    private void seed() throws PIMException {
//        addContact(contactList, "Jack", "Goldburg", "2345 High Park Ave", "Orlando", "USA", "32817");
//        addContact(contactList, "Mary", "Johnson", "777 Lucky Road", "London", "UK", "SW10 0XE");
//        addContact(contactList, "Johnathan", "Knudsen", "234 Sunny Java Street", "Sausalito", "USA", "94965");
//        addContact(contactList, "Sing", "Li", "168 Technology Drive", "Edmonton", "Canada", "T6G 2E1");
//    }


//    /** handle key events on map screen
//     * @param key
//     */
//    public void keyPressed(int key) {
//        try {
//            super.keyPressed(key);
//            System.out.println("KeyPressed");
//        } catch (Exception e) {
//            R.getErrorScreen().view(e, "MapScreen.keyPressed()", null);
//        }
//    }
//
//    public void keyReleased(int keyCode) {
//        try {
//            super.keyPressed(keyCode);
//            System.out.println("KeyReleased");
//        } catch (Exception e) {
//            R.getErrorScreen().view(e, "MapScreen.keyReleased()", null);
//        }
//    }

    public void commandAction(Command c, Displayable d) {
        if (c == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }
    
    public void commandAction(Command arg0, Item item) {
        if (item.equals(btnOK)) {
            viewFiltered(txtField.getText());
        }
    }
}
