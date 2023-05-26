package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import utils.Constants;

public class Graph {

    private static int NSECTORS = 37;

    private HashMap<Point, ArrayList<Edge>> adj;
    private HashMap<Point, ArrayList<Point>> adjInv;
    private Sectorization secto;

    /**
     * Build the initial graph
     * 
     * @param points List of the points of the graph
     */
    public Graph(ArrayList<Point> points) {

        this.adj = new HashMap<>();
        this.adjInv = new HashMap<>();

        this.secto = new Sectorization(NSECTORS); // Initialize the sectorization

        for (Point p : points) {
            this.secto.addPoint(p, p.getSector()); // add all points to their sector
        }

        for (Point p0 : points) {
            ArrayList<Edge> nodes = new ArrayList<>();
            for (Point p1 : points) {

                double d = p0.distance(p1);
                if (d < Constants.D_MAX && d > 5.) { // Compute the adjacency matrix according to the chosen maximum
                                                     // distance between two negihbors
                    nodes.add(new Edge(p0, p1));
                }

            }
            this.adj.put(p0, nodes);
        }

        for (Point p : points) {
            ArrayList<Point> froms = new ArrayList<>();
            for (Point p1 : points) {
                if (this.adj.get(p1).contains(p)) {
                    froms.add(p1); // Compute the inverse of the adjacency matrix
                }
            }
            this.adjInv.put(p, froms);
        }
    }

    /**
     * Return the adjacency matrix
     * 
     * @return HashMap<Point, ArrayList<Edge>>
     */
    public HashMap<Point, ArrayList<Edge>> getAdj() {
        return adj;
    }

    /**
     * Return the inverse of the adjacency matrix
     * 
     * @return HashMap<Point, ArrayList<Point>>
     */
    public HashMap<Point, ArrayList<Point>> getAdjInv() {
        return adjInv;
    }

    /**
     * Return the set of points of the graph
     * 
     * @return Set<Point>
     */
    public Set<Point> getPoints() {
        return adj.keySet();
    }

    /**
     * Return the sectorization associated to the graph
     * 
     * @return Sectorization
     */
    public Sectorization getSecto() {
        return this.secto;
    }

    /**
     * Add a point in the graph (typically at the end of a time window)
     * 
     * @param p  Point to be added
     * @param p1 First point of the edge on which it is added
     * @param p2 Second point of the edge on which it is added
     */
    public void addPoint(Point p, Point p1, Point p2) {
        Edge newEdge = new Edge(p, p2);
        ArrayList<Edge> nodes = new ArrayList<>();
        nodes.add(newEdge);
        this.adj.put(p, nodes);
        this.secto.addPoint(p, p.getSector());
    }

}
