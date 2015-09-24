package com.pandocloud.freeiot.ui.device;

import java.io.File;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.DevicesApi;
import com.pandocloud.freeiot.api.WrapperJsonHttpResponseHandler;
import com.pandocloud.freeiot.jsbridge.BridgeHandler;
import com.pandocloud.freeiot.jsbridge.BridgeUtil;
import com.pandocloud.freeiot.jsbridge.BridgeHelper;
import com.pandocloud.freeiot.jsbridge.CallBackFunction;
import com.pandocloud.freeiot.jsbridge.DefaultHandler;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

public class DeviceControlActivity extends BaseActivity implements OnClickListener, OnRefreshListener {

	private String mIdentifier;
	
	private String mTitleName;

	private BridgeHelper bridgeHelper;

	public XWalkView mWebView;
	
	private String mUrl;
	
	private SwipeRefreshLayout mSwipeRefreshLayout;
	
	private ProgressBar mProgressBar;

//	private String operateUrl = UrlConfigManager.getOutletControlUrl();
	
	@Override
	protected void onCreate(Bundle savedInstances) {
		super.onCreate(savedInstances);
		setContentView(R.layout.activity_outlet_control);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
            mIdentifier = bundle.getString("identifier");
//			operateUrl = String.format(operateUrl, mIdentifier);
			mUrl = bundle.getString("app");
			if (BridgeUtil.hasAssetFile(this, "index.html")) {
				mUrl = "file:///android_asset/index.html";
			}
			LogUtils.e("url=> " + mUrl);
//			mUrl = "file:///android_asset/index.html";
//            mUrl = "file:///android_asset/failed/404.html";
            mTitleName = bundle.getString("name");
			((TextView)findViewById(R.id.title)).setText(mTitleName);
		}
		
		mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
		mSwipeRefreshLayout.setColorScheme(R.color.main_red_color);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		
		mProgressBar = (ProgressBar)findViewById(R.id.progressbar);
		mWebView = (XWalkView) findViewById(R.id.webview);
		findViewById(R.id.back).setOnClickListener(this);
		bridgeHelper = new BridgeHelper(this, mWebView);
		initVebView();
	}

	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled", "NewApi" }) 
	public void initVebView() {
		mWebView.clearCache(false);
		XWalkSettings settings = mWebView.getSettings();
//		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        if (settings != null) {
            settings.setJavaScriptEnabled(true);
//		settings.setBuiltInZoomControls(false);
//		settings.setSupportZoom(false);
            settings.setUseWideViewPort(true);
//		settings.setRenderPriority(RenderPriority.HIGH);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
//		settings.setLoadWithOverviewMode(true);
            settings.setAppCacheEnabled(true);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ".pando");
            if (!file.exists()) {
                file.mkdirs();
            }
            settings.setAppCachePath(file.getAbsolutePath());
        }

		bridgeHelper.setLoadingListener(webviewLoadingListener);

		registerHandlers();
		//"file:///android_asset/pdjs.js"
		bridgeHelper.initContext(null, new DefaultHandler());
		mWebView.load(mUrl, null);
	}
	
	BridgeHelper.onLoadingListener webviewLoadingListener = new BridgeHelper.onLoadingListener(){

		@Override
		public void onPageStart() {
			mSwipeRefreshLayout.setRefreshing(true);
		}

		@Override
		public void onPageFinished() {
			mSwipeRefreshLayout.setRefreshing(false);
		}
		
	};	
	
	@Override
	public void onRefresh() {
		LogUtils.d("mUrl: " + mUrl);
		mWebView.load(mUrl, null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_DEVICE_CONTROL_ACTIVITY);
		MobclickAgent.onResume(this);
        if (mWebView != null) {
            mWebView.resumeTimers();
            mWebView.onShow();
        }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_DEVICE_CONTROL_ACTIVITY);
		MobclickAgent.onPause(this);
        if (mWebView != null) {
            mWebView.pauseTimers();
            mWebView.onHide();
        }
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			ActivityUtils.animFinish(DeviceControlActivity.this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			break;
		default:
			break;
		}
	}
	
	public void registerHandlers() {
		bridgeHelper.registerHandler("sendCommand", new BridgeHandler() {

			@Override
			public void handler(String data, final CallBackFunction function) {
				LogUtils.e("sendCommandHandler=>" + data);
					DevicesApi.sendCommands(DeviceControlActivity.this,
                            UserState.getInstances(DeviceControlActivity.this).getAccessToken(""),
                            mIdentifier, data,
                            new WrapperJsonHttpResponseHandler(DeviceControlActivity.this) {
                                @Override
                                public void onStart() {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onSuccess(int statusCode,
                                                      Header[] headers, JSONObject response) {
                                    if (response.has("code")) {
                                        try {
                                            int code = response.getInt("code");
                                            if (code != BaseResponse.CODE_SUCCESS) {
                                                super.onSuccess(statusCode, headers, response);
                                            } else {
                                                function.onCallBack(response);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onFinish() {
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
			}
		});

		bridgeHelper.registerHandler("currentStatus", new BridgeHandler() {

			@Override
			public void handler(String data, final CallBackFunction function) {

				DevicesApi.getDeviceCurrentState(DeviceControlActivity.this,
						UserState.getInstances(DeviceControlActivity.this).getAccessToken(""),
						mIdentifier, new WrapperJsonHttpResponseHandler(DeviceControlActivity.this) {

							@Override
							public void onStart() {
								mProgressBar.setVisibility(View.VISIBLE);
							}

							@Override
							public void onFailure(int statusCode, Header[] headers,
												  Throwable throwable, JSONObject errorResponse) {
								super.onFailure(statusCode, headers, throwable, errorResponse);
							}

							@Override
							public void onSuccess(int statusCode, Header[] headers,
												  JSONObject response) {
								LogUtils.e(response.toString());
								if (response.has("code")) {
									try {
										int code = response.getInt("code");
										if (code != BaseResponse.CODE_SUCCESS) {
											super.onSuccess(statusCode, headers, response);
										} else {
											function.onCallBack(response);
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}

							@Override
							public void onFinish() {
								mProgressBar.setVisibility(View.GONE);
							}
						});

			}
		});

		bridgeHelper.registerHandler("getCurrentStatus", new BridgeHandler() {

			@Override
			public void handler(String data, final CallBackFunction function) {

				DevicesApi.getDeviceCurrentState(DeviceControlActivity.this,
						UserState.getInstances(DeviceControlActivity.this).getAccessToken(""),
						mIdentifier, new WrapperJsonHttpResponseHandler(DeviceControlActivity.this) {

							@Override
							public void onStart() {
								mProgressBar.setVisibility(View.VISIBLE);
							}

							@Override
							public void onFailure(int statusCode, Header[] headers,
												  Throwable throwable, JSONObject errorResponse) {
								super.onFailure(statusCode, headers, throwable, errorResponse);
							}

							@Override
							public void onSuccess(int statusCode, Header[] headers,
												  JSONObject response) {
								LogUtils.e(response.toString());
								if (response.has("code")) {
									try {
										int code = response.getInt("code");
										if (code != BaseResponse.CODE_SUCCESS) {
											super.onSuccess(statusCode, headers, response);
										} else {
											function.onCallBack(response);
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}

							@Override
							public void onFinish() {
								mProgressBar.setVisibility(View.GONE);
							}
						});

			}
		});

		bridgeHelper.registerHandler("setCurrentStatus", new BridgeHandler() {

			@Override
			public void handler(String data, final CallBackFunction function) {

				DevicesApi.setDeviceCurrentState(DeviceControlActivity.this,
						UserState.getInstances(DeviceControlActivity.this).getAccessToken(""),
						mIdentifier,data, new WrapperJsonHttpResponseHandler(DeviceControlActivity.this){

							@Override
							public void onStart() {
								mProgressBar.setVisibility(View.VISIBLE);
							}
							@Override
							public void onFailure(int statusCode, Header[] headers,
												  Throwable throwable, JSONObject errorResponse) {
								super.onFailure(statusCode, headers, throwable, errorResponse);
							}

							@Override
							public void onSuccess(int statusCode, Header[] headers,
												  JSONObject response) {
								LogUtils.e(response.toString());
								if (response.has("code")) {
									try {
										int code = response.getInt("code");
										if (code != BaseResponse.CODE_SUCCESS) {
											super.onSuccess(statusCode, headers, response);
										} else {
											function.onCallBack(response);
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
							@Override
							public void onFinish() {
								mProgressBar.setVisibility(View.GONE);
							}
						});

			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        if (mWebView != null) {
            mWebView.onDestroy();
        }
		AbsOpenApi.cancel(this, true);
	}

}
