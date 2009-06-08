//#if planstudio
   /*
 * PlanStudioManager.java
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
package com.locify.client.maps.planStudio;
//# 
import com.locify.client.data.FileSystem;
import com.locify.client.locator.Location4D;
import com.locify.client.net.Http;
import com.locify.client.maps.fileMaps.CalibrationPoint;
import com.locify.client.maps.fileMaps.FileMapConfig;
import com.locify.client.maps.fileMaps.FileMapManagerPlanStudio;
import com.locify.client.maps.projection.Projection;
import com.locify.client.maps.projection.UTMProjection;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Sha1;
import com.locify.client.utils.StorageTar;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.TempRecord;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.List;
import de.enough.polish.util.base64.Base64;
import de.enough.polish.xml.XmlPullParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Vector;
import de.enough.polish.android.io.Connector;
import de.enough.polish.android.io.file.FileConnection;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
//# 
/**
 *
 * @author menion
 */
public class PlanStudioManager {
//# 
    /**    private static String data =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
    "<mapList>" +
    "<map id=\"evropa\" mapName=\"Evropa\" map_br_x=\"3677925.00\" map_br_y=\"3809082.00\" map_tl_x=\"-2647075.00\" map_tl_y=\"8474082.00\" maxZoom=\"3\" minZoom=\"0\">" +
    "   <zoom id=\"0\" mppx=\"5000.0\" name=\"1:25000000\">" +
    "       <zoomJsInfo DX=\"1250000.0\" DY=\"1250000.0\" " +
    "           bottomRightX=\"4265425.0\" bottomRightY=\"8641582.0\" " +
    "           countX=\"6\" countY=\"4\" fileType=\"png\" iconSize=\"16\" " +
    "           metersPerPixel=\"5000.0\" ratio=\"25000000\" " +
    "           tileH=\"250\" tileW=\"250\" topLeftX=\"-3234575.0\" topLeftY=\"3641582.0\"/>" +
    "   </zoom>" +
    "" +
    "   <zoom id=\"1\" mppx=\"2000.0\" name=\"1:10000000\">" +
    "       <zoomJsInfo DX=\"500000.0\" DY=\"500000.0\" " +
    "           bottomRightX=\"3762425.0\" bottomRightY=\"8642582.0\" " +
    "           countX=\"13\" countY=\"10\" fileType=\"png\" iconSize=\"16\" " +
    "           metersPerPixel=\"2000.0\" ratio=\"10000000\" " +
    "           tileH=\"250\" tileW=\"250\" topLeftX=\"-2737575.0\" topLeftY=\"3642582.0\"/>" +
    "   </zoom>" +
    "   <zoom id=\"2\" mppx=\"1000.0\" name=\"1:5000000\">" +
    "       <zoomJsInfo DX=\"250000.0\" DY=\"250000.0\" bottomRightX=\"3762425.0\" bottomRightY=\"8517582.0\" countX=\"26\" countY=\"19\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"1000.0\" ratio=\"5000000\" tileH=\"250\" tileW=\"250\" topLeftX=\"-2737575.0\" topLeftY=\"3767582.0\"/>" +
    "   </zoom>" +
    "   <zoom id=\"3\" mppx=\"500.0\" name=\"1:2500000\">" +
    "       <zoomJsInfo DX=\"125000.0\" DY=\"125000.0\" bottomRightX=\"3699425.0\" bottomRightY=\"8518082.0\" countX=\"51\" countY=\"38\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"500.0\" ratio=\"2500000\" tileH=\"250\" tileW=\"250\" topLeftX=\"-2675575.0\" topLeftY=\"3768082.0\"/>" +
    "   </zoom>" +
    "</map>" +
    "<map id=\"evropa-auto\" mapName=\"Automapa Evropa\" map_br_x=\"1661650.93\" map_br_y=\"4581867.69\" map_tl_x=\"-211599.07\" map_tl_y=\"6135367.69\" maxZoom=\"1\" minZoom=\"0\" parentId=\"evropa\">" +
    "<zoom id=\"0\" mppx=\"250.0\" name=\"1:1500000\"><zoomJsInfo DX=\"62500.0\" DY=\"62500.0\" bottomRightX=\"1662526.0\" bottomRightY=\"6139868.0\" countX=\"30\" countY=\"25\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"250.0\" ratio=\"1500000\" tileH=\"250\" tileW=\"250\" topLeftX=\"-212474.0\" topLeftY=\"4577368.0\"/></zoom>" +
    "<zoom id=\"1\" mppx=\"100.0\" name=\"1:500000\"><zoomJsInfo DX=\"25000.0\" DY=\"25000.0\" bottomRightX=\"1662476.0\" bottomRightY=\"6146193.0\" countX=\"75\" countY=\"63\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"100.0\" ratio=\"500000\" tileH=\"250\" tileW=\"250\" topLeftX=\"-212524.0\" topLeftY=\"4571193.0\"/></zoom>" +
    "</map>" +
    "" +
    "<map id=\"cesko\" mapName=\"Česko automapa iDNES\" map_br_x=\"781941.93\" map_br_y=\"5376837.38\" map_tl_x=\"292341.93\" map_tl_y=\"5656437.38\" maxZoom=\"5\" minZoom=\"0\" parentId=\"evropa-auto\">" +
    "     <zoom id=\"0\" mppx=\"600.0\" name=\"1:3000000\"><zoomJsInfo DX=\"150000.0\" DY=\"150000.0\" bottomRightX=\"837142.0\" bottomRightY=\"5666637.0\" countX=\"4\" countY=\"2\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"600.0\" ratio=\"3000000\" tileH=\"250\" tileW=\"250\" topLeftX=\"237142.0\" topLeftY=\"5366637.0\"/></zoom>" +
    "     <zoom id=\"1\" mppx=\"246.0\" name=\"1:1000000\"><zoomJsInfo DX=\"61500.0\" DY=\"61500.0\" bottomRightX=\"784269.0\" bottomRightY=\"5670138.0\" countX=\"8\" countY=\"5\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"246.0\" ratio=\"1000000\" tileH=\"250\" tileW=\"250\" topLeftX=\"292269.0\" topLeftY=\"5362638.0\"/></zoom>" +
    "     <zoom id=\"2\" mppx=\"100.0\" name=\"1:500000\"><zoomJsInfo DX=\"25000.0\" DY=\"25000.0\" bottomRightX=\"788196.0\" bottomRightY=\"5666429.0\" countX=\"20\" countY=\"12\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"100.0\" ratio=\"500000\" tileH=\"250\" tileW=\"250\" topLeftX=\"288196.0\" topLeftY=\"5366429.0\"/></zoom>" +
    "     <zoom id=\"3\" mppx=\"50.0\" name=\"1:250000\"><zoomJsInfo DX=\"12500.0\" DY=\"12500.0\" bottomRightX=\"792000.0\" bottomRightY=\"5665614.0\" countX=\"41\" countY=\"24\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"50.0\" ratio=\"250000\" tileH=\"250\" tileW=\"250\" topLeftX=\"279500.0\" topLeftY=\"5365614.0\"/></zoom>" +
    "     <zoom id=\"4\" mppx=\"25.0\" name=\"1:150000\"><zoomJsInfo DX=\"6250.0\" DY=\"6250.0\" bottomRightX=\"791938.0\" bottomRightY=\"5662520.0\" countX=\"82\" countY=\"47\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"25.0\" ratio=\"150000\" tileH=\"250\" tileW=\"250\" topLeftX=\"279438.0\" topLeftY=\"5368770.0\"/></zoom>" +
    "     <zoom id=\"5\" mppx=\"15.0\" name=\"1:100000\"><zoomJsInfo DX=\"3750.0\" DY=\"3750.0\" bottomRightX=\"790680.0\" bottomRightY=\"5661897.0\" countX=\"136\" countY=\"78\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"15.0\" ratio=\"100000\" tileH=\"250\" tileW=\"250\" topLeftX=\"280680.0\" topLeftY=\"5369397.0\"/></zoom>" +
    "</map>" +
    "" +
    "<map id=\"praha\" mapName=\"Praha\" map_br_x=\"481882.00\" map_br_y=\"5526636.00\" map_tl_x=\"442422.00\" map_tl_y=\"5558548.00\" maxZoom=\"2\" minZoom=\"0\" parentId=\"cesko\">" +
    "   <zoom id=\"0\" mppx=\"4.0\" name=\"1:20000\">" +
    "       <zoomJsInfo DX=\"1000.0\" DY=\"1000.0\"" +
    "           bottomRightX=\"482152.0\" bottomRightY=\"5558592.0\"" +
    "           countX=\"40\" countY=\"32\" fileType=\"png\" iconSize=\"16\"" +
    "           metersPerPixel=\"4.0\" ratio=\"20000\" tileH=\"250\" tileW=\"250\"" +
    "           topLeftX=\"442152.0\" topLeftY=\"5526592.0\"/>" +
    "   </zoom>" +
    "   <zoom id=\"1\" mppx=\"2.0\" name=\"1:10000\">" +
    "       <zoomJsInfo DX=\"500.0\" DY=\"500.0\" bottomRightX=\"481901.0\" bottomRightY=\"5558594.0\" countX=\"79\" countY=\"64\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"2.0\" ratio=\"10000\" tileH=\"250\" tileW=\"250\" topLeftX=\"442401.0\" topLeftY=\"5526594.0\"/>" +
    "   </zoom>" +
    "   <zoom id=\"2\" mppx=\"1.0\" name=\"1:5000\">" +
    "       <zoomJsInfo DX=\"250.0\" DY=\"250.0\" bottomRightX=\"481901.0\" bottomRightY=\"5558595.0\" countX=\"158\" countY=\"128\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"1.0\" ratio=\"5000\" tileH=\"250\" tileW=\"250\" topLeftX=\"442401.0\" topLeftY=\"5526595.0\"/>" +
    "   </zoom>" +
    "</map>" +
    "" +
    "<map id=\"olomouc\" mapName=\"Olomouc\" map_br_x=\"672738.00\" map_br_y=\"5490344.00\" map_tl_x=\"656162.00\" map_tl_y=\"5502988.00\" maxZoom=\"2\" minZoom=\"0\" parentId=\"cesko\">" +
    "   <zoom id=\"0\" mppx=\"4.0\" name=\"1:20000\"><zoomJsInfo DX=\"1000.0\" DY=\"1000.0\" bottomRightX=\"672950.0\" bottomRightY=\"5503166.0\" countX=\"17\" countY=\"13\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"4.0\" ratio=\"20000\" tileH=\"250\" tileW=\"250\" topLeftX=\"655950.0\" topLeftY=\"5490166.0\"/></zoom>" +
    "   <zoom id=\"1\" mppx=\"2.0\" name=\"1:10000\"><zoomJsInfo DX=\"500.0\" DY=\"500.0\" bottomRightX=\"672949.0\" bottomRightY=\"5503167.0\" countX=\"34\" countY=\"26\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"2.0\" ratio=\"10000\" tileH=\"250\" tileW=\"250\" topLeftX=\"655949.0\" topLeftY=\"5490167.0\"/></zoom>" +
    "   <zoom id=\"2\" mppx=\"1.0\" name=\"1:5000\"><zoomJsInfo DX=\"250.0\" DY=\"250.0\" bottomRightX=\"672823.0\" bottomRightY=\"5503042.0\" countX=\"67\" countY=\"51\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"1.0\" ratio=\"5000\" tileH=\"250\" tileW=\"250\" topLeftX=\"656073.0\" topLeftY=\"5490292.0\"/></zoom>" +
    "</map>" +
    "<map id=\"ceske-budejovice\" mapName=\"České Budějovice\" map_br_x=\"467272.00\" map_br_y=\"5420406.00\" map_tl_x=\"457992.00\" map_tl_y=\"5429318.00\" maxZoom=\"2\" minZoom=\"0\" parentId=\"cesko\">" +
    "   <zoom id=\"0\" mppx=\"4.0\" name=\"1:20000\"><zoomJsInfo DX=\"1000.0\" DY=\"1000.0\" bottomRightX=\"467632.0\" bottomRightY=\"5429362.0\" countX=\"10\" countY=\"9\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"4.0\" ratio=\"20000\" tileH=\"250\" tileW=\"250\" topLeftX=\"457632.0\" topLeftY=\"5420362.0\"/></zoom>" +
    "   <zoom id=\"1\" mppx=\"2.0\" name=\"1:10000\"><zoomJsInfo DX=\"500.0\" DY=\"500.0\" bottomRightX=\"467381.0\" bottomRightY=\"5429364.0\" countX=\"19\" countY=\"18\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"2.0\" ratio=\"10000\" tileH=\"250\" tileW=\"250\" topLeftX=\"457881.0\" topLeftY=\"5420364.0\"/></zoom>" +
    "   <zoom id=\"2\" mppx=\"1.0\" name=\"1:5000\"><zoomJsInfo DX=\"250.0\" DY=\"250.0\" bottomRightX=\"467381.0\" bottomRightY=\"5429365.0\" countX=\"38\" countY=\"36\" fileType=\"png\" iconSize=\"16\" metersPerPixel=\"1.0\" ratio=\"5000\" tileH=\"250\" tileW=\"250\" topLeftX=\"457881.0\" topLeftY=\"5420365.0\"/></zoom>" +
    "</map>" +
    "</mapList>";*/
    private Vector fileMapManagers;
    private static String ident = null;
    private static String identEncoded = null;
    private static String hashedFileName;
//# 
    public PlanStudioManager() {
        fileMapManagers = new Vector();
//# 
    /*  FileMapManager.obtainedData = "";
    /  R.getHttp().start("http://tomcat.planstudio.cz/omsmap/servlets/locify?emai=lerc145re&action=mapdef",Http.MAPS);
    long actualTime = System.currentTimeMillis();
    while (FileMapManager.obtainedData.equals("") && (System.currentTimeMillis() - actualTime) < 10000) {
    try {
    Thread.sleep(250);
    } catch (InterruptedException ex) {
    ex.printStackTrace();
    }
    }
//# 
    if (!FileMapManager.obtainedData.equals("")) {
    parseOnlineMapDefinitions(FileMapManager.obtainedData);
    }*/
//# 
    //parseMapDefinitions(data);
    //fileMapManagers.addElement(parseOfflineMap());
    }
//# 
    public void view() {
        try {
            if (fileMapManagers.size() == 0) {
                String url = "http://tomcat.planstudio.cz/omsmap/servlets/locify?ident=" + getPhoneIdentificationEncoded() + "&action=mapdef";
                hashedFileName = FileSystem.CACHE_MAP_FOLDER + FileSystem.hashFileName(url);
                if (R.getFileSystem().exists(hashedFileName)) {
                    String data = R.getFileSystem().loadString(hashedFileName);
                    parseOnlineMapDefinitionFile(data, false);
                } else {
                    R.getHttp().start(url, Http.MAPS);
                    final Form form = new Form("PlanStudio Maps");
                    form.append("Loading...");
                    form.addCommand(Commands.cmdBack);
                    form.setCommandListener(new CommandListener() {
//# 
                        public void commandAction(Command c, Displayable d) {
                            if (c.equals(Commands.cmdBack)) {
                                R.getMapScreen().view();
                            }
                        }
                    });
//                    fileMapManagers.addElement(parseOfflineMap());
                    R.getMidlet().switchDisplayable(null, form);
                }
            }
//#             
            if (fileMapManagers.size() != 0) {
//Logger.debug("fileMapManagers.size() " + fileMapManagers.size());
                final List list = new List("PlanStudio Maps", List.IMPLICIT);
                for (int i = 0; i < fileMapManagers.size(); i++) {
                    FileMapManagerPlanStudio fmmPS = (FileMapManagerPlanStudio) fileMapManagers.elementAt(i);
//Logger.debug("fileMapManagers.getMapName() " + fmmPS.getMapName());
                    list.append(fmmPS.getMapName(), null);
                }
                list.addCommand(Commands.cmdBack);
                list.setCommandListener(new CommandListener() {
//# 
                    public void commandAction(Command c, Displayable d) {
                        if (c.equals(List.SELECT_COMMAND)) {
                            FileMapManagerPlanStudio fmmPS = (FileMapManagerPlanStudio) fileMapManagers.elementAt(list.getSelectedIndex());
                            R.getMapScreen().setFileMap(fmmPS, R.getLocator().getLastLocation());
                        } else if (c.equals(Commands.cmdBack)) {
                            R.getMapScreen().view();
                        }
                    }
                });
//# 
                R.getMidlet().switchDisplayable(null, list);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "PlanStudioManager.view()", null);
        }
    }
//# 
    public void parseOnlineMapDefinitionFile(String data, boolean cacheFile) {
        ByteArrayInputStream stream = null;
        InputStreamReader reader = null;
        XmlPullParser parser;
//# 
        try {
            stream = new ByteArrayInputStream(UTF8.encode(data));
            reader = new InputStreamReader(stream, "utf-8");
            parser = new XmlPullParser(reader);
//# 
            int event;
            String tagName;
//# 
            // tag MAP
            String mapId = "";
            String mapName = "";
            CalibrationPoint cpTL = null;
            CalibrationPoint cpBR = null;
            int zoomMin;
            int zoomMax;
//# 
            // tag ZOOM
            String zoomId = "";
            String zoomName = "";
            int mppx;
//# 
            // tag ZOOM_JS_INFO
            int tileSizeX = 0;
            int tileSizeY = 0;
            int xmax = 0;
            int ymax = 0;
//# 
            FileMapManagerPlanStudio fmmPS = null;
//# 
            while (true) {
                event = parser.next();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    //System.out.println(event + " start: " + tagName);
                    if (tagName.equals("map")) {
                        mapId = parser.getAttributeValue("id");
                        mapName = parser.getAttributeValue("mapName");
                        //System.out.println(mapName);
                        cpTL = new CalibrationPoint();
                        cpTL.position = new Location4D(
                                GpsUtils.parseDouble(parser.getAttributeValue("map_tl_y")),
                                GpsUtils.parseDouble(parser.getAttributeValue("map_tl_x")),
                                0.0f);
                        cpBR = new CalibrationPoint();
                        cpBR.position = new Location4D(
                                GpsUtils.parseDouble(parser.getAttributeValue("map_br_y")),
                                GpsUtils.parseDouble(parser.getAttributeValue("map_br_x")),
                                0.0f);
                        zoomMin = GpsUtils.parseInt(parser.getAttributeValue("minZoom"));
                        zoomMax = GpsUtils.parseInt(parser.getAttributeValue("maxZoom"));
//# 
                        fmmPS = new FileMapManagerPlanStudio(mapId, mapName, zoomMin, zoomMax, cpTL, cpBR);
                    } else if (tagName.equals("zoom")) {
                        zoomId = parser.getAttributeValue("id");
                        zoomName = parser.getAttributeValue("name");
                        mppx = GpsUtils.parseInt(parser.getAttributeValue("mppx"));
                    } else if (tagName.equals("zoomJsInfo")) {
                        tileSizeX = GpsUtils.parseInt(parser.getAttributeValue("tileW"));
                        tileSizeY = GpsUtils.parseInt(parser.getAttributeValue("tileH"));
                        xmax = tileSizeX * GpsUtils.parseInt(parser.getAttributeValue("countX"));
                        ymax = tileSizeY * GpsUtils.parseInt(parser.getAttributeValue("countY"));
                        //System.out.println(zoomName + " " + xmax + " " + ymax);
                        cpBR = new CalibrationPoint();
                        cpBR.position = new Location4D(
                                GpsUtils.parseDouble(parser.getAttributeValue("topLeftY")),
                                GpsUtils.parseDouble(parser.getAttributeValue("bottomRightX")),
                                0.0f);
                        cpBR.x = xmax;
                        cpBR.y = ymax;
//# 
                        cpTL = new CalibrationPoint();
                        cpTL.position = new Location4D(
                                GpsUtils.parseDouble(parser.getAttributeValue("bottomRightY")),
                                GpsUtils.parseDouble(parser.getAttributeValue("topLeftX")),
                                0.0f);
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
                    //System.out.println(event + " end: " + tagName);
                    if (tagName.equals("map")) {
//Logger.log("Add map: " + fmmPS.getMapName());
                        fileMapManagers.addElement(fmmPS);
                    } else if (tagName.equals("zoom")) {
                        String MI = mapId + "_" + zoomId;
                        String MN = mapName + "(" + zoomName + ")";
                        //System.out.println("Add: " + MN + " " + xmax + " " + ymax);
                        FileMapConfig fmc = FileMapConfig.createConfigFromValues(
                                MN,
                                Projection.PROJECTION_UTM,
                                cpTL, cpBR,
                                xmax, ymax,
                                tileSizeX, tileSizeY,
                                GpsUtils.parseInt(zoomId));
                        ((UTMProjection) fmc.getMapProjection()).psFix = true;
//# 
                        fmmPS.addMap(GpsUtils.parseInt(zoomId), MI, MN, fmc, null);
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
//# 
            if (cacheFile) {
                R.getFileSystem().saveString(hashedFileName, data);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "PlanStudioManager.parseMapDefinitions()", data);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        view();
    }
//# 
    private static FileMapManagerPlanStudio parseOfflineMap() {
        FileConnection fc = null;
        InputStream is = null;
        try {
            FileMapManagerPlanStudio fmmPS = null;
            byte[] buffer = new byte[1024];
//# 
            fc = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + "boskovice.mpx", Connector.READ);
            is = fc.openInputStream();
//# 
            // read XMLP
            is.read(buffer, 0, 4);
            String startTag = new String(buffer, 0, 4);
//# 
            //Logger.log("" + is.read()); // vrati 176
            //is.read(buffer, 0, 1);
            //Logger.log("" + buffer[0]); // vrati -80
            //Logger.log("" + ((int) buffer[0])); // vrati -80
//# 
            is.read(buffer, 0, 4);
            int headerSize = Utils.readInt(buffer, 0, 4);
            buffer = new byte[headerSize];
            //Logger.log("HS: " + headerSize);
//# 
            is.read(buffer, 0, headerSize);
            encode(buffer, headerSize);
            //Logger.log("encoded: " + new String(buffer, 0, headerSize));
//# 
            int version = Utils.readInt(buffer, 0, 4);
            int mapID = Utils.readInt(buffer, 4, 4);
            String mapTextID = (new String(buffer, 8, 64)).trim();
            //Logger.log("MapTextID: " + mapTextID);
            String mapName = (readMWCHAR(buffer, 72, 128)).trim();
            //Logger.log("MapName: " + mapName);
            String mapCategoryID = (new String(buffer, 200, 64)).trim();
            //Logger.log("MapCategoryID: " + mapCategoryID);
            String mapCategoryName = (readMWCHAR(buffer, 264, 128)).trim();
            //Logger.log("MapCategoryName: " + mapCategoryName);
            double centerX = Utils.readDouble(buffer, 392, 8);
            double centerY = Utils.readDouble(buffer, 400, 8);
            //Logger.log("CenterX: " + centerX + " centerY: " + centerY);
            int dateGeneratedYear = Utils.readInt(buffer, 408, 2);
            int dateGeneratedMonth = Utils.readInt(buffer, 410, 1);
            int dateGeneratedDay = Utils.readInt(buffer, 411, 1);
            int dateCreatedYear = Utils.readInt(buffer, 412, 2);
            int dateCreatedMonth = Utils.readInt(buffer, 414, 1);
            int dateCreatedDay = Utils.readInt(buffer, 415, 1);
            //Logger.log("Created: " + dateCreatedYear + " " + dateCreatedMonth + " " + dateCreatedDay);
            //Logger.log("Generated: " + dateGeneratedYear + " " + dateGeneratedMonth + " " + dateGeneratedDay);
            int zoomCount = Utils.readInt(buffer, 416, 4);
            int zoomStructureSize = Utils.readInt(buffer, 420, 4);
            int miniMapPosition = Utils.readInt(buffer, 424, 4);
            int flags = Utils.readInt(buffer, 428, 4);
            //Logger.log("zoomCount: " + zoomCount + ", zoomStructureSize: " + zoomStructureSize + ", miniMapPosition: " + miniMapPosition + ", flags: " + flags);
//# 
            fmmPS = new FileMapManagerPlanStudio(mapID, mapTextID, mapName, mapCategoryID, mapCategoryName, 0, zoomCount - 1);
//# 
            // JPEG flag
            is.read();
//# 
            buffer = new byte[zoomStructureSize];
            for (int i = 0; i < zoomCount; i++) {
                is.read(buffer, 0, buffer.length);
                encode(buffer, buffer.length);
                //Logger.log("encoded: " + new String(buffer, 0, buffer.length));
//# 
                double mmpx = Utils.readDouble(buffer, 0, 8);
                int scale = Utils.readInt(buffer, 8, 4);
                int imageSizeX = Utils.readInt(buffer, 12, 4);
                int imageSizeY = Utils.readInt(buffer, 16, 4);
                int tileSizeX = Utils.readInt(buffer, 20, 4);
                int tileSizeY = Utils.readInt(buffer, 24, 4);
                int tileCountX = Utils.readInt(buffer, 28, 4);
                int tileCountY = Utils.readInt(buffer, 32, 4);
                double boundingRectX1 = Utils.readDouble(buffer, 40, 8);
                double boundingRectY1 = Utils.readDouble(buffer, 48, 8);
                double boundingRectX2 = Utils.readDouble(buffer, 56, 8);
                double boundingRectY2 = Utils.readDouble(buffer, 64, 8);
                int dataPosition = Utils.readInt(buffer, 72, 4);
                int dataSize = Utils.readInt(buffer, 76, 4);
//# 
                // palette not used in our format
                //                byte[] palette = new byte[256 * 4];
                //                for (int j = 0; j <  palette.length; j++) {
                //                     palette[j] = buffer[80 + j];
                //                }
//# 
                // right type of palette for use in own png files
                //                byte[] palette = new byte[256 * 3];
                //                int rgbIndex = 0;
                //                for (int j = 0; j <  palette.length; j += 3) {
                //                     palette[j] = buffer[80 + rgbIndex];
                //                     palette[j + 1] = buffer[80 + rgbIndex + 1];
                //                     palette[j + 2] = buffer[80 + rgbIndex + 2];
                //                     rgbIndex += 4;
                //                }
//# 
                // for unknown reason load one byte more
                is.read();
//# 
                // create map
                String MI = mapTextID + "_" + scale;
                String MN = mapName + "(1:" + scale + ")";
                CalibrationPoint cpTL = new CalibrationPoint();
                cpTL.position = new Location4D(boundingRectY1, boundingRectX1, 0.0f);
//# 
                CalibrationPoint cpBR = new CalibrationPoint();
                cpBR.x = imageSizeX;
                cpBR.y = imageSizeY;
                cpBR.position = new Location4D(boundingRectY2, boundingRectX2, 0.0f);
//# 
                StorageTar storage = new StorageTar();
                indexMpxFile(storage, "file:///" + FileSystem.ROOT + FileSystem.MAP_FOLDER + "boskovice.mpx",
                        //                        palette, dataPosition, dataSize, tileCountX * tileCountY);
                        dataPosition, dataSize, tileCountX, tileCountY);
//# 
                FileMapConfig fmc = FileMapConfig.createConfigFromValues(
                        MN, Projection.PROJECTION_UTM,
                        cpTL, cpBR,
                        imageSizeX, imageSizeY,
                        tileSizeX, tileSizeY,
                        i);
                ((UTMProjection) fmc.getMapProjection()).psFix = true;
                fmmPS.addMap(i, MI, MN, fmc, storage);
            //Logger.log("'" + mmpx + "' '" + scale + "' '" + imageSizeX + "' '" + imageSizeY + "' '" +
            //        tileSizeX + "' '" + tileSizeY + "' '" + tileCountX + "' '" + tileCountY + "'");
            //Logger.log("'" + boundingRectX1 + "' '" + boundingRectY1 + "' '" + boundingRectX2 + "' '" +
            //        boundingRectY2 + "' '" + dataPosition + "' '" + dataSize + "'");
//# 
            //                for (int j = 0; j < 2; j++) {
            //                    R.getFileSystem().saveBytes(j + ".png", storage.loadFile(storage.getTarRecord(j)));
            //                }
            //                System.out.println("New image: " + new String(storage.loadFile(storage.getTarRecord(0))));
            //                return null;
            }
//# 
            // reading border polygon
            is.read(buffer, 0, 4);
            int pointCount = Utils.readInt(buffer, 0, 4);
            //Logger.log("PC: " + pointCount);
            is.read(buffer, 0, pointCount * 2 * 4);
            for (int i = 0; i < pointCount; i++) {
                int X = Utils.readInt(buffer, 8 * i, 4);
                int Y = Utils.readInt(buffer, 8 * i + 4, 4);
            //Logger.log("X: " + X + " Y: " + Y);
            }
//# 
            is.read(buffer, 0, 4);
            int serialNumbers = Utils.readInt(buffer, 0, 4);
            //Logger.log("SN: " + serialNumbers);
            is.read(buffer, 0, serialNumbers * 4);
            encode(buffer, serialNumbers * 4);
            for (int i = 0; i < serialNumbers; i++) {
                int serialNumber = Utils.readInt(buffer, i * 4, 4);
            //Logger.log("SerN: " + serialNumber);
            }
//# 
            is.read(buffer, 0, 4);
            int userParametres = Utils.readInt(buffer, 0, 4);
            //Logger.log("UP: " + userParametres);
//# 
            //Logger.log("Stop");
            is.close();
            fc.close();
//# 
            return fmmPS;
        } catch (Exception ex) {
            Logger.error(ex.toString());
            return null;
        }
    }
//# 
    private static void indexMpxFile(StorageTar tar, String filePath, int dataStart, int dataSize, int tileCountX, int tileCountY) {
        try {
            //Logger.debug("IndexMpxFile: " + filePath + ", dataStart: " + dataStart + ", dataSize: " + dataSize + ", tileCount: " + tileCountX + ", " + tileCountY);
            FileConnection fc = (FileConnection) Connector.open(filePath, Connector.READ);
            InputStream is = fc.openInputStream();
//# 
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
//# 
            byte buff[] = new byte[256];
            StorageTar.skipBytes(is, dataStart);
            int actualPos = dataStart;
//# 
            // read header size
            is.read(buff, 0, 4);
            actualPos += 4;
            int headerSize = Utils.readInt(buff, 0, 4);
            //Logger.log("HeaderSize: " + headerSize);
//# 
            // read png header
            byte[] pngHeader = new byte[headerSize];
            is.read(pngHeader, 0, headerSize);
            //Logger.log("Header: " + new String(pngHeader));
            actualPos += headerSize;
            //Logger.log("ActualPos: " + actualPos);
//# 
            Vector images = new Vector();
            int pos, posX, posY;
            for (int i = 0; i < (tileCountX * tileCountY); i++) {
                is.read(buff, 0, 4);
                actualPos += 4;
                pos = Utils.readInt(buff, 0, 4);
                //Logger.log("Pos: " + pos + "  data: " + buff[0] + " " + buff[1] + " " + buff[2] + " " + buff[3]);
                images.addElement(new TempRecord(pos, 0));
            }
//# 
            for (int i = 0; i < images.size(); i++) {
                TempRecord rec = (TempRecord) images.elementAt(i);
                StorageTar.skipBytes(is, rec.recordStart - actualPos);
                rec.recordStart += 2;
                is.read(buff, 0, 2);
                rec.recordLength = Utils.readInt(buff, 0, 2);
                actualPos += (rec.recordStart - actualPos);
            //Logger.log(i + " buff[0]: " + buff[0] + " buff[1]: " + buff[1] + " tempRecord: " + rec.toString());
            }
//# 
            TempRecord[] imageArray = new TempRecord[tileCountX * tileCountY];
            for (int i = 0; i < (tileCountX * tileCountY); i++) {
                TempRecord rec = (TempRecord) images.elementAt(i);
                posX = i % tileCountX;
                posY = i / tileCountX;
                //Logger.log(i + ": posX: " + posX + " posY: " + posY);
                imageArray[tileCountY * posX + posY] = rec;
            }
//# 
            for (int i = 0; i < imageArray.length; i++) {
                TempRecord tr = imageArray[i];
                dos.writeInt(tr.recordStart);
                dos.writeInt(tr.recordLength);
            }
            imageArray = null;
//# 
            dos.flush();
            byte[] index = baos.toByteArray();
//# 
            baos.close();
            baos = null;
            dos.close();
            dos = null;
            is.close();
            is = null;
            fc.close();
            fc = null;
//# 
            // tarPath, imageDir,
            tar.setMpxFile(filePath, "", index, pngHeader);
        } catch (IOException ex) {
            Logger.error(ex.toString());
        } catch (NumberFormatException ex) {
            Logger.error(ex.toString());
        }
    }
//# 
    private static void encode(byte[] buffer, int size) {
        for (int i = 0; i < size; i++) {
            buffer[i] ^= ((byte) (i * 147));
        }
    }
//# 
    private static String readMWCHAR(byte[] buffer, int start, int len) {
        StringBuffer buff = new StringBuffer();
        for (int i = start; i < (start + len); i += 2) {
            buff.append((char) Utils.readInt(buffer, i, 2));
        }
        return buff.toString();
    }
//# 
    public static String getPhoneIdentification() {
        if (ident == null) {
            //get imei
            String imei = null;
            try {
                imei = System.getProperty("phone.imei");
                if (imei == null) {
                    imei = System.getProperty("com.nokia.IMEI");
                }
                if (imei == null) {
                    imei = System.getProperty("com.sonyericsson.imei");
                }
                if (imei == null) {
                    imei = System.getProperty("IMEI");
                }
                if (imei == null) {
                    imei = System.getProperty("com.motorola.IMEI");
                }
                if (imei == null) {
                    imei = System.getProperty("com.samsung.imei");
                }
                if (imei == null) {
                    imei = System.getProperty("com.siemens.imei");
                }
            } catch (Exception e) {
            }
            if (imei == null) {
                ident = System.getProperty("microedition.platform");
            } else {
                ident = imei + System.getProperty("microedition.platform");
            }
            ident = Sha1.encode(ident);
//# 
            ident = Utils.urlUTF8Encode(ident);
            return ident;
        } else {
            return ident;
        }
    }
//# 
    public static String getPhoneIdentificationEncoded() {
        if (identEncoded == null) {
            Vector vec = StringTokenizer.getVector((new Date()).toString(), " ");
            String date = Utils.addZerosBefore((String) vec.elementAt(2), 2) + Utils.addZerosBefore(String.valueOf(Utils.numericMonth((String) vec.elementAt(1)) + 1), 2) + (String) vec.elementAt(5);
//            System.out.println("ident urlencoded: " + getPhoneIdentification());
            String notEncoded = Utils.urlUTF8decode(getPhoneIdentification());
//            System.out.println("ident urldecoded:" + notEncoded);
//            System.out.println("date: " + date);
            byte[] decodedBytes = notEncoded.getBytes();
            byte[] encodedBytes = new byte[decodedBytes.length];
            byte[] keyBytes = date.getBytes();
            for (int i = 0; i < decodedBytes.length; i++) {
                encodedBytes[i] = (byte) (decodedBytes[i] ^ keyBytes[i % keyBytes.length]);
            }
//            System.out.println("before baseenc: ");
//            for (int i = 0; i < encodedBytes.length; i++) {
//                System.out.print(encodedBytes[i]);
//                System.out.print(',');
//            }
//            System.out.println();
//# 
            identEncoded = Base64.encodeBytes(encodedBytes);
//            System.out.println("encoded with base64: " + identEncoded);
            identEncoded = Utils.urlUTF8Encode(identEncoded);
//            System.out.println("urlencoded: " + identEncoded);
            return identEncoded;
        } else {
            return identEncoded;
        }
    }
}
//#endif
