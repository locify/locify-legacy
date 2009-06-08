package com.locify.client.net.browser;

import com.locify.client.net.Variables;
import com.locify.client.utils.Logger;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.ComboBox;
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
