package com.pandocloud.freeiot.ui.helper;

import com.pandocloud.android.api.interfaces.RequestListener;
import com.pandocloud.freeiot.api.ProductApi;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.ProductInfoPrefs;
import com.pandocloud.freeiot.ui.bean.ProductInfo;
import com.pandocloud.freeiot.ui.bean.http.ProductInfoResponse;
import com.pandocloud.freeiot.utils.GsonUtils;
import org.apache.http.Header;

import android.content.Context;
import android.text.TextUtils;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.utils.LogUtils;

public class ProductInfoHelper {

	private RequestListener listener;
	
	public ProductInfoHelper(RequestListener listener) {
		
		if (listener == null) {
			throw new IllegalArgumentException("ReuqestListener not allow be null...");
		}
		this.listener = listener;
	}
	
	public void getProductInfo(final Context context) {
		ProductApi.getProductInfo(context, AppConstants.PRODUCT_KEY, new BaseJsonHttpResponseHandler<ProductInfoResponse>() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e,
                                  String rawJsonResponse, ProductInfoResponse response) {
                listener.onFail(new Exception(e));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse,
                                  ProductInfoResponse response) {
                if (response != null) {
                    ProductInfo productInfo = response.data;
                    if (productInfo != null && context != null) {
                        ProductInfoPrefs.Builder builder = new ProductInfoPrefs.Builder(context);
                        builder.saveString("name", productInfo.name);
                        builder.saveString("code", productInfo.code);
                        builder.saveString("icon", productInfo.icon);
                        builder.saveString("app", productInfo.app);
                        builder.commit();
                    }
                    listener.onSuccess();
                } else {
                    listener.onFail(new Exception("response is null"));
                }

            }

            @Override
            protected ProductInfoResponse parseResponse(String rawJsonData,
                                                        boolean isFailure) throws Throwable {
                LogUtils.d("Outlet#getProductInfo-> parseResponse: " + rawJsonData);
                if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                    return null;
                }
                return GsonUtils.getInstance().getGson().fromJson(rawJsonData, ProductInfoResponse.class);
            }

            @Override
            public void onFinish() {
                listener.onFinish();
            }
        });
	}
}
