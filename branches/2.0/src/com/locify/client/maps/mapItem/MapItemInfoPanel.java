/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.locify.client.maps.mapItem;

import com.sun.lwuit.Button;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.GridLayout;

/**
 *
 * @author menion
 */
public class MapItemInfoPanel extends Container {

    private Button btnNext;
    private Button btnPrev;

    private Button btnClose;
    private Button btnNavigate;
    private Button btnMapNavig;

    private Label lbInfo;

    public MapItemInfoPanel() {
        super(new BorderLayout());

        btnPrev = new Button("P");
        addComponent(BorderLayout.WEST, btnPrev);

        btnNext = new Button("N");
        addComponent(BorderLayout.EAST, btnNext);

        Container contInfo = new Container(new BorderLayout());
        lbInfo = new Label("Label");
        contInfo.addComponent(BorderLayout.CENTER, lbInfo);

        Container contBtn = new Container(new GridLayout(3, 1));
        btnClose = new Button("C");
        contBtn.addComponent(btnClose);
        btnNavigate = new Button("N1");
        contBtn.addComponent(btnNavigate);
        btnMapNavig = new Button("N2");
        contBtn.addComponent(btnMapNavig);
        contInfo.addComponent(BorderLayout.EAST, contBtn);

        addComponent(BorderLayout.CENTER, contInfo);
    }
}
