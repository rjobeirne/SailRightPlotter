package com.sail.sailright2new;

/*
  Copyright 2017 Google Inc. All Rights Reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, softwareNext Mark
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 1;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    // Location request is a config file for all settings related to fusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;

    // UI Widgets.
    private TextView mNextMarkTextView;
    private TextView mCourseTextView;
    private TextView mSpeedTextView;
    private TextView mHeadingTextView;
    private TextView mAccuracyTextView;
    private TextView mDistanceTextView;
    private TextView mDistanceUnitTextView;
    private TextView mBearingTextView;
    private TextView mDiscrepTextView;
    private TextView mTimeToMarkTextView;
    private TextView mTimeTextView;

    // Define the 'Marks' and 'Courses' ArraysBoat
    Marks theMarks = null;
    Courses theCourses = null;

    // Define the other classes
    FinishLine theFinish = null;
    StartActivity theStart = null;
    Calculator theCalculator = null;

    // Define parameters of next mark
    double mSpeed;
    double mSmoothSpeed;
    String speedDisplay;
    int mHeading;
    int mSmoothHeading;
    int negHeading;
    String displayHeading;
    String nextMark;
    String nextMarkFull;
    Location destMark;
    Double destMarkLat, destMarkLon;
    float distToMark;
    int bearingToMark;
    int displayBearingToMark;
    String distUnits;
    String finMark = "race";
    int bearingVariance;
    boolean flagFinish = FALSE;
    boolean flagStart = FALSE;

    String displayDistToMark;
    String ttmDisplay;
    String accuracy;

    int posMark = 0;
    int posCourse = 0;
    int listMarkSize, listCourseSize;
    String lastMarkName = null;
    String raceCourse;
    ArrayList courseMarks;
    Bundle savedInstanceState;

    int directionFactor;
    Location aMark, hMark, lastMark, finishPoint;
    Double distToFinish;

    String a = "A"; // Finish line data
    String h = "H"; // Finish Line Data

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ArrayList<Mark> marks = new ArrayList<>();
        ArrayList<Course> courses = new ArrayList<>();
        ArrayList courseMarks = new ArrayList();

        // first check for runtime permission
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int grant = ContextCompat.checkSelfPermission(this, permission);

        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
        //Create the ArrayList object here, for use in all the MainActivity
        theMarks = new Marks();
        theCourses = new Courses();

        // Create the ArrayList in the constructor, so only done once
        try {
            theMarks.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Load all courses
        try {
            theCourses.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        // Create theStart object here and pass in course, nextMark
//        theStart = new StartActivity();

        // Create theFinish object here, and pass in 'A' Mark, and 'H' Mark
//        String a = "A"; // Finish line data
//        String h = "H"; // Finish Line Data
//        Location aMark = theMarks.getNextMark(a);
//        Location hMark = theMarks.getNextMark(h);
//        Location lastMark = theMarks.getNextMark(lastMarkName);
        // Should have A Mark, H Mark to create the Finish Line Object
//        theFinish = new FinishLine(aMark, hMark, lastMark);

        // Create theCalculator object for processing data readings
        theCalculator = new Calculator();

        // Locate the UI widgets.
        mNextMarkTextView = findViewById(R.id.next_mark_name);
        mCourseTextView = findViewById(R.id.course_name);
        mSpeedTextView = findViewById(R.id.speed_text);
        mHeadingTextView = findViewById(R.id.heading_text);
        mAccuracyTextView = findViewById(R.id.accuracy_text);
        mDistanceTextView = findViewById(R.id.distance_text);
        mDistanceUnitTextView = findViewById(R.id.dist_unit);
        mBearingTextView = findViewById(R.id.bearing_text);
        mDiscrepTextView = findViewById(R.id.variance_text);
        mTimeToMarkTextView = findViewById(R.id.time_to_mark);
        mTimeTextView = findViewById(R.id.time_text);

        // set all properties of LocationRequest
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save the location
                Location mCurrentLocation = locationResult.getLastLocation();
                updateLocationData(mCurrentLocation);
            }
        };

        // Locate Start button
        mNextMarkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nextMark.equals("Start")) {
                    // Create theStart object here and pass in course, nextMark
                    theStart = new StartActivity();
        Location lastMark = theMarks.getNextMark(lastMarkName);
                    openStartActivity();
                    flagStart = TRUE;
                }
            }
        });

        updateGPS();
        startLocationUpdates();

//        updateLocationData();
    } // end onCreate method

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "This app requires permission to be granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS() {
        // get permissions from the user to track GPS
        // get the current location from the fused client

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location mCurrentLocation) {
                    // we got permissions. Put the values of location. XXX into the UI components.
                    updateLocationData(mCurrentLocation);
                }
            });
        } else {
            // permission yet to be granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }


    /**
     * This method is called when the + course button is pressed
     */
    public void next_course(View view) {
        // Increment to the position of the next course on the list
        if (posCourse >= listCourseSize - 1) {
            posCourse = 0;
        } else {
            posCourse = posCourse + 1;
        }
        setCourse();
        setNextMark();
    }

    public void previous_course(View view) {
        // Decrement to the position of the previous course on the list
        if (posCourse <= 0) {
            posCourse = listCourseSize - 1;
        } else {
            posCourse = posCourse - 1;
        }
        setCourse();
        setNextMark();
    }

    /**
     * Set race course
     */
    public void setCourse() {

        listCourseSize = theCourses.courses.size();
        raceCourse = theCourses.courses.get(posCourse).getCourseName();
        courseMarks = theCourses.getCourse(raceCourse);

        mCourseTextView.setText(raceCourse);
    }

    /**
     * This method is called when the + button is pressed
     */
    public void next_mark(View view) {
        // Increment to the position of the nMath.abs(ext mark on the list
        if (posMark >= listMarkSize - 1) {
            posMark = 0;
        } else {
            posMark = posMark + 1;
        }
        flagFinish = FALSE;
        setNextMark();
    }

    public void previous_mark(View view) {
        // Decrement to the position of the previous mark on the list
        if (posMark <= 0) {
            posMark = listMarkSize - 1;
        } else {
            posMark = posMark - 1;
        }
        flagFinish = FALSE;
        setNextMark();
    }

    /**
     * Set next destination mark
     */
    public void setNextMark() {
        if (!flagFinish) {

            if (raceCourse.equals("None")) {
                listMarkSize = theMarks.marks.size();
                nextMark = theMarks.marks.get(posMark).getmarkName();

            } else {
                listMarkSize = courseMarks.size();
                nextMark = (String) courseMarks.get(posMark);
            }

            if (nextMark.length() == 1) {
                nextMarkFull = nextMark + " Mark";
            } else {
                nextMarkFull = nextMark;
            }

            if (nextMark.equals("Start")) {
                mNextMarkTextView.setTextColor(getResources().getColor(R.color.red));
                mNextMarkTextView.setBackgroundColor(getResources().getColor(R.color.button_background));
                mNextMarkTextView.setTypeface(mNextMarkTextView.getTypeface(), Typeface.BOLD);
            } else {
                mNextMarkTextView.setTypeface(mNextMarkTextView.getTypeface(), Typeface.ITALIC);
                mNextMarkTextView.setTextColor(getResources().getColor(R.color.normal_text));
                mNextMarkTextView.setBackgroundColor(getResources().getColor(R.color.white));
            }
            mNextMarkTextView.setText(nextMarkFull);

            // Check to see if next mark is not the finish
            if (nextMark.equals("Finish")) {
                // Identify the last mark to determine the direction of approach
                flagFinish = TRUE;
                lastMarkName = (String) courseMarks.get(listMarkSize - 2);
                Log.e("lastMarkName", lastMarkName);

                // Should have A Mark, H Mark to create the Finish Line Object
                aMark = theMarks.getNextMark(a);
                hMark = theMarks.getNextMark(h);
                lastMark = theMarks.getNextMark(lastMarkName);
                theFinish = new FinishLine(aMark, hMark, lastMark);

                // Find the direction of approach to the finish line
                directionFactor = theFinish.getFinishDirection();
            } else {
                // Not the finish, set the next mark normally
                destMark = theMarks.getNextMark(nextMark);
                flagFinish = FALSE;
            }
            updateLocationUI();
        }
    }

    public void openStartActivity() {
        Intent start = new Intent(this, StartActivity.class);
        start.putExtra("course", raceCourse);
        start.putExtra("mark", nextMark);
        startActivity(start);
    }

    /**
     * Calculates all the navigational data
     */
    private void updateLocationData(Location mCurrentLocation) {
        if (destMark == null) {
            setCourse();
            setNextMark();
        }

        if (flagStart) {
            posMark = 1;
            flagStart = FALSE;
            setNextMark();
        }

        if (flagFinish) {
            // Find the the target point on the finish line (A Mark, H Mark or Line)
            // Pass in the currentLocation
            finMark = theFinish.getFinishTarget(mCurrentLocation);

            if (finMark.equals("Line")) {
                // Insert the finish line crossing point
                mNextMarkTextView.setText(finMark);
                destMark = theFinish.getFinishPoint(mCurrentLocation);
                finishPoint = destMark;
            } else {
                // Set the next mark to either A or H
                mNextMarkTextView.setText("Fin - " + finMark + " Mark");
                destMark = theMarks.getNextMark(finMark);
            }
        }

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

            if (distToMark < 50 && finMark.equals("race")) {
                posMark = posMark + 1;
                setNextMark();
                playSounds("klaxon");
            }

            if (flagFinish && finishPoint != null) {
            // Calculate distance in metres to finish point from latitude
            distToFinish = (mCurrentLocation.getLatitude() - finishPoint.getLatitude()) * directionFactor * 60 * 1852;
            displayDistToMark = new DecimalFormat("###0").format(distToFinish);
            Log.e("boat, fin", mCurrentLocation.getLatitude() + ", " + finishPoint.getLatitude());
            Log.e("dist to fin", String.valueOf(distToFinish));
                if (distToFinish < 10) {
                    playSounds("whoop");
                    mNextMarkTextView.setText("** FINISHED **");
                    flagFinish = FALSE;
                    posMark = 0;
                }
            }

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
        mDiscrepTextView.setText(String.format("%03d", bearingVariance));
        if (bearingVariance < -2) {
            mDiscrepTextView.setTextColor(getResources().getColor(R.color.app_red));
        }
        if (bearingVariance > 2) {
    String lastMarkName = null;
            mDiscrepTextView.setTextColor(getResources().getColor(R.color.app_green));
        }
        mTimeToMarkTextView.setText(ttmDisplay);
        mAccuracyTextView.setText(accuracy);
    }

    public void playSounds(String sound) {
        if (sound == "klaxon") {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.klaxon);
            mediaPlayer.start();
        }
        if (sound == "shotgun") {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.shotgun);
            mediaPlayer.start();
        }
        if (sound == "whoop") {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.whoop);
            mediaPlayer.start();
        }
    }

    // Add double click to exit
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}


