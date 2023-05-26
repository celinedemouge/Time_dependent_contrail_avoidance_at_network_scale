package graph;

public class Edge {

    private Point p; 
    private double weight;
    private double distance;
    private double time;

    public Edge(Point p1, Point p2){
        this.p = p2;
        this.weight = p1.weight(p2);
        this.distance = p1.distance(p2);
        this.time = p1.time(p2);
    }

    public Point getP(){
        return this.p;
    }
    
    public double getWeight(){
        return this.weight;
    }

    public double getTime(){
        return this.time;
    }

    public double getDistance(){
        return this.distance;
    }
}
