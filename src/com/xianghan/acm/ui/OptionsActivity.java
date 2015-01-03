package com.xianghan.acm.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xianghan.acm.R;
import com.xianghan.acm.AcmApplication;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class OptionsActivity extends PreferenceActivity {

	
	private AcmApplication mApplication;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (AcmApplication)getApplication();
		addPreferencesFromResource(R.xml.preferences);
		
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		final Preference audioEnabled = findPreference("stream_audio");
		final ListPreference audioEncoder = (ListPreference) findPreference("audio_encoder");
		final ListPreference videoEncoder = (ListPreference) findPreference("video_encoder");
		final ListPreference videoResolution = (ListPreference) findPreference("video_resolution");
		final ListPreference videoBitrate = (ListPreference) findPreference("video_bitrate");
		final ListPreference videoFramerate = (ListPreference) findPreference("video_framerate");
		
		
		videoEncoder.setValue(String.valueOf(mApplication.videoEncoder));
		audioEncoder.setValue(String.valueOf(mApplication.audioEncoder));
		videoFramerate.setValue(String.valueOf(mApplication.videoQuality.framerate));
		videoBitrate.setValue(String.valueOf(mApplication.videoQuality.bitrate/1000));
		videoResolution.setValue(mApplication.videoQuality.resX+"x"+mApplication.videoQuality.resY);

		videoResolution.setSummary(getString(R.string.settings8_1)+" "+videoResolution.getValue()+"px");
		videoFramerate.setSummary(getString(R.string.settings9_1)+" "+videoFramerate.getValue()+"fps");
		videoBitrate.setSummary(getString(R.string.settings10_1)+" "+videoBitrate.getValue()+"kbps");
		
		audioEncoder.setEnabled(settings.getBoolean("stream_audio", false));
		
		videoResolution.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Editor editor = settings.edit();
				Pattern pattern = Pattern.compile("([0-9]+)x([0-9]+)");
				Matcher matcher = pattern.matcher((String)newValue);
				matcher.find();
				editor.putInt("video_resX", Integer.parseInt(matcher.group(1)));
				editor.putInt("video_resY", Integer.parseInt(matcher.group(2)));
				editor.commit();
				videoResolution.setSummary(getString(R.string.settings8_1)+" "+(String)newValue+"px");
				return true;
			}
		});

		videoFramerate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				videoFramerate.setSummary(getString(R.string.settings9_1)+" "+(String)newValue+"fps");
				return true;
			}
		});

		videoBitrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				videoBitrate.setSummary(getString(R.string.settings10_1)+" "+(String)newValue+"kbps");
				return true;
			}
		});
		
		audioEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean state = (Boolean)newValue;
				audioEncoder.setEnabled(state);
				return true;
			}
		});
	}

	
}
