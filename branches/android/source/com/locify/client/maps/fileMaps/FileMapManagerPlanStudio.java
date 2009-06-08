//#if planstudio
  /*
 * PlanStudioMap.java
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
package com.locify.client.maps.fileMaps;
//# 
import com.locify.client.utils.StorageTar;
import java.util.Vector;
import com.locify.client.utils.R;
import de.enough.polish.android.lcdui.Graphics;
//# 
/**
 *
 * @author menion
 */
public class FileMapManagerPlanStudio extends FileMapManager {
//# 
    private String packId;
    private int zoomMin;
    private int zoomMax;
    private PlanstudioMapObject[] maps;
    private int activeZoomId = 0;
    private static String prefix = "http://tomcat.planstudio.cz/omsmap/servlets/locify?ident=" + R.getPlanstudio().getPhoneIdentificationEncoded() + "&action=maptile";
//# 
    public FileMapManagerPlanStudio(String packId, String packName, int zoomMin, int zoomMax,
            CalibrationPoint cpTL, CalibrationPoint cpBR) {
        super(prefix);
//# 
        this.packId = packId;
        this.mapFilename = packName;
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
//# 
        this.maps = new PlanstudioMapObject[zoomMax - zoomMin + 1];
    }
//# 
    public FileMapManagerPlanStudio(int mapID, String mapTextID, String mapName, String mapCategoryID,
            String mapCategoryName, int zoomMin, int zoomMax) {
        super(mapTextID + ".mpx");
//# 
        this.packId = mapTextID;
        this.mapFilename = mapName;
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
//# 
        this.maps = new PlanstudioMapObject[zoomMax - zoomMin + 1];
    }
//# 
    public void addMap(int zoomId, String mapId, String mapName, FileMapConfig fmc, StorageTar storage) {
        //Logger.log("ADD: zoomId: " + zoomId + ", mapId: " + mapId + ", mapName: " + mapName + ", fmc: " + fmc.toString() + ", storage: " + storage.toString());
        maps[zoomId] = new PlanstudioMapObject(mapId, mapName, fmc, storage);
        if (zoomId == 0) {
            setZoomLevel(0);
        }
    }
//# 
    public boolean drawActualMap(Graphics gr, FileMapViewPort targetPort, Vector imageExist,
            Vector imageNotExist, int mapPanX, int mapPanY) {
        appendRequests(imageExist, imageNotExist, targetPort, maps[activeZoomId].storage, mapPanX, mapPanY);
        return true;
    }
//# 
    protected String createImageName(int i, int j) {
        if (stringBuffer.length() > 0) {
            stringBuffer.delete(0, stringBuffer.length());
        }
        stringBuffer.append("&map=" + packId);
        stringBuffer.append("&zoom=" + activeZoomId);
        stringBuffer.append("&tilex=" + i);
        stringBuffer.append("&tiley=" + j);
//# 
        return stringBuffer.toString();
    }
//# 
    public int getMinZoomLevel() {
        return zoomMin;
    }
//# 
    public int getMaxZoomLevel() {
        return zoomMax;
    }
//# 
    public void setZoomLevel(int zoom) {
        activeZoomId = zoom;
        PlanstudioMapObject pmo = maps[zoom];
        setFileMapConfig(pmo.fmc);
        setStorageTar(pmo.storage);
    }
//# 
    public String getMapName() {
        //Logger.log("ActiveZoomId: " + activeZoomId + " maps.size(): " + maps.length);
        PlanstudioMapObject pmo = maps[activeZoomId];
        return pmo.mapName;
    }
//# 
    private class PlanstudioMapObject {
//# 
        private String mapId;
        private String mapName;
        private FileMapConfig fmc;
        private StorageTar storage;
//# 
        public PlanstudioMapObject(String mapId, String mapName, FileMapConfig fmc, StorageTar storage) {
            this.mapId = mapId;
            this.mapName = mapName;
            this.fmc = fmc;
            this.storage = storage;
        }
    }
}
//#endif
