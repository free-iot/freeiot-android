package com.pandocloud.freeiot.ui.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	
	public static final String DB_NAME = "smart_plug";
	
	public static final String TABLE_DEVICE_INFO = "device_info";
	
	public static final String COLUMN_ID = "_id";
	
	public static final String COLUMN_IDENTIFIER = "identifier";
	
	public static final String COLUMN_DEVICE_NAME = "device_name";
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	
	 private static final String DATABASE_CREATE = "create table if not exists "
		      + TABLE_DEVICE_INFO + "(" + COLUMN_ID
		      + " integer primary key autoincrement, " + COLUMN_IDENTIFIER
		      + " text not null, " + COLUMN_DEVICE_NAME 
		      + " text not null);";

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE_INFO);
		onCreate(db);
	}

}
