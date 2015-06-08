package com.pandocloud.freeiot.ui.device.config;

import com.pandocloud.android.config.GateWayConfigManager;
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
	
	private static final String TAG_AP_FRAGMENT = "APConnect";
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
        	tag = TAG_AP_FRAGMENT;
        	fm.beginTransaction()
        	.replace(android.R.id.content, new APConnectFragment(), tag)
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
	
	public void toGateWayConfigView(String apSSID) {
		ApSsidConfigFragment gwConfigFragment = new ApSsidConfigFragment();
		Bundle bundle = new Bundle();
		bundle.putString("apSSID", apSSID);
		gwConfigFragment.setArguments(bundle);
		
		tag = TAG_CONFIG_FRAGMENT;
		fm.beginTransaction()
			.replace(android.R.id.content, gwConfigFragment, tag)
			.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
			.commitAllowingStateLoss();
	}
	
	public void backToApConnectView() {
		 tag = TAG_AP_FRAGMENT;
		 fm.beginTransaction()
     		.replace(android.R.id.content, new APConnectFragment(), "APConnect")
     		.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
     		.commitAllowingStateLoss();   
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (tag.equals(TAG_CONFIG_FRAGMENT)) {
				backToApConnectView();
			} else {
				ActivityUtils.animFinish(this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			}
		}
		return true;
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
		GateWayConfigManager.getInstances().finishConfig();
		super.onDestroy();
	}
}
