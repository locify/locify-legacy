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

import com.locify.client.maps.ImageRequest;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StorageTar;
import com.locify.client.utils.StringTokenizer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
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
    /** size of tiles if map is separated into tiles */
    private int tileSizeX = 0;
    private int tileSizeY = 0;
    /** zoom value in multiTile map */
    private int zoom;
    /** type of projection defined in xml file */
    private String projectionType;
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

    /** max values from calibrationPoints */
    private double min_longitude;
    private double max_longitude;
    private double min_latitude;
    private double max_latitude;

    /** size of screen */
    private int screenWidth = 0,  screenHeight = 0;
    /** starting number of tile X-line */
    private int tileX;
    /** starting number of tile Y-line */
    private int tileY;
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

    /**
     * Create FileTile object
     * @param fileData <b>relative</b> path in fileSystem to file (relative from ROOT)
     * or loaded data from http link
     */
    public ConfigFileTile(String fileData, FileMapManager manager) {
        this.manager = manager;
        try {
            calibrationPoints = new Vector(4);
            descriptor_loaded = false;
            if (manager instanceof FileMapManagerTarTrekBuddy) {
                parseDotMapDescriptor(fileData);
            } else if (manager instanceof FileMapManagerTarLocify) {
                parseLocifyDescriptor(fileData);
            } else if (manager.mapPath.startsWith("http://")) {
                parseLocifyDescriptor(fileData);
            } else {
                if (R.getFileSystem().exists(fileData)) {
                    parseLocifyDescriptor(R.getFileSystem().loadString(fileData));
                }
            }

            if (isDescriptorLoaded()) {
                calculateViewPort();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ConfigFileTile()", fileData);
        }
    }

    public void setTilesVariables() {
    }

    private void clearMinMax() {
        min_longitude = Double.MAX_VALUE;
        max_longitude = Double.MIN_VALUE;
        min_latitude = Double.MAX_VALUE;
        max_latitude = Double.MIN_VALUE;
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
                        this.projectionType = parser.nextText();
                    } else if (tagName.equals("position")) {
                        int x, y;
                        x = Integer.parseInt(parser.getAttributeValue(1));
                        y = Integer.parseInt(parser.getAttributeValue(2));

                        double lat, lon;
                        parser.nextTag();
                        lat = Double.parseDouble(parser.nextText().replace(',', '.'));
                        parser.nextTag();
                        lon = Double.parseDouble(parser.nextText().replace(',', '.'));

                        addCalibrationPoint(x, y, lat, lon);
                    //System.out.println("Added calib. point:" + calpoint);
                    } else if (tagName.equals("imageWidth")) {
                        this.xmax = Integer.parseInt(parser.nextText());
                    } else if (tagName.equals("imageHeight")) {
                        this.ymax = Integer.parseInt(parser.nextText());
                    }

                } else if (event == XmlPullParser.END_DOCUMENT) {
                        break;
                }
            }
            if (calibrationPoints.size() == 4) {
                descriptor_loaded = true;
            }

        } catch (Exception e) {
            R.getErrorScreen().view(e, "RouteData.parseRoute", data);
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
//System.out.println("ParseDotMapDescriptor() - start");
        Vector lines = StringTokenizer.getVector(data, "\n");
        if (lines.size() < 6)
            return;
        else {
            // set tileSize
            int[] tileSize = FileMapManager.tar.getMapTileSize();
            this.tileSizeX = tileSize[0];
            this.tileSizeY = tileSize[1];

            // parsing map file
            Vector token;
            clearMinMax();
            CalibrationPoint[] cal = null;
            for (int i = 0; i < lines.size(); i++) {
                token = StringTokenizer.getVector(String.valueOf(lines.elementAt(i)), ",");
                // Map Projection,Mercator
                if (((String) token.elementAt(0)).startsWith("Map Projection")) {
                    this.projectionType = ((String) token.elementAt(1)).trim();
                // Point01,xy,0,0,in,deg,50,9.50552549907,N,14,15.7800292969,E,grid,,,,N
                // Point01,xy,0,0,in, deg, 51,37.0809929264226,N,11,15,E, grid,  , , ,N

                // Point01,xy,  243,  438,in, deg,    ,        ,N,    ,        ,E, grid,   ,    3414424,    5563747,N
                // Point02,xy,15736,  438,in, deg,    ,        ,N,    ,        ,E, grid,   ,    3538424,    5563747,N
                // Point03,xy,15736,12059,in, deg,    ,        ,N,    ,        ,E, grid,   ,    3538424,    5470747,N
                // Point04,xy,  243,12059,in, deg,    ,        ,N,    ,        ,E, grid,   ,    3414424,    5470747,N
                } else if (((String) token.elementAt(0)).startsWith("Point")) {
                    int x, y;
                    double lat, lon, latX, lonY;
                    double[] coo;

                    // test point
                    if (String.valueOf(token.elementAt(2)).trim().length() == 0)
                        continue;
                    
                    x = parseInt(token.elementAt(2));
                    y = parseInt(token.elementAt(3));
                    lat = parseDouble(token.elementAt(6)) + parseDouble(token.elementAt(7)) / 60.0;
                    lon = parseDouble(token.elementAt(9)) + parseDouble(token.elementAt(10)) / 60.0;

                    lonY = parseDouble(token.elementAt(14));
                    latX = parseDouble(token.elementAt(15));

                    if (lat != 0 && lon != 0) {
                        coo = FileMapManager.getProjection(projectionType).projectionToFlat(lat, lon);
                    } else if (latX != 0 && lonY != 0) {
                        coo = new double[2];
                        coo[0] = latX;
                        coo[1] = lonY;
                    } else
                        continue;

                    lat = coo[0];
                    lon = coo[1];
                    addCalibrationPoint(x, y, lat, lon);
                //MMPNUM,4
                } else if (((String) token.elementAt(0)).startsWith("MMPNUM")) {
                    int size = parseInt(token.elementAt(1));
                    if (size >= 4) {
                        cal = new CalibrationPoint[size];
                        for (int j = 0; j < cal.length; j++) {
                            cal[j] = new CalibrationPoint();
                        }
                    }
                // MMPXY,1,0,0
                } else if (((String) token.elementAt(0)).startsWith("MMPXY")) {
                    if (cal.length > 3) {
                        CalibrationPoint point = cal[parseInt(token.elementAt(1)) - 1];
                        point.x = parseInt(token.elementAt(2));
                        point.y = parseInt(token.elementAt(3));
                    }
                // MMPLL,1,  11.751193,  50.953687
                } else if (((String) token.elementAt(0)).startsWith("MMPLL")) {
                    if (cal.length > 3) {
                        CalibrationPoint point = cal[parseInt(token.elementAt(1)) - 1];
                        double lat = parseDouble(token.elementAt(3));
                        double lon = parseDouble(token.elementAt(2));

                        double[] coo;
                        coo = FileMapManager.getProjection(projectionType).projectionToFlat(lat, lon);
//System.out.println("Old lat: " + lat + " lon: " + lon);
                        lat = coo[0];
                        lon = coo[1];
//System.out.println("New lat: " + lat + " lon: " + lon);
                        point.position = new Location4D(lat, lon, 0.0f);
                    }
                // IWH,Map Image Width/Height,2000,2000
                } else if (((String) token.elementAt(0)).startsWith("IWH")) {
                    this.xmax = parseInt(token.elementAt(2));
                    this.ymax = parseInt(token.elementAt(3));
                }
            }

            if (cal != null && cal.length > 3) { // && calibrationPoints.size() < 4) {
            //if (cal != null && cal.length > 3 && calibrationPoints.size() < 4) {
                calibrationPoints.removeAllElements();
                clearMinMax();
                for (int i = 0; i < cal.length; i++) {
                    addCalibrationPoint(cal[i].x, cal[i].y,
                            cal[i].position.getLatitude(), cal[i].position.getLongitude());
                }
                cal = null;
            }

            if (calibrationPoints.size() == 4 &&
                    xmax != 0 && ymax != 0 &&
                    tileSizeX != 0 && tileSizeY != 0 &&
                    tileSizeX != Integer.MAX_VALUE && tileSizeY != Integer.MAX_VALUE) {
                descriptor_loaded = true;
            } else {
                Logger.error("Map initializing problem: calibrationPoints.size() - " + calibrationPoints.size() +
                        "  xmax - " + xmax +
                        "  ymax - " + ymax +
                        "  tileSizeX - " + tileSizeX +
                        "  tileSizeY - " + tileSizeY);
            }
        }
//System.out.println("ParseDotMapDescriptor() - end");
    }

    private double parseDouble(Object obj) {
        try {
            return Double.parseDouble(String.valueOf(obj).trim());
        } catch (Exception ex) {
            System.err.println("'" + String.valueOf(obj) + "'  ex: " + ex.toString());
        }
        return 0.0;
    }

    private int parseInt(Object obj) {
        try {
            return Integer.parseInt(String.valueOf(obj).trim());
        } catch (Exception ex) {
            System.err.println("'" + String.valueOf(obj) + "'  ex: " + ex.toString());
        }
        return 0;
    }

    private void addCalibrationPoint(int x, int y, double lat, double lon) {
        CalibrationPoint calpoint = new CalibrationPoint();

        calpoint.x = x;
        calpoint.y = y;

        calpoint.position = new Location4D(lat, lon, 0);

        if (calpoint.position.getLatitude() < min_latitude) {
            min_latitude = calpoint.position.getLatitude();
        }

        if (calpoint.position.getLatitude() > max_latitude) {
            max_latitude = calpoint.position.getLatitude();
        }

        if (calpoint.position.getLongitude() < min_longitude) {
            min_longitude = calpoint.position.getLongitude();
        }

        if (calpoint.position.getLongitude() > max_longitude) {
            max_longitude = calpoint.position.getLongitude();
        }

        this.calibrationPoints.addElement(calpoint);
    }

    public boolean isDescriptorLoaded() {
        return descriptor_loaded;
    }

    public String getProjectionType() {
        return projectionType;
    }

    private void calculateViewPort() {
        // fix calibration points
        CalibrationPoint A = (CalibrationPoint) calibrationPoints.elementAt(0);
        CalibrationPoint B = (CalibrationPoint) calibrationPoints.elementAt(1);
        CalibrationPoint C = (CalibrationPoint) calibrationPoints.elementAt(2);
        CalibrationPoint D = (CalibrationPoint) calibrationPoints.elementAt(3);

//System.out.print("\nA: " + A.toString());
//System.out.print("\nB: " + B.toString());
//System.out.print("\nC: " + C.toString());
//System.out.print("\nD: " + D.toString());

        if (A.position.getLongitude() > B.position.getLongitude()) {
            CalibrationPoint temp = A;
            A = B;
            B = temp;
        }
        if (C.position.getLongitude() > D.position.getLongitude()) {
            CalibrationPoint temp = C;
            C = D;
            D = temp;
        }

        double lonPerPix = (((B.position.getLongitude() - A.position.getLongitude()) / (B.x - A.x)) +
                ((D.position.getLongitude() - C.position.getLongitude()) / (D.x - C.x))) / 2;
        double latPerPix = (((A.position.getLatitude() - C.position.getLatitude()) / (C.y - A.y)) +
                ((B.position.getLatitude() - D.position.getLatitude()) / (D.y - B.y))) / 2;

        // fix A (top left)
        A.position = new Location4D(
                A.position.getLatitude() + A.y * latPerPix,
                A.position.getLongitude() - A.x * lonPerPix,
                0.0f);
        A.x = 0;
        A.y = 0;

        // fix B (top right)
        B.position = new Location4D(
                B.position.getLatitude() + B.y * latPerPix,
                B.position.getLongitude() + (xmax - B.x) * lonPerPix,
                0.0f);
        B.x = xmax;
        B.y = 0;

        // fix C (bottom left)
        C.position = new Location4D(
                C.position.getLatitude() - (ymax - C.y) * latPerPix,
                C.position.getLongitude() - C.x * lonPerPix,
                0.0f);
        C.x = 0;
        C.y = ymax;

        // fix D (bottom right)
        D.position = new Location4D(
                D.position.getLatitude() - (ymax - D.y) * latPerPix,
                D.position.getLongitude() + (xmax - D.x) * lonPerPix,
                0.0f);
        D.x = xmax;
        D.y = ymax;

//System.out.print("\nA: " + A.toString());
//System.out.print("\nB: " + B.toString());
//System.out.print("\nC: " + C.toString());
//System.out.print("\nD: " + D.toString());

        mapViewPort = new FileMapViewPort(A.position, B.position, C.position, D.position, xmax, ymax);
    }

    public FileMapViewPort getAvailableViewPort() {
        return mapViewPort;
    }

    public synchronized boolean drawImageSingle(Graphics gr, FileMapViewPort targetPort) {
        try {
            Point2D mapPoint = mapViewPort.getPointAnyWhere(targetPort.center);
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

    public synchronized boolean drawImageMulti(Graphics gr, FileMapViewPort targetPort, StorageTar tar) {
        try {
            Point2D mapPoint = mapViewPort.getPointAnyWhere(targetPort.center);
//System.out.println("mapViewPort " + mapViewPort.toString());
//System.out.println("targetPort " + targetPort.toString());
//System.out.println("Center: " + targetPort.center.toString());
            int x1, x2, y1, y2;

            if (screenWidth == 0) {
                screenWidth = R.getMapScreen().getWidth();
                screenHeight = R.getMapScreen().getHeight();
                numOfTilesXperScreen = (int) Math.floor(screenWidth / tileSizeX) + 2;
                numOfTilesYperScreen = (int) Math.floor(screenHeight / tileSizeY) + 2;
                numOfTotalTilesX = (int) Math.floor(xmax / tileSizeX);
                numOfTotalTilesY = (int) Math.floor(ymax / tileSizeY);
            }
            mapPoint = new Point2D.Double(mapPoint.getX() - screenWidth / 2,
                    mapPoint.getY() - screenHeight / 2);
            tileX = (int) Math.floor(mapPoint.getX() / tileSizeX);
            tileY = (int) Math.floor(mapPoint.getY() / tileSizeY);
            moveX = (int) ((tileX * tileSizeX) - mapPoint.getX());
            moveY = (int) ((tileY * tileSizeY) - mapPoint.getY());
//System.out.println("drawImageMulti - moveX: " + moveX + " moveY: " + moveY);
//System.out.println("drawImageMulti - mapPointX: " + mapPoint.getX() + " mapPointY: " + mapPoint.getY());
//System.out.println("drawImageMulti - tileX: " + tileX + " tileY: " + tileY);
            Vector imageNames = new Vector();
            String imageName;
            for (int i = 0; i < numOfTilesXperScreen; i++) {
                for (int j = 0; j < numOfTilesYperScreen; j++) {
                    if (imageHaveToExist(tileX + i, tileY + j)) {
                        imageName =  createImageName(i, j);
                        if (tar != null) {
//System.out.println("Add: " + (manager.mapPathPrefix + manager.mapPath + imageName) + "  " + tar.getTarFile() + "  " + tar.getFilePosition(imageName));
                            imageNames.addElement(new ImageRequest(
                                    manager.mapPathPrefix + manager.mapPath + imageName,
                                    tar.getTarFile(), tar.getFilePosition(imageName)));

                        } else {
                            imageNames.addElement(new ImageRequest(
                                    manager.mapPathPrefix + manager.mapPath + imageName));
                        }
                    }
                }
            }
            manager.getCache().newRequest(imageNames);

            for (int i = 0; i < numOfTilesXperScreen; i++) {
                for (int j = 0; j < numOfTilesYperScreen; j++) {
                    if (!imageHaveToExist(tileX + i, tileY + j)) {
                        image = MapScreen.getImageNotExisted(tileSizeX, tileSizeY);
                    } else {
                        imageName = createImageName(i, j);
                        image = manager.getCache().getImage(
                                manager.mapPathPrefix + manager.mapPath + imageName);
                    }
                    if (image != null) {
//                        x1 = Math.max(0, moveX + i * tileSizeX);
//                        y1 = Math.max(0, moveY + j * tileSizeY);
//                        x2 = Math.min(screenWidth, moveX + (i + 1) * tileSizeX);
//                        y2 = Math.min(screenHeight, moveY + (j + 1) * tileSizeY);
//                        if (x1 < screenWidth && y1 < screenHeight) {
//                            drawImageMulti(image, x1, x2, y1, y2, gr);
//                        }
                        x1 = moveX + i * tileSizeX;
                        y1 = moveY + j * tileSizeY;
                        //x2 = moveX + (i + 1) * tileSizeX;
                        //y2 = moveY + (j + 1) * tileSizeY;
                        if (x1 < screenWidth && y1 < screenHeight) {
                            System.out.println("DrawImage: x1 " + x1 + " y: " + y1);
                            gr.drawImage(image, x1, y1, Graphics.LEFT | Graphics.TOP);
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
        if (manager instanceof FileMapManagerTarLocify) {
            return createImageNameLocify(i, j);
        } else if (manager instanceof FileMapManagerTarTrekBuddy) {
            return createImageNameTrekBuddy(i, j);
        }
        return null;
    }

    private String createImageNameLocify(int i, int j) {
        return manager.mapImageDir + zoom + "_" + (tileX + i) + "_" + (tileY + j) + ".png";
    }

    private String createImageNameTrekBuddy(int i, int j) {
        return "set/" + manager.mapImageDir.substring(0, manager.mapImageDir.length() - 1)
                                        + "_" + ((tileX + i) * tileSizeX)
                                        + "_" + ((tileY + j) * tileSizeY) + ".png";
    }

    private boolean imageHaveToExist(int tileX, int tileY) {
//System.out.println("\ngetTile: tX: " + tileX + " tY: " + tileY);
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
   
//    private boolean drawImageMulti(Image image, int x1, int x2, int y1, int y2, Graphics gr) {
//        int width = x2 - x1;
//        int height = y2 - y1;
//        int to_x = 0, to_y = 0;
//
//        try {
//            if (width < tileSizeX) {
//                if (x1 == 0) {
//                    to_x = tileSizeX - width;
//                }
//            }
//
//            if (height < tileSizeY) {
//                if (y1 == 0) {
//                    to_y = tileSizeY - height;
//                }
//            }
//
//            gr.drawRegion(
//                    image,
//                    to_x, to_y, width, height,
//                    Sprite.TRANS_NONE,
//                    //target
//                    x1, y1, Graphics.LEFT | Graphics.TOP);
//
//System.out.println("FileTile.drawImage(): " +
//                    " image null: " + (image == null) +
//                    " orig img.width: " + image.getWidth() +
//                    " orig img.height: " + image.getHeight() +
//                    "  x1: " + x1 +
//                    "  y1: " + y1 +
//                    "  x2: " + x2 +
//                    "  y2: " + y2 +
//                    "  to_X: " + to_x +
//                    "  to_y: " + to_y);
//            return true;
//        } catch (Exception e) {
//            R.getErrorScreen().view(e, "FileTile.drawImage()", " draw:" +
//                    " image null: " + (image == null) +
//                    " orig img.width: " + image.getWidth() +
//                    " orig img.height: " + image.getHeight() +
//                    "  x1: " + x1 +
//                    "  y1: " + y1 +
//                    "  x2: " + x2 +
//                    "  y2: " + y2 +
//                    "  to_X: " + to_x +
//                    "  to_y: " + to_y);
//            return false;
//        }
//    }

    public String toString() {
        return "image: " + manager.mapPath + " center: " + mapViewPort.center.toString();
    }

    public double getLonDiffPerPixel() {
System.out.println("LonDiff: " + max_longitude + "  " + min_longitude + "  " + xmax);
        return Math.abs(max_longitude - min_longitude) / xmax;
    }

    public double getLatDiffPerPixel() {
System.out.println("LatDiff: " + max_latitude + "  " + min_latitude + "  " + ymax);
        return Math.abs(max_latitude - min_latitude) / ymax;
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
 * Transform coordinates on sphere (in m) to image pixels
 * @param x
 * @param y
 * @return
 */
//    private Point2D transformToImage(double x, double y) {
//        int pixX = (int) ((x - min_longitudepositionTopLeft.getX()) / measureWidth);
//        int pixY = (int) ((positionTopLeft.getY() - y) / measureHeight);
//        return new Point2D.Double(x, y);
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
