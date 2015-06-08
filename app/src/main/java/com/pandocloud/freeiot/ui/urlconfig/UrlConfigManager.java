package com.pandocloud.freeiot.ui.urlconfig;

import java.lang.reflect.Field;

import android.text.TextUtils;

import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.utils.LogUtils;


public class UrlConfigManager {
	
	public static final int DEVELOP_STATE = 1;
	
	public static final int PREPRODUCT_STATE = 2;
	
	public static final int RELEASE_STATE = 3;
	
	private static int sCurrentState = RELEASE_STATE;
	
	static {
		updateUrl(sCurrentState);
	}
	
	private UrlConfigManager() {
		
	}
	
	protected static String API_HOST_URL;
	
	public static int getCurrentState() {
		return sCurrentState;
	}

	public static void setCurrentState(int currentState) {
		UrlConfigManager.sCurrentState = currentState;
	}
	
	public static void updateUrl(int newState) {
		sCurrentState = newState;
		switch (newState) {
		case DEVELOP_STATE: {
			API_HOST_URL = DevelopUrlConfig.API_HOST_DEV_URL;
		}
			break;
		case PREPRODUCT_STATE: {
			API_HOST_URL = PreProductUrlConfig.API_HOST_PRE_URL;
		}
			break;
		case RELEASE_STATE: {
			API_HOST_URL = ReleaseUrlConfig.API_HOST_RELEASE_URL;
		}
			break;
		default:
			break;
		}
		updateApiHostUrl();
	}
	
	public static void updateApiHostUrl() {
		Class absOpenApiClazz = AbsOpenApi.class;
		try {
			Field[] fields = absOpenApiClazz.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				String str = field.get(null).toString();
				if (!TextUtils.isEmpty(str) && str.contains("pandocloud.com")) {
					LogUtils.e("UrlConfigManager", "url before set: " + str);
					field.set(null, API_HOST_URL);
					String result = field.get(null).toString();
					LogUtils.e("UrlConfigManager", "url set result: " + result);
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
	}
	
	public static String getApiHostUrl() {
		return API_HOST_URL;
	}
	
	public static final class DevelopUrlConfig {
		public static final String API_HOST_DEV_URL = "https://testapi.pandocloud.com";
		
	}
	
	public static final class PreProductUrlConfig {
		public static final String API_HOST_PRE_URL = "https://testapi.pandocloud.com";
	}
	
	public static final class ReleaseUrlConfig {
		public static final String API_HOST_RELEASE_URL = "https://api.pandocloud.com";
	}
}
