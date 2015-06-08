package com.pandocloud.freeiot.utils;

import android.content.Context;
import android.util.SparseArray;
import com.pandocloud.freeiot.R;

/**
 * Created by yellow on 15/5/10.
 */
public class ErrorCodeHelper {

    public static final int CODE_INVALID_TOKEN = 10010;

    private static ErrorCodeHelper sInstances;

    private SparseArray<String> msgCodeArray = new SparseArray<String>();

    private ErrorCodeHelper(Context context){
        msgCodeArray.clear();
        int[] codeArray = context.getResources().getIntArray(R.array.error_code);
        String[] msgArray = context.getResources().getStringArray(R.array.error_code_msg);
        for (int index = 0, size = codeArray.length; index < size; index ++) {
            msgCodeArray.put(codeArray[index], msgArray[index]);
        }
    }

    public static ErrorCodeHelper getInstances(Context context) {
        if (sInstances == null) {
            sInstances = new ErrorCodeHelper(context);
        }
        return sInstances;
    }

    public String getErrorMessage(int code) {
        return msgCodeArray.get(code, "");
    }

}
