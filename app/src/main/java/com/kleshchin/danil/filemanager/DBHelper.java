package com.kleshchin.danil.filemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by Danil Kleshchin on 22.06.2017.
 */

class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "File_Manager";
    static final String TABLE_NAME = "File_data";
    private static final String KEY_ID = "_id";
    static final String KEY_FILE_PATH = "File_path";
    static final String KEY_FILE_SIZE = "File_size";
    static final String KEY_FILE_MODIFIED_DATE = "File_modified_date";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String FILE_PATH_INDEX = "file_path_index";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    KEY_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    KEY_FILE_SIZE + INT_TYPE + COMMA_SEP +
                    KEY_FILE_MODIFIED_DATE + INT_TYPE + COMMA_SEP +
                    "UNIQUE" + "(" + KEY_FILE_PATH + ")" + "ON CONFLICT REPLACE" + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String SQL_CREATE_INDEX =
            "CREATE INDEX " + FILE_PATH_INDEX + " ON " + TABLE_NAME + "(" + KEY_FILE_PATH + ")";

    DBHelper(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    void insertIntoDB(@NonNull Long size, @NonNull File file, @NonNull SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_FILE_SIZE, size);
        contentValues.put(DBHelper.KEY_FILE_MODIFIED_DATE, file.lastModified());
        contentValues.put(DBHelper.KEY_FILE_PATH, file.getPath());
        database.insert(DBHelper.TABLE_NAME, null, contentValues);
    }
}
