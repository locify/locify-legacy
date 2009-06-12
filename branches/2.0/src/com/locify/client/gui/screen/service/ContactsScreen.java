///#if WM
// /*
// * PIMscreen.java
// * This file is part of Locify.
// *
// * Locify is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version.
// * (read more at: http://www.gnu.org/licenses/gpl.html)
// *
// * Commercial licenses are also available, please
// * refer http://code.google.com/p/locify/ for details.
// */
package com.locify.client.gui.screen.service;

import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.sun.lwuit.Form;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.DefaultListModel;
import java.util.Enumeration;
//import javax.microedition.pim.Contact;
//import javax.microedition.pim.ContactList;
//import javax.microedition.pim.PIM;

/**
 *
 * @author menion
 */
public class ContactsScreen implements ActionListener {

//    private PIM pim;
//    private ContactList contactList;
    public static final int FILTER_ALL = 0;
    public static final int FILTER_EMAIL = 1;
    public static final int FILTER_TEL = 2;
    public static boolean initialized;
    private boolean supportEmail;
    private boolean supportTel;
    private Form frmView;
    private List lstView;
    private int actualFilter;
    private int numOfContacts;
    private static StringBuffer buffer = new StringBuffer();

    public ContactsScreen() {
    }

    public void load() {
//        initialized = false;
//        try {
//            if (Capabilities.hasPIMSupport()) {
//                pim = PIM.getInstance();
//                contactList = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
//                supportEmail = contactList.isSupportedField(Contact.EMAIL);
//                supportTel = contactList.isSupportedField(Contact.TEL);
//                initialized = true;
//            }
//        } catch (Exception ex) {
//        }
    }

    public void viewFiltered(String filterName, int filter) {
//        this.actualFilter = filter;
//        this.numOfContacts = 0;
//        try {
//            frmView = new Form(Locale.get("Contacts_screen"));
//            lstView = new List(new DefaultListModel());
//            frmView.addCommand(Commands.cmdBack);
//            frmView.addCommand(Commands.cmdSelect);
//            frmView.setCommandListener(this);
//
//            frmView.setLayout(new BorderLayout());
//            frmView.addComponent(BorderLayout.CENTER, lstView);
//
//            if (initialized) {
//                Enumeration items;
//                Contact contact;
//
//                if (filterName != null && filterName.length() > 0) {
//                    contact = contactList.createContact();
//                    String[] name = new String[contactList.stringArraySize(Contact.NAME)];
//                    name[Contact.NAME_FAMILY] = filterName;
//                    contact.addStringArray(Contact.NAME, Contact.ATTR_NONE, name);
//
//                    addContacts(contactList.items(contact));
//
//                    contact = contactList.createContact();
//                    name = new String[contactList.stringArraySize(Contact.NAME)];
//                    name[Contact.NAME_GIVEN] = filterName;
//                    contact.addStringArray(Contact.NAME, Contact.ATTR_NONE, name);
//
//                    addContacts(contactList.items(contact));
//                } else {
//                    addContacts(contactList.items());
//                }
//
//
//                if (numOfContacts == 0) {
//                    if (actualFilter == FILTER_ALL) {
//                        lstView.addItem(Locale.get("Contacts_error_1"));
//                    } else if (actualFilter == FILTER_EMAIL) {
//                        lstView.addItem(Locale.get("Contacts_error_1"));
//                    } else if (actualFilter == FILTER_TEL) {
//                        lstView.addItem(Locale.get("Contacts_error_1"));
//                    }
//                }
//
//            }
//            frmView.show();
//        } catch (Exception ex) {
//            Logger.error("PIMScreen.viewFiltered(): " + ex.toString());
//        }
    }

    private void addContacts(Enumeration items) {
//        Contact contact;
//        String[] nameValues;
//        String firstName;
//        String lastName;
//        String otherName;
//        String email;
//        String tel;
//
//        while (items.hasMoreElements()) {
//            try {
//                numOfContacts++;
//                contact = (Contact) items.nextElement();
//                nameValues = contact.getStringArray(Contact.NAME, 0);
//                firstName = nameValues[Contact.NAME_GIVEN];
//                lastName = nameValues[Contact.NAME_FAMILY];
//                otherName = nameValues[Contact.NAME_OTHER];
//
//                if (firstName == null)
//                    firstName = "";
//                if (lastName == null)
//                    lastName = "";
//                if (otherName == null)
//                    otherName = "";
//
//                try {
//                    email = supportEmail ? contact.getString(Contact.EMAIL, 0) : "";
//                } catch (Exception e) {
//                    email = "";
//                }
//                try {
//                    tel = supportTel ? contact.getString(Contact.TEL, 0) : "";
//                } catch (Exception e) {
//                    tel = "";
//                }
//
//                //Logger.log(lastName + "," + firstName + "," + email + "," + tel);
//
//
//                if (actualFilter == FILTER_ALL) {
//                    lstView.addItem(createName(firstName, lastName, otherName) + firstName + "\n   " + email + ", " + tel);
//                } else if (actualFilter == FILTER_EMAIL && !email.equals("")) {
//                    lstView.addItem(createName(firstName, lastName, otherName) + "\n   " + email);
//                } else if (actualFilter == FILTER_TEL && !tel.equals("")) {
//                    lstView.addItem(createName(firstName, lastName, otherName) + "\n   " + tel);
//                }
//            } catch (Exception e) {
//                Logger.error("PIMScreen.addContacts(): " + e.toString());
//            }
//        }
//
    }

//    private String createName(String firstName, String lastName, String otherName) {
//        buffer.delete(0, buffer.length());
//        if (!firstName.equals(""))
//            buffer.append(firstName + ", ");
//        if (!lastName.equals(""))
//            buffer.append(lastName + ", ");
//        if (firstName.equals("") && lastName.equals(""))
//            buffer.append(otherName + ", ");
//
//        return buffer.toString();
//    }
//
////    private void addContact(ContactList list, String firstName, String lastName,
////            String street, String city, String country, String postalcode, String email, String phoneNum) throws PIMException {
////
////        Contact contact = list.createContact();
////        String[] name = new String[list.stringArraySize(Contact.NAME)];
////        name[Contact.NAME_GIVEN] = firstName;
////        name[Contact.NAME_FAMILY] = lastName;
////        contact.addStringArray(Contact.NAME, Contact.ATTR_NONE, name);
////        String[] addr = new String[list.stringArraySize(Contact.ADDR)];
////        addr[Contact.ADDR_STREET] = street;
////        addr[Contact.ADDR_LOCALITY] = city;
////        addr[Contact.ADDR_COUNTRY] = country;
////        addr[Contact.ADDR_POSTALCODE] = street;
////        contact.addStringArray(Contact.ADDR, Contact.ATTR_NONE, addr);
////        contact.addString(Contact.EMAIL, Contact.ATTR_NONE, email);
////        contact.addString(Contact.TEL, Contact.ATTR_MOBILE, phoneNum);
////        contact.commit();
////    }
////
////    private void seed() throws PIMException {
////        addContact(contactList, "Jack", "Goldburg", "2345 High Park Ave", "Orlando", "USA", "32817", "jack.gold@centrum.cz", "+420 776 133013");
////        addContact(contactList, "Mary", "Johnson", "777 Lucky Road", "London", "UK", "SW10 0XE", "jack.gold@centrum.cz", "");
////        addContact(contactList, "Johnathan", "Knudsen", "234 Sunny Java Street", "Sausalito", "USA", "94965", "", "+420 776 133013");
////        addContact(contactList, "Sing", "Li", "168 Technology Drive", "Edmonton", "Canada", "T6G 2E1", "", "");
////    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdSelect) {
            String text = (String) lstView.getSelectedItem();
            text = text.substring(text.lastIndexOf(',') + 1).trim();
            if (actualFilter == FILTER_TEL) {
                R.getHtmlScreen().updateContactTelInfo(text);
            } else if (actualFilter == FILTER_EMAIL) {
                R.getHtmlScreen().updateContactEmailInfo(text);
            }
        }
    }
}
////////#endif