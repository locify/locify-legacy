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

    public HtmlTextArea(String text, boolean editable) {
        super(2, 128);
        getStyle().setBorder(Border.createEmpty(), false);
        setGrowByContent(true);
        setEditable(editable);
        setFocusable(false);

        setText(text);
    }


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
}
