package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import graph.Point;
import graph.PointT;
import utils.Constants;

/**
 * Class for aircraft representation
 */
public class Aircraft {

    private int id;

    private ArrayList<PointT> path;

    private Point dep;
    private Point arr;

    private double time;
    private double deptime; // departure time
    private boolean finished; // has the aircraft finished its flight ?

    /**
     * Creates an aircraft object
     * 
     * @param dep     departure point of the aircraft
     * @param arr     arrival point of the aircraft
     * @param id      id of the aircraft
     * @param deptime departure time of the aircraft
     */
    public Aircraft(Point dep, Point arr, int id, double deptime) {
        this.dep = dep;
        this.arr = arr;
        this.time = deptime;
        this.id = id;
        this.path = new ArrayList<>();
        this.finished = false;
        this.deptime = time;
    }

    /**
     * Has the aircraft reached its final point ?
     * 
     * @return boolean
     */
    public boolean isDone() {
        return this.finished;
    }

    /*
     * Set to true the value finished : the flight is finished.
     */
    public void finish() {
        this.finished = true;
    }

    /**
     * Returns the departure time of the aircraft
     * 
     * @return double
     */
    public double getDepTime() {
        return this.deptime;
    }

    /**
     * Add a point p at the time t to the path of the aircraft
     * 
     * @param p
     * @param t
     */
    public void addPointToPath(Point p, double t) {
        this.path.add(new PointT(p, t));
    }

    /**
     * Prints the path of the aircraft
     */
    public void printPath() {
        for (PointT p : this.path) {
            System.out.println(p);
        }

    }

    /**
     * Updates the departure time of the aircraft and its departure point (for
     * instance at the end of a time window)
     * 
     * @param newTime
     * @param newDep
     */
    public void updateDep(double newTime, Point newDep) {
        this.dep = newDep;
        this.time = newTime;
    }

    /**
     * Returns the departure time of the aircraft
     * 
     * @return double
     */
    public double getTime() {
        return this.time;
    }

    /**
     * Returns the departure point of the aircraft
     * 
     * @return Point
     */
    public Point getDep() {
        return this.dep;
    }

    /**
     * Returns the destination of the aircraft
     * 
     * @return Point
     */
    public Point getEnd() {
        return this.arr;
    }

    /**
     * Returns the id of the aircraft
     * 
     * @return int
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns the path of the aircraft
     * 
     * @return ArrayList<PointT>
     */
    public ArrayList<PointT> getPath() {
        return this.path;
    }

    /**
     * Prints the path of several aircraft in a file defined in utils.Constants
     * 
     * @param aircrafts
     * @throws IOException
     */
    public static void printPath(ArrayList<Aircraft> aircrafts) throws IOException {
        FileWriter writer = new FileWriter(Constants.PATH_FILE);
        BufferedWriter buffer = new BufferedWriter(writer);
        for (Aircraft ac : aircrafts) {
            int i = ac.getId();
            for (PointT point : ac.getPath()) {
                buffer.write("" + i + " " + point.getPoint().getId() + " " + point.getPoint().getLon() + " "
                        + point.getPoint().getLat() + " " + point.getT() + "\n");
            }
        }

        buffer.close();
    }

    /**
     * Filter the path from artificial nodes for time window management
     */
    public void filterPath() {
        ArrayList<PointT> newPath = new ArrayList<>();
        for (PointT p : this.path) {
            try {
                Integer.valueOf(p.getPoint().getId());
            } catch (NumberFormatException e) { // Keep points only if they don't have a special id
                newPath.add(p);
            }
        }
        this.path = newPath;
    }

}