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


import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MainActivity extends AppCompatActivity {

    // UI Widgets.
    private TextView mNextMarkTextView;
    private TextView mCourseTextView;
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mSpeedTextView;
    private TextView mSpeedUnitView;
    private TextView mHeadingTextView;
    private TextView mAccuracyTextView;
    private TextView mMarkLatitudeTextView;
    private TextView mMarkLongitudeTextView;
    private TextView mDistanceTextView;
    private TextView mDistanceUnitTextView;
    private TextView mBearingTextView;
    private TextView mDiscrepTextView;
    private TextView mTimeToMarkTextView;
    private TextView mTimeTextView;

    // Define the 'Marks' Array
    Marks theMarks = null;
    Courses theCourses = null;
    FinishLine theFinish = null;
    StartActivity theStart = null;

    // Define parameters of next mark
    double mSpeed;
    double mSpeed1;
    double mSpeed2;
    double mSpeed3;
    double mSmoothSpeed;
    double vmgToMark;
    String speedDisplay;
    int mHeading;
    int mHeading1;
    int mHeading2;
    int mHeading3;
    int mSmoothHeading;
    int negHeading;
    String displayHeading;
    String nextMark = "A Mark";
    String nextMarkFull;
    Location destMark;
    Double destMarkLat, destMarkLon;
    float distToMark;
    int bearingToMark;
    int displayBearingToMark;
    String distUnits;
    int rawVariance;
    int bearingVariance;
    boolean flagFinish = FALSE;

    float distDisplay;
    String displayDistToMark;
    float distanceToMark;
    long lastUpdateTime;
    long timeSinceLastUpdate;
    long timeToMark;
    long ttm1;
    long ttm2;
    long mSmoothTimeToMark;
    String ttmDisplay;
    long currentTime;
    String currentTimeDisplay;

    int posMark = 0;
    int posCourse = 0;
    int listMarkSize, listCourseSize;
    String raceCourse;
    ArrayList courseMarks;
    Bundle savedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ArrayList<Mark> marks = new ArrayList<>();
        ArrayList<Course> courses = new ArrayList<>();
        ArrayList courseMarks = new ArrayList();

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

        // Create theStart object here and pass in course, nextMark
        theStart = new StartActivity();

        // Create theFinish object here, and pass in 'A' Mark, and 'H' Mark
        String a = "A"; // Finish line data
        String h = "H"; // Finish Line Data
        Location aMark = theMarks.getNextMark(a);
        Location hMark = theMarks.getNextMark(h);
        // Should have A Mark, H Mark to create the Finish Line Object
        theFinish = new FinishLine(aMark, hMark);


        // Locate the UI widgets.
        mNextMarkTextView = (TextView) findViewById(R.id.next_mark_name);
        mCourseTextView = (TextView) findViewById(R.id.course_name);
//        mLatitudeTextView = (TextView) findViewById(R.id.latitude);
//        mLongitudeTextView = (TextView) findViewById(R.id.longitude);
        mSpeedTextView = (TextView) findViewById(R.id.speed_text);
//        mSpeedUnitView = (TextView) findViewById(R.id.speed_unit);
        mHeadingTextView = (TextView) findViewById(R.id.heading_text);
        mAccuracyTextView = (TextView) findViewById(R.id.accuracy_text);
//        mMarkLatitudeTextView = (TextView) findViewById(R.id.next_mark_lat);
//        mMarkLongitudeTextView = (TextView) findViewById(R.id.next_mark_lon);
        mDistanceTextView = (TextView) findViewById(R.id.distance_text);
        mDistanceUnitTextView = (TextView) findViewById(R.id.dist_unit);
        mBearingTextView = (TextView) findViewById(R.id.bearing_text);
        mDiscrepTextView = (TextView) findViewById(R.id.variance_text);
//        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mTimeToMarkTextView = (TextView) findViewById(R.id.time_to_mark);
        mTimeTextView = (TextView) findViewById(R.id.time_text);

    }

//    /**
//     * Updates fields based on data stored in the bundle.
//     *
//     * @param savedInstanceState The activity state saved in the Bundle.

    /**
     * This method is called when the + course button is pressed
     */
    public void next_course(View view) {
        {
            // Increment to the position of the next course on the list
            if (posCourse >= listCourseSize - 1) {
                posCourse = 0;
            } else
                posCourse = posCourse + 1;
        }
        setCourse();
        setNextMark();
    }

    public void previous_course(View view) {
        {
            // Decrement to the position of the previous course on the list
            if (posCourse <= 0) {
                posCourse = listCourseSize - 1;
            } else
                posCourse = posCourse - 1;
        }
        setCourse();
        setNextMark();
    }

    /**
     *  Set race course
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
        {
            // Increment to the position of the nMath.abs(ext mark on the list
            if (posMark >= listMarkSize - 1) {
                posMark = 0;
            } else
                posMark = posMark + 1;
        }
        setNextMark();
    }

    public void previous_mark(View view) {
        {
            // Decrement to the position of the previous mark on the list
            if (posMark <= 0) {
                posMark = listMarkSize - 1;
            } else
                posMark = posMark - 1;
        }
        setNextMark();
    }

    /**
     *  Set next destination mark
     */
    public void setNextMark() {
        if (raceCourse.equals("None")) {
            listMarkSize = theMarks.marks.size();
            nextMark = theMarks.marks.get(posMark).getmarkName();

        } else {
            listMarkSize = courseMarks.size();
            nextMark = (String) courseMarks.get(posMark);
        }

        if (nextMark.length() == 1){
            nextMarkFull = nextMark + " Mark";
        } else {
            nextMarkFull = nextMark;
        }

        mNextMarkTextView.setText(nextMarkFull);

        // Check to see if next mark is not the finish
        if (nextMark.equals("Finish")) {
            flagFinish = TRUE;
        } else {
         // Not the finish, set the next mark normally
        destMark = theMarks.getNextMark(nextMark);
        flagFinish = FALSE;
        updateUI();
        }
    }

    /**
     * Updates all UI fields.
     *///
    private void updateUI() {

        updateLocationUI();
    }

    private void openStartActivity() {
//        Intent start = new Intent(this, StartActivity.class);
//        startActivity(start);



    }

    /**Math.abs(
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (destMark == null) {
            setCourse();
            setNextMark();
        }

        if(nextMark.equals("Start")) {

//            // Create theStart object here and pass in course, nextMark
//            theStart = new StartActivity(raceCourse, nextMarkFull);

          setContentView(R.layout.activity_start);
            openStartActivity();

        }


            // Process gps data for display on UI
            // Get speed in m/s and smooth for 4 readings
            mSpeed3 = mSpeed2;
            mSpeed2 = mSpeed1;
            mSpeed1 = mSpeed;
//            mSpeed = mCurrentLocation.getSpeed();
            mSmoothSpeed = (mSpeed + mSpeed1 + mSpeed2 + mSpeed3)/4;
            // Convert to knots and display
            speedDisplay = new DecimalFormat( "##0.0").format( mSmoothSpeed * 1.943844); //convert to knots

            // Change heading to correct format and smooth
            mHeading3 = mHeading2;
            mHeading2 = mHeading1;
            mHeading1 = mHeading;
//            mHeading = (int) mCurrentLocation.getBearing();
            mSmoothHeading = (mHeading + mHeading1 + mHeading2 + mHeading3)/4;
            if(mSmoothHeading > 180) {
                negHeading = mSmoothHeading - 360;
            } else {
                negHeading = mSmoothHeading;
            }

            displayHeading = String.format("%03d", mSmoothHeading);

            // Change distance to mark to nautical miles if > 500m and correct formattring.format decimal places
//            distToMark = mCurrentLocation.distanceTo(destMark);

                // Use nautical miles when distToMark is >500m.
                if ( distToMark >500) {
                    displayDistToMark = new DecimalFormat("###0.00").format(distToMark / 1852);
                    distUnits = "NM";
                } else {
                    displayDistToMark = new DecimalFormat("###0").format(distToMark);
                    distUnits = "m";
                }

            // Get bearing to mark
//            bearingToMark = (int) mCurrentLocation.bearingTo(destMark);

                // Correct negative bearings

                if ( bearingToMark < 0) {
                    displayBearingToMark = bearingToMark + 360;
                } else {
                    displayBearingToMark = bearingToMark;
                }

            // Calculate discrepancy between heading and bearing to mark
            rawVariance = mSmoothHeading - displayBearingToMark;
            if (rawVariance < -180) {
                bearingVariance = rawVariance + 360;
            } else {
                if (rawVariance > 180) {
                    bearingVariance = rawVariance - 360;
                } else {
                    bearingVariance = rawVariance;
                }
            }

            // Get time since last update
//            lastUpdateTime = mCurrentLocation.getTime();
             currentTime = Calendar.getInstance().getTimeInMillis();
            timeSinceLastUpdate = (currentTime - lastUpdateTime)/1000;

            SimpleDateFormat time = new SimpleDateFormat("kkmm:ss");

//            currentTimeDisplay = java.text.DateFormat.getTimeInstance().format(new Date());
            currentTimeDisplay = time.format(currentTime);

            // Calc time to mark
            vmgToMark = Math.cos(Math.toRadians(bearingVariance)) * mSmoothSpeed;

            ttm2 = ttm1;
            ttm1 = timeToMark;
            timeToMark = (long) (distToMark / vmgToMark);
            mSmoothTimeToMark = timeToMark ;

            // Keep displayed figure below 100 hours 360000 secs.
            if (mSmoothTimeToMark < 360000 && mSmoothTimeToMark > 0) {


                if (mSmoothTimeToMark > 3559) {
                    ttmDisplay = String.format("%02dh %02d' %02d\"",
                            TimeUnit.SECONDS.toHours(mSmoothTimeToMark),
                            TimeUnit.SECONDS.toMinutes(mSmoothTimeToMark) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(mSmoothTimeToMark)),
                            TimeUnit.SECONDS.toSeconds(mSmoothTimeToMark) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(mSmoothTimeToMark)));
                } else {
                    ttmDisplay = String.format("%02d' %02d\"",
                            TimeUnit.SECONDS.toMinutes(mSmoothTimeToMark) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(mSmoothTimeToMark)),
                            TimeUnit.SECONDS.toSeconds(mSmoothTimeToMark) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(mSmoothTimeToMark)));
                }
            }else {
                ttmDisplay = "--h --' --\"";
            }

        // Send info to UI
//            mLatitudeTextView.setText(mLatitudeLabel + ": " + mCurrentLocation.getLatitude());
//            mLongitudeTextView.setText(mLongitudeLabel + ": " + mCurrentLocation.getLongitude());
            mSpeedTextView.setText(speedDisplay);
//            mSpeedUnitView.setText("kts");
            mHeadingTextView.setText(displayHeading);
//            mAccuracyTextView.setText(String.valueOf((int) mCurrentLocation.getAccuracy()) + " m");
//            mMarkLatitudeTextView.setText("Mark " + mLatitudeLabel + ": " + String.format("%.4f", destMark.getLatitude()));
//            mMarkLongitudeTextView.setText("Mark " + mLongitudeLabel + ": " + String.format("%.4f", destMark.getLongitude()));
            mDistanceTextView.setText(displayDistToMark);
            mDistanceUnitTextView.setText(distUnits);
            mBearingTextView.setText(String.format("%03d", displayBearingToMark));
            mDiscrepTextView.setText(String.format("%03d", bearingVariance));
            if ( bearingVariance < -2) {
                mDiscrepTextView.setTextColor(getResources().getColor(R.color.app_red));
            }
            if ( bearingVariance > 2) {
                mDiscrepTextView.setTextColor(getResources().getColor(R.color.app_green));
            }
//            mLastUpdateTimeTextView.setText(mLastUpdateTimeLabel + ": " + timeSinceLastUpdate);updateValuesFromBundle
            mTimeToMarkTextView.setText(ttmDisplay);
            mTimeTextView.setText(currentTimeDisplay);
//        }
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
                doubleBackToExitPressedOnce=false;
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


