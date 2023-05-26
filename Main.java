import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import graph.DataExtractor;
import graph.Graph;
import graph.Point;
import ilog.concert.IloException;
import model.Aircraft;
import model.Model;
import utils.Constants;
import utils.Evaluator;

public class Main {

    public static void main(String[] args) {
        //System.out.println(args[0]);
        try {
            loop(14);
        } catch (IloException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void loop(int capa) throws IloException, IOException {
        System.out.println("______________CAPA________________");
        System.out.println(capa);
        System.out.println("__________________________________");
        ArrayList<Point> points = DataExtractor.extractPointData();
        Graph g = new Graph(points);
        int n = 20;
        Point[] dep = new Point[n];
        Point[] arr = new Point[n];

        Random generator = new Random(5);
        ArrayList<Aircraft> aircrafts = new ArrayList<>();

        double deb = 0.;
        int iter = 0;

        while (deb < 3) {
            for (int i = 0; i < n; i++) {
                int idep = (int) (generator.nextDouble() * points.size());
                int iarr = (int) (generator.nextDouble() * points.size());
                boolean found = (points.get(idep).distance(points.get(iarr))>200);

                while (idep == iarr || !found) {
                    idep = (int) (generator.nextDouble() * points.size());
                    iarr = (int) (generator.nextDouble() * points.size());
                    found = (points.get(idep).distance(points.get(iarr))>200);
                }
                double d = deb+0.25*generator.nextDouble();
                //System.out.println(points.get(idep).distance(points.get(iarr)));
                aircrafts.add(new Aircraft(points.get(idep), points.get(iarr), n * iter + i,
                        d ));
                System.out.println(""+points.get(idep).getId()+" "+points.get(iarr).getId());

            }
            deb += 0.5;
            iter += 1;
        }
        

        double currentTime = 0;

        ArrayList<Aircraft> aircraftUnderStudy = new ArrayList<>();
        for (Aircraft ac : aircrafts) {
            if (ac.getTime() < Constants.SIZE_SLIDING_WINDOW) {
                aircraftUnderStudy.add(ac);
            }
        }

        boolean allFinished = false;

        long tdeb = System.currentTimeMillis();
        ArrayList<Integer> evolNB = new ArrayList<>();
        while (!allFinished) {

            System.out.println(aircraftUnderStudy.size());
            evolNB.add(aircraftUnderStudy.size());
            Model model = new Model(g, aircraftUnderStudy, currentTime,capa);

            int nbToDo = 0;
            currentTime += Constants.SIZE_SLIDING_WINDOW;
            aircraftUnderStudy = new ArrayList<>();
            for (Aircraft ac : aircrafts) {

                if (!ac.isDone()) {
                    nbToDo += 1;
                    if (ac.getDepTime() <= currentTime + Constants.SIZE_SLIDING_WINDOW) {
                        aircraftUnderStudy.add(ac);
                    }
                }
                allFinished = (nbToDo == 0);
            }
            
        }
        long tfin = System.currentTimeMillis();
        System.out.println("Computation time : " + (tfin - tdeb) + " ms");
        Aircraft.printPath(aircrafts);
        Evaluator.evaluate(aircrafts, "results/capa20.csv");
        Evaluator.evaluateTotalCost(aircrafts);

    String t = "";
        for (Integer i:evolNB){
            t+=" "+i;
        }
        System.out.println(t);


    }




}
