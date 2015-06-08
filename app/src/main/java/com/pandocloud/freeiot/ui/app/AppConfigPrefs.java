package com.pandocloud.freeiot.ui.app;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfigPrefs {
	
	private final static String PREFS_CONFIG_NAME = "app_config_prefs";
	
	private static AppConfigPrefs sInstances;
	
	private SharedPreferences mPreferences;
	
	
	private AppConfigPrefs(Context context) {
		mPreferences = context.getSharedPreferences(PREFS_CONFIG_NAME, Context.MODE_PRIVATE);
	}
	
	/**
	 * 不能多线程操作
	 * @param context
	 * @return
	 */
	public static AppConfigPrefs getInstances(Context context) {
		if (sInstances == null) {
			sInstances = new AppConfigPrefs(context);
		}
		return sInstances;
	}
	
	public void saveStringValue(String key, String value) {
		mPreferences.edit().putString(key, value).commit();
	}
	
	public void saveBooleanValue(String key, boolean value) {
		mPreferences.edit().putBoolean(key, value).commit();
	}
	
	public String getStringValue(String key, String defValue) {
		return mPreferences.getString(key, defValue);
	}
	
	public boolean getBooleanValue(String key, boolean defValue) {
		return mPreferences.getBoolean(key, defValue);
	}
	
	public void saveIntValue(String key, int value) {
		mPreferences.edit().putInt(key, value).commit();
	}
	
	public int getIntValue(String key, int defValue) {
		return mPreferences.getInt(key, defValue);
	}
	
	public boolean containKey(String key) {
		return mPreferences.contains(key);
	}
	
	public void saveFloatValue(String key, float value) {
		mPreferences.edit().putFloat(key, value).commit();
	}
	
	public float getFloatValue(String key, float defValue) {
		return mPreferences.getFloat(key, defValue);
	}
	
	public void removeKey(String key){
		mPreferences.edit().remove(key).commit();
	}
}
