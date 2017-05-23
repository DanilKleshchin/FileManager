package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Danil Kleshchin on 11.05.2017.
 */
class ListAdapter extends BaseAdapter {
    @NonNull
    private ArrayList<File> file_;
    @NonNull
    private Context context_;

    ListAdapter(@NonNull Context context, File file) {
        context_ = context;
        try {
            file_ = new ArrayList<>(Arrays.asList(file.listFiles()));
            fillList(file_);
        } catch (NullPointerException e) {
            file_ = new ArrayList<>();
        }
    }

    private final static class ViewHolder {
        TextView fileName;
        ImageView fileImage;
    }

    @Override
    public int getCount() {
        return file_.size();
    }

    @Override
    public File getItem(int i) {
        return file_.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context_).inflate(R.layout.list_items, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.fileName = (TextView) view.findViewById(R.id.fileName);
            viewHolder.fileImage = (ImageView) view.findViewById(R.id.fileImage);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        File file = getItem(i);
        viewHolder.fileName.setText(file.getName());
        if (file.isDirectory()) {
            viewHolder.fileImage.setImageDrawable(ContextCompat.getDrawable(context_, R.drawable.folder));
        } else {
            viewHolder.fileImage.setImageDrawable(ContextCompat.getDrawable(context_, R.drawable.file));
        }
        return view;
    }

    private void fillList(ArrayList<File> file) {
        Collections.sort(file, new FileNameComparator());
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
