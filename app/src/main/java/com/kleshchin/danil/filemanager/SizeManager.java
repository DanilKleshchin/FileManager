package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Danil Kleshchin on 13.06.2017.
 */
class SizeManager {

    private static Map<File, Long> files_ = new HashMap<>();
    private OnCountFileSizeListener listener_;
    private static DBHelper dbHelper_;
    private static SQLiteDatabase database_;

    private SizeManager() {
    }

    private static class SizeManagerHolder {
        @NonNull
        private final static SizeManager instance = new SizeManager();
    }

    void setListener(ListViewFragment listener) {
        this.listener_ = listener;
    }

    void openDB(Context context) {
        dbHelper_ = new DBHelper(context);
    }

    @NonNull
    static SizeManager getInstance() {
        return SizeManagerHolder.instance;
    }

    void getWritableDB(File file) {
        DBGetter dbGetter = new DBGetter();
        dbGetter.execute(file);
    }

    private void countSize(File file) {
        if (file.list() != null) {
            List<File> files = new ArrayList<>(Arrays.asList(file.listFiles()));
            Collections.sort(files, new FileNameComparator());
            for (File f : files) {
                if (f.isFile()) {
                    files_.put(f, f.length());
                } else {
                    checkDirectoryInDB(f);
                }
                if (files_.containsKey(f)) {
                    listener_.onCountFileSize(f, files_.get(f));
                } else {
                    try {
                        SizeCounter counter = new SizeCounter();
                        counter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, f);
                    } catch (RejectedExecutionException e) {
                        listener_.onCountFileSize(f, -1L);
                    }
                }
            }
        }
    }

    private void checkDirectoryInDB(File file) {
        database_ = dbHelper_.getWritableDatabase();
        Cursor cursor = database_.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int filePathIndex = cursor.getColumnIndex(DBHelper.KEY_FILE_PATH);
            int fileDateIndex = cursor.getColumnIndex(DBHelper.KEY_FILE_MODIFIED_DATE);
            int fileSizeIndex = cursor.getColumnIndex(DBHelper.KEY_FILE_SIZE);
            do {
                if (cursor.getString(filePathIndex).equals(file.getPath())) {
                    if (file.lastModified() == cursor.getLong(fileDateIndex)) {
                        files_.put(file, cursor.getLong(fileSizeIndex));
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private class SizeCounter extends AsyncTask<File, Void, Long> {
        private File file_;

        @NonNull
        @Override
        protected Long doInBackground(File... params) {
            file_ = params[0];
            try {
                return getDirSize(params[0]);
            } catch (Exception e) {
                return -1L;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            files_.put(file_, aLong);
            if (file_.isDirectory()) {
                dbHelper_.insertIntoDB(aLong, file_, database_);
            }
            listener_.onCountFileSize(file_, aLong);
        }

        boolean isSymlink(File file) throws IOException {
            File canon;
            if (file.getParent() == null) {
                canon = file;
            } else {
                canon = new File(file.getParentFile().getCanonicalFile(),
                        file.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        }

        long getDirSize(File file) throws IOException {
            if (file != null && file.exists()) {
                if (file.isFile()) {
                    return file.length();
                }
            } else {
                return 0L;
            }
            File[] files = file.listFiles();
            if (files == null) {
                return 0L;
            } else {
                long size = 0L;
                for (File f : files) {
                    if (f.isFile()) {
                        size += f.length();
                    } else if (!isSymlink(f)) {
                        size += getDirSize(f);
                    } else {
                        size += getDirSize(f.getAbsoluteFile());
                    }
                }
                return size;
            }
        }
    }

    private class DBGetter extends AsyncTask<File, Void, File> {
        @Override
        protected File doInBackground(File... params) {
            database_ = dbHelper_.getWritableDatabase();
            return params[0];
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            countSize(file);
        }
    }

    private class FileNameComparator implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() == rhs.isDirectory()) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            } else if (lhs.isDirectory()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    static void closeDB() {
        dbHelper_.close();
    }
}

interface OnCountFileSizeListener {
    void onCountFileSize(File file, Long sizeValue);
}
