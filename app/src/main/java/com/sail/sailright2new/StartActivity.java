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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class StartActivity extends AppCompatActivity {

    // Location request is a config file for all settings related to fusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;

    // Define the classes
    Marks theMarks;
    FinishLine theLine;
    Calculator theCalculator = null;

    TextView startCourseTextView, startNextMarkTextView;
    TextView mSpeedTextView, mHeadingTextView, mDistanceTextView, mDistanceUnitTextView;
    TextView mBearingTextView, mDiscrepTextView,mTimeToMarkTextView, mAccuracyTextView;

    // Define variables
    String startMark = "A";
    Location destMark;
    double mSpeed, mSmoothSpeed;
    String speedDisplay, displayHeading;
    int mHeading, mSmoothHeading, negHeading;
    double distToMark;
    String displayDistToMark, distUnits;
    int bearingToMark, displayBearingToMark, bearingVariance;
    String ttmDisplay, accuracy;

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
        mSpeedTextView = (TextView) findViewById(R.id.speed_text);
        mHeadingTextView = (TextView) findViewById(R.id.heading_text);
        mDistanceTextView = (TextView) findViewById(R.id.distance_text);
        mDistanceUnitTextView = (TextView) findViewById(R.id.dist_unit);
        mBearingTextView = (TextView) findViewById(R.id.bearing_text);
        mDiscrepTextView = (TextView) findViewById(R.id.variance_text);
        mTimeToMarkTextView = (TextView) findViewById(R.id.time_to_line);
        mAccuracyTextView = (TextView) findViewById(R.id.accuracy_text);


        //Create the ArrayList object here, for use in all the MainActivity
        theMarks = new Marks();

        // Create the ArrayList in the constructor, so only done once
        try {
            theMarks.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create theFinish object here, and pass in 'A' Mark, and 'H' Mark
        String a = "A"; // Finish line data
        String h = "H"; // Finish Line Data
        Location aMark = theMarks.getNextMark(a);
        Location hMark = theMarks.getNextMark(h);
        // Should have A Mark, H Mark to create the Finish Line Object
        theLine = new FinishLine(aMark, hMark);

        // Create theCalculator object for processing data readings
        theCalculator = new Calculator();

        //Locate stop button
        TextView killStart = findViewById(R.id.stop_start);
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
                updateLocationData(location);
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
                    updateLocationData(location);
                }
            });
        }
    }

    /**
     *  Set next destination mark
     */
    public void switchStartMark(View view) {
        if (startMark.equals("A")) {
            startMark =  "H";
        } else {
            startMark = "A";
        }
        destMark = theMarks.getNextMark(startMark);
        startNextMarkTextView.setText(startMark + "  Mark");
    }

    public void StartDisplay(String startCourse, String  startMark) {

        startCourseTextView.setText(startCourse);
        startNextMarkTextView.setText(startMark);
    }

    /**
     * Calculates all the navigational data
     */
    private void updateLocationData(Location mCurrentLocation) {
        if (mCurrentLocation != null) {
            // Process gps data for display on UI
            mSpeed = mCurrentLocation.getSpeed();
            mSmoothSpeed = theCalculator.getSmoothSpeed(mSpeed);
            // Convert to knots and display
            speedDisplay = new DecimalFormat("##0.0").format(mSmoothSpeed * 1.943844); //convert to knots

            // Change heading to correct format and smooth
            mHeading = (int) mCurrentLocation.getBearing();
            mSmoothHeading = theCalculator.getSmoothHeading(mHeading);
            // Calc negHeading +/- from
            negHeading = theCalculator.getNegHeading();

            displayHeading = String.format("%03d", mSmoothHeading);

            // Change distance to mark to nautical miles if > 500m and correct formatting.format decimal places
            // TODO define startMark
            destMark = theMarks.getNextMark(startMark);
            distToMark = mCurrentLocation.distanceTo(destMark);

            // Use nautical miles when distToMark is >500m.
            displayDistToMark = theCalculator.getDistScale(distToMark);
            distUnits = theCalculator.getDistUnit(distToMark);

            // Get bearing to mark and correct negative bearings
            bearingToMark = (int) mCurrentLocation.bearingTo(destMark);
            displayBearingToMark = theCalculator.getCorrectedBearingToMark(bearingToMark);

            // Calculate discrepancy between heading and bearing to mark
            bearingVariance = theCalculator.getVariance();

            // Calc time to mark
            ttmDisplay = theCalculator.getTimeToMark(distToMark);

            // Get GPS accuracy
            accuracy = new DecimalFormat("###0").format(mCurrentLocation.getAccuracy()) + " m";

            updateLocationUI();
        }
    }

    private void updateLocationUI() {
        // Send info to UI
        mSpeedTextView.setText(speedDisplay);
        mHeadingTextView.setText(displayHeading);
        mDistanceTextView.setText(displayDistToMark);
        mDistanceUnitTextView.setText(distUnits);
        mBearingTextView.setText(String.format("%03d", displayBearingToMark));
//        mDiscrepTextView.setText(String.format("%03d", bearingVariance));
//        if ( bearingVariance < -2) {
//            mDiscrepTextView.setTextColor(getResources().getColor(R.color.app_red));
//        }
//        if ( bearingVariance > 2) {
//            mDiscrepTextView.setTextColor(getResources().getColor(R.color.app_green));
//        }
        mTimeToMarkTextView.setText(ttmDisplay);
        mAccuracyTextView.setText(accuracy);
    }

}
