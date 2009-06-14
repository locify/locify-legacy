/*
 * ColorsFonts.java
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
package com.locify.client.utils;

import com.sun.lwuit.Font;
import javax.microedition.lcdui.Image;

/**
 * Color constants definitions for canvas drawings (map and navigation screen)
 * @author Menion
 */
public class ColorsFonts {
    public static final int WHITE = 0xffffff;
    public static final int LIGHT_GRAY = 0xc0c0c0;
    public static final int GRAY = 0x808080;
    public static final int DARK_GRAY = 0x404040;
    public static final int BLACK = 0x000000;
    public static final int RED = 0xff0000;
    public static final int PINK = 0xffafaf;
    public static final int ORANGE = 0xff8000;
    public static final int LIGHT_ORANGE = 0xfeddb1;
    public static final int YELLOW = 0xffff00;
    public static final int GREEN = 0x00ff00;
    public static final int GREEN_SHINY = 0x78e239;
    public static final int MAGENTA = 0xff00ff;
    public static final int CYAN = 0x00ffff;
    public static final int BLUE = 0x0000ff;
    public static final int LIGHT_YELLOW = 0xFFFFDF;
    public static final int INDIGO = 0x00416a;
    
    public static final int MAP_BG_COLOR = LIGHT_ORANGE;
    public static final int MAP_GRID_COLOR = 0xccccff;
    public static final int MAP_COMPAS_COLOR = 0x999999;
    public static final int MAP_TEXT_COLOR = 0x000000;
    public static final int MAP_POINTER_COLOR = 0x000000;
    public static final int MAP_TRACK_COLOR = RED;
    public static final int MAP_TRACK_COLOR_SPACE = BLUE;
    public static final int MAP_WP_COLOR = BLUE;
    public static final int MAP_WP_COLOR_HIGHLIGHT = GREEN_SHINY;
    
    public static int MAP_ACTUAL_LOCATION = RED;

    // final version of fonts
//    public static final Font FONT_SMALL = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_SMALL = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_MEDIUM = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font FONT_LARGE = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE);

    public static final Font FONT_LINK = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_UNDERLINED | Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
//    public static final Font FONT_LARGE = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);


    /* bitMap fonts */
    public static final Font FONT_BMF_10 = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font FONT_BMF_14 = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font FONT_BMF_16 = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font FONT_BMF_18 = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font FONT_BMF_18W = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font FONT_BMF_20 = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
//    public static final Font FONT_BMF_10 = ResourcesLocify.getFont("arial_10");
//    public static final Font FONT_BMF_14 = ResourcesLocify.getFont("arial_14");
//    public static final Font FONT_BMF_16 = ResourcesLocify.getFont("arial_16");
//    public static final Font FONT_BMF_18 = ResourcesLocify.getFont("arial_18");
//    public static final Font FONT_BMF_18W = ResourcesLocify.getFont("arial_18_white_big_bold_letters_only");
//    public static final Font FONT_BMF_20 = ResourcesLocify.getFont("arial_20");
    
    public static final Font FONT_PLAIN_SMALL = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_PLAIN_MEDIUM = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font FONT_BOLD_MEDIUM = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font FONT_BOLD_LARGE = Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);

    public static Image getImageCopyWithTransparentBackground(Image img, int transparentColor) {
        int imgW = img.getWidth();
        int imgH = img.getHeight();
        int[] rGB = new int[imgW * imgH];
        img.getRGB(rGB, 0, imgW, 0, 0, imgW, imgH);

        for (int i = 0; i < rGB.length; i++) {
            if ((rGB[i] & 0x00ffffff) != transparentColor) {
                rGB[i] = rGB[i] | 0xff000000;
            } else {
                rGB[i] = 0x00ffffff & transparentColor;
            }
        }
        Image copy = (Image.createRGBImage(rGB, imgW, imgH, true));
        return copy;
    }
}
