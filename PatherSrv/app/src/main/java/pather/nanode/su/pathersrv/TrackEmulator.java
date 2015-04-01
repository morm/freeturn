package pather.nanode.su.pathersrv;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

/**
 * Created by hedger on 05.08.2014.
 */
public class TrackEmulator {
    private Track track;
    List<Waypoint> wps;
    private Location currentTarget;
    private Location lastLocation;

    // 1 sec = 30.864 m
    private static final double metersToDegrees = 40000000.0f / 360.0f;
    public float speed = 25; // m/s
    public float imitstep = 1; // sec

    private static final String PROVIDER = "flp";
    private static final float ACCURACY = 3.0f;

    public TrackEmulator(Track _src) {
        track = _src;
        wps = track.GetPoints();
        nextWaypoint();
        lastLocation = currentTarget;
        nextWaypoint();
    }

    public void setSpeed(float val) {
        speed = val;
    }

    private void nextWaypoint(){
        Log.e("pathersrv", "POPPED A POINT");
        if (wps.size() == 0)
            return;
        currentTarget = wps.remove(0).toLocation(PROVIDER, ACCURACY);
    }

    public Location getNextLocation() {
        //return lastLocation;

        Location newLocation;
        if ((lastLocation.distanceTo(currentTarget) < speed * imitstep)) {
            newLocation = currentTarget;
            nextWaypoint();
        }
        else {
            Log.w("pathersrv", "calc: moving to =" + currentTarget.getLatitude() + "/" + currentTarget.getLongitude() +
                    "we're at = " + lastLocation.getLatitude() + "/" + lastLocation.getLongitude());

            newLocation = new Location(PROVIDER);

            double dX = currentTarget.getLatitude() * 10000 - lastLocation.getLatitude() * 10000;
            double dY = currentTarget.getLongitude() * 10000 - lastLocation.getLongitude() * 10000;
            double len = dX * dX + dY * dY;
            Log.w("pathersrv", "LEN =" + len);
            double normCoeff = Math.sqrt(len);

            newLocation.setLatitude(lastLocation.getLatitude() + dX  * speed / normCoeff/ metersToDegrees);
            newLocation.setLongitude(lastLocation.getLongitude() + dY  * speed / normCoeff / metersToDegrees);
        }

        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        newLocation.setAltitude(0);
        newLocation.setAccuracy(ACCURACY);
        newLocation.setTime(System.currentTimeMillis());

        Log.w("pathersrv", "Set MOCK: nanos=" + newLocation.getElapsedRealtimeNanos() +
        "dist from prev = " + newLocation.distanceTo(lastLocation));


        lastLocation = newLocation;
        return newLocation;
    }
}
