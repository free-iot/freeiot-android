package com.pandocloud.freeiot.ui.db;

import java.util.HashMap;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pandocloud.freeiot.ui.bean.Device;


public class DBManager {
	
	private static DBManager dbManager;
	
	private DBHelper dbHelper;
	private SQLiteDatabase readDB;
	private SQLiteDatabase writeDB;
	private DBManager(Context context) {
		dbHelper = new DBHelper(context);
		readDB = dbHelper.getReadableDatabase();
		writeDB = dbHelper.getWritableDatabase();
	}
	
	public static DBManager getInstances(Context context) {
		if (dbManager == null) {
			dbManager = new DBManager(context.getApplicationContext());
		}
		return dbManager;
	}
	
	
	public synchronized HashMap<String, Device> queryDeviceInfo() {
		HashMap<String, Device> resultHashMap = null;
		if (readDB == null) {
			readDB = dbHelper.getReadableDatabase();
		}
		Cursor cursor = null;
		try {
			cursor = readDB.query(DBHelper.TABLE_DEVICE_INFO, 
					new String[]{DBHelper.COLUMN_IDENTIFIER, DBHelper.COLUMN_DEVICE_NAME}, null, null, null, null, null);
			if (cursor != null) {
				resultHashMap = new HashMap<String, Device>();
				while(cursor.moveToNext()) {
					String identifier = cursor.getString(0);
					String name = cursor.getString(1);
					Device device = new Device();
					device.identifier = identifier;
					device.name = name;
					resultHashMap.put(identifier, device);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
		return resultHashMap;
	}
	
	public synchronized void updateDevieInfo(String identifier, String name) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_IDENTIFIER, identifier);
		values.put(DBHelper.COLUMN_DEVICE_NAME, name);
		try {
			if (writeDB != null) {
				writeDB = dbHelper.getWritableDatabase();
			}
			writeDB.update(DBHelper.TABLE_DEVICE_INFO, values, DBHelper.COLUMN_IDENTIFIER+"=?", new String[]{identifier});
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void insertDeviceInfo(final String identifier, final String name) {
//		new Thread(){
//			@Override
//			public void run() {
				ContentValues values = new ContentValues();
				values.put(DBHelper.COLUMN_IDENTIFIER, identifier);
				values.put(DBHelper.COLUMN_DEVICE_NAME, name);
				if (writeDB == null) {
					writeDB = dbHelper.getWritableDatabase();
				}
				try {
					writeDB.insert(DBHelper.TABLE_DEVICE_INFO, null, values);
				} catch (Exception e) {
					e.printStackTrace();
				}
//			}
//		}.start();
		
	}
}
