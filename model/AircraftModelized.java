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

    public AircraftModelized(int id, Graph g, IloCplex cplex, int m, Point arr, Point dep) throws IloException{
        this.id = id;
        this.dep = dep;
        this.end = arr;
        initialize(g,cplex,m,arr,dep);
        computeTmin(g, arr,  dep) ;
        //tMin = 0.;
    }



    private void initialize(Graph g, IloCplex cplex, int m, Point arr, Point dep) throws IloException{

        
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

            for (Edge e : g.getAdj().get(p)) {
                String name = "x_" + this.id + "_" + p.getId() + "_" + e.getP().getId();
                this.vars[i] = cplex.intVar(0, 1, name);
                this.costs[i] = e.getWeight();
                times[i]=e.getTime();

                this.varOut.get(p).add(vars[i]);
                this.varIn.get(e.getP()).add(vars[i]);
                this.p1Var.put(vars[i], p);
                this.p2Var.put(vars[i], e.getP());

                i += 1;
                
            }

        }

        // Contraintes chemin
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

        this.obj = cplex.scalProd(this.vars, this.costs);
        this.objT = cplex.scalProd(this.vars, times);


        
    }


    private void computeTmin(Graph g,Point arr, Point dep){
        Set<Point> points = g.getPoints();
        ArrayList<Point> pointsA = new ArrayList<>();
        pointsA.addAll(points);
        HashMap<Point,Double> costs = new HashMap<>();
        for (Point p : pointsA){
            if (p.equals(dep)){
                costs.put(p,0.);
            }
            else{
                costs.put(p,Double.MAX_VALUE);
            }
        }

        PriorityQueue<Point> pointsQ = new PriorityQueue<>((Point p1, Point p2)-> Double.compare(costs.get(p1),costs.get(p2)));
        pointsQ.addAll(points);

        while (pointsQ.size()>0){
            double m = Double.MAX_VALUE;
            Point pMin = pointsQ.poll();
            /*System.out.println(pMin);
            System.out.println(g.getAdj().get(pMin).size());
            
            System.out.println(g.getAdj().get(pMin).size());*/
            for (Edge eN : g.getAdj().get(pMin)){
                //System.out.println("here");
                if (eN.getTime()+costs.get(pMin)<costs.get(eN.getP())){
                    costs.put(eN.getP(), eN.getTime()+costs.get(pMin));
                    //System.out.println("here");
                }
            }
            //pointsA.remove(pMin);
            if (pMin.equals(arr)){
                break;
            }
        }
        System.out.println(costs.get(arr));
        this.tMin=costs.get(arr);

        




    }

    

    public double gettMin() {
        return tMin;
    }



    public IloNumExpr getObj() {
        return this.obj;
    }

    public IloNumExpr getObjT() {
        return this.objT;
    }

    public IloNumVar[] getVars() {
        return this.vars;
    }

    public ArrayList<IloNumVar> getVarOut(Point p) {
        return this.varOut.get(p);
    }

    public ArrayList<IloNumVar> getVarIn(Point p) {
        return this.varIn.get(p);
    }

    public Point get1(IloNumVar var){
        return this.p1Var.get(var);
    }

    public Point get2(IloNumVar var){
        return this.p2Var.get(var);
    }

    public Point getDep(){
        return this.dep;
    }

    public Point getEnd(){
        return this.end;
    }




    
}
