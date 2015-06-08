package com.pandocloud.freeiot.ui;

import android.os.Bundle;
import android.os.Handler;

import com.pandocloud.android.api.interfaces.SimpleRequestListener;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.helper.ProductInfoHelper;
import com.pandocloud.freeiot.ui.login.EmailLoginActivity;
import com.pandocloud.freeiot.ui.login.LoginActivity;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by ywen on 15/5/18.
 */
public class SplashActivity extends BaseActivity {

    private Handler mHandler = new Handler();
    private boolean mBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);

        setContentView(R.layout.activity_splash);

        ProductInfoHelper productInfoHelper = new ProductInfoHelper(new SimpleRequestListener() {});
        productInfoHelper.getProductInfo(this);

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mBackPressed) {
                    finish();
                    return;
                }
                if (UserState.getInstances(SplashActivity.this).isLogin()) {
                    ActivityUtils.start(SplashActivity.this, MainActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                } else {
                    ActivityUtils.start(SplashActivity.this, LoginActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                }
                finish();
            }
        }, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_SPLASH_ACTIVITY);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_SPLASH_ACTIVITY);
        MobclickAgent.onPause(this);
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;
        super.onBackPressed();
    }
}
