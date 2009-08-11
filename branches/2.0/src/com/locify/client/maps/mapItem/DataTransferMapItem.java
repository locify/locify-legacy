/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.locify.client.maps.mapItem;

import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Graphics;
import java.util.Vector;

/**
 *
 * @author menion
 */
public class DataTransferMapItem extends MapItem {

    private int width = 80;
    private int height = 35;

    private int stringHeight;
    private String valueSession;
    private int valueSessionWidth;
    private String valueTotal;
    private int valueTotalWidth;
    
    public DataTransferMapItem() {
        super();
        valueSession = "";
        valueTotal = "";
    }

    public void initialize() {
        return;
    }

    public void panItem(int moveX, int moveY) {
        return;
    }

    public void drawItem(Graphics g) {
        if (enabled) {
            g.setFont(ColorsFonts.FONT_SMALL);
            stringHeight = g.getFont().getHeight();
            valueSession = "S: " + Utils.formatDataSize(R.getMapTileCache().getDownloadedDataSession());
            valueSessionWidth = g.getFont().stringWidth(valueSession);
            valueTotal = "T: " + Utils.formatDataSize(R.getMapTileCache().getDownloadedDataTotal());
            valueTotalWidth = g.getFont().stringWidth(valueTotal);

            g.setColor(ColorsFonts.LIGHT_GRAY);
            g.fillRect(g.getClipX() + g.getClipWidth() - width, g.getClipY(), width, height);
            g.setColor(ColorsFonts.BLACK);
            g.drawRect(g.getClipX() + g.getClipWidth() - width, g.getClipY(), width, height);
            g.drawString(valueSession, g.getClipX() + g.getClipWidth() - width + (width - valueSessionWidth) / 2,
                    g.getClipY() + (height - 2 * stringHeight - 5) / 2);
            g.drawString(valueTotal, g.getClipX() + g.getClipWidth() - width + (width - valueTotalWidth) / 2,
                    g.getClipY() + (height - 2 * stringHeight - 5) / 2 + stringHeight + 5);
        }
    }

    public void getWaypointsAtPositionByPoint(Vector data, int x, int y, int radiusSquare) {
        return;
    }

    public void getWaypointsAtPositionByIcon(Vector data, int x, int y) {
        return;
    }

    public boolean touchInside(int x, int y) {
        return false;
    }

}
