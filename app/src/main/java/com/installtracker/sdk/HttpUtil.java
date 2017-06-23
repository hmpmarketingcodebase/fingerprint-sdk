package com.installtracker.sdk;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;

import android.util.Log;

public class HttpUtil {

    private static final String TAG=HttpUtil.class.getName();

    public static String CurrentUserAgent="Android";
	
	public static String encodeString(String unEncodedString){
		return URLUTF8Encoder.encode(unEncodedString);
	}
		
    public static HttpURLConnection getHttpConnection(String u,boolean doInput,boolean doOutput,boolean useCaches)
    throws Exception{
    	URL url=new URL(u);
    	try{
    		HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
    		if (conn==null){
    			throw new Exception("Conn in getHttpConnection is null");
    		}
    		try{
    			conn.setDoInput(doInput);
    			conn.setDoOutput(doOutput);
    		}catch(Exception ex){}
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent",CurrentUserAgent);
    		return conn;
    	}catch(Exception ex){
    		throw new Exception("Error in url.openConnection(): "+ex.getMessage());
    	}        
    }

	private static void setCommonHeaders(HttpURLConnection sconn,String method,String contentType,String contentLength)
			throws Exception{
		sconn.setRequestMethod(method);
		sconn.setRequestProperty("Cache-Control","no-cache");
		//sconn.setRequestProperty("Connection","keep-alive");
		sconn.setRequestProperty("Pragma","no-cache");
		sconn.setRequestProperty("Accept","text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
		sconn.setRequestProperty("User-Agent","Android");
		sconn.setRequestProperty("Content-Length",contentLength);
		sconn.setRequestProperty("Content-Type",contentType);
	}
    
    public static String encodeUTF8Data(String name,String val){
    	return name+"="+URLUTF8Encoder.encode(val);
    }
    
    public static String getHttpResponseBody(String url)
    throws Exception{
    	HttpURLConnection sconn = getHttpConnection(url,true,false,false);
    	if (sconn==null){
    		throw new Exception("sconn is null");    		
    	}    	
    	InputStream is=null;
    	try{
    		is=sconn.getInputStream();
    	}catch(Exception ex){
    		throw new Exception("Error in getInputStream(): "+ex.getMessage());
    	}
    	int rCode=200;
    	
    	try{
    		rCode=sconn.getResponseCode();   
    		Log.d("HttpUtil","responsecode: "+String.valueOf(rCode));
    	}catch(Exception ex){
    		throw new Exception("Error in getResponseCode(): "+ex.getMessage());
    	}

        if (rCode!=200){
            if (rCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || rCode == HttpURLConnection.HTTP_MOVED_PERM
                    || rCode == HttpURLConnection.HTTP_SEE_OTHER){
                Log.d(TAG,"Redirect true");
                String urlToRedirect=sconn.getHeaderField("Location");
                Log.d(TAG,"Redirecting to: "+urlToRedirect);
                try{
                    is.close();
                }catch(Exception ex){
                }

                try{
                    sconn.disconnect();
                }catch(Exception ex){}

                return getHttpResponseBody(urlToRedirect);
            }else {
                throw new Exception("Status code is " + String.valueOf(rCode));
            }
    	}	
    	
        if (is==null){
        	rCode=sconn.getResponseCode();
        	throw new Exception("InputStream is null and status code is "+String.valueOf(rCode));
        }
        
        byte[] buff=new byte[1024];
        String body="";
        int bread=0;
        
        try{
        	while((bread=is.read(buff,0,1024))!=-1){
        		body+=new String(buff,0,bread);
        	}
        }catch(Exception ex){
        	throw new Exception("Error in is.read(): "+ex.getMessage());
        }
        
        try{
        	is.close();
        }catch(Exception ex){
        	throw new Exception("Error in is.close(): "+ex.getMessage());
        }
        
        try{
        	sconn.disconnect();
        }catch(Exception ex){}

        return body;
    }

	private static String readResponseBodyAsString(HttpURLConnection sconn,boolean tryReadError)
			throws Exception {
		InputStream is=null;
		try{
			is=sconn.getInputStream();
		}catch(Exception ex){
			Log.e(TAG,"Getting is error");
			if (tryReadError){
				try{
					is=sconn.getErrorStream();
				}catch(Exception ex2){
					throw new Exception("Could not connect to webservice");
				}
			}else{
				//throw new Exception("Error in getInputStream(): "+ex.getMessage());
				throw new Exception("Could not connect to webservice");
			}
		}
		int rCode=200;
		try{
			rCode=sconn.getResponseCode();
			Log.d(TAG,"responsecode: "+String.valueOf(rCode));
		}catch(Exception ex){
			throw new Exception("Connection timed out");
		}
		if (is==null){
			throw new Exception("InputStream is null and status code is "+String.valueOf(rCode));
		}

		byte[] buff=new byte[1024];
		String body="";
		int bread=0;

		try{
			while((bread=is.read(buff,0,1024))!=-1){
				body+=new String(buff,0,bread);
			}
		}catch(Exception ex){
			throw new Exception("Error in is.read(): "+ex.getMessage());
		}
		is.close();
		if (rCode!=200){
			throw new Exception(body);
		}
		return body;
	}

	public static String postDataAndGetResponse(String url,Hashtable<String,String> formData)
			throws Exception {

		//int valCount=formData.size();
		String data="";
		Enumeration<String> en=formData.keys();

		while(en.hasMoreElements()){
			String objKey=en.nextElement();
			String objVal=formData.get(objKey);
			data+=encodeUTF8Data(objKey,objVal);
			data+="&";
		}
		Log.d(TAG,data);
		return postDataAndGetResponse(url,data,null);
	}


	public static String postDataAndGetResponse(String url,String data,String contentType)
			throws Exception {
		return postDataAndGetResponse(url,data.getBytes(),contentType,data);
	}

	public static String postDataAndGetResponse(String url,byte[] data,String contentType,String origData)
			throws Exception {

		int length=data.length;
		String sLength=String.valueOf(length);
		Log.d(TAG,"Length: "+sLength);

		HttpURLConnection sconn=getHttpConnection(url,true,true,false);
		setCommonHeaders(sconn,"POST",contentType,sLength);
		OutputStream os=null;
		try{
			os=sconn.getOutputStream();
			os.write(data);
			String str=readResponseBodyAsString(sconn,true);
			os.close();
			sconn.disconnect();
			return str;
		}catch(Exception ex){
			if (os!=null){
				try{
					os.close();
				}catch(Exception ex2){}
			}
			sconn.disconnect();
			throw ex;
		}
	}
}
