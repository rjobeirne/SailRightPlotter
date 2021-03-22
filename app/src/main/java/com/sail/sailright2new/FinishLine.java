package com.sail.sailright2new;

import android.location.Location;
import android.util.Log;

public class FinishLine {

    public FinishLine (Location a, Location h, Location last) {
        // constructor with 'A' Mark, and 'H' mark location details, and first currentLocation
        markA = a;
        markH = h;
        lastMark = last;
        latA = markA.getLatitude();
        lonA = markA.getLongitude();
        latLastMark = lastMark.getLatitude();
    }


    // Initialise global object variables
    String finishTarget = null;
    Location markA = null;
    Location markH = null;
    Location lastMark;
    double latA = 0;
    double lonA = 0;
    double latLastMark;
    float lineBearing = 0;
    double slopeLine = 0;
    double constLine = 0;

    // Boat Location details
    Location mCurrentLocation = null;
    double latBoat = 0;
    double lonBoat = 0;
    float boatHeading, bearingToA, bearingToH;
    float displayBearingToA, displayBearingToH;
    double slopeBoat = 0;
    double constBoat = 0;


    // This will set the current boat Location details
    public void setBoatDetails(Location currentLocation) {
        // Define boat heading as linear equation lat = slope * lon + constant
        mCurrentLocation = currentLocation;
        latBoat = mCurrentLocation.getLatitude();
        lonBoat = mCurrentLocation.getLongitude();

        boatHeading = mCurrentLocation.getBearing();
        slopeBoat = Math.tan(Math.toRadians(boatHeading));
        constBoat = latBoat - (slopeBoat * lonBoat);

        bearingToA = mCurrentLocation.bearingTo(markA);
                if ( bearingToA < 0) {
                    displayBearingToA = bearingToA + 360;
                } else {
                    displayBearingToA = bearingToA;
                }
        bearingToH = mCurrentLocation.bearingTo(markH);
                if ( bearingToH < 0) {
                    displayBearingToH = bearingToH + 360;
                } else {
                    displayBearingToH = bearingToH;
                }


        // Define finish line as linear equation lat = slope * lon + constant
        lineBearing = markA.bearingTo(markH);
        slopeLine = Math.tan(Math.toRadians(lineBearing));
        constLine = latA - (slopeLine * lonA);

    }
    /**
     *
     * @return
     */
    public String getFinishTarget(Location currentLocation) {
        Log.e("**start getFinishTarget", String.valueOf(latLastMark));
        // Update current Location of the boat, passed in from Main
        mCurrentLocation = currentLocation;
        setBoatDetails(mCurrentLocation);  // Update current boat location details

        if (latLastMark > latA) {
            // Approaching from the north
            Log.e("Approach from the north", String.valueOf(latLastMark));
            if (boatHeading > displayBearingToA) {
                finishTarget = "A";
            } else if (boatHeading < displayBearingToH) {
                finishTarget = "H";
            } else {
                finishTarget = "Line";
            }

        } else {
            // Approaching from the south
            // First correct heading >180 to be negative to avoid problem at due north
            if (boatHeading > 180) {
                boatHeading = boatHeading - 360;
            }
            if (bearingToA > 180) {
                bearingToA = bearingToA - 360;
            }
            if (bearingToH > 180) {
                bearingToH = bearingToH - 360;
            }

            if (boatHeading < displayBearingToA) {
                finishTarget = "A";
            } else if (boatHeading > displayBearingToH) {
                finishTarget = "H";
            } else {
                finishTarget = "Line";
            }

        }
        return  finishTarget;
    }

    /**
     *
     * @return
     */
    public Location getFinishPoint(Location currentLocation) {

        Location finishPoint = new Location("");
        Log.e("started getFinishPoint","");
        mCurrentLocation = currentLocation;
        setBoatDetails(mCurrentLocation);  // Update the current Location of the boat

        double finLon = (constLine - constBoat) / (slopeBoat - slopeLine);
        double finLat = slopeLine * finLon +constLine;
        finishPoint.setLongitude(finLon);
        finishPoint.setLatitude(finLat);
         Log.e("**finishPoint", String.valueOf(finishPoint));

        return finishPoint;
    }

}
