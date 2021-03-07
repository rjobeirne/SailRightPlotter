package com.sail.sailright2new;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class StartActivity extends AppCompatActivity {

    // Location request is a config file for all settings related to fusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;



    TextView startCourseTextView, startNextMarkTextView;
    TextView startSpeedTextView;
    boolean flagKillStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Intent intent = getIntent();
        String startCourse = intent.getStringExtra("course");
        String startMark = intent.getStringExtra("mark");

        // Locate the UI widgets.
        startCourseTextView = (TextView) findViewById(R.id.start_course_name);
        startNextMarkTextView = (TextView) findViewById(R.id.start_next_mark_name);
        startSpeedTextView = (TextView) findViewById(R.id.start_speed_text);

        //Locate stop button
        TextView killStart = findViewById(R.id.stopStart);
        killStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

// set all properties of LocationRequest
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        StartDisplay(startCourse, startMark);
        updateGPS();
        startLocationUpdates();
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    }

    private void updateGPS() {
        // get permissions from the user to track GPS
        // get the current location from the fused client

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(StartActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED) {
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // we got permissions. Put the values of location. XXX into the UI components.
                    updateUIValues(location);
                }
            });
        }
    }


    public void StartDisplay(String startCourse, String  startMark) {

        startCourseTextView.setText(startCourse);
        startNextMarkTextView.setText(startMark);

    }

    public void updateUIValues(Location location) {

        String displaySpeed = new DecimalFormat("##0.0").format(location.getSpeed());

        // Send info to UI
        startSpeedTextView.setText(displaySpeed);



    }
}
