package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Danil Kleshchin on 11.05.2017.
 */
class ListAdapter extends BaseAdapter {
    private Map<File, String> file_ = new LinkedHashMap<>();
    //    private List<File> files_ = new ArrayList<>();
    private static final String placeHolderForCounting = "Counting...";

    ListAdapter(File file) {
        if(file_.isEmpty()) {
            try {
                List<File> files_ = new ArrayList<>(Arrays.asList(file.listFiles()));
                Collections.sort(files_, new FileNameComparator());
                for (int i = 0; i < files_.size(); i++) {
                    file_.put(files_.get(i), placeHolderForCounting);
                }
            } catch (NullPointerException ignored) {
            }
        }
    }

    @Override
    public int getCount() {
        return file_.size();
    }

    @Override
    @NonNull
    public File getItem(int i) {
        return file_.;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        File file = getItem(i);
        String size = file_.get(file);
        Context context_ = viewGroup.getContext();
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context_).inflate(R.layout.item_list_view, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.fileName.setText(file.getName());

        if (size.equals(placeHolderForCounting)) {
            viewHolder.progressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            viewHolder.progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
        viewHolder.fileSize.setText(size);
        boolean directory = file.isDirectory();
        viewHolder.fileImage.setImageDrawable(directory
                ? (ContextCompat.getDrawable(context_, R.mipmap.folder_image))
                : (ContextCompat.getDrawable(context_, R.mipmap.file_image)));
        viewHolder.fileImage.setContentDescription(directory
                ? (context_.getString(R.string.directory_desc))
                : (context_.getString(R.string.file_desc)));
        return view;
    }

    /*@Override
    public void onUpdateListView(File file, String size) {
        file_.put(file, size);
        this.notifyDataSetChanged();
        *//*((TextView) view.findViewById(R.id.file_size)).setText(sizeValueArray_.get(i).second);
        view.findViewById(R.id.progress_bar).setVisibility(ProgressBar.INVISIBLE);*//*
    }*/

    void setFileSize(File file, String size) {
        file_.put(file, size);
        this.notifyDataSetChanged();
    }

    private final static class ViewHolder {
        TextView fileName;
        TextView fileSize;
        ImageView fileImage;
        ProgressBar progressBar;

        ViewHolder(View view) {
            fileName = (EditText) view.findViewById(R.id.file_name);
            fileImage = (ImageView) view.findViewById(R.id.file_image);
            fileSize = (TextView) view.findViewById(R.id.file_size);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
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
