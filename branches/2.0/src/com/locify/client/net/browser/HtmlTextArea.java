/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.locify.client.net.browser;

import com.sun.lwuit.TextArea;
import com.sun.lwuit.plaf.Border;

/**
 *
 * @author menion
 */
public class HtmlTextArea extends TextArea {

    public HtmlTextArea(String text) {
        this(text, false);
    }

    public HtmlTextArea(String text, boolean editable) {
        this(text, 1, 124, TextArea.ANY);
        setSingleLineTextArea(false);
        setEditable(editable);
        setFocusable(false);
    }

    public HtmlTextArea(String text, int rows, int columns, int constraint) {
        super(text, rows, columns, constraint);
        getStyle().setBorder(Border.createEmpty(), false);
        setGrowByContent(true);
        setEditable(true);
getStyle().setBorder(Border.createLineBorder(1));
        setText(text);
    }

//    public void initComponent() {
//System.out.println("Add1: " + getText() + " Rows: " + getLines() + " " + getRows() + " " + getParent().getLayout());
////System.out.println("Add2: " + getText() + " Columns: " + getColumns() + " " + getPreferredSize());
//        if (getLines() < getRows()) {
//            setRows(getLines());
////            if (getLines() == 1) {
////                setColumns(getText().length());
////                setWidth(getStyle().getFont().stringWidth(getText()));
////            }
//        }
////System.out.println("Add3: " + getText() + " Rows: " + getLines() + " " + getRows() + " " + getParent().getLayout());
////System.out.println("Add4: " + getText() + " Columns: " + getColumns() + " " + getPreferredSize());
//
//    }


    private HtmlForm form;
    private String type;
    private String name;
    private String value;

    public String getAttributeName() {
        return name;
    }

    public void setAttributeName(String name) {
        this.name = name;
    }

    public String getAttributeValue() {
        return value;
    }

    public void setAttributeValue(String value) {
        this.value = value;
    }

    public void setAttributeForm(HtmlForm form) {
        this.form = form;
    }

    public HtmlForm getAttributeForm() {
        return form;
    }

    public void setAttributeType(String type) {
        this.type = type;
    }

    public String getAttributeType() {
        return type;
    }

    public String toString() {
        return super.toString() + " text: " + getText();
    }
}
