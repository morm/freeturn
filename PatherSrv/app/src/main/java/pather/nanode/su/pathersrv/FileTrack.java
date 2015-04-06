package pather.nanode.su.pathersrv;
import android.location.Location;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;

/**
 * Created by hedger on 04.08.2014.
 */
class Waypoint {
    private float lng;
    private float lat;

    public float GetLat() {
        return lat;
    }

    public float GetLng() {
        return lng;
    }

    Waypoint(float _long, float _lat) {
        lng = _long;
        lat = _lat;
    }

    Location toLocation(String provider, float accuracy) {
        Location newLocation = new Location(provider);
        newLocation.setLatitude(GetLat());
        newLocation.setLongitude(GetLng());
        newLocation.setAccuracy(accuracy);
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        newLocation.setAltitude(0);
        newLocation.setTime(System.currentTimeMillis());
        return  newLocation;
    }
}

class Track {
    protected  List<Waypoint> waypoints;

    public List<Waypoint> GetPoints() {
        return waypoints;
    }

}

    public class FileTrack extends Track {

        FileTrack(String src) {
        waypoints = new ArrayList<Waypoint>();

        try {
            FileInputStream s = new FileInputStream(src);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s));
            String coords;
            try {
                reader.readLine();
                while (true) {
                    coords = reader.readLine();

                    if ((coords == null) || coords.isEmpty())
                        break;
                    //String[] parts = coords.split(" ");
                    //waypoints.add(new Waypoint(Float.parseFloat(parts[0]),
                    //        Float.parseFloat(parts[1])));

                    String[] parts = coords.split(",");
                    waypoints.add(new Waypoint(Float.parseFloat(parts[2]),
                            Float.parseFloat(parts[1])));
                }
            }
            catch (IOException e) {
                //...
            }
        }
        catch (FileNotFoundException e) {

        }
    }
}
