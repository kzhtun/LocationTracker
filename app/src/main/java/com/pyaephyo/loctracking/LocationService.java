package com.pyaephyo.loctracking;

import android.*;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

/**
 * Created by KZHTUN on 7/14/2017.
 */


public class LocationService extends Service implements OnLocationUpdatedListener {

    LocationGooglePlayServicesProvider provider;
    Location mLocation;
    LocationParams mParams;
    private static final int LOCATION_PERMISSION_ID = 1001;

    private DatabaseReference mFireBaseRef;
    private DatabaseReference mCurrentLocation;
    private DatabaseReference mLocationHistory;

    @Override
    public void onCreate() {
        super.onCreate();

//        // Get permissions
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
//            return;
//        }

        // Create location config
        mParams = new LocationParams.Builder()
                .setAccuracy(LocationAccuracy.HIGH)
                .setDistance(0)
                .setInterval(5000)
                .build();

        // Initialize FireBase
        mFireBaseRef = FirebaseDatabase.getInstance().getReference();

        startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationUpdated(Location location) {
        if (location != null) {
            pushToCloud(location);
        }
    }

    private void startLocation() {
        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);
        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.location(provider).config(mParams).start(this);
    }

    private void stopLocation() {
        SmartLocation.with(this).location().stop();
        SmartLocation.with(this).geocoding().stop();
    }

    private void locationServiceUnavailable() {
        // TODO Do something when location service is unavailable
    }


    private void pushToCloud(Location location) {
//        String lat = mLatitude.getText() + System.getProperty("line.separator");
//        String lon = mLongitude.getText() + System.getProperty("line.separator");
//        String time = mTime.getText() + System.getProperty("line.separator");

        Long tsLong = System.currentTimeMillis() / 1000;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
        String formattedTime = simpleDateFormat.format(new Date());

        // Push on Cloud
        mCurrentLocation = mFireBaseRef.child("CurrentLocation");
        mLocationHistory = mFireBaseRef.child("LocationHistory");

        mCurrentLocation.setValue(location);
        mLocationHistory.child(formattedTime).setValue(location);

//        mLatitude.setText(lat + Double.toString(location.getLatitude()));
//        mLongitude.setText(lon + Double.toString(location.getLongitude()));
//        mTime.setText(time + formattedTime);



    }
}
