package com.pandocloud.freeiot.ui.app;

import android.content.Context;
import android.content.SharedPreferences;


public class ProductInfoPrefs {

	private static final String USER_PREFS_NAME = "pando_product_infos_prefs";

	private SharedPreferences mPrefs;

	private static ProductInfoPrefs sInstances;

	private void init(Context context) {
		if (mPrefs == null) {
			mPrefs = context.getApplicationContext().getSharedPreferences(
					USER_PREFS_NAME, Context.MODE_PRIVATE);
		}
	}

	private ProductInfoPrefs(Context context) {
		init(context);
	}

	public static ProductInfoPrefs getInstances(Context context) {
		if (sInstances == null) {
			synchronized (ProductInfoPrefs.class) {
				if (sInstances == null) {
					sInstances = new ProductInfoPrefs(context);
				}
			}
		}
		return sInstances;
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
				mEditor = ProductInfoPrefs.getInstances(context)
						.getSharedPreferences().edit();
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

		public void commit() {
			mEditor.commit();
		}
	}

}
