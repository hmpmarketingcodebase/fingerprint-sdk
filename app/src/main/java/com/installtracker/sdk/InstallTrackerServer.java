package com.installtracker.sdk;

import android.util.Log;

import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Created by Rameez Usmani on 6/16/2017.
 */

public class InstallTrackerServer {
    private static final String TAG=InstallTrackerServer.class.getName();

    //private static final String API_URL="http://rameezusmani.com/api.php?x=1";
    private static final String API_URL="https://fingerprint.adserver3.com/trackerfeed?x=1";

    /*public static List<String> getUrlsFromApi(String packageName,String appName,String deviceId,String gaId)
    throws Exception{
        List<String> urls=new Vector<String>();
        String url=API_URL+"&package="+HttpUtil.encodeString(packageName);
        url+="&device_id="+HttpUtil.encodeString(deviceId);
        url+="&gaid="+HttpUtil.encodeString(gaId);
        Log.d(TAG,url);
        String response=HttpUtil.getHttpResponseBody(url);
        JSONObject jobj=new JSONObject(response);
        if (jobj.has("url")){
            urls.add(jobj.getString("url"));
        }
        return urls;
    }*/

    public static List<String> getUrlsFromApi(String packageName,String appName,String installedAppName,String deviceId,String gaId)
            throws Exception{
        List<String> urls=new Vector<String>();
        String url=API_URL;
        Hashtable<String,String> formData=new Hashtable<String,String>();
        formData.put("installed_package_name",packageName);
        formData.put("device_id",deviceId);
        formData.put("gaid",gaId);
        //formData.put("app_name",appName);
        formData.put("package_name",installedAppName);
        Log.d(TAG,url);
        String response=HttpUtil.postDataAndGetResponse(url,formData);
        JSONObject jobj=new JSONObject(response);
        if (jobj.has("url")){
            urls.add(jobj.getString("url"));
        }
        return urls;
    }

    public static void visitTheUrl(String url)
    throws Exception{
        //String url="http://strikingmobile.com/302";
        HttpUtil.getHttpResponseBody(url);
    }
}
