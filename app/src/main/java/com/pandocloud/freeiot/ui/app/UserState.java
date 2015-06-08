package com.pandocloud.freeiot.ui.app;



import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class UserState {
	
	private static final String USER_PREFS_NAME = "pando_user_prefs";
	
	private SharedPreferences mPrefs;
	
	private String accessToken;
	
	private static UserState sInstances;
	
	private void init(Context context) {
		if (mPrefs == null) {
			mPrefs = context.getApplicationContext()
					.getSharedPreferences(USER_PREFS_NAME, Context.MODE_MULTI_PROCESS);
		}
	}
	
	private UserState(Context context) {
		init(context);
	}
	
	public static UserState getInstances(Context context) {
		if (sInstances == null) {
			synchronized (UserState.class) {
				if (sInstances == null) {
					sInstances = new UserState(context);
				}
			}
		}
		return sInstances;
	}
	
	public void saveAccessToken(String value) {
		this.accessToken = value;
		mPrefs.edit().putString("accessToken", value).commit();
	}
	
	public String getAccessToken(String defValue) {
		if (TextUtils.isEmpty(accessToken)) {
			accessToken = mPrefs.getString("accessToken", defValue);
		}
		return accessToken;
	}
	
	public boolean isLogin() {
		return getAccessToken(null) != null;
	}
	
	public void logout(Context context) {
		accessToken = null;
		mPrefs.edit().clear().commit();
		ProductInfoPrefs.getInstances(context).clear();
//		DeviceState.getInstances(context).clear();
	}
	
	public void clear() {
		mPrefs.edit().clear().commit();
	}
	
	public void putString(String key, String value) {
		mPrefs.edit().putString(key, value).commit();
	}
	
	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}
	
	public void putInt(String key, int value) {
		mPrefs.edit().putInt(key, value).commit();
	}
	
	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}
	
	public void putBoolean(String key, boolean value) {
		mPrefs.edit().putBoolean(key, value).commit();
	}
	
	public boolean getBoolean(String key, boolean defValue) {
		return mPrefs.getBoolean(key, defValue);
	}
	
	private SharedPreferences getSharedPreferences() {
		return mPrefs;
	}
	
	public static class Builder {
		
		private SharedPreferences.Editor mEditor;
		
		public Builder(Context context) {
			if (mEditor == null) {
				mEditor = UserState.getInstances(context).getSharedPreferences().edit();
			}
		}
		
		public Builder saveString(String key, String value) {
			mEditor.putString(key, value);
			return this;
		}
		
		public Builder saveLong(String key, long value) {
			mEditor.putLong(key, value);
			return this;
		}
		
		public Builder saveInt(String key, int value) {
			mEditor.putInt(key, value);
			return this;
		}
		
		public void commit(){
			mEditor.commit();
		}
	}

}
