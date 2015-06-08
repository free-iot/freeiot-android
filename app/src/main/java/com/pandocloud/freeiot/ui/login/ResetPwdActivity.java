package com.pandocloud.freeiot.ui.login;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.UserApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.bean.http.UserRegisterResponse;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

public class ResetPwdActivity extends BaseActivity implements OnClickListener {

	private EditText mPhoneEditView;
	
	private EditText mAuthCodeEditView;
	
	private EditText mPasswordEditView;
	
	private Timer mTimer;
	
	private int mDexTime = 60;
	
	private TextView tvAuthCode;
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mDexTime--;
			if (mDexTime <=0) {
				tvAuthCode.setEnabled(true);
				tvAuthCode.setText(R.string.get_authcode);
			} else {
				tvAuthCode.setText(getString(R.string.reget_authcode, mDexTime));
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_reset_pwd);
		
		mPhoneEditView = (EditText)findViewById(R.id.mobile);
		mAuthCodeEditView = (EditText)findViewById(R.id.authcode);
		mPasswordEditView = (EditText)findViewById(R.id.pwd);
		
		tvAuthCode = (TextView) findViewById(R.id.authcode_btn);
		tvAuthCode.setOnClickListener(this);
		findViewById(R.id.reset_btn).setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
	}
	
	public void userVertify() {
		String mobile = mPhoneEditView.getText().toString().trim();
		if (TextUtils.isEmpty(mobile)) {
			CommonUtils.ToastMsg(this, R.string.error_empty_mobile);
			return;
		}
		MobclickAgent.onEvent(ResetPwdActivity.this, AnalyticsUtils.AnalyticsEventKeys.EVENT_RESET_PWD_VERTIFY);
		
		UserApi.verification(ResetPwdActivity.this, AppConstants.PRODUCT_KEY, null, mobile, null,
        new WrapperBaseJsonHttpResponseHandler<BaseResponse>(this) {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                  BaseResponse response) {
                if (response.isSuccess()) {
                    CommonUtils.ToastMsg(ResetPwdActivity.this, R.string.send_authcode_success);
                    tvAuthCode.setEnabled(false);
                    if (mTimer == null) {
                        mTimer = new Timer();
                    }
                    mDexTime = 60;
                    mTimer.schedule(new VertifyTimeTask(), 0, 1000);
                } else {
                    CommonUtils.ToastMsg(ResetPwdActivity.this, R.string.send_authcode_failed);
                }
            }

            @Override
            protected BaseResponse parseResponse2(String rawJsonData,
                                                  boolean isFailure) throws Throwable {
                LogUtils.e("ResetPwdActivity", "rawJsonData: " + rawJsonData);
                if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                    return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
                }
                return null;
            }

            @Override
            public void onFinish() {
                CommonUtils.dismissDialog();
            }
        });
	}
	
	
	class VertifyTimeTask extends TimerTask {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(0);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.authcode_btn:
			userVertify();
			break;
		case R.id.reset_btn:
			resetPassword();
			break;
		case R.id.back:
			ActivityUtils.animFinish(this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_RESET_PWD_ACTIVITY);
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_RESET_PWD_ACTIVITY);
		MobclickAgent.onPause(this);
	}
	
	public void resetPassword() {
		String authCode = mAuthCodeEditView.getText().toString().trim();
		final String password = mPasswordEditView.getText().toString().trim();
		if (TextUtils.isEmpty(authCode)) {
			CommonUtils.ToastMsg(this, R.string.error_empty_authcode);
			return;
		}
		if (TextUtils.isEmpty(password)) {
			CommonUtils.ToastMsg(this, R.string.error_empty_pwd);
			return;
		}
		final String phone = mPhoneEditView.getText().toString().trim();
		
		MobclickAgent.onEvent(ResetPwdActivity.this, AnalyticsUtils.AnalyticsEventKeys.EVENT_RESET_PWD);
		
		UserApi.reset(ResetPwdActivity.this, AppConstants.PRODUCT_KEY, null,
				phone, null, password, authCode, new WrapperBaseJsonHttpResponseHandler<UserRegisterResponse>(ResetPwdActivity.this) {
					
					@Override
					public void onStart() {
						CommonUtils.showProgressDialog(ResetPwdActivity.this, null, null, new OnDismissListener() {
							
							@Override
							public void onDismiss(DialogInterface dialog) {
								UserApi.cancel(ResetPwdActivity.this, true);
							}
						});	
					}
				
					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable throwable, String rawJsonData,
							UserRegisterResponse errorResponse) {
						super.onFailure(statusCode, headers, throwable, rawJsonData, errorResponse);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
							UserRegisterResponse response) {
						if (response.isSuccess()) {
							if (mTimer != null) {
								mTimer.cancel();
								mTimer = null;
							}
							if (response.data != null) {
								UserState.getInstances(ResetPwdActivity.this).saveAccessToken(response.data.access_token);
							}
							CommonUtils.ToastMsg(ResetPwdActivity.this, R.string.reset_pwd_success);
							ActivityUtils.animFinish(ResetPwdActivity.this, R.anim.slide_out_to_right, R.anim.slide_in_from_left);
						} else {
							super.onSuccess(statusCode, headers, rawJsonData, response);	
						}
					}

					@Override
					protected UserRegisterResponse parseResponse2(String rawJsonData,
							boolean isFailure) throws Throwable {
						LogUtils.d("UserApi#reset pwd-> parseResponse: " + rawJsonData);
						if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
							return GsonUtils.getInstance().getGson().fromJson(rawJsonData, UserRegisterResponse.class);
						}
						return null;
					}
					
					@Override
					public void onFinish() {
						CommonUtils.dismissDialog();
					}
				});

	}
	
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
	
	
	@Override
	protected void onDestroy() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		UserApi.cancel(ResetPwdActivity.this, true);
		super.onDestroy();
	}
}
