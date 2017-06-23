package com.installtracker.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Rameez Usmani on 6/20/2017.
 */

public class SdkHelper {
    public static void startTrackingInstalls(Context activity){
        Intent it=new Intent(activity,InstallTrackerService.class);
        activity.startService(it);
    }
}
