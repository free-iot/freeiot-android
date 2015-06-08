package com.pandocloud.freeiot.ui.bean;

import java.io.Serializable;

public class Device implements Serializable {
	
	public String identifier;
	
	public String name;
	
	/**
	 * online/offline/unknown
	 */
	public String status; 
	
	public boolean is_owner = true;

    public String icon;

    public String app;
	
	public boolean isOnline() {
		return "online".equals(status);
	}
	
	public boolean isOffline() {
		return "offline".equals(status);
	}
	
	public boolean isUnknown() {
		return "unknown".equals(status);
	}

	public boolean isOwner() {
		return is_owner;
	}
	
	@Override
	public String toString() {
		return "Device [identifier=" + identifier + ", name=" + name
				+ ", status=" + status + "]";
	}
	
}
