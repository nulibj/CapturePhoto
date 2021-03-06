package com.client.capturephoto.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.client.capturephoto.adapter.ImageInfo;
import com.client.capturephoto.util.Constant;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
	private static final String TAG = "MySQLiteOpenHelper";

	public MySQLiteOpenHelper(final Context context) {
		super(context, Constant.DB_NAME, null, Constant.VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createPictureTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/**
	 * 超时时间记录表
	 */
	private void createPictureTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + Constant.TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY autoincrement, "
				+ Constant.Column.TIME + " TEXT, " + Constant.Column.FULL_NAME
				+ " TEXT, " + Constant.Column.IMAGE_CODE + " TEXT, "
				+ Constant.Column.NOT_USED + " TEXT,"
				+ Constant.Column.NOT_USED_1 + " TEXT, "
				+ Constant.Column.NOT_USED_2 + " TEXT)";

		db.execSQL(sql);
	}

	public static void insert(MySQLiteOpenHelper helper, ImageInfo imageInfo) {
		Cursor c = query(helper, new String[] { Constant.Column.FULL_NAME,
				Constant.Column.IMAGE_CODE },
				new String[] { imageInfo.fullName });
		if (c == null || c.getCount() <= 0) {
			if (c != null)
				c.close();
			ContentValues values = new ContentValues(3);
			values.put(Constant.Column.TIME, Constant.simpleDateFormat
					.format(System.currentTimeMillis()));
			values.put(Constant.Column.FULL_NAME, imageInfo.fullName);
			values.put(Constant.Column.IMAGE_CODE, imageInfo.imageCode);
			helper.getWritableDatabase().insert(Constant.TABLE_NAME, null,
					values);
			if (Constant.DEBUG) {
				Log.e(TAG, "==================插入成功 >>>" + imageInfo.fullName);
			}
			return;
		}
		c.close();
	}

	public static void delete(MySQLiteOpenHelper helper, ImageInfo imageInfo) {
		helper.getWritableDatabase().delete(Constant.TABLE_NAME,
				Constant.Column.FULL_NAME + " = ? ",
				new String[] { imageInfo.fullName });
		if (Constant.DEBUG) {
			Log.e(TAG, "==================删除成功 >>>" + imageInfo.fullName);
		}
	}

	public static Cursor query(MySQLiteOpenHelper helper, String[] columns,
			String[] selectionArgs) {
		Cursor c = helper.getReadableDatabase().query(Constant.TABLE_NAME,
				columns, Constant.Column.FULL_NAME + " = ? ", selectionArgs,
				null, null, null);
		if (Constant.DEBUG) {
			Log.e(TAG, "==================查询成功 >>> cursor " + c.getCount());
		}
		return c;
	}

	public static ArrayList<ImageInfo> queryAll(MySQLiteOpenHelper helper) {
		Cursor c = helper.getReadableDatabase().query(
				Constant.TABLE_NAME,
				new String[] { Constant.Column.TIME, Constant.Column.FULL_NAME,
						Constant.Column.IMAGE_CODE }, null, null, null, null,
				null);
		if (Constant.DEBUG) {
			Log.e(TAG, "==================查询成功 >>> cursor " + c.getCount());
		}

		try {
			if (c == null || c.getCount() <= 0) {
				return null;
			}
			ImageInfo imageInfo = null;
			int fullNameIndex = c.getColumnIndex(Constant.Column.FULL_NAME);
			int imageCodeIndex = c.getColumnIndex(Constant.Column.IMAGE_CODE);
			ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
			while (c.moveToNext()) {
				imageInfo = new ImageInfo();
				imageInfo.fullName = c.getString(fullNameIndex);
				imageInfo.imageCode = c.getString(imageCodeIndex);
				imageInfos.add(imageInfo);
			}
			return imageInfos;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	public static void insert(MySQLiteOpenHelper helper,
			ArrayList<ImageInfo> imageInfos) {
		// String[] columns = { Constant.Column.FULL_NAME,
		// Constant.Column.IMAGE_CODE };
		// String[] selectionArgs = null;
		for (ImageInfo imageInfo : imageInfos) {
			// selectionArgs = new String[] { imageInfo.fullName };
			insert(helper, imageInfo);
			// Cursor c = query(helper, columns, selectionArgs);
			//
			// if (c == null || c.getCount() <= 0)
			// {
			// if (c != null)
			// c.close();
			// }
			// else
			// {
			// c.close();
			// }
		}

		// ContentValues values = new ContentValues();
		// values.put(Constant.Column.TIME,
		// Constant.simpleDateFormat.format(System.currentTimeMillis()));
		// values.put(Constant.Column.FULL_NAME, imageInfo.fullName);
		// values.put(Constant.Column.IMAGE_CODE, imageInfo.imageCode);
		// helper.getWritableDatabase().insert(Constant.TABLE_NAME, null,
		// values);
		// if (Constant.DEBUG)
		// {
		// Log.e(TAG, "==================插入成功 >>>" + imageInfo.fullName);
		// }
	}
}
