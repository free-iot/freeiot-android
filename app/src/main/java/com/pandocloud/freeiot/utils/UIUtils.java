package com.pandocloud.freeiot.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class UIUtils {
	
	public static int dip2px(Context context, float dip) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return (int) px;
	}

	/**
	 * px转dip
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		
		return (int) (pxValue / scale + 0.5f);
	}
}
