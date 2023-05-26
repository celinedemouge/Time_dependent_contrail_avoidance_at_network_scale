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

/**
 * Main class
 */
public class Main {

    /**
     * Main function, lauch the algorithm
     * 
     * @param args
     */
    public static void main(String[] args) {
        int capacity = 14;
        try {
            loop(capacity);
        } catch (IloException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loop(int capa) throws IloException, IOException {
        ArrayList<Point> points = DataExtractor.extractPointData();
        Graph g = new Graph(points);

        // Instance building
        int n = 20; // Number of aircraft
        Point[] dep = new Point[n];
        Point[] arr = new Point[n];

        Random generator = new Random();
        ArrayList<Aircraft> aircrafts = new ArrayList<>();

        double deb = 0.;
        int iter = 0;

        while (deb < 3) { // Generates n aircraft per 0.5 hours during 3 hours
            for (int i = 0; i < n; i++) {
                int idep = (int) (generator.nextDouble() * points.size());
                int iarr = (int) (generator.nextDouble() * points.size());
                boolean found = (points.get(idep).distance(points.get(iarr)) > 200); // keeps only if departure and
                                                                                     // final points are far enough
                                                                                     // (here 200 NM)

                while (idep == iarr || !found) {
                    idep = (int) (generator.nextDouble() * points.size());
                    iarr = (int) (generator.nextDouble() * points.size());
                    found = (points.get(idep).distance(points.get(iarr)) > 200);
                }
                double d = deb + 0.25 * generator.nextDouble(); // generates a random departure time

                aircrafts.add(new Aircraft(points.get(idep), points.get(iarr), n * iter + i,
                        d));
                System.out.println("" + points.get(idep).getId() + " " + points.get(iarr).getId());

            }
            deb += 0.5;
            iter += 1;
        }

        // Begins the process, time is 0 and keeps only the aircraft which are departing
        // in the first time window
        double currentTime = 0;

        ArrayList<Aircraft> aircraftUnderStudy = new ArrayList<>();
        for (Aircraft ac : aircrafts) {
            if (ac.getTime() < Constants.SIZE_SLIDING_WINDOW) {
                aircraftUnderStudy.add(ac);
            }
        }

        boolean allFinished = false;

        long tdeb = System.currentTimeMillis();
        ArrayList<Integer> evolNB = new ArrayList<>(); // Number of aircraft in the simulation during each time window
        while (!allFinished) { // while all the aircraft are not at their final destination point

            // System.out.println(aircraftUnderStudy.size());
            evolNB.add(aircraftUnderStudy.size());
            Model model = new Model(g, aircraftUnderStudy, currentTime, capa); // Model and solve the problem for this
                                                                               // time window

            int nbToDo = 0;
            currentTime += Constants.SIZE_SLIDING_WINDOW; // increase the time
            aircraftUnderStudy = new ArrayList<>();
            for (Aircraft ac : aircrafts) {

                if (!ac.isDone()) { // if the aircraft is not at its destination point
                    nbToDo += 1;
                    if (ac.getDepTime() <= currentTime + Constants.SIZE_SLIDING_WINDOW) { // if its departure time is
                                                                                          // before the end of the next
                                                                                          // time window
                        aircraftUnderStudy.add(ac); // it will be considered for the next time window
                    }
                }
                allFinished = (nbToDo == 0); // if no aircraft meets these conditions, the process is finished since no
                                             // aircraft is departing and all the aircraft have reached their final
                                             // destination
            }

        }
        long tend = System.currentTimeMillis();
        System.out.println("Computation time : " + (tend - tdeb) + " ms"); // print the computation time
        Aircraft.printPath(aircrafts);
        Evaluator.evaluate(aircrafts, "results/capa14.csv"); // evaluate the solution and save results in a file
        Evaluator.evaluateTotalCost(aircrafts); // evaluate and print the total costs

    }

}
