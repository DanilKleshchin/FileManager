package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Danil Kleshchin on 11.05.2017.
 */
class ListAdapter extends BaseAdapter {
    private List<File> fileNameArr_ = new ArrayList<>();
    private Map<File, String> fileSizeArr_ = new HashMap<>();
    private static final String placeHolderForCounting = "Counting...";

    ListAdapter(File file, Map<File, String> size) {
        if (file.list() != null) {
            fileNameArr_ = new ArrayList<>(Arrays.asList(file.listFiles()));
            Collections.sort(fileNameArr_, new FileNameComparator());
            fileSizeArr_ = size;
        }
    }

    @Override
    public int getCount() {
        return fileNameArr_.size();
    }

    @Override
    @NonNull
    public File getItem(int i) {
        return fileNameArr_.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        File file = getItem(i);
        String size = fileSizeArr_.get(file);
        if(size == null) {
            fileSizeArr_.put(file, placeHolderForCounting);
            size = placeHolderForCounting;
        }
        Context context = viewGroup.getContext();
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list_view, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        fillViewHolder(context, viewHolder, file, size);
        return view;
    }

    int getPositionByFile(File file) {
        return fileNameArr_.indexOf(file);
    }

    void setFileSize(View view, String size) {
        ((TextView) view.findViewById(R.id.file_size)).setText(size);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.fileSize.setText(size);
        viewHolder.progressBar.setVisibility(View.INVISIBLE);
    }

    private static void fillViewHolder(Context context, @NonNull ViewHolder holder, File file, String size) {
        holder.fileName.setText(file.getName());

        if (size.equals(placeHolderForCounting)) {
            holder.progressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            holder.progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
        holder.fileSize.setText(size);
        boolean directory = file.isDirectory();
        holder.fileImage.setImageDrawable(directory
                ? (ContextCompat.getDrawable(context, R.mipmap.folder_image))
                : (ContextCompat.getDrawable(context, R.mipmap.file_image)));
        holder.fileImage.setContentDescription(directory
                ? (context.getString(R.string.directory_desc))
                : (context.getString(R.string.file_desc)));
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
