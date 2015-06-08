package com.pandocloud.freeiot.utils;

import com.umeng.analytics.MobclickAgent;

public class AnalyticsUtils {
	
	public static void init() {
		
		MobclickAgent.setDebugMode(false);
		
		MobclickAgent.openActivityDurationTrack(false);
	}
	
	public static final class AnalyticsViewKeys {
		
		public static final String VIEW_SPLASH_ACTIVITY = "Splash";
		
		public static final String VIEW_MAIN_ACTIIVTY = "Main";
		
		public static final String VIEW_REGISTER_ACTIVITY = "Register";

        public static final String VIEW_EMAIL_REGISTER_ACTIVITY = "EmailRegister";
		
		public static final String VIEW_RESET_PWD_ACTIVITY = "ResetPassword";

        public static final String VIEW_EMAIL_RESET_PWD_ACTIVITY = "EmailResetPassword";
		
		public static final String VIEW_LOGIN_ACTIVITY = "Login";

        public static final String VIEW_EMAIL_LOGIN_ACTIVITY = "EmailLogin";
		
		public static final String VIEW_GATEWAY_CONFIG_ACTIVITY = "GatewayConfigActivity";
		
		public static final String VIEW_APCONNECT_FGMENT = "ApConnect";
		
		public static final String VIEW_APSSID_CONFIG_FGMENT = "ApSsidConfig";
		
		public static final String VIEW_DEVICE_CONTROL_ACTIVITY = "DeviceControl";
		
		public static final String VIEW_DEVICE_AUTH_ACTIVITY = "DeviceAuth";
	}
	
	public static final class AnalyticsEventKeys {
		
		/**
		 * vertification code click event
		 */
		public static final String EVENT_VERTIFY = "vertify";


		public static final String EVENT_RESET_PWD_VERTIFY = "reset_password_vertify";
		
		/**
		 * register click
		 */
		public static final String EVENT_REGISTER = "register";

        /**
         * reset password click event
         */
		public static final String EVENT_RESET_PWD = "reset_password";
		
		/**
		 * login click event
		 */
		public static final String EVENT_LOGIN = "login";
		
		/**
		 * add device click event
		 */
		public static final String EVENT_ADD_DEVICE = "add_device";
		
		/**
		 * exit config click event
		 */
		public static final String EVENT_EXIT_CONFIG = "exit_config";
		
		/**
		 * complete device online config event
		 */
		public static final String EVENT_APCONFIG = "apconfig";
		
		/**
		 * device auth click event
		 */
		public static final String EVENT_DEVICE_AUTH = "device_auth";
	}
}
