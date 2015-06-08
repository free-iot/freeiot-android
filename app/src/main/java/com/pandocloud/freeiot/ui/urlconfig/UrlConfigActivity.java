package com.pandocloud.freeiot.ui.urlconfig;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.ui.SplashActivity;
import com.pandocloud.freeiot.ui.app.AppConfigPrefs;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.umeng.analytics.MobclickAgent;

public class UrlConfigActivity extends BaseActivity implements OnClickListener {
	
	public static final int URL_CONFIG_REQUEST_CODE = 1 << 2;
	
	private TextView mApiHostUrlView;
	
	private RadioGroup mUrlRadioGroup;
	
	private RadioButton mDevRadioBtn, mPreProductRadioBtn, mReleaseRadioBtn;
	
	private int lastUrlState = UrlConfigManager.getCurrentState();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_url_config);
		
		mUrlRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
		
		mDevRadioBtn = (RadioButton) mUrlRadioGroup.findViewById(R.id.dev_radio);
		mPreProductRadioBtn = (RadioButton) mUrlRadioGroup.findViewById(R.id.pre_product_radio);
		mReleaseRadioBtn = (RadioButton) mUrlRadioGroup.findViewById(R.id.release_radio);
		
		findViewById(R.id.back).setOnClickListener(this);
		
		if (UrlConfigManager.getCurrentState() == UrlConfigManager.DEVELOP_STATE) {
			mDevRadioBtn.setChecked(true);
		} else if (UrlConfigManager.getCurrentState() == UrlConfigManager.PREPRODUCT_STATE) {
			mPreProductRadioBtn.setChecked(true);
		} else if (UrlConfigManager.getCurrentState() == UrlConfigManager.RELEASE_STATE) {
			mReleaseRadioBtn.setChecked(true);
		}
		
		mApiHostUrlView = (TextView)findViewById(R.id.tv_apihost_url);
		
		setData();
		
		mUrlRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.dev_radio:
                        UrlConfigManager.setCurrentState(UrlConfigManager.DEVELOP_STATE);
                        break;
                    case R.id.pre_product_radio:
                        UrlConfigManager.setCurrentState(UrlConfigManager.PREPRODUCT_STATE);
                        break;
                    case R.id.release_radio:
                        UrlConfigManager.setCurrentState(UrlConfigManager.RELEASE_STATE);
                        break;
                    default:
                        break;
                }
                UrlConfigManager.updateUrl(UrlConfigManager.getCurrentState());
                AppConfigPrefs.getInstances(UrlConfigActivity.this).saveIntValue("cur_env", UrlConfigManager.getCurrentState());
                setData();
            }
        });
	}
	
	public void setData() {
		int currentState = UrlConfigManager.getCurrentState();
		switch (currentState) {
		case UrlConfigManager.DEVELOP_STATE:
			mApiHostUrlView.setText(UrlConfigManager.API_HOST_URL);
			break;
		case UrlConfigManager.PREPRODUCT_STATE:
			mApiHostUrlView.setText(UrlConfigManager.API_HOST_URL);
			break;
		case UrlConfigManager.RELEASE_STATE:
			mApiHostUrlView.setText(UrlConfigManager.API_HOST_URL);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			onBackPressed();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if (UserState.getInstances(this).isLogin()) {
			if (lastUrlState != UrlConfigManager.getCurrentState()) {
				UserState.getInstances(this).logout(this);
				setResult(RESULT_OK);
				
				Intent mStartActivity = new Intent(this, SplashActivity.class);
				int mPendingIntentId = 123456;
				PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
				AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, mPendingIntent);
				MobclickAgent.onKillProcess(this);
				android.os.Process.killProcess(android.os.Process.myPid());
				return;
			}
		}
		ActivityUtils.animFinish(this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
}
