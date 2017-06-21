package com.kleshchin.danil.filemanager;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

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
        if (file.list() != null) {
            List<File> files = new ArrayList<>(Arrays.asList(file.listFiles()));
            Collections.sort(files, new FileNameComparator());
            for (File f : files) {
                if (files_.containsKey(f)) {
                    listener_.onCountFileSize(f, files_.get(f));
                } else {
                    try {
                        SizeCounter counter = new SizeCounter();
                        counter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, f);
                    } catch (RejectedExecutionException e) {
                        listener_.onCountFileSize(f, (long) -1);
                    }
                }
            }
        }

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
                return (long) -1;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            files_.put(file_, aLong);
            listener_.onCountFileSize(file_, aLong);
        }

        private boolean isValidDir(File dir) {
            return dir != null && dir.exists() && dir.isDirectory();
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

        long getDirSize(File dir) throws IOException {
            if (dir != null && dir.exists()) {
                if (dir.isFile()) {
                    return dir.length();
                }
            } else {
                return 0L;
            }
            File[] files = dir.listFiles();
            if (files == null) {
                return 0L;
            } else {
                long size = 0L;
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                            if (!isSymlink(file)) {
                                size += getDirSize(file);
                            }
                    }
                }
                return size;
            }
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
}

interface OnCountFileSizeListener {
    void onCountFileSize(File file, Long sizeValue);
}
