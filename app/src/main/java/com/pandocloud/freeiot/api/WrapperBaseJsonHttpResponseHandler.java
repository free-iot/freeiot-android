package com.pandocloud.freeiot.api;

import com.pandocloud.android.utils.NetworkUtil;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.utils.BusinessUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.ErrorCodeHelper;
import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;

import com.loopj.android.http.BaseJsonHttpResponseHandler;

public abstract class WrapperBaseJsonHttpResponseHandler<T extends BaseResponse> extends
		BaseJsonHttpResponseHandler<T> {
	private Activity context;
	
	public WrapperBaseJsonHttpResponseHandler(Activity context) {
		this.context = context;
	}
	
	@Override
	public void onFailure(int statusCode, Header[] headers,
			Throwable throwable, String rawJsonData, T errorResponse) {
		handlerFailedToast(context, statusCode, throwable);
	}

	@Override
	public void onSuccess(int statusCode, Header[] headers, String rawJsonData, T response) {
		if (!response.isSuccess()) {
			int code = response.code;
			CommonUtils.ToastMsg(context, ErrorCodeHelper.getInstances(context).getErrorMessage(code));
			BusinessUtils.checkTokenAvailable(context, code);
		}
	}

	@Override
	protected T parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
		return parseResponse2(rawJsonData, isFailure);
	}
	
	protected abstract T parseResponse2(String rawJsonData, boolean isFailure) throws Throwable;
	
	
	public static void handlerFailedToast(Context context, int statusCode, Throwable throwable) {
		if (!NetworkUtil.isNetworkAvailable(context) || statusCode == 0) {
			CommonUtils.ToastMsg(context, context.getString(R.string.http_error_message));
		} else {
			if (statusCode >= 500) {
				CommonUtils.ToastMsg(context, context.getString(R.string.http_server_error));
			} else {
				if (throwable != null) {
					CommonUtils.ToastMsg(context, throwable.getMessage());
				}
			}
		}
	}
}
