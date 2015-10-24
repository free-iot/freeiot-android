package com.pandocloud.freeiot.ui.device.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.android.config.wifi.WifiConfigManager;
import com.pandocloud.android.config.wifi.WifiConfigMessageHandler;
import com.pandocloud.android.utils.WifiConnectUtil;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.DevicesApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.ActionConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.EasyBaseAdapter;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.DialogFactory;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.pandocloud.freeiot.utils.UIUtils;
import com.pandocloud.freeiot.utils.WakeLockWrapper;
import com.umeng.analytics.MobclickAgent;


public class ApSsidConfigFragment extends Fragment implements OnClickListener {
	
	private static final String TAG = "APSsidConfigFragment#blocker";

    private EditText etSsid;
	
	private EditText etPwd;
	
	private Button btnConfig;
	
	private SSIDAdapter adapter;
	
	private List<ScanResult> scanResults;
	
	private WifiManager wifiManager;
	
	private WifiConnectUtil wifiConnectUtil;
	
	private String ssid;
	
	private String pwd; // wifi network pwd

	private WifiConfigMessageHandler msgHandler;
	
	private String deviceKey;
	
	private Timer timer;

	private int refreshTimeDex = 3000;
	
	private WakeLock wakeLock;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case WifiConfigManager.CONFIG_SUCCESS:
				if (getActivity() == null) {
					return;
				}
				CommonUtils.showProgressDialog(getActivity(), "", getString(R.string.config_success));
				String tokenValue = (String) msg.obj;
				deviceKey = tokenValue;
				bindDevice(deviceKey);
				LogUtils.e(TAG, "config success...");
				break;
			case WifiConfigManager.CONFIG_FAILED:
				LogUtils.e(TAG, "config failed...");
				CommonUtils.dismissDialog();
				CommonUtils.ToastMsg(getActivity(), R.string.config_failed);
				break;
			case WifiConfigManager.DEVICE_CONNECT_FAILED:
				LogUtils.e(TAG, "socket connect failed...");
				break;
			case WifiConfigManager.DEVICE_SEND_FAILED:
				LogUtils.e(TAG, "send msg failed...");
				break;
			default:
				break;
			}
		};
	};
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		wakeLock = WakeLockWrapper.getWakeLockInstance(getActivity(), getClass().getSimpleName());
		wakeLock.acquire();
		
		wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		
		wifiConnectUtil = new WifiConnectUtil(wifiManager);
		
		getActivity().registerReceiver(ssidRefreshReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_gateway_config, container, false);
		
		initView(rootView);
		
		return rootView;
	}
	
	public void initView(View rootView) {
        etSsid = (EditText) rootView.findViewById(R.id.et_ssid);
		etPwd = (EditText) rootView.findViewById(R.id.et_pwd);
		btnConfig = (Button) rootView.findViewById(R.id.btn_config);
		btnConfig.setOnClickListener(this);
		rootView.findViewById(R.id.back).setOnClickListener(this);
		rootView.findViewById(R.id.btn_exit).setOnClickListener(this);

		ssid = wifiConnectUtil.getCurrentWifiSSID(getClass().getSimpleName());
		etSsid.setText(ssid);
	}
	
	public synchronized void bindDevice(String deviceKey) {
		LogUtils.e(TAG, "deviceKey: " + deviceKey);
		DevicesApi.deviceBind(getActivity(),
            UserState.getInstances(getActivity()).getAccessToken(""),
            deviceKey, new WrapperBaseJsonHttpResponseHandler<BaseResponse>(getActivity()) {

                @Override
                public void onStart() {
                    if (!CommonUtils.showingProgressDialog()) {
                        CommonUtils.showProgressDialog(getActivity(), null, null);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      Throwable throwable, String rawJsonData,
                                      BaseResponse errorResponse) {
                    super.onFailure(statusCode, headers, throwable, rawJsonData, errorResponse);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                                      String rawJsonData, BaseResponse response) {
                    if (response.isSuccess()) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ActionConstants.ACTON_REFRESH_DEVICES));

                        MobclickAgent.onEventValue(getActivity(), AnalyticsUtils.AnalyticsEventKeys.EVENT_APCONFIG, null, (int) (System.currentTimeMillis() - GateWayConfigActivity.startConfigTime));

                        ActivityUtils.animFinish(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    } else {
                        super.onSuccess(statusCode, headers, rawJsonData, response);
                    }
                }

                @Override
                protected BaseResponse parseResponse2(String rawJsonData,
                                                      boolean isFailure) throws Throwable {
                    LogUtils.d("ApSsidConfigFragment# rawJsonData: " + rawJsonData);
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
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_APSSID_CONFIG_FGMENT);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_APSSID_CONFIG_FGMENT);
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initData();
	}
	

	public void initData() {
		msgHandler = new WifiConfigMessageHandler(mHandler);
		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
			openWifi();
		} else {
			refreshCurrentSSID();
		}
	}
	
	public void openWifi() {
		boolean openWifi = wifiConnectUtil.OpenWifi();
		if (openWifi) {
			refreshCurrentSSID();
		}
	}
	
	private void refreshCurrentSSID() {
		if (wifiManager.startScan()) {
			if (adapter == null || adapter.isEmpty()) {
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
                        refreshSSIDSpinner();
                        etSsid.requestFocus();
                        //TODO
//                        String currentWifiSSID =  wifiConnectUtil.getCurrentWifiSSID("");
//                        if (!TextUtils.isEmpty(currentWifiSSID)) {
//                            autoCompleteTextView.setText(currentWifiSSID);
//                        }
					}
				});
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_config:
			pwd = etPwd.getText().toString().trim();
			ssid = etSsid.getText().toString().trim();
			if (TextUtils.isEmpty(ssid)) {
				return;
			}
			if (TextUtils.isEmpty(pwd)) {
				DialogFactory.createCommonDialog(getActivity(), getString(android.R.string.yes),
                        getString(R.string.wifi_no_pwd_tip, ssid),
                        getString(android.R.string.ok), getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                CommonUtils.showProgressDialog(getActivity(), null, getString(R.string.configing));
                                connect();
                                refreshTimeDex = 5000;
                            }
                        }, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
				return;
			} else {
				CommonUtils.showProgressDialog(getActivity(), null, getString(R.string.configing));
				connect();
				refreshTimeDex = 5000;
			}
			break;

		case R.id.back:
		case R.id.btn_exit:
			WifiConfigManager.stopConfig();
			MobclickAgent.onEvent(getActivity(), AnalyticsUtils.AnalyticsEventKeys.EVENT_EXIT_CONFIG);
			ActivityUtils.animFinish(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			break;
		default:
			break;
		}
	}
	
	
	public void connect() {
		WifiConfigManager.setMsgHandler(msgHandler);
		WifiConfigManager.startConfig(getActivity(), WifiConfigManager.CONFIG_MODE_SMARTLINK, ssid, pwd);
	}
	
	class WifiRefreshRunnable extends TimerTask {
		
		@Override
		public void run() {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					LogUtils.e(getClass().getSimpleName(), "WifiRefreshRunnable#run....");

					String currentSSID = wifiConnectUtil.getCurrentWifiSSID(getClass().getSimpleName());
					if (!currentSSID.equals(ssid) && TextUtils.isEmpty(deviceKey)) {
						wifiConnectUtil.Connect(ssid, pwd, WifiConnectUtil.WifiCipherType.WIFICIPHER_WPA);
					}					
				}
			});
		}
	}
	
	
	class SSIDAdapter extends EasyBaseAdapter<ScanResult> implements Filterable {

		public SSIDAdapter(Context context, List<ScanResult> dataset) {
			super(context, dataset);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			}
			
			TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
			ScanResult scanResult = (ScanResult) getItem(position);
			textView.setText(scanResult.SSID);
			return convertView;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {
				
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
//					if (results != null && results.count > 0) {
//	                    // 有过滤结果，显示自动完成列表
//	                   adapter.updateDataSet((List<ScanResult>)results.values);
//	                } 
				}
				
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					  FilterResults filterResults = new FilterResults();
		                filterResults.values = scanResults;   // results是上面的过滤结果
		                filterResults.count = scanResults == null ? 0 : scanResults.size();
					return filterResults;
				}
				
				@Override
				public CharSequence convertResultToString(Object resultValue) {
					ScanResult scanResult = (ScanResult) resultValue;
					return scanResult.SSID;
				}
				
			};
		}
		
	}
	
	private Object lock = new Object();
	public void refreshSSIDSpinner() {
//		synchronized (lock) {
			scanResults = wifiManager.getScanResults();
			if (scanResults != null) {
				List<ScanResult> tmpScanResults = new ArrayList<ScanResult>();
				List<String> rss = new ArrayList<String>();
				for (ScanResult result : scanResults) {
					if (!TextUtils.isEmpty(result.SSID)) {
						rss.add(result.SSID);
					} else {
						tmpScanResults.add(result);
					}
				}
				
				if (!tmpScanResults.isEmpty()) {
					scanResults.removeAll(tmpScanResults);
				}
				
				if (adapter == null) {
					if (getActivity() != null) {
						adapter = new SSIDAdapter(getActivity(), scanResults);
					}
				} else {
					adapter.updateDataSet(scanResults);
				}
			}
//		}
	}
	
	private BroadcastReceiver ssidRefreshReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
				LogUtils.d("refreshCurrentSSID...");
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						refreshSSIDSpinner();
					}
				});
			}
		}
		
	};
	
	
	public void onDestroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (wakeLock != null) {
			wakeLock.release();
		}
		getActivity().unregisterReceiver(ssidRefreshReceiver);
		AbsOpenApi.cancel(getActivity(), true);
		super.onDestroy();
	};
}
