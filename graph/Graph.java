package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import utils.Constants;

public class Graph {

    private HashMap<Point, ArrayList<Edge>> adj;
    private HashMap<Point, ArrayList<Point>> adjInv;
    private Sectorization secto;

    public Graph(ArrayList<Point> points) {

        this.adj = new HashMap<>();
        this.adjInv = new HashMap<>();

        this.secto = new Sectorization(37);

        for (Point p : points) {
            this.secto.addPoint(p, p.getSector());
        }


        for (Point p0 : points) {
            ArrayList<Edge> nodes = new ArrayList<>();
            for (Point p1 : points) {
                
                    double d = p0.distance(p1);
                if (d < Constants.D_MAX && d>5.) {
                    nodes.add(new Edge(p0, p1));
                }
                
                
            }
            this.adj.put(p0, nodes);
        }

        for (Point p : points) {
            ArrayList<Point> froms = new ArrayList<>();
            for (Point p1 : points) {
                if (this.adj.get(p1).contains(p)) {
                    froms.add(p1);
                }
            }
            this.adjInv.put(p, froms);
        }
    }

    public HashMap<Point, ArrayList<Edge>> getAdj() {
        return adj;
    }

    public HashMap<Point, ArrayList<Point>> getAdjInv() {
        return adjInv;
    }

    public Set<Point> getPoints() {
        return adj.keySet();
    }

    public Sectorization getSecto() {
        return this.secto;
    }

    public void addPoint(Point p, Point p1, Point p2){
        Edge newEdge = new Edge(p, p2);
        ArrayList<Edge> nodes = new ArrayList<>();
        nodes.add(newEdge);
        this.adj.put(p,nodes);
        this.secto.addPoint(p, p.getSector());
    }
    
}
