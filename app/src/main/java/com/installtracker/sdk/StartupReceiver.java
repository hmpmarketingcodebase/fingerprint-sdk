package com.installtracker.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.installtracker.sdk.SdkHelper;

/**
 * Created by Rameez Usmani on 6/21/2017.
 */

public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SdkHelper.startTrackingInstalls(context);
    }
}
