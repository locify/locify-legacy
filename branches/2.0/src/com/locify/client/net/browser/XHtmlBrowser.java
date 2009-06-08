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
import com.locify.client.locator.LocationContext;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Disables some J2ME Polish HTML browser features and leaves the work to Locify
 * @author Destil
 */
public class XHtmlBrowser extends Container implements Runnable {

    private XHtmlTagHandler XHTMLTagHandler;
    private Label context;
    private Label fileText;
    private TextArea contactTelText;
    private TextArea contactEmailText;
    protected Hashtable tagHandlersByTag = new Hashtable();
    private Hashtable imageCache = new Hashtable();
    protected Vector tagHandlers = new Vector();
    protected Container currentContainer;
    private Style style;

    // history
    protected String currentDocumentBase = null;
    protected Stack history = new Stack();
    private String scheduledHistoryUrl;
    private Thread loadingThread;
    private boolean isRunning;
    private boolean isWorking;
    private boolean isCancelRequested;
    private String nextUrl;
    private String nextPostData;

    public XHtmlBrowser() {
//        super(new BoxLayout(BoxLayout.Y_AXIS));
        super();
        XHTMLTagHandler = new XHtmlTagHandler();
        XHTMLTagHandler.register(this);

        style = new Style();
        style.setBorder(Border.createLineBorder(1));
        style.setFont(ColorsFonts.FONT_SMALL);

        this.loadingThread = new Thread(this);
        this.loadingThread.start();
    }

    public void loadPage(String data) {
        ByteArrayInputStream stream = null;
        XmlPullParser parser;

        try {
            stream = new ByteArrayInputStream(UTF8.encode(data));
            parser = new KXmlParser();
            parser.setInput(new InputStreamReader(stream));

            // Clear out all items in the browser.
            removeAll();
            // Clear image cache when visiting a new page.
            this.imageCache.clear();
            // Really free memory.
            System.gc();
            parsePartialPage(parser);
        //        Object o = this.currentContainer;
        ////        while (o != null && o != this) {
        ////            //System.out.println("closing container with " + this.currentContainer.size() );
        ////            closeContainer();
        ////            o = this.currentContainer;
        ////        }
        } catch (Exception e) {
//            Logger.error("Parsing: wrongFile or data: " + data + " ex: " + e.toString());
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void addItem(Component component) {
//        System.out.println("AddItem: " + component);
        if (currentContainer != null) {
            currentContainer.addComponent(component);
        } else {
            addComponent(component);
        }
    }

    /**
     * @param parser the parser to read the page from
     */
    private void parsePartialPage(XmlPullParser parser) {
        try {
            Hashtable attributeMap = new Hashtable();
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                int type = parser.getEventType();
                if (type == XmlPullParser.START_TAG || type == XmlPullParser.END_TAG) {
                    boolean openingTag = type == XmlPullParser.START_TAG;

                    // #debug
//                    System.out.println("looking for handler for " + parser.getName() + ", openingTag=" + openingTag);
                    attributeMap.clear();
                    XHtmlTagHandler handler = getXHTMLTagHandler(parser, attributeMap);

                    if (handler != null) {
//                        System.out.println("Calling handler: " + parser.getName() + " " + attributeMap);
                        String styleName = (String) attributeMap.get("class");
                        HtmlStyle tagStyle = new HtmlStyle();
//                        if (styleName != null) {
//                            tagStyle = StyleSheet.getStyle(styleName);
//                        }
//                        if (tagStyle == null || styleName == null) {
//                            styleName = (String) attributeMap.get("id");
//                        }
//                        if (styleName != null) {
//                            tagStyle = StyleSheet.getStyle(styleName);
//                        }
                        Container container = this.currentContainer;
                        if (container == null) {
                            container = this;
                        }
                        handler.handleTag(container, parser, parser.getName(), openingTag, attributeMap, tagStyle);
                    } //#if polish.debug.debug
                    else {
//                        System.out.println("found no handler for tag [" + parser.getName() + "]");
                    }
                } else if (type == XmlPullParser.TEXT) {
                    handleText(parser.getText().trim());
                } else {
//                    System.out.println("unknown type: " + type + ", name=" + parser.getName());
                }
            }
        } catch (Exception ex) {
//            System.out.println("error in document...");
        }
//        System.out.println("end of document...");
    }

    private XHtmlTagHandler getXHTMLTagHandler(XmlPullParser parser, Hashtable attributeMap) {
        XHtmlTagHandlerKey key;
        XHtmlTagHandler handler = null;
        String name = parser.getName().toLowerCase();

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attributeName = parser.getAttributeName(i).toLowerCase();
            String attributeValue = parser.getAttributeValue(i);
            attributeMap.put(attributeName, attributeValue);

            key = new XHtmlTagHandlerKey(name, attributeName, attributeValue);
            handler = (XHtmlTagHandler) this.tagHandlersByTag.get(key);

            if (handler != null) {
                break;
            }
        }

        if (handler == null) {
            key = new XHtmlTagHandlerKey(name);
            handler = (XHtmlTagHandler) this.tagHandlersByTag.get(key);
        }
        return handler;
    }

    /**
     * Overides J2ME Polish http sending
     * @param url
     * @param postData
     */
    protected void goImpl(String url, String postData) {
        R.getHtmlScreen().quit();
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
            text = text.replace('\t', '\n');
            Vector data = StringTokenizer.getVector(text, "\n");

            for (int i = 0; i < data.size(); i++) {
                String str = (String) data.elementAt(i);
                str = Utils.replaceString(str, "&nbsp;", " ");
                HtmlTextArea textItem = new HtmlTextArea("", false);
                textItem.setText(str);

                if (this.XHTMLTagHandler.textStyle != null) {
                } else if (this.XHTMLTagHandler.textBold && this.XHTMLTagHandler.textItalic) {
                    textItem.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD | Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
                } else if (this.XHTMLTagHandler.textBold) {
                    textItem.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                } else if (this.XHTMLTagHandler.textItalic) {
                    textItem.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
                } else if (this.XHTMLTagHandler.textLabel) {
                    this.XHTMLTagHandler.labelText = str;
                    return;
                }
                Label lab = new Label(text);
                lab.setEndsWith3Points(false);
                addItem(lab);
                //addItem(textItem);
            }
        }
    }

    public void openContainer(Container container) {
//        System.out.println("Opening nested container " + container);

        Container previousContainer = this.currentContainer;
        if (previousContainer != null) {
            previousContainer.addComponent(container);
        } else {
            addComponent(container);
        }
        //add(container);
        this.currentContainer = container;
    }

    /**
     * Closes the current container
     *
     * If the current container only contains a single item, that item will be extracted and directly appended using the current container's style.
     * @return the previous container, if any is known
     */
    public Container closeContainer() {
        if (this.currentContainer == null) {
            return null;
        }

        //System.out.println("closing container with " + this.currentContainer.size() + " items, previous=" + this.currentContainer.getParent());
        Container current = this.currentContainer;
        Container previousContainer = current.getParent();
        if (previousContainer == this) {
            this.currentContainer = null;
        } else {
            this.currentContainer = previousContainer;
        }

        //System.out.println("closing container with size " + current.size() + ", 0=" + current.get(0));
//		if (current.size() == 1) {
//			Item item = current.get(0);
//			if (item != null) {
//				if (current.getStyle() != null) {
//					item.setStyle( current.getStyle() );
//				}
//				//previousContainer.remove(current);
//				add( item );
//			}
//		} else {
//			add(current);
//		}
        return previousContainer;
    }

    /**
     * Adds custom locify item - <locify:where />
     */
    public void addContextItem() {
        context = new Label();
        context.setAlignment(Label.LEFT);
        context.setTextPosition(Label.RIGHT);

        switch (R.getContext().getSource()) {
            case LocationContext.GPS:
                context.setIcon(IconData.get("locify://icons/gps.png"));
                if (R.getLocator().hasFix()) {
                    context.setText(Locale.get("Valid_gps_position"));
                } else {
                    context.setText(Locale.get("Waiting_for_gps"));
                }
                break;
            case LocationContext.SAVED_LOCATION:
                context.setIcon(IconData.get("locify://icons/savedLocation.png"));
                context.setText(R.getContext().getSourceData());
                break;
            case LocationContext.ADDRESS:
                context.setIcon(IconData.get("locify://icons/address.png"));
                context.setText(R.getContext().getSourceData());
                break;
            case LocationContext.COORDINATES:
                context.setIcon(IconData.get("locify://icons/coordinates.png"));
                context.setText(R.getContext().getSourceData());
                break;
            case LocationContext.LAST_KNOWN:
                context.setIcon(IconData.get("locify://icons/lastKnown.png"));
                context.setText(R.getContext().getSourceData());
                break;
        }

        addItem(new Label(Locale.get("Where") + ":"));
        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        container.addComponent(context);
        Button btnChange = new Button(Locale.get("Change"));
        btnChange.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                R.getContext().setBackScreen("locify://htmlBrowser");
                R.getURL().call("locify://setLocation");
            }
        });
        container.addComponent(btnChange);

        addItem(container);
    }

    /**
     * Updates <locify:where />
     */
    public void updateContextItem() {
        try {
            if (context != null) {
                if (R.getContext().isTemporary()) {
                    context.setIcon(IconData.get("locify://icons/where.png"));
                    context.setText(Locale.get("Temporary_location"));
                } else {
                    switch (R.getContext().getSource()) {
                        case LocationContext.GPS:
                            context.setIcon(IconData.get("locify://icons/gps.png"));
                            if (R.getLocator().hasFix()) {
                                context.setText(Locale.get("Valid_gps_position"));
                            } else {
                                context.setText(Locale.get("Waiting_for_gps"));
                            }
                            break;
                        case LocationContext.SAVED_LOCATION:
                            context.setIcon(IconData.get("locify://icons/savedLocation.png"));
                            context.setText(R.getContext().getSourceData());
                            break;
                        case LocationContext.ADDRESS:
                            context.setIcon(IconData.get("locify://icons/address.png"));
                            context.setText(R.getContext().getSourceData());
                            break;
                        case LocationContext.COORDINATES:
                            context.setIcon(IconData.get("locify://icons/coordinates.png"));
                            context.setText(R.getContext().getSourceData());
                            break;
                        case LocationContext.LAST_KNOWN:
                            context.setIcon(IconData.get("locify://icons/lastKnown.png"));
                            context.setText(R.getContext().getSourceData());
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
        addComponent(new Label(this.XHTMLTagHandler.labelText));
        fileText = new Label(Locale.get("No_file_selected"));
        //#style contextContainer
        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        //#style fileUploadText
        container.addComponent(fileText);
        Button btnBrowse = new Button(Locale.get("Browse"));
        btnBrowse.addActionListener(XHTMLTagHandler);
        //#style contextButton
        container.addComponent(btnBrowse);
        addComponent(container);
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
        addComponent(new Label(Locale.get("Tel") + ":"));
        contactTelText = new TextArea("", 1, 50, TextArea.PHONENUMBER);

        //#style contextContainer
        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        //#style contactsTextField
        container.addComponent(contactTelText);

        if (!Capabilities.isWindowsMobile()) {
            Button btnBrowse = new Button(Locale.get("Browse"));
            btnBrowse.addActionListener(XHTMLTagHandler);
            //#style contextButton
            container.addComponent(btnBrowse);
        }
        addComponent(container);
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
        addComponent(new Label(Locale.get("Email") + ":"));
        contactEmailText = new TextArea("", 1, 50, TextArea.EMAILADDR);

        //#style contextContainer
        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        //#style contactsTextField
        container.addComponent(contactEmailText);

        if (!Capabilities.isWindowsMobile()) {
            Button btnBrowse = new Button(Locale.get("Browse"));
            btnBrowse.addActionListener(XHTMLTagHandler);
            //#style contextButton
            container.addComponent(btnBrowse);
        }

        addComponent(container);
    }

    /**
     * Update contact item
     * @param data data to be written
     */
    public void updateContactEmailItem(String data) {
        contactEmailText.setText(data);
    }

    public void addTagHandler(String tagName, XHtmlTagHandler handler) {
        this.tagHandlersByTag.put(new XHtmlTagHandlerKey(tagName.toLowerCase()), handler);
        if (!this.tagHandlers.contains(handler)) {
            this.tagHandlers.addElement(handler);
        }
    }

    public XHtmlTagHandler getXHtmlTagHandler() {
        return XHTMLTagHandler;
    }

    public String getContactTel() {
        return contactTelText.getText();
    }

    public String getContactEmail() {
        return contactEmailText.getText();
    }


    //////////////////////////// History //////////////////////////////
    /**
     * Schedules the given history document for loading.
     *
     * @param historySteps the steps that should go back, e.g. 1 for the last page that has been shown
     */
    public void go(int historySteps) {
        String entry = null;

        while (historySteps > 0 && this.history.size() > 0) {
            entry = (String) this.history.pop();
            historySteps--;
        }

        if (entry != null) {
            this.scheduledHistoryUrl = entry;
            schedule(entry, null);
            if (this.history.size() == 0 && getComponentForm() != null) {
                getComponentForm().removeCommand(Commands.cmdBack);
            }
        }
    }

    /**
     * Schedules the given URL for loading with HTTP POST data.
     * @param url the URL that should be loaded
     * @param postData the data to be sent via HTTP POST
     */
    public void go(String url, String postData) {
        System.out.println("Browser: going to [" + url + "]");
        if (this.currentDocumentBase != null) {
            this.history.push(this.currentDocumentBase);
            if (this.history.size() == 1 && getComponentForm() != null) {
                getComponentForm().addCommand(Commands.cmdBack);
            }
        }
        schedule(url, postData);
    }

    /**
     * Schedules the given URL for loading.
     * @param url the URL that should be loaded
     */
    public void go(String url) {
        System.out.println("Browser: going to [" + url + "]");
        if (this.currentDocumentBase != null) {
            this.history.push(this.currentDocumentBase);
            if (this.history.size() == 1 && getComponentForm() != null) {
                getComponentForm().addCommand(Commands.cmdBack);
            }
        }
        schedule(url, null);
    }

    /**
     * Goes back one history step.
     *
     * @return true when the browser has a previous document in its history
     * @see #go(int)
     */
    public boolean goBack() {
        if (this.history.size() > 0) {
            go(1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the browser can go back
     * @return true when there is a known previous document
     * @see #goBack()
     */
    public boolean canGoBack() {
        return this.history.size() > 0;
    }

    /**
     * Clears the history
     * @see #goBack()
     * @see #go(int)
     */
    public void clearHistory() {
        this.history.removeAllElements();
        this.imageCache.clear();
//        clear();
//        this.currentDocumentBase = null;
//        if (this.cmdBack != null && getScreen() != null) {
//            getScreen().removeCommand(this.cmdBack);
//        }
    }

    protected void schedule(String url, String postData) {
        this.nextUrl = url;
        this.nextPostData = postData;
        System.out.println("Url: " + url + ", postData: " + postData);
        synchronized (this.loadingThread) {
            this.loadingThread.notify();
        }
    }

    public void run() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        this.isRunning = true;

        while (this.isRunning) {
            try {
                if (this.isRunning && this.nextUrl != null) {
                    this.isWorking = true;
                    String url = this.nextUrl;
                    String postData = this.nextPostData;
                    this.nextUrl = null;
                    this.nextPostData = null;

                    if (this.isCancelRequested != true) {
                        int size = 50 * 1024;
                        byte[] memorySaver = new byte[size];

                        try {
                            goImpl(url, postData);
                        } catch (OutOfMemoryError e) {
                            memorySaver = null;
                            System.gc();
                        } finally {
                            if (memorySaver != null) {
                                memorySaver = null;
                            }
                        }
                    }

                    this.isWorking = false;
                    repaint();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (this.isCancelRequested == true) {
                this.isWorking = false;
                repaint();
                this.isCancelRequested = false;
                this.nextUrl = null;
                this.nextPostData = null;
                loadPage("Request canceled");
            }

            try {
                this.isWorking = false;
                synchronized (this.loadingThread) {
                    this.loadingThread.wait();
                }
            } catch (InterruptedException ie) {
            }
        } // end while(isRunning)
    }
}
