package com.pandocloud.freeiot.jsbridge;


import com.pandocloud.freeiot.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class DefaultHandler implements BridgeHandler{

	String TAG = "DefaultHandler";
	
	@Override
	public void handler(String data, CallBackFunction function) {
		LogUtils.i(TAG, "receive data" + data);
		if(function != null){
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("data", "DefaultHandler response data");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			function.onCallBack(jsonObject);
		}
	}

}
