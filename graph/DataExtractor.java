package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.Constants;

public class DataExtractor {
    public static ArrayList<Point> extractPointData() throws IOException {
        FileReader filereader = new FileReader(Constants.POINTS_FILE);
        BufferedReader br = new BufferedReader(filereader);
        String line;
        ArrayList<Point> points = new ArrayList<>();
        while ((line = br.readLine()) != null) {

            String[] words = line.split(",");
            String id = words[0];
            if (id.equals("name")) { // Premiere ligne 
                continue;
            }
            double lat = Double.valueOf(words[1]);
            double lon = Double.valueOf(words[2]);
            int s = Integer.valueOf(words[3]);

            points.add(new Point(lat, lon, id,s));
        }
        br.close();
        return points;
    }
}
