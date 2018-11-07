package com.afa.geobuddy.ui.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.afa.geobuddy.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;


public class LocationFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mapGoogleApiClient;

    private Marker mCurrentLocationMarker;
    private Location myLastLocation;
    private CameraPosition mCameraPosition;
    private LocationRequest mapLocationRequest;


    // Keys for storing activity state
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";

    // Some params
    private final LatLng mDefaultLocation = new LatLng(51.3863, 0.5514); // Gilli .. something
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;
    private boolean mLocationPermissionGranted;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            myLastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Construct the API client
        buildGoogleApiClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_location, container, false);

        SupportMapFragment locationmapfragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.locationmap);
        if (locationmapfragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            locationmapfragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.locationmap, locationmapfragment).commit();
        }
        locationmapfragment.getMapAsync(this);
        return v;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Prompt the user for permission
        getLocationPermission();

        // Turn on the My location layer and the related control on the map.
        updateLocationUI();

        // mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Marker in Gillingham"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /**
         * Saves the state of the map when the activity is paused.
         */
        if (mMap != null) {
            outState.putParcelable(KEY_LOCATION, myLastLocation);
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
        }

    }

    protected synchronized void buildGoogleApiClient() {
        mapGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mapGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mapLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mapGoogleApiClient, mapLocationRequest, this);
            Log.i(TAG, "Location Services Connected.");
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended, Kindly Reconnect");
    }

    @Override
    public void onLocationChanged(Location location) {
        myLastLocation = location;

        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }
        // Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        String titleAddress = mapAddress(latLng);
        markerOptions.title(titleAddress);
        mCurrentLocationMarker = mMap.addMarker(markerOptions);


        //moving map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomOut());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        //stopping location updates
        if (mapGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mapGoogleApiClient, this);
        }


    }

    private String mapAddress(LatLng latLng) {
        //created geocoder object converting long and lat coordinates into address and vice versa
        Geocoder geocoder = new Geocoder(getActivity());
        String txtaddress = "";
        List<Address> addressList = null;
        Address address = null;
        try {
            //asks geocoder for address passed to method
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            //if address is available  convert to string and return
            if (null != addressList && !addressList.isEmpty()) {
                address = addressList.get(0);


                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    txtaddress += (i == 0) ? address.getAddressLine(i) : ("\n" + address.getAddressLine(i));

                }

            }

        } catch (IOException e) {

        }
        return txtaddress;
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

                // Move camera to last known location (if exists)
                if (myLastLocation != null) {
                    LatLng latLng = new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                }

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                myLastLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        /*
         * Handles the result of the permission request
         */

        Log.i("onRequestPermission", "requestCode: " + String.valueOf(requestCode));

        mLocationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

        updateLocationUI();
    }




    public LocationFragment() {
        // Required empty public constructor
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
