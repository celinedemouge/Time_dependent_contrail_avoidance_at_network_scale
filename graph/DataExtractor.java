package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.Constants;

public class DataExtractor {

    /**
     * Extract Points from the points file (defined in the constants Module))
     * 
     * @see utils.Constants
     * @return ArrayList<Point>
     * @throws IOException
     */
    public static ArrayList<Point> extractPointData() throws IOException {
        FileReader filereader = new FileReader(Constants.POINTS_FILE);
        BufferedReader br = new BufferedReader(filereader);
        String line;
        ArrayList<Point> points = new ArrayList<>();
        while ((line = br.readLine()) != null) {

            String[] words = line.split(",");
            String id = words[0];
            if (id.equals("name")) { // Adapted to file structure (here name,lat,lon,sector)
                continue;
            }
            double lat = Double.valueOf(words[1]);
            double lon = Double.valueOf(words[2]);
            int s = Integer.valueOf(words[3]);

            points.add(new Point(lat, lon, id, s)); // Create a new point for each line
        }
        br.close();
        return points;
    }
}
