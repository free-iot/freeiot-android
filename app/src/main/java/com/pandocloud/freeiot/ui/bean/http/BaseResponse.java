package com.pandocloud.freeiot.ui.bean.http;

/**
 * Created by yellow on 15/5/10.
 */
public class BaseResponse {
    public static final int CODE_SUCCESS = 0;

    public int code;

    public String message;

    public boolean isSuccess() {
        return code == CODE_SUCCESS;
    }
}
