package utils;

import graph.ContrailsData;
import graph.WindData;

/**
 * Constants useful for computations
 */
public class Constants {
    public static double TO_RADIANS = Math.PI / 180.;
    public static double TO_DEGREES = 180. / Math.PI;

    /**
     * Aircraft airspeed
     */
    public static double V = 400.;

    /**
     * Wind data
     */
    public static final WindData WD = new WindData();
    /**
     * Constrails data
     */
    public static final ContrailsData CD = new ContrailsData();

    /**
     * GWP according to the time horizon chosen
     */
    public static final double GWP = 0.63;

    /**
     * Maximum distance between two neighbors nodes in the graph (in NM)
     */
    public static final double D_MAX = 75.;

    /**
     * Filename for path saving
     */
    public static String PATH_FILE = "results/paths.txt";
    /**
     * Filename for points data
     */
    public static String POINTS_FILE = "data/points_with_sectors.csv";

    /**
     * Size of the sliding window (in hours)
     */
    public static double SIZE_SLIDING_WINDOW = 0.25;

    /**
     * Coefficient for flight time constraint
     */
    public static double MAX_TIME = 1.1;
}
