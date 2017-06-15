package com.kleshchin.danil.filemanager;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Danil Kleshchin on 13.06.2017.
 */
class SizeManager {
    private static Map<File, Long> files_ = new HashMap<>();
    private OnCountFileSizeListener listener_;

    private SizeManager() {
    }

    private static class SizeManagerHolder {
        @NonNull
        private final static SizeManager instance = new SizeManager();
    }

    void setListener(ListViewFragment listener) {
        this.listener_ = listener;
    }

    @NonNull
    static SizeManager getInstance() {
        return SizeManagerHolder.instance;
    }

    void countSize(File file) {
        if (files_.containsKey(file)) {
            listener_.onCountFileSize(file, files_.get(file));
        }else {
            SizeCounter counter = new SizeCounter();
            counter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
        }
    }

    private class SizeCounter extends AsyncTask<File, Void, Long> {
        private File file_;

        @Override
        protected Long doInBackground(File... params) {
            file_ = params[0];
            return countSize(params[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            files_.put(file_, aLong);
            listener_.onCountFileSize(file_, aLong);
        }

        private Long countSize(File directory) {
            long length = 0;
            if (directory.isFile()) {
                length += directory.length();
            }
            File[] files = directory.listFiles();
            if (files != null) {
                for (File dir : files) {
                    if (dir.isFile())
                        length += dir.length();
                    else
                        length += countSize(dir);
                }
            }
            return length;
        }
    }
}

interface OnCountFileSizeListener {
    void onCountFileSize(File file, Long sizeValue);
}
