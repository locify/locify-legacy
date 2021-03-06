/*
 * XHTMLBrowser.java
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
package com.locify.client.net;

import com.locify.client.data.IconData;
import com.locify.client.locator.LocationContext;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import de.enough.polish.browser.html.HtmlBrowser;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;
import de.enough.polish.util.Locale;
import de.enough.polish.util.StringTokenizer;
import de.enough.polish.util.TextUtil;

/**
 * Disables some J2ME Polish HTML browser features and leaves the work to Locify
 * @author Destil
 */
public class XHTMLBrowser extends HtmlBrowser {

    private XHTMLTagHandler tagHandler;
    private ImageItem contextImage;
    private StringItem contextText;
    private StringItem fileText;
    private TextField contactTelText;
    private TextField contactEmailText;

    public XHTMLBrowser() {
        //#style browser
        super(de.enough.polish.ui.StyleSheet.browserStyle );
        tagHandler = new XHTMLTagHandler();
        tagHandler.register(this);
    }

    /**
     * Overides J2ME Polish http sending
     * @param url
     * @param postData
     */
    protected void goImpl(String url, String postData) {
        R.getHTMLScreen().quit();
        R.getPostData().setRaw(postData, true);
        url = Utils.replaceString(url, "&amp;", "&");
        R.getURL().call(url);
    }

    /**
     * Support for <label>s
     * @param text
     */
    protected void handleText(String text) {
        if (text.length() > 0) {
            StringTokenizer st = new StringTokenizer(text, "\n\t");

            while (st.hasMoreTokens()) {
                String str = st.nextToken();
                str = TextUtil.replace(str, "&nbsp;", " ");
                StringItem stringItem = null;
                if (this.tagHandler.textStyle != null) {
                    stringItem = new StringItem(null, str, this.tagHandler.textStyle);
                } else if (this.tagHandler.textBold && this.tagHandler.textItalic) {
                    //#style browserTextBoldItalic
                    stringItem = new StringItem(null, str, de.enough.polish.ui.StyleSheet.browsertextbolditalicStyle );
                } else if (this.tagHandler.textBold) {
                    //#style browserTextBold
                    stringItem = new StringItem(null, str, de.enough.polish.ui.StyleSheet.browsertextboldStyle );
                } else if (this.tagHandler.textItalic) {
                    //#style browserTextItalic
                    stringItem = new StringItem(null, str, de.enough.polish.ui.StyleSheet.browsertextitalicStyle );
                } else if (this.tagHandler.textLabel) {
                    this.tagHandler.labelText = str;
                    return;
                } else {
                    //#style browserText
                    stringItem = new StringItem(null, str, de.enough.polish.ui.StyleSheet.browsertextStyle );
                }
                add(stringItem);
            }
        }
    }


    /**
     * Adds custom locify item - locify:where
     */
    public void addContextItem() {
        switch (R.getContext().getSource()) {
            case LocationContext.GPS:
                contextImage = new ImageItem(null, IconData.get("locify://icons/gps.png"), ImageItem.LAYOUT_SHRINK, null);
                if (R.getLocator().hasFix()) {
                    contextText = new StringItem(null, Locale.get(409));
                } else {
                    contextText = new StringItem(null, Locale.get(410));
                }
                break;
            case LocationContext.SAVED_LOCATION:
                contextImage = new ImageItem(null, IconData.get("locify://icons/savedLocation.png"), ImageItem.LAYOUT_SHRINK, null);
                contextText = new StringItem(null, R.getContext().getSourceData());
                break;
            case LocationContext.ADDRESS:
                contextImage = new ImageItem(null, IconData.get("locify://icons/address.png"), ImageItem.LAYOUT_SHRINK, null);
                contextText = new StringItem(null, R.getContext().getSourceData());
                break;
            case LocationContext.COORDINATES:
                contextImage = new ImageItem(null, IconData.get("locify://icons/coordinates.png"), ImageItem.LAYOUT_SHRINK, null);
                contextText = new StringItem(null, R.getContext().getSourceData());
                break;
            case LocationContext.LAST_KNOWN:
                contextImage = new ImageItem(null, IconData.get("locify://icons/lastKnown.png"), ImageItem.LAYOUT_SHRINK, null);
                contextText = new StringItem(null, R.getContext().getSourceData());
                break;
        }


        //#style contextLabel
        add(new StringItem(Locale.get(384) + ":", null), de.enough.polish.ui.StyleSheet.contextlabelStyle );

        //#style contextContainer
        Container container = new Container(false, de.enough.polish.ui.StyleSheet.contextcontainerStyle );
        //#style contextImg
        container.add(contextImage, de.enough.polish.ui.StyleSheet.contextimgStyle );
        //#style contextText
        container.add(contextText, de.enough.polish.ui.StyleSheet.contexttextStyle );
        StringItem btnChange = new StringItem("", Locale.get(408), StringItem.BUTTON);
        btnChange.setDefaultCommand(Commands.cmdAnotherLocation);
        btnChange.setItemCommandListener(tagHandler);
        //#style contextButton
        container.add(btnChange, de.enough.polish.ui.StyleSheet.contextbuttonStyle );
        add(container);
    }

    /**
     * Updates <locify:where />
     */
    public void updateContextItem() {
        try {
            if (contextImage != null && contextText != null) {
                if (R.getContext().isTemporary()) {
                    contextImage.setImage(IconData.get("locify://icons/where.png"));
                    contextText.setText(Locale.get(411));
                } else {
                    switch (R.getContext().getSource()) {
                        case LocationContext.GPS:
                            contextImage.setImage(IconData.get("locify://icons/gps.png"));
                            if (R.getLocator().hasFix()) {
                                contextText.setText(Locale.get(409));
                            } else {
                                contextText.setText(Locale.get(410));
                            }
                            break;
                        case LocationContext.SAVED_LOCATION:
                            contextImage.setImage(IconData.get("locify://icons/savedLocation.png"));
                            contextText.setText(R.getContext().getSourceData());
                            break;
                        case LocationContext.ADDRESS:
                            contextImage.setImage(IconData.get("locify://icons/address.png"));
                            contextText.setText(R.getContext().getSourceData());
                            break;
                        case LocationContext.COORDINATES:
                            contextImage.setImage(IconData.get("locify://icons/coordinates.png"));
                            contextText.setText(R.getContext().getSourceData());
                            break;
                        case LocationContext.LAST_KNOWN:
                            contextImage.setImage(IconData.get("locify://icons/lastKnown.png"));
                            contextText.setText(R.getContext().getSourceData());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XHTMLBrowser.updateContextItem", null);
        }
    }

    /**
     * Adds file browser item <input type="file" />
     */
    public void addFileItem() {
        //#style contextLabel
        add(new StringItem(this.tagHandler.labelText, null), de.enough.polish.ui.StyleSheet.contextlabelStyle );
        fileText = new StringItem(null, Locale.get(448));
        //#style contextContainer
        Container container = new Container(false, de.enough.polish.ui.StyleSheet.contextcontainerStyle );
        //#style fileUploadText
        container.add(fileText, de.enough.polish.ui.StyleSheet.fileuploadtextStyle );
        StringItem btnBrowse = new StringItem("", Locale.get(386), StringItem.BUTTON);
        btnBrowse.setDefaultCommand(Commands.cmdSelect);
        btnBrowse.setItemCommandListener(tagHandler);
        //#style contextButton
        container.add(btnBrowse, de.enough.polish.ui.StyleSheet.contextbuttonStyle );
        add(container);
    }

    /**
     * Updates <input type="file" /> file name
     * @param fileName file name
     */
    public void updateFileItem(String fileName) {
        fileText.setText(fileName);
    }

    /**
     * Add contact item tag: <input type="contactTel" />
     */
    public void addContactTelItem() {
        //#style contextLabel
        add(new StringItem(Locale.get(464) + ":", null), de.enough.polish.ui.StyleSheet.contextlabelStyle );
        contactTelText = new TextField("", "", 50, TextField.PHONENUMBER);

        //#style contextContainer
        Container container = new Container(false, de.enough.polish.ui.StyleSheet.contextcontainerStyle );
        //#style contactsTextField
        container.add(contactTelText, de.enough.polish.ui.StyleSheet.contactstextfieldStyle );

        if (!Capabilities.isWindowsMobile()) {
            StringItem btnBrowse = new StringItem("", Locale.get(386), StringItem.BUTTON);
            btnBrowse.setDefaultCommand(Commands.cmdContactTel);
            btnBrowse.setItemCommandListener(tagHandler);
            //#style contextButton
            container.add(btnBrowse, de.enough.polish.ui.StyleSheet.contextbuttonStyle );
        }
        add(container);
    }

    /**
     * Update contact item
     * @param data data to be written
     */
    public void updateContactTelItem(String data) {
        contactTelText.setText(data);
    }

    /**
     * Add contact item tag: <input type="contactEmail" />
     */
    public void addContactEmailItem() {
        //#style contextLabel
        add(new StringItem(Locale.get(46) + ":", null), de.enough.polish.ui.StyleSheet.contextlabelStyle );
        contactEmailText = new TextField("", "", 50, TextField.EMAILADDR);

        //#style contextContainer
        Container container = new Container(false, de.enough.polish.ui.StyleSheet.contextcontainerStyle );
        //#style contactsTextField
        container.add(contactEmailText, de.enough.polish.ui.StyleSheet.contactstextfieldStyle );

        if (!Capabilities.isWindowsMobile()) {
            StringItem btnBrowse = new StringItem("", Locale.get(386), StringItem.BUTTON);
            btnBrowse.setDefaultCommand(Commands.cmdContactEmail);
            btnBrowse.setItemCommandListener(tagHandler);
            //#style contextButton
            container.add(btnBrowse, de.enough.polish.ui.StyleSheet.contextbuttonStyle );
        }

        add(container);
    }

    /**
     * Update contact item
     * @param data data to be written
     */
    public void updateContactEmailItem(String data) {
        contactEmailText.setText(data);
    }

    public XHTMLTagHandler getTagHandler() {
        return tagHandler;
    }

    public String getContactTel() {
        return contactTelText.getText();
    }

    public String getContactEmail() {
        return contactEmailText.getText();
    }

}
