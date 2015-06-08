package com.pandocloud.freeiot.ui.device;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;

import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.DevicesApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.ActionConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.Device;
import com.pandocloud.freeiot.ui.bean.DeviceInfo;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.bean.http.DeviceInfoResponse;
import com.pandocloud.freeiot.ui.db.DBManager;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;


public class DeviceInfoModifyActivity extends BaseActivity implements OnClickListener {
	
	private EditText etDeviceName;
	
	private RadioButton secureBtn1, secureBtn2;

	private static final int DEVICE_BINDING_EVERYTHING = 1;
	
	private static final int DEVICE_BINDING_ONLY_UNBINDING = 2;
	
	private Device device;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_device_info_modify);
		
		device = (Device) getIntent().getSerializableExtra("device");
		
		etDeviceName = (EditText) findViewById(R.id.et_name);
		secureBtn1 = (RadioButton) findViewById(R.id.radioBtn1);
		secureBtn2 = (RadioButton) findViewById(R.id.radioBtn2);
		
		etDeviceName.setText(device.name);
		
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.btn_ok).setOnClickListener(this);
		if (device != null && device.isOwner()) {
			loadDeivceInfo();
		} else {
			findViewById(R.id.permission_tip).setVisibility(View.GONE);
			findViewById(R.id.permission_layout).setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			ActivityUtils.animFinish(DeviceInfoModifyActivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			break;
		case R.id.btn_ok:
			modifyDeviceInfo();
			break;
		default:
			break;
		}
	}
	
	public void loadDeivceInfo() {
		DevicesApi.deviceInfo(this, UserState.getInstances(this).getAccessToken(""), device.identifier,
                new WrapperBaseJsonHttpResponseHandler<DeviceInfoResponse>(this) {

                    @Override
                    protected DeviceInfoResponse parseResponse2(
                            String rawJsonData, boolean isFailure)
                            throws Throwable {
                        if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                            return null;
                        }
                        return GsonUtils.getInstance().getGson().fromJson(rawJsonData, DeviceInfoResponse.class);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          String rawJsonData, DeviceInfoResponse response) {
                        if (response.isSuccess()) {
                            DeviceInfo deviceInfo = response.data;
                            if (deviceInfo.secure_level == DEVICE_BINDING_EVERYTHING) {
                                secureBtn1.setChecked(true);
                            } else if (deviceInfo.secure_level == DEVICE_BINDING_ONLY_UNBINDING) {
                                secureBtn2.setChecked(true);
                            }
                            etDeviceName.setText(deviceInfo.name);
                        } else {
                            super.onSuccess(statusCode, headers, rawJsonData, response);
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                });
	}
	
	public void modifyDeviceInfo() {
		String deviceName = etDeviceName.getText().toString().trim();
		if (TextUtils.isEmpty(deviceName)) {
			CommonUtils.ToastMsg(this, R.string.error_deivce_name_empty);
			return;
		}
		if (!device.isOwner()) {
			boolean hasInfo = getIntent().getBooleanExtra("hasInfo", false);
			if (hasInfo) {
				DBManager.getInstances(this).updateDevieInfo(device.identifier, deviceName);
			} else {
				DBManager.getInstances(this).insertDeviceInfo(device.identifier, deviceName);
			}
			updateSuccess();
			return;
		}
		int secureLevel = secureBtn1.isChecked() ? DEVICE_BINDING_EVERYTHING : DEVICE_BINDING_ONLY_UNBINDING;
		DevicesApi.deviceInfoModify(this, UserState.getInstances(this).getAccessToken(""), 
				device.identifier, deviceName, secureLevel, new WrapperBaseJsonHttpResponseHandler<BaseResponse>(this) {
				
			@Override
			public void onStart() {
				CommonUtils.showProgressDialog(DeviceInfoModifyActivity.this, "", getString(R.string.device_info_uploading));
			}
			
			@Override
			protected BaseResponse parseResponse2(String rawJsonData,
					boolean isFailure) throws Throwable {
				if (isFailure || TextUtils.isEmpty(rawJsonData)) {
					return null;
				}
				return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String rawJsonData, BaseResponse response) {
				if (response.isSuccess()) {
					updateSuccess();
				} else {
					super.onSuccess(statusCode, headers, rawJsonData, response);
				}
			}
			
			@Override
			public void onFinish() {
				CommonUtils.dismissDialog();
			}
		});
	}
	
	
	public void updateSuccess() {
		CommonUtils.ToastMsg(DeviceInfoModifyActivity.this, R.string.device_info_modify_success);
		LocalBroadcastManager.getInstance(DeviceInfoModifyActivity.this).sendBroadcast(new Intent(ActionConstants.ACTON_REFRESH_DEVICES));
		ActivityUtils.animFinish(DeviceInfoModifyActivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AbsOpenApi.cancel(DeviceInfoModifyActivity.this, true);
	}
	
}
