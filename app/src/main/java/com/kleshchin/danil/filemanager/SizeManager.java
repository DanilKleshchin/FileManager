package com.kleshchin.danil.filemanager;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Danil Kleshchin on 13.06.2017.
 */
class SizeManager implements ListAdapter.OnGetViewListener {
    private static HashMap<String, String> files_ = new HashMap<>();
    private SizeCounter counter;
    private OnCountFileSizeListener listener_;
    private SizeManager() {
    }

    @Override
    public void onGetView(File file, int i) {
        counter = new SizeCounter();
        counter.setPosition(i);
        counter.execute(file);
    }

    void setListener(ListViewFragment fragment) {
        this.listener_ = fragment;
    }

    private static class SizeManagerHolder {
        @NonNull
        private final static SizeManager instance = new SizeManager();
    }

    @NonNull
    static SizeManager getInstance() {
        return SizeManagerHolder.instance;
    }

    void setSize(String file, int position) {
        if (files_.containsKey(file)) {
            listener_.onCountFileSize(position, files_.get(file));
        }else {
            counter = new SizeCounter();
            counter.setPosition(position);
            counter.execute(new File(file));
        }
    }

    private class SizeCounter extends AsyncTask<File, Void, Double> {
        private int position_ = 0;
        private File file_;

        void setPosition(int position) {
            this.position_ = position;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Double doInBackground(File... params) {
            file_ = params[0];
            return countSize(params[0]);
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            if (!isCancelled()) {
                String value = countCorrectValue(aDouble, 0);
                files_.put(file_.getPath(), value);
                listener_.onCountFileSize(position_, value);
            }
        }

        private double countSize(File directory) {
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

    private String countCorrectValue(@NonNull Double value, int index) {
        String units[] = {"B", "kB", "MB", "GB"};
        double boundaryValue = 1024.0;
        if (value > boundaryValue) {
            if (index <= units.length) {
                return countCorrectValue(value / boundaryValue, ++index);
            }
        }
        return String.format(Locale.getDefault(), "%.2f", value) + " " + units[index];
    }
}

interface OnCountFileSizeListener {
    void onCountFileSize(int position, String sizeValue);
}
