package graph;

public class Edge {

    private Point p;
    private double weight;
    private double distance;
    private double time;

    /**
     * Creates the object, compute the flight time, the distance and the cost of the
     * edge (considering contrails and flight time)
     * 
     * @param p1 First point of the edge
     * @param p2 Second point of the edge
     */
    public Edge(Point p1, Point p2) {
        this.p = p2;
        this.weight = p1.weight(p2);
        this.distance = p1.distance(p2);
        this.time = p1.time(p2);
    }

    /**
     * Returns the second point of the edge
     * 
     * @return Point
     */
    public Point getP() {
        return this.p;
    }

    /**
     * Returns the cost of the edge (considering contrails and flight time)
     * 
     * @return double
     */
    public double getWeight() {
        return this.weight;
    }

    /**
     * Returns the flight time
     * 
     * @return double
     */
    public double getTime() {
        return this.time;
    }

    /**
     * Returns the distance of the edge
     * 
     * @return double
     */
    public double getDistance() {
        return this.distance;
    }

}
