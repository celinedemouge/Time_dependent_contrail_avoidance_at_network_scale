package graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ContrailsData {

    private static final String FILE_NAME = "/home/celine/codethese/extractDataFromWindy/firstStep.csv"; // File where
                                                                                                         // data are
                                                                                                         // searched

    private static final String FILE_EXTRACT = "extract.txt"; // File in which data can be exported and written

    // To be adapted regarding data : min and max latitudes in the file, min and max
    // longitudes in the file, and grid resolution
    private static final double LAT_MAX = 50.;
    private static final double LAT_MIN = 42.2;
    private static final double LON_MAX = 5.4;
    private static final double LON_MIN = -5.4;
    private static final double DELTA_LON = 0.2;
    private static final double DELTA_LAT = 0.2;

    private static final double CP = 1004.;
    private static final double EIH2O = 1.25;
    private static final double FUEL_COMBUSTION_HEAT = 43e6; // Classical data, can be changed
    private static final double EPSILON = 0.622;
    private static final double ETA = 0.3; // Classical data, can be changed
    private static final double TO_KELVINS = 273.15;
    private static final double PRESSURE = 300.; // in HPA

    private double[][] rh; // Relative humidity data
    private double[][] t; // Temperature data

    /**
     * Constructor for contrails data object, parameters are directly changed in
     * constants of the class
     */
    public ContrailsData() {

        int n = (int) ((LAT_MAX - LAT_MIN) / DELTA_LAT);
        int m = (int) ((LON_MAX - LON_MIN) / DELTA_LON);
        try {
            getFromFile(n, m);
            this.extractContrailsToFile();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates the object from the data file
     * 
     * @param n number of latitudes in the grid
     * @param m number of longitudes in the grid
     * @throws NumberFormatException
     * @throws IOException
     */
    public void getFromFile(int n, int m) throws NumberFormatException, IOException {
        this.rh = new double[n + 2][m + 2];
        this.t = new double[n + 2][m + 2];
        FileReader filereader = new FileReader(FILE_NAME);
        BufferedReader br = new BufferedReader(filereader);
        String line;
        while ((line = br.readLine()) != null) {

            String[] words = line.split(",");

            if (!words[0].equals("wv")) { // To be adapted according to the data file architecture (here lat is in
                                          // position 5 in the csv file for instance)
                double lat = Double.valueOf(words[5]);
                double lon = Double.valueOf(words[4]);
                double rh = Double.valueOf(words[2]);
                double t = Double.valueOf(words[1]);

                this.rh[getindexLat(lat)][getindexLon(lon)] = rh;
                this.t[getindexLat(lat)][getindexLon(lon)] = t;
            }

        }
        br.close();

    }

    /**
     * Computes the index in the grid of the given latitude
     * 
     * @param lat Latitude
     * @return int
     */
    public static int getindexLat(double lat) {
        if (lat < LAT_MIN) {
            return 0;
        }
        double lat_ii = ((lat - LAT_MIN) / DELTA_LAT);
        int lat_i = (int) Math.round(lat_ii);
        return lat_i;
    }

    /**
     * Computes the index in the grid of the given longitude
     * 
     * @param lon Longitude
     * @return int
     */
    public static int getindexLon(double lon) {
        if (lon < LON_MIN) {
            return 0;
        }
        double lon_ii = ((lon - LON_MIN) / DELTA_LON);
        int lon_i = (int) Math.round(lon_ii);
        return lon_i;
    }

    /**
     * Interpolates relative humidity on the given point
     * 
     * @param lon Longitude of the point
     * @param lat Latitude of the point
     * @return double
     */
    public double interpolateRH(double lon, double lat) {

        // Is the point in the grid ?
        boolean latGrid = (((lat / DELTA_LAT) - (int) (lat / DELTA_LAT) - 1) < 0.01);
        boolean lonGrid = (((lon / DELTA_LON) - (int) (lon / DELTA_LON)) < 0.01);

        // Different cases : it is on a latitude of the grid or not and it is on a
        // longitude of the grid or not
        // Here, quadratic interpolation is used (Shepard mentionned in the paper)
        if (latGrid) {
            if (lonGrid) {
                int i = getindexLat(lat);
                if (i < 0) {
                    i = 0;
                }
                return this.rh[getindexLat(lat)][getindexLon(lon)];
            } else {
                double lonA = Math.floor(lon / DELTA_LON) * DELTA_LON;
                double lonB = Math.ceil(lon / DELTA_LON) * DELTA_LON;
                double wUA = this.rh[getindexLat(lat)][getindexLon(lonA)];
                double dAS = lon - lonA;
                double wUB = this.rh[getindexLat(lat)][getindexLon(lonB)];
                double dBS = lonB - lon;
                return (dBS * wUA + dAS * wUB) / (dAS + dBS);
            }
        } else {
            if (lonGrid) {
                double latA = Math.floor(lat / DELTA_LAT) * DELTA_LAT;
                double latB = Math.ceil(lat / DELTA_LAT) * DELTA_LAT;
                double wUA = this.rh[getindexLat(latA)][getindexLon(lon)];
                double dAS = lat - latA;
                double wUB = this.rh[getindexLat(latB)][getindexLon(lon)];
                double dBS = latB - lat;
                return (dBS * wUA + dAS * wUB) / (dAS + dBS);

            } else {
                double latA = Math.floor(lat / DELTA_LAT) * DELTA_LAT;
                double latB = Math.floor(lat / DELTA_LAT) * DELTA_LAT;
                double latC = Math.ceil(lat / DELTA_LAT) * DELTA_LAT;
                double latD = Math.ceil(lat / DELTA_LAT) * DELTA_LAT;

                double lonA = Math.floor(lon / DELTA_LON) * DELTA_LON;
                double lonD = Math.floor(lon / DELTA_LON) * DELTA_LON;
                double lonB = Math.ceil(lon / DELTA_LON) * DELTA_LON;
                double lonC = Math.ceil(lon / DELTA_LON) * DELTA_LON;

                double dA = (lat - latA) * (lat - latA) + (lon - lonA) * (lon - lonA);
                double dB = (lat - latB) * (lat - latB) + (lon - lonB) * (lon - lonB);
                double dC = (lat - latC) * (lat - latC) + (lon - lonC) * (lon - lonC);
                double dD = (lat - latD) * (lat - latD) + (lon - lonD) * (lon - lonD);

                double wuA = this.rh[getindexLat(latA)][getindexLon(lonA)];
                double wuB = this.rh[getindexLat(latB)][getindexLon(lonB)];
                double wuC = this.rh[getindexLat(latC)][getindexLon(lonC)];
                double wuD = this.rh[getindexLat(latD)][getindexLon(lonD)];

                return ((1. / dA) * wuA + (1. / dB) * wuB + (1. / dC) * wuC + (1. / dD) * wuD)
                        / ((1. / dA) + (1. / dB) + (1. / dC) + (1. / dD));
            }
        }
    }

    /**
     * Interpolates temperature at the given point
     * 
     * @param lon Longitude of the point
     * @param lat Latitude of the point
     * @return double
     */
    public double interpolateT(double lon, double lat) {
        // Is the point in the grid ?
        boolean latGrid = (((lat / DELTA_LAT) - (int) (lat / DELTA_LAT) - 1) < 0.01);
        boolean lonGrid = (((lon / DELTA_LON) - (int) (lon / DELTA_LON)) < 0.01);

        // Different cases : it is on a latitude of the grid or not and it is on a
        // longitude of the grid or not
        // Here, quadratic interpolation is used (Shepard mentionned in the paper)
        if (latGrid) {
            if (lonGrid) {
                return this.t[getindexLat(lat)][getindexLon(lon)];
            } else {
                double lonA = Math.floor(lon / DELTA_LON) * DELTA_LON;
                double lonB = Math.ceil(lon / DELTA_LON) * DELTA_LON;
                double wUA = this.t[getindexLat(lat)][getindexLon(lonA)];
                double dAS = lon - lonA;
                double wUB = this.t[getindexLat(lat)][getindexLon(lonB)];
                double dBS = lonB - lon;
                return (dBS * wUA + dAS * wUB) / (dAS + dBS);
            }
        } else {
            if (lonGrid) {
                double latA = Math.floor(lat / DELTA_LAT) * DELTA_LAT;
                double latB = Math.ceil(lat / DELTA_LAT) * DELTA_LAT;
                double wUA = this.t[getindexLat(latA)][getindexLon(lon)];
                double dAS = lat - latA;
                double wUB = this.t[getindexLat(latB)][getindexLon(lon)];
                double dBS = latB - lat;
                return (dBS * wUA + dAS * wUB) / (dAS + dBS);

            } else {
                double latA = Math.floor(lat / DELTA_LAT) * DELTA_LAT;
                double latB = Math.floor(lat / DELTA_LAT) * DELTA_LAT;
                double latC = Math.ceil(lat / DELTA_LAT) * DELTA_LAT;
                double latD = Math.ceil(lat / DELTA_LAT) * DELTA_LAT;

                double lonA = Math.floor(lon / DELTA_LON) * DELTA_LON;
                double lonD = Math.floor(lon / DELTA_LON) * DELTA_LON;
                double lonB = Math.ceil(lon / DELTA_LON) * DELTA_LON;
                double lonC = Math.ceil(lon / DELTA_LON) * DELTA_LON;

                double dA = (lat - latA) * (lat - latA) + (lon - lonA) * (lon - lonA);
                double dB = (lat - latB) * (lat - latB) + (lon - lonB) * (lon - lonB);
                double dC = (lat - latC) * (lat - latC) + (lon - lonC) * (lon - lonC);
                double dD = (lat - latD) * (lat - latD) + (lon - lonD) * (lon - lonD);

                double wuA = this.t[getindexLat(latA)][getindexLon(lonA)];
                double wuB = this.t[getindexLat(latB)][getindexLon(lonB)];
                double wuC = this.t[getindexLat(latC)][getindexLon(lonC)];
                double wuD = this.t[getindexLat(latD)][getindexLon(lonD)];

                return ((1. / dA) * wuA + (1. / dB) * wuB + (1. / dC) * wuC + (1. / dD) * wuD)
                        / ((1. / dA) + (1. / dB) + (1. / dC) + (1. / dD));
            }
        }
    }

    /**
     * Compute if the point is in a persistent contrail area.
     * 
     * @param lon Longitude of the point
     * @param lat Latitude of the point
     * @return boolean
     */
    public boolean isInContrailP(double lon, double lat) {

        // Get RH and T for contrails computation on any point
        double rh = interpolateRH(lon, lat);
        double t = interpolateT(lon, lat);

        // See the paper for detail of contrails computation (appendix B)
        double tTh = temperatureThreshold(PRESSURE * 100);
        double pLiqT = saturationPressure(t);
        double pLiqTTh = saturationPressure(tTh + TO_KELVINS);
        double g = (CP * PRESSURE * 100 * EIH2O) / (EPSILON * FUEL_COMBUSTION_HEAT * (1 - ETA));

        double rhTh = 100 * (g * (t - (tTh + TO_KELVINS)) + pLiqTTh) / pLiqT; // remove 100* if rh is given between 0
                                                                              // and 1 in the data

        boolean inContrail = (t < (tTh + TO_KELVINS) && rh > rhTh); // Schmidt Appleman criteria : contrail or not
        if (inContrail) {
            double tc = t - TO_KELVINS;
            double num = 6.0612 * Math.exp((18.102 * tc) / (249.52 + tc));
            double den = 6.1162 * Math.exp((22.577 * tc) / (273.78 + tc));
            return (rh * num / den > 100); // is it in ISSR area ?
        }

        return false;

    }

    /**
     * Compute if the point (which is in the grid) is in a persistent contrail area.
     * 
     * @param lon Longitude of the point
     * @param lat Latitude of the point
     * @return boolean
     */
    public boolean isInContrailPG(double lon, double lat) {

        // Get RH and T for contrails computation on any point OF THE GRID
        double rh = this.rh[getindexLat(lat)][getindexLon(lon)];
        double t = this.t[getindexLat(lat)][getindexLon(lon)];

        // See the paper for detail of contrails computation (appendix B)
        double tTh = temperatureThreshold(PRESSURE * 100);
        double pLiqT = saturationPressure(t);
        double pLiqTTh = saturationPressure(tTh + TO_KELVINS);
        double g = (CP * PRESSURE * 100 * EIH2O) / (EPSILON * FUEL_COMBUSTION_HEAT * (1 - ETA));

        double rhTh = 100 * (g * (t - (tTh + TO_KELVINS)) + pLiqTTh) / pLiqT; // remove 100* if rh is given between 0
                                                                              // and 1 in the data

        boolean inContrail = (t < (tTh + TO_KELVINS) && rh > rhTh); // Schmidt Appleman criteria : contrail or not
        if (inContrail) {
            double tc = t - TO_KELVINS;
            double num = 6.0612 * Math.exp((18.102 * tc) / (249.52 + tc));
            double den = 6.1162 * Math.exp((22.577 * tc) / (273.78 + tc));
            return (rh * num / den > 100); // is it in ISSR area ?
        }

        return false;

    }

    /**
     * Compute if the point (which is in the grid) is in a contrail area.
     * 
     * @param lon Longitude of the point
     * @param lat Latitude of the point
     * @return boolean
     */
    public boolean isInContrailG(double lon, double lat) {

        // Get RH and T for contrails computation on any point OF THE GRID
        double rh = this.rh[getindexLat(lat)][getindexLon(lon)];
        double tt = this.t[getindexLat(lat)][getindexLon(lon)];

        double tTh = temperatureThreshold(PRESSURE * 100);
        double pLiqT = saturationPressure(tt);
        double pLiqTTh = saturationPressure(tTh + TO_KELVINS);
        double g = (CP * PRESSURE * 100 * EIH2O) / (EPSILON * FUEL_COMBUSTION_HEAT * (1 - ETA));

        double rhTh = 100 * (g * (tt - (tTh + TO_KELVINS)) + pLiqTTh) / pLiqT; // remove 100* if rh is given between 0
                                                                               // and 1 in the data

        return (tt < (tTh + TO_KELVINS) && rh > rhTh); // Schmidt Appleman criteria : contrail or not

    }

    /**
     * Compute if the point is in a contrail area.
     * 
     * @param lon Longitude of the point
     * @param lat Latitude of the point
     * @return boolean
     */
    public boolean isInContrail(double lon, double lat) {
        // Get RH and T for contrails computation on any point
        double rh = interpolateRH(lon, lat);
        double t = interpolateT(lon, lat);

        double tTh = temperatureThreshold(PRESSURE * 100);
        double pLiqT = saturationPressure(t);
        double pLiqTTh = saturationPressure(tTh + TO_KELVINS);
        double g = (CP * PRESSURE * 100 * EIH2O) / (EPSILON * FUEL_COMBUSTION_HEAT * (1 - ETA));

        double rhTh = 100 * (g * (t - (tTh + TO_KELVINS)) + pLiqTTh) / pLiqT; // remove 100* if rh is given between 0
                                                                              // and 1 in the data

        return (t < (tTh + TO_KELVINS) && rh > rhTh); // Schmidt Appleman criteria : contrail or not

    }

    private static double temperatureThreshold(double pressure) { // Compute the temperature threshold for Schmidt
                                                                  // Appleman criteria
        // pressure en Pa
        double g = (CP * pressure * EIH2O) / (EPSILON * FUEL_COMBUSTION_HEAT * (1 - ETA));
        double log = Math.log(g - 0.053);
        double temperatureThreshold = -46.46 + 9.43 * log + 0.72 * log * log;
        return temperatureThreshold; // in celcius
    }

    private static double saturationPressure(double temperature) { // Compute the saturation pressure (for Schmidt
                                                                   // Appleman criteria)
        // temperature in kelvins
        double satPressure = 100. * Math.exp((-6096.9385 / temperature) + 16.635794 - 0.02711193 * temperature
                + 1.673952e-5 * temperature * temperature + 2.433502 * Math.log(temperature));
        // result in Pa
        return satPressure;

    }

    private void extractContrailsToFile() throws FileNotFoundException { // Useful for debug (extract computed data to a
                                                                         // file)
        PrintWriter writer = new PrintWriter(FILE_EXTRACT);
        double lat = LAT_MIN;
        while (lat < LAT_MAX) {
            double lon = LON_MIN;
            while (lon < LON_MAX) {
                Boolean inContrails = this.isInContrailG(lon, lat);
                int inContrailInt = 0;
                if (inContrails) {
                    inContrailInt = 1;
                }
                Boolean inContrailsP = this.isInContrailPG(lon, lat);
                int inContrailPInt = 0;
                if (inContrailsP) {
                    inContrailPInt = 1;
                }
                float lat_print = (float) ((int) (lat * 1000)) / 1000;
                float lon_print = (float) ((int) (lon * 1000)) / 1000;
                String toWrite = "" + lat_print + " " + lon_print + " " + inContrailInt + " " + inContrailPInt;
                writer.println(toWrite);

                lon += DELTA_LON;
            }
            lat += DELTA_LAT;
        }

        writer.close();
    }
}
