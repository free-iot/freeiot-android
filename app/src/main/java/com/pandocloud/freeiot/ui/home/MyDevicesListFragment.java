package com.pandocloud.freeiot.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.android.utils.NetworkUtil;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.DevicesApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.ActionConstants;
import com.pandocloud.freeiot.ui.app.ProductInfoPrefs;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.EasyBaseAdapter;
import com.pandocloud.freeiot.ui.bean.Device;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.bean.http.DevicesResponse;
import com.pandocloud.freeiot.ui.db.DBManager;
import com.pandocloud.freeiot.ui.device.DeviceAuthActivity;
import com.pandocloud.freeiot.ui.device.DeviceControlActivity;
import com.pandocloud.freeiot.ui.device.DeviceInfoModifyActivity;
import com.pandocloud.freeiot.ui.device.DevicePermissionsListActivity;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.BusinessUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.DialogFactory;
import com.pandocloud.freeiot.utils.ErrorCodeHelper;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.pandocloud.freeiot.utils.UIUtils;
import com.umeng.update.UmengUpdateAgent;

import org.apache.http.Header;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class MyDevicesListFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView mListView;

    private DeviceAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private HashMap<String, Device> mDevicesHashMap;

    public MyDevicesListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_devices_list, container, false);
        initView(rootView);
        return rootView;
    }

    public void initView(View rootView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorScheme(R.color.main_red_color);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, UIUtils.dip2px(getActivity(), 48));
        mListView = (ListView) rootView.findViewById(R.id.listview);

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(refreshDevicesReceiver,
                        new IntentFilter(ActionConstants.ACTON_REFRESH_DEVICES));
        refreshDevices(true);
//		UpdateConfig.setDebug(true);
        UmengUpdateAgent.forceUpdate(getActivity());
    }

    @Override
    public void onRefresh() {
        refreshDevices(true);
    }

    @Override
    public void onClick(View v) {

    }

    public void unbindingDevice(final int position, String identifier) {
        DevicesApi.deviceUnbinding(getActivity(),
        UserState.getInstances(getActivity()).getAccessToken(""),
        identifier,
        new WrapperBaseJsonHttpResponseHandler<BaseResponse>(getActivity()) {

            @Override
            public void onStart() {
                CommonUtils.showingProgressDialog();
            }

            @Override
            protected BaseResponse parseResponse2(String rawJsonData,
                                                  boolean isFailure) throws Throwable {
                LogUtils.d("MainActivity", "rawJsonData: " + rawJsonData);
                if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                    return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
                }
                return null;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  String rawJsonData, BaseResponse response) {
                if (response.isSuccess()) {
                    CommonUtils.ToastMsg(getActivity(), R.string.unbinding_success);
                    mAdapter.removeAtPosition(position);
                    refreshDevices(false);
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

    public void refreshDevices(final boolean showLoadingView) {
        if (showLoadingView) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        mDevicesHashMap = DBManager.getInstances(getActivity()).queryDeviceInfo();
        DevicesApi.deviceList(getActivity(), UserState.getInstances(getActivity()).getAccessToken(""),
        new BaseJsonHttpResponseHandler<DevicesResponse>() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, String rawJsonResponse, DevicesResponse response) {
                LogUtils.e("end refresh devices http request with error", throwable);
                LogUtils.e("statusCode: " + statusCode);
                if (showLoadingView) {
                    if (!NetworkUtil.isNetworkAvailable(getActivity()) || statusCode == 0) {
                        CommonUtils.ToastMsg(getActivity(), getString(R.string.http_error_message));
                    } else {
                        if (statusCode >= 500) {
                            CommonUtils.ToastMsg(getActivity(), getString(R.string.http_server_error));
                        } else {
                            if (throwable != null) {
                                CommonUtils.ToastMsg(getActivity(), throwable.getMessage());
                            }
                        }
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                  DevicesResponse response) {
                if (response != null && getActivity() != null) {
                    if (response.isSuccess()) {
                        updateDevices(response.data);
                        Collections.sort(response.data, new DeviceComparator());
                        if (mAdapter == null) {
                            mAdapter = new DeviceAdapter(getActivity(), response.data);
                            mListView.setAdapter(mAdapter);
                        } else {
                            mAdapter.updateDataSet(response.data);
                        }
                    } else {
                        int code = response.code;
                        CommonUtils.ToastMsg(getActivity(), ErrorCodeHelper.getInstances(getActivity()).getErrorMessage(code));
                        BusinessUtils.checkTokenAvailable(getActivity(), code);
                    }
                }
            }

            @Override
            public void onFinish() {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            protected DevicesResponse parseResponse(String rawJsonData,
                                                    boolean isFailure) throws Throwable {
                LogUtils.e("rawJsonData:" + rawJsonData);
                if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                    LogUtils.d("UserApi.deviceList->parseResponse: rawJsonData->" + rawJsonData);
                    return null;
                }
                return GsonUtils.getInstance().getGson().fromJson(rawJsonData, DevicesResponse.class);
            }
        });
    }

    public void updateDevices(List<Device> devices) {
        if (mDevicesHashMap == null || mDevicesHashMap.isEmpty()) {
            return;
        }
        for (Device device : devices) {
            if (!device.isOwner() && mDevicesHashMap.containsKey(device.identifier)) {
                device.name = mDevicesHashMap.get(device.identifier).name;
            }
        }
    }

    public void toDeviceSetting(String identifier) {
        Bundle bundle = new Bundle();
        bundle.putString("identifier", identifier);
        ActivityUtils.start(getActivity(), DeviceAuthActivity.class, bundle, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                //TODO
//                case UrlConfigActivity.URL_CONFIG_REQUEST_CODE:
//                    finish();
//                    break;

                default:
                    break;
            }
        }
    }

    BroadcastReceiver refreshDevicesReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActionConstants.ACTON_REFRESH_DEVICES.equals(intent.getAction())) {
                LogUtils.i("handler broadcase receiver, begin refresh devices http request ...");
                refreshDevices(false);
            }
        }
    };

    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshDevicesReceiver);
        AbsOpenApi.cancel(getActivity(), true);
        super.onDestroy();
    }

    class DeviceAdapter extends EasyBaseAdapter<Device> {

        private String productName;
        public DeviceAdapter(Context context, List<Device> dataset) {
            super(context, dataset);
            productName = ProductInfoPrefs.getInstances(mContext).getString("name", "");
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.adapter_device_item, parent, false);
            }

            final Device device = (Device) getItem(position);
            ImageView imageView = (ImageView)convertView.findViewById(R.id.image);
            TextView textView = (TextView) convertView.findViewById(R.id.textview);

            String iconUrl = ProductInfoPrefs.getInstances(mContext).getString("icon", "");
            if (!TextUtils.isEmpty(iconUrl)) {
                ImageLoader.getInstance().displayImage(iconUrl, imageView);
            } else {
                imageView.setImageResource(R.drawable.icon);
            }

            if (device.isOnline()) {
                convertView.setClickable(true);
//				convertView.setBackgroundResource(R.drawable.selector_device_list);
                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        deviceItemClick(device);
                    }
                });

            } else {
                convertView.setClickable(false);
//				convertView.setBackgroundResource(R.drawable.bg_list_item_press);
                convertView.setOnClickListener(null);
            }

            ImageView settingView = (ImageView) convertView.findViewById(R.id.setting);
            settingView.setVisibility(View.VISIBLE);
            if (device.isOwner()) {
//				settingView.setVisibility(View.VISIBLE);
                settingView.setImageResource(R.drawable.icon_setting);
                settingView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
//						createSettingDialog(mContext, position, device);
                        showOwnerSettingDialog(mContext, position, device);
                    }
                });
            } else {
//				settingView.setVisibility(View.GONE);
                settingView.setImageResource(R.drawable.icon_settings2);
                settingView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showCommonSettingDailog(mContext, position, device);
                    }
                });
            }


            imageView.setVisibility(View.GONE);
            if (device.isOnline()) {
                textView.setTextColor(getResources().getColor(android.R.color.black));
                if (!TextUtils.isEmpty(device.name)) {
                    String txt = getString(R.string.device_name_format, device.name, device.identifier);
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder(txt);
                    stringBuilder.setSpan(new ForegroundColorSpan(Color.GRAY), txt.length() - device.identifier.length() - 2, txt.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    textView.setText(stringBuilder);
                } else if(!TextUtils.isEmpty(productName)){
                    String txt = getString(R.string.device_name_format, productName, device.identifier);
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder(txt);
                    stringBuilder.setSpan(new ForegroundColorSpan(Color.GRAY), txt.length() - device.identifier.length() - 2, txt.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    textView.setText(stringBuilder);
                } else {
                    textView.setText(device.identifier);
                }
            } else {
                String txt;
                if (!TextUtils.isEmpty(device.name)) {
                    txt = getString(R.string.device_name_format, device.name, device.identifier);
                } else if (!TextUtils.isEmpty(productName)) {
                    txt = getString(R.string.device_name_format, productName, device.identifier);
                } else {
                    txt = device.identifier;
                }
                textView.setText(txt);
                textView.setTextColor(Color.GRAY);
            }

            return convertView;
        }

    }

    class DeviceComparator implements Comparator<Device> {

        @Override
        public int compare(Device lhs, Device rhs) {
            if (lhs.isOnline() && rhs.isOnline()) {
                return 0;
            }
            if (lhs.isOnline()) {
                return -1;
            }
            if (rhs.isOnline()) {
                return 1;
            }
            return 0;
        }

    }

    public void deviceItemClick(Device device) {
        if (device.isOnline()) {
            Bundle bundle = new Bundle();
            bundle.putString("identifier", device.identifier);
//            String name = ProductInfoPrefs.getInstances(getActivity()).getString("name", "");
            bundle.putString("name", device.name);
            bundle.putString("app", device.app);
            ActivityUtils.start(getActivity(), DeviceControlActivity.class, bundle, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

    /**
     * 是owner时，显示的菜单设置
     * @param context
     * @param position
     * @param device
     */
    public void showOwnerSettingDialog(Context context, final int position, final Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.settings));
        String[] items = getResources().getStringArray(R.array.deivce_owner_setting);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0: {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("device", device);
                        ActivityUtils.start(getActivity(), DeviceInfoModifyActivity.class, bundle, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                    break;
                    case 1: {
                        Bundle bundle = new Bundle();
                        bundle.putString("identifier", device.identifier);
                        ActivityUtils.start(getActivity(), DevicePermissionsListActivity.class, bundle, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                    break;
                    case 2: {
                        DialogFactory.createCommonDialog(getActivity(), getString(R.string.warn),
                                getString(R.string.unbinding_tip),
                                getString(android.R.string.ok), getString(android.R.string.cancel),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        unbindingDevice(position, device.identifier);
                                    }
                                }, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                    break;
                    default:
                        break;
                }
            }
        });
        builder.create().show();
    }


    /**
     * 不是owner时显示的菜单设置
     * @param context
     * @param position
     * @param device
     */
    public void showCommonSettingDailog(Context context, final int position, final Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		builder.setTitle(getString(R.string.settings));
        String[] items = getResources().getStringArray(R.array.device_not_owner_setting);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Bundle bundle = new Bundle();
                        if (mDevicesHashMap != null && mDevicesHashMap.containsKey(device.identifier)) {
                            bundle.putBoolean("hasInfo", true); //本地数据库是否已经保存过数据
                        }
                        bundle.putSerializable("device", device);
                        ActivityUtils.start(getActivity(), DeviceInfoModifyActivity.class, bundle, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                        break;
                    case 1:
                        DialogFactory.createCommonDialog(getActivity(), getString(R.string.warn),
                                getString(R.string.device_delete_tip),
                                getString(android.R.string.ok), getString(android.R.string.cancel),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        deleteDevice(position, device);
                                    }
                                }, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        break;

                    default:
                        break;
                }
            }
        });
        builder.create().show();
    }

    public void deleteDevice(final int position, Device device) {
        DevicesApi.deviceDelete(getActivity(), UserState.getInstances(getActivity()).getAccessToken(""),
        device.identifier,
        new WrapperBaseJsonHttpResponseHandler<BaseResponse>(getActivity()) {

            @Override
            public void onStart() {
                CommonUtils.showingProgressDialog();
            }

            @Override
            protected BaseResponse parseResponse2(String rawJsonData, boolean isFailure) throws Throwable {
                if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                    return null;
                }
                return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  String rawJsonData, BaseResponse response) {
                if (response.isSuccess()) {
                    CommonUtils.ToastMsg(getActivity(), R.string.device_delete_success);
                    mAdapter.removeAtPosition(position);
                    refreshDevices(false);
                } else {
                    super.onSuccess(statusCode, headers, rawJsonData, response);
                }
            }

            @Override
            public void onFinish() {
                CommonUtils.showingProgressDialog();
            }
        });
    }
}
