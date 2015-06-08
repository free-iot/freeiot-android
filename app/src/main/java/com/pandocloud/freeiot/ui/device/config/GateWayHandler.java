package com.pandocloud.freeiot.ui.device.config;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.pandocloud.android.config.handler.GateWayMsgHandler;


public class GateWayHandler extends GateWayMsgHandler {

	public GateWayHandler(Context context, Handler handler) {
		super(handler);
	}

	@Override
	public void handlerGateWayMessage(Message msg) {
		getHandler().handleMessage(msg);
	}

}
