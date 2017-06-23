package com.installtracker.sdk;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.provider.Settings.Secure;
import android.webkit.WebView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.util.List;

public class InstallTrackerService
extends Service{
	
	private static final String TAG=InstallTrackerService.class.getName();

    private static final String APP_INSTALLED_ACTION=Intent.ACTION_PACKAGE_ADDED;
    private static final String APP_UNINSTALLED_ACTION=Intent.ACTION_PACKAGE_REMOVED;
	
	private InstallBroadcastReceiver installBroadcastReceiver=null;

    @Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate(){
		Log.d(TAG,"onCreate");
	}
	
	@Override
	public void onDestroy(){
		Log.d(TAG,"onDestroy");
		unregisterReceiver(installBroadcastReceiver);
	}
	
	@Override
	public int onStartCommand(Intent i,int flags,int startId){
		Log.d(TAG,"onStartCommand");
        doStartCommand();
		return START_STICKY;
	}

    private void doStartCommand(){
        installBroadcastReceiver = new InstallBroadcastReceiver();
        registerRouteBroadcastReceiver();

        String uAgent=new WebView(this).getSettings().getUserAgentString();
        Log.d(TAG,"User-Agent: "+uAgent);
        HttpUtil.CurrentUserAgent=uAgent;

        //startFollowing("package:com.sabapp");
    }

    private void registerRouteBroadcastReceiver(){
        IntentFilter ifilter=new IntentFilter(APP_INSTALLED_ACTION);
        ifilter.addAction(APP_UNINSTALLED_ACTION);
        ifilter.addDataScheme("package");
        registerReceiver(installBroadcastReceiver,ifilter);
    }

    private void startFollowing(final String packageName){
        Thread thr=new Thread(){
            public void run(){
                getUrlsAndFollow(packageName);
                /*try{
                    Log.d(TAG,"Visiting http://strikingmobile.com/302");
                    InstallTrackerServer.visitTheUrl("http://strikingmobile.com/302");
                    Log.d(TAG,"Visited");
                }catch(Exception ex){
                    Log.e(TAG,"Exception: "+ex.getMessage());
                }*/
            }
        };
        thr.start();
    }

    private String getApplicationName(String packageName){
        Log.d(TAG,"getApplicationName: "+packageName);
        PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName,0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,"NameNotFoundException: "+e.getMessage());
            ai = null;
        }
        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        return applicationName;
    }

    private void getUrlsAndFollow(String packageName){
        if (packageName==null){
            packageName="package:null";
        }
        Log.d(TAG,"getUrlsAndFollow: "+packageName);
        String android_id = Secure.getString(getContentResolver(),Secure.ANDROID_ID);
        String gaid="";
        try {
            AdvertisingIdClient.Info inf=AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
            if (inf!=null){
                gaid=inf.getId();
            }else{
                Log.e(TAG,"Inf is null");
            }
        }catch(Exception ex){
            Log.e(TAG,"Exception in getting AdvertisingIdInfo: "+ex.getMessage());
        }
        Log.d(TAG,"GAID: "+gaid);
        Log.d(TAG,"Device ID: "+android_id);
        String[] vals=packageName.split(":");
        if (vals.length>1){
            packageName=vals[1];
        }else{
            packageName=vals[0];
        }
        Log.d(TAG,"Package: "+packageName);
        String appName=getApplicationName(packageName);
        Log.d(TAG,"Application name: "+appName);
        //String installedAppName=getApplicationName(getApplicationContext().getPackageName());
        String installedAppName=getApplicationContext().getPackageName();
        Log.d(TAG,"Installed app name: "+installedAppName);
        try{
            List<String> urls=InstallTrackerServer.getUrlsFromApi(packageName,appName,installedAppName,android_id,gaid);
            for (int a=0;a<urls.size();a++){
                Log.d(TAG,"Following: "+urls.get(a));
                try{
                    String ur=urls.get(a);
                    //ur=ur.replace("[INSERTDEVICEID]",HttpUtil.encodeString(android_id));
                    //Log.d(TAG,"Replaced: "+ur);
                    String body=HttpUtil.getHttpResponseBody(ur);
                    Log.d(TAG,"Follow completed for: "+ur);
                }catch(Exception ex){
                    Log.e(TAG,"Exception in following: "+ex.getMessage());
                }
            }

        }catch(Exception ex){
            Log.e(TAG,"Exception: "+ex.getMessage());
        }
    }

    class InstallBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().compareTo(APP_INSTALLED_ACTION)==0) {
                try {
                    Log.d(TAG, "Received: " + intent.getAction());
                    Log.d(TAG, "Uri: " + intent.getData().toString());
                    Log.d(TAG, "String: " + intent.getDataString());
                    String packageString = intent.getDataString();
                    if (packageString == null) {
                        Log.e(TAG, "packageString is null");
                    } else {
                        startFollowing(packageString);
                    }
                }catch(Exception ex){
                    Log.e(TAG,"Error: "+ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
}
