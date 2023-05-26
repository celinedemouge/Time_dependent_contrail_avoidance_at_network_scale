package graph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Sectorization {

    private int nSector;
    private Sector[] sectors;

    public Sectorization(int nSector){
        this.nSector =nSector;
        this.sectors = new Sector[nSector];
        for (int i=0; i<nSector; i++){
            this.sectors[i] = new Sector();
        }
    }

    public int getNSectors(){
        return this.nSector;
    }

    public int inWhichSector(Point p){
        for (int i=0; i<this.nSector; i++){
            if (this.sectors[i].isInSector(p)){
                return i;
            }
        }
        return this.nSector;
    }

    public void addPoint(Point p, int i){
        this.sectors[i].addPoint(p);
    }

    
}