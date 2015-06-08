package com.pandocloud.freeiot.ui.base;

import android.support.v4.app.FragmentActivity;

import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.utils.ActivityUtils;

public class BaseActivity extends FragmentActivity {


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityUtils.animFinish(this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }
}
