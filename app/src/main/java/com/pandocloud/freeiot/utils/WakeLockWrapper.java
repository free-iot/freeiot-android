package com.pandocloud.freeiot.utils;


import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.PowerManager;


public class WakeLockWrapper {

	static final ConcurrentHashMap<String, PowerManager.WakeLock> sWakeLockMap = new ConcurrentHashMap<String, PowerManager.WakeLock>();

	public static PowerManager.WakeLock getWakeLockInstance(Context context, String tag) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

		if(sWakeLockMap.containsKey(tag) == false) {
			sWakeLockMap.put(tag, powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag));
		}
		return sWakeLockMap.get(tag);
	}
}
