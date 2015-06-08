package com.pandocloud.freeiot.jsbridge;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.webkit.WebView;

public class BridgeUtil {
	private static final String TAG = "BridgeUtil";
	
	final static String PANDO_OVERRIDE_SCHEMA = "pando://";
	final static String PANDO_RETURN_DATA = "pando://invoke/";//格式为   pando://invoke/invoke_params_string
	final static String EMPTY_STR = "";
	final static String SPLIT_MARK = "/";
	
	final static String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:PandoJSBridge._handleMessageFromNative('%s');";

	public static String getDataFromReturnUrl(String url) {
		String temp = url.replace(PANDO_RETURN_DATA, EMPTY_STR);
		String[] fuctionAndData = temp.split(SPLIT_MARK);
		if(fuctionAndData != null && fuctionAndData.length >= 1){
			return fuctionAndData[0];
		}
		return null;
	}

	public static String getFunctionFromReturnUrl(String url) {
		
		Log.e(TAG, "getFunctionFromReturnUrl  url=>" + url);
		
		String temp = url.replace(PANDO_RETURN_DATA, EMPTY_STR);
		String[] fuctionAndData = temp.split(SPLIT_MARK);
		if(fuctionAndData != null && fuctionAndData.length >= 1){
			return fuctionAndData[0];
		}
		return null;
	}

	
	
	/**
	 * js 文件将注入为第一个script引用
	 * @param view
	 * @param url
	 */
	public static void webViewLoadJs(WebView view, String url){
		String js = "var newscript = document.createElement(\"script\");";
		js += "newscript.src=\"" + url + "\";";
		js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);";
		view.loadUrl("javascript:" + js);
	}
	
	public static String assetFile2Str(Context c, String urlStr){
		InputStream in = null;
		try{
			in = c.getAssets().open(urlStr);
			
			int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
 
            // byte buffer into a string
            String text = new String(buffer);
//            Log.i(TAG, "assetFile2Str: " + text);
            return text;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public static boolean hasAssetFile(Context c, String filePath) {
		boolean hasFile = false;
		InputStream inputStream = null;
		try{
			AssetManager am = c.getAssets();
			inputStream = am.open(filePath);
			hasFile = true;
		}catch(Exception e){
			e.printStackTrace();
			hasFile = false;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return hasFile;
	}
}
