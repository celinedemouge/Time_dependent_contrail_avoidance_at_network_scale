package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.tree.VariableHeightLayoutCache;

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

public class Model {

    private IloCplex model;
    private IloNumVar[][] vars;

    private IloCplex modelWithoutContrails;
    private IloNumVar[][] varsWithoutContrails;



    private ArrayList<Aircraft> aircrafts;



    private ArrayList<AircraftModelized> aircraftsModelized;

    private int n;
    private int m;

    private Graph g;


    public final static double CAPA_MAX_SECTOR = 20.;

    private double currentTime;

    public Model(Graph g, ArrayList<Aircraft> aircrafts, double currentTime, double capa_max_sector)
            throws IloException, IOException {


        
        this.aircrafts = aircrafts;
        this.currentTime = currentTime;
        this.g=g;
        this.n = aircrafts.size();
        Point[] dep = new Point[n];
        Point[] arr = new Point[n];
        for (int i = 0; i < this.n; i++) {
            dep[i] = aircrafts.get(i).getDep();
            arr[i] = aircrafts.get(i).getEnd();
        }

        this.model = new IloCplex();
        /*this.model.setParam(IloCplex.Param.TimeLimit,10.);
        this.model.getBestObjValue();*/
        /*this.model.setParam(IloCplex.Param.Preprocessing.RepeatPresolve,
                        3);*/
        this.modelWithoutContrails = new IloCplex();


        //double[] lb = buildModelWithoutContrail(g, dep, arr, n);

        double[] lbsecours = new double[n];
        for (int iiii=0; iiii<n; iiii++){
            lbsecours[iiii] = 100000.;
        }
        
  
        buildModel(g, dep, arr, n,lbsecours,capa_max_sector);
        this.model.exportModel("test.lp");

        solve();
        

        this.model.close();
        this.modelWithoutContrails.close();


    }

  




    private void buildModel(Graph g, Point[] dep, Point[] arr, int n,double[] lb,double capa_max_sector) throws IloException {
        int mm = 0;
        Set<Point> points = g.getPoints();
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
            AircraftModelized ac = new AircraftModelized(k, g, this.model, m, arr[k], dep[k]);
            aircraft.add(ac);
            objs[k] = ac.getObj();
            objsT[k]=this.model.abs(this.model.diff(ac.getObjT(), ac.gettMin()));
            //this.model.addLe(ac.getObjT(),Constants.MAX_TIME*lb[k]);
            this.vars[k] = ac.getVars();
        }

        // Contrainte de capa secteur
        //double oMin = 0;
        //ArrayList<IloNumExpr> list_v = new ArrayList<>();
        for (int i = 0; i < g.getSecto().getNSectors(); i++) {
            
            ArrayList<IloNumVar> inSectorI = new ArrayList<>();
            IloNumVar[] inSectorI_v = new IloNumVar[aircraft.size()];
            int i_a = 0;
            ArrayList<Double> costsSector = new ArrayList<>();
            for (AircraftModelized ac : aircraft) {
                IloNumVar inSectorI_A = this.model.numVar(0, 1);
                inSectorI_v[i_a]=inSectorI_A;
                ArrayList<Double> cost_s = new ArrayList<>();
                ArrayList<IloNumVar> concernee_var = new ArrayList<>();

                i_a+=1;
                for (Point p : points) {
                    if (g.getSecto().inWhichSector(p) == i) {
                        ArrayList<IloNumVar> out = ac.getVarOut(p);
                        
                        inSectorI.addAll(out);
                        concernee_var.addAll(out);
                        
                        for (IloNumVar o : out){
                            double d = p.distance(ac.getDep())/Constants.V;
                            double c = 1.;
                            if (d>1.25*Constants.SIZE_SLIDING_WINDOW){
                                c = 0.;
                        
                            }
                            cost_s.add(c);
                            
                        }
                        
                        
                    }
                    
                    
                }
                double[] costs_a = new double[cost_s.size()];
                for (int iii = 0; iii<cost_s.size();iii++){
                    costs_a[iii]=cost_s.get(iii);
                }
                IloNumVar[] vars_concernnee = concernee_var.toArray(new IloNumVar[concernee_var.size()]);
                
                this.model.add(this.model.ifThen(this.model.ge(this.model.scalProd(costs_a, vars_concernnee), 1.), this.model.eq(inSectorI_A, 1.)));
                
            }
            //list_v.add(this.model.sum(inSectorI_v));
            this.model.addLe(this.model.sum(inSectorI_v),capa_max_sector);
        }

 
        IloNumExpr obj = this.model.sum(objs);
        IloNumExpr objEq = this.model.max(objsT);

        double alpha = 0.9;


        

        this.model.addMinimize(this.model.sum(this.model.prod((1.-alpha),obj),this.model.prod(alpha,objEq)));
        

        this.aircraftsModelized = aircraft;


    }


    private double[] buildModelWithoutContrail(Graph g, Point[] dep, Point[] arr, int n) throws IloException {
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

                // Contrainte de capa secteur
        //double oMin = 0;
        for (int i = 0; i < g.getSecto().getNSectors(); i++) {
            
            ArrayList<IloNumVar> inSectorI = new ArrayList<>();
            IloNumVar[] inSectorI_v = new IloNumVar[aircraft.size()];
            int i_a = 0;
            ArrayList<Double> costsSector = new ArrayList<>();
            for (AircraftModelized ac : aircraft) {
                IloNumVar inSectorI_A = this.modelWithoutContrails.numVar(0, 1);
                inSectorI_v[i_a]=inSectorI_A;
                ArrayList<Double> cost_s = new ArrayList<>();
                ArrayList<IloNumVar> concernee_var = new ArrayList<>();

                i_a+=1;
                for (Point p : points) {
                    if (g.getSecto().inWhichSector(p) == i) {
                        ArrayList<IloNumVar> out = ac.getVarOut(p);
                        
                        inSectorI.addAll(out);
                        concernee_var.addAll(out);
                        
                        for (IloNumVar o : out){
                            double d = p.distance(ac.getDep())/Constants.V;
                            double c = 1;
                            if (d>1.25*Constants.SIZE_SLIDING_WINDOW){
                                c = 0;
                        
                            }
                            cost_s.add(c);
                            
                        }
                        
                        
                    }
                    
                    
                }
                double[] costs_a = new double[cost_s.size()];
                for (int iii = 0; iii<cost_s.size();iii++){
                    costs_a[iii]=cost_s.get(iii);
                }
                IloNumVar[] vars_concernnee = concernee_var.toArray(new IloNumVar[concernee_var.size()]);
                this.modelWithoutContrails.add(this.modelWithoutContrails.ifThen(this.modelWithoutContrails.ge(this.modelWithoutContrails.scalProd(costs_a, vars_concernnee), 1.), this.modelWithoutContrails.eq(inSectorI_A, 1.)));
                
            }
            
            this.modelWithoutContrails.addLe(this.modelWithoutContrails.sum(inSectorI_v),CAPA_MAX_SECTOR);
        }

 
        IloNumExpr obj = this.modelWithoutContrails.sum(objs);

        this.modelWithoutContrails.addMinimize(obj);
        if (this.modelWithoutContrails.solve()){
            for (int a = 0; a<n; a++){
                lb[a]=this.modelWithoutContrails.getValue(objs[a]);
            }
            System.out.println("Helllloooo     "+this.modelWithoutContrails.getValue(obj));
        }
        /*else{
            return lb;
        }*/

        return lb;
        

    }

    public void solve() throws IloException, IOException {
        if (this.model.solve()) {
            for (int i = 0; i < this.aircrafts.size(); i++) {
                updateAircraft(this.aircrafts.get(i).getId(), i);
                //System.out.println(""+this.model.getValue(this.aircraftsModelized.get(i).getObjT())+"   "+lb)
            }
        }
        else{
            System.out.println("INFAISABLE !!!!!");
        }

    }





    

    private void updateAircraft(int idAC, int index) throws UnknownObjectException, IloException {
        ArrayList<Point> p1 = new ArrayList<>();
        ArrayList<Point> p2 = new ArrayList<>();
        for (int j = 0; j < m; j++) {
            if (this.model.getValue(this.vars[index][j]) > 0) {
                // System.out.println(this.vars[0][j]);
                p1.add(aircraftsModelized.get(index).get1(this.vars[index][j]));
                p2.add(aircraftsModelized.get(index).get2(this.vars[index][j]));

            }
        }
        Point end = null;
        Point dep = null;
        for (Point p11 : p1) {
            if (!p2.contains(p11)) {
                dep = p11;
            }
        }
        for (Point p22 : p2) {
            if (!p1.contains(p22)) {
                end = p22;
            }
        }

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

        double t = 0;//this.aircrafts.get(index).getTime();
        double t0 = this.aircrafts.get(index).getTime();
        this.aircrafts.get(index).addPointToPath(dep, t0);
        boolean found = false;

        for (int i = 1; i < path.size(); i++) {
            t += path.get(i - 1).time(path.get(i));
            double deltaT = Constants.SIZE_SLIDING_WINDOW - (this.aircrafts.get(index).getTime() - this.currentTime);
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
            this.aircrafts.get(index).addPointToPath(path.get(i), t+t0);
        }

        if (!found) {
            this.aircrafts.get(index).finish();
        }

    }

}

