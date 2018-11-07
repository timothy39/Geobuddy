package com.afa.geobuddy.ui.reminder;

import android.location.Address;

/**
 * Created by afa on 15/2/2018.
 */

public class GeoSearchResult {
    private static final String TAG = GeoSearchResult.class.getSimpleName();
    private Address address;

    public GeoSearchResult(Address address) {
        this.address = address;
    }

    public String getAddress() {

        String display_address = "";

        display_address += address.getAddressLine(0) + "\n";

        for (int i = 1; i < address.getMaxAddressLineIndex(); i++) {
            display_address += address.getAddressLine(i) + ", ";
        }

        if (display_address.endsWith("\n"))
            display_address = display_address.substring(0, display_address.length() - 1);

        return display_address;
    }

    public Address fullAddress() {
        return this.address;
    }

    public String toString() {
        String display_address = "";

        if (address.getFeatureName() != null) {
            display_address += address + ", ";
        }

        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
            display_address += address.getAddressLine(i);
        }

        return display_address;
    }
}