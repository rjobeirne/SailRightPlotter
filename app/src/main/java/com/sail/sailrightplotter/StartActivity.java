package com.sail.sailrightplotter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;
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

    TextView startCourseTextView, startCourseSummaryTextView, startNextMarkTextView;
    TextView mSpeedTextView, mHeadingTextView, mDistanceTextView, mDistanceUnitTextView;
    TextView mBearingTextView, mTimeVarianceTextView, mEarlyLateTextView;
    TextView mTimeToMarkTextView, mAccuracyTextView;
    TextView mClockTextView;
    private TextClock mClock;

    // Define variables
    Location destMark;
    String startMark = "A";
    String speedDisplay, displayHeading;
    String displayDistToMark, distUnits;
    String ttmDisplay, displayTimeVariance, timeliness, accuracy;
    int mHeading, mSmoothHeading;
    int bearingToMark, displayBearingToMark;
    double mSpeed, mSmoothSpeed;
    double distToMark;
    double approachAngle, distToDevice;
    int deviceOffset, startMargin, smoothSpeedFactor, smoothHeadFactor, deltaBearingSwitch;
    Boolean alarmMinute, alarmStart, alarmBadStart, maxBright;

    // Define clock variables
    long timeRemain = 75;
    long timeToStart;
    public Boolean timerStarted = false;
    Boolean resetClock = false;
    CountDownTimer startClock;
    double secsLeft;
    String clockControl = "Go";
    MapView map = null;
    Polyline startLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Intent intent = getIntent();
        String startCourse = intent.getStringExtra("course");
        String startCourseSummary = intent.getStringExtra("summary");
        String firstMarkName = intent.getStringExtra("first");

        // Locate the UI widgets.
        startCourseTextView = findViewById(R.id.start_course_name);
        startCourseSummaryTextView = findViewById(R.id.start_summary);
        startNextMarkTextView = findViewById(R.id.start_next_mark_name);
        mSpeedTextView = findViewById(R.id.speed_text);
        mHeadingTextView = findViewById(R.id.heading_text);
        mDistanceTextView = findViewById(R.id.distance_text);
        mDistanceUnitTextView = findViewById(R.id.dist_unit);
        mBearingTextView = findViewById(R.id.bearing_text);
        mTimeVarianceTextView = findViewById(R.id.start_time_early_late);
        mEarlyLateTextView = findViewById(R.id.start_time_early_late_title);
        mTimeToMarkTextView = findViewById(R.id.time_to_line);
        mAccuracyTextView = findViewById(R.id.accuracy_text);
        mClockTextView = findViewById(R.id.time_to_start);
        mClock = findViewById(R.id.time_text);

        //inflate and create the map
        map = (MapView) findViewById(R.id.map);
        setMapOfflineSource();
        showStart();

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
        String twr = "Tower RMYS";
        Location aMark = theMarks.getNextMark(a);
        Location hMark = theMarks.getNextMark(h);
        Location tower = theMarks.getNextMark(twr);
        Location firstMark = theMarks.getNextMark(firstMarkName);

        // Create theCalculator object for processing data readings
        theCalculator = new Calculator();

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
        Button mButton = findViewById(R.id.start_clock);
        mButton.setText(clockControl);
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start_clock(v);
            }
        });


        // set all properties of LocationRequest
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
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

        // Get settings from preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        deviceOffset = Integer.parseInt(sharedPreferences.getString("prefs_bot_to_gps", "10"));
        timeToStart = 60 * Integer.parseInt(sharedPreferences.getString
               ("prefs_default_start_time", "15"));
        startMargin = Integer.parseInt(sharedPreferences.getString("prefs_start_margin", "15"));
        smoothSpeedFactor = Integer.parseInt(sharedPreferences.getString("prefs_speed_smooth", "4"));
        smoothHeadFactor = Integer.parseInt(sharedPreferences.getString("prefs_heading_smooth", "4"));
        alarmMinute = sharedPreferences.getBoolean("prefs_mins_airhorn", Boolean.parseBoolean("TRUE"));
        alarmStart = sharedPreferences.getBoolean("prefs_start_gun", Boolean.parseBoolean("TRUE"));
        alarmBadStart = sharedPreferences.getBoolean("prefs_bad_start", Boolean.parseBoolean("TRUE"));
        deltaBearingSwitch = Integer.parseInt(sharedPreferences.getString("prefs_delta_bearing", "15"));
        maxBright = sharedPreferences.getBoolean("prefs_max_bright", Boolean.parseBoolean("TRUE"));

        // Set screen to maximum brightness
        if (maxBright) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 1;
            getWindow().setAttributes(lp);
        }
        distToDevice = deviceOffset * Math.sin(Math.toRadians(approachAngle));

        // Create the start line
        theLine = new StartLine(aMark, hMark, tower, firstMark, deltaBearingSwitch);

        StartDisplay(startCourse, startCourseSummary, startMark + " Mark");
        updateGPS();
        showClock(timeToStart);
        startLocationUpdates();
        setMapOfflineSource();
    } // end of onCreate


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
//            map.getOverlays().add(courseMark);
            map.invalidate();
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

//        targetMark = new Marker(map);
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

//        for ( int i = 0; i < theMarks.listNames.size(); i++ ) {
//            String nameMark = (String) theMarks.listNames.get(i);
//            String nameMarkFull = nameMark;
//            if (nameMark.length() ==1) {
//                nameMarkFull = nameMark + " Mark";
//            }
//            Location locationMark = theMarks.getNextMark(nameMark);
//            double lat = locationMark.getLatitude();
//            double lon = locationMark.getLongitude();
//
//            Marker courseMark = new Marker(map);
//            courseMark.setTitle(nameMarkFull);
//            courseMark.setPosition(new GeoPoint(lat, lon));
//            courseMark.setIcon(getResources().getDrawable(R.drawable.course_mark));
////            courseMark.setAnchor((float) 0.3, (float) .45); // set for Laser787
//            map.getOverlays().add(courseMark);
//            map.invalidate();
//        }
        return map;

    }

    /**
     * Show the start line on the map
     */
    public void showStart() {

        GeoPoint aMarkGeo = new GeoPoint(theMarks.getNextMark("A").getLatitude(),
                theMarks.getNextMark("A").getLongitude());
        GeoPoint hMarkGeo = new GeoPoint(theMarks.getNextMark("H").getLatitude(),
                theMarks.getNextMark("H").getLongitude());
        startLine = new Polyline();
        ArrayList lineStartFin = new ArrayList();
        lineStartFin.add(aMarkGeo);
        lineStartFin.add(hMarkGeo);
        startLine.setColor(R.color.black);
        startLine.setWidth(10F);
        startLine.setPoints(lineStartFin);
        map.getOverlays().add(startLine);
        map.invalidate();
    }
    /**
     * Display course and start mark
     * @param startCourse Course selected
     * @param startMark Nearest mark or the line
     */
    public void StartDisplay(String startCourse, String startCourseSummary, String startMark) {
        startCourseTextView.setText(startCourse);
        startCourseSummaryTextView.setText(startCourseSummary);
        startNextMarkTextView.setText(startMark);
    }

    /**
     * Calculates all the navigational data
     */
    private void updateLocationData(Location mCurrentLocation) {
        if (mCurrentLocation != null) {

            startMark = theLine.getStartTarget(mCurrentLocation);

            if (startMark.equals("Line")) {
                // Insert the finish line crossing point
                startNextMarkTextView.setText(startMark);
                destMark = theLine.getStartPoint(mCurrentLocation);
                approachAngle = Math.abs(theLine.getApproachAngle());
            } else {
                // Set the next mark to either A or H
                startNextMarkTextView.setText("Start - " + startMark + " Mark");
                destMark = theMarks.getNextMark(startMark);
            }

        // Process gps data for display on UI
            mSpeed = mCurrentLocation.getSpeed();
            mSmoothSpeed = theCalculator.getSmoothSpeed(mSpeed, smoothSpeedFactor);
            // Convert to knots and display
            speedDisplay = new DecimalFormat("##0.0").format(mSmoothSpeed * 1.943844); //convert to knots

            // Change heading to correct format and smooth
            mHeading = (int) mCurrentLocation.getBearing();
            mSmoothHeading = theCalculator.getSmoothHeading(mHeading, smoothHeadFactor);
            displayHeading = String.format("%03d", mSmoothHeading);

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
        mClock.setFormat24Hour("HH:mm:ss");

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
        Button mButton = findViewById(R.id.start_clock);
        if (timerStarted) {
            Toast.makeText(this, "Clock synchronised", Toast.LENGTH_SHORT).show();
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

                if (Math.round((secsLeft) / 60) * 60 == secsLeft && secsLeft > 0) {
                    if (alarmMinute) {
                        playSounds("air_horn");
                    }
                }
            }

            public void onFinish() {
                if (alarmStart) {
                    playSounds("shotgun");
                }
                double timeToLine = (theLine.getShortestDist() - distToDevice) / mSmoothSpeed;
                if (timeToLine > startMargin) {
                    if (alarmBadStart) {
                        playSounds("fail");
                    }
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
        timeToStart = Math.round((secsLeft) / 60) * 60;
        resetClock = true;
        countdown();
    }

    public void showClock(long timeRemain) {
        String clockDisplay = String.format("%02d' %02d\"",
                TimeUnit.SECONDS.toMinutes(timeRemain) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeRemain)),
                TimeUnit.SECONDS.toSeconds(timeRemain) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeRemain)));

        mClockTextView.setText(clockDisplay);
    }

    public void playSounds(String sound) {
        if (sound.equals("air_horn")) {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.air_horn);
            mediaPlayer.start();
        }
        if (sound.equals("shotgun")) {
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.shotgun);
            mediaPlayer.start();
        }
        if (sound.equals("fail")) {
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
}
