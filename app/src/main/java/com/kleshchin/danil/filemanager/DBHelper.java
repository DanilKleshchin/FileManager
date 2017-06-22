package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Danil Kleshchin on 22.06.2017.
 */

public class DBHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "File_Manager";
    public static final String TABLE_NAME = "";
    public static final String KEY_ID = "_id";
    public static final String KEY_FILE_PATH = "File_path";
    public static final String KEY_FILE_SIZE = "File_size";
    public static final String KEY_FILE_MODIFIED_DATE = "File_modified_date";
    public static final String KEY_FILE_IMAGE = "File_image";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE" + TABLE_NAME + " (" +
            KEY_ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
            KEY_FILE_PATH + TEXT_TYPE + COMMA_SEP +
            KEY_FILE_SIZE + TEXT_TYPE + COMMA_SEP +
            KEY_FILE_MODIFIED_DATE + TEXT_TYPE + COMMA_SEP +
            KEY_FILE_IMAGE + TEXT_TYPE + COMMA_SEP + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
