package com.pandocloud.freeiot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
	
	private static GsonUtils sInstance;
	
	public static GsonUtils getInstance() {
		if (sInstance == null) {
			sInstance = new GsonUtils();
		}
		return sInstance;
	}
	
	private Gson mGson;
	
	private GsonUtils() {
//		mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
//				.enableComplexMapKeySerialization()
//				.create();
		mGson = new Gson();
	}
	
	public String toJson(Object object) {
		return mGson.toJson(object);
	}
	
	public Gson getGson() {
		return mGson;
	}
}
