package com.xianghan.acm.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.gson.Gson;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class WebsocketService extends Service  {

	public final static boolean DEBUG = true;
	public final static String TAG = WebsocketService.class.getName();
	public final static String KEY_RTPS_PORT = "rtsp_port";
	public final static String KEY_PORT = "server_port";
	public final static String KEY_SERVER = "server_ip";
	
	protected SharedPreferences mSharedPreferences;
	protected String mProtocol = "ws";
	protected String mServer = "192.168.1.100";
	protected int mPort = 8080;
	protected String mRtspPort = "8086";
	protected String mPath = "/CmAppServer/androidwebsocket";
	protected boolean mRestart = false;
	
	private final IBinder mBinder = new WSBinder();
	
	
	
	private final LinkedList<WebsocketServiceCallbackListener> mListeners = new LinkedList<WebsocketServiceCallbackListener>();
	
	public interface WebsocketServiceCallbackListener{
		public void onMessage( AppMessage message );
	}

	public class WSBinder extends Binder{
		public WebsocketService getService(){
			return WebsocketService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mPort = Integer.parseInt(mSharedPreferences.getString(KEY_PORT, String.valueOf(mPort)));
		mServer = mSharedPreferences.getString(KEY_SERVER, mServer);
		mRtspPort = mSharedPreferences.getString(KEY_RTPS_PORT, mRtspPort);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
		start();
	}

	@Override
	public void onDestroy() {
		if(DEBUG) Log.d(TAG,TAG + " 销毁");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	/*
	 * 配置文件改变侦听器
	 */
	private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener(){

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			
			if (key.equals(KEY_PORT) ) {
				int port = Integer.parseInt(sharedPreferences.getString(KEY_PORT, String.valueOf(mPort)));
				if (port != mPort) {
					mPort = port;
					mRestart = true;
					start();
				}
			}else if(key.equals(KEY_SERVER)){
				String server = sharedPreferences.getString(KEY_SERVER, mServer);
				if(!mServer.equals(server)){
					mServer = server;
					mRestart = true;
				}
			}else if(key.equals(KEY_RTPS_PORT)){
				String rtsp_port = sharedPreferences.getString(KEY_RTPS_PORT, mRtspPort);
				if(!rtsp_port.equals(mRtspPort)){
					mRtspPort = rtsp_port;
					start();
				}
			}
		}
		
	};
	
	
	public void addCallbackListener(WebsocketServiceCallbackListener listener) {
		synchronized (mListeners) {
			if (mListeners.size() > 0) {
				for (WebsocketServiceCallbackListener cl : mListeners) {
					if (cl == listener) return;
				}
			}
			mListeners.add(listener);			
		}
	}
	
	public void removeCallbackListener(WebsocketServiceCallbackListener listener) {
		synchronized (mListeners) {
			mListeners.remove(listener);				
		}
	}
	
	private void postMessage(AppMessage message){
		synchronized (mListeners) {
			if (mListeners.size() > 0) {
				for (WebsocketServiceCallbackListener cl : mListeners) {
					cl.onMessage(message);
				}
			}			
		}
	}
	
	
	public void login(String user,String pwd,boolean reconnect){
		
		AppMessage jm = new AppMessage();
		jm.setC(AppMessage.USER_LOGIN);
		String[] d = new String[]{
				user,
				pwd,
				mRtspPort,
				"android",
				android.os.Build.VERSION.RELEASE
		};
		jm.setD(d);
		
		String json = "";
		
		try {
			json = jm.toJson();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		
		if(DEBUG) Log.d(TAG,"发送:"+json);
		
	}
	
	protected void start(){
		if(mRestart) stop();
		
		
		
		mRestart = false;
	}
	
	protected void stop(){
		
	}
	
	private String getWsuri(){
		return mProtocol + "://"+mServer+":"+mPort+mPath;
	}

	

}


