package com.kleshchin.danil.filemanager;

import android.os.AsyncTask;

import java.io.File;

/**
 * Created by Danil Kleshchin on 07.06.2017.
 */

class SizeCounter extends AsyncTask<File, Void, Double> {
    private int i_ = 0;

    void setI(int i) {
        this.i_ = i;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Double doInBackground(File... params) {
        return countSize(params[0]);
    }

    @Override
    protected void onPostExecute(Double aDouble) {
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