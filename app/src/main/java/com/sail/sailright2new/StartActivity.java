package com.sail.sailright2new;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.concurrent.TimeUnit;

public class StartActivity extends AppCompatActivity {

    // Location request is a config file for all settings related to fusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;

    // Define the classes
    Marks theMarks;
    StartLine theLine;
    Calculator theCalculator = null;
    Sounds theSounds;

    TextView startCourseTextView, startNextMarkTextView;
    TextView mSpeedTextView, mHeadingTextView, mDistanceTextView, mDistanceUnitTextView;
    TextView mBearingTextView, mTimeVarianceTextView, mEarlyLateTextView;
    TextView mTimeToMarkTextView, mAccuracyTextView;
    TextView mClockTextView;

    // Define variables
    Location destMark, startPoint;
    String startMark = "A";
    String speedDisplay, displayHeading;
    String displayDistToMark, distUnits;
    String ttmDisplay, displayTimeVariance, timeliness, accuracy;
    int mHeading, mSmoothHeading, negHeading;
    int directionFactor;
    int bearingToMark, displayBearingToMark;
    double mSpeed, mSmoothSpeed;
    double distToMark;
    double approachAngle, distToStart, distToDevice;

    // Define clock variables
    long timeRemain = 75;
    long timeToStart = 15 * 60;
    public Boolean timerStarted = false;
    Boolean resetClock = false;
    CountDownTimer startClock;
    private String clockDisplay;
    double secsLeft;
    String clockControl = "Go";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Intent intent = getIntent();
        String startCourse = intent.getStringExtra("course");
        String firstMarkName = intent.getStringExtra("first");

        // Locate the UI widgets.
        startCourseTextView = (TextView) findViewById(R.id.start_course_name);
        startNextMarkTextView = (TextView) findViewById(R.id.start_next_mark_name);
        mSpeedTextView = (TextView) findViewById(R.id.speed_text);
        mHeadingTextView = (TextView) findViewById(R.id.heading_text);
        mDistanceTextView = (TextView) findViewById(R.id.distance_text);
        mDistanceUnitTextView = (TextView) findViewById(R.id.dist_unit);
        mBearingTextView = (TextView) findViewById(R.id.bearing_text);
        mTimeVarianceTextView = (TextView) findViewById(R.id.start_time_early_late);
        mEarlyLateTextView = (TextView) findViewById(R.id.start_time_early_late_title);
        mTimeToMarkTextView = (TextView) findViewById(R.id.time_to_line);
        mAccuracyTextView = (TextView) findViewById(R.id.accuracy_text);
        mClockTextView = (TextView) findViewById(R.id.time_to_start);

        //Create the ArrayList object here, for use in all the MainActivity
        theMarks = new Marks();

        // Create the ArrayList in the constructor, so only done once
        try {
            theMarks.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create the Start object here, and pass in 'A' Mark, and 'H' Mark
        String a = "A"; // Start line data
        String h = "H"; // Start Line Data
        Location aMark = theMarks.getNextMark(a);
        Location hMark = theMarks.getNextMark(h);
        Location firstMark = theMarks.getNextMark(firstMarkName);
        // Should have A Mark, H Mark to create the Start Line Object
        theLine = new StartLine(aMark, hMark, firstMark);

        // Create theCalculator object for processing data readings
        theCalculator = new Calculator();

        theSounds = new Sounds();

        //Locate stop button
        TextView killStart = findViewById(R.id.stop_start);
        killStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerStarted) {
                    stop_clock();
                }
                finish();
            }
        });

        // Locate start timer button
        Button mButton = (Button) findViewById(R.id.start_clock);
        mButton.setText(clockControl);
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start_clock(v);
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

        // Create the start line
        theLine = new StartLine(aMark, hMark, firstMark);

        StartDisplay(startCourse, startMark + " Mark");
        updateGPS();
        showClock(timeToStart);
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
     * Display course and start mark
     * @param startCourse
     * @param startMark
     */
    public void StartDisplay(String startCourse, String startMark) {
        startCourseTextView.setText(startCourse);
        startNextMarkTextView.setText(startMark);
    }

    /**
     * Calculates all the navigational data
     */
    private void updateLocationData(Location mCurrentLocation) {
        if (mCurrentLocation != null) {

            startMark = theLine.getStartTarget(mCurrentLocation);
                // Find the direction of approach to the finish line
                directionFactor = theLine.getStartDirection();

            if (startMark.equals("Line")) {
                // Insert the finish line crossing point
                startNextMarkTextView.setText(startMark);
                destMark = theLine.getStartPoint(mCurrentLocation);
                startPoint = destMark;
                approachAngle = Math.abs(theLine.getApproachAngle());
                distToDevice = 10 * Math.sin(Math.toRadians(approachAngle));
                distToStart = (mCurrentLocation.getLatitude() - startPoint.getLatitude()) * directionFactor * 60 * 1852;
            } else {
                // Set the next mark to either A or H
                startNextMarkTextView.setText("Start - " + startMark + " Mark");
                destMark = theMarks.getNextMark(startMark);
            }

        // Process gps data for display on UI
            mSpeed = mCurrentLocation.getSpeed();
            mSmoothSpeed = theCalculator.getSmoothSpeed(mSpeed);
            // Convert to knots and display
            speedDisplay = new DecimalFormat("##0.0").format(mSmoothSpeed * 1.943844); //convert to knots

            // Change heading to correct format and smooth
            mHeading = (int) mCurrentLocation.getBearing();
            mSmoothHeading = theCalculator.getSmoothHeading(mHeading);
            displayHeading = String.format("%03d", mSmoothHeading);

            // Calc negHeading +/- from North
            negHeading = theCalculator.getNegHeading();

            // Find distance to the start point
             distToMark = mCurrentLocation.distanceTo(destMark);

            // Use nautical miles when distToMark is >500m.
            displayDistToMark = theCalculator.getDistScale(distToMark);
            distUnits = theCalculator.getDistUnit(distToMark);

            // Get bearing to mark and correct negative bearings
            bearingToMark = (int) mCurrentLocation.bearingTo(destMark);
            displayBearingToMark = theCalculator.getCorrectedBearingToMark(bearingToMark);

            // Calc time to mark
            ttmDisplay = theCalculator.getTimeToMark(distToMark);

            // Calc discrepancy between time remaining and time to mark
            displayTimeVariance = theCalculator.getTimeVariance(timeRemain);
            timeliness = theCalculator.getStartTimeliness();

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
        mTimeVarianceTextView.setText(displayTimeVariance);
        mTimeToMarkTextView.setText(ttmDisplay);
        mAccuracyTextView.setText(accuracy);

        if (timeliness.equals("Late")) {
            mTimeVarianceTextView.setTextColor(getResources().getColor(R.color.app_red));
            mEarlyLateTextView.setText("Late");
            mEarlyLateTextView.setTextColor(getResources().getColor(R.color.app_red));
        }
        if (timeliness.equals("Early")) {
            mTimeVarianceTextView.setTextColor(getResources().getColor(R.color.app_green));
            mEarlyLateTextView.setText("Early");
            mEarlyLateTextView.setTextColor(getResources().getColor(R.color.app_green));
        }
    }

    public void time_plus(View view) {
        if (timerStarted) {
            resetClock = true;
            timeToStart = timeRemain + 60;
            countdown();
        } else {
            timeToStart = timeToStart + 60;
            showClock(timeToStart);
        }
    }

    public void time_minus(View view) {
        if (timeToStart > 0) {
            if (timerStarted) {
                resetClock = true;
                timeToStart = timeRemain - 60;
                countdown();
            } else {
                timeToStart = timeToStart - 60;
                showClock(timeToStart);
            }
        }
    }

    public void start_clock(View view) {
        Button mButton = (Button) findViewById(R.id.start_clock);
        if (timerStarted) {
            Toast.makeText(this, "Clock synchronised", Toast.LENGTH_SHORT).show();
            mButton.setBackgroundColor(Color.GREEN);
            sync_clock(view);
            showClock(timeToStart);
        } else {
            Toast.makeText(this, "Clock started", Toast.LENGTH_SHORT).show();
            mButton.setBackgroundColor(Color.YELLOW);
            clockControl = "Sync";
            countdown();
        }
        mButton.setText(clockControl);
    }

    public void countdown() {
        if (resetClock) {
            startClock.cancel();
        }
        timerStarted = true;
        startClock = new CountDownTimer(timeToStart * 1000, 1000) {
            public void onTick(long millisUntilStart) {
                timeRemain = (millisUntilStart) / 1000;
                showClock(timeRemain);
                secsLeft = (double) timeRemain;

                if (Math.round((secsLeft) / 60) * 60 == secsLeft) {
                    playSounds("air_horn");
                }
            }

            public void onFinish() {
                playSounds("shotgun");
                double timeToLine = theLine.getShortestDist() / mSmoothSpeed;
                long acceptableStart = 10;
                if (timeToLine > acceptableStart) {
                    playSounds("fail");
                }
                mClockTextView.setText("* GO ! *");
                finish();
            }
        }.start();
    }

    public void stop_clock() {
        startClock.cancel();
    }

    public void sync_clock(View view) {
        timeToStart = (long) Math.round((secsLeft) / 60) * 60;
        resetClock = true;
        countdown();
    }

    public void showClock(long timeRemain) {
        clockDisplay = String.format("%02d' %02d\"",
                TimeUnit.SECONDS.toMinutes(timeRemain) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeRemain)),
                TimeUnit.SECONDS.toSeconds(timeRemain) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeRemain)));

        mClockTextView.setText(clockDisplay);
    }

    public void playSounds(String sound) {
        if (sound == "air_horn") {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.air_horn);
            mediaPlayer.start();
        }
        if (sound == "shotgun") {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.shotgun);
            mediaPlayer.start();
        }
        if (sound == "fail") {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.fail);
            mediaPlayer.start();
        }
    }

    // Hide navigation and status bar
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
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY70 ;/
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
