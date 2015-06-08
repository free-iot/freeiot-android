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
import com.pandocloud.android.config.GateWayConfigManager;
import com.pandocloud.android.config.handler.GateWayMsgHandler;
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

    private PopupWindow mPopupWindow;
	
	private List<ScanResult> scanResults;
	
	private WifiManager wifiManager;
	
	private WifiConnectUtil wifiConnectUtil;
	
	private String apSSID; //AP wifi ssid
	
	private String ssid;
	
	private String pwd; // wifi network pwd
	
	private GateWayMsgHandler msgHandler;
	
	private String deviceKey;
	
	private Timer timer;
	
	private int refreshTimeDex = 3000;
	
	private WakeLock wakeLock;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GateWayConfigManager.CONFIG_SUCCESS:
				if (getActivity() == null) {
					return;
				}
				CommonUtils.showProgressDialog(getActivity(), "", getString(R.string.config_success));
				String tokenValue = (String) msg.obj;
				deviceKey = tokenValue;
				backToNetwork();
				LogUtils.e(TAG, "config success...");
				break;
			case GateWayConfigManager.CONFIG_FAILED:
				LogUtils.e(TAG, "config failed...");
				CommonUtils.dismissDialog();
				CommonUtils.ToastMsg(getActivity(), R.string.config_failed);
				break;
			case GateWayConfigManager.CONNECT_SOCKET_FAILED:
				LogUtils.e(TAG, "socket connect failed...");
				break;
			case GateWayConfigManager.SEND_MSG_FAILED:
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
		
		Bundle bundle = getArguments();
		if (bundle != null) {
			apSSID = bundle.getString("apSSID");
		}
		
		wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		
		wifiConnectUtil = new WifiConnectUtil(wifiManager);
		
		getActivity().registerReceiver(ssidRefreshReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		getActivity().registerReceiver(connectionChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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

        etSsid.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mPopupWindow == null || !mPopupWindow.isShowing()) {
                    showPopupWindow();
                }
                return false;
            }
        });
        etSsid.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mPopupWindow == null || !mPopupWindow.isShowing()) {
                        showPopupWindow();
                    }
                } else {
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        dismissPopupWindow();
                    }
                }
            }
        });
	}

    public void showPopupWindow() {
        if (mPopupWindow == null) {
            ListView listView = new ListView(getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    ScanResult curScanResult = (ScanResult) parent.getItemAtPosition(position);
                    ssid = curScanResult.SSID;
                    etSsid.setText(curScanResult.SSID);
                    etSsid.setSelection(curScanResult.SSID.length());
                    dismissPopupWindow();
                }
            });
            mPopupWindow = new PopupWindow(getActivity());
            mPopupWindow.setContentView(listView);
            int width = getResources().getDisplayMetrics().widthPixels - 2 * UIUtils.dip2px(getActivity(), 10);
            mPopupWindow.setWidth(width);
            mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#eeeeee")));
        }
        mPopupWindow.showAsDropDown(etSsid, 0, 0);
    }

    public void dismissPopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

	public synchronized void backToNetwork() {
		String curSSID = wifiConnectUtil.getCurrentWifiSSID(getClass().getSimpleName());
		if (!TextUtils.isEmpty(curSSID)) {
			WifiConfiguration curConfig = wifiConnectUtil.IsExsits(curSSID);
			if (curConfig != null) {
				wifiManager.removeNetwork(curConfig.networkId);
			}
		}
		WifiConnectUtil.enableAllAps(getActivity());
		wifiConnectUtil.Connect(ssid, pwd, WifiConnectUtil.WifiCipherType.WIFICIPHER_WPA);
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new WifiRefreshRunnable(), refreshTimeDex, refreshTimeDex);
		}
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
		msgHandler = new GateWayHandler(getActivity(), mHandler);
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
		} else {
			APConnectFragment.showOpenWifiDialog(getActivity());
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
			((GateWayConfigActivity)getActivity()).backToApConnectView();
			break;
		case R.id.btn_exit:
			GateWayConfigManager.getInstances().finishConfig();
			MobclickAgent.onEvent(getActivity(), AnalyticsUtils.AnalyticsEventKeys.EVENT_EXIT_CONFIG);
			ActivityUtils.animFinish(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			break;
		default:
			break;
		}
	}
	
	
	public void connect() {
		GateWayConfigManager.getInstances()
			.setGateWayMsgHandler(msgHandler)
			.startConfig(getActivity(), ssid, pwd);
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
                    if (mPopupWindow != null) {
                        mPopupWindow.update();
                    }
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
	private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
				boolean success = false;
				
				ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				
				State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
				
				if (State.CONNECTED == state) {
					success = true;
				}
				if (success) {
					String currentSSID = wifiConnectUtil.getCurrentWifiSSID(getClass().getSimpleName());
					LogUtils.e(TAG, "connectionChangeReceiver: currentSSID: " + currentSSID + "/ apSSID: " + apSSID);
					if (currentSSID.equals(apSSID)) {
						if (TextUtils.isEmpty(deviceKey) && !TextUtils.isEmpty(ssid)) {
							return;
						}
					}  
					if (currentSSID.equals(ssid)) {
						if (TextUtils.isEmpty(deviceKey)) {
							wifiConnectUtil.Connect(apSSID, null, WifiConnectUtil.WifiCipherType.WIFICIPHER_NOPASS);
						} else {
							bindDevice(deviceKey);
						}
					}
				}
			} else {
				openWifi();
			}
		};
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
		getActivity().unregisterReceiver(connectionChangeReceiver);
		AbsOpenApi.cancel(getActivity(), true);
		super.onDestroy();
	};
}
