/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.locify.client.net.browser;

import com.sun.lwuit.Button;
import com.sun.lwuit.Font;
import com.sun.lwuit.Image;

/**
 *
 * @author menion
 */
public class HtmlButton extends Button {

    protected String url;
    
    public HtmlButton() {
        super();
    }

    public HtmlButton(String button) {
        super(button);
    }

    public HtmlButton(Image icon) {
        super(icon);
    }

    public void setStyle(HtmlStyle style) {
        this.getStyle().setFont(Font.createSystemFont(style.fontFace, style.fontStyle, style.fontSize));
        this.getStyle().setFgColor(style.fontColor);
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
