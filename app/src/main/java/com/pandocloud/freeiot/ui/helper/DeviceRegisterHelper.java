package com.pandocloud.freeiot.ui.helper;


import android.content.Context;

import com.pandocloud.android.api.DeviceLoginManager;
import com.pandocloud.android.api.DeviceState;
import com.pandocloud.android.api.interfaces.SimpleRequestListener;
import com.pandocloud.freeiot.ui.app.AppConstants;

public class DeviceRegisterHelper {
	
	private static DeviceRegisterHelper sInstances;
	
	private int tryCount = 0;
	
	private static final int MAX_TRY_COUNT = 3;
	
	private DeviceRegisterHelper(){
	}
	
	public static DeviceRegisterHelper getInstances() {
		if (sInstances == null) {
			synchronized (DeviceRegisterHelper.class) {
				if (sInstances == null) {
					sInstances = new DeviceRegisterHelper();
				}
			}
		}
		return sInstances;
	}

	public void checkDeviceRegister(Context context) {
		if (!DeviceState.getInstances(context).hasAccessToken()) {
			registerDevice(context);
		}
	}
	
	private void registerDevice(final Context context) {
		tryCount ++;
		if (tryCount > MAX_TRY_COUNT) {
			tryCount = 0;
			return;
		}
		DeviceLoginManager.getInstances().registerDevice(context,
				AppConstants.VENDOR_KEY,
				AppConstants.PRODUCT_KEY,
			    new SimpleRequestListener(){
			
			@Override
			public void onFail(Exception e) {
				e.printStackTrace();
				registerDevice(context);
			}
			
			@Override
			public void onSuccess() {
				super.onSuccess();
				tryCount = 0;
			}
		});
	}
}
