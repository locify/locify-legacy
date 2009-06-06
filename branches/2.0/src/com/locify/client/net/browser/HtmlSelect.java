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
package com.locify.client.net.browser;

import com.locify.client.net.Variables;
import com.locify.client.utils.Logger;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.RadioButton;
import com.sun.lwuit.layouts.BoxLayout;
import java.util.Vector;

public class HtmlSelect {

    public static final int SELECT_STYLE_COMBOBOX = 0;
    public static final int SELECT_STYLE_RADIOBUTTON = 1;
    
    private final static HtmlStyle NO_STYLE = new HtmlStyle();
    public static final String SELECT = "select";
    private final String name;
    private int size;
    private String label;
    private boolean isMultiple;
    private int selectedIndex;
    private String selectedValue;
    private final Vector optionNames;
    private final Vector optionValues;
    private final Vector optionStyles;
    private HtmlStyle style;

    public HtmlSelect(String name, String label, int size, boolean isMultiple, HtmlStyle style, String selectedValue) {
        if (name == null) {
            name = "";
        }
        this.name = name;
        this.size = size;
        this.label = label;
        this.isMultiple = isMultiple;

        this.style = style;
        this.selectedValue = Variables.replace(selectedValue, false);

        this.selectedIndex = -1;
        this.optionNames = new Vector();
        this.optionValues = new Vector();
        this.optionStyles = new Vector();
    }

    public String getName() {
        return this.name;
    }

    public String getValue(int index) {
        return (String) this.optionValues.elementAt(index);
    }

    public void addOption(String name) {
        addOption(name, name, false, null);
    }

    public void addOption(String name, String value, boolean selected, HtmlStyle optionStyle) {
        if (selected) {
            this.selectedIndex = this.optionNames.size();
        }
        this.optionNames.addElement(name);
        this.optionValues.addElement(value);
        if (optionStyle != null) {
            this.optionStyles.addElement(optionStyle);
        } else {
            this.optionStyles.addElement(NO_STYLE);
        }
    }

    public Container getSelectComponent() {
        try {
            Container container = new Container();
            container.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
            container.addComponent(new Label(label));

            if (selectedIndex < 0)
                selectedIndex = 0;
            
            ComboBox cb = null;
            ButtonGroup bg = null;
//System.out.println("OptionNames: " + optionNames.toString());
//System.out.println("SelectedIndex: " + selectedIndex);
            if (this.isMultiple) {
                cb = new ComboBox(optionNames);
                cb.setSelectedIndex(selectedIndex);
            } else {
                bg = new ButtonGroup();
                for (int i = 0; i < optionNames.size(); i++) {
                    bg.add(new RadioButton((String) optionNames.elementAt(i)));
                }
                bg.setSelected(selectedIndex);
            }

            //locify specific preselected select
            if (selectedValue != null) {
                for (int i = 0; i < optionValues.size(); i++) {
                    String optionName = (String) optionValues.elementAt(i);
                    if (optionName.equals(selectedValue)) {
                        this.selectedIndex = i;
                    }
                }
            }

            if (this.selectedIndex != -1) {
                if (this.isMultiple)
                    cb.setSelectedIndex(selectedIndex);
                else
                    bg.setSelected(selectedIndex);
            }
            //bgChoice.setAttribute("name", this.name);
            //bgChoice.setAttribute(SELECT, this);

            if (this.isMultiple) {
                container.addComponent(cb);
            } else {
                for (int i = 0; i < bg.getButtonCount(); i++) {
                    container.addComponent(bg.getRadioButton(i));
                }
            }

            return container;
        } catch (Exception e) {
            Logger.error("HtmlSelect.getSelectComponent() ex: " + e.toString());
            return null;
        }
    }
}
