package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import graph.Point;
import graph.PointT;
import utils.Constants;

public class Aircraft {

    private int id;

    private ArrayList<PointT> path;

    private Point dep;
    private Point arr;

    private double time;
    private double deptime;
    private boolean finished; 

    public Aircraft(Point dep, Point arr, int id,double deptime) {
        this.dep = dep;
        this.arr = arr;
        this.time = deptime;
        this.id = id;
        this.path = new ArrayList<>();
        this.finished = false;
        this.deptime = time;
    }

    public boolean isDone(){
        return this.finished;
    }

    public void finish(){
        this.finished = true; 
    }

    public double getDepTime(){
        return this.deptime;
    }



    public void addPointToPath(Point p,double t){
        this.path.add(new PointT(p, t));
    }


    public void printPath(){
        String toPrint = "";
        for (PointT p: this.path){
            System.out.println(p);
        }
        
    }

    public void updateDep(double newTime, Point newDep) {
        this.dep = newDep;
        this.time = newTime;
    }

    public double getTime() {
        return this.time;
    }

    public Point getDep() {
        return this.dep;
    }

    public Point getEnd() {
        return this.arr;
    }

    public int getId() {
        return this.id;
    }

    public ArrayList<PointT> getPath(){
        return this.path;
    }

    public static void printPath(ArrayList<Aircraft> aircrafts) throws IOException{
        FileWriter writer = new FileWriter(Constants.PATH_FILE);
        BufferedWriter buffer = new BufferedWriter(writer);
        for (Aircraft ac : aircrafts){
            int i = ac.getId();
            for (PointT point : ac.getPath()){
                buffer.write(""+i+" "+point.getPoint().getId()+" "+point.getPoint().getLon()+" "+point.getPoint().getLat()+" "+point.getT()+"\n");
            }
        }
        
        buffer.close();
    }

    public void filterPath(){
        ArrayList<PointT> newPath = new ArrayList<>();
        for (PointT p : this.path){
           try {
            Integer.valueOf(p.getPoint().getId());
           }
           catch (NumberFormatException e){
            newPath.add(p);
           }
        }
        this.path = newPath;
    }


}