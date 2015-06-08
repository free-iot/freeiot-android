package com.pandocloud.freeiot.ui.helper;

import android.widget.ProgressBar;

import com.pandocloud.freeiot.jsbridge.BridgeHelper;
import com.pandocloud.freeiot.ui.device.DeviceControlActivity;

/**
 * Created by ywen on 15/5/25.
 */
public class DeviceControlHelper {

    private DeviceControlActivity mDeviceControlActivity;

    private BridgeHelper mWebView;

    private ProgressBar mProgressBar;

    private String mIdentifier;

    public DeviceControlHelper(DeviceControlActivity activity) {
        this.mDeviceControlActivity = activity;
    }


}
