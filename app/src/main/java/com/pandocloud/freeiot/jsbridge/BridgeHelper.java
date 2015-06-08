package com.pandocloud.freeiot.jsbridge;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkJavascriptResultInternal;
import org.xwalk.core.internal.XWalkUIClientInternal;
import org.xwalk.core.internal.XWalkViewInternal;
import org.xwalk.core.internal.XWalkWebChromeClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pandocloud.freeiot.utils.LogUtils;


@SuppressLint("SetJavaScriptEnabled")
public class BridgeHelper {

	private final String TAG = "BridgeWebView";

	String toLoadJs = null;
	Map<String, CallBackFunction> mResponseCallbacks = new HashMap<String, CallBackFunction>();
	Map<String, BridgeHandler> mMessageHandlers = new HashMap<String, BridgeHandler>();
	BridgeHandler defaultHander = new DefaultHandler();

	List<Message> startupMessage  = null;
	long uniqueId = 0;

	private onLoadingListener mLoadingListener;

	private XWalkView xWalkView;
	
//	public BridgeWebView(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		init(context);
//	}
//
//	public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		init(context);
//	}

	public BridgeHelper(Context context, XWalkView xWalkView) {
		this.xWalkView = xWalkView;
		init(context);
	}
	
	public void setLoadingListener(onLoadingListener loadingListener) {
		this.mLoadingListener = loadingListener;
	}

	/**
	 * 
	 * @param toLoadedJsUrl
	 *            要注入的js http地址
	 * @param handler
	 *            默认的handler,负责处理js端没有指定handlerName的消息,若js端有指定handlerName,
	 *            则由native端注册的指定处理
	 */
	public void initContext(String toLoadedJsUrl, BridgeHandler handler) {
		if (toLoadedJsUrl != null) {
			this.toLoadJs = toLoadedJsUrl;
		}
		if (handler != null) {
			this.defaultHander = handler;
		}
	}

	private void init(Context context) {
		xWalkView.setVerticalScrollBarEnabled(false);
		xWalkView.setHorizontalScrollBarEnabled(false);
		xWalkView.setXWalkWebChromeClient(new BridgeWebViewClient(xWalkView));
		xWalkView.setResourceClient(new BridgeResourceViewClient(xWalkView));
        xWalkView.setUIClient(new BridgeUIClient(xWalkView));
	}

	private void handlerReturnData(String url) {
		String data = BridgeUtil.getDataFromReturnUrl(url);
		List<Message> list = null;
		try {
			list = Message.toArrayList(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (list == null || list.size() == 0) {
			Log.i(TAG, "message is empty or null...");
		}
		for (int i = 0; i < list.size(); i++) {
			Message m = list.get(i);
			String responseId = m.getResponseId();
			// 是否是response
			if (!TextUtils.isEmpty(responseId)) {
				CallBackFunction fuction = mResponseCallbacks.get(responseId);
				JSONObject responseData = m.getResponseData();
				fuction.onCallBack(responseData);
				mResponseCallbacks.remove(responseId);
			} else {
				CallBackFunction responseFunction = null;
				// 是否是callbackId
				final String callbackId = m.getCallbackId();
				if (!TextUtils.isEmpty(callbackId)) {
					responseFunction = new CallBackFunction() {
						@Override
						public void onCallBack(JSONObject data) {
							LogUtils.i(TAG, "responseFunction " + data.toString());
							Message responseMsg = new Message();
							responseMsg.setResponseId(callbackId);
							responseMsg.setResponseData(data);
							queueMessage(responseMsg);
						}
					};
				} else {
					responseFunction = new CallBackFunction() {
						@Override
						public void onCallBack(JSONObject data) {
							// do nothing
						}
					};
				}
				BridgeHandler handler;
				if (!TextUtils.isEmpty(m.getHandlerName())) {
					handler = mMessageHandlers.get(m.getHandlerName());
				} else {
					handler = defaultHander;
				}
				if (handler != null) {
					handler.handler(m.getData(), responseFunction);
				} else {
					LogUtils.e("no handler found for "+ m.getHandlerName());
				}
				
			}
		}
	}


	class BridgeWebViewClient extends XWalkWebChromeClient {

		public BridgeWebViewClient(XWalkViewInternal view) {
			super(view);
		}

	}

	class BridgeResourceViewClient extends XWalkResourceClient {

		public BridgeResourceViewClient(XWalkView view) {
			super(view);
		}

		@Override
		public void onLoadStarted(XWalkView view, String url) {
			super.onLoadStarted(view, url);
            LogUtils.e("onPageStarted=>" + url);
			if (mLoadingListener != null) {
				mLoadingListener.onPageStart();
			}
		}

		@Override
		public void onLoadFinished(XWalkView view, String url) {
			super.onLoadFinished(view, url);
            xWalkView.requestFocus();
		}

		@Override
		public void onProgressChanged(XWalkView view, int progressInPercent) {
            if (progressInPercent >= 100) {
                if (mLoadingListener != null) {
                    mLoadingListener.onPageFinished();
                }
                if (startupMessage != null) {
                    for (Message m : startupMessage) {
                        dispatchMessage(m);
                    }
                    startupMessage = null;
                }
            }
			super.onProgressChanged(view, progressInPercent);
		}

        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
            Log.e("failedUrl", "failedUrl; " + failingUrl);
            Log.e("error", "errorCode: " + errorCode);
            xWalkView.load("file:///android_asset/failed/404.html", null);
        }
    }

    class BridgeUIClient extends XWalkUIClient {

        public BridgeUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public boolean onJavascriptModalDialog(XWalkViewInternal view, JavascriptMessageTypeInternal typeInternal, String url, String message, String defaultValue, XWalkJavascriptResultInternal result) {
            LogUtils.e("url:" + url+"\n message:" + message);
            try {
                String msg = URLDecoder.decode(message, "utf-8");
                if (msg.startsWith(BridgeUtil.PANDO_RETURN_DATA)) { // 如果是返回数据
                    handlerReturnData(message);
                    result.cancel();
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return super.onJavascriptModalDialog(view, typeInternal, url, message, defaultValue, result);
        }
    }

	private void queueMessage(Message m) {
		if (startupMessage != null) {
			LogUtils.e(TAG, "queueMessage#  startupMessage is not null...");
			startupMessage.add(m);
		} else {
			dispatchMessage(m);
		}
	}

	private void dispatchMessage(Message m) {
		String messageJson = m.toBase64Json();
		String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson).trim();
		LogUtils.i(TAG, "dispatchMessage " + javascriptCommand);
		if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
			LogUtils.e("dispatchMessage#loadUrl");
			javascriptCommand = javascriptCommand.replaceAll("\n","");
			xWalkView.load(javascriptCommand, null);
		} 
	}

	
	/**
	 * 注册handler,方便web调用
	 * 
	 * @param handlerName
	 * @param handler
	 */
	public void registerHandler(String handlerName, BridgeHandler handler) {
		if (handler != null) {
			mMessageHandlers.put(handlerName, handler);
		}
	}

	public interface onLoadingListener {
		
		public void onPageStart();
		
		public void onPageFinished();
	}
}
