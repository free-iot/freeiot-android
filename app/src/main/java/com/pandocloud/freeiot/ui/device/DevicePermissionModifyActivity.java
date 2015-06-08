package com.pandocloud.freeiot.ui.device;

import org.apache.http.Header;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.DevicesApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;


public class DevicePermissionModifyActivity extends BaseActivity implements OnClickListener {
	
	private String identifier;
	int permissionId;
	
	private RadioButton readBtn, readWriteBtn;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_permission_modify);
		
		identifier = getIntent().getStringExtra("identifier");
		permissionId = getIntent().getIntExtra("permissionId", 0);
		int privilege = getIntent().getIntExtra("privilege", 0);
		
		readBtn = (RadioButton)findViewById(R.id.radio_read_only);
		readWriteBtn = (RadioButton)findViewById(R.id.radio_read_write);
		
		if (privilege == 1) {
			readBtn.setChecked(true);
		} else {
			readWriteBtn.setChecked(true);
		}
		
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.btn_ok).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			onBackPressed();
			break;
		case R.id.btn_ok:
			devicePermissionModify();
			break;
		default:
			break;
		}
	}
	
	public void devicePermissionModify() {
		int privilege = readBtn.isChecked() ? DeviceAuthActivity.PERMISSION_READ_NOTIFICATION : DeviceAuthActivity.PERMISSION_CONTROL;
		DevicesApi.deivcePermissionModify(this,
            UserState.getInstances(this).getAccessToken(""),
            identifier, permissionId, privilege,
                new WrapperBaseJsonHttpResponseHandler<BaseResponse>(this) {

                    @Override
                    public void onStart() {
                        CommonUtils.showingProgressDialog();
                    }

                    @Override
                    protected BaseResponse parseResponse2(String rawJsonData,
                                                          boolean isFailure) throws Throwable {
                        LogUtils.e(DevicePermissionModifyActivity.class.getSimpleName() + " rawJsonData->"
                                + rawJsonData);
                        if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                            return null;
                        }
                        return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          String rawJsonData, BaseResponse response) {
                        if (response.isSuccess()) {
                            CommonUtils.ToastMsg(DevicePermissionModifyActivity.this, R.string.device_permission_modify_success);
                            setResult(RESULT_OK);
                            ActivityUtils.animFinish(DevicePermissionModifyActivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DevicesApi.cancel(this, true);
	}
}
