package com.xianghan.acm.ui;

import java.util.Locale;


import net.majorkernelpanic.spydroid.api.CustomRtspServer;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import com.xianghan.acm.R;
import com.xianghan.acm.AcmApplication;
import com.xianghan.acm.Utilities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class PreviewActivity extends ActionBarActivity {

	private static final String TAG = "PreviewActivity";
	private AcmApplication mApplication;
	private SurfaceView mSurfaceView;
	private TextView textMessage,mTextBitrate;
	private RtspServer mRtspServer;
	private PowerManager.WakeLock mWakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		mApplication = (AcmApplication) getApplication();
		textMessage = (TextView)findViewById(R.id.textMessage);
		mTextBitrate = (TextView)findViewById(R.id.textBitrate);
		mSurfaceView = (SurfaceView)findViewById(R.id.camera_view);
		SessionBuilder.getInstance().setSurfaceView(mSurfaceView);
		
		SessionBuilder.getInstance().setPreviewOrientation(90);
		
		// Prevents the phone from going to sleep mode
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "net.majorkernelpanic.spydroid.wakelock");
		
		this.startService(new Intent(this,CustomRtspServer.class));
	}

	

	@Override
	protected void onStart() {
		super.onStart();
		
		mWakeLock.acquire();
		bindService(new Intent(this,CustomRtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (mWakeLock.isHeld()) mWakeLock.release();
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mApplication.applicationForeground = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mApplication.applicationForeground = false;
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG,"PreviewActivity destroyed");
		super.onDestroy();
	}
	
	@Override    
	public void onBackPressed() {
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	
	private ServiceConnection mRtspServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mRtspServer = (CustomRtspServer) ((RtspServer.LocalBinder)service).getService();
			mRtspServer.addCallbackListener(mRtspCallbackListener);
			mRtspServer.start();
			update();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {}

	};
	
	
	private RtspServer.CallbackListener mRtspCallbackListener = new RtspServer.CallbackListener() {

		@Override
		public void onError(RtspServer server, Exception e, int error) {
			// We alert the user that the port is already used by another app.
			if (error == RtspServer.ERROR_BIND_FAILED) {
				new AlertDialog.Builder(PreviewActivity.this)
				.setTitle(R.string.port_used)
				.setMessage(getString(R.string.bind_failed, "RTSP"))
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivityForResult(new Intent(PreviewActivity.this, OptionsActivity.class),0);
					}
				})
				.show();
			}
		}

		@Override
		public void onMessage(RtspServer server, int message) {
			if (message==RtspServer.MESSAGE_STREAMING_STARTED) {
				update();
			} else if (message==RtspServer.MESSAGE_STREAMING_STOPPED) {
				update();
			}
		}

	};
	
	private void update(){
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (!mRtspServer.isStreaming())
					displayIpAddress();
				else
					streamingState(1);
			}
		});
	}
	
	private void displayIpAddress() {
		WifiManager wifiManager = (WifiManager) mApplication.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		String ipaddress = null;
    	if (info!=null && info.getNetworkId()>-1) {
	    	int i = info.getIpAddress();
	        String ip = String.format(Locale.ENGLISH,"%d.%d.%d.%d", i & 0xff, i >> 8 & 0xff,i >> 16 & 0xff,i >> 24 & 0xff);
	    	
	        textMessage.setText("rtsp://");
	        textMessage.append(ip);
	        textMessage.append(":"+mRtspServer.getPort());
	    	streamingState(0);
	    	Log.d(TAG,"msg rtsp//:"+ip+":"+mRtspServer.getPort());
    	} else if((ipaddress = Utilities.getLocalIpAddress(true)) != null) {
    		
    		textMessage.setText("rtsp://");
    		textMessage.append(ipaddress);
    		textMessage.append(":"+mRtspServer.getPort());
	    	streamingState(0);
	    	Log.d(TAG,"msg rtsp//:"+ipaddress+":"+mRtspServer.getPort());
    	} else {
    		streamingState(2);
    	}
	}
	
	private void streamingState(int state) {
		if(state == 0){
			//没实现
		}else if(state == 1){
			//更新传输比特率
			mHandler.post(mUpdateBitrate);
		}else if(state == 2){
			//没实现
		}
	}

	private void removeNotification() {
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
	}

	public void log(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
	
	private void quitCMonitor() {
		// Removes notification
		if (mApplication.notificationEnabled) removeNotification();       
		
		// Kills RTSP server
		this.stopService(new Intent(this,CustomRtspServer.class));
		// Returns to home menu
		finish();
	}
	
	private final Handler mHandler = new Handler();
    
	private Runnable mUpdateBitrate = new Runnable() {
		@Override
		public void run() {
			if (mRtspServer != null && mRtspServer.isStreaming()) {
				long bitrate = 0;
				bitrate += mRtspServer!=null?mRtspServer.getBitrate():0;
				mTextBitrate.setText(""+bitrate/1000+" kbps");
				mHandler.postDelayed(mUpdateBitrate, 1000);
			} else {
				mTextBitrate.setText("0 kbps");
			}
		}
	};
}
