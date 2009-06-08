/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.locify.client.net.browser;

import com.locify.client.utils.ColorsFonts;
import com.sun.lwuit.Button;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.Border;

/**
 *
 * @author menion
 */
public class HtmlButton extends Button {

    protected String url;
    private boolean link = false;

    public HtmlButton() {
        super();
    }

    public HtmlButton(String button) {
        super(button);
    }

    public HtmlButton(Image icon) {
        super(icon);
    }

    public void setLinkBehaviour() {
        this.link = true;
        getStyle().setBorder(Border.createEmpty());
        getStyle().setFgColor(0x0000FF);
        getStyle().setBgTransparency(0);
        getStyle().setFont(ColorsFonts.FONT_LINK);
    }

    public boolean isLink() {
        return link;
    }

    public void paint(Graphics g) {
        if (link) {
            if (isFocus) {
                g.setColor(0x0000ff);
                g.fillRect(getX(), getY(), getWidth(), getHeight());

                g.setColor(0xFFFFFF);
                g.setFont(ColorsFonts.FONT_LINK);
                g.drawString(getText(), this.getX(), this.getY());
            } else {
                g.setColor(0x0000FF);
                g.setFont(ColorsFonts.FONT_LINK);
                g.drawString(getText(), this.getX(), this.getY());
            }
        } else {
            super.paint(g);
        }
    }
    
    private boolean isFocus = false;

    protected void focusGained() {
        if (link)
            isFocus = true;
        else
            super.focusGained();
    }

    protected void focusLost() {
        if (link)
            isFocus = false;
        else
            super.focusLost();
    }

    public Dimension getPreferredSize() {
        if (link)
            return new Dimension(ColorsFonts.FONT_LINK.stringWidth(getText()), ColorsFonts.FONT_LINK.getHeight());
        else
            return super.getPreferredSize();
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
