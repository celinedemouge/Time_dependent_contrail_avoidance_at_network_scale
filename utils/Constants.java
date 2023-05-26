package utils;

import graph.ContrailsData;
import graph.WindData;

public class Constants {
    public static double TO_RADIANS = Math.PI / 180.;
    public static double TO_DEGREES = 180. / Math.PI;
    public static double V = 400.;

    public static final WindData WD = new WindData();
    public static final ContrailsData CD = new ContrailsData();

    public static final double GWP = 0.63;

    public static final double D_MAX = 75.;

    public static String PATH_FILE = "results/paths.txt";
    public static String POINTS_FILE = "data/points_with_sectors.csv";

    public static double SIZE_SLIDING_WINDOW = 0.25; //en heures !

    public static double MAX_TIME=1.1;
}
