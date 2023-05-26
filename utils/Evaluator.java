package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import graph.PointT;
import model.Aircraft;

public class Evaluator {

    public static void evaluate(ArrayList<Aircraft> aircrafts,String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write("id,d,t\n");

        

        for (Aircraft aircraft : aircrafts){
            aircraft.filterPath();
            ArrayList<PointT> path = aircraft.getPath();

            double d = 0.;
            double t = 0.;

            for (int i=1; i<path.size(); i++){
                double di = path.get(i).getPoint().distance(path.get(i-1).getPoint());
                if (Double.isNaN(di)){
                    di=0.;
                }
                d+= di;

                double ti = path.get(i-1).getPoint().time(path.get(i).getPoint(),true);
                if (Double.isNaN(ti)){
                    ti=0.;
                }
                t+= ti;
            }

            String toWrite = ""+aircraft.getId()+","+d+","+t+"\n";
            fw.write(toWrite);
        }

        fw.close();
    }

    public static void evaluateTimeFlown(ArrayList<Aircraft> aircrafts){
        double tt =0.;
        for (Aircraft aircraft : aircrafts){
            ArrayList<PointT> path = aircraft.getPath();

            double t = 0.;

            for (int i=1; i<path.size(); i++){
                double ti = path.get(i-1).getPoint().time(path.get(i).getPoint(),true);
                if (Double.isNaN(ti)){
                    ti=0.;
                }
                t+= ti;
            }

            tt+=t;
        }
        System.out.println(tt);
    }

    public static void evaluateTotalCost(ArrayList<Aircraft> aircrafts){
        double c= 0.;
        for (Aircraft aircraft : aircrafts){
            ArrayList<PointT> path = aircraft.getPath();
            double t = 0.;

            for (int i=1; i<path.size(); i++){
                double ti = path.get(i-1).getPoint().weight(path.get(i).getPoint());
                if (Double.isNaN(ti)){
                    ti=0.;
                }
                t+= ti;
            }

            c+=t;
        }
        System.out.println(c);
    }
    
}
