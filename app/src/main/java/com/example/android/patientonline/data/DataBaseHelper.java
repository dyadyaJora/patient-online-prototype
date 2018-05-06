package com.example.android.patientonline.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBaseHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 3;
    public static final String DB_NAME = "patient_online";
    public static final String TABLE_DEVICES = "devices";

    public static final String COL_NAME = "name";
    public static final String COL_BT_NAME = "bt_name";
    public static final String COL_MAC = "mac";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TYPE = "type";
    public static final String COL_FORMAT = "format";
    public static final String COL_LAST_VAL = "last_value";
    public static final String COL_LAST_UP = "last_update";

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_DEVICES +" ("
                + "id integer primary key autoincrement,"
                + COL_NAME + " text,"
                + COL_BT_NAME + " text,"
                + COL_MAC + " text,"
                + COL_DESCRIPTION + " text,"
                + COL_TYPE + " text,"
                + COL_FORMAT + " text,"
                + COL_LAST_VAL + " text,"
                + COL_LAST_UP + " datetime default CURRENT_TIMESTAMP"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + TABLE_DEVICES);

        onCreate(db);
    }
}
