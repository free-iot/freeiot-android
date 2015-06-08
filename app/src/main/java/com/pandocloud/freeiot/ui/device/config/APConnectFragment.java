package com.pandocloud.freeiot.ui.device.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.pandocloud.android.config.GateWayConfigManager;
import com.pandocloud.android.utils.NetworkUtil;
import com.pandocloud.android.utils.WifiConnectUtil;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.ui.app.ProductInfoPrefs;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.DialogFactory;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;


public class APConnectFragment extends Fragment implements OnClickListener {
	
	private static final String TAG = "pando#APConnectFragment";
	
	private List<ScanResult> scanResults;
	
	private WifiManager wifiManager;
	
	private WifiConnectUtil wifiConnectUtil;
	
	private static String apSSID;
	
	private boolean hasConnectedAP = false;
	
	/**
	 * wifi scan次数
	 */
	private int scanCount = 0;
	
	/**
	 * wifi scan最大次数
	 */
	private static final int maxScanCount = 15;
	
	/**
	 * AP连接次数
	 */
	private int connectCount = 0;
	
	/**
	 * AP连接最大次数
	 */
	private static final int maxTryCount = 5;
	
	private Handler mHandler = new Handler();
	
	private Timer timer;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GateWayConfigActivity.startConfigTime = System.currentTimeMillis();
		
		apSSID = ProductInfoPrefs.getInstances(getActivity()).getString("code", "pando_test");

		LogUtils.e(apSSID);
		wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(connectionChangeReceiver, filter);
		wifiConnectUtil = new WifiConnectUtil(wifiManager);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater,
		 ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_apconnect, container, false);
		return rootView;
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			((GateWayConfigActivity)getActivity()).backToApConnectView();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_APCONNECT_FGMENT);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_APCONNECT_FGMENT);
	}
	
	@Override
	public void onActivityCreated( Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		LogUtils.e(TAG, "APConnectFragment:  onActivityCreated...");
//		if (wifiManager == null) {
//			wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
//			wifiConnectUtil = new WifiConnectUtil(wifiManager);
//		}	
		hasConnectedAP = false;
		initData();	
	}
	
	private void showScanningDeviceDialog() {
		CommonUtils.setNeedExit(true);
		CommonUtils.showProgressDialog(getActivity(), "", getString(R.string.scaning_devices), new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				LogUtils.e(TAG, "DialogInterface#OnDismissListener...");
				if (!hasConnectedAP && CommonUtils.getNeedExit()) {
					CommonUtils.setNeedExit(false);
					ActivityUtils.animFinish(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
				}
			}
		});
	}
	
	private void initData() {
		showScanningDeviceDialog();
		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
			boolean openWifi = wifiConnectUtil.OpenWifi();
			if (openWifi) {
				startScanWifi();
			} else {
				showOpenWifiDialog(getActivity());
			}
		} else {
			startScanWifi();
		}
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					startScanWifi();
				}
			}, 2000, 2000);
		}
	}
	
	
	private void startScanWifi() {
		if (wifiManager.startScan()) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					refreshSSID();
				}
			});
		}
	}
	
	public static void showOpenWifiDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(null);
		builder.setMessage(R.string.open_wifi_tip);
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				context.startActivity(intent);
			}
		});
		builder.setNegativeButton(android.R.string.yes, null);
		builder.create().show();
	}
	
	public void setAPHostAddress() {
		DhcpInfo dhcpinfo = wifiManager.getDhcpInfo(); 
		if (dhcpinfo == null) {
			return;
		}
		try {
			InetAddress ipAddress = InetAddress.getByAddress(NetworkUtil.toIPByteArray(dhcpinfo.serverAddress));
			if (ipAddress != null) {
				String host = ipAddress.getHostAddress();
				LogUtils.e("blocker", "AP host: " + host);
				GateWayConfigManager.getInstances().setConnectHost(host);
				if (apNotConnectedDialog != null && apNotConnectedDialog.isShowing()) {
					apNotConnectedDialog.dismiss();
				}
				if (apNotFoundDialog != null && apNotFoundDialog.isShowing()) {
					apNotFoundDialog.dismiss();
				}
				toGateWayConfigView();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	
	public void toGateWayConfigView() {
		CommonUtils.dismissDialog();
		if (getActivity() != null) {
			((GateWayConfigActivity)getActivity()).toGateWayConfigView(apSSID);
		}
	}
	
	public void wifiConnect() {
		LogUtils.d(TAG, "wifi connect...");
		wifiConnectUtil.Connect(apSSID, null, WifiConnectUtil.WifiCipherType.WIFICIPHER_NOPASS);
//		wifiConnectUtil.Connect(apSSID, "123456123456", WifiCipherType.WIFICIPHER_WPA);
	}
	
	@Override
	public void onDestroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		mHandler.removeCallbacksAndMessages(null);
		getActivity().unregisterReceiver(connectionChangeReceiver);
		super.onDestroy();
	}
	
	public void refreshSSID() {
		LogUtils.i(TAG, "WifiManager SCAN_RESULTS_AVAILABLE_ACTION...");
		scanCount ++;
		if (scanCount > maxScanCount) {
			showNoApFoundDialog();
			return;
		}
		String currentSSID = wifiConnectUtil.getCurrentWifiSSID(getClass().getSimpleName());
		LogUtils.v(TAG, "currentSSID: " + currentSSID);
		if (apSSID.equals(currentSSID) && NetworkUtil.isWifi(getActivity())) {
			mHandler.postDelayed(new Runnable(){
				
				@Override
				public void run() {
					String curSSID = wifiConnectUtil.getCurrentWifiSSID(getClass().getSimpleName());
					LogUtils.e(TAG, "checkAPSSID -> curSSID: " + curSSID);
					if(getActivity() != null && apSSID.equals(curSSID) 
							&& NetworkUtil.isWifi(getActivity())) {
						CommonUtils.dismissDialog();
						setAPHostAddress();
						hasConnectedAP = true;
					} else {
						startScanWifi();
					}
				}
			}, 500);
			return;
		} 
		else {
			if (!apSSID.equals(currentSSID)) {
				LogUtils.d("ssidRefreshReceiver -> currentSSID: " + currentSSID);
				WifiConfiguration config = wifiConnectUtil.IsExsits(currentSSID);
				if (config != null) {
					wifiManager.disableNetwork(config.networkId);
				}
			}
		}
		scanResults = wifiManager.getScanResults();
		boolean needScanWifi = true;
		if (scanResults != null) {
			for (ScanResult result : scanResults) {
				if (!TextUtils.isEmpty(result.SSID) && apSSID.equals(result.SSID)) {
					LogUtils.e(APConnectFragment.class.getSimpleName(), "scanResult: " + result.SSID);
					needScanWifi = false;
					hasConnectedAP = false;
					connectCount = 1;
					LogUtils.e(TAG, "ssid selected: " + apSSID);
					wifiConnect();
					break;
				}
			}
		} 
		if (needScanWifi) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			mHandler.postDelayed(new Runnable() {
//
//				@Override
//				public void run() {
//					startScanWifi();
//				}
//			}, 800);
			startScanWifi();
		}
	}
	
	
	private AlertDialog apNotFoundDialog;
	public void showNoApFoundDialog() {
		CommonUtils.setNeedExit(false);
		CommonUtils.dismissDialog();
		if (apNotFoundDialog != null && apNotFoundDialog.isShowing()) {
			return;
		}
		apNotFoundDialog = DialogFactory.createCommonDialog(getActivity(), "",
                getString(R.string.tip_not_found_devices), getString(android.R.string.ok), getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        scanCount = 0;
                        connectCount = 0;
                        showScanningDeviceDialog();
                        startScanWifi();
                    }
                }, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityUtils.animFinish(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    }
                });
		apNotFoundDialog.show();
	}

	
	
	private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtils.i(TAG + " connectionChangeReceiver#onReceive -> action: " + action);
			handlerConnectionChanged(context);
		};
	};
	
	
	private volatile long lastHandlerTime = 0;
	
	private AlertDialog apNotConnectedDialog;
	
	public void handlerConnectionChanged(Context context) {
		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			if (hasConnectedAP) {
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastHandlerTime < 2000) {
				return;
			}
			lastHandlerTime = currentTime;
			
			boolean success = false;
			
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			
			if (State.CONNECTED == state) {
				success = true;
			}
			
			if (success) {
				String currentSSID = wifiConnectUtil.getCurrentWifiSSID(getClass().getSimpleName());
				currentSSID = currentSSID.trim();
				LogUtils.e(TAG, "connectionChangeReceiver: currentSSID: " + currentSSID + "/ apSSID: " + apSSID);
				if (currentSSID.equals(apSSID) && NetworkUtil.isWifi(context)) {
					CommonUtils.dismissDialog();
					LogUtils.e(TAG, "connectionChangeReceiver -> ap config success....");
					setAPHostAddress();
					hasConnectedAP = true;
				} else {
					connectCount ++;
					if (connectCount > maxTryCount) {
						CommonUtils.dismissDialog();
						if (apNotConnectedDialog != null && apNotConnectedDialog.isShowing()) {
							return;
						}
						apNotConnectedDialog = DialogFactory.createCommonDialog(getActivity(), "", 
								getString(R.string.tip_not_found_devices), getString(android.R.string.ok), 
							getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								connectCount = 0;
								scanCount = 0;
								showScanningDeviceDialog();
								wifiConnect();
							}
						}, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								ActivityUtils.animFinish(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
							}
						});
						apNotConnectedDialog.show();
					} else {
						wifiConnect();
					}
				}
			} 
		}
	}
}
