package graph;

public class PointT {
    private Point point;
    private double t;

    /**
     * Creates an object with a point associated to a time t
     * 
     * @param p
     * @param t
     */
    public PointT(Point p, double t) {
        this.point = p;
        this.t = t;
    }

    /**
     * @return double
     */
    public double getT() {
        return this.t;
    }

    /**
     * @return Point
     */
    public Point getPoint() {
        return this.point;
    }

    /**
     * @return String
     */
    @Override
    public String toString() {
        return "[ " + this.t + " : " + this.point.getId() + " ]";
    }

}
