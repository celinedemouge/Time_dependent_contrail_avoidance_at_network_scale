package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WindData {
    private static final String FILE_NAME = "/home/celine/codethese/extractDataFromWindy/firstStep.csv";

    private static final double LAT_MAX = 50.;
    private static final double LAT_MIN = 42.2;
    private static final double LON_MAX = 5.4;
    private static final double LON_MIN = -5.4;
    private static final double DELTA_LON = 0.2;
    private static final double DELTA_LAT = 0.2;


    private double[][] wU;
    private double[][] wV;


    public WindData(){

        int n = (int) ((LAT_MAX-LAT_MIN)/DELTA_LAT);
        int m = (int) ((LON_MAX-LON_MIN)/DELTA_LON);
        try {
            getwFromFile(n,m);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getwFromFile(int n, int m) throws NumberFormatException, IOException{
        this.wU = new double[n+1][m+1];
        this.wV = new double[n+1][m+1];
        FileReader filereader = new FileReader(FILE_NAME);
        BufferedReader br = new BufferedReader(filereader);
        String line;
        while ((line = br.readLine()) != null) {

            String[] words = line.split(",");

            if (!words[0].equals("wv")){
                double lat = Double.valueOf(words[5]);
            double lon = Double.valueOf(words[4]);
            double wu = Double.valueOf(words[3]);
            double wv = Double.valueOf(words[0]);

            this.wU[getindexLat(lat)][getindexLon(lon)]=msToKts(wu);
            this.wV[getindexLat(lat)][getindexLon(lon)]=msToKts(wv);
            }
            
        }
        br.close();


    }

    public static int getindexLat(double lat){
        return (int) ((lat-LAT_MIN)/DELTA_LAT);
    }

    public static int getindexLon(double lon){
        return (int) ((lon-LON_MIN)/DELTA_LON);
    }

    public double interpolateWu(double lon, double lat){

        // On check si c'est des points de la grille 
        boolean latGrid = (((lat/DELTA_LAT)-(int)(lat/DELTA_LAT)-1)<0.01);
        boolean lonGrid = (((lon/DELTA_LON)-(int)(lon/DELTA_LON))<0.01);

        if (latGrid){
            if (lonGrid){
                return this.wU[getindexLat(lat)][getindexLon(lon)];
            }
            else{
                double lonA = Math.floor(lon/DELTA_LON)*DELTA_LON;
                double lonB = Math.ceil(lon/DELTA_LON)*DELTA_LON;
                double wUA = this.wU[getindexLat(lat)][getindexLon(lonA)];
                double dAS = lon-lonA;
                double wUB = this.wU[getindexLat(lat)][getindexLon(lonB)];
                double dBS = lonB-lon;
                return (dBS*wUA+dAS*wUB)/(dAS+dBS);
            }
        }
        else{
            if (lonGrid){
                double latA = Math.floor(lat/DELTA_LAT)*DELTA_LAT;
                double latB = Math.ceil(lat/DELTA_LAT)*DELTA_LAT;
                double wUA = this.wU[getindexLat(latA)][getindexLon(lon)];
                double dAS = lat-latA;
                double wUB = this.wU[getindexLat(latB)][getindexLon(lon)];
                double dBS = latB-lat;
                return (dBS*wUA+dAS*wUB)/(dAS+dBS);

            }
            else{
                double latA = Math.floor(lat/DELTA_LAT)*DELTA_LAT;
                double latB = Math.floor(lat/DELTA_LAT)*DELTA_LAT;
                double latC = Math.ceil(lat/DELTA_LAT)*DELTA_LAT;
                double latD = Math.ceil(lat/DELTA_LAT)*DELTA_LAT;

                double lonA = Math.floor(lon/DELTA_LON)*DELTA_LON;
                double lonD = Math.floor(lon/DELTA_LON)*DELTA_LON;
                double lonB = Math.ceil(lon/DELTA_LON)*DELTA_LON;
                double lonC = Math.ceil(lon/DELTA_LON)*DELTA_LON;

                double dA = (lat-latA)*(lat-latA)+(lon-lonA)*(lon-lonA);
                double dB = (lat-latB)*(lat-latB)+(lon-lonB)*(lon-lonB);
                double dC = (lat-latC)*(lat-latC)+(lon-lonC)*(lon-lonC);
                double dD = (lat-latD)*(lat-latD)+(lon-lonD)*(lon-lonD);

                double wuA = this.wU[getindexLat(latA)][getindexLon(lonA)];
                double wuB = this.wU[getindexLat(latB)][getindexLon(lonB)];
                double wuC = this.wU[getindexLat(latC)][getindexLon(lonC)];
                double wuD = this.wU[getindexLat(latD)][getindexLon(lonD)];

                return ((1./dA)*wuA+(1./dB)*wuB+(1./dC)*wuC+(1./dD)*wuD)/((1./dA)+(1./dB)+(1./dC)+(1./dD));
            }
        }


    }

    public double interpolateWv(double lon, double lat){

        // On check si c'est des points de la grille 
        boolean latGrid = (((lat/DELTA_LAT)-(int)(lat/DELTA_LAT)-1)<0.01);
        boolean lonGrid = (((lon/DELTA_LON)-(int)(lon/DELTA_LON))<0.01);

        if (latGrid){
            if (lonGrid){
                return this.wV[getindexLat(lat)][getindexLon(lon)];
            }
            else{
                double lonA = Math.floor(lon/DELTA_LON)*DELTA_LON;
                double lonB = Math.ceil(lon/DELTA_LON)*DELTA_LON;
                double wUA = this.wV[getindexLat(lat)][getindexLon(lonA)];
                double dAS = lon-lonA;
                double wUB = this.wV[getindexLat(lat)][getindexLon(lonB)];
                double dBS = lonB-lon;
                return (dBS*wUA+dAS*wUB)/(dAS+dBS);
            }
        }
        else{
            if (lonGrid){
                double latA = Math.floor(lat/DELTA_LAT)*DELTA_LAT;
                double latB = Math.ceil(lat/DELTA_LAT)*DELTA_LAT;
                double wUA = this.wV[getindexLat(latA)][getindexLon(lon)];
                double dAS = lat-latA;
                double wUB = this.wV[getindexLat(latB)][getindexLon(lon)];
                double dBS = latB-lat;
                return (dBS*wUA+dAS*wUB)/(dAS+dBS);

            }
            else{
                double latA = Math.floor(lat/DELTA_LAT)*DELTA_LAT;
                double latB = Math.floor(lat/DELTA_LAT)*DELTA_LAT;
                double latC = Math.ceil(lat/DELTA_LAT)*DELTA_LAT;
                double latD = Math.ceil(lat/DELTA_LAT)*DELTA_LAT;

                double lonA = Math.floor(lon/DELTA_LON)*DELTA_LON;
                double lonD = Math.floor(lon/DELTA_LON)*DELTA_LON;
                double lonB = Math.ceil(lon/DELTA_LON)*DELTA_LON;
                double lonC = Math.ceil(lon/DELTA_LON)*DELTA_LON;

                double dA = (lat-latA)*(lat-latA)+(lon-lonA)*(lon-lonA);
                double dB = (lat-latB)*(lat-latB)+(lon-lonB)*(lon-lonB);
                double dC = (lat-latC)*(lat-latC)+(lon-lonC)*(lon-lonC);
                double dD = (lat-latD)*(lat-latD)+(lon-lonD)*(lon-lonD);

                double wuA = this.wV[getindexLat(latA)][getindexLon(lonA)];
                double wuB = this.wV[getindexLat(latB)][getindexLon(lonB)];
                double wuC = this.wV[getindexLat(latC)][getindexLon(lonC)];
                double wuD = this.wV[getindexLat(latD)][getindexLon(lonD)];

                return ((1./dA)*wuA+(1./dB)*wuB+(1./dC)*wuC+(1./dD)*wuD)/((1./dA)+(1./dB)+(1./dC)+(1./dD));
            }
        
        }
    }


    public static double msToKts(double ms){
        return ms*1.9438444924406;
    }
}
