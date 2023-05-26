package graph;

import java.util.Objects;

import utils.Constants;

public class Point {

    private double lat;
    private double lon;
    private String id;
    private int sector;

    /**
     * Constructs a point from its latitude, longitude, id and sector
     * 
     * @param lat
     * @param lon
     * @param id
     * @param sector
     */
    public Point(double lat, double lon, String id, int sector) {
        this.lat = lat;
        this.lon = lon;
        this.id = id;
        this.sector = sector;
    }

    /**
     * Returns the latitude of the point
     * 
     * @return double
     */
    public double getLat() {
        return lat;
    }

    /**
     * Returns the longitude of the point
     * 
     * @return double
     */
    public double getLon() {
        return lon;
    }

    /**
     * Returns the id of the point
     * 
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the sector number
     * 
     * @return int
     */
    public int getSector() {
        return sector;
    }

    /**
     * @return String
     */
    @Override
    public String toString() {
        return "Point [id=" + id + ", lat=" + lat + ", lon=" + lon + ", sector=" + sector + "]";
    }

    /**
     * @param o
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Point point = (Point) o;
        return Double.compare(point.lat, lat) == 0 && Double.compare(point.lon, lon) == 0
                && Objects.equals(id, point.id);
    }

    /**
     * Computes the great circle distance between two points
     * 
     * @param other
     * @return double
     */
    public double distance(Point other) {
        double lat0Rad = Constants.TO_RADIANS * this.lat;
        double lon0rad = Constants.TO_RADIANS * this.lon;
        double lat1Rad = Constants.TO_RADIANS * other.getLat();
        double lon1rad = Constants.TO_RADIANS * other.getLon();

        double sin0 = Math.sin(lat0Rad);
        double cos0 = Math.cos(lat0Rad);
        double sin1 = Math.sin(lat1Rad);
        double cos1 = Math.cos(lat1Rad);

        double d = 60. * Constants.TO_DEGREES * Math.acos(sin0 * sin1 + cos0 * cos1 * Math.cos(lon1rad - lon0rad));

        return d;
    }

    /**
     * Computes the flight time between two points (considering wind)
     * 
     * @param other
     * @return double
     */
    public double time(Point other) {
        double lat0Rad = Constants.TO_RADIANS * this.lat;
        double lon0rad = Constants.TO_RADIANS * this.lon;
        double lat1Rad = Constants.TO_RADIANS * other.getLat();
        double lon1rad = Constants.TO_RADIANS * other.getLon();

        double sin0 = Math.sin(lat0Rad);
        double cos0 = Math.cos(lat0Rad);
        double sin1 = Math.sin(lat1Rad);
        double cos1 = Math.cos(lat1Rad);

        // great circle distance computation
        double d = 60. * Constants.TO_DEGREES * Math.acos(sin0 * sin1 + cos0 * cos1 * Math.cos(lon1rad - lon0rad));

        // wind data computation
        double wu0 = Constants.WD.interpolateWu(this.lon, this.lat);
        double wu1 = Constants.WD.interpolateWu(other.getLon(), other.getLat());
        double wv0 = Constants.WD.interpolateWv(this.lon, this.lat);
        double wv1 = Constants.WD.interpolateWv(other.getLon(), other.getLat());

        double wu = (wu0 + wu1) / 2.;
        double wv = (wv0 + wv1) / 2.;

        double dy = (this.lat - other.getLat());
        double dx = (this.lon - other.getLon());
        double dC = Math.sqrt(dx * dx + dy * dy);

        double vx = (-Constants.V / dC) * dx + wu;
        double vy = (-Constants.V / dC) * dy + wv;

        // ground speed computation
        double v = Math.sqrt(vx * vx + vy * vy);

        if (Double.isNaN(v)) {
            return 0.;
        }

        return d / v;
    }

    /**
     * Computes the cost of flying from a point to another (considering contrails
     * and defined GWP)
     * 
     * @param other
     * @return double
     */
    public double weight(Point other) {
        double t = time(other);
        double pourcent = inContrails(this, other);

        return t * (1. + pourcent * Constants.GWP);

    }

    /**
     * Useful for debug
     * 
     * @param other
     * @param fake
     * @return double
     */
    public double time(Point other, boolean fake) {
        double lat0Rad = Constants.TO_RADIANS * this.lat;
        double lon0rad = Constants.TO_RADIANS * this.lon;
        double lat1Rad = Constants.TO_RADIANS * other.getLat();
        double lon1rad = Constants.TO_RADIANS * other.getLon();

        double sin0 = Math.sin(lat0Rad);
        double cos0 = Math.cos(lat0Rad);
        double sin1 = Math.sin(lat1Rad);
        double cos1 = Math.cos(lat1Rad);

        double d = 60. * Constants.TO_DEGREES * Math.acos(sin0 * sin1 + cos0 * cos1 * Math.cos(lon1rad - lon0rad));
        double v;
        if (!fake) {
            double wu0 = Constants.WD.interpolateWu(this.lon, this.lat);
            double wu1 = Constants.WD.interpolateWu(other.getLon(), other.getLat());
            double wv0 = Constants.WD.interpolateWv(this.lon, this.lat);
            double wv1 = Constants.WD.interpolateWv(other.getLon(), other.getLat());

            double wu = (wu0 + wu1) / 2.;
            double wv = (wv0 + wv1) / 2.;

            double dy = (this.lat - other.getLat());
            double dx = (this.lon - other.getLon());
            double dC = Math.sqrt(dx * dx + dy * dy);

            double vx = (-Constants.V / dC) * dx + wu;
            double vy = (-Constants.V / dC) * dy + wv;
            v = Math.sqrt(vx * vx + vy * vy);
        } else {
            v = Constants.V;
        }

        if (Double.isNaN(v)) {
            return 0.;
        }

        return d / v;

    }

    /**
     * Is the arc from p1 to p2 in contrail area ? In which pourcentage ?
     * 
     * @param p1
     * @param p2
     * @return double
     */
    public static double inContrails(Point p1, Point p2) {
        /*
         * if (Constants.CD.isInContrailP(p1.getLon(), p1.getLat())) {
         * return true;
         * } else if (Constants.CD.isInContrailP(p2.getLon(), p2.getLat())) {
         * return true;
         * }
         */
        return segmentInContrails(p1, p2);

    }

    // Compute the pourcentage of the arc in persitent contrail area
    private static double segmentInContrails(Point p1, Point p2) {
        if (p1.equals(p2)) {
            return 0.;
        }
        double m = (p2.getLat() - p1.getLat()) / (p2.getLon() - p1.getLon());
        double b = p1.getLat() - m * p1.getLon();

        double pourcent = 0.;
        double delta = (p2.getLon() - p1.getLon()) / 10.;
        for (int i = 0; i < 10; i++) {
            double lon = p1.getLon() + i * delta;
            double lat = m * lon + b;
            if (Constants.CD.isInContrailP(lon, lat)) {
                pourcent += 0.1;
            }
        }

        return pourcent;

    }

}
