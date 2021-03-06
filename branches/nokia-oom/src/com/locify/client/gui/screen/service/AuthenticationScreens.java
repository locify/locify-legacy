/*
 * AuthenticationScreen.java
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

import com.locify.client.data.ServicesData;
import com.locify.client.data.SettingsData;
import com.locify.client.net.Http;
import com.locify.client.utils.R;
import com.locify.client.utils.Sha1;
import com.locify.client.utils.Utils;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.utils.Commands;
import de.enough.polish.util.Locale;
import de.enough.polish.util.base64.Base64;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * Manages Locify and HTTP Basic Authentication and shows login screens
 * @author Destil
 */
public class AuthenticationScreens implements CommandListener, ItemCommandListener {

    private Form frmLogin;
    private TextField tfLogin;
    private TextField tfPassword;
    private StringItem siLinkRegister;
    private StringItem siLoginButton;
    private ChoiceGroup cgAuto;
    private Command cmdSelect = new Command(Locale.get("Select"), Command.ITEM, 10);
    private String authUrl;
    private String next;
    private boolean httpBasic = false;

    public void setNext(String next) {
        this.next = next;
    }

    private void viewLogin(String loginTo, String savedLogin, String savedPassword) {
        try {
            frmLogin = new Form(Locale.get("Login_field"));
            StringItem siLoginTo = new StringItem("", Locale.get("Credentials_to") + " " + loginTo + ":");
            frmLogin.append(siLoginTo);
            tfLogin = new TextField(Locale.get("Login_field"), savedLogin, 32, TextField.ANY);
            frmLogin.append(tfLogin);
            tfPassword = new TextField(Locale.get("Password"), savedPassword, 32, TextField.ANY | TextField.PASSWORD | TextField.NON_PREDICTIVE | TextField.SENSITIVE);
            frmLogin.append(tfPassword);
            cgAuto = new ChoiceGroup("", Choice.MULTIPLE);
            cgAuto.append(Locale.get("Save_password"), null);
            cgAuto.append(Locale.get("Autologin"), null);
            if (savedLogin.equals("")) {
                cgAuto.setSelectedIndex(0, false);
            } else {
                cgAuto.setSelectedIndex(0, true);
            }
            if (R.getSettings().getAutoLogin() == SettingsData.ON) {
                cgAuto.setSelectedIndex(1, true);
            } else {
                cgAuto.setSelectedIndex(1, false);
            }
            frmLogin.append(cgAuto);


            siLoginButton = new StringItem("", Locale.get("Login_field"), Item.BUTTON);
            siLoginButton.addCommand(cmdSelect);
            siLoginButton.setItemCommandListener(this);
            siLoginButton.setDefaultCommand(cmdSelect);
            siLoginButton.setLayout(ImageItem.LAYOUT_DEFAULT | ImageItem.LAYOUT_NEWLINE_AFTER);
            frmLogin.append(siLoginButton);

            frmLogin.addCommand(Commands.cmdBack);
            //#style imgHome
            frmLogin.addCommand(Commands.cmdHome);
            frmLogin.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, frmLogin);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "AuthenticationScreens.viewLogin", loginTo);
        }
    }


    /**
     * Shows auth form - if auth is locify://authentication, locify login form is shown, otherwise normal service
     * login form is shown
     * @param url id of auth
     */
    public void start(String url) {
        try {
            url = R.getHttp().makeAbsoluteURL(url);
            authUrl = url;
            if (url.equals("locify://authentication")) {
                if (R.getSettings().getAutoLogin() == SettingsData.ON && !R.getSettings().getName().equals("")) {
                    sendLogin(R.getSettings().getName(), R.getSettings().getPassword(), false, false);
                } else {
                    viewLogin("Locify", R.getSettings().getName(), R.getSettings().getPassword());
                }
            } else if (url.startsWith("locify://authentication?challenge=")) {
                String[] parts = StringTokenizer.getArray(url, "=");
                String challenge = parts[1];
                if (R.getSettings().getAutoLogin() == SettingsData.ON && !R.getSettings().getName().equals("")) {
                    R.getPostData().add("login", R.getSettings().getName());
                    R.getPostData().add("hash", Utils.urlUTF8Encode(Sha1.encode(Sha1.encode(R.getSettings().getPassword()) + challenge)));
                } else {
                    R.getPostData().add("login", tfLogin.getString());
                    R.getPostData().add("hash", Utils.urlUTF8Encode(Sha1.encode(Sha1.encode(tfPassword.getString()) + challenge)));
                }
                
                R.getPostData().add("next", next);
                R.getHttp().start(Http.DEFAULT_URL + "user/login",Http.AUTH);
            } else {
                //service authentication
                String realm = "";
                if (url.startsWith("locify://authentication?realm=")) {
                    String[] parts = StringTokenizer.getArray(url, "=");
                    realm = parts[1];
                    httpBasic = true;
                } else {
                    realm = ServicesData.getService(ServicesData.getCurrent()).getName();
                    httpBasic = false;
                }
                if (R.getSettings().getAutoLogin() == SettingsData.ON && !ServiceSettingsData.getValue("login", ServicesData.getCurrent()).equals("")) {
                    sendLogin(ServiceSettingsData.getValue("login", ServicesData.getCurrent()), ServiceSettingsData.getValue("password", ServicesData.getCurrent()), false, false);
                } else {
                    viewLogin(realm, ServiceSettingsData.getValue("login", ServicesData.getCurrent()), ServiceSettingsData.getValue("password", ServicesData.getCurrent()));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "AuthenticationScreens.start", url);
        }
    }

    private void sendLogin(String login, String password, boolean saveCredentials, boolean setAutologin) {
        if (setAutologin)
        {
            R.getSettings().setAutologin(SettingsData.ON);
        }

        if (authUrl.equals("locify://authentication")) {
            if (saveCredentials) {
                R.getSettings().saveLocifyCredentials(login, password);
            }
            R.getPostData().add("login", login);
            R.getHttp().start(Http.DEFAULT_URL + "user/login",Http.AUTH);
        } else {
            if (saveCredentials) {
                ServiceSettingsData.addEdit("login", login, ServicesData.getCurrent());
                ServiceSettingsData.addEdit("password", password, ServicesData.getCurrent());
                ServiceSettingsData.saveXML();
            }
            if (httpBasic) {
                String basicResponse = Base64.encodeBytes(UTF8.encode(Utils.replaceString(login,":","{colon}") + ":" + password));
                R.getHttp().repeat(basicResponse);
            } else {
                R.getPostData().add("login", login);
                R.getPostData().add("password", password);
                R.getHttp().start(authUrl,Http.AUTH);
            }
        }
    }

    public void commandAction(Command command, Displayable displayble) {
        if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }

    public void commandAction(Command command, Item item) {
        if (item == siLinkRegister) {
            R.getURL().call("locify://register");
        } else if (item == siLoginButton) {
            sendLogin(tfLogin.getString(), tfPassword.getString(), cgAuto.isSelected(0), cgAuto.isSelected(1));
        }
    }
}
