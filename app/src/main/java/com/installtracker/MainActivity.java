package com.installtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.installtracker.sdk.InstallTrackerService;
import com.installtracker.sdk.SdkHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SdkHelper.startTrackingInstalls(this);
    }
}
