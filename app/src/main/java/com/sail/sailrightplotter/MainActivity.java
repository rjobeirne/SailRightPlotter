package com.sail.sailrightplotter;

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
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 3;
    public static final int FAST_UPDATE_INTERVAL = 1;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    public static final  int LONG_PRESS_DELAY_MILLIS = 3000;

    // Location request is a config file for all settings related to fusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;
    File dir;

    // UI Widgets.
    private TextView mNextMarkTextView;
    private TextView mMarkExtraTextView;
    private TextView mCourseTextView;
    private TextView mSpeedTextView;
    private TextView mHeadingTextView;
    private TextView mAccuracyTextView;
    private TextView mDistanceTextView;
    private TextView mDistanceUnitTextView;
    private TextView mBearingTextView;
    private TextView mDiscrepTextView;
    private TextView mTimeToMarkTextView;
    private TextView mCourseDistTextView;
    private ImageButton settingsBtn;
    private TextClock mClock;
    private TextView mKeepTextView;
    private TextView mCourseListTextView;
    private TextView mPowerWarning;

    // Define the 'Marks' and 'Courses' Arrays
    Marks theMarks = null;
    Courses1 theCourses1 = null;
    Courses2 theCourses2 = null;

    // Define the other classes
    FinishLine theFinish = null;
    StartActivity theStart = null;
    Calculator theCalculator = null;
    PlugInControlReceiver chargeReceiver;

    // Define parameters of next mark
    double mSpeed;
    double mSmoothSpeed;
    String speedDisplay;
    int mHeading;
    int mSmoothHeading;
    String displayHeading;
    String nextMark;
    String nextMarkFull;
    Location destMark;
    float distToMark;
    int bearingToMark;
    int displayBearingToMark;
    String distUnits;
    String finMark = "race";
    int bearingVariance;
    boolean flagFinish = FALSE;
    boolean flagStart = FALSE;
    boolean flagFinished = FALSE;
    boolean doubleFinToEndPressOnce = FALSE;
    boolean charging = true;

    String displayDistToMark;
    String ttmDisplay;
    String accuracy;

    int posMark = 0;
    int posCourse = 0;
    int listMarkSize, listCourseSize;
    String lastMarkName = null;
    String raceCourse;
    String nextRounding = "A";
    ArrayList courseMarks, markRounding;
    String courseDist;
    String courseList, courseSummary;
    Boolean flagMarkExtra;
    MapView map = null;
    private Marker targetMark;

    int deviceOffset,smoothSpeedFactor, smoothHeadFactor, distMarkProximity;
    Boolean autoAdvance, alarmProx, alarmFinish, maxBright, extPower;
    String screenOrientation, division;

    int directionFactor;
    Location aMark, hMark, tower, lastMark, finishPoint;
    Location locationNextMark;
    Double distToFinish;

    final String a = "A"; // Finish line data
    final String h = "H"; // Finish Line Data
    final String twr = "Tower RMYS";

    private Polyline courseLine, startLine;
    GeoPoint aMarkGeo, hMarkGeo;

    // onCreate
    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        // Check device has course files. If not copy course and mark files from assets
        dir = new File(Environment.getExternalStorageDirectory() + "/SailRight");
        copyAsset("courses2.gpx");
        copyAsset("marks.gpx");
        copyAsset("map.sqlite");
        copyAsset("launch3.png");

        //Create the ArrayList object here, for use in all the MainActivity
        theMarks = new Marks();
        theCourses1 = new Courses1();
        theCourses2 = new Courses2();

        IntentFilter filter =  new IntentFilter();
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        chargeReceiver = new PlugInControlReceiver();
        registerReceiver(chargeReceiver, filter);

        //inflate and create the map
        map = (MapView) findViewById(R.id.map);
        setMapOfflineSource();
        showMarks();

        // Create the ArrayList in the constructor, so only done once
        try {
            theMarks.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Load all courses
        try {
            theCourses1.parseXML();
            theCourses2.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

        aMark = theMarks.getNextMark(a);
        hMark = theMarks.getNextMark(h);
        tower = theMarks.getNextMark(twr);
        aMarkGeo = new GeoPoint(aMark.getLatitude(), aMark.getLongitude());
        hMarkGeo = new GeoPoint(hMark.getLatitude(), hMark.getLongitude());

        // Create theCalculator object for processing data readings
        theCalculator = new Calculator();

        // Locate the UI widgets.
        mNextMarkTextView = findViewById(R.id.next_mark_name);
        mMarkExtraTextView = findViewById(R.id.mark_extra);
        mCourseTextView = findViewById(R.id.course_name);
        mSpeedTextView = findViewById(R.id.speed_text);
        mHeadingTextView = findViewById(R.id.heading_text);
        mAccuracyTextView = findViewById(R.id.accuracy_text);
        mDistanceTextView = findViewById(R.id.distance_text);
        mDistanceUnitTextView = findViewById(R.id.dist_unit);
        mBearingTextView = findViewById(R.id.bearing_text);
        mDiscrepTextView = findViewById(R.id.variance_text);
        mTimeToMarkTextView = findViewById(R.id.time_to_mark);
        settingsBtn = findViewById(R.id.button_settings);
        mCourseDistTextView = findViewById(R.id.course_dist);
        mClock = findViewById(R.id.time_text);
        mKeepTextView = findViewById(R.id.keep_title);
        mCourseListTextView = findViewById(R.id.course_details);
        mPowerWarning = findViewById(R.id.power_warning);

        // Settings and preferences
        // Send Toast message on short click
        settingsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                shortClick();
            }
        });

        // Go to settings page on long click
        settingsBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // opening a new intent to open settings activity.
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return false;
            }
        });

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
                    openStartActivity();
                    flagStart = TRUE;
                }
            }
        });

        // Setup long click of "FINISHED" to close app
        mNextMarkTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    switch (v.getId()) {
                        case R.id.next_mark_name:
                            ;
                            initSendInfo(v, System.currentTimeMillis());
                            return true;
                        default:
                            break;
                    }
                return true;
            }
        });

        // Set audio volume to maximum
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);

        updateGPS();
        startLocationUpdates();

    } // end onCreate method


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    }
    @Override
    protected void onSaveInstanceState(Bundle data) {
        data.putInt("course", posCourse);
        data.putInt("mark", posMark);
        super.onSaveInstanceState(data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        posCourse = savedInstanceState.getInt("course");
        posMark = savedInstanceState.getInt("mark");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get settings from preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        deviceOffset = Integer.parseInt(sharedPreferences.getString("prefs_bot_to_gps", "7"));
        smoothSpeedFactor = Integer.parseInt(sharedPreferences.getString("prefs_speed_smooth", "4"));
        if ( smoothSpeedFactor > 20) {
            smoothSpeedFactor = 20;
            Toast.makeText(this, "Speed smoothing factor limited to 20", Toast.LENGTH_LONG).show();
        }
        smoothHeadFactor = Integer.parseInt(sharedPreferences.getString("prefs_heading_smooth", "4"));
        if ( smoothHeadFactor > 20) {
            smoothHeadFactor = 20;
            Toast.makeText(this, "Heading smoothing factor limited to 20", Toast.LENGTH_LONG).show();
        }
        distMarkProximity = Integer.parseInt(sharedPreferences.getString("prefs_proximity_dist", "50"));
        autoAdvance = sharedPreferences.getBoolean("prefs_auto_advance", Boolean.parseBoolean("TRUE"));
        extPower = sharedPreferences.getBoolean("prefs_ext_power", Boolean.parseBoolean("TRUE"));
        alarmProx = sharedPreferences.getBoolean("prefs_mark_prox", Boolean.parseBoolean("TRUE"));
        alarmFinish = sharedPreferences.getBoolean("prefs_finish", Boolean.parseBoolean("TRUE"));
        maxBright = sharedPreferences.getBoolean("prefs_max_bright", Boolean.parseBoolean("TRUE"));
        screenOrientation = sharedPreferences.getString("prefs_orientation", "both");
        division = sharedPreferences.getString("prefs_division", "2");

        // Set screen to maximum brightness
        if (maxBright) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 1;
            getWindow().setAttributes(lp);
        }

        switch(screenOrientation) {
            case "both":
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
               break;
            case "portrait":
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
               break;
            case "landscape":
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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

    void setMapOfflineSource() {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SailRight/");
        if (f.exists()) {
            File[] list = f.listFiles();
            if (list != null) {
                for (File aList : list) {
                    if (aList.isDirectory()) {
                        continue;
                    }
                    String name = aList.getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue;
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue;
                    }
                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) {
                        try {
                            OfflineTileProvider tileProvider =
                                    new OfflineTileProvider(new SimpleRegisterReceiver(this),
                                    new File[]{aList});
                            map.setTileProvider(tileProvider);
                            String source = "";
                            IArchiveFile[] archives = tileProvider.getArchives();
                            if (archives.length > 0) {
                                Set<String> tileSources = archives[0].getTileSources();
                                if (!tileSources.isEmpty()) {
                                    source = tileSources.iterator().next();
                                    map.setTileSource(FileBasedTileSource.getSource(source));
                                } else {
                                    map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                                }
                            } else {
                                map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                            }
                            map.setUseDataConnection(false);
                            IMapController mapController = map.getController();
                            mapController.setZoom(14.0);
                            GeoPoint startPoint = new GeoPoint(-37.87, 144.954);
                            mapController.setCenter(startPoint);
                            map.setMinZoomLevel(12.0);
                            map.setMaxZoomLevel(17.0);
                            map.setScrollableAreaLimitLatitude(-37.82, -38.0, 0);
                            map.setScrollableAreaLimitLongitude(144.8, 145.05, 0);
                            map.invalidate();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    public MapView showMarks() {

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        targetMark = new Marker(map);
        // Create the ArrayList in the constructor, so only done once
        try {
            theMarks.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(map);
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.setPersonHotspot(51, 51);
        Bitmap bitmapStationary = BitmapFactory.decodeResource(getResources(), R.drawable.my_location);
        Bitmap bitmapMoving = BitmapFactory.decodeResource(getResources(), R.drawable.arrow2);
        mLocationOverlay.setDirectionArrow(bitmapMoving, bitmapMoving);
        mLocationOverlay.setPersonIcon(bitmapStationary);
        map.getOverlays().add(mLocationOverlay);

        for ( int i = 0; i < theMarks.listNames.size(); i++ ) {
            String nameMark = (String) theMarks.listNames.get(i);
            String nameMarkFull = nameMark;
            if (nameMark.length() ==1) {
                nameMarkFull = nameMark + " Mark";
            }
            Location locationMark = theMarks.getNextMark(nameMark);
            double lat = locationMark.getLatitude();
            double lon = locationMark.getLongitude();

            Marker courseMark = new Marker(map);
            courseMark.setTitle(nameMarkFull);
            courseMark.setPosition(new GeoPoint(lat, lon));
            courseMark.setIcon(getResources().getDrawable(R.drawable.course_mark));
//            courseMark.setAnchor((float) 0.3, (float) .45); // set for Laser787
            map.getOverlays().add(courseMark);
            map.invalidate();
        }
        return map;
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
        posMark = 0;
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
        posMark = 0;
        setCourse();
        setNextMark();
    }

    /**
     * Set race course
     */
    public void setCourse() {

        if(division.equals("1")) {
            listCourseSize = theCourses1.courses.size();
            raceCourse = theCourses1.courses.get(posCourse).getCourseName();
            courseMarks = theCourses1.getCourse(raceCourse);
            markRounding = theCourses1.getRounding(raceCourse);
            courseDist = theCourses1.getCourseDist(raceCourse);
        } else {
            listCourseSize = theCourses2.courses.size();
            raceCourse = theCourses2.courses.get(posCourse).getCourseName();
            courseMarks = theCourses2.getCourse(raceCourse);
            markRounding = theCourses2.getRounding(raceCourse);
            courseDist = theCourses2.getCourseDist(raceCourse);
        }

        courseList = String.valueOf(courseMarks);
        if (!raceCourse.equals("RMYS")) {
            courseSummary = courseList.substring(courseList.indexOf(",")+1, courseList.lastIndexOf(","));
            map.getOverlays().remove(courseLine);
        } else {
            courseSummary = "";
        }

        mCourseTextView.setBackgroundColor(getResources().getColor(R.color.white));

        if (markRounding.get(0).equals("S")) {
            mCourseTextView.setBackgroundColor(getResources().getColor(R.color.starboard));
        }
        if (markRounding.get(0).equals("P")) {
            mCourseTextView.setBackgroundColor(getResources().getColor(R.color.port));
        }

        mCourseTextView.setText(raceCourse);
        mCourseDistTextView.setText(courseDist);
        mCourseListTextView.setText(courseSummary);


        if (posCourse == 0) {
            settingsBtn.setVisibility(View.VISIBLE);
        } else {
            settingsBtn.setVisibility(View.INVISIBLE);
        }

        // Display the course route
        if (courseLine != null) {
            map.getOverlays().remove(courseLine);
        }

        startLine = new Polyline();
        ArrayList lineStartFin = new ArrayList();
        lineStartFin.add(aMarkGeo);
        lineStartFin.add(hMarkGeo);
        startLine.setPoints(lineStartFin);

        GeoPoint startFin = GeoPoint.fromCenterBetween(aMarkGeo, hMarkGeo);
        courseLine = new Polyline();
        ArrayList route = new ArrayList();
        GeoPoint wayPt;
        String nextWayPt;

        if (posCourse != 0) {
            for (int i = 0; i < courseMarks.size(); i++) {
                if (i == 0 || i == courseMarks.size() - 1) {
                    wayPt = startFin;
                } else {
                    nextWayPt = (String) courseMarks.get(i);
                    Location xx = theMarks.getNextMark(nextWayPt);
                    double lat = xx.getLatitude();
                    double lon = xx.getLongitude();
                    wayPt = new GeoPoint(lat, lon);
                }
                route.add(wayPt);
            }
        }
        courseLine.setColor(R.color.red);
        courseLine.setWidth(3F);
        courseLine.setPoints(route);
    }

    /**
     * This method is called when the + button is pressed
     */
    public void next_mark(View view) {
        // Increment to the position of the nMath.abs(ext mark on the list
        if (posMark >= listMarkSize - 1) {
            posMark = 0;
            // Reset markExtra
            flagMarkExtra = FALSE;
            mMarkExtraTextView.setBackgroundColor(getResources().getColor(R.color.white));
            mMarkExtraTextView.setText("");
        } else {
            posMark = posMark + 1;
        }
        flagFinish = FALSE;
        finMark = "race";
        setNextMark();
    }

    public void previous_mark(View view) {
        // Decrement to the position of the previous mark on the list
        if (posMark <= 0) {
            posMark = listMarkSize - 1;
        } else {
            if (flagMarkExtra) {
                posMark = posMark - 2;
            } else {
                posMark = posMark - 1;
            }
        }
        flagFinish = FALSE;
        finMark = "race";
        setNextMark();
    }

    /**
     * Set next destination mark
     */
    public void setNextMark() {
        if (!flagFinish) {
            mNextMarkTextView.setTextSize(32);

            if (raceCourse.equals("RMYS")) {
                listMarkSize = theMarks.marks.size();
                nextMark = theMarks.marks.get(posMark).getmarkName();
                nextRounding = "A";
            } else {
                listMarkSize = courseMarks.size();
                nextMark = (String) courseMarks.get(posMark);
                nextRounding = (String) markRounding.get(posMark);
            }

            if (nextMark.length() == 1) {
                nextMarkFull = nextMark + " Mark";
            } else {
                nextMarkFull = nextMark;
            }

            if (nextMark.equals("Start")) {
                destMark = theMarks.getNextMark("A");
                mNextMarkTextView.setTextColor(getResources().getColor(R.color.red));
                mNextMarkTextView.setBackgroundColor(getResources().getColor(R.color.button_background));
                mNextMarkTextView.setTypeface(mNextMarkTextView.getTypeface(), Typeface.BOLD);
                mKeepTextView.setVisibility(View.INVISIBLE);
            } else {

                // Check to see if next mark is a non-destination mark.
                // If so, use mMarkExtraTextView to show mark and passing side
                if (nextMark.contains("[")) {
                    flagMarkExtra = TRUE;
                    mKeepTextView.setVisibility(View.VISIBLE);
                    if (nextRounding.equals("S")) {
                        mMarkExtraTextView.setBackgroundColor(getResources().getColor(R.color.starboard));
                    }
                    if (nextRounding.equals("P")) {
                        mMarkExtraTextView.setBackgroundColor(getResources().getColor(R.color.port));
                    }
                    mMarkExtraTextView.setText(nextMarkFull);
                    posMark = posMark +1;
                    nextMark = (String) courseMarks.get(posMark);
                    nextRounding = (String) markRounding.get(posMark);
                    if (nextMark.length() == 1) {
                        nextMarkFull = nextMark + " Mark";
                    } else {
                        nextMarkFull = nextMark;
                    }

                } else {
                    //Reset to normal mark
                    flagMarkExtra = FALSE;
                    mKeepTextView.setVisibility(View.INVISIBLE);
                    mMarkExtraTextView.setBackgroundColor(getResources().getColor(R.color.white));
                    mMarkExtraTextView.setText("");
                }
                mNextMarkTextView.setTypeface(mNextMarkTextView.getTypeface(), Typeface.ITALIC);
                mNextMarkTextView.setTextColor(getResources().getColor(R.color.normal_text));
                mNextMarkTextView.setBackgroundColor(getResources().getColor(R.color.white));
                    if (nextRounding.equals("S")) {
                        mNextMarkTextView.setBackgroundColor(getResources().getColor(R.color.starboard));
                    }
                    if (nextRounding.equals("P")) {
                        mNextMarkTextView.setBackgroundColor(getResources().getColor(R.color.port));
                    }
            }
            mNextMarkTextView.setText(nextMarkFull);

            // Check to see if next mark is not the finish
            if (nextMark.equals("Finish")) {
                mNextMarkTextView.setBackgroundColor(getResources().getColor(R.color.white));
                // Identify the last mark to determine the direction of approach
                flagFinish = TRUE;
                lastMarkName = (String) courseMarks.get(listMarkSize - 2);

                // Should have A Mark, H Mark to create the Finish Line Object
                lastMark = theMarks.getNextMark(lastMarkName);
                theFinish = new FinishLine(aMark, hMark, tower, lastMark);

                // Find the direction of approach to the finish line
                directionFactor = theFinish.getFinishDirection();
            } else {
                // Not the finish, set the next mark normally
                // unless it is the start in which case leave it as A Mark
                if (!nextMark.equals("Start")) {
                    destMark = theMarks.getNextMark(nextMark);
                }
                flagFinish = FALSE;
            }
            updateLocationUI();

        }
        // Put balloon over next mark
        map.getOverlays().remove(targetMark);
        map.getOverlays().remove(courseLine);
        locationNextMark = theMarks.getNextMark(nextMark);
        double lat = locationNextMark.getLatitude();
        double lon = locationNextMark.getLongitude();
        targetMark.setTitle(nextMarkFull);
        targetMark.setPosition(new GeoPoint(lat, lon));
        targetMark.setIcon(getResources().getDrawable(R.drawable.balloon2));
        targetMark.setAnchor((float) 0.45, (float) 1.1); // set for Laser787
        map.getOverlays().add(targetMark);
        map.getOverlays().add(courseLine);
        map.getOverlays().add(startLine);
        map.invalidate();
    }

    public void openStartActivity() {
        // Find first mark
        String firstMarkName = (String) courseMarks.get(1);

        Intent start = new Intent(this, StartActivity.class);
        start.putExtra("course", raceCourse);
        start.putExtra("summary", courseSummary);
        start.putExtra("mark", nextMark);
        start.putExtra("first", firstMarkName);
        startActivity(start);
        posMark = 1;
        setNextMark();
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

        if (mCurrentLocation != null && !flagStart) {

            // Process gps data for display on UI
            mSpeed = mCurrentLocation.getSpeed();
            mSmoothSpeed = theCalculator.getSmoothSpeed(mSpeed, smoothSpeedFactor);

            // Convert to knots and display
            speedDisplay = new DecimalFormat("##0.0").format(mSmoothSpeed * 1.943844); //convert to knots

            // Change heading to correct format and smooth
            mHeading = (int) mCurrentLocation.getBearing();
            mSmoothHeading = theCalculator.getSmoothHeading(mHeading, smoothHeadFactor);

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

            if (distToMark < distMarkProximity
                    && finMark.equals("race")
                    && autoAdvance
                    && !nextMark.equals("Start")
                    && !raceCourse.equals("RMYS")) {
                posMark = posMark + 1;
                setNextMark();
                if (alarmProx) {
                    playSounds("klaxon");
                }
            }

            if (flagFinish && finishPoint != null) {
            // Calculate distance in metres to finish point from latitude
                double approachAngle = Math.abs(theFinish.getApproachAngle());
                double distToDevice = deviceOffset * Math.sin(Math.toRadians(approachAngle));
                distToFinish = (mCurrentLocation.getLatitude() - finishPoint.getLatitude()) * directionFactor * 60 * 1852;
                displayDistToMark = theCalculator.getDistScale(distToFinish);
                if (distToFinish < distToDevice) {
                    if (alarmFinish) {
                        playSounds("whoop");
                    }
                    mNextMarkTextView.setTextSize(24);
                    mNextMarkTextView.setText(R.string.finished);
                    flagFinish = FALSE;
                    flagFinished = TRUE;
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
            mDiscrepTextView.setTextColor(getResources().getColor(R.color.app_green));
        }
        mClock.setFormat24Hour("HH:mm:ss");
        mTimeToMarkTextView.setText(ttmDisplay);
        mAccuracyTextView.setText(accuracy);

        if (!charging && extPower) {
            mPowerWarning.setVisibility(View.VISIBLE);
            } else {
            // Display warning
            mPowerWarning.setVisibility(View.GONE);
        }
    }

    public void playSounds(String sound) {
        if (sound.equals("klaxon")) {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.klaxon);
            mediaPlayer.start();
        }
        if (sound.equals("shotgun")) {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.shotgun);
            mediaPlayer.start();
        }
        if (sound.equals("whoop")) {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.whoop2);
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

    public void shortClick() {
        Toast.makeText(this, "Long click to get to Settings", Toast.LENGTH_LONG).show();
    }

    // Long press of "FINISHED" to close app
    private void initSendInfo(final View v, final long startTime) {
        Toast.makeText(this, "Hold for 3 seconds to close app", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (v.isPressed() && System.currentTimeMillis() - startTime >+ LONG_PRESS_DELAY_MILLIS) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    }
                    return;
                } else if (!v.isPressed()) {
                    return;
                }
            }
        }, LONG_PRESS_DELAY_MILLIS);
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
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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

    private void copyAsset(String filename) {

        // Check if SailRight directory exists, if not create it
        if (!dir.exists()) {
            dir.mkdir();
        }
        // If courses or marks file do not exist copy them from assets
        File storedFile = new File(dir + "/" + filename);
        if (!storedFile.exists()) {
            AssetManager assetManager = getAssets();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(dir, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

//Detect if device is charging
    public class PlugInControlReceiver extends BroadcastReceiver {
        String action;
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();

            if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                // Do something when power connected
                charging = true;
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                // Do something when power disconnected
                charging = false;
            }
        }
    }
}


