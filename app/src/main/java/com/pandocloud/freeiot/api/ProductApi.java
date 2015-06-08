package com.pandocloud.freeiot.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pandocloud.android.api.AbsOpenApi;

public class ProductApi extends AbsOpenApi {
	
	/**
	 * get product info
	 */
	private static final String PRODUCT_INFO = "/v1/product/info";
	

	/**
	 * get product information by product key
	 * 
	 * <p>Method: GET</p>
	 * @param context
	 * @param productKey
	 * @param responseHandler
	 */
	public static void getProductInfo(Context context, String productKey, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
		headerList.add(new BasicHeader(ApiKey.HeadKey.PRODUCT_KEY, productKey));
		get(context, getApiServerUrl() + PRODUCT_INFO, headerList, null, responseHandler);
	}

}
