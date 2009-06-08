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
import com.locify.client.utils.StringTokenizer;
import com.locify.client.data.ServiceSettingsData;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.base64.Base64;
import com.sun.lwuit.Button;
import com.sun.lwuit.CheckBox;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * Manages Locify and HTTP Basic Authentication and shows login screens
 * @author Destil
 */
public class AuthenticationScreens implements ActionListener {

    private FormLocify frmLogin;
    private TextArea tfLogin;
    private TextArea tfPassword;
    private Button siLoginButton;
    private CheckBox cbSavePasswd;
    private CheckBox cbAutologin;
    private String authUrl;
    private String next;
    private boolean httpBasic = false;

    public void setNext(String next) {
        this.next = next;
    }

    private void viewLogin(String loginTo, String savedLogin, String savedPassword) {
        try {
            frmLogin = new FormLocify(Locale.get("Login"));
            Label siLoginTo = new Label(Locale.get("Credentials_to") + " " + loginTo + ":");
            frmLogin.addComponent(siLoginTo);
            Label label01 = new Label(Locale.get("Login_field"));
            tfLogin = new TextArea(savedLogin, 1, 32, TextArea.ANY);
            frmLogin.addComponent(label01);
            frmLogin.addComponent(tfLogin);
            Label label02 = new Label(Locale.get("Password"));
            tfPassword = new TextArea(savedPassword, 1, 32, TextArea.ANY |
                    TextArea.PASSWORD | TextArea.NON_PREDICTIVE | TextArea.SENSITIVE);
            frmLogin.addComponent(label02);
            frmLogin.addComponent(tfPassword);
            cbSavePasswd = new CheckBox(Locale.get("Save_password"));
            cbSavePasswd.setSelected(!savedLogin.equals(""));
            cbAutologin = new CheckBox(Locale.get("Autologin"));
            cbAutologin.setSelected(R.getSettings().getAutoLogin() == SettingsData.ON);
            frmLogin.addComponent(cbSavePasswd);
            frmLogin.addComponent(cbAutologin);

            siLoginButton = new Button(Locale.get("Login"));
            siLoginButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    sendLogin(tfLogin.getText(), tfPassword.getText(), cbSavePasswd.isSelected(), cbAutologin.isSelected());
                }
            });
            frmLogin.addComponent(siLoginButton);

            frmLogin.addCommand(Commands.cmdBack);
            frmLogin.addCommand(Commands.cmdHome);
            frmLogin.setCommandListener(this);
            frmLogin.show();
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
                    R.getPostData().add("login", tfLogin.getText());
                    R.getPostData().add("hash", Utils.urlUTF8Encode(Sha1.encode(Sha1.encode(tfPassword.getText()) + challenge)));
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
        if (setAutologin) {
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

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }
}
