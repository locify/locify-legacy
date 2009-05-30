/*
 * FormLocify.java
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
package com.locify.client.gui.extension;

import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.utils.R;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.List;
import com.sun.lwuit.animations.Animation;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.DefaultListCellRenderer;
import java.util.Vector;

/**
 *
 * @author menion
 */
public class FormLocify extends Form {

    private Dialog menuDialog;
    private ParentCommand selection;
    
    public FormLocify() {
        this(null);
    }

    public FormLocify(String title) {
        super(title);

        // set tile bar
        getTitleComponent().getStyle().setBgTransparency(200);
        getTitleComponent().getStyle().setBgPainter(R.getTopBar());
        getTitleComponent().setAlignment(Component.LEFT);

        // add subMenu support
        setMenuTransitions(null, null);
        setMenuCellRenderer(new DefaultListCellRenderer() {

            private boolean isSubMenu;

            public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
                isSubMenu = value != null && value instanceof ParentCommand;
                return super.getListCellRendererComponent(list, value, index, isSelected);
            }

            public void paintBackground(Graphics g) {
                super.paintBackground(g);
//                super.paintBackgrounds(g);
                if (isSubMenu) {
                    int oldColor = g.getColor();
                    if (hasFocus()) {
                        g.setColor(getStyle().getFgSelectionColor());
                    } else {
                        g.setColor(getStyle().getFgColor());
                    }
                    int leftPoint = getX() + getWidth() - 10;
                    int rightPoint = getX() + getWidth() - 2;
                    int centerPoint = getY() + (getHeight() / 2);
                    int topPoint = centerPoint - 4;
                    int bottomPoint = centerPoint + 4;
                    g.drawLine(leftPoint, topPoint, rightPoint, centerPoint);
                    g.drawLine(leftPoint, bottomPoint, rightPoint, centerPoint);
                    g.drawLine(leftPoint, topPoint, leftPoint, bottomPoint);
                    g.setColor(oldColor);
                }
            }
        });
    }

    public void setAsNew(String title) {
        removeAll();
        removeAllCommands();
        setTitle(title);
    }

    protected List createCommandList(Vector commands) {
        List commandList = super.createCommandList(commands);
        SelectionMonitor s = new SelectionMonitor(commandList);
        commandList.addSelectionListener(s);
        commandList.addActionListener(s);
        return commandList;
    }

    protected Command showMenuDialog(Dialog menu) {
        menuDialog = menu;
        Command c = super.showMenuDialog(menu);
        menuDialog = null;
        return c;
    }

    class SelectionMonitor implements SelectionListener, Animation, ActionListener {

        private List commandList;
        private long selectTime;
        private static final int SUBMENU_POPUP_DELAY = 1000;

        public SelectionMonitor(List commandList) {
            this.commandList = commandList;
        }

        public boolean animate() {
            // we use the animate method as a timer, gauging the time that passed since a selection was made
            if (selection == null) {
                menuDialog.deregisterAnimated(this);
            } else {
                if (System.currentTimeMillis() - selectTime >= SUBMENU_POPUP_DELAY) {
                    menuDialog.deregisterAnimated(this);
                    showSubmenu();
                }
            }

            return false;
        }

        public void selectionChanged(int oldSelected, int newSelected) {
            Object o = commandList.getSelectedItem();
            if (o != null && o instanceof ParentCommand) {
                // cause the animation of the parent form to be invoked
                menuDialog.registerAnimated(this);
                selection = (ParentCommand) o;
                selectTime = System.currentTimeMillis();
            } else {
                selection = null;
            }
        }

        public void paint(Graphics g) {
        }

        public void actionPerformed(ActionEvent evt) {
            menuDialog.deregisterAnimated(this);
            if (selection != null) {
                showSubmenu();
            }
        }

        private void showSubmenu() {
            final Dialog subMenu = new Dialog();
            subMenu.getStyle().setBgTransparency(150);
            final List content = new List(selection.getChildren());
//            content.getStyle().setBgTransparency(255);
            content.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    subMenu.dispose();
                    Command c = (Command) content.getSelectedItem();
                    ActionEvent e = new ActionEvent(c);
                    c.actionPerformed(e);
                    actionCommand(c);
                }
            });
            subMenu.setLayout(new BorderLayout());
            subMenu.addComponent(BorderLayout.CENTER, content);
            Command select = new Command("Select") {

                public void actionPerformed(ActionEvent evt) {
                    Command c = (Command) content.getSelectedItem();
                    ActionEvent e = new ActionEvent(c);
                    c.actionPerformed(e);
                    actionCommand(c);
                }
            };
            final Dialog oldMenuDialog = menuDialog;
            Command cancel = new Command("Cancel") {

                public void actionPerformed(ActionEvent evt) {
                    oldMenuDialog.show();
                }
            };
            subMenu.setDialogStyle(menuDialog.getDialogStyle());
            subMenu.addCommand(cancel);
            subMenu.addCommand(select);
            subMenu.show(getHeight() / 2 - 20, 20, getWidth() / 4, 20, true, true);
        }
    }
}
