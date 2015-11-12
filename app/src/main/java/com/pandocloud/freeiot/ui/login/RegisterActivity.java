package com.pandocloud.freeiot.ui.login;

import java.util.Timer;
import java.util.TimerTask;

import com.pandocloud.android.api.interfaces.RequestListener;
import com.pandocloud.android.api.interfaces.SimpleRequestListener;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.UserApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.MainActivity;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.bean.http.UserLoginResponse;
import com.pandocloud.freeiot.ui.bean.http.UserRegisterResponse;
import com.pandocloud.freeiot.ui.helper.ProductInfoHelper;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;


public class RegisterActivity extends BaseActivity implements OnClickListener {

	public static final String MOBILE_REGX = "^((\\+86)|(86))?((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[0-9]))\\d{8}$";
	
	private EditText mPhoneEditView;

	private EditText mAuthCodeEditView;

	private EditText mPasswordEditView;
	
	private Timer mTimer;
	
	private int mDexTime = 60;
	
	private TextView mAuthCodeTextView;
	
	private ProductInfoHelper mProductInfoHelper;
	
	
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mDexTime--;
			if (mDexTime <=0) {
				mAuthCodeTextView.setEnabled(true);
				mAuthCodeTextView.setText(R.string.get_authcode);
			} else {
				mAuthCodeTextView.setText(getString(R.string.reget_authcode, mDexTime));
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_register);
		
		mProductInfoHelper = new ProductInfoHelper(productRequestListener);
		
		mPhoneEditView = (EditText)findViewById(R.id.mobile);
		mAuthCodeEditView = (EditText)findViewById(R.id.authcode);
		mPasswordEditView = (EditText)findViewById(R.id.pwd);
		
		mAuthCodeTextView = (TextView) findViewById(R.id.authcode_btn);
		mAuthCodeTextView.setOnClickListener(this);
		findViewById(R.id.register_btn).setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void userVertify() {
		String mobile = mPhoneEditView.getText().toString().trim();
		if (TextUtils.isEmpty(mobile)) {
			CommonUtils.ToastMsg(this, R.string.error_empty_mobile);
			return;
		}
		if (!mobile.matches(RegisterActivity.MOBILE_REGX)) {
			CommonUtils.ToastMsg(this, getString(R.string.error_mobile_format));
			return;
		}
		MobclickAgent.onEvent(RegisterActivity.this, AnalyticsUtils.AnalyticsEventKeys.EVENT_VERTIFY);
		
		UserApi.verification(RegisterActivity.this, AppConstants.PRODUCT_KEY, AppConstants.VENDOR_KEY,
                mobile,
                null,
				new WrapperBaseJsonHttpResponseHandler<BaseResponse>(this) {
					
					@Override
					public void onStart() {
						super.onStart();
					}
					
					@Override
					public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
							BaseResponse response) {
						if (response.isSuccess()) {
							CommonUtils.ToastMsg(RegisterActivity.this, R.string.send_authcode_success);
							mAuthCodeTextView.setEnabled(false);
							if (mTimer == null) {
								mTimer = new Timer();
							}
							mDexTime = 60;
							mTimer.schedule(new VertifyTimeTask(), 0, 1000);
						} else {
							CommonUtils.ToastMsg(RegisterActivity.this, R.string.send_authcode_failed);
//							super.onSuccess(statusCode, headers, rawJsonData, response);
						}
					}

					@Override
					protected BaseResponse parseResponse2(String rawJsonData,
							boolean isFailure) throws Throwable {
						LogUtils.e("RegisterActivity", "rawJsonData: " + rawJsonData);
						if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
							return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
						}
						return null;
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
		case R.id.register_btn:
			register();
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
		MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_REGISTER_ACTIVITY);
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_REGISTER_ACTIVITY);
		MobclickAgent.onPause(this);
	}
	
	public void register() {
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
		
		MobclickAgent.onEvent(RegisterActivity.this, AnalyticsUtils.AnalyticsEventKeys.EVENT_REGISTER);
		
		UserApi.mobileRegister(RegisterActivity.this,
                AppConstants.PRODUCT_KEY,
                null,
                phone,
                password,
                authCode,
                new WrapperBaseJsonHttpResponseHandler<UserRegisterResponse>(RegisterActivity.this) {

                    @Override
                    public void onStart() {
//                        CommonUtils.showProgressDialog(RegisterActivity.this, null, getString(R.string.registering), new OnDismissListener() {
//
//                            @Override
//                            public void onDismiss(DialogInterface dialog) {
//                                UserApi.cancel(RegisterActivity.this, true);
//                            }
//                        });
                        CommonUtils.showProgressDialog(RegisterActivity.this, null, null);
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
                                UserState.getInstances(RegisterActivity.this).saveAccessToken(response.data.access_token);
                            }
                            login(phone, password);
                        } else {
                            CommonUtils.dismissDialog();
                            super.onSuccess(statusCode, headers, rawJsonData, response);
                        }
                    }

                    @Override
                    protected UserRegisterResponse parseResponse2(String rawJsonData,
                                                                  boolean isFailure) throws Throwable {
                        LogUtils.d("UserApi#register-> parseResponse: " + rawJsonData);
                        if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                            return GsonUtils.getInstance().getGson().fromJson(rawJsonData, UserRegisterResponse.class);
                        }
                        return null;
                    }
                });
	}
	
	
	public void login(String phone, String pwd) {
		UserApi.login(RegisterActivity.this, AppConstants.PRODUCT_KEY,
        null,
        phone,
        null,
        pwd,
        new WrapperBaseJsonHttpResponseHandler<UserLoginResponse>(this) {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, String rawJsonData, UserLoginResponse errorResponse) {
                CommonUtils.ToastMsg(RegisterActivity.this, R.string.register_success);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                  UserLoginResponse response) {
                if (response.isSuccess()) {
                    if (response.data != null) {
                        UserState.getInstances(RegisterActivity.this).saveAccessToken(response.data.access_token);
                    }
                    mProductInfoHelper.getProductInfo(RegisterActivity.this);
                } else {
                    CommonUtils.ToastMsg(RegisterActivity.this, R.string.register_success);
                    onBackPressed();
                }
            }

            @Override
            protected UserLoginResponse parseResponse2(String rawJsonData,
                                                       boolean isFailure) throws Throwable {
                LogUtils.e("RegisterActivity", "rawJsonData: " + rawJsonData);
                if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                    return GsonUtils.getInstance().getGson().fromJson(rawJsonData, UserLoginResponse.class);
                }
                return null;
            }

            @Override
            public void onFinish() {
                CommonUtils.dismissDialog();
            }
        });
	}
	
	
	RequestListener productRequestListener = new SimpleRequestListener() {
		
		public void onSuccess() {
			CommonUtils.ToastMsg(RegisterActivity.this, R.string.register_success);
			setResult(RESULT_OK);
			ActivityUtils.start(RegisterActivity.this, MainActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			finish();
		}
		
		public void onFail(Exception e) {
			CommonUtils.ToastMsg(RegisterActivity.this, R.string.register_success);
			onBackPressed();
		}
		
		public void onFinish() {
			CommonUtils.dismissDialog();
		}
	};
	
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
		UserApi.cancel(RegisterActivity.this, true);
		super.onDestroy();
	}

}
