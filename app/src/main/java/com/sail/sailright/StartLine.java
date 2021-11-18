package com.sail.sailright;

import android.location.Location;

public class StartLine {

    // Initialise global object variables
    String startTarget = null;
    final Location markA;
    final Location markH;
    final Location tower;
    final double latA;
    final double lonA;
    final double latFirstMark;
    final double slopeLine;
    final double constLine;
    final float slopeLineAngle;
    Location startPoint = new Location("");

    // Initialise boat details
    Location mCurrentLocation = null;
    double latBoat = 0;
    double lonBoat = 0;
    float boatHeading, bearingToA, bearingToH, deltaBearing;
    float distToA, distToH;
    float negBoatHeading, displayBearingToA, displayBearingToH;
    String nearestMark;
    double slopeBoat = 0;
    float slopeBoatAngle;
    double constBoat = 0;
    int directionFactor;
    int deltaBearingSwitch;

    /**
     * Define the start line from the mark locations
     * @param a name of one end of the line
     * @param h name of the other end of the line
     * @param first name of the first mark after the start
     */
    public StartLine (Location a, Location h, Location twr, Location first, int approach) {
        // constructor with 'A' Mark, and 'H' mark location details, and first currentLocation
        markA = a;
        markH = h;
        tower = twr;
        deltaBearingSwitch = approach;
//        Location firstMark = first;
        latA = markA.getLatitude();
        lonA = markA.getLongitude();
        latFirstMark = first.getLatitude();

        // Define finish line as linear equation lat = slope * lon + constant
        float lineBearing = markA.bearingTo(tower);

        // Convert line bearing to angle from latitude (not north)
        if ( lineBearing > 0) {
            slopeLineAngle = 90 - lineBearing;
        } else {
            slopeLineAngle = 90 + lineBearing;
        }

        slopeLine = Math.tan(Math.toRadians(slopeLineAngle));
        constLine = latA - (slopeLine * lonA);

        getStartDirection();
    }

    /**
     * Calculate the boat location details
     * @param currentLocation current gps location
     */
    private void setBoatDetails(Location currentLocation) {
        // Define boat heading as linear equation lat = slope * lon + constant
        mCurrentLocation = currentLocation;
        latBoat = mCurrentLocation.getLatitude();
        lonBoat = mCurrentLocation.getLongitude();
        boatHeading = mCurrentLocation.getBearing();
        distToA = mCurrentLocation.distanceTo(markA);
        distToH = mCurrentLocation.distanceTo(markH);

        // Convert boat heading to angle from latitude (not north)
        if ( boatHeading > 0) {
            slopeBoatAngle = 90 - boatHeading;
        } else {
            slopeBoatAngle = 90 + boatHeading;
        }

        slopeBoat = Math.tan(Math.toRadians(slopeBoatAngle));
        constBoat = latBoat - (slopeBoat * lonBoat);

        //Convert boat heading from compass bearing to +/- from north
        if (boatHeading >180) {
            negBoatHeading = boatHeading - 360;
        } else {
            negBoatHeading = boatHeading;
        }

        // Convert bearings compass bearings
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
        deltaBearing = Math.abs(bearingToA-bearingToH);


    }

    /**
     * Determine approach direction to line
     * @return direction factor, 1= North, -1= South
     */
    public int getStartDirection() {
        if (latFirstMark < latA) {
            // Approaching start from the north
            directionFactor = 1;
        } else {
            // Approach from the south
            directionFactor = -1;
        }
        return  directionFactor;
    }

    /**
     *
     * @return The name of the start point
     * Mark name or Line
     */
    public String getStartTarget(Location currentLocation) {
        // Update current Location of the boat, passed in from Main
        mCurrentLocation = currentLocation;
        setBoatDetails(mCurrentLocation);  // Update current boat location details

        if(deltaBearing > deltaBearingSwitch) {
            // Approaching the line squarish
            if (directionFactor == 1) {
                // Approaching from the north
                // Use compass bearing
                if (boatHeading > displayBearingToA) {
                    startTarget = "A";
                } else if (boatHeading < displayBearingToH) {
                    startTarget = "H";
                } else {
                    startTarget = "Line";
                }

            } else {
                // Approaching from the south
                // Use bearing +/- from north
                if (negBoatHeading < bearingToA) {
                    startTarget = "A";
                } else if (negBoatHeading > bearingToH) {
                    startTarget = "H";
                } else {
                    startTarget = "Line";
                }
            }
        } else {
            // Approaching the start from the side
            // Find nearest mark
            if (distToA < distToH) {
                startTarget = "A";
            } else {
                startTarget = "H";
            }
        }
        return startTarget;
    }

    /**
     *
     * @return startPoint
     * The location of the start point on the line
     */
    public Location getStartPoint(Location currentLocation) {
        // Update the current Location of the boat
        mCurrentLocation = currentLocation;
        setBoatDetails(mCurrentLocation);

        // Calculate lon & lat of start point
        double finLon = (constLine - constBoat) / (slopeBoat - slopeLine);
        double finLat = slopeLine * finLon + constLine;

        // Define startPoint by lon & lat
        startPoint.setLongitude(finLon);
        startPoint.setLatitude(finLat);
        return startPoint;
    }

    /**
     * Calculate the approach angle to the line
     * @return approachAngle
     */
    public double getApproachAngle() {
        double approachAngle = slopeLineAngle - slopeBoatAngle;
        return approachAngle;
    }

    /**
     * Calculate the closest dist from current location to the line
     * @return shortest distance to the line
     */
    public double getShortestDist() {
        double shortestDist = Math.abs(slopeLine * lonBoat - latBoat + constLine)
                / Math.sqrt(Math.pow(slopeLine, 2) + 1) * 60 * 1852;
        return shortestDist;
    }

}
