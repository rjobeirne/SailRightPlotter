package com.sail.sailrightplotter;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

public class Calculator {

    double mSmoothSpeed, sumSpeed;
    int nSpeed = 20;
    public double storeSpeed[] = new double[nSpeed];
    int mSmoothHeading, sumHeading;
    int mSmoothBearing, sumBearing;
    int nHeading = 20;
    int nBearing = 20;
    public int storeHeading[] = new int[nHeading];
    public int storeBearing[] = new int[nHeading];
    String displayDistToMark, distUnits;
    int displayBearingToMark;
    int rawVariance, bearingVariance;
    double vmgToMark;
    long timeToMark, timeVariance;
    String ttmDisplay, timeVarDisplay, timeVarianceSense;



    public double getSmoothSpeed(double mSpeed, int nSpeed) {
       // Process gps data for display on UI
        // Get speed in m/s and smooth for n=nSpeed readings
        sumSpeed = 0;
        for ( int i = nSpeed - 1; i > 0; i--) {
            storeSpeed[i] = storeSpeed[i - 1];
            sumSpeed = sumSpeed + storeSpeed[i];
        }
        storeSpeed[0] = mSpeed;
        sumSpeed = sumSpeed + storeSpeed[0];
        mSmoothSpeed = sumSpeed / nSpeed;
        return mSmoothSpeed;
    }
    
      public int getSmoothHeading(int mHeading, int nHeading) {
       // Process gps data for display on UI
        // Get heading and smooth for n=nHeading readings
        sumHeading = 0;
        for ( int i = nHeading - 1; i > 0; i--) {
            storeHeading[i] = storeHeading[i - 1];
            sumHeading = sumHeading + storeHeading[i];
        }
        storeHeading[0] = mHeading;
        sumHeading = sumHeading + storeHeading[0];
        mSmoothHeading = sumHeading / nHeading;
        return mSmoothHeading;
    }

      public int getSmoothBearing(int mBearing, int nBearing) {
       // Process gps data for display on UI
        // Get heading and smooth for n=nHeading readings
        sumBearing = 0;
        for ( int i = nBearing - 1; i > 0; i--) {
            storeBearing[i] = storeBearing[i - 1];
            sumBearing = sumBearing + storeBearing[i];
        }
        storeBearing[0] = mBearing;
        sumBearing = sumBearing + storeBearing[0];
        mSmoothBearing = sumBearing / nBearing;
        return mSmoothBearing;
    }

    public String getDistScale(double distToMark) {
        // Use nautical miles when distToMark is >500m.
        if (distToMark > 500) {
            displayDistToMark = new DecimalFormat("###0.00").format(distToMark / 1852);
            if (distToMark > 18520) { // Reduce significant figures for > 10 nm
                displayDistToMark = new DecimalFormat("###0.0").format(distToMark / 1852);
            }
        } else {
            displayDistToMark = new DecimalFormat("###0").format(distToMark);
        }
        return displayDistToMark;
    }

    public String getDistUnit(double distToMark) {
        // Use nautical miles when distToMark is >500m.
        if (distToMark > 500) {
            distUnits = "NM";
        } else {
            distUnits = "m";
        }
        return distUnits;
    }
    
    public int getCorrectedBearingToMark(int bearingToMark) {
        // Correct negative bearings
        if (bearingToMark < 0) {
            displayBearingToMark = bearingToMark + 360;
        } else {
            displayBearingToMark = bearingToMark;
        }
        return displayBearingToMark;
    }
    
    public int getVariance() {
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
        return  bearingVariance;        
    }
    
    public String getTimeToMark(double distToMark) {
        vmgToMark = Math.cos(Math.toRadians(bearingVariance)) * mSmoothSpeed;
        timeToMark = (long) (distToMark / vmgToMark);

        // Keep displayed figure below 100 hours 360000 secs.
        if (timeToMark < 360000 && timeToMark > 0) {

            if (timeToMark > 3559) {
                ttmDisplay = String.format("%2dh %02d' %02d\"",
                        TimeUnit.SECONDS.toHours(timeToMark),
                        TimeUnit.SECONDS.toMinutes(timeToMark) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeToMark)),
                        TimeUnit.SECONDS.toSeconds(timeToMark) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeToMark)));
            } else {
                ttmDisplay = String.format("%02d' %02d\"",
                        TimeUnit.SECONDS.toMinutes(timeToMark) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeToMark)),
                        TimeUnit.SECONDS.toSeconds(timeToMark) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeToMark)));
            }
        } else {
            ttmDisplay = "--' --\"";
        }
        return ttmDisplay;
    }

    public String getTimeVariance(long timeRemain) {
        // Calc early/late to line
        timeVariance = timeRemain - timeToMark;
        if (timeVariance < 360000 && timeVariance > -360000) {
            timeVarDisplay = String.format("%02d' %02d\"",
                    abs(TimeUnit.SECONDS.toMinutes(timeVariance)),
                    abs(TimeUnit.SECONDS.toSeconds(timeVariance) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeVariance))));
        } else {
            timeVarDisplay = "--' --\"";
        }
        return timeVarDisplay;
    }

    public String getStartTimeliness() {
        // flag the time variance display as early or late
        if (timeVariance < 0) {
            timeVarianceSense = "Late";
        }
        if (timeVariance > 0) {
            timeVarianceSense = "Early";
        }
        return timeVarianceSense;
    }
}
