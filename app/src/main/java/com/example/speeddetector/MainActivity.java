package com.example.speeddetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener{

    float lastSpeed ; //keeps last speed
    long maxid=0; //tracks number of data in firebase
    TextView speed; //shows current speed
    TextView accelerate;
    TextView tv_email;
    Data data;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid(); //get user's code
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference reff= db.getReference();
    Button seeOnMap;
    Button logout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        data =new Data();
        lastSpeed = 0;
        tv_email=findViewById(R.id.email);
        logout = findViewById(R.id.logout);
        speed = findViewById(R.id.speed);
        accelerate = findViewById(R.id.accelerate);
        tv_email=findViewById(R.id.email);
        tv_email.setText(user.getEmail());
        speed.setText(0.00 + " km/h");


        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        //Count data in firebasase
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

        //Logout
        logout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));

        });

        //Open map
        seeOnMap= findViewById(R.id.map);
        seeOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity((new Intent(MainActivity.this, MapActivity.class)));
            }
        });

        seeOnMap.setOnClickListener(view -> {

            startActivity(new Intent(MainActivity.this, MapActivity.class));

        });
        //check for GPS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            // if the permission is granted
            doStuff();
        }


        this.updateSpeed(null);

    }

    //Every time the location is changed
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            locationFunctions myLocation = new locationFunctions(location);
            this.updateSpeed(myLocation);
        }
    }
    public void doStuff() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }
        Toast.makeText(this, "waiting for GPS connection!", Toast.LENGTH_SHORT).show();
    }

    //We take the last speed and the current speed to find the acceleration
    private void updateSpeed(locationFunctions location) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //keep the old speed
        float nCurrentSpeed = lastSpeed;

        //if location has changed
        if (location != null) {

            nCurrentSpeed = location.getSpeed();
        }
        //just for looking better
        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");

        speed.setText(strCurrentSpeed + " km/h");
        DecimalFormat df = new DecimalFormat("#.0000");
        DecimalFormat df2 = new DecimalFormat("#.00");

        //acceleration
        if (nCurrentSpeed - lastSpeed >20) {
            data.setLocationX(String.valueOf(df.format(location.getLatitude())));
            data.setLocationY(String.valueOf(df.format(location.getLongitude())));
            data.setLastSpeed(df2.format(lastSpeed));
            data.setAcceleration(String.valueOf( df2.format(nCurrentSpeed - lastSpeed)));
            data.setTimestamp(timestamp.toString());
            data.setCategory("acceleration");
            data.setId(uid);
            reff.child(String.valueOf(maxid+1)).setValue(data);//make a new child in firebase to store data
            Toast.makeText(MainActivity.this,"Acceleration was detected!", Toast.LENGTH_SHORT).show();

            //braking
        } else if (nCurrentSpeed - lastSpeed < -20) {

            data.setLocationX(String.valueOf(df.format(location.getLatitude())));
            data.setLocationY(String.valueOf(df.format(location.getLongitude())));
            data.setLastSpeed(df2.format(lastSpeed));
           data.setAcceleration(String.valueOf( df2.format(nCurrentSpeed - lastSpeed)));
            data.setTimestamp(timestamp.toString());
            data.setCategory("braking");
            data.setId(uid);
            reff.child(String.valueOf(maxid+1)).setValue(data); //make a new child in firebase to store data
            Toast.makeText(MainActivity.this,"Braking was detected!", Toast.LENGTH_SHORT).show();
        }else{
        }
        //update last speed
        lastSpeed=nCurrentSpeed;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        if(requestCode == 1000){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff();


            }else {
                finish();
            }
        }
    }
    //if the user is not logged in
    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity((new Intent(MainActivity.this, Login.class)));
        }

    }




}