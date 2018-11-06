package com.example.afa.geobuddy;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

import java.sql.Blob;

/**
 * Created by AFA on 14/02/2018.
 */


public class Reminder extends SugarRecord implements Parcelable {
     private String locationString;
     private Double latitude;
     private Double longitude;
     private int notificationType;
     private String title;
     private String note;
     private int radius;

    private static final String KEY_TITLE = "key_title";
    private static final String KEY_LATITUDE = "key_latitude";
    private static final String KEY_LONGITUDE = "key_longitude";
    private static final String KEY_NOTE = "key_note";
    private static final String KEY_RADIUS = "key_radius";
    private static final String KEY_NOTIFICATIONTYPE = "key_notification_type";
    private static final String KEY_LOCATIONSTRING = "key_location_string";
    private static final String KEY_ID = "key_id";


    public Reminder() {
        Log.i("DataBase", "I was called");
    }


    public Reminder(String title, LatLng location, int radius, int notificationType, String note) {
        this.title=title;
        this.latitude =  location.latitude;
        this.longitude = location.longitude;
        this.radius = radius;
        this.notificationType = notificationType;
        this.note =note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocationString() {
        return locationString;
    }

    public void setLocationString(String locationString) {
        this.locationString = locationString;
    }

    public LatLng getLocation() {
        return new LatLng(this.latitude, this.longitude);
    }

    public void setLocation(LatLng location) {
        this.latitude = location.latitude;
        this.longitude = location.longitude;
    }

    public int getNotificationType() {
        return notificationType;
    }
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }



    /** Parcelable methods */

    public Reminder(Parcel in){
        Bundle data;

        data = in.readBundle(getClass().getClassLoader());
        this.setId(data.getLong(KEY_ID));
        this.title = data.getString(KEY_TITLE);
        this.locationString = data.getString(KEY_LOCATIONSTRING);
        this.radius = data.getInt(KEY_RADIUS);
        this.note = data.getString(KEY_NOTE);
        this.notificationType = data.getInt(KEY_NOTIFICATIONTYPE);
        this.latitude = data.getDouble(KEY_LATITUDE);
        this.longitude = data.getDouble(KEY_LONGITUDE);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, this.title);
        bundle.putString(KEY_NOTE, this.note);
        bundle.putDouble(KEY_LATITUDE, this.latitude);
        bundle.putDouble(KEY_LONGITUDE, this.longitude);

        bundle.putInt(KEY_NOTIFICATIONTYPE, this.notificationType);
        bundle.putInt(KEY_RADIUS, this.radius);
        bundle.putString(KEY_LOCATIONSTRING, this.locationString);
        bundle.putLong(KEY_ID, this.getId());
        dest.writeBundle(bundle);
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };
}



