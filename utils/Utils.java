package utils;

import graph.Point;

public class Utils {
    public static Point getPointSW(Point p1, Point p2,double t1, double t2, int idAC, double deltaTime){
        double lat1 = p1.getLat();
        double lat2 = p2.getLat();

        double lon1 = p1.getLon();
        double lon2 = p2.getLon();

        double m = (lat2-lat1)/(lon2-lon1);

        double b = lat2-lon2*m;

        double deltaLon = lon2-lon1;
        //double deltaLat = lat2-lat1;


        double newLon = lon1+deltaLon/2.;
        double newLat = m*newLon+b;

        

        double t = t1+(t2-t1)/2.;

        while(Math.abs(t-deltaTime)>0.0001){
            if (t>deltaTime){
                lon2 = newLon;
                lat2 = newLat;
                t2 = t;
            }
            else{
                lon1 = newLon;
                lat1 = newLat;
                t1 = t;
            }
            m = (lat2-lat1)/(lon2-lon1);
            b = lat2-lon2*m;
            deltaLon = lon2-lon1;
            newLon = lon1+deltaLon/2.;
            newLat = m*newLon+b;
            t = t1+(t2-t1)/2.;
        }


        return new Point(newLat, newLon, ""+idAC,p1.getSector());
    }
}
