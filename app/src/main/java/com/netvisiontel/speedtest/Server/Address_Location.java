package com.netvisiontel.speedtest.Server;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by ngodi on 2/22/2016.
 */
public class Address_Location {
    private static final String TAG = "Address_Location";

    public static String getCountry(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
        String result = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                return address.getCountryName();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        }
        return null;

    }
}
