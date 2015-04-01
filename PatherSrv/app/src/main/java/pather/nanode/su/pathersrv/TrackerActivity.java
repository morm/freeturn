package pather.nanode.su.pathersrv;

import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.GooglePlayServicesUtil;
import android.util.Log;
import android.os.Handler;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.location.Address;
import android.location.Location;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.AsyncTask;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pather.nanode.su.pathersrv.BluetoothService;

class PathDirectionController {
    Track points;
    List<Waypoint> wps;
    Location lastKnownLoc;
    Location movingTo;
    Location nextPoint;
    boolean almostAtEnd = false;

    private final static int arrivalEpsilon = 10; // meters

    public PathDirectionController(Track track) {
        points = track;
        wps  = track.GetPoints();
        movingTo = popPointFromTrack();
        nextPoint = popPointFromTrack();
    }

    private Location popPointFromTrack(){
        almostAtEnd = (wps.size() <= 1);
        return wps.remove(0).toLocation("flp", 3.0f);
    }

    void ReceiveUpdate(Location loc) {
        lastKnownLoc = loc;
        if (lastKnownLoc.distanceTo(movingTo) < arrivalEpsilon) {
            movingTo = nextPoint;
            if (!almostAtEnd)
                nextPoint = popPointFromTrack();
         }
    }

    double[] GetDirections() {
        double dist = 0, dirn = 0;
        if ((lastKnownLoc != null) && (movingTo != null)) {
            dist = lastKnownLoc.distanceTo(movingTo);
            dirn = - lastKnownLoc.bearingTo(movingTo) + movingTo.bearingTo(nextPoint);
        }
        Log.e("pathersrv", "Issuing a command: dist= " + dist + ", route = " + dirn);
        double [] aaaa = { dist, dirn };
        return  aaaa;
    }
}

public class TrackerActivity extends FragmentActivity
        implements ConnectionCallbacks, OnConnectionFailedListener
{
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothService mChatService = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private int mInterval = 1500; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private Track track;
    private LocationClient locationClient;
    private TrackEmulator emu;
    private PathDirectionController dirnController;
    int cycle = 0;
    boolean mocking = false;
    JSONObject jsonobject;
    JSONArray jsonarray;
    List<Integer> ids;

    List<Waypoint> mocks;

    int tracktoload = -1;

    void LoadAndStartTrack(Track trk){
        track = trk;
        mocks = track.GetPoints();

        mMap.clear();
        LatLng where;
        for (Waypoint p: mocks) {
            where = new LatLng(p.GetLat(), p.GetLng());
            mMap.addMarker(
                    new MarkerOptions().position(where).title("TRACK"));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mocks.get(0).GetLat(), mocks.get(0).GetLng()), 15.0f));

        emu = new TrackEmulator(track);
        dirnController = new PathDirectionController(track);
    }

    void RequestRemoteTrack(int id) {
        tracktoload = id;
        new DownloadTrack().execute();
    }
    private class DownloadTrack extends AsyncTask<Void, Void, Void> {
        JSONObject tmpobj;
        @Override
        protected Void doInBackground(Void... params) {
                 String trackURL = "http://159.253.20.162:8080/rest.api/InputServlet?req=get_points&id=" + tracktoload + "&";
                tmpobj  = JSONFunctions.getJSONfromURL(trackURL);


            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            // Locate the spinner in activity_main.xml
            LoadAndStartTrack(new JsonTrack(tmpobj));
        }
    }

    private class DownloadJSON extends AsyncTask<Void, Void, Void> {
        List<String> list;
        List<Integer> ids;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                jsonobject = JSONFunctions
                        .getJSONfromURL("http://159.253.20.162:8080/rest.api/InputServlet?req=get_list&");
                jsonarray = jsonobject.getJSONArray("main");
                list = new ArrayList<String>();
                ids = new ArrayList<Integer>();
                for (int i = 0; i < jsonarray.length(); i++) {

                    jsonobject = jsonarray.getJSONObject(i);
                    list.add(jsonobject.optString("name"));
                    ids.add(Integer.parseInt(jsonobject.optString("id")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            // Locate the spinner in activity_main.xml
            final Spinner spin = ((Spinner) TrackerActivity.this.findViewById(R.id.spin));

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(TrackerActivity.this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin.setAdapter(dataAdapter);
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                    int itemid= spin.getSelectedItemPosition(); // .getSelectedItem().toString();
                    Log.i("pathersrv", "Selected item : " + itemid);
                    RequestRemoteTrack(ids.get(itemid));
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }

            });
            TrackerActivity.this.ids = ids;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracker);

        new DownloadJSON().execute();

        mHandler = new Handler();
        setUpMapIfNeeded();
        loadTrack();

        try {
           locationClient = new LocationClient(this, this, this);
        }
        catch (Exception e) {
            Log.e("pather", e.toString());
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    @Override
    public void onStart()
    {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        if (mChatService == null)
            mChatService = new BluetoothService(this, mHandler);
    }

    private void ensureDiscoverable()
    {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
        }
    }

    private void loadTrack()
    {
        LoadAndStartTrack(new FileTrack("/sdcard/test.wpl"));
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            updateStatus(); //this function can change value of mInterval.
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    void updateStatus() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(cycle % 90, cycle % 90 + 1)).title("Marker"));
        //cycle++;
        //Message msg = mHandler.obtainMessage("Fucking hed");
        //mHandler.sendMessage(msg);

        if (dirnController != null)
            dirnController.GetDirections();

        DumpLocation();
        if (mocking && (mocks.size() > 0)) {
            //Waypoint p = mocks.remove(0);

            locationClient.setMockLocation(emu.getNextLocation());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationClient != null) {
            locationClient.connect();
        }
        setUpMapIfNeeded();

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (locationClient != null) {
            locationClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        //Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
        Log.w("pathersrv", "CONNECTION FAILED!");
    }
    @Override
    public void onDisconnected() {
        //Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
        Log.w("pathersrv", "Disconnected!");
    }

    private void DumpLocation() {
        Log.d("pathersrv", "DumpLocation");
        if (locationClient == null)
            return;

        if (!mocking)
            return;

        Location loc = locationClient.getLastLocation();
        if (loc == null) {
            Log.d("pathersrv", "Location inaccurate: scanner disabled.");
            return;
        }

        if (dirnController != null) {
            dirnController.ReceiveUpdate(loc);
        }

        Log.d("pathersrv", "location=" + loc.toString());
        Log.d("pathersrv", "location2=" + loc.getLongitude() + " " + loc.getLatitude());

        LatLng where = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.addMarker(
                    new MarkerOptions().position(where).title("Marker").alpha(0.3f)
            );

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(where, 18.2f));
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.d("pathersrv", "onConnected");

        DumpLocation();
        locationClient.setMockMode(true);
        mocking = true;
    }

    private CharSequence addressToText(Address address) {
        final StringBuilder addressText = new StringBuilder();
        for (int i = 0, max = address.getMaxAddressLineIndex(); i < max; ++i) {
            addressText.append(address.getAddressLine(i));
            if ((i+1) < max) {
                addressText.append(", ");
            }
        }
        addressText.append(", ");
        addressText.append(address.getCountryName());
        return addressText;
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
            Log.w("pathersrv", "No gapps avaliable!");
            return;
        }

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                startRepeatingTask();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
    }
}
