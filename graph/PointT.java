package graph;

public class PointT {
    private Point point; 
    private double t; 

    public PointT(Point p, double t){
        this.point = p;
        this.t = t;
    }

    public double getT(){
        return this.t;
    }

    public Point getPoint(){
        return this.point;
    }

    @Override
    public String toString() {
        return "[ "+this.t+" : "+this.point.getId()+" ]";
    }

}
