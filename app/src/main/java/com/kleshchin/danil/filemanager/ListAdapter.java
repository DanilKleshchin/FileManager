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

    ListAdapter(File file) {
        try {
            file_ = new ArrayList<>(Arrays.asList(file.listFiles()));
            Collections.sort(file_, new FileNameComparator());
        } catch (NullPointerException e) {
            file_ = new ArrayList<>();
        }
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
        Context context = viewGroup.getContext();
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list_view, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        File file = getItem(i);
        viewHolder.fileName.setText(file.getName());
        boolean directory = file.isDirectory();
        viewHolder.fileImage.setImageDrawable(directory
                ? (ContextCompat.getDrawable(context, R.mipmap.folder_image))
                : (ContextCompat.getDrawable(context, R.mipmap.file_image)));
        viewHolder.fileImage.setContentDescription(directory
                ? (context.getString(R.string.directory_desc))
                : (context.getString(R.string.file_desc)));
        return view;
    }

    private final static class ViewHolder {
        TextView fileName;
        ImageView fileImage;
        ViewHolder(View view){
            fileName = (TextView) view.findViewById(R.id.fileName);
            fileImage = (ImageView) view.findViewById(R.id.fileImage);
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
