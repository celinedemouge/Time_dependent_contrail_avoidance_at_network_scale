package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import graph.Edge;
import graph.Graph;
import graph.Point;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import utils.Constants;
import utils.Utils;

/**
 * Class representing the optimization model
 */
public class Model {

    private IloCplex model;
    private IloNumVar[][] vars;

    private IloCplex modelWithoutContrails; // model without contrails consideration in the case of bouding the flight
                                            // time
    private IloNumVar[][] varsWithoutContrails;

    private ArrayList<Aircraft> aircrafts;
    private ArrayList<AircraftModelized> aircraftsModelized;

    private int n; // aircraft number
    private int m; // edges number

    private Graph g;

    private double currentTime;

    /**
     * Constructs the optimization model and solve it
     * 
     * @param g               Graph considered
     * @param aircrafts       List of the aircraft considered
     * @param currentTime     Current time for time window consideration
     * @param capa_max_sector Capacity of all the sectors (can be modified to not be
     *                        the same for all the aircraft)
     * @throws IloException
     * @throws IOException
     */
    public Model(Graph g, ArrayList<Aircraft> aircrafts, double currentTime, double capa_max_sector)
            throws IloException, IOException {

        this.aircrafts = aircrafts;
        this.currentTime = currentTime;
        this.g = g;
        this.n = aircrafts.size();
        Point[] dep = new Point[n];
        Point[] arr = new Point[n];

        for (int i = 0; i < this.n; i++) {
            dep[i] = aircrafts.get(i).getDep();
            arr[i] = aircrafts.get(i).getEnd();
        }

        this.model = new IloCplex();

        this.modelWithoutContrails = new IloCplex();

        // double[] lb = buildModelWithoutContrail(g, dep, arr, n, capa_max_sector); //
        // Compute lower bound without contrails

        double[] lbNone = new double[n]; // use fake lower bound in the case where the flight time is not bounded
        for (int iiii = 0; iiii < n; iiii++) {
            lbNone[iiii] = 100000.;
        }

        buildModel(g, dep, arr, n, lbNone, capa_max_sector);

        solve();

        this.model.close();
        this.modelWithoutContrails.close();

    }

    /**
     * Builds the model
     * 
     * @param g
     * @param dep
     * @param arr
     * @param n
     * @param lb
     * @param capa_max_sector
     * @throws IloException
     */
    private void buildModel(Graph g, Point[] dep, Point[] arr, int n, double[] lb, double capa_max_sector)
            throws IloException {
        int mm = 0;
        Set<Point> points = g.getPoints();

        // Counts the number of edges
        for (Point p : points) {
            for (Edge ni : g.getAdj().get(p)) {
                mm += 1;
            }
        }
        this.m = mm;

        IloNumExpr[] objs = new IloNumExpr[n];
        IloNumExpr[] objsT = new IloNumExpr[n];
        this.vars = new IloNumVar[n][m];

        ArrayList<AircraftModelized> aircraft = new ArrayList<>();

        for (int k = 0; k < n; k++) {

            // Models each aircraft specific part
            AircraftModelized ac = new AircraftModelized(k, g, this.model, m, arr[k], dep[k]);
            aircraft.add(ac);
            objs[k] = ac.getObj();
            objsT[k] = this.model.abs(this.model.diff(ac.getObjT(), ac.gettMin()));
            // this.model.addLe(ac.getObjT(),Constants.MAX_TIME*lb[k]);
            this.vars[k] = ac.getVars();
        }

        // Capacity constraints
        for (int i = 0; i < g.getSecto().getNSectors(); i++) {

            ArrayList<IloNumVar> inSectorI = new ArrayList<>();
            IloNumVar[] inSectorI_v = new IloNumVar[aircraft.size()];
            int i_a = 0;

            // Is the aircraft flying through this sector during the considered time window
            // (K*Deta_t in the paper) ?
            for (AircraftModelized ac : aircraft) {
                IloNumVar inSectorI_A = this.model.numVar(0, 1);
                inSectorI_v[i_a] = inSectorI_A;
                ArrayList<Double> cost_s = new ArrayList<>();
                ArrayList<IloNumVar> concerned_var = new ArrayList<>();

                i_a += 1;
                // Computes the variables considered for this time window (K * DELTA_T in the
                // paper)
                for (Point p : points) {
                    if (g.getSecto().inWhichSector(p) == i) {
                        ArrayList<IloNumVar> out = ac.getVarOut(p);

                        inSectorI.addAll(out);
                        concerned_var.addAll(out);

                        double d = p.distance(ac.getDep()) / Constants.V;
                        double c = 1.;
                        if (d > 1.25 * Constants.SIZE_SLIDING_WINDOW) {
                            c = 0.;
                        }
                        // cost_s is used n to check if the sector is flown during the time window
                        // (K*DELTA_T)
                        cost_s.add(c);
                    }
                }
                // Transform costs_s to an array costs_a
                double[] costs_a = new double[cost_s.size()];
                for (int iii = 0; iii < cost_s.size(); iii++) {
                    costs_a[iii] = cost_s.get(iii);
                }
                IloNumVar[] vars_concernnee = concerned_var.toArray(new IloNumVar[concerned_var.size()]);
                // Modelling the passage through the sector during the time window
                // inSectorI_A is equal to 1 if the aircraft fly through this sector during the
                // consideriod time period
                this.model.add(this.model.ifThen(this.model.ge(this.model.scalProd(costs_a, vars_concernnee), 1.),
                        this.model.eq(inSectorI_A, 1.)));

            }
            // Capacity constraint for the sector considered
            this.model.addLe(this.model.sum(inSectorI_v), capa_max_sector);
        }

        IloNumExpr obj = this.model.sum(objs);
        IloNumExpr objEq = this.model.max(objsT);

        // If fairness is considered
        // double alpha = 0.9;
        // this.model.addMinimize(this.model.sum(this.model.prod((1. - alpha), obj),
        // this.model.prod(alpha, objEq)));

        this.model.addMinimize(obj);

        this.aircraftsModelized = aircraft;

    }

    private double[] buildModelWithoutContrail(Graph g, Point[] dep, Point[] arr, int n, double capa_max_sector)
            throws IloException {
        int mm = 0;
        Set<Point> points = g.getPoints();
        for (Point p : points) {
            for (Edge ni : g.getAdj().get(p)) {
                mm += 1;
            }
        }
        this.m = mm;

        IloNumExpr[] objs = new IloNumExpr[n];
        this.varsWithoutContrails = new IloNumVar[n][m];

        double[] lb = new double[n];

        ArrayList<AircraftModelized> aircraft = new ArrayList<>();

        for (int k = 0; k < n; k++) {
            AircraftModelized ac = new AircraftModelized(k, g, this.modelWithoutContrails, m, arr[k], dep[k]);
            aircraft.add(ac);
            objs[k] = ac.getObjT();
            this.varsWithoutContrails[k] = ac.getVars();
        }

        // Capacity constraints
        for (int i = 0; i < g.getSecto().getNSectors(); i++) {

            ArrayList<IloNumVar> inSectorI = new ArrayList<>();
            IloNumVar[] inSectorI_v = new IloNumVar[aircraft.size()];
            int i_a = 0;
            ArrayList<Double> costsSector = new ArrayList<>();
            for (AircraftModelized ac : aircraft) {
                IloNumVar inSectorI_A = this.modelWithoutContrails.numVar(0, 1);
                inSectorI_v[i_a] = inSectorI_A;
                ArrayList<Double> cost_s = new ArrayList<>();
                ArrayList<IloNumVar> concerned_var = new ArrayList<>();

                i_a += 1;
                for (Point p : points) {
                    if (g.getSecto().inWhichSector(p) == i) {
                        ArrayList<IloNumVar> out = ac.getVarOut(p);

                        inSectorI.addAll(out);
                        concerned_var.addAll(out);

                        double d = p.distance(ac.getDep()) / Constants.V;
                        double c = 1;
                        if (d > 1.25 * Constants.SIZE_SLIDING_WINDOW) {
                            c = 0;

                        }
                        // cost_s is used n to check if the sector is flown during the time window
                        // (K*DELTA_T)
                        cost_s.add(c);

                    }

                }

                // Transform costs_s to an array costs_a
                double[] costs_a = new double[cost_s.size()];
                for (int iii = 0; iii < cost_s.size(); iii++) {
                    costs_a[iii] = cost_s.get(iii);
                }
                IloNumVar[] vars_concernnee = concerned_var.toArray(new IloNumVar[concerned_var.size()]);
                // Modelling the passage through the sector during the time window
                this.modelWithoutContrails
                        .add(this.modelWithoutContrails.ifThen(
                                this.modelWithoutContrails
                                        .ge(this.modelWithoutContrails.scalProd(costs_a, vars_concernnee), 1.),
                                this.modelWithoutContrails.eq(inSectorI_A, 1.)));

            }
            // Capacity constraint
            this.modelWithoutContrails.addLe(this.modelWithoutContrails.sum(inSectorI_v), capa_max_sector);
        }

        IloNumExpr obj = this.modelWithoutContrails.sum(objs);

        this.modelWithoutContrails.addMinimize(obj);
        if (this.modelWithoutContrails.solve()) {
            for (int a = 0; a < n; a++) {
                lb[a] = this.modelWithoutContrails.getValue(objs[a]);
            }
        }
        return lb;

    }

    /**
     * Solves the problem and updates aircraft depatures to go to the next time
     * window
     * 
     * @throws IloException
     * @throws IOException
     */
    public void solve() throws IloException, IOException {
        if (this.model.solve()) {
            for (int i = 0; i < this.aircrafts.size(); i++) {
                updateAircraft(this.aircrafts.get(i).getId(), i);
            }
        } else {
            System.out.println("--- UNFEASIBLE ---");
        }

    }

    /**
     * Updates aircraft starting point at the end of the sliding window and append
     * points to its path until this new point
     * 
     * @param idAC  id of the aircraft
     * @param index the index of the aircraft in the list of (modelized) aircraft
     * @throws UnknownObjectException
     * @throws IloException
     */
    private void updateAircraft(int idAC, int index) throws UnknownObjectException, IloException {
        ArrayList<Point> p1 = new ArrayList<>();
        ArrayList<Point> p2 = new ArrayList<>();

        // Get the points flown by the aircraft (at the beginning of the edge for p1 and
        // at the end for p2)
        for (int j = 0; j < m; j++) {
            if (this.model.getValue(this.vars[index][j]) > 0) {
                p1.add(aircraftsModelized.get(index).get1(this.vars[index][j]));
                p2.add(aircraftsModelized.get(index).get2(this.vars[index][j]));

            }
        }
        Point end = aircraftsModelized.get(index).getEnd();
        Point dep = aircraftsModelized.get(index).getDep();

        // get the list of points flown by the aircraft in the right order
        ArrayList<Point> path = new ArrayList<>();
        path.add(dep);
        Point pCurrent = dep;
        while (!pCurrent.equals(end)) {
            for (int i = 0; i < p1.size(); i++) {
                if (p1.get(i).equals(pCurrent)) {
                    pCurrent = p2.get(i);
                    path.add(pCurrent);
                    break;
                }
            }
        }

        // End of the current time window management
        double t = 0;
        double t0 = this.aircrafts.get(index).getTime();
        this.aircrafts.get(index).addPointToPath(dep, t0);
        boolean found = false;

        for (int i = 1; i < path.size(); i++) {
            t += path.get(i - 1).time(path.get(i));
            double deltaT = Constants.SIZE_SLIDING_WINDOW - (this.aircrafts.get(index).getTime() - this.currentTime);

            // if the end of the time window is reached : create a new departure
            if (t > deltaT && !found) {
                found = true;

                Point newPoint = Utils.getPointSW(path.get(i - 1), path.get(i),
                        t - path.get(i - 1).time(path.get(i)), t, idAC,
                        Constants.SIZE_SLIDING_WINDOW - (this.aircrafts.get(index).getTime() - this.currentTime));

                this.aircrafts.get(index).addPointToPath(newPoint, this.currentTime + Constants.SIZE_SLIDING_WINDOW);
                this.g.addPoint(newPoint, path.get(i - 1), path.get(i));
                this.aircrafts.get(index).updateDep(this.currentTime + Constants.SIZE_SLIDING_WINDOW, newPoint);
                break;
            }
            this.aircrafts.get(index).addPointToPath(path.get(i), t + t0);
        }

        // If the aircraft does not reached the end of the sliding window, it is because
        // it has reached its final pooint during this time window
        if (!found) {
            this.aircrafts.get(index).finish();
        }

    }

}
