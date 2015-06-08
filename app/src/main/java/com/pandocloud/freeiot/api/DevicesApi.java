package com.pandocloud.freeiot.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONStringer;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.utils.LogUtils;

public class DevicesApi extends AbsOpenApi {

	/**
	 * list devices that I have bound or authorized to me.
	 */
	private static final String DEVICE_LIST = "/v1/devices";

    /**
     * get device information by device identifier
     */
    private static final String DEVICE_INFO = "/v1/devices/%s";

    /**
     * modify device information
     */
    private static final String DEVICE_INFO_MODIFY = "/v1/devices/%s";

    /**
     * delete my device
     */
    private static final String DEVICE_DELETE = "/v1/devices/%s";

	/**
	 * bound device
	 */
	private static final String DEVICE_BINDING = "/v1/devices/binding";
	
	/**
	 * device permission authorization
	 */
	private static final String DEVICE_PERMISSION_AUTH = "/v1/devices/%s/permissions";
	
	/**
	 * unbound device
	 */
	private static final String DEVICE_UNBINDING = "/v1/devices/%s/unbinding";
	
	
	/**
	 * send command to device
	 */
	private static final String DEVICE_SEND_COMMANDS = "/v1/devices/%s/commands";

	
	/**
	 * get all authorized devices information
	 */
	private static final String DEVICE_PERMISSION_LIST = "/v1/devices/%s/permissions";
	
	/**
	 * delete device access authorization
	 */
	private static final String DEVICE_PERMISSION_DELETE = "/v1/devices/%s/permissions/%d";
	
	/**
	 * modify device access authorization
	 */
	private static final String DEVICE_PERMISSION_MODIFY = "/v1/devices/%s/permissions/%d";

    /**
     * query device real-time status
     */
    private static final String DEVICE_CURRENT_STATUS = "/v1/devices/%s/status/current";


    /**list devices that I have bound or authorized to me. (/v1/devices)
     * @param context
     * @param accessToken
     * @param responseHandler
     */
    public static void deviceList(Context context, String accessToken, AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        get(context, getApiServerUrl() + DEVICE_LIST, headerList, null, responseHandler);
    }

    /**
     * get device information by device identifier（/v1/devices/{identifier}）
     * <p> Method: get</p>
     * @param context
     * @param accessToken
     * @param identifier
     * @param responseHandler
     */
    public static void deviceInfo(Context context, String accessToken, String identifier, AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        get(context,  String.format(getApiServerUrl() + DEVICE_INFO, identifier), headerList, null, responseHandler);
    }


    /**
     * modify device information (/v1/devices/{identifier})
     * <p>Method: put</p>
     * @param context
     * @param accessToken
     * @param identifier
     * @param deviceName
     * @param secureLevel
     * @param responseHandler
     */
    public static void deviceInfoModify(Context context, String accessToken, String identifier,
                   String deviceName, int secureLevel, AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));

        try {
            String jsonParams = new JSONStringer()
                    .object()
                    .key(ApiKey.DEVICE_NAME).value(deviceName)
                    .key(ApiKey.SECURE_LEVEL).value(secureLevel)
                    .endObject().toString();
            if (DEBUG) {
                LogUtils.d(jsonParams);
            }
            put(context, String.format(getApiServerUrl() + DEVICE_INFO_MODIFY, identifier), headerList, jsonParams, responseHandler);
        } catch (JSONException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        }
    }


    /**
     * delete device (/v1/devices/{identifier})
     * <p>Method: delete</p>
     * @param context
     * @param accessToken
     * @param identifier
     * @param responseHandler
     */
    public static void deviceDelete(Context context, String accessToken, String identifier,
                                    AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        delete(context, String.format(getApiServerUrl() + DEVICE_DELETE, identifier), headerList, null, responseHandler);
    }

    /**
	 * device binding(/v1/devices/binding)
     * bind device
	 * @param context
	 * @param accessToken
	 * @param deviceKey
	 * @param responseHandler
	 */
	public static void deviceBind(Context context, String accessToken, String deviceKey, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
		headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
		try {
			String jsonParams = new JSONStringer()
							.object()
							.key(ApiKey.DEVICE_KEY).value(deviceKey)
							.endObject().toString();
			if (DEBUG) {
				LogUtils.d(jsonParams);
			}
			post(context, getApiServerUrl() + DEVICE_BINDING, headerList, jsonParams, responseHandler);
		} catch (JSONException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		}
	}

	/**
	 * device unbinding (/v1/devices/{identifier}/unbinding)
	 * @param context
	 * @param accessToken
	 * @param responseHandler
	 */
	public static void deviceUnbinding(Context context, String accessToken, String identifier, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
		headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
		try {
			post(context, String.format(getApiServerUrl() + DEVICE_UNBINDING, identifier), headerList, null, responseHandler);
		}  catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
		}
	}

    /**
     * get all authorized devices information(/v1/devices/{identifier}/permissions)
     * <p>Method: get</p>
     * @param context
     * @param accessToken
     * @param identifier
     * @param responseHandler
     */
    public static void devicePermissions(Context context, String accessToken, String identifier,
                                         AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        get(context, String.format(getApiServerUrl() + DEVICE_PERMISSION_LIST, identifier), headerList, null, responseHandler);
    }

    /**
     * delete device access authorization(/v1/devices/{identifier}/permissions/{permission-id})
     * <p>Method: delete</p>
     * @param context
     * @param accessToken
     * @param identifier
     * @param permissionId
     * @param responseHandler
     */
    public static void devicePermissionDelete(Context context, String accessToken,
                   String identifier, int permissionId, AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        delete(context, String.format(getApiServerUrl() + DEVICE_PERMISSION_DELETE, identifier, permissionId), headerList, null, responseHandler);
    }

    /**
     * add device access authorization (/v1/devices/{identifier}/permissions)
     * @param context
     * @param accessToken
     * @param identifier
     * @param userPhone
     * @param privilege
     * @param responseHandler
     */
    public static final void devicePermissionAuth(Context context,String productKey, String accessToken,
          String identifier, String userPhone, int privilege, AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.PRODUCT_KEY, productKey));
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        try {
            String jsonParams = new JSONStringer()
                    .object()
                    .key("user").value(userPhone)
                    .key(ApiKey.PRIVILEGE).value(privilege)
                    .endObject().toString();
            if (DEBUG) {
                LogUtils.d(jsonParams);
            }
            post(context, String.format(getApiServerUrl() + DEVICE_PERMISSION_AUTH, identifier), headerList, jsonParams, responseHandler);
        } catch (JSONException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        }
    }


    /**
     * modify device access authorization (/v1/devices/{identifier}/permissions/{permission-id})
     * <p>Method: put</p>
     * @param context
     * @param accessToken
     * @param identifier
     * @param permissionId
     * @param privilege
     * @param responseHandler
     */
    public static void deivcePermissionModify(Context context, String accessToken, String identifier,
                   int permissionId, int privilege,AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
        try {
            String jsonParams = new JSONStringer()
                    .object()
                    .key(ApiKey.PRIVILEGE).value(privilege)
                    .endObject().toString();
            if (DEBUG) {
                LogUtils.d(jsonParams);
            }
            put(context, String.format(getApiServerUrl() + DEVICE_PERMISSION_MODIFY, identifier, permissionId), headerList, jsonParams, responseHandler);
        } catch (JSONException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseHandler.onFailure(INNER_ERROR_CODE, null, null, e);
        }
    }

    /**
	 * /v1/devices/{identifier}/commands
     *
	 * @param context
	 * @param accessToken
	 * @param identifier
	 * @param jsonBody
	 * @param responseHandler
	 */
	public static void sendCommands(Context context, String accessToken,
			String identifier, String jsonBody, AsyncHttpResponseHandler responseHandler) {
		List<Header> headerList = new ArrayList<Header>();
		headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));
		try {
			if (DEBUG) {
				LogUtils.d(jsonBody);
			}
			post(context, 
					String.format(getApiServerUrl() + DEVICE_SEND_COMMANDS, identifier)
					, headerList, jsonBody, responseHandler);
		}  catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}


    /**
     * query device real-time status (/v1/devices/%s/status/current)
     * @param context
     * @param accessToken
     * @param identifier
     * @param responseHandler
     */
    public static void getDeviceCurrentState(Context context, String accessToken,
               String identifier, AsyncHttpResponseHandler responseHandler) {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader(ApiKey.HeadKey.ACCESS_TOKEN, accessToken));

        get(context,
                String.format(getApiServerUrl() + DEVICE_CURRENT_STATUS, identifier),
                headerList, null, responseHandler);
    }
}
