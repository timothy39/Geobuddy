package com.afa.geobuddy.services;


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.afa.geobuddy.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by afa on 16/2/2018.
 */

public class LocationAlertIntentService extends IntentService {


    private static final String TAG = LocationAlertIntentService.class.getSimpleName();
    private static final int JOB_ID = 573;
    private static final String CHANNEL_ID = "channel_01";


    public LocationAlertIntentService() {
        super("LocationAlert");
    }

    public LocationAlertIntentService(String name) {
        super(name);
    }

//    public static void enqueueWork(Context context, Intent intent){
//        enqueueWork(context, LocationAlertIntentService.class, JOB_ID, intent);
//    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMsg = geofencingEvent.toString() + " " + geofencingEvent.getErrorCode();
            Log.e(TAG, errorMsg);
            return;
        }

        // Get transition type
        int geoTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest
        if (geoTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geoTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String transitionDetails = getGeofenceTransitionDetails(geoTransition, triggeringGeofences);

            String transitionType = getTransitionString(geoTransition);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            // Send notification and log the transition details
            notifyLocationAlert(transitionType, transitionDetails, alarmSound);
            Log.i(TAG, transitionDetails);
        } else {
            Log.e(TAG, "INVALID TRANSITION");
        }
    }


    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences) {
        /*
         * Gets transition details and returns them as a formatted string.
         *
         * @param geofenceTransition    The ID of the geofence transition.
         * @param triggeringGeofences   The geofence(s) triggered.
         * @return                      The transition details formatted as String.
         */

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }


    private String getLocationName(String key) {
        String[] strs = key.split("-");

        String locationName = null;
        if (strs != null && strs.length == 2) {
            double lat = Double.parseDouble(strs[0]);
            double lng = Double.parseDouble(strs[1]);

            locationName = getLocationNameGeocoder(lat, lng);
        }
        if (locationName != null) {
            return locationName;
        } else {
            return key;
        }
    }

    private String getLocationNameGeocoder(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception ioException) {
            Log.e("", "Error in getting location name for the location");
        }

        if (addresses == null || addresses.size() == 0) {
            Log.d("", "no location name");
            return null;
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressInfo = new ArrayList<>();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressInfo.add(address.getAddressLine(i));
            }

            return TextUtils.join(System.getProperty("line.separator"), addressInfo);
        }
    }

    private String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "geofence too many_geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "geofence too many pending_intents";
            default:
                return "geofence error";
        }
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "location entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "location exited";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell at location";
            default:
                return "location transition";
        }
    }

    private void notifyLocationAlert(String locTransitionType, String locationDetails, Uri alarmsound) {

        String CHANNEL_ID = "GEOBUDDY";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.addreminder)
                        .setContentTitle(locTransitionType)
                        .setContentText(locationDetails)
                        .setSound(alarmsound);


        builder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }
}
