/*
 * FileMapConfig.java
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

import com.locify.client.locator.Location4D;
import com.locify.client.maps.projection.NullProjection;
import com.locify.client.maps.projection.Projection;
import com.locify.client.maps.projection.ReferenceEllipsoid;
import com.locify.client.maps.projection.S42Projection;
import com.locify.client.maps.projection.UTMProjection;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.math.Matrix;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 *
 * @author menion
 */
public class FileMapConfig {

    /** name from file */
    private String name;
    /** description of file */
    private String description;
    /** size of tiles in X dimension */
    protected int tileSizeX;
    /** size of tiles in Y dimension */
    protected int tileSizeY;
    /** pixel size of whole image - X*/
    protected int xmax;
    /** pixel size of whole image - Y*/
    protected int ymax;
    /** zoom value in multiTile map */
    private int zoom;
    /** are coordinates flat or spheric */
    private boolean sphericCoordinates;
    /** type of projection */
    private Projection mapProjection;
    /** projction type for saving */
    private int mapProjectionType;
    /** main map viewPort */
    private FileMapViewPort mapViewPort;

    /** point that define raster */
    private Vector calibrationPoints;

    public FileMapConfig() {
        name = "";
        description = "";
        calibrationPoints = new Vector();
        mapProjectionType = Projection.PROJECTION_NULL;
    }

    private void setProjection(String projection) {
        if (projection.startsWith("WGS 84") || projection.startsWith("WGS84") ||
                projection.startsWith("UTM") || projection.equalsIgnoreCase("Mercator") ||
                projection.equalsIgnoreCase("Transverse Mercator")) {
            this.mapProjection = new UTMProjection(ReferenceEllipsoid.WGS84);
            this.mapProjectionType = Projection.PROJECTION_UTM;
        } else if (projection.startsWith("Pulkovo 1942") || projection.startsWith("S42") ||
                projection.startsWith("S-42")) {
            this.mapProjection = new S42Projection(ReferenceEllipsoid.KRASOVSKY);
            this.mapProjectionType = Projection.PROJECTION_S42;
        } else {
            this.mapProjection = new NullProjection();
            this.mapProjectionType = Projection.PROJECTION_NULL;
        }
    }

    private void setProjection(int projectionType) {
        this.mapProjectionType = projectionType;
        if (projectionType == Projection.PROJECTION_UTM) {
            this.mapProjection = new UTMProjection(ReferenceEllipsoid.WGS84);
        } else if (projectionType == Projection.PROJECTION_S42) {
            this.mapProjection = new S42Projection(ReferenceEllipsoid.KRASOVSKY);
        } else {
            this.mapProjection = new NullProjection();
        }
    }
    
    public Projection getMapProjection() {
        return mapProjection;
    }

    private boolean isSuccesfullyLoaded() {
        if (calibrationPoints.size() >= 3 && xmax > 0 && ymax > 0) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isReady() {
        if (xmax > 0 && ymax > 0 && tileSizeX != 0 && tileSizeY != 0 &&
                tileSizeX != Integer.MAX_VALUE && tileSizeY != Integer.MAX_VALUE &&
                mapProjection != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function parse Locify XML file into FileMapConfig file.
     * @param data Loaded XML config file.
     * @return Filled FileMapConfigFile;
     */
    public static FileMapConfig parseLocifyDescriptor(String data) {
        FileMapConfig fmc = new FileMapConfig();
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
                        fmc.name = parser.nextText();
                    } else if (tagName.equals("description")) {
                        fmc.description = parser.nextText();
                    } else if (tagName.equals("zoom")) {
                        fmc.zoom = Integer.parseInt(parser.nextText());
                    } else if (tagName.equals("projection")) {
                        fmc.setProjection(parser.nextText());
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
                            fmc.sphericCoordinates = false;
                        } else {
                            fmc.sphericCoordinates = true;
                        }
                        fmc.addCalibrationPoint(x, y, lat, lon);
                    } else if (tagName.equals("imageWidth")) {
                        fmc.xmax = Integer.parseInt(parser.nextText());
                    } else if (tagName.equals("imageHeight")) {
                        fmc.ymax = Integer.parseInt(parser.nextText());
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
            if (fmc.isSuccesfullyLoaded() && fmc.calculateViewPort()) {
                return fmc;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileMapConfig.parseLocifyDescriptor", data);
        } finally {
            try {
                stream.close();
                reader.close();
            } catch (IOException ex) {
            }
        }
        Logger.error("FileMapConfig.parseLocifyDescriptor() - problem while parsing");
        return null;
    }

    public static FileMapConfig parseDotMapDescriptor(String data) {
        try {
            FileMapConfig fmc = new FileMapConfig();
            Vector lines = StringTokenizer.getVector(data, "\n");
            if (lines.size() > 5) {
                // parsing map file
                Vector token;
                CalibrationPoint[] cal = null;
                for (int i = 0; i < lines.size(); i++) {
//Logger.debug("  FileMapConfig.parseDotDescriptor() line: " + String.valueOf(lines.elementAt(i)));
                    token = StringTokenizer.getVector(String.valueOf(lines.elementAt(i)), ",");
                    // Map Projection,Mercator
                    if (i == 1 && !((String) token.elementAt(0)).startsWith("Map Projection") &&
                            !((String) token.elementAt(0)).startsWith("Point")) {
                        fmc.name = (String) lines.elementAt(i);
                    } else if (i == 4 && !((String) token.elementAt(0)).startsWith("Point")) {
                        fmc.setProjection((String) token.elementAt(0));
                    } else if (((String) token.elementAt(0)).startsWith("Map Projection") &&
                            fmc.mapProjectionType == Projection.PROJECTION_NULL) {
                        fmc.setProjection(((String) token.elementAt(1)).trim());
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
                        if (lat != 0.0 && String.valueOf(token.elementAt(8)).equals("S"))
                            lat = -1 * lat;
                        lon = GpsUtils.parseDouble(token.elementAt(9)) + GpsUtils.parseDouble(token.elementAt(10)) / 60.0;
                        if (lon != 0.0 && String.valueOf(token.elementAt(11)).equals("W"))
                            lon = -1 * lon;
                        latX = GpsUtils.parseDouble(token.elementAt(15));
                        lonY = GpsUtils.parseDouble(token.elementAt(14));

                        if (lat != 0 && lon != 0) {
                            coo[0] = lat;
                            coo[1] = lon;
                            fmc.sphericCoordinates = true;
                        } else if (latX != 0 && lonY != 0) {
                            coo[0] = latX;
                            coo[1] = lonY;
                            fmc.sphericCoordinates = false;
                        } else
                            continue;

                        fmc.addCalibrationPoint(x, y, coo[0], coo[1]);
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
//Logger.debug("  FileMapConfig.parseDotDescriptor() " + token.elementAt(1) + " " + token.elementAt(2) + " " + token.elementAt(3));
                        fmc.xmax = GpsUtils.parseInt(token.elementAt(2));
                        fmc.ymax = GpsUtils.parseInt(token.elementAt(3));
                    }
                }

                if (fmc.calibrationPoints.size() < 2 && cal != null && cal.length > 3) {
                    fmc.calibrationPoints.removeAllElements();
                    fmc.sphericCoordinates = true;
                    for (int i = 0; i < cal.length; i++) {
                        fmc.addCalibrationPoint(cal[i].x, cal[i].y,
                                cal[i].position.getLatitude(), cal[i].position.getLongitude());
                    }
                    cal = null;
                } else if (fmc.calibrationPoints.size() == 2) {
                    CalibrationPoint tl = (CalibrationPoint) fmc.calibrationPoints.elementAt(0);
                    CalibrationPoint br = (CalibrationPoint) fmc.calibrationPoints.elementAt(1);
                    fmc.addCalibrationPoint(br.x, tl.y, tl.position.getLatitude(), br.position.getLongitude());
                    fmc.addCalibrationPoint(tl.x, br.y, br.position.getLatitude(), tl.position.getLongitude());
                }

                if (fmc.isSuccesfullyLoaded() && fmc.calculateViewPort()) {
                    return fmc;
                } else {
                    Logger.error("  FileMapConfig.parseDotMapDescriptor() - map initializing problem: calibrationPoints.size() - " + fmc.calibrationPoints.size() +
                            "  xmax - " + fmc.xmax +
                            "  ymax - " + fmc.ymax);
                }
            }
        } catch (Exception ex) {
            Logger.error("FileMapConfig.parseDotMapDescriptor(): " + data + " " + ex.toString());
        }
        Logger.error("FileMapConfig.parseDotMapDescriptor() - problem while parsing");
        return null;
    }

    protected void addCalibrationPoint(int x, int y, double lat, double lon) {
//Logger.log("  ConfigFileTile.addCalibrationPoint x: " + x + " y: " + y + " lat: " + lat + " lon: " + lon);
        CalibrationPoint calpoint = new CalibrationPoint();
        calpoint.x = x;
        calpoint.y = y;
        calpoint.position = new Location4D(lat, lon, 0);
        this.calibrationPoints.addElement(calpoint);
    }
    
    private boolean calculateViewPort() {
        try {
            if (calibrationPoints != null && calibrationPoints.size() > 2) {
                // compute transformation
                Matrix A = new Matrix(calibrationPoints.size() * 2, 6);
                Matrix X = new Matrix(calibrationPoints.size() * 2, 1);

                CalibrationPoint calP;
                for (int i = 0; i < calibrationPoints.size(); i++) {
                    calP = (CalibrationPoint) calibrationPoints.elementAt(i);
                    A.set(i, 0, 1);
                    A.set(i, 1, 0);
                    A.set(i, 2, calP.position.getLatitude());
                    A.set(i, 3, 0);
                    A.set(i, 4, 0);
                    A.set(i, 5, -1 * calP.position.getLongitude());

                    X.set(i, 0, calP.x);
                    X.set(i + calibrationPoints.size(), 0, calP.y);
                }
                for (int i = calibrationPoints.size(); i < (2 * calibrationPoints.size()); i++) {
                    calP = (CalibrationPoint) calibrationPoints.elementAt(i - calibrationPoints.size());
                    A.set(i, 0, 0);
                    A.set(i, 1, 1);
                    A.set(i, 2, 0);
                    A.set(i, 3, calP.position.getLatitude());
                    A.set(i, 4, calP.position.getLongitude());
                    A.set(i, 5, 0);
                }
                Matrix H = (A.transpose().times(A)).inverse().times(A.transpose().times(X));
//Logger.log(A.print(4));
//Logger.log(X.print(4));
//Logger.log(H.print(4));
                mapViewPort = new FileMapViewPort(H.get(0, 0), H.get(1, 0), H.get(3, 0), H.get(2, 0),
                        H.get(5, 0), H.get(4, 0), xmax, ymax);

                // compute inverse Helmert transformation
                A.fill(0);
                X.fill(0);

                for (int i = 0; i < calibrationPoints.size(); i++) {
                    calP = (CalibrationPoint) calibrationPoints.elementAt(i);
                    A.set(i, 0, 1);
                    A.set(i, 1, 0);
                    A.set(i, 2, calP.x);
                    A.set(i, 3, 0);
                    A.set(i, 4, 0);
                    A.set(i, 5, -1 * calP.y);

                    X.set(i, 0, calP.position.getLatitude());
                    X.set(i + calibrationPoints.size(), 0, calP.position.getLongitude());
                }
                for (int i = calibrationPoints.size(); i < (2 * calibrationPoints.size()); i++) {
                    calP = (CalibrationPoint) calibrationPoints.elementAt(i - calibrationPoints.size());
                    A.set(i, 0, 0);
                    A.set(i, 1, 1);
                    A.set(i, 2, 0);
                    A.set(i, 3, calP.x);
                    A.set(i, 4, calP.y);
                    A.set(i, 5, 0);
                }
                H = (A.transpose().times(A)).inverse().times(A.transpose().times(X));
//Logger.log(A.print(4));
//Logger.log(X.print(4));
//Logger.log(H.print(4));
                mapViewPort.setInverseTransformParametres(H.get(0, 0), H.get(1, 0),
                        H.get(3, 0), H.get(2, 0), H.get(5, 0), H.get(4, 0));

//Logger.log("trn: " + mapViewPort.convertGeoToMapPixel(new Location4D(50.3420, 13.5352, 0.0f)).toString());
//Logger.log("trn: " + mapViewPort.convertGeoToMapPixel(new Location4D(50.3420, 14.8584, 0.0f)).toString());
//Logger.log("trn: " + mapViewPort.convertGeoToMapPixel(new Location4D(49.4574, 13.5352, 0.0f)).toString());
//Logger.log("trn: " + mapViewPort.convertGeoToMapPixel(new Location4D(49.4574, 14.8584, 0.0f)).toString());
//Logger.log("Points: " + mapViewPort.getCalibrationCorner(1).toString());
//Logger.log("Points: " + mapViewPort.getCalibrationCorner(2).toString());
//Logger.log("Points: " + mapViewPort.getCalibrationCorner(3).toString());
//Logger.log("Points: " + mapViewPort.getCalibrationCorner(4).toString());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ConfigFileTile.calculateViewPort()", "");
            return false;
        }
    }

    public FileMapViewPort getMapViewPort() {
        return mapViewPort;
    }

    public boolean isSphericCoordinate() {
        return sphericCoordinates;
    }

    public double getLatDiffPerPixel() {
        return mapViewPort.getLatitudeDimension() / ymax;
    }

    public double getLonDiffPerPixel() {
        return mapViewPort.getLongitudeDimension() / xmax;
    }

    /**
     * Load config file from cached file insted of parsing config map file.
     */
    public static FileMapConfig loadConfigFile(DataInputStream dis) {
        FileMapConfig fmc = new FileMapConfig();
        try {
            fmc.name = dis.readUTF();
            fmc.description = dis.readUTF();
            fmc.tileSizeX = dis.readInt();
            fmc.tileSizeY = dis.readInt();
            fmc.xmax = dis.readInt();
            fmc.ymax = dis.readInt();
            fmc.zoom = dis.readInt();
            fmc.sphericCoordinates = dis.readBoolean();
            fmc.setProjection(dis.readInt());
            fmc.mapViewPort = new FileMapViewPort(dis.readDouble(), dis.readDouble(),
                    dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readDouble(),
                    fmc.xmax, fmc.ymax);
            fmc.mapViewPort.setInverseTransformParametres(dis.readDouble(), dis.readDouble(),
                    dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readDouble());
//Logger.debug("FileMapConfig.loadConfigFile: " + fmc.toString());
        } catch (IOException ex) {
            R.getErrorScreen().view(ex, "ConfigFileTile.loadConfigFile())", null);
            ex.printStackTrace();
        }
        return fmc;
    }

    public void saveConfigFile(DataOutputStream dos) {
        try {
//Logger.debug("FileMapConfig.saveConfigFile: " + toString() + " " + (dos == null));
            dos.writeUTF(name);
            dos.writeUTF(description);
            dos.writeInt(tileSizeX);
            dos.writeInt(tileSizeY);
            dos.writeInt(xmax);
            dos.writeInt(ymax);
            dos.writeInt(zoom);
            dos.writeBoolean(sphericCoordinates);
            dos.writeInt(mapProjectionType);

            mapViewPort.appendHelmertParametres(dos);
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "FileMapConfig.saveConfigFile())", null);
        }
    }

    public String toString() {
        String data = "";
        data += " Name: " + name;
        data += ", description: " + description;
        data += ", tileSizeX: " + tileSizeX;
        data += ", tileSizeY: " + tileSizeY;
        data += ", xmax: " + xmax;
        data += ", ymax: " + ymax;
        data += ", zoom: " + zoom;
        data += ", sphericCoordinates: " + sphericCoordinates;
        data += ", mapProjectionType: " + mapProjectionType;

        data += mapViewPort.toString();

        return data;
    }


    public int getMapZoom() {
        return zoom;
    }
}
