package com.pandocloud.freeiot.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

public class DialogFactory {

	public static AlertDialog createCommonDialog(Context context, String title, String message,
			String positiveTxt, String cancelTxt,
			DialogInterface.OnClickListener positionClickListener, 
			DialogInterface.OnClickListener cancelListener) {
		Builder builder = new Builder(context);
		builder.setTitle(title).setMessage(message);
		builder.setPositiveButton(positiveTxt, positionClickListener);
		builder.setNegativeButton(cancelTxt, cancelListener);
		return builder.create();
	}

}
