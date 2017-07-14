package com.pyaephyo.loctracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import com.google.android.gms.location.Geofence;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;

public class MainActivity extends AppCompatActivity implements OnLocationUpdatedListener {

    TextView mLatitude;
    TextView mLongitude;
    TextView mTime;
    Button btnShowMap;
    LocationGooglePlayServicesProvider provider;

    Location mLocation;
    LocationParams mParams;

    String mDeviceID;

    SupportMapFragment mMap;

    private static final int LOCATION_PERMISSION_ID = 1001;

    private DatabaseReference mFireBaseRef;
    private DatabaseReference mCurrentLocation;
    private DatabaseReference mLocationHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Get permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }

        initializeControls();
        initializeEvents();

        // Start Location
        startLocation();

    }


    private void initializeEvents(){
        //Initialize Events
        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });
    }

    private  void initializeControls(){
        // Get Device ID for Multi device support
        mDeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        setTitle(mDeviceID);

        // Initialize FireBase
        mFireBaseRef = FirebaseDatabase.getInstance().getReference();

        // Initialize Controls
        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);
        mTime = (TextView) findViewById(R.id.time);
        btnShowMap = (Button) findViewById(R.id.btn_show_map);

        // Create location config
        mParams = new LocationParams.Builder()
                .setAccuracy(LocationAccuracy.HIGH)
                .setDistance(0)
                .setInterval(5000)
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocation();
    }

    @Override
    public void onLocationUpdated(Location location) {
        if (location != null) {
            pushToCloudAndDisplayLog(location);
        }
    }



    private void pushToCloudAndDisplayLog(Location location) {
        String lat = mLatitude.getText() + System.getProperty("line.separator");
        String lon = mLongitude.getText() + System.getProperty("line.separator");
        String time = mTime.getText() + System.getProperty("line.separator");

        Long tsLong = System.currentTimeMillis() / 1000;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
        String formattedTime = simpleDateFormat.format(new Date());

        mLatitude.setText(lat + Double.toString(location.getLatitude()));
        mLongitude.setText(lon + Double.toString(location.getLongitude()));
        mTime.setText(time + formattedTime);

        // Push on Cloud
        mCurrentLocation = mFireBaseRef.child("CurrentLocation");
        mLocationHistory = mFireBaseRef.child("LocationHistory");

        mCurrentLocation.setValue(location);
        mLocationHistory.child(formattedTime).setValue(location);

    }

    private void locationServiceUnavailable() {
        // TODO Do something when location service is unavailable
    }


    private void startLocation() {
        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);
        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.location(provider).config(mParams).start(this);
    }

    private void stopLocation() {
        SmartLocation.with(this).location().stop();
    }

}
