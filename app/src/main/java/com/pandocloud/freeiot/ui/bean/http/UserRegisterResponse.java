package com.pandocloud.freeiot.ui.bean.http;


public class UserRegisterResponse extends BaseResponse {
	
	public UserRegisterResponseData data;
	
	public class UserRegisterResponseData {
		
		public String access_token;
	
	}
}
