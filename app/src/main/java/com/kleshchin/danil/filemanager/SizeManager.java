package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    private Map<File, Long> files_ = new HashMap<>();

    @Nullable
    private OnCountFileSizeListener listener_;
    private static DBHelper dbHelper_;
    private static SQLiteDatabase database_;

    private SizeManager() {
    }

    private static class SizeManagerHolder {

        @NonNull
        private final static SizeManager instance = new SizeManager();
    }

    void setListener(@Nullable ListViewFragment listener) {
        listener_ = listener;
    }

    @NonNull
    static SizeManager getInstance(@NonNull Context context) {
        dbHelper_ = new DBHelper(context);
        return SizeManagerHolder.instance;
    }

    Map<File, Long> getFiles() {
        return files_;
    }

    /***
     *Start getting a writable database.
     * @param file - a file whose size is need counted.
     */
    void startFileSizeCounting(@NonNull File file) {
        if (files_.isEmpty()) {
            File list[] = file.listFiles();
            if (list != null) {
                for (File aList : list) {
                    files_.put(aList, null);
                }
            }
        }
        DBGetter dbGetter = new DBGetter();
        dbGetter.execute(file);
    }

    private void countSize(@NonNull File file) {
        if (listener_ == null) {
            return;
        }

        if (file.list() != null) {
            List<File> files = new ArrayList<>(Arrays.asList(file.listFiles()));
            Collections.sort(files, new FileNameComparator());
            for (File f : files) {
                if (f.isFile()) {
                    files_.put(f, f.length());
                } else {
                    checkDirectoryInDB(f);
                }
                if (files_.containsKey(f) && files_.get(f) != null) {
                    listener_.onFileSizeCounted(f, files_.get(f));
                } else {
                    try {
                        SizeCounter counter = new SizeCounter();
                        counter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, f);
                    } catch (RejectedExecutionException e) {
                        listener_.onFileSizeCounted(f, -1L);
                    }
                }
            }
        }
    }

    private void checkDirectoryInDB(@NonNull File file) {
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
                        break;
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
        protected Long doInBackground(@NonNull File... params) {
            file_ = params[0];
            Long result;
            try {
                result = getDirectorySize(params[0]);
            } catch (Exception e) {
                result = -1L;
            }
            DBHelper.insertValuesIntoDatabase(result, file_, database_);
            files_.put(file_, result);
            return result;
        }

        @Override
        protected void onPostExecute(@NonNull Long size) {
            if (listener_ != null) {
                listener_.onFileSizeCounted(file_, size);
            }
        }

        long getDirectorySize(@Nullable File file) throws IOException {
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
                        size += getDirectorySize(f);
                    } else {
                        size += getDirectorySize(f.getAbsoluteFile());
                    }
                }
                return size;
            }
        }

        boolean isSymlink(@NonNull File file) throws IOException {
            File canon;
            if (file.getParent() == null) {
                canon = file;
            } else {
                canon = new File(file.getParentFile().getCanonicalFile(),
                        file.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        }
    }

    /**
     * Call countSize() method when database is configured.
     * @param database - database.
     * @param file - file which need to count size.
     */
    private void onDatabaseOpened(@NonNull SQLiteDatabase database, @NonNull File file) {
        database_ = database;
        countSize(file);
    }

    /**
     * Close database.
     */
    static void releaseSpace() {
        if(dbHelper_ != null) {
            dbHelper_.close();
        }
        if(database_ != null) {
            database_.close();
        }
    }

    private class DBGetter extends AsyncTask<File, Void, SQLiteDatabase> {
        private File file_;

        @Override
        protected SQLiteDatabase doInBackground(File... params) {
            file_ = params[0];
            return dbHelper_.getWritableDatabase();
        }

        @Override
        protected void onPostExecute(@NonNull SQLiteDatabase database) {
            onDatabaseOpened(database, file_);
        }
    }

    private class FileNameComparator implements Comparator<File> {
        @Override
        public int compare(@NonNull File lhs, @NonNull File rhs) {
            if (lhs.isDirectory() == rhs.isDirectory()) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            } else if (lhs.isDirectory()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    interface OnCountFileSizeListener {
        void onFileSizeCounted(@NonNull File file, @NonNull Long sizeValue);
    }
}
