package com.sail.sailright2new;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

public class Calculator {

    double mSpeed1, mSpeed2, mSpeed3, mSmoothSpeed;
    int mHeading1, mHeading2, mHeading3, mSmoothHeading;
    int negHeading;
    String displayDistToMark, distUnits;
    int displayBearingToMark;
    int rawVariance, bearingVariance;
    double vmgToMark;
    long timeToMark, timeVariance;
    String ttmDisplay, timeVarDisplay, timeVarianceSense;

    public double getSmoothSpeed(double mSpeed) {
       // Process gps data for display on UI
        // Get speed in m/s and smooth for 4 readings
        mSpeed3 = mSpeed2;
        mSpeed2 = mSpeed1;
        mSpeed1 = mSpeed;
        mSmoothSpeed = (mSpeed + mSpeed1 + mSpeed2 + mSpeed3) / 4;
        return mSmoothSpeed;
    }
    
      public int getSmoothHeading(int mHeading) {
       // Process gps data for display on UI
        // Get heading and smooth for 4 readings
        mHeading3 = mHeading2;
        mHeading2 = mHeading1;
        mHeading1 = mHeading;
        mSmoothHeading = (mHeading + mHeading1 + mHeading2 + mHeading3) / 4;
        return mSmoothHeading;
    }

    public int getNegHeading() {
         if (mSmoothHeading > 180) {
            negHeading = mSmoothHeading - 360;
        } else {
            negHeading = mSmoothHeading;
        }
         return negHeading;
    }

    public String getDistScale(double distToMark) {
        // Use nautical miles when distToMark is >500m.
        if (distToMark > 500) {
            displayDistToMark = new DecimalFormat("###0.00").format(distToMark / 1852);
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
                ttmDisplay = String.format("%02dh %02d' %02d\"",
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
            ttmDisplay = "--h --' --\"";
        }
        return ttmDisplay;
    }

    public String getTimeVariance(long timeRemain) {
        // Calc early/late to line
        timeVariance = timeRemain - timeToMark;
        if (timeVariance < 360000 && timeVariance > -360000) {
            timeVarDisplay = String.format("%02dh %02d' %02d\"",
                    TimeUnit.SECONDS.toHours(timeVariance),
                    abs(TimeUnit.SECONDS.toMinutes(timeVariance) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeVariance))),
                    abs(TimeUnit.SECONDS.toSeconds(timeVariance) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeVariance))));
        } else {
            timeVarDisplay = "--h --' --\"";
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
