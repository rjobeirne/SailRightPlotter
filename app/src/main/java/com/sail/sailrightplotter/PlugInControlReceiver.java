package com.sail.sailrightplotter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlugInControlReceiver extends BroadcastReceiver {
         String action;
         boolean charging;

    public void onReceive(Context context , Intent intent) {
        action = intent.getAction();

        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            Log.e("Charging", " ** Charging");
            charging = true;
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Log.e("Not Charging", " ** Not Charging");
            // Do something when power disconnected
            charging = false;
        }
    }

    public Boolean onCharge() {
        return charging;
    }
}
