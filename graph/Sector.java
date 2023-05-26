package graph;

import java.util.ArrayList;

public class Sector {

    private ArrayList<Point> points;

    public Sector(ArrayList<Point> points){
        this.points = points; 
    }

    public Sector(){
        this.points = new ArrayList<>();
    }

    public void addPoint(Point p){
        this.points.add(p);
    }

    public ArrayList<Point> getPoints(){
        return points;
    }

    public boolean isInSector(Point p){
        return this.points.contains(p);
    }
    
}
