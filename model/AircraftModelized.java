package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;

import graph.Edge;
import graph.Graph;
import graph.Point;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * Class used to model aircraft specific parts (for instance flow conservation
 * specific to each aircraft)
 */
public class AircraftModelized {

    public int id;
    private HashMap<Point, ArrayList<IloNumVar>> varOut;
    private HashMap<Point, ArrayList<IloNumVar>> varIn;
    private HashMap<IloNumVar, Point> p1Var;
    private HashMap<IloNumVar, Point> p2Var;
    private IloNumVar[] vars;
    private double[] costs;
    private IloNumExpr obj;
    private IloNumExpr objT;

    private Point dep;
    private Point end;

    private double tMin;

    /**
     * Constructs the object
     * 
     * @param id    Id of the aircraft
     * @param g     Graph used
     * @param cplex Instance of the model
     * @param m     Number of edges in the graph
     * @param arr   Destination point
     * @param dep   Starting point
     * @throws IloException
     */
    public AircraftModelized(int id, Graph g, IloCplex cplex, int m, Point arr, Point dep) throws IloException {
        this.id = id;
        this.dep = dep;
        this.end = arr;
        initialize(g, cplex, m, arr, dep); // Model for the aircraft
        // computeTmin(g, arr, dep) ; // Compute the ref time in case of fairness
        // consideration
        this.tMin = 0;
    }

    /**
     * Constructs the model for the aircraft specific parts
     * 
     * @param g
     * @param cplex
     * @param m
     * @param arr
     * @param dep
     * @throws IloException
     */
    private void initialize(Graph g, IloCplex cplex, int m, Point arr, Point dep) throws IloException {

        Set<Point> points = g.getPoints();

        this.varOut = new HashMap<>();
        this.varIn = new HashMap<>();
        this.p1Var = new HashMap<>();
        this.p2Var = new HashMap<>();
        this.vars = new IloNumVar[m];

        for (Point p : points) {
            varOut.put(p, new ArrayList<>());
            varIn.put(p, new ArrayList<>());
        }

        this.costs = new double[m];
        double[] times = new double[m];
        int i = 0;
        for (Point p : points) {

            for (Edge e : g.getAdj().get(p)) { // creates a variable for each edge (x(u,v,i))
                String name = "x_" + this.id + "_" + p.getId() + "_" + e.getP().getId();
                this.vars[i] = cplex.intVar(0, 1, name);
                this.costs[i] = e.getWeight();
                times[i] = e.getTime();

                this.varOut.get(p).add(vars[i]);
                this.varIn.get(e.getP()).add(vars[i]);
                this.p1Var.put(vars[i], p);
                this.p2Var.put(vars[i], e.getP());

                i += 1;

            }

        }

        // Flow conservation constraints
        for (Point p : points) {
            if (!p.equals(arr) && !p.equals(dep)) {
                int nOut = varOut.get(p).size();
                int nIn = varIn.get(p).size();
                IloNumExpr sum1 = cplex.sum(varOut.get(p).toArray(new IloNumVar[nOut]));
                IloNumExpr sum2 = cplex.prod(-1., cplex.sum(varIn.get(p).toArray(new IloNumVar[nIn])));
                cplex.addEq(cplex.sum(sum1, sum2), 0.);
            } else if (p.equals(arr)) {
                int nOut = varOut.get(p).size();
                int nIn = varIn.get(p).size();
                IloNumExpr sum1 = cplex.sum(varOut.get(p).toArray(new IloNumVar[nOut]));
                IloNumExpr sum2 = cplex.prod(-1., cplex.sum(varIn.get(p).toArray(new IloNumVar[nIn])));
                cplex.addEq(cplex.sum(sum1, sum2), -1.);
            } else {
                int nOut = varOut.get(p).size();
                int nIn = varIn.get(p).size();
                IloNumExpr sum1 = cplex.sum(varOut.get(p).toArray(new IloNumVar[nOut]));
                IloNumExpr sum2 = cplex.prod(-1., cplex.sum(varIn.get(p).toArray(new IloNumVar[nIn])));
                cplex.addEq(cplex.sum(sum1, sum2), 1.);
            }
        }

        this.obj = cplex.scalProd(this.vars, this.costs); // expression for aircraft specifc objectif (cost of its path)
        this.objT = cplex.scalProd(this.vars, times); // expression for time computation

    }

    /**
     * Computes the ref time in case of fairness consideration (Dijkstra algorithm)
     * 
     * @param g
     * @param arr
     * @param dep
     */
    private void computeTmin(Graph g, Point arr, Point dep) {
        Set<Point> points = g.getPoints();
        ArrayList<Point> pointsA = new ArrayList<>();
        pointsA.addAll(points);
        HashMap<Point, Double> costs = new HashMap<>();
        for (Point p : pointsA) {
            if (p.equals(dep)) {
                costs.put(p, 0.);
            } else {
                costs.put(p, Double.MAX_VALUE);
            }
        }

        PriorityQueue<Point> pointsQ = new PriorityQueue<>(
                (Point p1, Point p2) -> Double.compare(costs.get(p1), costs.get(p2)));
        pointsQ.addAll(points);

        while (pointsQ.size() > 0) {
            Point pMin = pointsQ.poll();
            for (Edge eN : g.getAdj().get(pMin)) {
                if (eN.getTime() + costs.get(pMin) < costs.get(eN.getP())) {
                    costs.put(eN.getP(), eN.getTime() + costs.get(pMin));

                }
            }
            if (pMin.equals(arr)) {
                break;
            }
        }

        this.tMin = costs.get(arr);
    }

    /**
     * Returns the ref time in case of fairness consideration
     * 
     * @return double
     */
    public double gettMin() {
        return tMin;
    }

    /**
     * Returns the aircraft specific objective
     * 
     * @return IloNumExpr
     */
    public IloNumExpr getObj() {
        return this.obj;
    }

    /**
     * Returns the expression for aircraft flight time
     * 
     * @return IloNumExpr
     */
    public IloNumExpr getObjT() {
        return this.objT;
    }

    /**
     * Returns aircraft variables
     * 
     * @return IloNumVar[]
     */
    public IloNumVar[] getVars() {
        return this.vars;
    }

    /**
     * Returns the variables for edges going out the point
     * 
     * @param p
     * @return ArrayList<IloNumVar>
     */
    public ArrayList<IloNumVar> getVarOut(Point p) {
        return this.varOut.get(p);
    }

    /**
     * Returns the variables for edges going in the point
     * 
     * @param p
     * @return ArrayList<IloNumVar>
     */
    public ArrayList<IloNumVar> getVarIn(Point p) {
        return this.varIn.get(p);
    }

    /**
     * Returns the point p1 where the variable is associated to the edge (p1,p2)
     * 
     * @param var
     * @return Point
     */
    public Point get1(IloNumVar var) {
        return this.p1Var.get(var);
    }

    /**
     * Returns the point p2 where the variable is associated to the edge (p1,p2)
     * 
     * @param var
     * @return Point
     */
    public Point get2(IloNumVar var) {
        return this.p2Var.get(var);
    }

    /**
     * Returns the starting point
     * 
     * @return Point
     */
    public Point getDep() {
        return this.dep;
    }

    /**
     * Returns the final point
     * 
     * @return Point
     */
    public Point getEnd() {
        return this.end;
    }

}
