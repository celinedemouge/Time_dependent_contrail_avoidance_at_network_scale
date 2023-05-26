package graph;

import java.util.ArrayList;

public class Sector {

    private ArrayList<Point> points;

    /**
     * Constructs a sector
     * 
     * @param points
     */
    public Sector(ArrayList<Point> points) {
        this.points = points;
    }

    /**
     * Constructs an empty sector
     */
    public Sector() {
        this.points = new ArrayList<>();
    }

    /**
     * Adds a point in the sector
     * 
     * @param p
     */
    public void addPoint(Point p) {
        this.points.add(p);
    }

    /**
     * Returns the points in the sector
     * 
     * @return ArrayList<Point>
     */
    public ArrayList<Point> getPoints() {
        return points;
    }

    /**
     * Checks if a point is in the sector
     * 
     * @param p
     * @return boolean
     */
    public boolean isInSector(Point p) {
        return this.points.contains(p);
    }

}
