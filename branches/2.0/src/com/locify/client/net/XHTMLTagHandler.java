/*
 * Created on 11-Jan-2006 at 19:20:28.
 * 
 * Copyright (c) 2007 - 2008 Michael Koch / Enough Software
 *
 * This file is part of J2ME Polish.
 *
 * J2ME Polish is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * J2ME Polish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with J2ME Polish; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Commercial licenses are also available, please
 * refer to the accompanying LICENSE.txt or visit
 * http://www.j2mepolish.org for details.
 */
package com.locify.client.net;

import com.locify.client.gui.screen.service.HtmlSelect;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import com.locify.client.gui.screen.service.ContactsScreen;
import com.sun.lwuit.Command;
import java.util.Enumeration;
import java.util.Hashtable;

//#if polish.cldc1.0
//# 	import de.enough.polish.util.TextUtil;
//#endif
/**
 * Handles HTML tags.
 */
public class XHTMLTagHandler
////        extends TagHandler
////        implements ItemCommandListener
{

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
    public static final String ATTR_FORM = "polish_form";
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

    /**
     * Creates a new html tag handler
     */
    public XHTMLTagHandler() {
        //#ifdef polish.i18n.useDynamicTranslations
//# 	  	if (Locale.get("polish.command.followlink") != CMD_LINK.getLabel()) {
//# 	  		CMD_LINK = new Command( Locale.get("polish.command.followlink"), Command.OK, 2 );
//# 	  		CMD_SUBMIT = new Command( Locale.get("polish.command.submit"), Command.ITEM, 2 );
//# 	  		CMD_BACK = new Command( Locale.get("polish.command.back"), Command.BACK, 10 );
//# 	  	}
                    //#endif
                    }

////    public void register(Browser parent) {
////        this.browser = (XHTMLBrowser) parent;
////        this.textBold = false;
////        this.textItalic = false;
////        this.textLabel = false;
////        this.labelText = "";
////
////        parent.addTagHandler(TAG_TITLE, this);
////        parent.addTagHandler(TAG_STYLE, this);
////
////        parent.addTagHandler(TAG_BR, this);
////        parent.addTagHandler(TAG_P, this);
////        parent.addTagHandler(TAG_IMG, this);
////        parent.addTagHandler(TAG_DIV, this);
////        parent.addTagHandler(TAG_SPAN, this);
////        parent.addTagHandler(TAG_A, this);
////        parent.addTagHandler(TAG_B, this);
////        parent.addTagHandler(TAG_STRONG, this);
////        parent.addTagHandler(TAG_I, this);
////        parent.addTagHandler(TAG_EM, this);
////        parent.addTagHandler(TAG_FORM, this);
////        parent.addTagHandler(TAG_INPUT, this);
////        parent.addTagHandler(TAG_BUTTON, this);
////        parent.addTagHandler(TAG_SELECT, this);
////        parent.addTagHandler(TAG_OPTION, this);
////        parent.addTagHandler(TAG_SCRIPT, this);
////        parent.addTagHandler(TAG_TEXT_AREA, this);
////        parent.addTagHandler(TAG_TABLE, this);
////        parent.addTagHandler(TAG_TR, this);
////        parent.addTagHandler(TAG_TH, this);
////        parent.addTagHandler(TAG_TD, this);
////        parent.addTagHandler(TAG_LABEL, this);
////        parent.addTagHandler(TAG_LOCIFY_ELEMENT, this);
////        parent.addTagHandler(TAG_LOCIFY_WHERE, this);
////        parent.addTagHandler(TAG_LOCIFY_VIBRATE, this);
////        parent.addTagHandler(TAG_LOCIFY_BLINK, this);
////        parent.addTagHandler(TAG_LOCIFY_CONTACT, this);
////        parent.addTagHandler(TAG_EMBED, this);
////    }
////
////    /* (non-Javadoc)
////     * @see de.enough.polish.browser.TagHandler#handleTag(de.enough.polish.ui.Container, de.enough.polish.xml.PullParser, java.lang.String, boolean, de.enough.polish.util.HashMap, de.enough.polish.ui.Style)
////     */
////    public boolean handleTag(Container parentItem, SimplePullParser parser, String tagName, boolean opening, HashMap attributeMap, Style style) {
////        try {
////            tagName = tagName.toLowerCase();
////            if (TAG_DIV.equals(tagName) || TAG_SPAN.equals(tagName)) {
////                if (opening) {
////                    this.browser.openContainer(style);
////                } else {
////                    Container container = this.browser.closeContainer();
////                    if (UiAccess.cast(container) instanceof TableItem) {
////                        this.currentTable = (TableItem) UiAccess.cast(container);
////                    }
////                }
////            } else if (TAG_SELECT.equals(tagName)) {
////                if (opening) {
////                    if (this.currentSelect != null) {
////                        //#debug error
////                        System.out.println("Error in HTML-Code: You cannot open a <select>-tag inside another <select>-tag.");
////                        ChoiceGroup choiceGroup = this.currentSelect.getChoiceGroup();
////                        add(choiceGroup);
////                        if (this.currentForm == null) {
////                            //#debug error
////                            System.out.println("Error in HTML-Code: no <form> for <select> element found!");
////                        } else {
////                            this.currentForm.addItem(choiceGroup);
////                        }
////                        this.currentSelect = null;
////                    }
////
////                    String name = parser.getAttributeValue(ATTR_NAME);
////                    String value = null;
////                    if (name != null && this.currentForm.getLocifyElementValue(name) != null) {
////                        value = this.currentForm.getLocifyElementValue(name);
////                    } else {
////                        value = null;
////                    }
////                    String sizeStr = parser.getAttributeValue(ATTR_SIZE);
////                    int size;
////
////                    try {
////                        size = Integer.parseInt(sizeStr);
////                    } catch (NumberFormatException e) {
////                        size = -1;
////                    }
////
////                    boolean isMultiple = parser.getAttributeValue(ATTR_MULTIPLE) != null;
////                    this.currentSelect = new HtmlSelect(name, labelText, size, isMultiple, style, value);
////                } else { // tag is closed
////                    if (this.currentSelect != null) {
////                        ChoiceGroup choiceGroup = this.currentSelect.getChoiceGroup();
////                        add(choiceGroup);
////                        if (this.currentForm == null) {
////                            //#debug error
////                            System.out.println("Error in HTML-Code: no <form> for <select> element found!");
////                        } else {
////                            this.currentForm.addItem(choiceGroup);
////                        }
////                        this.currentSelect = null;
////                    }
////                    //#mdebug error
////                    else {
////                        //#debug error
////                        System.out.println("Error in HTML-Code. You cannot close a <select>-tag without opening one.");
////                    }
////                //#enddebug
////                }
////                return true;
////            } else if (TAG_OPTION.equals(tagName)) {
////                if (this.currentSelect != null && opening) {
////                    // TODO: handle "selected" attribute.
////                    String value = parser.getAttributeValue(ATTR_VALUE);
////                    String selected = parser.getAttributeValue("selected");
////                    parser.next();
////                    String name = parser.getText();
////
////                    if (value == null) {
////                        value = name;
////                    }
////
////                    this.currentSelect.addOption(name, value, selected != null, style);
////                }
////                return true;
////            }
////
////            if (opening) {
////                if (TAG_TITLE.equals(tagName)) {
////                    // Hack to read title.
////                    parser.next();
////                    String name = parser.getText();
////                    Screen myScreen = this.browser.getScreen();
////                    if (name != null && myScreen != null) {
////                        myScreen.setTitle(name);
////                    }
////                    return true;
////                } else if (TAG_STYLE.equals(tagName)) {
////                    // Hack to read style content.
////                    parser.next();
////                    return true;
////                } else if (TAG_A.equals(tagName)) {
////                    String href = (String) attributeMap.get(ATTR_HREF);
////                    parser.next();
////                    Item linkItem;
////                    if (href != null) {
////                        String anchorText = parser.getText();
////                        // hack for image links:
////                        if ("".equals(anchorText) && TAG_IMG.equals(parser.getName())) {
////                            // this is an image link:
////                            attributeMap.clear();
////                            for (int i = 0; i < parser.getAttributeCount(); i++) {
////                                String attributeName = parser.getAttributeName(i);
////                                String attributeValue = parser.getAttributeValue(i);
////                                attributeMap.put(attributeName, attributeValue);
////                            }
////                            String src = (String) attributeMap.get("src");
////                            String url = R.getHttp().makeAbsoluteURL(src);
////                            Image image = this.browser.loadImage(url);
////                            linkItem = new ImageItem(null, image, 0, (String) attributeMap.get("alt"));
////                        //this.browser.loadImageLater( url, (ImageItem) linkItem );
////
////                        } else {
////                            //#style browserLink
////                            linkItem = new StringItem(null, anchorText);
////                        }
////                        linkItem.setDefaultCommand(CMD_LINK);
////                        linkItem.setItemCommandListener(this);
////                        linkItem.setAttribute(ATTR_HREF, href);
////                        addCommands(TAG_A, linkItem);
////                    } else {
////                        //#style browserText
////                        linkItem = new StringItem(null, parser.getText());
////                    }
////                    if (style != null) {
////                        linkItem.setStyle(style);
////                    }
////                    add(linkItem);
////                    return true;
////                } else if (TAG_BR.equals(tagName)) {
////                    // TODO: Can we do this without adding a dummy StringItem?
////                    //#style breakline
////                    StringItem stringItem = new StringItem(null, null);
////                    add(stringItem);
////                    return true;
////                } else if (TAG_P.equals(tagName)) {
////                    //#style breakline
////                    StringItem stringItem = new StringItem(null, null);
////                    add(stringItem);
////                    if (opening) {
////                        this.textStyle = style;
////                    }
////                    return true;
////                } else if (TAG_IMG.equals(tagName)) {
////                    String src = (String) attributeMap.get("src");
////                    String url = R.getHttp().makeAbsoluteURL(src);
////                    Image image = this.browser.loadImage(url);
////                    if (image != null) {
////                        ImageItem item = new ImageItem(null, image, Item.LAYOUT_DEFAULT, "");
////                        if (style != null) {
////                            item.setStyle(style);
////                        }
////
////                        add(item);
////                    }
////                    return true;
////                } else if (TAG_TEXT_AREA.equals(tagName)) {
////                    parser.next();
////                    String value = parser.getText();
////                    int maxCharNumber = 500;
////                    String cols = (String) attributeMap.get("cols");
////                    String rows = (String) attributeMap.get("rows");
////                    if (cols != null && rows != null) {
////                        try {
////                            maxCharNumber = Integer.parseInt(cols) * Integer.parseInt(rows);
////                        } catch (Exception e) {
////                            //#debug error
////                            System.out.println("Unable to parse textarea cols or rows attribute: cols=" + cols + ", rows=" + rows);
////                        }
////                    }
////                    TextField textField = new TextField(null, value, maxCharNumber, TextField.ANY);
////                    if (style != null) {
////                        textField.setStyle(style);
////                    }
////                    add(textField);
////                    if (this.currentForm != null) {
////                        this.currentForm.addItem(textField);
////                        textField.setAttribute(ATTR_FORM, this.currentForm);
////                        String name = (String) attributeMap.get(INPUT_NAME);
////                        if (value == null) {
////                            value = name;
////                        }
////                        if (name != null) {
////                            textField.setAttribute(ATTR_NAME, name);
////                            textField.setAttribute(ATTR_VALUE, value);
////                        }
////                    }
////                    return true;
////                } else if (TAG_BUTTON.equals(tagName) && this.currentForm != null) {
////                    String name = (String) attributeMap.get(INPUT_NAME);
////                    String value = null;
////                    if (this.currentForm.getLocifyElementValue(name) != null) {
////                        value = this.currentForm.getLocifyElementValue(name);
////                    } else {
////                        value = (String) attributeMap.get(INPUT_VALUE);
////                    }
////
////                    if (value == null) {
////                        value = name;
////                    }
////                    //#style button
////                    StringItem buttonItem = new StringItem(null, value);
////                    if (style != null) {
////                        buttonItem.setStyle(style);
////                    }
////                    buttonItem.setDefaultCommand(CMD_SUBMIT);
////                    buttonItem.setItemCommandListener(this);
////                    addCommands(TAG_INPUT, INPUT_TYPE, INPUTTYPE_SUBMIT, buttonItem);
////                    add(buttonItem);
////
////                    this.currentForm.addItem(buttonItem);
////                    buttonItem.setAttribute(ATTR_FORM, this.currentForm);
////                    buttonItem.setAttribute(ATTR_TYPE, "submit");
////
////                    if (name != null) {
////                        buttonItem.setAttribute(ATTR_NAME, name);
////                        buttonItem.setAttribute(ATTR_VALUE, value);
////                    }
////                } else if (TAG_INPUT.equals(tagName)) {
////                    if (this.currentForm != null) {
////                        String type = (String) attributeMap.get(INPUT_TYPE);
////                        String name = (String) attributeMap.get(INPUT_NAME);
////                        String value = null;
////                        if (name != null && this.currentForm.getLocifyElementValue(name) != null) {
////                            value = this.currentForm.getLocifyElementValue(name);
////                        } else {
////                            value = (String) attributeMap.get(INPUT_VALUE);
////                        }
////
////                        if (this.formListener != null && name != null) {
////                            value = this.formListener.verifyInitialFormValue(this.currentForm.getAction(), name, value);
////                        }
////
////                        if (INPUTTYPE_TEXT.equals(type)) {
////                            //#style textfield
////                            TextField textField = new TextField(labelText, value, 1000, TextField.ANY);
////                            if (style != null) {
////                                textField.setStyle(style);
////                            }
////                            add(textField);
////
////                            this.currentForm.addItem(textField);
////                            textField.setAttribute(ATTR_FORM, this.currentForm);
////
////                            if (name != null) {
////                                textField.setAttribute(ATTR_NAME, name);
////                                if (value == null) {
////                                    value = "";
////                                }
////                                textField.setAttribute(ATTR_VALUE, value);
////                            }
////                        } else if (INPUTTYPE_SUBMIT.equals(type)) {
////
////                            if (value == null) {
////                                value = name;
////                            }
////                            //#style button
////                            StringItem buttonItem = new StringItem(null, value);
////                            if (style != null) {
////                                buttonItem.setStyle(style);
////                            }
////                            buttonItem.setDefaultCommand(CMD_SUBMIT);
////                            buttonItem.setItemCommandListener(this);
////                            addCommands(TAG_INPUT, INPUT_TYPE, INPUTTYPE_SUBMIT, buttonItem);
////                            add(buttonItem);
////
////                            this.currentForm.addItem(buttonItem);
////                            buttonItem.setAttribute(ATTR_FORM, this.currentForm);
////                            buttonItem.setAttribute(ATTR_TYPE, "submit");
////
////                            if (name != null) {
////                                buttonItem.setAttribute(ATTR_NAME, name);
////                                buttonItem.setAttribute(ATTR_VALUE, value);
////                            }
////                        } else if (INPUTTYPE_HIDDEN.equals(type)) {
////                            this.currentForm.addHiddenElement(name, value);
////                        } else if (INPUTTYPE_FILE.equals(type)) {
////                            if (opening) {
////                                fileName = name;
////                                String accept = (String) attributeMap.get("accept");
////                                if (accept == null) {
////                                    fileType = "";
////                                } else if (accept.equals("image/*")) {
////                                    fileType = "image";
////                                } else if (accept.equals("video/*")) {
////                                    fileType = "video";
////                                } else if (accept.equals("application/vnd.locify.place")) {
////                                    fileType = "place";
////                                } else if (accept.equals("application/vnd.locify.route")) {
////                                    fileType = "route";
////                                } else {
////                                    fileType = "";
////                                }
////                                browser.addFileItem();
////                            }
////                            return true;
////                        }
////                    //#if polish.debug.debug
//////#           else
//////#           {
////                    //#debug
//////#             System.out.println("unhandled html form input type: " + type);
//////#           }
////                    //#endif
////                    }
////
////                    return true;
////                } else if (TAG_SCRIPT.equals(tagName)) {
////                    // Consume javascript code.
////                    parser.next();
////                    return true;
////                } else if (TAG_TABLE.equals(tagName)) {
////                    //#style browserTable?
////                    TableItem table = new TableItem();
////                    table.setSelectionMode(TableItem.SELECTION_MODE_CELL | TableItem.SELECTION_MODE_INTERACTIVE);
////                    table.setCellContainerStyle(this.browser.getStyle());
////                    if (style != null) {
////                        table.setStyle(style);
////                    }
////
////                    this.currentTable = table;
////                    this.browser.openContainer(table);
////                    return true;
////                } else if (this.currentTable != null && TAG_TR.equals(tagName)) {
////                    this.currentTable.moveToNextRow();
////                    return true;
////                } else if (this.currentTable != null && TAG_TH.equals(tagName)) {
////                    //TODO differentiate between th and td
////                    this.currentTable.moveToNextColumn();
////                    return true;
////                } else if (this.currentTable != null && TAG_TD.equals(tagName)) {
////                    //TODO differentiate between th and td
////                    this.currentTable.moveToNextColumn();
////                    return true;
////                }
////            } else {
////                // the tag is being closed:
////                if (TAG_TABLE.equals(tagName)) {
////                    Container container = this.browser.closeContainer();
////                    if (UiAccess.cast(container) instanceof TableItem) {
////                        this.currentTable = (TableItem) UiAccess.cast(container);
////                    } else {
////                        this.currentTable = null;
////                    }
////                    return true;
////                }
////            }
////            if (TAG_B.equals(tagName) || TAG_STRONG.equals(tagName)) {
////                this.textBold = opening;
////                return true;
////            } else if (TAG_I.equals(tagName) || TAG_EM.equals(tagName)) {
////                this.textItalic = opening;
////                return true;
////            } else if (TAG_FORM.equals(tagName)) {
////                if (opening) {
////                    String name = (String) attributeMap.get("name");
////                    String action = (String) attributeMap.get("action");
////                    String method = (String) attributeMap.get("method");
////                    multipart = false;
////                    if ("multipart/form-data".equals(attributeMap.get("enctype"))) {
////                        multipart = true;
////                    }
////                    if (method == null) {
////                        method = "GET";
////                    }
////                    this.currentForm = new HtmlForm(name, action, method.toUpperCase());
////                } else {
////                    this.currentForm = null;
////                }
////
////                return true;
////            } else if (TAG_LABEL.equals(tagName)) {
////                this.textLabel = opening;
////                return true;
////            } else if (TAG_LOCIFY_ELEMENT.equals(tagName)) {
////                this.currentForm.addLocifyElement((String) attributeMap.get("name"), (String) attributeMap.get("value"));
////                return true;
////            } else if (TAG_LOCIFY_WHERE.equals(tagName)) {
////                if (opening) {
////                    this.browser.addContextItem();
////                }
////                return true;
////            } else if (TAG_LOCIFY_VIBRATE.equals(tagName)) {
////                if (opening) {
////                    int duration = 500;
////                    if (attributeMap.get("duration") != null) {
////                        duration = Integer.parseInt((String) attributeMap.get("duration"));
////                    }
////                    R.getMidlet().getDisplay().vibrate(duration);
////                }
////                return true;
////            } else if (TAG_LOCIFY_BLINK.equals(tagName)) {
////                if (opening) {
////                    int duration = 500;
////                    if (attributeMap.get("duration") != null) {
////                        duration = Integer.parseInt((String) attributeMap.get("duration"));
////                    }
////                    R.getMidlet().getDisplay().flashBacklight(duration);
////                }
////                return true;
////            // <locify:contact name="telefon" type="tel" />
////            } else if (TAG_LOCIFY_CONTACT.equalsIgnoreCase(tagName)) {
////                if (opening) {
////                    String type = (String) attributeMap.get("type");
////                    if (type.equals("tel")) {
////                        this.currentForm.addLocifyPIM((String) attributeMap.get("name"), "tel");
////                        this.browser.addContactTelItem();
////                    } else if (type.equals("email")) {
////                        this.currentForm.addLocifyPIM((String) attributeMap.get("name"), "email");
////                        this.browser.addContactEmailItem();
////                    }
////                }
////                return true;
////            } else if (TAG_EMBED.equals(tagName)) {
////                if (opening) {
////                    if (attributeMap.get("src") != null) {
////                        String file = (String) attributeMap.get("src");
////                        if (file.endsWith("wav"))
////                        {
////                            R.getAudio().play(R.getHttp().makeAbsoluteURL(file));
////                        }
////                    }
////                }
////                return true;
////            }
////            return false;
////        } catch (Exception e) {
////            R.getErrorScreen().view(e, "XHTMLTagHandler.handle", "Tag:" + tagName + ", Opening:" + opening + ", Attributes:" + attributeMap.toString());
////            return false;
////        }
////
////    }
////
////    /**
////     * Adds an item either to the browser or to the current table.
////     * @param item the item
////     */
////    private void add(Item item) {
////        this.browser.add(item);
////    }
////
////    /* (non-Javadoc)
////     * @see de.enough.polish.browser.TagHandler#handleCommand(javax.microedition.lcdui.Command)
////     */
////    public boolean handleCommand(Command command) {
////        if (command == CMD_LINK) {
////            handleLinkCommand();
////            return true;
////        } else if (command == CMD_SUBMIT) {
////            handleSubmitCommand();
////            return true;
////        } else if (command == CMD_BACK) {
////            handleBackCommand();
////            return true;
////        } else if (command == Commands.cmdAnotherLocation) { //locify:where
////            R.getContext().setBackScreen("locify://htmlBrowser");
////            R.getURL().call("locify://setLocation");
////        } else if (command == Commands.cmdSelect) //input type file
////        {
////            if (fileType.equals("place")) {
////                R.getURL().call("locify://files?to=upload&filter=place");
////            } else if (fileType.equals("route")) {
////                R.getURL().call("locify://files?to=upload&filter=route");
////            } else {
////                R.getURL().call("locify://filebrowser?type=" + fileType);
////            }
////        } else if (command == Commands.cmdContactTel) { // text a type
////            R.getContext().setBackScreen("locify://htmlBrowser");
////            R.getURL().call("locify://contactsScreen?text=" + browser.getContactTel() +
////                    "&type=" + ContactsScreen.FILTER_TEL);
////        } else if (command == Commands.cmdContactEmail) {
////            R.getContext().setBackScreen("locify://htmlBrowser");
////            R.getURL().call("locify://contactsScreen?text=" + browser.getContactEmail() +
////                    "&type=" + ContactsScreen.FILTER_EMAIL);
////        }
////
////        return false;
////    }
////
////    protected void handleBackCommand() {
////        this.browser.goBack();
////    }
////
////    /**
////     * Creates a Form GET method URL for the specified browser.
////     *
////     * @return the GET URL or null when the browser's current item is not a Submit button
////     */
////    public String createGetSubmitCall() {
////        Item submitItem = this.browser.getFocusedItem();
////        HtmlForm form = (HtmlForm) submitItem.getAttribute(ATTR_FORM);
////        while (form == null && (submitItem instanceof Container)) {
////            submitItem = ((Container) submitItem).getFocusedItem();
////            form =
////                    (HtmlForm) submitItem.getAttribute(ATTR_FORM);
////        }
////
////        return createGetSubmitCall(submitItem, form);
////    }
////
////    /**
////     * Creates a Form GET method URL for the specified browser.
////     *
////     * @param submitItem the item that triggered the action
////     * @param form the form that contains necessary data
////     * @return the GET URL or null when the browser's current item is not a Submit button
////     */
////    public String createGetSubmitCall(
////            Item submitItem, HtmlForm form) {
////
////        if (form == null) {
////            return null;
////        }
////
////        StringBuffer sb = new StringBuffer();
////        sb.append(form.getAction());
////        Hashtable elements = form.getFormElements(this.formListener, submitItem);
////        Enumeration enumeration = elements.keys();
////        char separatorChar = '?';
////        while (enumeration.hasMoreElements()) {
////            String name = (String) enumeration.nextElement();
////            String value = (String) elements.get(name);
////            value =
////                    TextUtil.encodeUrl(value);
////            sb.append(separatorChar);
////            sb.append(name).append('=').append(value);
////            separatorChar =
////                    '&';
////        }
////
////        return sb.toString();
////    }
////
////    /**
////     * Does a Form POST method call.
////     * @param submitItem the item triggering the call
////     * @param form the form containing the data
////     */
////    public void doPostSubmitCall(Item submitItem, HtmlForm form) {
////        try {
////            if (form == null) {
////                return;
////            }
////
////            Hashtable params = null;
////            StringBuffer sb = null;
////            if (multipart && R.getFileBrowser().isFileSelected()) {
////                params = new Hashtable();
////            } else {
////                sb = new StringBuffer();
////            }
////
////            Hashtable elements = form.getFormElements(this.formListener, submitItem);
////            Enumeration enumeration = elements.keys();
////            while (enumeration.hasMoreElements()) {
////                String name = (String) enumeration.nextElement();
////                String value = Variables.replace((String) elements.get(name), true);
////
////                //replace friendly coordianates to decimal format
////                Item[] items = form.getItems();
////                for (int i = 0; i < items.length; i++) {
////                    Item item = items[i];
////                    if (item instanceof TextField) {
////                        String originalValue = (String) item.getAttribute("value");
////                        String itemName = (String) item.getAttribute("name");
////                        if (itemName != null && itemName.equals(name) && (originalValue.equals("$lat") || originalValue.equals("$lon"))) {
////                            if (!value.equals("")) {
////                                try {
////                                    value = String.valueOf(GpsUtils.parseWGS84Coordinate(value));
////                                    break;
////
////                                } catch (IllegalArgumentException e) {
////                                    if (originalValue.equals("$lat")) {
////                                        R.getCustomAlert().quickView(Locale.get("Wrong_latitude"), "Warning", "locify://refresh");
////                                    } else {
////                                        R.getCustomAlert().quickView(Locale.get("Wrong_longitude"), "Warning", "locify://refresh");
////                                    }
////
////                                    return;
////                                }
////
////                            }
////                        }
////                    }
////                }
////                if (multipart && R.getFileBrowser().isFileSelected()) {
////                    params.put(name, value);
////                } else {
////                    value = TextUtil.encodeUrl(value);
////                    sb.append(name).append('=').append(value);
////                    if (enumeration.hasMoreElements()) {
////                        sb.append('&');
////                    }
////
////                }
////            }
////            if (multipart && R.getFileBrowser().isFileSelected()) {
////                HttpMultipartRequest req = new HttpMultipartRequest(
////                        R.getHttp().makeAbsoluteURL(form.getAction()),
////                        params,
////                        fileName, R.getFileBrowser().getFileName(), "unknown/unknown", R.getFileBrowser().getFilePath(), browser);
////                req.send();
////            } else {
////                this.browser.go(form.getAction(), sb.toString());
////            }
////
////        } catch (Exception e) {
////            R.getErrorScreen().view(e, "XHTMLTagHandler.doPostSubmitCall", null);
////        }
////
////    }
////
////    protected void handleSubmitCommand() {
////        Item submitItem = this.browser.getFocusedItem();
////        HtmlForm form = (HtmlForm) submitItem.getAttribute(ATTR_FORM);
////        while (form == null && (submitItem instanceof Container)) {
////            submitItem = ((Container) submitItem).getFocusedItem();
////            form =
////                    (HtmlForm) submitItem.getAttribute(ATTR_FORM);
////        }
////
////        if (form == null) {
////            return;
////        }
////
////        if (form.isPost()) {
////            doPostSubmitCall(submitItem, form);
////        } else {
////            String url = createGetSubmitCall(submitItem, form);
////            this.browser.go(url);
////        }
////
////    }
////
////    protected void handleLinkCommand() {
//////#if polish.usePolishGui
//////#         Item linkItem = getFocusedItemWithAttribute(ATTR_HREF, this.browser);
//////#         String href = (String) linkItem.getAttribute(ATTR_HREF);
//////#         if (href != null) {
//////#             this.browser.go(href);
//////#         }
////        //#endif
////        //#if polish.debug.error
//////#     else {
////        //#debug error
//////#     	System.out.println("Unable to handle link command for item " + linkItem + ": no " + ATTR_HREF + " attribute found.");
//////#     }
////        //#endif
////    }
////    //#if polish.LibraryBuild
//////# 	private Item getFocusedItemWithAttribute(String attribute, de.enough.polish.ui.FakeContainerCustomItem container )
//////# 	{
//////# 		return null;
//////# 	}
////    //#endif
////
////    /**
////     * Retrieves the currently focused item that has specified the attribute
////     * @param attribute the attribute
////     * @param container the container that should have focused the item
////     * @return the item that contains the attribute or the focused item which is not a Container itself
////     */
////    protected Item getFocusedItemWithAttribute(String attribute, Container container) {
////        Item item = container.getFocusedItem();
////        if (item.getAttribute(attribute) == null && item instanceof Container) {
////            return getFocusedItemWithAttribute(attribute, (Container) item);
////        }
////
////        return item;
////    }
////
////    /**
////     * Handles item commands (implements ItemCommandListener).
////     *
////     * @param command the command
////     * @param item the item from which the command originates
////     */
////    public void commandAction(Command command, Item item) {
////        handleCommand(command);
////    }
////
////    /**
////     * Sets the form listener that is notified about form creation and submission events
////     *
////     * @param listener the listener, use null for de-registering a previous listener
////     */
////    public void setFormListener(FormListener listener) {
////        this.formListener = listener;
////    }
////
////    public void addCommands(String a, String b, String c, Item i) {
////        super.addCommands(a, b, c, i);
////    }
                    }
