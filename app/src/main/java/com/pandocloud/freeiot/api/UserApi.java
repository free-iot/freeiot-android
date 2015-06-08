package com.pandocloud.freeiot.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.utils.LogUtils;

public class UserApi extends AbsOpenApi {
	/**
	 * user vertification
	 */
	private static final String USER_VERIFICATION = "/v1/users/verification";
	/**
	 * user register
	 */
	private static final String USER_REGISTER = "/v1/users/registration";
	
	/**
	 * user login
	 */
	private static final String USER_LOGIN = "/v1/users/authentication";
	
	/**
	 * user reset password
	 */
	private static final String USER_RESET_PWD = "/v1/users/reset";
	
	/**
	 * user change password
	 */
	private static final String USER_CHANGE_PWD = "/v1/users/password";
	
	/**
	 * user logout
	 */
	private static final String USER_LOGOUT = "/v1/users/logout";

    /**
     * user vertify(/v1/users/verification)
     * send vertification code to mobile or email.
     * @param context
     * @param productKey
     * @param vendorKey
     * @param mobile
     * @param mail
     * @param responseHandler
     */
	public static void verification(Context context, String productKey,String vendorKey,
                   String mobile, String mail, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
        if (!TextUtils.isEmpty(productKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.PRODUCT_KEY, productKey));
        }
		if (!TextUtils.isEmpty(vendorKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.VENDOR_KEY, vendorKey));
        }
		try {
//			String jsonParams = new JSONStringer().object()
//						.key(ApiKey.MOBILE).value(mobile)
//						.endObject().toString();
            String jsonParams = "";
            JSONStringer jsonStringer = new JSONStringer().object();
            if (!TextUtils.isEmpty(mobile)) {
                jsonStringer.key(ApiKey.MOBILE).value(mobile);
            } else {
                jsonStringer.key(ApiKey.MAIL).value(mail);
            }
            jsonParams = jsonStringer.endObject().toString();
			if (DEBUG) {
				LogUtils.d(jsonParams);
			}
			post(context, getApiServerUrl() + USER_VERIFICATION, headerList, jsonParams, responseHandler);
		} catch (JSONException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		}
		
	}

    /**
     * user register(/v1/users/registration)
     * register with phone number
     * @param context
     * @param productKey
     * @param vendorKey
     * @param mobile
     * @param password
     * @param verification
     * @param responseHandler
     */
	public static void mobileRegister(Context context, String productKey, String vendorKey,
                   String mobile, String password, String verification, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
        if (!TextUtils.isEmpty(productKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.PRODUCT_KEY, productKey));
        }
        if (!TextUtils.isEmpty(vendorKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.VENDOR_KEY, vendorKey));
        }
		try {
			String jsonParams = new JSONStringer()
							.object()
							.key(ApiKey.MOBILE).value(mobile)
							.key(ApiKey.PASSWORD).value(password)
							.key(ApiKey.VERTIFICATION).value(verification)
							.endObject().toString();
			if (DEBUG) {
				LogUtils.d(jsonParams);
			}
			post(context, getApiServerUrl() + USER_REGISTER, headerList, jsonParams, responseHandler);
		} catch (JSONException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		}
		
	}

    /**
     *  user register(/v1/users/registration)
     * register with email
     * @param context
     * @param productKey
     * @param vendorKey
     * @param mail
     * @param password
     * @param responseHandler
     */
    public static void mailRegister(Context context, String productKey, String vendorKey, String mail,
                          String password,  AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        if (!TextUtils.isEmpty(productKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.PRODUCT_KEY, productKey));
        }
        if (!TextUtils.isEmpty(vendorKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.VENDOR_KEY, vendorKey));
        }
        try {
            String jsonParams = new JSONStringer()
                    .object()
                    .key(ApiKey.MAIL).value(mail)
                    .key(ApiKey.PASSWORD).value(password)
                    .endObject().toString();
            if (DEBUG) {
                LogUtils.d(jsonParams);
            }
            post(context, getApiServerUrl() + USER_REGISTER, headerList, jsonParams, responseHandler);
        } catch (JSONException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        }
    }

    /**
     * user login(/v1/users/authentication)
     * user login with phone number or email.
     * @param context
     * @param productKey
     * @param vendorKey
     * @param mobile
     * @param password
     * @param responseHandler
     */
	public static void login(Context context, String productKey, String vendorKey, String mobile,
                          String mail, String password, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
        if (!TextUtils.isEmpty(productKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.PRODUCT_KEY, productKey));
        }
        if (!TextUtils.isEmpty(vendorKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.VENDOR_KEY, vendorKey));
        }
		try {
			String jsonParams = "";
            JSONStringer jsonStringer = new JSONStringer().object();
            if (!TextUtils.isEmpty(mobile)) {
                jsonStringer.key(ApiKey.MOBILE).value(mobile);
            } else {
                jsonStringer.key(ApiKey.MAIL).value(mail);
            }
            jsonStringer.key(ApiKey.PASSWORD).value(password);
			jsonParams = jsonStringer.endObject().toString();
			if (DEBUG) {
				LogUtils.d(jsonParams);
			}
			post(context, getApiServerUrl() + USER_LOGIN, headerList, jsonParams, responseHandler);
		} catch (JSONException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		}
	}


    /**
     * user logout (/v1/users/logout)
     * @param context
     * @param accessToken
     * @param responseHandler
     */
    public static void logout(Context context, String accessToken, AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        try {
            post(context, getApiServerUrl() + USER_LOGOUT, headerList, null, responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        }
    }

    /**
     * user reset password(/v1/users/reset)
     * @param context
     * @param productKey
     * @param vendorKey
     * @param mobile
     * @param mail
     * @param password
     * @param verification
     * @param responseHandler
     */
	public static void reset(Context context, String productKey, String vendorKey, String mobile,
            String mail, String password, String verification, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
        if (!TextUtils.isEmpty(productKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.PRODUCT_KEY, productKey));
        }
        if (!TextUtils.isEmpty(vendorKey)) {
            headerList.add(new BasicHeader(ApiKey.HeadKey.VENDOR_KEY, vendorKey));
        }
		try {
			String jsonParams = "";
            JSONStringer jsonStringer = new JSONStringer().object();
            if (!TextUtils.isEmpty(mobile)) {
                jsonStringer.key(ApiKey.MOBILE).value(mobile);
            } else {
                jsonStringer.key(ApiKey.MAIL).value(mail);
            }
			jsonParams = jsonStringer
                    .key(ApiKey.PASSWORD)
                    .value(password)
                    .key(ApiKey.VERTIFICATION).value(verification)
					.endObject().toString();
			if (DEBUG) {
				LogUtils.d(jsonParams);
			}
			post(context, getApiServerUrl() + USER_RESET_PWD, headerList, jsonParams, responseHandler);
		} catch (JSONException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		}
	}


	/**
	 * user change password（/v1/users/password）
	 * <p>Method: put</p>
	 * @param context
	 * @param accessToken
	 * @param mobile
	 * @param password
	 * @param newPassword
	 * @param responseHandler
	 */
	public static void changePassword(Context context, String accessToken, String mobile,
			String password, String newPassword, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
		headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
		try {
			String jsonParams = new JSONStringer()
							.object()
							.key(ApiKey.MOBILE).value(mobile)
							.key(ApiKey.PASSWORD).value(password)
							.key(ApiKey.NEW_PASSWORD).value(newPassword)
							.endObject().toString();
			if (DEBUG) {
				LogUtils.d(jsonParams);
			}
			put(context, getApiServerUrl() + USER_CHANGE_PWD, headerList, jsonParams, responseHandler);
		} catch (JSONException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		}
	}

}
