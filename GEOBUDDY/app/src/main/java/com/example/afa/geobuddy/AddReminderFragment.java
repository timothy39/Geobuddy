package com.example.afa.geobuddy;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO: 1. Add ImageButton for clearing "location" field


public class AddReminderFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = AddReminderFragment.class.getSimpleName();

    /* Tracks whether the user requested to add or remove geofences, or to do neither */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }


    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;
    private static final int NOTIFICATION_ARRIVAL = 0;
    private static final int NOTIFICATION_DEPARTURE = 1;
    private static final int DEFAULT_RADIUS = 5;

    // Widgets

    private TextView txt_arrival;
    private TextView txt_departure;
    private TextView txt_radius;
    private EditText edit_title;
    private DelayAutoCompleteTextView edit_location;
    private EditText edit_note;
    private EditText edit_address;
    private Slider bar_radius;
    private Toolbar rtoolbar;
    private Button btn_save;

    private Integer THRESHOLD = 2;

    private ImageView geo_autocomplete_clear;


    private GoogleMap mMap;
    LatLng latLng;
    MarkerOptions markerOptions;
    private GoogleApiClient mGoogleApiClient;
    private Marker mLocationMarker;
    private Location mLastLocation;
    private CameraPosition mCameraPosition;
    private LocationRequest mLocationRequest;
    private boolean mLocationPermissionGranted;
    private Circle mCircle = null;

    private GeofencingClient mGeofencingClient;     // Provides access to the Geofencing API
    private PendingIntent mGeofencePendingIntent;           // Used when requesting to add or remove geofences
    private ArrayList<Geofence> mGeofenceList;      // The list of geofences used


    // RESULTS
    private int mNotificationType = NOTIFICATION_ARRIVAL;
    private GeoSearchResult mGeoSearchResult;
    private int mRadius;
    private String mTitle, mlocation;
    private String mNote;
    private boolean editreminder;

    // Required empty public constructor
    public AddReminderFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null
        mGeofencePendingIntent = null;

        // Empty list for storing geofences
        mGeofenceList = new ArrayList<>();

        // Construct the API client
        buildGoogleApiClient();

        mGeofencingClient = LocationServices.getGeofencingClient(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isLocationAccessPermitted())
            getLocationPermission();
        else
            updateLocationUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment)
        View view = inflater.inflate(R.layout.fragment_add_reminder, container, false);
        btn_save = view.findViewById(R.id.btn_save);
        btn_save.setEnabled(false);
        initUI(view);

        SupportMapFragment searchmapfragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (searchmapfragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            searchmapfragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, searchmapfragment).commit();
        }

        searchmapfragment.getMapAsync(this);




        //btn save
        btn_save = view.findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Prompt the user for permission
        getLocationPermission();

        // Turn on the My location layer and the related control on the map.
        if (isLocationAccessPermitted()) {
            updateLocationUI();
        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000);
        try {
            if (isLocationAccessPermitted()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                Log.i(TAG, "Location Services Connected.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error in onConnected " + e.getMessage());
        }


    }

    private void updateLocationUI() {
        /*
         * Updates the map's UI settings based on user's decision to grant Geobuddy, location permission
         */
        if (mMap == null)
            return;

        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                if (mLastLocation == null) {

                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (location == null) {
                        return;
                        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    } else {
                        mLastLocation = location;
                    }
                }

                // Move camera to last known location (if exists)
                if (mLastLocation != null) {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                }

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    /**
     * initialize UI
     */
    private void initUI(View view) {
        edit_title = view.findViewById(R.id.txt_title);
        edit_note = view.findViewById(R.id.txt_note);
        edit_location = view.findViewById(R.id.txt_location);
        edit_address = view.findViewById(R.id.txtSearchAddress);
        bar_radius = view.findViewById(R.id.radius_seekbar);
        txt_arrival = view.findViewById(R.id.txt_arrival);
        txt_departure = view.findViewById(R.id.txt_departure);
        txt_radius = view.findViewById(R.id.txt_radius);


        editreminder = getActivity().getIntent().getBooleanExtra("edit this note", false);
        if (editreminder) {
            mTitle = getActivity().getIntent().getStringExtra("title");
            mlocation = getActivity().getIntent().getStringExtra("location");

            edit_note.setText(mTitle);
            edit_address.setText(mlocation);
            btn_save.setEnabled(true);
        }
        else{
            btn_save.setEnabled(false);
        }

        // TITLE field
        edit_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0 && !edit_location.getText().toString().isEmpty() && mGeoSearchResult != null)
                    btn_save.setEnabled(true);
                else
                    btn_save.setEnabled(false);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // LOCATION field
        edit_location.setThreshold(THRESHOLD);
        edit_location.setAdapter(new GeoAutoCompleteAdapter(getContext()));
        edit_location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                mGeoSearchResult = result; // set result for storing in database

                edit_location.setText(result.getAddress());

                drawCircle(); // Draw Circle on Map
                placeMarker();
            }
        });
        edit_location.measure(0, 0); // must call
        edit_location.setDropDownWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        edit_location.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0 && !edit_title.getText().equals(""))
                    btn_save.setEnabled(true);
                else
                    btn_save.setEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
//                if(s.length() > 0)
//                {
//                    geo_autocomplete_clear.setVisibility(View.VISIBLE);
//                }
//                else
//                {
//                    geo_autocomplete_clear.setVisibility(View.GONE);
//                }
            }
        });

//        geo_autocomplete_clear.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                edit_location.setText("");
//            }
//        });


        // TODO: Add listener for "TITLE" EditText for enabling or disabling "OK" button appropriately

        /* NOTIFICATION-TYPE field */
        txt_departure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNotificationType(NOTIFICATION_DEPARTURE);
            }
        });

        txt_arrival.setSelected(true);
        txt_arrival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNotificationType(NOTIFICATION_ARRIVAL);
            }
        });


        /* RADIUS field */
        bar_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = bar_radius.getValue();
                txt_radius.setText(bar_radius.formatString());
                if (mCircle != null) // update circle drawn on the map
                    drawCircle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        bar_radius.setProgress(DEFAULT_RADIUS);
        txt_radius.setText(bar_radius.formatString());
        mRadius = bar_radius.getValue();
    }




    /** Add a new reminder  */
    private void save() {

        String title = edit_title.getText().toString();
        String locationString = edit_location.getText().toString();
        String note = edit_note.getText().toString();
        LatLng latLng = new LatLng(mGeoSearchResult.fullAddress().getLatitude(), mGeoSearchResult.fullAddress().getLongitude());



        Reminder newReminder = new Reminder(title, latLng, mRadius, mNotificationType, note);
        newReminder.save();
        Log.i("REMINDER SAVED", title + locationString);
        addLocationAlert(latLng.latitude, latLng.longitude);

        getActivity().finish();

    }

    /** Annotates map with a circle */
    private void drawCircle(){

        // clear previous circle
        if (mCircle != null)
            mCircle.remove();

        Address address = mGeoSearchResult.fullAddress();
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
        mCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(mRadius)
                .strokeColor(Color.argb(100, 0, 122, 255))
                .fillColor(Color.argb(0, 212, 226, 240)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                mCircle.getCenter(), getZoomLevel(mCircle)));
    }

    /** Annotates map with a marker */
    private void placeMarker(){

        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }

        Address address = mGeoSearchResult.fullAddress();
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        String titleAddress = mapAddress(latLng);
        markerOptions.title(titleAddress);
        mLocationMarker = mMap.addMarker(markerOptions);
    }

    /** Adjusts camera zoom based on the circle drawn */
    public int getZoomLevel(Circle circle) {
        int zoomLevel = 11;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    /** Updates notification type when "Departure" or "Arrival" buttons are clicked */
    private void setNotificationType(int notificationType){
        if(notificationType == this.mNotificationType)
            return;

        if (notificationType == NOTIFICATION_ARRIVAL){
            txt_arrival.setSelected(true);
            txt_departure.setSelected(false);
        }
        else if (notificationType == NOTIFICATION_DEPARTURE){
            txt_arrival.setSelected(false);
            txt_departure.setSelected(true);
        }
        this.mNotificationType = notificationType;
    }


    /** **************************************************************************************
     *
     * GEOFENCING METHODS for adding or removing alerts
     *
     ******************************************************************************************/


    private void addLocationAlert(double lat, double lng){

        // Get Location Access permission (if needed)
        getLocationPermission();

        String key = ""+lat+"-"+lng;
        Geofence geofence = getGeofence(lat, lng, key, mRadius, mNotificationType);
        try{
            mGeofencingClient.addGeofences(
                    getGeofencingRequest(geofence, mNotificationType),
                    getGeofencePendingIntent())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(),
                                        "Reminder added",
                                        Toast.LENGTH_SHORT).show();

                            }else{
                                Toast.makeText(getActivity(),
                                        "Location alter could not be added",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to add geofence");
                }});
        }
        catch (SecurityException e){
            Log.e(TAG, e.toString());
        }


    }

    private void removeLocationAlert(){

        // Checks for location access permission
        getLocationPermission();

        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Location alters have been removed",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getContext(),
                                    "Location alters could not be removed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private PendingIntent getGeofencePendingIntent() {
        /*
         * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
         * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
         * current list of geofences.
         *
         * @return A PendingIntent for the IntentService that handles geofence transitions.
         */

        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null)
            return mGeofencePendingIntent;

        Intent intent = new Intent(getContext(), LocationAlertIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    private GeofencingRequest getGeofencingRequest(Geofence geofence, int notificationType) {
        /*
         * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
         * Also specifies how the geofence notifications are initially triggered.
         */

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();


        if (notificationType == NOTIFICATION_ARRIVAL){
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER ); // | GeofencingRequest.INITIAL_TRIGGER_DWELL
        }
        else if (notificationType == NOTIFICATION_DEPARTURE){
            // The INITIAL_TRIGGER_EXIT flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_EXIT notification when the geofence is added and if the device
            // is already outside that geofence.
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        }

        // Add the geofences to be monitored by geofencing service
        builder.addGeofence(geofence);

        // Return a GeofencingRequest
        return builder.build();
    }


    private Geofence getGeofence(double lat, double lang, String key, int radius, int notificationType) {
        /*
         *  returns a Geofence object
         */

        Geofence.Builder geofenceBuilder = new Geofence.Builder();

        geofenceBuilder.setRequestId(key)
                .setCircularRegion(lat, lang, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(10000);

        // Set the transition types of interest
        if (notificationType == NOTIFICATION_ARRIVAL)
            geofenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL);
        else if (notificationType == NOTIFICATION_DEPARTURE)
            geofenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT);

        // Create the Geofence
        Geofence geofence = geofenceBuilder.build();
        mGeofenceList.add(geofence);
        return geofence;
    }


//    private boolean getGeofencesAdded(){
//        /*
//        * returns true if geofences were added, otherwise false.
//        */
//        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
//    }
//
//
//    private void updateGeofencesAdded(boolean added) {
//        /*
//         * Stores whether geofences were added ore removed in {@link SharedPreferences};
//         *
//         * @param added Whether geofences were added or removed.
//         */
//        PreferenceManager.getDefaultSharedPreferences(getContext())
//                .edit()
//                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
//                .apply();
//    }

    /** **************************************************************************************
     *
     * LOCATION PERMISSION METHODS
     *
     ******************************************************************************************/

    private boolean isLocationAccessPermitted(){
        /*
         * Checks if Location Access has been allowed by the user
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    private void getLocationPermission(){
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (this.isLocationAccessPermitted()) {
            mLocationPermissionGranted = true;
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }


    /** **************************************************************************************
     *
     * MISCELLANEOUS METHODS
     *
     ******************************************************************************************/




    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        //moving map camera
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomOut());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng ));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

        //stopping location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }



    private String mapAddress(LatLng latLng){
        //created geocoder object converting long and lat coordinates into address and vice versa
        Geocoder geocoder = new Geocoder(getActivity());
        String txtaddress = "";
        List<Address> addressList = null;
        Address address = null;
        try{
            //asks geocoder for address passed to method
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            //if address is available  convert to string and return
            if(null != addressList && !addressList.isEmpty()){
                address = addressList.get(0);
                for(int i =0; i < address.getMaxAddressLineIndex(); i++){
                    txtaddress += (i == 0)?address.getAddressLine(i):("\n" + address.getAddressLine(i));

                }
            }

        }catch(IOException e){

        }
        return txtaddress;
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        /*
         * Handles the result of the permission request
         */

        Log.i(TAG, "requestCode: " + String.valueOf(requestCode));

        mLocationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateLocationUI();
                }
            }
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended, Kindly Reconnect");
    }




    /**  @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.add_reminder_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
    }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
     case R.id.add_reminder:
     save();
     return true;
     //            case R.id.cancel:
     //                getActivity().finish();
     //                return true;
     default:
     return super.onOptionsItemSelected(item);

     }
     }**/


}
