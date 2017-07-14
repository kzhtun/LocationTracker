package com.pyaephyo.loctracking;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseDatabase mFireBaseInstance;
    private DatabaseReference mFireBaseRef;
    private DatabaseReference mCurrentLocation;
    Marker now;

    static String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
        MapsActivity.this.startService(serviceIntent);


        mFireBaseInstance = FirebaseDatabase.getInstance();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showTrackOnMap(googleMap);
    }

    private void showTrackOnMap(final GoogleMap googleMap) {
        mCurrentLocation = mFireBaseInstance.getReference("CurrentLocation");

        mCurrentLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("KZHTUN", dataSnapshot.getValue().toString());

                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                double latitude = (double) map.get("latitude");
                double longitude = (double) map.get("longitude");

                // Location location =  dataSnapshot.getValue(Location.class);

                if (now != null) {
                    now.remove();
                }

                // Creating a LatLng object for the current location
                LatLng latLng = new LatLng(latitude, longitude);
                now = googleMap.addMarker(new MarkerOptions().position(latLng));

                // Showing the current location in Google Map
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom in the Google Map
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    private String getAddress(LatLng latLng) {
//        Location location = new Location("myProvider");
//
//        location.setLatitude(latLng.latitude);
//        location.setLatitude(latLng.longitude);
//
//        final String text = String.format("Latitude %.6f, Longitude %.6f",
//                location.getLatitude(),
//                location.getLongitude());
//
//
//
//        // We are going to get the address for the current position
//        SmartLocation.with(this).geocoding().reverse(location, new OnReverseGeocodingListener() {
//            @Override
//            public void onAddressResolved(Location original, List<Address> results) {
//                if (results.size() > 0) {
//                    Address result = results.get(0);
//                    StringBuilder builder = new StringBuilder(text);
//                    builder.append("\n[Reverse Geocoding] ");
//                    List<String> addressElements = new ArrayList<>();
//                    for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
//                        addressElements.add(result.getAddressLine(i));
//                    }
//
//                    builder.append(TextUtils.join(", ", addressElements));
//
//                    address = builder.toString();
//
//                }
//            }
//        });
//
//        return address;
//
//    }
}
