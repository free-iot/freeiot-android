package com.pandocloud.freeiot.ui.app;


import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.pandocloud.freeiot.ui.helper.DeviceRegisterHelper;
import com.pandocloud.freeiot.ui.urlconfig.UrlConfigManager;
import com.pandocloud.freeiot.utils.AnalyticsUtils;


public class FreeIOTApp extends Application {
	
	@Override
	public void onCreate() {
		
//		AbsOpenApi.DEBUG = false;
		
		AnalyticsUtils.init();
		
		super.onCreate();
		
		initConfig();

		DeviceRegisterHelper.getInstances().checkDeviceRegister(this);
		
	}
	
	public void initConfig() {
		//环境配置
		if(AppConfigPrefs.getInstances(this).containKey("cur_env")) {
			int state = AppConfigPrefs.getInstances(this).getIntValue("cur_env", UrlConfigManager.RELEASE_STATE);
			UrlConfigManager.updateUrl(state);
		}
		initImageLoader(this);
	}
	
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
//		config.writeDebugLogs(); // Remove for release app

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config.build());
	}
}
