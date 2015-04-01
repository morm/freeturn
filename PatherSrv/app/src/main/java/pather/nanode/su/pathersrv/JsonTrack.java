package pather.nanode.su.pathersrv;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hedger on 05.08.2014.
 */
public class JsonTrack extends  Track {
    public JsonTrack(JSONObject jsonobject){
        JSONArray jsonarray;
        waypoints = new ArrayList<Waypoint>();
        try {
            jsonarray = jsonobject.getJSONArray("main");

            for (int i = 0; i < jsonarray.length(); i++){

                JSONObject tmp = jsonarray.getJSONObject(i);
                Log.w("pathersrv", "Got !" + tmp.optString("lon") + " - " + tmp.optString("lat"));
                waypoints.add(new Waypoint(Float.parseFloat(tmp.optString("lon")),
                        Float.parseFloat(tmp.optString("lat"))));
                Log.w("pathersrv", "SUCCESS");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
