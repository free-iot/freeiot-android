package com.pandocloud.freeiot.ui.login;


import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.pandocloud.android.api.interfaces.RequestListener;
import com.pandocloud.android.api.interfaces.SimpleRequestListener;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.UserApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.MainActivity;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.UserLoginResponse;
import com.pandocloud.freeiot.ui.helper.ProductInfoHelper;
import com.pandocloud.freeiot.ui.urlconfig.UrlConfigActivity;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.ErrorCodeHelper;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;
import org.apache.http.Header;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;


public class LoginActivity extends BaseActivity implements OnClickListener {
	
	public static final int REIGSTER_REQUEST_CODE = 11;
	
	private EditText mPhoneEditView;
    private EditText mPasswordEditView;
	
	private ProductInfoHelper mProductInfoHelper;
	
	@Override
	protected void onCreate(Bundle savedInstances) {
		super.onCreate(savedInstances);
		
		setContentView(R.layout.activity_login);
		mPhoneEditView = (EditText) findViewById(R.id.mobile);
		mPasswordEditView = (EditText)findViewById(R.id.pwd);
		findViewById(R.id.login_btn).setOnClickListener(this);
		findViewById(R.id.register_btn).setOnClickListener(this);
		findViewById(R.id.reset_btn).setOnClickListener(this);
		findViewById(R.id.title).setOnClickListener(this);

		mProductInfoHelper = new ProductInfoHelper(requestListener);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_LOGIN_ACTIVITY);
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_LOGIN_ACTIVITY);
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title:
			handlerTitleClick();
			break;
		case R.id.login_btn:
			login();
			break;
		case R.id.register_btn:
			ActivityUtils.start(LoginActivity.this, RegisterActivity.class, REIGSTER_REQUEST_CODE, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			break;
		case R.id.reset_btn:
			ActivityUtils.start(LoginActivity.this, ResetPwdActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			break;
		default:
			break;
		}
	}

	private int count = 0;
	private long lastClickTime = 0;
	public void handlerTitleClick() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastClickTime < 500) {
			lastClickTime = currentTime;
			count ++;
			if (count > 6) {
				count = 0;
				ActivityUtils.start(LoginActivity.this, UrlConfigActivity.class, UrlConfigActivity.URL_CONFIG_REQUEST_CODE, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			}
		} else {
			lastClickTime = currentTime;
			count = 1;
		}
	}

	public void login() {
		String phone = mPhoneEditView.getText().toString().trim();
		if (TextUtils.isEmpty(phone)) {
			CommonUtils.ToastMsg(this, R.string.error_empty_mobile);
			return;
		}
		String pwd = mPasswordEditView.getText().toString().trim();
		if (TextUtils.isEmpty(pwd)) {
			CommonUtils.ToastMsg(this, R.string.error_login_pwd_empty);
			return;
		}
		
		MobclickAgent.onEvent(this, AnalyticsUtils.AnalyticsEventKeys.EVENT_LOGIN);
		
		UserApi.login(LoginActivity.this, AppConstants.PRODUCT_KEY, null,
            phone,
            null,
            pwd,
            new BaseJsonHttpResponseHandler<UserLoginResponse>() {

                @Override
                public void onStart() {
                    requestListener.onPrepare();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        Throwable throwable, String rawJsonData, UserLoginResponse errorResponse) {
                    CommonUtils.dismissDialog();
                    WrapperBaseJsonHttpResponseHandler.handlerFailedToast(LoginActivity.this, statusCode, throwable);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                        UserLoginResponse response) {
                    if (response.isSuccess()) {
                        if (response.data != null) {
                            UserState.getInstances(LoginActivity.this).saveAccessToken(response.data.access_token);
                        }
                        mProductInfoHelper.getProductInfo(LoginActivity.this);
                    } else {
                        CommonUtils.dismissDialog();
                        CommonUtils.ToastMsg(LoginActivity.this, ErrorCodeHelper.getInstances(LoginActivity.this).getErrorMessage(response.code));
                    }
                }

                @Override
                protected UserLoginResponse parseResponse(String rawJsonData,
                        boolean isFailure) throws Throwable {
                    LogUtils.d("UserApi#login-> parseResponse: " + rawJsonData);
                    if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                        return GsonUtils.getInstance().getGson().fromJson(rawJsonData, UserLoginResponse.class);
                    }
                    return null;
                }

            });
	}
	
	private RequestListener requestListener = new SimpleRequestListener() {
		
		public void onPrepare() {
			CommonUtils.showProgressDialog(LoginActivity.this, null, getString(R.string.logining), new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    LogUtils.d("progress dialog onDissmiss Listener, cancel login...");
                    UserApi.cancel(LoginActivity.this, true);
                }
            });
		}
		
		@Override
		public void onSuccess() {
			CommonUtils.ToastMsg(LoginActivity.this, R.string.login_success);
			ActivityUtils.start(LoginActivity.this, MainActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			finish();
		}
		
		@Override
		public void onFinish() {
			CommonUtils.dismissDialog();
		}
		
		@Override
		public void onFail(Exception e) {
			UserState.getInstances(LoginActivity.this).clear();
			CommonUtils.ToastMsg(LoginActivity.this, R.string.login_failed);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REIGSTER_REQUEST_CODE) {
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onDestroy() {
		UserApi.cancel(LoginActivity.this, true);
		super.onDestroy();
	}
}
