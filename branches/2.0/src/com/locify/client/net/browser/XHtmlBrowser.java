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
import com.locify.client.locator.LocationContext;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
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
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.layouts.GroupLayout;
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
public class XHtmlBrowser implements Runnable {

    private Container container;
    private Label contextIcon;
    private Label contextText;
    private Label fileText;
    private TextArea contactTelText;
    private TextArea contactEmailText;

    private XHtmlTagHandler XHTMLTagHandler;
    protected Hashtable tagHandlersByTag = new Hashtable();
//    protected Vector tagHandlers = new Vector();

    private Hashtable imageCache = new Hashtable();
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

    public XHtmlBrowser(Container container) {
        this.container = container;
        container.setLayout(new FlowLayoutYScroll());
        currentContainer = container;

        XHTMLTagHandler = new XHtmlTagHandler();
        XHTMLTagHandler.register(this);

        style = new Style();
        style.setBorder(Border.createLineBorder(1));
        style.setFont(ColorsFonts.FONT_SMALL);

        this.loadingThread = new Thread(this);
        this.loadingThread.start();
    }

    public Container getContainer() {
        return container;
    }

    public void addTagHandler(String tagName, XHtmlTagHandler handler) {
        this.tagHandlersByTag.put(new XHtmlTagHandlerKey(tagName.toLowerCase()), handler);
//        if (!this.tagHandlers.contains(handler)) {
//            this.tagHandlers.addElement(handler);
//        }
    }

    public XHtmlTagHandler getXHtmlTagHandler() {
        return XHTMLTagHandler;
    }
//    // html scrolling style
//    Rectangle rect = new Rectangle(getScrollX(), getScrollY(), getWidth(), getHeight());
//    int step = Font.getDefaultFont().getHeight();
//    boolean reachToBottom = false;
//    boolean reachToTop = true;
//
//    public void keyPressed(int keyCode) {
//        int action = Display.getInstance().getGameAction(keyCode);
//        if (action == Display.GAME_DOWN) {
//            if (reachToBottom) { // scroll back
//                rect = new Rectangle(getScrollX(), getScrollY(), getWidth(), getHeight());
//                scrollRectToVisible(rect, this);
//                reachToBottom = false;
//                reachToTop = true;
//                return;
//            }
//
//            reachToBottom = false;
//            reachToTop = false;
//            int testHeight = getScrollY() + getHeight();
//
//            if (testHeight < getPreferredH()) {
//                rect.setY(rect.getY() - step);
//                scrollRectToVisible(rect, this);
//                scrollRectToVisible(rect, this);
//            } else {
//                rect = new Rectangle(getScrollX(), getPreferredH() - getHeight(), getWidth(), getHeight());
//                scrollRectToVisible(rect, this);
//                reachToBottom = true;
//                reachToTop = false;
//            }
//        } else {
//            super.keyPressed(keyCode);
//        }
//    }

    public void loadPage(String data) {
        Utils.printMemoryState("XHtmlBrowser - loadPage()");
        ByteArrayInputStream stream = null;
        XmlPullParser parser;
//System.out.println("\nLoadPage: " + data);

        data = Utils.replaceString(data, "&nbsp;", " ");
//        data = UTF8.decode(data.getBytes(), 0, data.length());
//        data = data.replace((char) 10, ' ');
//        data = data.replace((char) 13, ' ');
//System.out.println("\nLoadPage: " + data);
        try {
            //stream = new ByteArrayInputStream(UTF8.encode(data));
            stream = new ByteArrayInputStream(data.getBytes());
            parser = new KXmlParser();
            parser.setInput(new InputStreamReader(stream));

            // Clear out all items in the browser.
            container.removeAll();

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
            Logger.error("XHtmlBrowser.loadPage() - parsing: wrongFile or data: " + data + " ex: " + e.toString());
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

    public void addComponent(Component component) {
System.out.println("AddComponent: " + component + ", container: " + currentContainer);
        if (component instanceof Label) {
            ((Label) component).getStyle().setBorder(Border.createLineBorder(1, ColorsFonts.RED));
        } else if (component instanceof Container) {
            ((Container) component).getStyle().setBorder(Border.createLineBorder(1, ColorsFonts.MAGENTA));
        }

        currentContainer.addComponent(component);
    }

    /**
     * @param parser the parser to read the page from
     */
    private void parsePartialPage(XmlPullParser parser) {
        try {
            Hashtable attributeMap = new Hashtable();
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                int type = parser.getEventType();
//System.out.println("PARSER_TYPE: " + type);
                if (type == XmlPullParser.START_TAG || type == XmlPullParser.END_TAG) {
                    boolean openingTag = type == XmlPullParser.START_TAG;

                    // #debug
//                    System.out.println("looking for handler for " + parser.getName() + ", openingTag=" + openingTag);
                    attributeMap.clear();
                    XHtmlTagHandler handler = getXHTMLTagHandler(parser, attributeMap);

                    if (handler != null) {
//                        System.out.println("Calling handler: " + parser.getName() + " " + attributeMap);
//                        String styleName = (String) attributeMap.get("class");
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
                        handler.handleTag(currentContainer, parser, parser.getName(), openingTag, attributeMap, tagStyle);
                    } else {
//                        System.out.println("found no handler for tag [" + parser.getName() + "]");
                    }
                } else if (type == XmlPullParser.TEXT) {
                    handleText(parser.getText().trim());
                } else {
//                    System.out.println("unknown type: " + type + ", name=" + parser.getName());
                }
            }
        } catch (Exception ex) {
//            System.out.println("error in document... ex: " + ex.toString());
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
     * Support for <label>s
     * @param text
     */
    protected void handleText(String text) {
        if (text.length() > 0) {
            System.out.println("XHtmlBrowser: handleText: '" + text + "'");
            text = text.replace('\t', '\n');
            Vector data = StringTokenizer.getVector(text, "\n");

            Font currentFont = currentContainer.getStyle().getFont();
            for (int i = 0; i <
                    data.size(); i++) {

                String str = (String) data.elementAt(i);
                Component component;

                if (currentFont.stringWidth(str) < Capabilities.getWidth()) {
                    component = new Label(str);
                } else {
                    component = new HtmlTextArea(str);
                }

                if (this.XHTMLTagHandler.textStyle != null) {
                } else if (this.XHTMLTagHandler.textBold && this.XHTMLTagHandler.textItalic) {
                    component.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD | Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
                } else if (this.XHTMLTagHandler.textBold) {
                    component.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                } else if (this.XHTMLTagHandler.textItalic) {
                    component.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM));
                } else if (this.XHTMLTagHandler.textLabel) {
                    this.XHTMLTagHandler.labelText = str;
                    return;
                }
                addComponent(component);
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
        current.revalidate();
        if (previousContainer instanceof FormLocify) {
            this.currentContainer = container;
        } else {
            this.currentContainer = previousContainer;
        }

//System.out.println("closing container " + current.toString());
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
        contextIcon = new Label();
        contextText = new Label();

        switch (R.getContext().getSource()) {
            case LocationContext.GPS:
                contextIcon.setIcon(IconData.get("locify://icons/gps.png"));
                if (R.getLocator().hasFix()) {
                    contextText.setText(Locale.get("Valid_gps_position"));
                } else {
                    contextText.setText(Locale.get("Waiting_for_gps"));
                }

                break;
            case LocationContext.SAVED_LOCATION:
                contextIcon.setIcon(IconData.get("locify://icons/savedLocation.png"));
                contextText.setText(R.getContext().getSourceData());
                break;

            case LocationContext.ADDRESS:
                contextIcon.setIcon(IconData.get("locify://icons/address.png"));
                contextText.setText(R.getContext().getSourceData());
                break;

            case LocationContext.COORDINATES:
                contextIcon.setIcon(IconData.get("locify://icons/coordinates.png"));
                contextText.setText(R.getContext().getSourceData());
                break;

            case LocationContext.LAST_KNOWN:
                contextIcon.setIcon(IconData.get("locify://icons/lastKnown.png"));
                contextText.setText(R.getContext().getSourceData());
                break;

        }
        Label where = new Label(Locale.get("Where") + ":");
        addComponent(where);
        addNewLine();
        Button btnChange = new Button(Locale.get("Change"));
        btnChange.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                R.getContext().setBackScreen("locify://htmlBrowser");
                R.getURL().call("locify://setLocation");
            }
        });

        Container container = new Container(new GridLayout(2, 1));
        container.getStyle().setBorder(Border.createLineBorder(1));

        GroupLayout layout = new GroupLayout(container);
        container.setLayout(layout);

        layout.setAutocreateGaps(true);
        layout.setAutocreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().add(contextIcon).add(layout.createParallelGroup().add(contextText).add(btnChange)));
        layout.setVerticalGroup(layout.createSequentialGroup().add(layout.createParallelGroup().add(contextIcon).add(contextText)).add(btnChange));

        addComponent(container);
    }

    /**
     * Updates <locify:where />
     */
    public void updateContextItem() {
        try {
            if (contextText != null) {
                if (R.getContext().isTemporary()) {
                    contextIcon.setIcon(IconData.get("locify://icons/where.png"));
                    contextText.setText(Locale.get("Temporary_location"));
                } else {
                    switch (R.getContext().getSource()) {
                        case LocationContext.GPS:
                            contextIcon.setIcon(IconData.get("locify://icons/gps.png"));
                            if (R.getLocator().hasFix()) {
                                contextText.setText(Locale.get("Valid_gps_position"));
                            } else {
                                contextText.setText(Locale.get("Waiting_for_gps"));
                            }

                            break;
                        case LocationContext.SAVED_LOCATION:
                            contextIcon.setIcon(IconData.get("locify://icons/savedLocation.png"));
                            contextText.setText(R.getContext().getSourceData());
                            break;

                        case LocationContext.ADDRESS:
                            contextIcon.setIcon(IconData.get("locify://icons/address.png"));
                            contextText.setText(R.getContext().getSourceData());
                            break;

                        case LocationContext.COORDINATES:
                            contextIcon.setIcon(IconData.get("locify://icons/coordinates.png"));
                            contextText.setText(R.getContext().getSourceData());
                            break;

                        case LocationContext.LAST_KNOWN:
                            contextIcon.setIcon(IconData.get("locify://icons/lastKnown.png"));
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
        addComponent(new Label(this.XHTMLTagHandler.labelText));
        fileText =
                new Label(Locale.get("No_file_selected"));
        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        container.addComponent(fileText);
        Button btnBrowse = new Button(Locale.get("Browse"));
        btnBrowse.addActionListener(XHTMLTagHandler);
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
        addComponent(new Label(Locale.get("Tel") + ":"));
        contactTelText =
                new TextArea("", 1, 50, TextArea.PHONENUMBER);

        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        container.addComponent(contactTelText);

        if (!Capabilities.isWindowsMobile()) {
            Button btnBrowse = new Button(Locale.get("Browse"));
            btnBrowse.addActionListener(XHTMLTagHandler);
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
        addComponent(new Label(Locale.get("Email") + ":"));
        contactEmailText =
                new TextArea("", 1, 50, TextArea.EMAILADDR);

        Container cont = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        cont.addComponent(contactEmailText);

        if (!Capabilities.isWindowsMobile()) {
            Button btnBrowse = new Button(Locale.get("Browse"));
            btnBrowse.addActionListener(XHTMLTagHandler);
            cont.addComponent(btnBrowse);
        }

        addComponent(cont);
    }

    /**
     * Update contact item
     * @param data data to be written
     */
    public void updateContactEmailItem(String data) {
        contactEmailText.setText(data);
    }

    public String getContactTel() {
        return contactTelText.getText();
    }

    public String getContactEmail() {
        return contactEmailText.getText();
    }

    /**
     * Adds new line to the page
     */
    public void addNewLine() {
        Label stringItem = new Label("");
        stringItem.getStyle().setMargin(0, 0, 500, 500);
        addComponent(stringItem);
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
            if (this.history.size() == 0 && container.getComponentForm() != null) {
                container.getComponentForm().removeCommand(Commands.cmdBack);
            }

        }
    }

    /**
     * Schedules the given URL for loading with HTTP POST data.
     * @param url the URL that should be loaded
     * @param postData the data to be sent via HTTP POST
     */
    public void go(String url, String postData) {
System.out.println("XHtmlBrowser.go(" + url + " ," + postData + ")");
        if (this.currentDocumentBase != null) {
            this.history.push(this.currentDocumentBase);
            if (this.history.size() == 1 && container.getComponentForm() != null) {
                container.getComponentForm().addCommand(Commands.cmdBack);
            }

        }
        schedule(url, postData);
    }

    /**
     * Schedules the given URL for loading.
     * @param url the URL that should be loaded
     */
    public void go(String url) {
System.out.println("XHtmlBrowser.go(" + url + ")");
        if (this.currentDocumentBase != null) {
            this.history.push(this.currentDocumentBase);
            if (this.history.size() == 1 && container.getComponentForm() != null) {
                container.getComponentForm().addCommand(Commands.cmdBack);
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
System.out.println("XHtmlBrowser.schedule(" + url + ", " + postData + ")");
        synchronized (this.loadingThread) {
            this.loadingThread.notify();
        }
    }

    /**
     * Overides J2ME Polish http sending
     * @param url
     * @param postData
     */
    protected void goImpl(String url, String postData) {
System.out.println("XHtmlBrowser.goImpl(" + url + ", " + postData + ")");
        R.getHtmlScreen().quit();
        R.getPostData().setRaw(postData, true);
        url = Utils.replaceString(url, "&amp;", "&");
        R.getURL().call(url);
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
                        try {
                            goImpl(url, postData);
                        } catch (OutOfMemoryError e) {
                            System.gc();
                        }
                    }

                    this.isWorking = false;
                    container.repaint();
                }
            } catch (Exception e) {
                Logger.error("XHtmlBrowser.run() ex: " + e.toString());
            }

            if (this.isCancelRequested == true) {
                this.isWorking = false;
                container.repaint();

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
        }
    }
}
