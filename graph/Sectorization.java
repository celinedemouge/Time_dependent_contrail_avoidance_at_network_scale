package graph;

public class Sectorization {

    private int nSector;
    private Sector[] sectors;

    /**
     * Creates a set of sector
     * 
     * @param nSector
     */
    public Sectorization(int nSector) {
        this.nSector = nSector;
        this.sectors = new Sector[nSector];
        for (int i = 0; i < nSector; i++) {
            this.sectors[i] = new Sector();
        }
    }

    /**
     * @return int
     */
    public int getNSectors() {
        return this.nSector;
    }

    /**
     * Checks in which sector is the point
     * 
     * @param p
     * @return int
     */
    public int inWhichSector(Point p) {
        for (int i = 0; i < this.nSector; i++) {
            if (this.sectors[i].isInSector(p)) {
                return i;
            }
        }
        return this.nSector;
    }

    /**
     * Adds a points to a sector
     * 
     * @param p
     * @param i
     */
    public void addPoint(Point p, int i) {
        this.sectors[i].addPoint(p);
    }

}