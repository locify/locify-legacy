/*
 * Projection.java
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
package com.locify.client.maps.projection;

/**
 * Abstract map projection
 * @author MenKat
 */
public abstract class Projection {

    public static final int PROJECTION_NULL = -1;
    public static final int PROJECTION_UTM = 1;
    public static final int PROJECTION_S42 = 2;
    public static final int PROJECTION_SPHERICAL_MERCATOR = 3;

    /** WGS84 radius in metres*/
    public static final double WGS84_RADIUS = 6378137.0;
    /** WGS84 perimetr in metres*/
    public static final double WGS84_EARTH_PERIMETER = 2 * Math.PI * WGS84_RADIUS;
    /** WGS84 flattering*/
    public static final double WGS84_FLATTENING = 1.0 / 298.2572235630;
    /** angle reduction between ferro and greenwich */
    public static final double SJTSK_FERRO_REDUCTION = 17 + 39 / 60 + 46 / 3600;
    
    protected ReferenceEllipsoid ellipsoid;

    public Projection(ReferenceEllipsoid ellipsoid) {
        this.ellipsoid = ellipsoid;
    }

    public abstract double[] projectionToFlat(double lat, double lon);

    /**
     * Convert coordinates from X, Y in projection space to lat, lon on sphere.
     *
     * @param X coordinate in flat.
     * @param Y coordinate in flat.
     * @return Array of Latitude (double[0]), Longitude (double[1]) on sphere (in degreee).
     */
    public abstract double[] projectionToSphere(double X, double Y);

    /* converts lat/long to UTM coords.  Equations from USGS Bulletin 1532
    East Longitudes are positive, West longitudes are negative.
    North latitudes are positive, South latitudes are negative
    Lat and Long are in decimal degrees
    Written by Chuck Gantz- chuck.gantz@globalstar.com */
//    public static double[] LatLonToUTM(ReferenceEllipsoid refEllipsoid, double Lat, double Long) { //, char UTMZone) {
//        double UTMNorthing;
//        double UTMEasting;
//
//        double deg2rad = Math.PI / 180;
//        //double rad2deg = 180.0 / Math.PI;
//
//        double a = refEllipsoid.a;
//        double eccSquared = refEllipsoid.getEccentricitySquared();
//        double k0 = 0.9996;
//
//        double LongOrigin;
//        double eccPrimeSquared;
//        double N, T, C, A, M;
//
//        //Make sure the longitude is between -180.00 .. 179.9
//        double LongTemp = (Long + 180) - (int) ((Long + 180) / 360) * 360 - 180; // -180.00 .. 179.9;
//
//        double LatRad = Lat * deg2rad;
//        double LongRad = LongTemp * deg2rad;
//        double LongOriginRad;
//        int ZoneNumber;
//
//        ZoneNumber = (int) ((LongTemp + 180) / 6) + 1;
//
//        if (Lat >= 56.0 && Lat < 64.0 && LongTemp >= 3.0 && LongTemp < 12.0) {
//            ZoneNumber = 32;
//        }
//
//        // Special zones for Svalbard
//        if (Lat >= 72.0 && Lat < 84.0) {
//            if (LongTemp >= 0.0 && LongTemp < 9.0) {
//                ZoneNumber = 31;
//            } else if (LongTemp >= 9.0 && LongTemp < 21.0) {
//                ZoneNumber = 33;
//            } else if (LongTemp >= 21.0 && LongTemp < 33.0) {
//                ZoneNumber = 35;
//            } else if (LongTemp >= 33.0 && LongTemp < 42.0) {
//                ZoneNumber = 37;
//            }
//        }
//        LongOrigin = (ZoneNumber - 1) * 6 - 180 + 3;  //+3 puts origin in middle of zone
//        LongOriginRad = LongOrigin * deg2rad;
//
//        //compute the UTM Zone from the latitude and longitude
//        //sprintf(UTMZone, "%d%c", ZoneNumber, UTMLetterDesignator(Lat));
//
//        eccPrimeSquared = (eccSquared) / (1 - eccSquared);
//
//        N = a / Math.sqrt(1 - eccSquared * Math.sin(LatRad) * Math.sin(LatRad));
//        T = Math.tan(LatRad) * Math.tan(LatRad);
//        C = eccPrimeSquared * Math.cos(LatRad) * Math.cos(LatRad);
//        A = Math.cos(LatRad) * (LongRad - LongOriginRad);
//
//        M = a * ((1 - eccSquared / 4 - 3 * eccSquared * eccSquared / 64 - 5 * eccSquared * eccSquared * eccSquared / 256) * LatRad - (3 * eccSquared / 8 + 3 * eccSquared * eccSquared / 32 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(2 * LatRad) + (15 * eccSquared * eccSquared / 256 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(4 * LatRad) - (35 * eccSquared * eccSquared * eccSquared / 3072) * Math.sin(6 * LatRad));
//
//        UTMEasting = (double) (k0 * N * (A + (1 - T + C) * A * A * A / 6 + (5 - 18 * T + T * T + 72 * C - 58 * eccPrimeSquared) * A * A * A * A * A / 120) + 500000.0);
//
//        UTMNorthing = (double) (k0 * (M + N * Math.tan(LatRad) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24 + (61 - 58 * T + T * T + 600 * C - 330 * eccPrimeSquared) * A * A * A * A * A * A / 720)));
//        if (Lat < 0) {
//            UTMNorthing += 10000000.0; //10000000 meter offset for southern hemisphere
//        }
//
//        double[] res = new double[2];
//        res[0] = UTMNorthing;
//        res[1] = UTMEasting;
//        return res;
//    }

//    char UTMLetterDesignator(double Lat)
//    {
//    //This routine determines the correct UTM letter designator for the given latitude
//    //returns 'Z' if latitude is outside the UTM limits of 84N to 80S
//        //Written by Chuck Gantz- chuck.gantz@globalstar.com
//        char LetterDesignator;
//
//        if((84 >= Lat) && (Lat >= 72)) LetterDesignator = 'X';
//        else if((72 > Lat) && (Lat >= 64)) LetterDesignator = 'W';
//        else if((64 > Lat) && (Lat >= 56)) LetterDesignator = 'V';
//        else if((56 > Lat) && (Lat >= 48)) LetterDesignator = 'U';
//        else if((48 > Lat) && (Lat >= 40)) LetterDesignator = 'T';
//        else if((40 > Lat) && (Lat >= 32)) LetterDesignator = 'S';
//        else if((32 > Lat) && (Lat >= 24)) LetterDesignator = 'R';
//        else if((24 > Lat) && (Lat >= 16)) LetterDesignator = 'Q';
//        else if((16 > Lat) && (Lat >= 8)) LetterDesignator = 'P';
//        else if(( 8 > Lat) && (Lat >= 0)) LetterDesignator = 'N';
//        else if(( 0 > Lat) && (Lat >= -8)) LetterDesignator = 'M';
//        else if((-8> Lat) && (Lat >= -16)) LetterDesignator = 'L';
//        else if((-16 > Lat) && (Lat >= -24)) LetterDesignator = 'K';
//        else if((-24 > Lat) && (Lat >= -32)) LetterDesignator = 'J';
//        else if((-32 > Lat) && (Lat >= -40)) LetterDesignator = 'H';
//        else if((-40 > Lat) && (Lat >= -48)) LetterDesignator = 'G';
//        else if((-48 > Lat) && (Lat >= -56)) LetterDesignator = 'F';
//        else if((-56 > Lat) && (Lat >= -64)) LetterDesignator = 'E';
//        else if((-64 > Lat) && (Lat >= -72)) LetterDesignator = 'D';
//        else if((-72 > Lat) && (Lat >= -80)) LetterDesignator = 'C';
//        else LetterDesignator = 'Z'; //This is here as an error flag to show that the Latitude is outside the UTM limits
//
//        return LetterDesignator;
//    }

    /** Converts UTM coords to lat/long.  Equations from USGS Bulletin 1532
     * East Longitudes are positive, West longitudes are negative.
     * North latitudes are positive, South latitudes are negative
     * Lat and Long are in decimal degrees.
     * Written by Chuck Gantz- chuck.gantz@globalstar.com */
//    public void UTMtoLL(int ReferenceEllipsoid, const double UTMNorthing, const double UTMEasting, const char* UTMZone,
//                  double& Lat,  double& Long )
//    {
//
//
//        double k0 = 0.9996;
//        double a = ellipsoid[ReferenceEllipsoid].EquatorialRadius;
//        double eccSquared = ellipsoid[ReferenceEllipsoid].eccentricitySquared;
//        double eccPrimeSquared;
//        double e1 = (1-sqrt(1-eccSquared))/(1+sqrt(1-eccSquared));
//        double N1, T1, C1, R1, D, M;
//        double LongOrigin;
//        double mu, phi1, phi1Rad;
//        double x, y;
//        int ZoneNumber;
//        char* ZoneLetter;
//        int NorthernHemisphere; //1 for northern hemispher, 0 for southern
//
//        x = UTMEasting - 500000.0; //remove 500,000 meter offset for longitude
//        y = UTMNorthing;
//
//        ZoneNumber = strtoul(UTMZone, &ZoneLetter, 10);
//        if((*ZoneLetter - 'N') >= 0)
//            NorthernHemisphere = 1;//point is in northern hemisphere
//        else
//        {
//            NorthernHemisphere = 0;//point is in southern hemisphere
//            y -= 10000000.0;//remove 10,000,000 meter offset used for southern hemisphere
//        }
//
//        LongOrigin = (ZoneNumber - 1)*6 - 180 + 3;  //+3 puts origin in middle of zone
//
//        eccPrimeSquared = (eccSquared)/(1-eccSquared);
//
//        M = y / k0;
//        mu = M/(a*(1-eccSquared/4-3*eccSquared*eccSquared/64-5*eccSquared*eccSquared*eccSquared/256));
//
//        phi1Rad = mu	+ (3*e1/2-27*e1*e1*e1/32)*sin(2*mu)
//                    + (21*e1*e1/16-55*e1*e1*e1*e1/32)*sin(4*mu)
//                    +(151*e1*e1*e1/96)*sin(6*mu);
//        phi1 = phi1Rad*rad2deg;
//
//        N1 = a/sqrt(1-eccSquared*sin(phi1Rad)*sin(phi1Rad));
//        T1 = tan(phi1Rad)*tan(phi1Rad);
//        C1 = eccPrimeSquared*cos(phi1Rad)*cos(phi1Rad);
//        R1 = a*(1-eccSquared)/pow(1-eccSquared*sin(phi1Rad)*sin(phi1Rad), 1.5);
//        D = x/(N1*k0);
//
//        Lat = phi1Rad - (N1*tan(phi1Rad)/R1)*(D*D/2-(5+3*T1+10*C1-4*C1*C1-9*eccPrimeSquared)*D*D*D*D/24
//                        +(61+90*T1+298*C1+45*T1*T1-252*eccPrimeSquared-3*C1*C1)*D*D*D*D*D*D/720);
//        Lat = Lat * rad2deg;
//
//        Long = (D-(1+2*T1+C1)*D*D*D/6+(5-2*C1+28*T1-3*C1*C1+8*eccPrimeSquared+24*T1*T1)
//                        *D*D*D*D*D/120)/cos(phi1Rad);
//        Long = LongOrigin + Long * rad2deg;
//
//    }
}
