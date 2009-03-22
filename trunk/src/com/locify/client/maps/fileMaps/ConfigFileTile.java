/*
 * ConfigFileTile.java
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

import com.locify.client.data.FileSystem;
import com.locify.client.maps.ImageRequest;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StorageTar;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.math.Matrix;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.DataElement;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Manages and describes config file needed for offline maps
 * @author MenKat
 */
public class ConfigFileTile {

    /** name from file */
    private String name;
    /** description of file */
    private String description;
    /** fileName of config file */
    private String fileName;
    /** size of tiles if map is separated into tiles */
    private int tileSizeX = 0;
    private int tileSizeY = 0;
    /** zoom value in multiTile map */
    private int zoom;
    /** type of projection defined in xml file */
    private String projectionType;
    /** are coordinates flat or spheric */
    private boolean sphericCoordinates;
    /** point that define raster */
    private Vector calibrationPoints;
    /** pixel size of whole image - X*/
    private int xmax;
    /** pixel size of whole image - Y*/
    private int ymax;
    /** is desvription loaded */
    private boolean descriptor_loaded;

    /** map viewPort */
    private FileMapViewPort mapViewPort;
    /** manager of this tile */
    private FileMapManager manager;

    /** loaded image */
    private Image image;

    /** size of screen */
    private int screenWidth = 0,  screenHeight = 0;
    /** starting number of tile X-line */
    private int tileX1;
    /** starting number of tile Y-line */
    private int tileY1;
    /** ending number of tile X-line */
    private int tileX2;
    /** ending number of tile Y-line */
    private int tileY2;
    /** num of files able to draw on screen X-line */
    private int numOfTilesXperScreen;
    /** num of files able to draw on screen Y-line */
    private int numOfTilesYperScreen;
    /** num of files of whole map X-line */
    private int numOfTotalTilesX;
    /** num of files of whole map Y-line */
    private int numOfTotalTilesY;
    private int moveX;
    private int moveY;

    private StringBuffer stringBuffer;
    
    /**
     * Create FileTile object
     * @param fileData <b>relative</b> path in fileSystem to file (relative from ROOT)
     * or loaded data from http link
     */
    public ConfigFileTile(String fileData, FileMapManager manager) {
//long time = System.currentTimeMillis();
//Logger.debug("ConfigFileTile ... initializing");
        this.manager = manager;
        this.stringBuffer = new StringBuffer();
        this.zoom = 0;
        
        try {
            calibrationPoints = new Vector(4);
            descriptor_loaded = false;

            FileConnection fileConnection;

            if (manager instanceof FileMapManagerTar) {
                fileName = FileMapManager.tar.getTarFile().substring(FileMapManager.tar.getTarFile().lastIndexOf('/') + 1);
                fileConnection = (FileConnection) Connector.open(FileMapManager.tar.getTarFile(), Connector.READ);
            } else {
                fileName = manager.mapFilename;
                fileConnection = (FileConnection) Connector.open(manager.mapPathPrefix +
                        manager.mapPath + manager.mapFilename, Connector.READ);
            }
            long size = -1;
            if (fileConnection.exists())
                size = fileConnection.fileSize();
            fileConnection.close();
            String data = R.getFileSystem().loadString(FileSystem.CACHE_FOLDER + fileName);
//Logger.debug("ConfigFileTile ... step1: " + (System.currentTimeMillis() - time));

            if (!loadConfigFile(data, size)) {
//Logger.debug("ConfigFileTile ... step2: " + (System.currentTimeMillis() - time));
                if (manager instanceof FileMapManagerTar) {
                    parseDescriptor(fileData, manager.mapCategory);
                } else if (manager.mapPath.startsWith("http://")) {
                    parseDescriptor(fileData, manager.mapCategory);
                } else {
                    parseDescriptor(R.getFileSystem().loadString(fileData), manager.mapCategory);
                }
//Logger.debug("ConfigFileTile ... step3: " + (System.currentTimeMillis() - time));
                if (isDescriptorLoaded()) {
                    if (!calculateViewPort()) {
                        descriptor_loaded = false;
                    } else {
                        saveConfigFile();
                    }
                }
            }
//Logger.debug("ConfigFileTile ... step4: " + (System.currentTimeMillis() - time));
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ConfigFileTile()", fileData);
        }
    }

    private boolean loadConfigFile(String data, long size) {
        if (data != null && size != -1) {
            String[] values = StringTokenizer.getArray(data, ";");
            if (GpsUtils.parseLong(values[0]) == size && values.length == 18) {
                tileSizeX = GpsUtils.parseInt(values[1]);
                tileSizeY = GpsUtils.parseInt(values[2]);
                sphericCoordinates = values[3].equals("true") ? true : false;
                projectionType = values[4];

                xmax = GpsUtils.parseInt(values[9]);
                ymax = GpsUtils.parseInt(values[10]);

                mapViewPort = new FileMapViewPort(GpsUtils.parseDouble(values[5]), GpsUtils.parseDouble(values[6]),
                        GpsUtils.parseDouble(values[7]), GpsUtils.parseDouble(values[8]),
                        xmax, ymax);
                mapViewPort.setInverseHelmert(GpsUtils.parseDouble(values[11]), GpsUtils.parseDouble(values[12]),
                        GpsUtils.parseDouble(values[13]), GpsUtils.parseDouble(values[14]));

                name = values[15].equals("null") ? manager.mapFilename : values[15];
                description = values[16].equals("null") ? "Map: " + projectionType : values[16];

                zoom = GpsUtils.parseInt(values[17]);
                descriptor_loaded = true;
                return true;
            }
        }
        return false;
    }

    private void saveConfigFile() {
        try {
            FileConnection fileConnection;
            stringBuffer.delete(0, stringBuffer.length());
            if (manager instanceof FileMapManagerTar) {
                    fileConnection = (FileConnection) Connector.open(FileMapManager.tar.getTarFile(), Connector.READ);
                    stringBuffer.append(fileConnection.fileSize());
                    stringBuffer.append(";" + tileSizeX + ";" + tileSizeY + ";");
            } else {
                fileConnection = (FileConnection) Connector.open(manager.mapPathPrefix +
                        manager.mapPath + manager.mapFilename, Connector.READ);
                stringBuffer.append(fileConnection.fileSize());
                stringBuffer.append(";" + tileSizeX + ";" + tileSizeY + ";");
            }
            fileConnection.close();

            stringBuffer.append(String.valueOf(sphericCoordinates) + ";");
            stringBuffer.append(projectionType + ";");
            mapViewPort.appendInverseHelmertParametres(stringBuffer);
            stringBuffer.append(";" + name + ";" + description + ";");
            stringBuffer.append(zoom);
//Logger.debug("Save " + stringBuffer.toString());
            R.getFileSystem().saveString(FileSystem.CACHE_FOLDER + fileName, stringBuffer.toString());
        } catch (IOException ex) {
            R.getErrorScreen().view(ex, "ConfigFileTile.saveConfigFile())", null);
        }
    }

    private void parseDescriptor(String data, int type) {
        if (type == FileMapManager.CATEGORY_MAP)
            parseDotMapDescriptor(data);
        else if (type == FileMapManager.CATEGORY_XML)
            parseLocifyDescriptor(data);
    }

    private void parseLocifyDescriptor(String data) {
        ByteArrayInputStream stream = null;
        InputStreamReader reader = null;
        XmlPullParser parser;
        
        try {
            int event;
            String tagName;
            parser = new KXmlParser();
            byte[] byteArray = data.getBytes();
            stream = new ByteArrayInputStream(byteArray);
            reader = new InputStreamReader(stream);
            parser.setInput(reader);

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();

                    if (tagName.equals("name")) {
                        this.name = parser.nextText();
                    } else if (tagName.equals("description")) {
                        this.description = parser.nextText();
                    } else if (tagName.equals("tile")) {
                        this.tileSizeX = Integer.parseInt(parser.nextText());
                        this.tileSizeY = this.tileSizeX;
                    } else if (tagName.equals("tileX")) {
                        this.tileSizeX = Integer.parseInt(parser.nextText());
                    } else if (tagName.equals("tileY")) {
                        this.tileSizeY = Integer.parseInt(parser.nextText());
                    } else if (tagName.equals("zoom")) {
                        this.zoom = Integer.parseInt(parser.nextText());
                    } else if (tagName.equals("projection")) {
                        setProjectionType(parser.nextText());
                    } else if (tagName.equals("position")) {
                        int x = 0, y = 0;
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            if (parser.getAttributeName(i).equalsIgnoreCase("x"))
                                x = GpsUtils.parseInt(parser.getAttributeValue(i));
                            else if (parser.getAttributeName(i).equalsIgnoreCase("y"))
                                y = GpsUtils.parseInt(parser.getAttributeValue(i));
                        }
                        
                        double lat, lon;
                        parser.nextTag();
                        boolean xy = parser.getName().equalsIgnoreCase("x") ||
                                parser.getName().equalsIgnoreCase("y");
                        lat = Double.parseDouble(parser.nextText().replace(',', '.'));
                        parser.nextTag();
                        lon = Double.parseDouble(parser.nextText().replace(',', '.'));

                        if (xy) {
                            sphericCoordinates = false;
                        } else {
                            sphericCoordinates = true;
                        }
                        addCalibrationPoint(x, y, lat, lon);
                    } else if (tagName.equals("imageWidth")) {
                        this.xmax = Integer.parseInt(parser.nextText());
                    } else if (tagName.equals("imageHeight")) {
                        this.ymax = Integer.parseInt(parser.nextText());
                    }

                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
            if (calibrationPoints.size() == 4 && tileSizeX != 0 && tileSizeY != 0) {
                descriptor_loaded = true;
            }

        } catch (Exception e) {
            R.getErrorScreen().view(e, "ConfigFileTile.parseLocifyDescriptor", data);
            descriptor_loaded = false;
        } finally {
            try {
                stream.close();
                reader.close();
            } catch (IOException ex) {
            }
        }
    }

    private void parseDotMapDescriptor(String data) {
        try {
//    long time = System.currentTimeMillis();
//    Logger.debug("ParseDotMapDescriptor() - start");
            Vector lines = StringTokenizer.getVector(data, "\n");
            if (lines.size() < 6)
                return;
            else {
                // set tileSize
                int[] tileSize = new int[2];
                tileSize[0] = Integer.MAX_VALUE;
                tileSize[1] = Integer.MAX_VALUE;

                if (manager instanceof FileMapManagerTar) {
                    tileSize = FileMapManager.tar.getMapTileSize();
                } else {
                    FileConnection con = null;
                    try {
                        con = (FileConnection) Connector.open(
                                manager.mapPathPrefix + manager.mapPath + createImageName(0, 0));
//System.out.println("File path: " + (manager.mapPathPrefix + manager.mapPath + createImageName(0, 0)));
                        if (con.exists()) {
                            Image img = Image.createImage(con.openInputStream());
                            tileSize[0] = img.getWidth();
                            tileSize[1] = img.getHeight();
                            img = null;
                        }
                    } catch (Exception ex) {
                        R.getErrorScreen().view(ex, "ConfigFileTile.getMapTileSizeDir()", null);
                    } finally {
                        try {
                            if (con != null) {
                                con.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                this.tileSizeX = tileSize[0];
                this.tileSizeY = tileSize[1];
                System.gc();
//    Logger.debug("  tileSizeX: " + tileSizeX + " tileSizeY: " + tileSizeY);
//    Logger.debug("  time: " + (System.currentTimeMillis() - time));
                // parsing map file
                Vector token;
                CalibrationPoint[] cal = null;
                for (int i = 0; i < lines.size(); i++) {
//    Logger.debug("  time" + i + ": " + (System.currentTimeMillis() - time));
                    token = StringTokenizer.getVector(String.valueOf(lines.elementAt(i)), ",");
                    // Map Projection,Mercator
                    if (i == 1) {
                        this.name = (String) lines.elementAt(i);
                    } else if (i == 4) {
                        setProjectionType((String) token.elementAt(0));
                    } else if (((String) token.elementAt(0)).startsWith("Map Projection")) {
                        //this.projectionType = ((String) token.elementAt(1)).trim();
                    } else if (((String) token.elementAt(0)).startsWith("Point")) {
                        int x, y;
                        double lat, lon, latX, lonY;
                        double[] coo = new double[2];

                        // test point
                        if (String.valueOf(token.elementAt(2)).trim().length() == 0)
                            continue;

                        x = GpsUtils.parseInt(token.elementAt(2));
                        y = GpsUtils.parseInt(token.elementAt(3));
                        lat = GpsUtils.parseDouble(token.elementAt(6)) + GpsUtils.parseDouble(token.elementAt(7)) / 60.0;
                        lon = GpsUtils.parseDouble(token.elementAt(9)) + GpsUtils.parseDouble(token.elementAt(10)) / 60.0;
                        latX = GpsUtils.parseDouble(token.elementAt(15));
                        lonY = GpsUtils.parseDouble(token.elementAt(14));

                        if (lat != 0 && lon != 0) {
                            coo[0] = lat;
                            coo[1] = lon;
                            sphericCoordinates = true;
                        } else if (latX != 0 && lonY != 0) {
                            coo[0] = latX;
                            coo[1] = lonY;
                            sphericCoordinates = false;
                        } else
                            continue;

                        addCalibrationPoint(x, y, coo[0], coo[1]);
                    // MMPNUM,4
                    } else if (((String) token.elementAt(0)).startsWith("MMPNUM")) {
                        int size = GpsUtils.parseInt(token.elementAt(1));
                        if (size >= 4) {
                            cal = new CalibrationPoint[size];
                            for (int j = 0; j < cal.length; j++) {
                                cal[j] = new CalibrationPoint();
                            }
                        }
                    // MMPXY,1,0,0
                    } else if (((String) token.elementAt(0)).startsWith("MMPXY")) {
                        if (cal.length > 3) {
                            CalibrationPoint point = cal[GpsUtils.parseInt(token.elementAt(1)) - 1];
                            point.x = GpsUtils.parseInt(token.elementAt(2));
                            point.y = GpsUtils.parseInt(token.elementAt(3));
                        }
                    // MMPLL,1,  11.751193,  50.953687
                    } else if (((String) token.elementAt(0)).startsWith("MMPLL")) {
                        if (cal.length > 3) {
                            CalibrationPoint point = cal[GpsUtils.parseInt(token.elementAt(1)) - 1];
                            double lat = GpsUtils.parseDouble(token.elementAt(3));
                            double lon = GpsUtils.parseDouble(token.elementAt(2));
                            point.position = new Location4D(lat, lon, 0.0f);
                        }
                    // IWH,Map Image Width/Height,2000,2000
                    } else if (((String) token.elementAt(0)).startsWith("IWH")) {
                        this.xmax = GpsUtils.parseInt(token.elementAt(2));
                        this.ymax = GpsUtils.parseInt(token.elementAt(3));
                    }
                }

    //            if (cal != null && cal.length > 3) { // && calibrationPoints.size() < 4) {
                if (cal != null && cal.length > 3 && calibrationPoints.size() < 2) {
                    calibrationPoints.removeAllElements();
                    sphericCoordinates = true;
                    for (int i = 0; i < cal.length; i++) {
                        addCalibrationPoint(cal[i].x, cal[i].y,
                                cal[i].position.getLatitude(), cal[i].position.getLongitude());
                    }
                    cal = null;
                }

                if (calibrationPoints.size() >= 2 &&
                        xmax != 0 && ymax != 0 &&
                        tileSizeX != 0 && tileSizeY != 0 &&
                        tileSizeX != Integer.MAX_VALUE && tileSizeY != Integer.MAX_VALUE) {
                    descriptor_loaded = true;
                } else {
                    Logger.error("  Map initializing problem: calibrationPoints.size() - " + calibrationPoints.size() +
                            "  xmax - " + xmax +
                            "  ymax - " + ymax +
                            "  tileSizeX - " + tileSizeX +
                            "  tileSizeY - " + tileSizeY);
                }
//    Logger.debug("  finish: " + (System.currentTimeMillis() - time));
            }
        } catch (Exception ex) {
            Logger.error("ConfigFileTile.parseDotMapDescriptor(): " + data);
        }
    }

    private void setProjectionType(String projection) {
        if (projection.startsWith("WGS 84") || projection.startsWith("WGS84") ||
                projection.startsWith("UTM")) {
            this.projectionType = "UTM";
        } else if (projection.startsWith("Pulkovo 1942") || projection.startsWith("S42") ||
                projection.startsWith("S-42")) {
            this.projectionType = "S42";
        } 
    }

    public boolean isSphericCoordinate() {
        return sphericCoordinates;
    }

    private void addCalibrationPoint(int x, int y, double lat, double lon) {
//Logger.log("  ConfigFileTile.addCalibrationPoint x: " + x + " y: " + y + " lat: " + lat + " lon: " + lon);
        CalibrationPoint calpoint = new CalibrationPoint();

        calpoint.x = x;
        calpoint.y = y;
        calpoint.position = new Location4D(lat, lon, 0);
        this.calibrationPoints.addElement(calpoint);
    }

    public boolean isDescriptorLoaded() {
        return descriptor_loaded;
    }

    public String getProjectionType() {
        return projectionType;
    }

    private boolean calculateViewPort() {
        try {
            // compute Helmert transformation
            Matrix A = new Matrix(calibrationPoints.size() * 2, 4);
            Matrix X = new Matrix(calibrationPoints.size() * 2, 1);

            CalibrationPoint calP;
            for (int i = 0; i < calibrationPoints.size(); i++) {
                calP = (CalibrationPoint) calibrationPoints.elementAt(i);
                A.set(i, 0, 1);
                A.set(i, 1, 0);
                A.set(i, 2, calP.position.getLatitude());
                A.set(i, 3, -1 * calP.position.getLongitude());

                X.set(i, 0, calP.x);
                X.set(i + calibrationPoints.size(), 0, calP.y);
            }
            for (int i = calibrationPoints.size(); i < (2 * calibrationPoints.size()); i++) {
                calP = (CalibrationPoint) calibrationPoints.elementAt(i - calibrationPoints.size());
                A.set(i, 0, 0);
                A.set(i, 1, 1);
                A.set(i, 2, calP.position.getLongitude());
                A.set(i, 3, calP.position.getLatitude());
            }

            Matrix H = (A.transpose().times(A)).inverse().times(A.transpose().times(X));
            mapViewPort = new FileMapViewPort(H.get(0, 0), H.get(1, 0), H.get(2, 0), H.get(3, 0), xmax, ymax);

            // compute inverse Helmert transformation
            A.fill(0);
            X.fill(0);
            
            for (int i = 0; i < calibrationPoints.size(); i++) {
                calP = (CalibrationPoint) calibrationPoints.elementAt(i);
                A.set(i, 0, 1);
                A.set(i, 1, 0);
                A.set(i, 2, calP.x);
                A.set(i, 3, -1 * calP.y);

                X.set(i, 0, calP.position.getLatitude());
                X.set(i + calibrationPoints.size(), 0, calP.position.getLongitude());
            }
            for (int i = calibrationPoints.size(); i < (2 * calibrationPoints.size()); i++) {
                calP = (CalibrationPoint) calibrationPoints.elementAt(i - calibrationPoints.size());
                A.set(i, 0, 0);
                A.set(i, 1, 1);
                A.set(i, 2, calP.y);
                A.set(i, 3, calP.x);
            }

            H = (A.transpose().times(A)).inverse().times(A.transpose().times(X));
            mapViewPort.setInverseHelmert(H.get(0, 0), H.get(1, 0), H.get(2, 0), H.get(3, 0));
            return true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ConfigFileTile.calculateViewPort()", "");
            return false;
        }
    }

    public FileMapViewPort getAvailableViewPort() {
        return mapViewPort;
    }

    public synchronized boolean drawImageSingle(Graphics gr, FileMapViewPort targetPort) {
        try {
            Point2D mapPoint = mapViewPort.convertGeoToMapPixel(targetPort.center);
            int x1, x2, y1, y2;
            if (mapViewPort.containsPosition(targetPort.center)) {
                x1 = Math.max(0, (int) (mapPoint.getX() - targetPort.xmax / 2));
                y1 = Math.max(0, (int) (mapPoint.getY() - targetPort.ymax / 2));
                x2 = Math.min(image.getWidth(), (int) (mapPoint.getX() + targetPort.xmax / 2));
                y2 = Math.min(image.getHeight(), (int) (mapPoint.getY() + targetPort.ymax / 2));

                drawImageSingle(image, x1, x2, y1, y2, gr, targetPort);
                return true;
            }
            return false;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileTile.draw()", null);
            return false;
        }
    }

    public synchronized boolean drawImageMulti(Graphics gr, FileMapViewPort targetPort, StorageTar tar,
            int mapPanX, int mapPanY) {
        try {
            Point2D mapPoint = mapViewPort.convertGeoToMapPixel(targetPort.center);
//System.out.println("ConfigFileTile.drawImageMulti()");
//System.out.println("\n  mapViewPort " + mapViewPort.toString());
//System.out.println("\n  targetPort " + targetPort.toString());
//System.out.println("\n  center: " + targetPort.center.toString());
            int x1,y1;

            if (screenWidth == 0) {
                screenWidth = R.getMapScreen().getWidth();
                screenHeight = R.getMapScreen().getHeight();
                numOfTotalTilesX = (int) Math.floor(xmax / tileSizeX);
                numOfTotalTilesY = (int) Math.floor(ymax / tileSizeY);
            }
            // top left map point (in map pixel coordinates)
            Point2D mapPointTL = new Point2D.Double(mapPoint.getX() - screenWidth / 2,
                    mapPoint.getY() - screenHeight / 2);
            // bottom right map point (in map pixel coordinates)
            Point2D mapPointBR = new Point2D.Double(mapPoint.getX() + screenWidth / 2,
                    mapPoint.getY() + screenHeight / 2);

            tileX1 = (int) Math.floor(mapPointTL.getX() / tileSizeX);
            tileY1 = (int) Math.floor(mapPointTL.getY() / tileSizeY);
            tileX2 = (int) Math.floor(mapPointBR.getX() / tileSizeX);
            tileY2 = (int) Math.floor(mapPointBR.getY() / tileSizeY);
            numOfTilesXperScreen = tileX2 - tileX1 + 1;
            numOfTilesYperScreen = tileY2 - tileY1 + 1;
            moveX = (int) ((tileX1 * tileSizeX) - mapPointTL.getX());
            moveY = (int) ((tileY1 * tileSizeY) - mapPointTL.getY());
//System.out.println(mapPointTL.getX() + " " + tileSizeX + " " + tileX1);
//System.out.println(mapPointTL.getY() + " " + tileSizeY + " " + tileY1);
//System.out.println(mapPointBR.getX() + " " + tileSizeX + " " + tileX2);
//System.out.println(mapPointBR.getY() + " " + tileSizeY + " " + tileY2);
//System.out.println("\n  drawImageMulti - moveX: " + moveX + " moveY: " + moveY);
//System.out.println("\n  drawImageMulti - centerMapPointX: " + mapPoint.getX() + " mapPointY: " + mapPoint.getY());
//System.out.println("\n  drawImageMulti - tileX1: " + tileX1 + " tileY1: " + tileY1);
//System.out.println("\n  drawImageMulti - tileX2: " + tileX2 + " tileY2: " + tileY2);
            Vector imageNames = new Vector();
            String imageName = null;
            for (int i = 0; i < numOfTilesXperScreen; i++) {
                for (int j = 0; j < numOfTilesYperScreen; j++) {
                    if (imageHaveToExist(tileX1 + i, tileY1 + j)) {
                        imageName =  createImageName(i, j);
                        if (tar != null) {
//System.out.println("Add: " + (manager.mapPathPrefix + manager.mapPath + imageName) + "  " + tar.getTarFile() + " " + tar.getTarRecord(imageName));
                            imageNames.addElement(new ImageRequest(
                                    manager.mapPathPrefix + manager.mapPath + imageName,
                                    tar.getTarFile(), tar.getTarRecord(imageName)));

                        } else {
//Logger.debug("ConfigFileTile.drawImageMulti() Add: " + manager.mapPathPrefix + manager.mapPath + imageName);
                            imageNames.addElement(new ImageRequest(
                                    manager.mapPathPrefix + manager.mapPath + imageName));
                        }
                    }
                }
            }
            manager.getCache().newRequest(imageNames);

            for (int i = 0; i < numOfTilesXperScreen; i++) {
                for (int j = 0; j < numOfTilesYperScreen; j++) {
                    if (!imageHaveToExist(tileX1 + i, tileY1 + j)) {
                        image = MapScreen.getImageNotExisted(tileSizeX, tileSizeY);
                    } else {
                        imageName = createImageName(i, j);
                        image = manager.getCache().getImage(
                                manager.mapPathPrefix + manager.mapPath + imageName);
                    }
                    if (image != null) {
                        x1 = moveX + i * tileSizeX;
                        y1 = moveY + j * tileSizeY;
                        if (x1 < screenWidth && y1 < screenHeight) {
//                            System.out.println("\n  draw: x1 " + x1 + " y: " + y1 +
//                                    " img: " + ( manager.mapPathPrefix + manager.mapPath + imageName));
                            gr.drawImage(image, x1 + mapPanX, y1 + mapPanY, Graphics.LEFT | Graphics.TOP);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileTile.draw()", null);
            return false;
        }
    }

    private String createImageName(int i, int j) {
        if (stringBuffer.length() > 0)
        stringBuffer.delete(0, stringBuffer.length());
        stringBuffer.append(manager.mapImageDir);
        if (manager.mapCategory == FileMapManager.CATEGORY_XML) {
            stringBuffer.append(zoom + "_" + (tileX1 + i) + "_" + (tileY1 + j) + ".png");
        } else if (manager.mapCategory == FileMapManager.CATEGORY_MAP) {
            stringBuffer.append(manager.mapFilename.substring(0, manager.mapFilename.lastIndexOf('.'))
                    + "_" + ((tileX1 + i) * tileSizeX)
                    + "_" + ((tileY1 + j) * tileSizeY) + ".png");
        }
        return stringBuffer.toString();
    }

    private boolean imageHaveToExist(int tileX, int tileY) {
//System.out.println("ConfigFileTile.imageHaveToExist() - getTile: tX: " + tileX + " tY: " + tileY);
        return (tileX >= 0 && tileY >= 0 && tileX <= numOfTotalTilesX && tileY <= numOfTotalTilesY);
    }

    private boolean drawImageSingle(Image image, int x1, int x2, int y1, int y2, Graphics gr, FileMapViewPort targetPort) {
        int width = x2 - x1;
        int height = y2 - y1;
        int to_x = 0, to_y = 0;

        try {
            if (width == targetPort.xmax) {
                to_x = 0;
            } else {
                if (x1 == 0) {
                    to_x = targetPort.xmax - width;
                } else {
                    to_x = 0;
                }
            }

            if (height == targetPort.ymax) {
                to_y = 0;
            } else {
                if (y1 == 0) {
                    to_y = targetPort.ymax - height;
                } else {
                    to_y = 0;
                }
            }

            gr.drawRegion(
                    //source
                    image,
                    x1, y1, width, height,
                    Sprite.TRANS_NONE,
                    //target
                    to_x, to_y, Graphics.LEFT | Graphics.TOP);

            return true;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileTile.drawImageSingle()", " draw:" +
                    " image null: " + (image == null) +
                    " orig img.width: " + image.getWidth() +
                    " orig img.height: " + image.getHeight() +
                    "  x1: " + x1 +
                    "  y1: " + y1 +
                    "  x2: " + x2 +
                    "  y2: " + y2 +
                    "  to_X: " + to_x +
                    "  to_y: " + to_y);
            return false;
        }
    }

    public String toString() {
        return "image: " + manager.mapPath + " center: " + mapViewPort.center.toString();
    }

    public double getLatDiffPerPixel() {
//System.out.println("!!! " + mapViewPort.latitude_dimension + " " + ymax);
        return mapViewPort.latitude_dimension / ymax;
    }
    
    public double getLonDiffPerPixel() {
        return mapViewPort.longitude_dimension / xmax;
    }

    public int getTileSizeX() {
        return tileSizeX;
    }

    public int getTileSizeY() {
        return tileSizeY;
    }

    public int getXmax() {
        return xmax;
    }

    public int getYmax() {
        return ymax;
    }

    public int getZoom() {
        return zoom;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public class CalibrationPoint {

        public int x;
        public int y;
        public Location4D position;

        public CalibrationPoint() {
            x = 0;
            y = 0;
            position = new Location4D(0.0, 0.0, 0.0f);
        }

        public String toString() {
            return "[" + x + "," + y + "] = {" + position.getLongitude() + "," + position.getLatitude() + "}";
        }
    }
}


/**
 * Subcreate image from whole map file.
 * @param center center of target subimage in metres on sphere
 * @param zoom zoom of required image (zoom == metres x zoom per pixel)
 * @param viewportBounds bounds of screen
 */
//    public Image getSubImage(int x, int y, int w, int h) {
//        ImageUtil.
//        // request for image
//        Point2D imageTopLeft = new Point2D.Double(center.getX() - (viewportBounds.getWidth() / 2) * zoom,
//                center.getY() + (viewportBounds.getHeight() / 2) * zoom);
//
//        double imageWidth = viewportBounds.getWidth() * zoom;
//        double imageHeight = viewportBounds.getHeight() * zoom;
//
//        // test points if at least one lie in image
//        if (size.intersects(imageTopLeft.getX(), imageTopLeft.getY(),
//                imageWidth, imageHeight)) {
//
//            // repair subImageBorders
//            Point2D pointTL = transformToImage(imageTopLeft.getX(), imageTopLeft.getY());
//            Point2D pointBD = transformToImage(imageTopLeft.getX() + imageWidth, imageTopLeft.getY() - imageHeight);
//            Image smallImage = Image.createImage(image,
//                    (int) pointTL.getX(),
//                    (int) pointTL.getY(),
//                    (int) (pointBD.getX() - pointTL.getX()),
//                    (int) (pointBD.getY() - pointTL.getY()),
//                    Sprite.TRANS_NONE);
//            return getThumbnail(smallImage, viewportBounds.width, viewportBounds.height);
//        } else {
//            return null;
//        }
//    }
/**
 * Gets the thumbnail that fit with given screen width, height and padding ..
 *
 * @param image The source image
 * @param padding padding to the screen
 * @return scaled image
 */
//    private final Image getThumbnailWrapper(Image image, int expectedWidth, int expectedHeight, int padding) {
//        final int sourceWidth = image.getWidth();
//        final int sourceHeight = image.getHeight();
//        int thumbWidth = -1;
//        int thumbHeight = -1;
//
//        // big width
//        if (sourceWidth >= sourceHeight) {
//            thumbWidth = expectedWidth - padding;
//            thumbHeight = thumbWidth * sourceHeight / sourceWidth;
//
//            // fits to height ?
//            if (thumbHeight > (expectedHeight - padding)) {
//                thumbHeight = expectedHeight - padding;
//                thumbWidth = thumbHeight * sourceWidth / sourceHeight;
//            }
//        } else {
//            // big height
//            thumbHeight = expectedHeight - padding;
//            thumbWidth = thumbHeight * sourceWidth / sourceHeight;
//            // fits to width ?
//            if (thumbWidth > (expectedWidth - padding)) {
//                thumbWidth = expectedWidth - padding;
//                thumbHeight = thumbWidth * sourceHeight / sourceWidth;
//            }
//        }
//
//        // XXX As we do not have floating point, sometimes the thumbnail resolution gets bigger ...
//        // we are trying hard to avoid that ..
//        thumbHeight = (sourceHeight < thumbHeight) ? sourceHeight : thumbHeight;
//        thumbWidth = (sourceWidth < thumbWidth) ? sourceWidth : thumbWidth;
//
//        return getThumbnail(image, thumbWidth, thumbHeight);
//    }
/**
 * Gets thumbnail with a height and width specified ..
 * @param image
 * @param thumbWidth
 * @param thumbHeight
 * @return scaled image
 */
//    private final Image getThumbnail(Image image, int thumbWidth, int thumbHeight) {
//        int x, y, pos, tmp, z = 0;
//        final int sourceWidth = image.getWidth();
//        final int sourceHeight = image.getHeight();
//
//        // integer ratio ..
//        final int ratio = sourceWidth / thumbWidth;
//
//        // buffer where we read in data from image source
//        final int[] in = new int[sourceWidth];
//
//        // buffer of output thumbnail image
//        final int[] out = new int[thumbWidth * thumbHeight];
//
//        final int[] cols = new int[thumbWidth];
//
//        // pre-calculate columns we need to access from source image
//        for (x = 0        ,pos = 0; x < thumbWidth; x++) {
//            cols[x] = pos;
//
//            // increase the value without fraction calculation
//            pos += ratio;
//            tmp = pos + (thumbWidth - x) * ratio;
//            if (tmp > sourceWidth) {
//                pos--;
//            } else if (tmp < sourceWidth - ratio) {
//                pos++;
//            }
//        }
//
//        // read through the rows ..
//        for (y = 0        , pos = 0        , z = 0; y < thumbHeight; y++) {
//
//            // read a single row ..
//            image.getRGB(in, 0, sourceWidth, 0, pos, sourceWidth, 1);
//
//            for (x = 0; x < thumbWidth; x++, z++) {
//                // write this row to thumbnail
//                out[z] = in[cols[x]];
//            }
//
//            pos += ratio;
//            tmp = pos + (thumbHeight - y) * ratio;
//            if (tmp > sourceHeight) {
//                pos--;
//            } else if (tmp < sourceHeight - ratio) {
//                pos++;
//            }
//        }
//        return Image.createRGBImage(out, thumbWidth, thumbHeight, false);
//    }
