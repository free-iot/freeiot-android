package com.pandocloud.freeiot.ui.bean.http;

public class UserLoginResponse extends BaseResponse {

	public UserLoginResponseData data;
	
	public class UserLoginResponseData {
		
		public String access_token;
		
	}
}
