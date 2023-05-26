package utils;

import graph.Point;

/**
 * Class for useful computation methods
 */
public class Utils {

    /**
     * Computes the point to be created for sliding window management
     * 
     * @param p1        First point of the edge on which the point is
     * @param p2        Second point of the edge on which the point is
     * @param t1        Time at p1
     * @param t2        Time at p2
     * @param idAC      Id of the aircraft
     * @param deltaTime Date (time) to which the point has to be created
     * @return Point
     */
    public static Point getPointSW(Point p1, Point p2, double t1, double t2, int idAC, double deltaTime) {
        double lat1 = p1.getLat();
        double lat2 = p2.getLat();

        double lon1 = p1.getLon();
        double lon2 = p2.getLon();

        double m = (lat2 - lat1) / (lon2 - lon1);

        double b = lat2 - lon2 * m;

        double deltaLon = lon2 - lon1;

        double newLon = lon1 + deltaLon / 2.;
        double newLat = m * newLon + b;

        double t = t1 + (t2 - t1) / 2.;

        // Dichotomic search to find the point
        while (Math.abs(t - deltaTime) > 0.0001) {
            if (t > deltaTime) {
                lon2 = newLon;
                lat2 = newLat;
                t2 = t;
            } else {
                lon1 = newLon;
                lat1 = newLat;
                t1 = t;
            }
            m = (lat2 - lat1) / (lon2 - lon1);
            b = lat2 - lon2 * m;
            deltaLon = lon2 - lon1;
            newLon = lon1 + deltaLon / 2.;
            newLat = m * newLon + b;
            t = t1 + (t2 - t1) / 2.;
        }
        // Creates a new point with a specific name to recognize it
        return new Point(newLat, newLon, "" + idAC, p1.getSector());
    }
}
