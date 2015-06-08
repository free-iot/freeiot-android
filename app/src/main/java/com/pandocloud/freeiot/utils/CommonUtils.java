package com.pandocloud.freeiot.utils;



import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.text.TextUtils;
import android.widget.Toast;
import com.pandocloud.freeiot.R;

public class CommonUtils {
	
	private static Dialog mProgressDialog;
	
	private static Toast mToast;
	
	private static boolean needExit = false;
	
	public static void ToastMsg(Context context, String msg) {
		if (context == null || TextUtils.isEmpty(msg)) {
			return;
		}
		if (mToast != null) {
			mToast.cancel();
			mToast = null;
		}
		mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		mToast.show();
	}
	
	
	public static void ToastMsg(Context context, int resourceId) {
		if (context == null) {
			return;
		}
		if (mToast != null) {
			mToast.cancel();
			mToast = null;
		}
		mToast = Toast.makeText(context, resourceId, Toast.LENGTH_SHORT);
		mToast.show();
	}

	public static void ToastMsg(final Activity activity, final String msg) {
		if (activity == null || TextUtils.isEmpty(msg)) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mToast != null) {
					mToast.cancel();
					mToast = null;
				}
				mToast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
				mToast.show();
			}
		});
	}
	
	public static final void setNeedExit(Boolean exit) {
		needExit = exit;
	}
	
	public static final boolean getNeedExit() {
		return needExit;
	}
	
	public static final void showProgressDialog(Context context, String title,
			String message, OnDismissListener onDismissListener) {
		if (context == null) {
			return;
		}
		dismissDialog();
		if (TextUtils.isEmpty(title)) {
			title = "";
		}
		if (TextUtils.isEmpty(message)) {
			message = context.getString(R.string.loading);
		}
		mProgressDialog = ProgressDialog.show(context, title, message);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setOnDismissListener(onDismissListener);
	}

	public static final void showProgressDialog(Context context, String title,
			String message) {
		showProgressDialog(context, title, message, null);
	}
	
	public static boolean showingProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			return true;
		}
		return false;
	}
	
	public static final void dismissDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
}
