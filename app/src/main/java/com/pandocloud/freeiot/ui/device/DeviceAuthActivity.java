package com.pandocloud.freeiot.ui.device;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;

import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.DevicesApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.login.RegisterActivity;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.umeng.analytics.MobclickAgent;

public class DeviceAuthActivity extends BaseActivity implements OnClickListener {

	private String identifier;
	
	private EditText mPhoneView;
	private RadioButton mReadBtn;
    private RadioButton mReadWriteBtn;
	
	public static final int PERMISSION_READ_NOTIFICATION = 1;
	public static final int PERMISSION_CONTROL = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_device_auth);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			identifier = bundle.getString("identifier");
		}
		
		mPhoneView = (EditText)findViewById(R.id.et_user);

		mReadBtn = (RadioButton)findViewById(R.id.radio_read_only);
		mReadWriteBtn = (RadioButton)findViewById(R.id.radio_read_write);
		
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.btn_ok).setOnClickListener(this);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_DEVICE_AUTH_ACTIVITY);
		MobclickAgent.onResume(this);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_DEVICE_AUTH_ACTIVITY);
		MobclickAgent.onPause(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_ok:
			deviceAuth();
			break;
		case R.id.back:
			ActivityUtils.animFinish(DeviceAuthActivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			break;
		default:
			break;
		}
	}
	
	public void deviceAuth() {
		String userPhone = mPhoneView.getText().toString().trim();
		if (TextUtils.isEmpty(userPhone)) {
			CommonUtils.ToastMsg(this, getString(R.string.error_auth_phone_empty));
			return;
		}
		if (!userPhone.matches(RegisterActivity.MOBILE_REGX)) {
			CommonUtils.ToastMsg(this, getString(R.string.error_mobile_format));
			return;
		}
		int privilege = 1;
		if (mReadBtn.isChecked()) {
			privilege = 1;
		}
		if (mReadWriteBtn.isChecked()) {
			privilege = 2;
		}
		
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("auth", privilege == 1 ? "read" : "read write");
		MobclickAgent.onEvent(this, AnalyticsUtils.AnalyticsEventKeys.EVENT_DEVICE_AUTH, hashMap);
		
		DevicesApi.devicePermissionAuth(DeviceAuthActivity.this, AppConstants.PRODUCT_KEY,
                UserState.getInstances(this).getAccessToken(""),
                identifier, userPhone, privilege,
                new WrapperBaseJsonHttpResponseHandler<BaseResponse>(this) {

                    @Override
                    public void onStart() {
                        CommonUtils.showProgressDialog(DeviceAuthActivity.this, "", "");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          Throwable throwable, String rawJsonData, BaseResponse errorResponse) {
                        super.onFailure(statusCode, headers, throwable, rawJsonData, errorResponse);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                          BaseResponse response) {
                        if (response.isSuccess()) {
                            setResult(RESULT_OK);
                            CommonUtils.ToastMsg(DeviceAuthActivity.this, R.string.device_auth_success);
                            ActivityUtils.animFinish(DeviceAuthActivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                        } else {
                            super.onSuccess(statusCode, headers, rawJsonData, response);
                        }
                    }

                    @Override
                    protected BaseResponse parseResponse2(String rawJsonData,
                                                          boolean isFailure) throws Throwable {
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AbsOpenApi.cancel(this, true);
	}
}
