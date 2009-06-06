/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.locify.client.net.browser;

import com.locify.client.utils.ColorsFonts;
import com.sun.lwuit.Font;

/**
 *
 * @author menion
 */
public class HtmlStyle {

    protected int fontSize;
    protected int fontFace;
    protected int fontStyle;
    protected int fontColor;

    public HtmlStyle() {
        fontSize = Font.SIZE_MEDIUM;
        fontFace = Font.FACE_PROPORTIONAL;
        fontStyle = Font.STYLE_PLAIN;
        fontColor = ColorsFonts.BLACK;
    }
}
