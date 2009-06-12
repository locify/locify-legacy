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
package com.locify.client.net.browser;

import com.locify.client.data.IconData;
import com.locify.client.gui.extension.FlowLayoutYScroll;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.R;
import com.locify.client.gui.screen.service.ContactsScreen;
import com.locify.client.net.HttpMultipartRequest;
import com.locify.client.net.Variables;
import com.locify.client.net.browser.HtmlButton;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import java.util.Enumeration;
import java.util.Hashtable;
import org.xmlpull.v1.XmlPullParser;

/**
 * Handles HTML tags.
 */
public class XHtmlTagHandler implements ActionListener {

    /** title tag */
    public static final String TAG_TITLE = "title";
    /** style tag */
    public static final String TAG_STYLE = "style";
    /** br tag */
    public static final String TAG_BR = "br";
    /** p tag */
    public static final String TAG_P = "p";
    /** img tag */
    public static final String TAG_IMG = "img";
    /** div tag */
    public static final String TAG_DIV = "div";
    /** span tag */
    public static final String TAG_SPAN = "span";
    /** a tag */
    public static final String TAG_A = "a";
    /** b tag */
    public static final String TAG_B = "b";
    /** strong tag */
    public static final String TAG_STRONG = "strong";
    /** i tag */
    public static final String TAG_I = "i";
    /** em tag */
    public static final String TAG_EM = "em";
    /** form tag */
    public static final String TAG_FORM = "form";
    /** input tag */
    public static final String TAG_INPUT = "input";
    /** button tag */
    public static final String TAG_BUTTON = "button";
    /** text area tag */
    public static final String TAG_TEXT_AREA = "textarea";
    /** select tag */
    public static final String TAG_SELECT = "select";
    /** option tag */
    public static final String TAG_OPTION = "option";
    /** script tag */
    public static final String TAG_SCRIPT = "script";
    /** table tag */
    public static final String TAG_TABLE = "table";
    /** table row tag */
    public static final String TAG_TR = "tr";
    /** table header tag */
    public static final String TAG_TH = "th";
    /** table data tag */
    public static final String TAG_TD = "td";
    public static final String TAG_LABEL = "label";
    /* Locify specific tags */
    public static final String TAG_LOCIFY_ELEMENT = "locify:element";
    public static final String TAG_LOCIFY_WHERE = "locify:where";
    public static final String TAG_LOCIFY_VIBRATE = "locify:vibrate";
    public static final String TAG_LOCIFY_BLINK = "locify:blink";
    public static final String TAG_LOCIFY_CONTACT = "locify:contact";
    
    public static final String TAG_EMBED = "embed";
    /** type attribute */
    public static final String INPUT_TYPE = "type";
    /** name attribute */
    public static final String INPUT_NAME = "name";
    /** value attribute */
    public static final String INPUT_VALUE = "value";
    /** text type-value */
    public static final String INPUTTYPE_TEXT = "text";
    /** hidden type-value */
    public static final String INPUTTYPE_HIDDEN = "hidden";
    /** submit type-value */
    public static final String INPUTTYPE_SUBMIT = "submit";
    public static final String INPUTTYPE_FILE = "file";
    /** href attribute */
    public static final String ATTR_HREF = "href";
    /** form attribute */
    public static final String ATTR_FORM = "form";
    /** type attribute */
    public static final String ATTR_TYPE = "type";
    /** value attribute */
    public static final String ATTR_VALUE = "value";
    /** name attribute */
    public static final String ATTR_NAME = "name";
    /** size attribute */
    public static final String ATTR_SIZE = "size";
    /** multiple attribute */
    public static final String ATTR_MULTIPLE = "multiple";

    public static final String CMD_LINK = "Link";
    public static final String CMD_SUBMIT = "Submit";
    public static final String CMD_BACK = "Back";

    private Hashtable commandsByKey;
    private HtmlForm currentForm;
    private HtmlSelect currentSelect;
//    private TableItem currentTable;
    private Component currentTable;

    private XHtmlBrowser browser;
    protected String labelText;
    /** next text should be added in bold font style */
    protected boolean textBold;
    /** next text should be added in italic font style */
    protected boolean textItalic;
    protected boolean textLabel;
    public HtmlStyle textStyle;
    
    /* for input type=file */
    private String fileName;
    private String fileType;
    private boolean multipart = false;

    /**
     * Creates a new html tag handler
     */
    public XHtmlTagHandler() {
    }

    public void register(XHtmlBrowser parent) {
        this.browser = parent;
        this.textBold = false;
        this.textItalic = false;
        this.textLabel = false;
        this.labelText = "";

        parent.addTagHandler(TAG_TITLE, this);
        parent.addTagHandler(TAG_STYLE, this);
        parent.addTagHandler(TAG_BR, this);
        parent.addTagHandler(TAG_P, this);
        parent.addTagHandler(TAG_IMG, this);
        parent.addTagHandler(TAG_DIV, this);
        parent.addTagHandler(TAG_SPAN, this);
        parent.addTagHandler(TAG_A, this);
        parent.addTagHandler(TAG_B, this);
        parent.addTagHandler(TAG_STRONG, this);
        parent.addTagHandler(TAG_I, this);
        parent.addTagHandler(TAG_EM, this);
        parent.addTagHandler(TAG_FORM, this);
        parent.addTagHandler(TAG_INPUT, this);
        parent.addTagHandler(TAG_BUTTON, this);
        parent.addTagHandler(TAG_SELECT, this);
        parent.addTagHandler(TAG_OPTION, this);
        parent.addTagHandler(TAG_SCRIPT, this);
        parent.addTagHandler(TAG_TEXT_AREA, this);
        parent.addTagHandler(TAG_TABLE, this);
        parent.addTagHandler(TAG_TR, this);
        parent.addTagHandler(TAG_TH, this);
        parent.addTagHandler(TAG_TD, this);
        parent.addTagHandler(TAG_LABEL, this);
        parent.addTagHandler(TAG_LOCIFY_ELEMENT, this);
        parent.addTagHandler(TAG_LOCIFY_WHERE, this);
        parent.addTagHandler(TAG_LOCIFY_VIBRATE, this);
        parent.addTagHandler(TAG_LOCIFY_BLINK, this);
        parent.addTagHandler(TAG_LOCIFY_CONTACT, this);
        parent.addTagHandler(TAG_EMBED, this);
    }

    /* (non-Javadoc)
     * @see de.enough.polish.browser.TagHandler#handleTag(de.enough.polish.ui.Container, de.enough.polish.xml.PullParser, java.lang.String, boolean, de.enough.polish.util.HashMap, de.enough.polish.ui.Style)
     */
    public boolean handleTag(Container parentItem, XmlPullParser parser, String tagName, boolean opening, Hashtable attributeMap, HtmlStyle style) {
        try {
            tagName = tagName.toLowerCase();
            if (TAG_DIV.equals(tagName) || TAG_SPAN.equals(tagName)) {
                if (opening) {
                    this.browser.openContainer(new Container(new FlowLayout()));
                } else {
                    Container container = this.browser.closeContainer();
////                    if (container instanceof TableItem) {
////                        this.currentTable = (TableItem) UiAccess.cast(container);
////                    }
                }
            } else if (TAG_P.equals(tagName)) {
                if (opening)
                    browser.openContainer(new Container(new FlowLayoutYScroll()));
                else
                    browser.closeContainer();
                return true;
            } else if (TAG_SELECT.equals(tagName)) {
                if (opening) {
                    if (this.currentSelect != null) {
                        System.out.println("Error in HTML-Code: You cannot open a <select>-tag inside another <select>-tag.");
                        Container container = this.currentSelect.getSelectComponent();
                        this.browser.addComponent(container);

                        if (this.currentForm == null) {
                            System.out.println("Error in HTML-Code: no <form> for <select> element found!");
                        } else {
                            this.currentForm.addItem(container);
                        }
                        this.currentSelect = null;
                    }

                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    String value = null;
                    if (name != null && this.currentForm.getLocifyElementValue(name) != null) {
                        value = this.currentForm.getLocifyElementValue(name);
                    } else {
                        value = null;
                    }
                    String sizeStr = parser.getAttributeValue(null, ATTR_SIZE);
                    int size;

                    try {
                        size = Integer.parseInt(sizeStr);
                    } catch (NumberFormatException e) {
                        size = -1;
                    }

                    boolean isMultiple = parser.getAttributeValue(null, ATTR_MULTIPLE) != null;
                    this.currentSelect = new HtmlSelect(name, labelText, size, isMultiple, style, value);
                } else { // tag is closed
                    if (this.currentSelect != null) {
                        Container container = this.currentSelect.getSelectComponent();
                        this.browser.addComponent(container);
                        if (this.currentForm == null) {
                            System.out.println("Error in HTML-Code: no <form> for <select> element found!");
                        } else {
                            this.currentForm.addItem(container);
                        }
                        this.currentSelect = null;
                    } else {
                        System.out.println("Error in HTML-Code. You cannot close a <select>-tag without opening one.");
                    }
                }
                return true;
            } else if (TAG_OPTION.equals(tagName)) {
                if (this.currentSelect != null && opening) {
                    // TODO: handle "selected" attribute.
                    String value = parser.getAttributeValue(null, ATTR_VALUE);
                    String selected = parser.getAttributeValue(null, "selected");
                    parser.next();
                    String name = parser.getText();

                    if (value == null) {
                        value = name;
                    }

                    this.currentSelect.addOption(name, value, selected != null, style);
                }
                return true;
            }

            if (opening) {
                if (TAG_TITLE.equals(tagName)) {
                    parser.next();
                    String name = parser.getText();
                    if (this.browser.getParent() instanceof FormLocify)
                        ((FormLocify) this.browser.getParent()).setTitle(name);
                    return true;
                } else if (TAG_STYLE.equals(tagName)) {
                    parser.next();
                    return true;
                } else if (TAG_A.equals(tagName)) {
                    String href = (String) attributeMap.get(ATTR_HREF);
                    parser.next();
                    HtmlButton linkItem = new HtmlButton();
                    if (href != null) {
                        String anchorText = parser.getText();
                        // hack for image links:
                        if ("".equals(anchorText) && TAG_IMG.equals(parser.getName())) {
                            // this is an image link:
                            attributeMap.clear();
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                String attributeName = parser.getAttributeName(i);
                                String attributeValue = parser.getAttributeValue(i);
                                attributeMap.put(attributeName, attributeValue);
                            }
                            String src = (String) attributeMap.get("src");
                            String url = R.getHttp().makeAbsoluteURL(src);
                            Image image = IconData.get(url);
                            linkItem = new HtmlButton(image);
                            linkItem.setLinkBehaviour();
                            linkItem.setText((String) attributeMap.get("alt"));
////                            this.browser.loadImageLater( url, (ImageItem) linkItem );
                        } else {
                            linkItem.url = href;
                        }
                        linkItem.addActionListener(this);
//                        addCommands(TAG_A, linkItem);
                    } else {
                        linkItem = new HtmlButton(parser.getText());
                        linkItem.setLinkBehaviour();
                        linkItem.setFocusable(false);
                    }
                    if (style != null) {
                        linkItem.setStyle(style);
                    }
                    browser.addComponent(linkItem);
                    return true;
                } else if (TAG_BR.equals(tagName)) {
                    browser.addNewLine();
                    return true;
                } else if (TAG_IMG.equals(tagName)) {
                    String src = (String) attributeMap.get("src");
                    String url = R.getHttp().makeAbsoluteURL(src);
                    Label image = new Label("[Image]");
                    browser.addComponent(image);
//                    Image image = this.browser.loadImage(url);
//                    if (image != null) {
//                        ImageItem item = new ImageItem(null, image, Item.LAYOUT_DEFAULT, "");
//                        if (style != null) {
//                            item.setStyle(style);
//                        }
//
//                        add(item);
//                    }
                    return true;
                } else if (TAG_TEXT_AREA.equals(tagName)) {
                    parser.next();
                    String value = parser.getText();
                    int maxCharNumber = 500;
                    String cols = (String) attributeMap.get("cols");
                    String rows = (String) attributeMap.get("rows");
                    if (cols != null && rows != null) {
                        try {
                            maxCharNumber = Integer.parseInt(cols) * Integer.parseInt(rows);
                        } catch (Exception e) {
                            //#debug error
                            System.out.println("Unable to parse textarea cols or rows attribute: cols=" + cols + ", rows=" + rows);
                        }
                    }
                    HtmlTextArea textArea = new HtmlTextArea(value, true);
                    if (style != null) {
                        //textArea.setStyle(style);
                    }
                    browser.addComponent(textArea);
                    if (this.currentForm != null) {
                        this.currentForm.addItem(textArea);
                        textArea.setAttributeForm(this.currentForm);
                        String name = (String) attributeMap.get(INPUT_NAME);
                        if (value == null) {
                            value = name;
                        }
                        if (name != null) {
                            textArea.setAttributeName(name);
                            textArea.setAttributeValue(value);
                        }
                    }
                    return true;
                } else if (TAG_BUTTON.equals(tagName) && this.currentForm != null) {
                    String name = (String) attributeMap.get(INPUT_NAME);
                    String value = null;
                    if (this.currentForm.getLocifyElementValue(name) != null) {
                        value = this.currentForm.getLocifyElementValue(name);
                    } else {
                        value = (String) attributeMap.get(INPUT_VALUE);
                    }

                    if (value == null) {
                        value = name;
                    }
                    HtmlButton buttonItem = new HtmlButton(value);
                    if (style != null) {
                        buttonItem.setStyle(style);
                    }
                    buttonItem.addActionListener(this);
                    addCommands(TAG_INPUT, INPUT_TYPE, INPUTTYPE_SUBMIT, buttonItem);
                    browser.addComponent(buttonItem);

                    this.currentForm.addItem(buttonItem);
                    buttonItem.setAttributeForm(this.currentForm);
                    buttonItem.setAttributeType(CMD_SUBMIT);

                    if (name != null) {
                        buttonItem.setAttributeName(name);
                        buttonItem.setAttributeValue(value);
                    }
                } else if (TAG_INPUT.equals(tagName)) {
                    if (this.currentForm != null) {
                        String type = (String) attributeMap.get(INPUT_TYPE);
                        String name = (String) attributeMap.get(INPUT_NAME);
                        String value = null;
                        if (name != null && this.currentForm.getLocifyElementValue(name) != null) {
                            value = this.currentForm.getLocifyElementValue(name);
                        } else {
                            value = (String) attributeMap.get(INPUT_VALUE);
                        }

                        if (INPUTTYPE_TEXT.equals(type)) {
                            Label lab = new Label(labelText);
                            this.browser.addComponent(lab);
                            this.currentForm.addItem(lab);
                            TextArea textArea = new TextArea(value, 2, 1000, TextArea.ANY);
                            if (style != null) {
                                textArea.getStyle().setFont(Font.createSystemFont(style.fontFace, style.fontStyle, style.fontSize));
                                textArea.getStyle().setFgColor(style.fontColor);
                            }
                            this.browser.addComponent(textArea);
                            this.currentForm.addItem(textArea);
                            //textField.setAttribute(ATTR_FORM, this.currentForm);

                            if (name != null) {
                                //textField.setAttribute(ATTR_NAME, name);
                                if (value == null) {
                                    value = "";
                                }
                                //textField.setAttribute(ATTR_VALUE, value);
                            }
                        } else if (INPUTTYPE_SUBMIT.equals(type)) {
                            if (value == null) {
                                value = name;
                            }

                            HtmlButton buttonItem = new HtmlButton(value);
                            if (style != null) {
                                //buttonItem.setStyle(style);
                            }
                            buttonItem.addActionListener(this);
                            addCommands(TAG_INPUT, INPUT_TYPE, INPUTTYPE_SUBMIT, buttonItem);
                            browser.addComponent(buttonItem);

                            this.currentForm.addItem(buttonItem);
                            buttonItem.setAttributeForm(this.currentForm);
                            buttonItem.setAttributeType(CMD_SUBMIT);

                            if (name != null) {
                                buttonItem.setAttributeName(name);
                                buttonItem.setAttributeValue(value);
                            }
                        } else if (INPUTTYPE_HIDDEN.equals(type)) {
                            this.currentForm.addHiddenElement(name, value);
                        } else if (INPUTTYPE_FILE.equals(type)) {
                            if (opening) {
                                fileName = name;
                                String accept = (String) attributeMap.get("accept");
                                if (accept == null) {
                                    fileType = "";
                                } else if (accept.equals("image/*")) {
                                    fileType = "image";
                                } else if (accept.equals("video/*")) {
                                    fileType = "video";
                                } else if (accept.equals("application/vnd.locify.place")) {
                                    fileType = "place";
                                } else if (accept.equals("application/vnd.locify.route")) {
                                    fileType = "route";
                                } else {
                                    fileType = "";
                                }
                                browser.addFileItem();
                            }
                            return true;
                        }
                    }
                    return true;
                } else if (TAG_SCRIPT.equals(tagName)) {
                    // Consume javascript code.
                    parser.next();
                    return true;
                } else if (TAG_TABLE.equals(tagName)) {
//                    //#style browserTable?
//                    TableItem table = new TableItem();
//                    table.setSelectionMode(TableItem.SELECTION_MODE_CELL | TableItem.SELECTION_MODE_INTERACTIVE);
//                    table.setCellContainerStyle(this.browser.getStyle());
//                    if (style != null) {
//                        table.setStyle(style);
//                    }
//
//                    this.currentTable = table;
//                    this.browser.openContainer(table);
//                    return true;
                } else if (this.currentTable != null && TAG_TR.equals(tagName)) {
//                    this.currentTable.moveToNextRow();
                    return true;
                } else if (this.currentTable != null && TAG_TH.equals(tagName)) {
                    //TODO differentiate between th and td
//                    this.currentTable.moveToNextColumn();
                    return true;
                } else if (this.currentTable != null && TAG_TD.equals(tagName)) {
                    //TODO differentiate between th and td
//                    this.currentTable.moveToNextColumn();
                    return true;
                } else if (TAG_LOCIFY_VIBRATE.equals(tagName)) {
                    int duration = 500;
                    if (attributeMap.get("duration") != null) {
                        duration = Integer.parseInt((String) attributeMap.get("duration"));
                    }
                    Display.getInstance().vibrate(duration);
                    return true;
                } else if (TAG_LOCIFY_BLINK.equals(tagName)) {
                    int duration = 500;
                    if (attributeMap.get("duration") != null) {
                        duration = Integer.parseInt((String) attributeMap.get("duration"));
                    }
                    Display.getInstance().flashBacklight(duration);
                    return true;
                // <locify:contact name="telefon" type="tel" />
                } else if (TAG_LOCIFY_CONTACT.equalsIgnoreCase(tagName)) {
                    String type = (String) attributeMap.get("type");
                    if (type.equals("tel")) {
                        this.currentForm.addLocifyPIM((String) attributeMap.get("name"), "tel");
                        this.browser.addContactTelItem();
                    } else if (type.equals("email")) {
                        this.currentForm.addLocifyPIM((String) attributeMap.get("name"), "email");
                        this.browser.addContactEmailItem();
                    }
                    return true;
                } else if (TAG_LOCIFY_ELEMENT.equals(tagName)) {
                    this.currentForm.addLocifyElement((String) attributeMap.get("name"), (String) attributeMap.get("value"));
                    return true;
                } else if (TAG_LOCIFY_WHERE.equals(tagName)) {
                    this.browser.addContextItem();
                    return true;
                }
            } else {
                // the tag is being closed:
                if (TAG_TABLE.equals(tagName)) {
                    Container container = this.browser.closeContainer();
//                    if (UiAccess.cast(container) instanceof TableItem) {
//                        this.currentTable = (TableItem) UiAccess.cast(container);
//                    } else {
//                        this.currentTable = null;
//                    }
                    return true;
                }
            }

            if (TAG_B.equals(tagName) || TAG_STRONG.equals(tagName)) {
                this.textBold = opening;
                return true;
            } else if (TAG_I.equals(tagName) || TAG_EM.equals(tagName)) {
                this.textItalic = opening;
                return true;
            } else if (TAG_FORM.equals(tagName)) {
                if (opening) {
                    String name = (String) attributeMap.get("name");
                    String action = (String) attributeMap.get("action");
                    String method = (String) attributeMap.get("method");
                    multipart = false;
                    if ("multipart/form-data".equals(attributeMap.get("enctype"))) {
                        multipart = true;
                    }
                    if (method == null) {
                        method = "GET";
                    }
                    this.currentForm = new HtmlForm(name, action, method.toUpperCase());
                } else {
                    this.currentForm = null;
                }
                return true;
            } else if (TAG_LABEL.equals(tagName)) {
                this.textLabel = opening;
                return true;
            } else if (TAG_EMBED.equals(tagName)) {
                if (opening) {
                    if (attributeMap.get("src") != null) {
                        String file = (String) attributeMap.get("src");
                        if (file.endsWith("wav")) {
                            R.getAudio().play(R.getHttp().makeAbsoluteURL(file));
                        }
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "XHTMLTagHandler.handle", "Tag:" + tagName + ", Opening:" + opening + ", Attributes:" + attributeMap.toString());
            return false;
        }

    }

    protected void handleBackCommand() {
        this.browser.goBack();
    }

    /**
     * Creates a Form GET method URL for the specified browser.
     *
     * @return the GET URL or null when the browser's current item is not a Submit button
     */
    public String createGetSubmitCall() {
////        Component submitItem = this.browser.getFocusedItem();
////        HtmlForm form = (HtmlForm) submitItem.getAttribute(ATTR_FORM);
////        while (form == null && (submitItem instanceof Container)) {
////            submitItem = ((Container) submitItem).getFocusedItem();
////            form =
////                    (HtmlForm) submitItem.getAttribute(ATTR_FORM);
////        }
////        return createGetSubmitCall(submitItem, form);
        return "";
    }

    /**
     * Creates a Form GET method URL for the specified browser.
     *
     * @param submitItem the item that triggered the action
     * @param form the form that contains necessary data
     * @return the GET URL or null when the browser's current item is not a Submit button
     */
    public String createGetSubmitCall(HtmlButton submitItem, HtmlForm form) {

        if (form == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(form.getAction());
        Hashtable elements = form.getFormElements(submitItem);
        Enumeration enumeration = elements.keys();
        char separatorChar = '?';
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = (String) elements.get(name);
            value = Utils.encodeUrl(value);
            sb.append(separatorChar);
            sb.append(name).append('=').append(value);
            separatorChar =
                    '&';
        }

        return sb.toString();
    }

    /**
     * Does a Form POST method call.
     * @param submitItem the item triggering the call
     * @param form the form containing the data
     */
    public void doPostSubmitCall(HtmlButton submitItem, HtmlForm form) {
        try {
            if (form == null) {
                return;
            }

            Hashtable params = null;
            StringBuffer sb = null;
            if (multipart && R.getFileBrowser().isFileSelected()) {
                params = new Hashtable();
            } else {
                sb = new StringBuffer();
            }

            Hashtable elements = form.getFormElements(submitItem);
            Enumeration enumeration = elements.keys();
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                String value = Variables.replace((String) elements.get(name), true);

                //replace friendly coordianates to decimal format
                Component[] items = form.getItems();
                for (int i = 0; i < items.length; i++) {
                    if (items[i] instanceof HtmlTextArea) {
                        HtmlTextArea area = (HtmlTextArea) items[i];
                        String originalValue = area.getAttributeValue();
                        String itemName = area.getAttributeName();
                        if (itemName != null && itemName.equals(name) && (originalValue.equals("$lat") || originalValue.equals("$lon"))) {
                            if (!value.equals("")) {
                                try {
                                    value = String.valueOf(GpsUtils.parseWGS84Coordinate(value));
                                    break;

                                } catch (IllegalArgumentException e) {
                                    if (originalValue.equals("$lat")) {
                                        R.getCustomAlert().quickView(Locale.get("Wrong_latitude"), Dialog.TYPE_WARNING, "locify://refresh");
                                    } else {
                                        R.getCustomAlert().quickView(Locale.get("Wrong_longitude"), Dialog.TYPE_WARNING, "locify://refresh");
                                    }

                                    return;
                                }

                            }
                        }
                    }
                }
                if (multipart && R.getFileBrowser().isFileSelected()) {
                    params.put(name, value);
                } else {
                    value = Utils.encodeUrl(value);
                    sb.append(name).append('=').append(value);
                    if (enumeration.hasMoreElements()) {
                        sb.append('&');
                    }
                }
            }
            if (multipart && R.getFileBrowser().isFileSelected()) {
                HttpMultipartRequest req = new HttpMultipartRequest(
                        R.getHttp().makeAbsoluteURL(form.getAction()),
                        params,
                        fileName, R.getFileBrowser().getFileName(), "unknown/unknown", R.getFileBrowser().getFilePath(), browser);
                req.send();
            } else {
                this.browser.go(form.getAction(), sb.toString());
            }

        } catch (Exception e) {
            R.getErrorScreen().view(e, "XHTMLTagHandler.doPostSubmitCall", null);
        }
    }

    protected void handleSubmitCommand(HtmlButton button) {
        HtmlForm form = button.getAttributeForm();
        if (form == null) {
            return;
        }

        if (form.isPost()) {
            doPostSubmitCall(button, form);
        } else {
            String url = createGetSubmitCall(button, form);
            this.browser.go(url);
        }
    }

    protected void handleLinkCommand(HtmlButton button) {
         String href = (String) button.url;
         if (href != null) {
             this.browser.go(href);
        }
    }

    /**
     * Retrieves the currently focused item that has specified the attribute
     * @param attribute the attribute
     * @param container the container that should have focused the item
     * @return the item that contains the attribute or the focused item which is not a Container itself
     */
//    protected Component getFocusedItemWithAttribute(String attribute, Container container) {
//        Item item = container.getFocusedItem();
////        if (item.getAttribute(attribute) == null && item instanceof Container) {
////            return getFocusedItemWithAttribute(attribute, (Container) item);
////        }
////
//        return item;
//        return null;
//    }

    public void addCommands(String a, String b, String c, Component i) {
////        super.addCommands(a, b, c, i);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof HtmlButton &&
                ((HtmlButton) evt.getSource()).getAttributeType().equals(CMD_LINK)) {
            handleLinkCommand((HtmlButton) evt.getSource());
        } else if (evt.getSource() instanceof HtmlButton &&
                ((HtmlButton) evt.getSource()).getAttributeType().equals(CMD_SUBMIT)) {
            handleSubmitCommand((HtmlButton) evt.getSource());
        } else if (evt.getSource() instanceof HtmlButton &&
                ((HtmlButton) evt.getSource()).getAttributeType().equals(CMD_BACK)) {
            handleBackCommand();
        } else if (evt.getCommand() == Commands.cmdAnotherLocation) { //locify:where
            R.getContext().setBackScreen("locify://htmlBrowser");
            R.getURL().call("locify://setLocation");
        } else if (evt.getCommand() == Commands.cmdSelect) //input type file
        {
            if (fileType.equals("place")) {
                R.getURL().call("locify://files?to=upload&filter=place");
            } else if (fileType.equals("route")) {
                R.getURL().call("locify://files?to=upload&filter=route");
            } else {
                R.getURL().call("locify://filebrowser?type=" + fileType);
            }
        } else if (evt.getCommand() == Commands.cmdContactTel) { // text a type
            R.getContext().setBackScreen("locify://htmlBrowser");
            R.getURL().call("locify://contactsScreen?text=" + browser.getContactTel() +
                    "&type=" + ContactsScreen.FILTER_TEL);
        } else if (evt.getCommand() == Commands.cmdContactEmail) {
            R.getContext().setBackScreen("locify://htmlBrowser");
            R.getURL().call("locify://contactsScreen?text=" + browser.getContactEmail() +
                    "&type=" + ContactsScreen.FILTER_EMAIL);
        }
    }
}
