package com.afa.geobuddy.ui.search;


import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.afa.geobuddy.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class SearchFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap myMap;
    private LatLng latLng;
    private MarkerOptions markerOptions;
    private EditText txtSearch;
    private Button btnSearch;
    private List<Address> addresses = null;
    private Address address;
    private Circle circle;


    // Keys for storing activity state
    private static final String KEY_LOCATION = "address";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private CameraPosition mCameraPosition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment)
        View searchView = inflater.inflate(R.layout.fragment_search, container, false);
        txtSearch = (EditText) searchView.findViewById(R.id.txtSearchAddress);
        btnSearch = (Button) searchView.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GeocoderTask().execute(txtSearch.getText().toString());
                Log.i("clicked", "i got here somehow");
            }
        });
        SupportMapFragment searchmapfragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.searchmap);

        if (searchmapfragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            searchmapfragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.searchmap, searchmapfragment).commit();
        }

        searchmapfragment.getMapAsync(this);

        return searchView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.4047, 0.5418), 5.0f));
        //provide focus and zoom control
    }

    protected void executeSearch(List<Address> addresses) {
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getActivity(), "No Address", Toast.LENGTH_SHORT).show();
        }
        myMap.clear();

        //adding markers for addresses
        for (int i = 0; i < addresses.size(); i++) {
            address = (Address) addresses.get(i);


            //creating instance of geopoint
            latLng = new LatLng(address.getLatitude(), address.getLongitude());

            String txtAddress = String.format("%s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getCountryName());
            address.getPostalCode();
            address.getCountryCode();
            address.getSubLocality();
            address.getAdminArea();

            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(txtAddress);
            myMap.addMarker(markerOptions);
            circle = myMap.addCircle(new CircleOptions().center(latLng).radius(100).strokeColor(Color.BLUE));


            //locate first location
            if (i == 0) {
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            }
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            Geocoder geocoder = new Geocoder(getActivity());

            try {
                addresses = geocoder.getFromLocationName(locationName[0], 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }


        @Override
        protected void onPostExecute(List<Address> addresses) {
            executeSearch(addresses);

        }
    }
}