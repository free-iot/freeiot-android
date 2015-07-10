package com.pandocloud.freeiot.ui.device.config;


import com.pandocloud.android.config.wifi.WifiConfigManager;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.BusinessUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.WindowManager;

public class GateWayConfigActivity extends BaseActivity {
	private static final String TAG_CONFIG_FRAGMENT = "GateWayConfig";
	
	private FragmentManager fm;
	
	private String tag;
	
	public static long startConfigTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.activity_gateway_config);
		
		fm = getSupportFragmentManager();
		
        if (savedInstanceState == null) {
        	tag = TAG_CONFIG_FRAGMENT;
        	fm.beginTransaction()
        	.replace(android.R.id.content, new ApSsidConfigFragment(), tag)
        	.commit();
		} else {
			tag = savedInstanceState.getString("tag");
			Fragment fragment = fm.findFragmentByTag(tag);
			fm.beginTransaction()
			.replace(android.R.id.content, fragment, tag)
			.commit();
		}
        
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tag", tag);
	}
	
	
	@Override
	public void onBackPressed() {
		if (CommonUtils.showingProgressDialog()) {
			CommonUtils.dismissDialog();
		}
		BusinessUtils.checkWifi(this);
		ActivityUtils.animFinish(this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
	
	@Override
	protected void onDestroy() {
		WifiConfigManager.stopConfig();
		super.onDestroy();
	}
}
