package com.pandocloud.freeiot.utils;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import com.pandocloud.android.utils.NetworkUtil;
import com.pandocloud.android.utils.WifiConnectUtil;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.login.LoginActivity;

public class BusinessUtils {
	
	/**
	 * 根据code检查是否是token失效。
	 * 失效则进入登录界面重新登录
	 * @param activity
	 * @param code
	 */
	public static void checkTokenAvailable(Activity activity, int code) {
		if (code == ErrorCodeHelper.CODE_INVALID_TOKEN) {
			UserState.getInstances(activity).clear();
			ActivityUtils.start(activity, LoginActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			activity.finish();
		}
	}
	
	public static void checkWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
				|| wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
			if (!NetworkUtil.isWifi(context)) {
				WifiConnectUtil.enableAllAps(context);
			}
		}
	}
}
