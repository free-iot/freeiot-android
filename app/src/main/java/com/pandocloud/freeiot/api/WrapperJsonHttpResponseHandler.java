package com.pandocloud.freeiot.api;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.utils.BusinessUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.ErrorCodeHelper;

public class WrapperJsonHttpResponseHandler extends JsonHttpResponseHandler {
	
	private Activity activity;
	
	public WrapperJsonHttpResponseHandler(Activity activity) {
		this.activity = activity;
	}
	
	@Override
	public void onFailure(int statusCode, Header[] headers,
			Throwable throwable, JSONObject errorResponse) {
		WrapperBaseJsonHttpResponseHandler.handlerFailedToast(activity, statusCode, throwable);
	}
	
	@Override
	public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
		if (response.has("code")) {
			try {
				int code = response.getInt("code");
				if (code != BaseResponse.CODE_SUCCESS) {
					CommonUtils.ToastMsg(activity, ErrorCodeHelper.getInstances(activity).getErrorMessage(code));
					BusinessUtils.checkTokenAvailable(activity, code);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
