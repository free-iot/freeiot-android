package com.pandocloud.freeiot.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {
	public static final int VERBOSE = 0x0001;
	public static final int DEBUG = 0x0002;
	public static final int INFO = 0x0004;
	public static final int WARN = 0x0008;
	public static final int ERROR = 0x0010;
	
	public static final int LEVEL_RELEASE = 0x0000;

	public static final int LEVEL_TEST = VERBOSE | DEBUG | INFO | WARN | ERROR;
	
	private static int LEVEL = LEVEL_TEST;//上线时改成LEVEL_RELEASE
	
	private static final String TAG = "PANDO_LOG";
	
	public static final void setLevel(int level) {
		LEVEL = level;
	}
	
	private static boolean check(int level) {
		if ((LEVEL & level) > 0) {
			return true;
		}
		return false;
	}
	
	public static void v(String text) {
		if (check(VERBOSE)) android.util.Log.v(TAG, text);
	}
	
	public static void v(String tag, String text) {
		if (check(VERBOSE)) android.util.Log.v(tag, text);
	}
	
	public static void d(String text) {
		if (check(DEBUG)) android.util.Log.d(TAG, text);
	}

	public static void d(String text, Throwable tr) {
		if (check(DEBUG)) android.util.Log.d(TAG, text, tr);
	}
	
	public static void d(String tag, String text) {
		if (check(DEBUG)) android.util.Log.d(tag, text);
	}

	public static void d(String tag, String text, Throwable tr) {
		if (check(DEBUG)) android.util.Log.d(tag, text, tr);
	}

	public static void i(String text) {
		if (check(INFO)) android.util.Log.i(TAG, text);
	}

	public static void i(String text, Throwable tr) {
		if (check(INFO)) android.util.Log.i(TAG, text, tr);
	}

	public static void i(String tag, String text) {
		if (check(INFO)) android.util.Log.i(tag, text);
	}
	
	public static void i(String tag, String text, Throwable tr) {
		if (check(INFO)) android.util.Log.i(tag, text, tr);
	}
	
	public static void w(String text) {
		if (check(WARN)) android.util.Log.w(TAG, text);
	}
	
	public static void w(String text, Throwable tr) {
		if (check(WARN)) android.util.Log.w(TAG, text, tr);
	}
	
	public static void w(String tag, String text) {
		if (check(WARN)) android.util.Log.w(tag, text);
	}
	
	public static void w(String tag, String text, Throwable tr) {
		if (check(WARN)) android.util.Log.w(tag, text, tr);
	}
	
	public static void e(String text) {
		if (check(ERROR)) android.util.Log.e(TAG, text);
	}
	
	public static void e(Throwable e) {
		e(getStringFromThrowable(e));
	}
	
	public static void e(Throwable e, String message) {
		e(message +"\n" + getStringFromThrowable(e));
	}
	
	public static void e(String text, Throwable tr) {
		if (check(ERROR)) android.util.Log.e(TAG, text, tr);
	}
	
	public static void e(String tag, String text) {
		if (check(ERROR)) android.util.Log.e(tag, text);
	}
	
	public static void e(String tag, String text, Throwable tr) {
		if (check(ERROR)) android.util.Log.e(tag, text, tr);
	}

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }
    
    public static String getStringFromThrowable(Throwable e) {
		StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    return sw.toString();
	}
    
    public static void e(String tag, byte[] data) {
    	if (data == null) {
			return;
		}
    	StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i< data.length; i++) {
			sBuffer.append(Integer.toHexString(data[i])).append(" ");
		}
		LogUtils.e("body length: " + sBuffer.toString());
    }
}

