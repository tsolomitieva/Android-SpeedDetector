package com.example.speeddetector;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    long maxid=0; // number of data in firebase
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reff= database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize view
        setContentView(R.layout.activity_map);
        //initialize map fragment
        SupportMapFragment mapfr = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapfr.getMapAsync(MapActivity.this);

    }
    //when map is opened
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Query downloadData = reff;

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {     maxid=snapshot.getChildrenCount();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        map=googleMap;
        downloadData.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) { //for all data
                        for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                            //get location
                            String locationX = childDataSnapshot.child("locationX").getValue().toString();
                            String locationY = childDataSnapshot.child("locationY").getValue().toString();
                            double lat = Double.parseDouble(locationX);
                            double lon = Double.parseDouble(locationY);

                            //get category
                            String category = childDataSnapshot.child("category").getValue().toString();

                            //get speed before accelaration or braking
                            String lspeed = childDataSnapshot.child("lastSpeed").getValue().toString();

                            //get acceleration or braking
                            String acceleration = childDataSnapshot.child("acceleration").getValue().toString();

                            //get timestamp
                            String timestamp = childDataSnapshot.child("timestamp").getValue().toString();

                            //get user
                            String user = childDataSnapshot.child("id").getValue().toString();


                            LatLng name;
                            name = new LatLng(lat, lon);
                            DecimalFormat df2 = new DecimalFormat("#.00");
                            //acceleration
                            if(category.equals("acceleration")){
                            map.addMarker(new MarkerOptions()
                                    .position(name)
                                    .title(category)
                                    .snippet("Last speed: "+lspeed+" Acceleration: "+acceleration +" timestamp: "+timestamp+ " user: "+user )
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(name,15f));
                        }else{
                                //braking
                                map.addMarker(new MarkerOptions()
                                        .position(name)
                                        .title(category)
                                        .snippet("Last speed: "+lspeed+"  Braking: "+ acceleration + "timestamp: "+timestamp+ "user: "+user)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(name,15f));
                            }
                        }
                    }
                }

                        @Override
                        public void onCancelled (@NonNull DatabaseError error){

                        }

            });


    }
}