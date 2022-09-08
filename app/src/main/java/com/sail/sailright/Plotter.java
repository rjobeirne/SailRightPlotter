package com.sail.sailright;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

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
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Plotter extends AppCompatActivity {
    MapView map = null;
    Marks theMarks = null;
    int posMark = -1;
    int listMarkSize;

    // UI Widgets.
    private TextView mNextMarkTextView;
    private TextView mDistToMark, mDistToMarkTitle;
    private String nextMark;
    private Marker courseMark;
    private Location currentLocation, locationMark;

    String distance;
    String displayDistToMark;
    double distToMark;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//        // The request code used in ActivityCompat.requestPermissions()
//        // and returned in the Activity's onRequestPermissionsResult()
//        int PERMISSION_ALL = 1;
//        String[] PERMISSIONS = {
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        };
//
//        if (!hasPermissions(this, PERMISSIONS)) {
//            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//        }
//
//        Context ctx = getApplicationContext();
//        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
//
//        // Locate the UI widgets.
//        mNextMarkTextView = (TextView) findViewById(R.id.next_mark_name);
//        mNextMarkTextView.setText("RMYS");
//
//        //inflate and create the map
//        map = (MapView) findViewById(R.id.map);
//        setMapOfflineSource();
//
//
//
//        // for debugging offline maps
////        Configuration.getInstance().setDebugMode(true);
////        Configuration.getInstance().setDebugTileProviders(true);
////        File path = Configuration.getInstance().getOsmdroidBasePath();
////        Log.e("Default path**", String.valueOf(path));
//
//        //Create the ArrayList object here, for use in all the MainActivity
//        theMarks = new Marks();
//        courseMark = new Marker(map);
//
//        // Create the ArrayList in the constructor, so only done once
//        try {
//            theMarks.parseXML();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        listMarkSize = theMarks.listNames.size();
//
////        showMarks();
//
//        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(map);
//        mLocationOverlay.enableFollowLocation();
//        mLocationOverlay.enableMyLocation();
//        mLocationOverlay.setPersonHotspot(51, 51);
//        Bitmap bitmapStationary = BitmapFactory.decodeResource(getResources(), R.drawable.my_location);
//        Bitmap bitmapMoving = BitmapFactory.decodeResource(getResources(), R.drawable.arrow1);
//        mLocationOverlay.setDirectionArrow(bitmapMoving, bitmapMoving);
//        mLocationOverlay.setPersonIcon(bitmapStationary);
//        map.getOverlays().add(mLocationOverlay);
//
//        MyLocationListener locationListener = new MyLocationListener();
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if( currentLocation != null ) {
////                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
//         }
//
//    }  // end of onCreate

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
//                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//                }
            }
        }
        return true;
    }
        public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     * This method is called when the + button is pressed
     */
    public void next_mark(View view) {
        // Increment to the position of the nMath.abs(ext mark on the list
        if (posMark >= listMarkSize - 1) {
            posMark = -1;
        } else {
            posMark = posMark + 1;
        }
        setNextMark();
    }

    public void previous_mark(View view) {
        // Decrement to the position of the previous mark on the list
        if (posMark <= -1) {
            posMark = listMarkSize - 1;
        } else {
                posMark = posMark - 1;
        }
        setNextMark();
    }

    public void setNextMark() {

        if (nextMark != null) {
            courseMark.closeInfoWindow();
            map.getOverlays().remove(courseMark);
        }
        String nextMarkFull;
        if (posMark == -1) {
            nextMarkFull = "RMYS";
        } else {
            nextMark = theMarks.marks.get(posMark).getmarkName();
            nextMarkFull = nextMark;
            if (nextMark.length() ==1) {
                nextMarkFull = nextMark + " Mark";
            }

            locationMark = theMarks.getNextMark(nextMark);
            double lat = locationMark.getLatitude();
            double lon = locationMark.getLongitude();
            courseMark.setTitle(nextMarkFull);
            courseMark.setPosition(new GeoPoint(lat, lon));
            courseMark.setIcon(getResources().getDrawable(R.drawable.pin));
            courseMark.setAnchor((float) 0.42, (float) 1.0);
            map.getOverlays().add(courseMark);
            map.invalidate();
        }
        mNextMarkTextView.setText(nextMarkFull);
    }

    public MapView showMarks() {
//        setContentView(R.layout.activity_main);

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

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        theMarks = new Marks();
        courseMark = new Marker(map);
        // Create the ArrayList in the constructor, so only done once
        try {
            theMarks.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listMarkSize = theMarks.listNames.size();

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(map);
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.setPersonHotspot(51, 51);
        Bitmap bitmapStationary = BitmapFactory.decodeResource(getResources(), R.drawable.my_location);
        Bitmap bitmapMoving = BitmapFactory.decodeResource(getResources(), R.drawable.arrow1);
        mLocationOverlay.setDirectionArrow(bitmapMoving, bitmapMoving);
        mLocationOverlay.setPersonIcon(bitmapStationary);
        map.getOverlays().add(mLocationOverlay);

        MyLocationListener locationListener = new MyLocationListener();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( currentLocation != null ) {
//                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
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
            map.getOverlays().add(courseMark);
            map.invalidate();
        }
        return map;
    }

    void setMapOfflineSource() {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
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
                                    new OfflineTileProvider(new SimpleRegisterReceiver(Plotter.this),
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
                            mapController.setZoom(13.0);
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

    public static class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location currentLocation) {
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
