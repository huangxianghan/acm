package com.xianghan.acm.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.java_websocket.drafts.Draft_17;


import com.google.gson.Gson;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class WebsocketService extends Service implements AcmWebsocketClient.AcmMessageCallback  {

	public final static boolean DEBUG = true;
	public final static String TAG = WebsocketService.class.getName();
	public final static String KEY_RTPS_PORT = "rtsp_port";
	public final static String KEY_PORT = "server_port";
	public final static String KEY_SERVER = "server_ip";
	
	protected SharedPreferences mSharedPreferences;
	protected String mProtocol = "ws";
	protected String mServer = "192.168.1.101";
	protected int mPort = 8080;
	protected String mRtspPort = "8086";
	protected String mPath = "/CmAppServer/androidwebsocket";
	protected boolean mRestart = false;
	
	private final IBinder mBinder = new WSBinder();
	
	private AcmWebsocketClient mWebsocketClient;
	
	private final LinkedList<Handler> mConnectionHandlers = new LinkedList<Handler>();
	private final LinkedList<Handler> mMessageHandlers = new LinkedList<Handler>();
	

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
	
	
	public void addMessageHandler(Handler handler) {
		synchronized (mMessageHandlers) {
			if (mMessageHandlers.size() > 0) {
				for (Handler cl : mMessageHandlers) {
					if (cl == handler) return;
				}
			}
			mMessageHandlers.add(handler);			
		}
	}
	
	public void removeMessageHandler(Handler handler) {
		synchronized (mMessageHandlers) {
			mMessageHandlers.remove(handler);				
		}
	}
	
	
	public void addConnectionHandlers(Handler handler){
		synchronized (mConnectionHandlers) {
			if (mConnectionHandlers.size() > 0) {
				for (Handler cl : mConnectionHandlers) {
					if (cl == handler) return;
				}
			}
			mConnectionHandlers.add(handler);
		}
	}
	
	
	public void removeConnectionHandlers(Handler handler){
		synchronized(mConnectionHandlers){
			mConnectionHandlers.remove(handler);
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
		mWebsocketClient.send(json);
		
		if(DEBUG) Log.d(TAG,"发送:"+json);
		
	}
	
	protected void start(){
		if(mRestart) stop();
		
		mWebsocketClient = new AcmWebsocketClient(getWsuri(), new Draft_17());
		//mWebsocketClient.setMessageCallback(this);
		mWebsocketClient.connect();
		
		mRestart = false;
	}
	
	protected void stop(){
		
	}
	
	private URI getWsuri(){
		try {
			return new URI( mProtocol + "://"+mServer+":"+mPort+mPath);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onOpen(short httpStatus, String httpStatusMessage) {
		Message msg = new Message();
		
		for(Handler h : mConnectionHandlers){
			h.sendMessage(msg);
		}
	}

	@Override
	public void onMessage(String msg) {
		
	}

	@Override
	public void onError(Exception ex) {
		
	}

	@Override
	public void onClose(int code, String reason) {
		
	}

	

}


