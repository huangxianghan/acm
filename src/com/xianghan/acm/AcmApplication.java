package com.xianghan.acm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.video.VideoQuality;

public class AcmApplication extends android.app.Application {

	public final static String TAG = "CMonitorApplication";
	
	/** 默认视频流质量. */
	public VideoQuality videoQuality = new VideoQuality(320,240,20,500000);

	/** 默认音频编码器为AMR. */
	public int audioEncoder = SessionBuilder.AUDIO_AAC;

	/** 默认视频编码器为H.263. */
	public int videoEncoder = SessionBuilder.VIDEO_H263;
	
	//public String serverIp = "";
	
	//public String 
	
	/** 开启通知. */
	public boolean notificationEnabled = true;

	public boolean applicationForeground = true;
	public Exception lastCaughtException = null;

	/** Contains an approximation of the battery level. */
	public int batteryLevel = 0;
	
	
	private static AcmApplication sApplication;
	
	/** 获取实例 */
	public static AcmApplication getInstance() {
		return sApplication;
	}

	@Override
	public void onCreate() {
		
		sApplication = this;
		
		super.onCreate();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		//  Android 3.* 不支持 AAC ADTS 所以我们设置默认音频编码器为 AMR-NB, android 4.* AAC 是默认的音频编码器.
		audioEncoder = (Integer.parseInt(android.os.Build.VERSION.SDK)<14) ? SessionBuilder.AUDIO_AMRNB : SessionBuilder.AUDIO_AAC;
		audioEncoder = Integer.parseInt(settings.getString("audio_encoder", String.valueOf(audioEncoder)));
		videoEncoder = Integer.parseInt(settings.getString("video_encoder", String.valueOf(videoEncoder)));
		
		//从preferences读取视频质量设置 
		videoQuality = new VideoQuality(
						settings.getInt("video_resX", videoQuality.resX),
						settings.getInt("video_resY", videoQuality.resY), 
						Integer.parseInt(settings.getString("video_framerate", String.valueOf(videoQuality.framerate))), 
						Integer.parseInt(settings.getString("video_bitrate", String.valueOf(videoQuality.bitrate/1000)))*1000);
		
		SessionBuilder.getInstance() 
		.setContext(getApplicationContext())
		.setAudioEncoder(!settings.getBoolean("stream_audio", true)?0:audioEncoder)
		.setVideoEncoder(videoEncoder)
		.setVideoQuality(videoQuality);
		
		// 侦听Preference改变
		settings.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

		registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
	private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals("video_resX") || key.equals("video_resY")) {
				videoQuality.resX = sharedPreferences.getInt("video_resX", 0);
				videoQuality.resY = sharedPreferences.getInt("video_resY", 0);
			}

			else if (key.equals("video_framerate")) {
				videoQuality.framerate = Integer.parseInt(sharedPreferences.getString("video_framerate", "0"));
			}

			else if (key.equals("video_bitrate")) {
				videoQuality.bitrate = Integer.parseInt(sharedPreferences.getString("video_bitrate", "0"))*1000;
			}

			else if (key.equals("audio_encoder") || key.equals("stream_audio")) { 
				audioEncoder = Integer.parseInt(sharedPreferences.getString("audio_encoder", String.valueOf(audioEncoder)));
				SessionBuilder.getInstance().setAudioEncoder( audioEncoder );
				if (!sharedPreferences.getBoolean("stream_audio", false)) 
					SessionBuilder.getInstance().setAudioEncoder(0);
			}

			else if (key.equals("video_encoder")) {
				videoEncoder = Integer.parseInt(sharedPreferences.getString("video_encoder", String.valueOf(videoEncoder)));
				SessionBuilder.getInstance().setVideoEncoder( videoEncoder );
				
			}

			else if (key.equals("notification_enabled")) {
				notificationEnabled  = sharedPreferences.getBoolean("notification_enabled", true);
			}

		}  
	};
	
	private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			batteryLevel = intent.getIntExtra("level", 0);
		}
	};
}
