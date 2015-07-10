package com.pandocloud.freeiot.ui.device.config;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.pandocloud.android.config.wifi.WifiConfigMessageHandler;


public class GateWayHandler extends WifiConfigMessageHandler {

	public GateWayHandler(Context context, Handler handler) {
		super(handler);
	}

	@Override
	public void handleMessage(Message msg) {
		getHandler().handleMessage(msg);
	}

}
