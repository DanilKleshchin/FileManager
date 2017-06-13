package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.support.annotation.NonNull;
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

/**
 * Created by Danil Kleshchin on 11.05.2017.
 */
class ListAdapter extends BaseAdapter implements OnUpdateListViewListener{
    @NonNull
    private ArrayList<File> file_;
    private OnGetViewListener listener_;
    private ArrayList<String> sizeValueArray_;
    private ViewHolder viewHolder_;

    ListAdapter(File file, OnGetViewListener listener, ArrayList<String> size) {
        listener_ = listener;
        sizeValueArray_ = size;
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
        File file = getItem(i);
        Context context_ = viewGroup.getContext();
        if (view == null) {
            view = LayoutInflater.from(context_).inflate(R.layout.item_list_view, viewGroup, false);
            viewHolder_ = new ViewHolder(view);
            view.setTag(viewHolder_);
        } else {
            viewHolder_ = (ViewHolder) view.getTag();
        }
        viewHolder_.fileName.setText(file.getName());
        viewHolder_.fileSize.setText(sizeValueArray_.get(i));
        viewHolder_.progressBar.setVisibility(ProgressBar.VISIBLE);
        boolean directory = file.isDirectory();
        viewHolder_.fileImage.setImageDrawable(directory
                ? (ContextCompat.getDrawable(context_, R.mipmap.folder_image))
                : (ContextCompat.getDrawable(context_, R.mipmap.file_image)));
        viewHolder_.fileImage.setContentDescription(directory
                ? (context_.getString(R.string.directory_desc))
                : (context_.getString(R.string.file_desc)));
        listener_.onGetView(file, i);
        return view;
    }

    @Override
    public void onUpdateListView(View view, int i) {
        if (view != null) {
            ((TextView) view.findViewById(R.id.file_size)).setText(sizeValueArray_.get(i));
            view.findViewById(R.id.progress_bar).setVisibility(ProgressBar.INVISIBLE);
        }
        viewHolder_.progressBar.setVisibility(ProgressBar.INVISIBLE);
        viewHolder_.fileSize.setText(sizeValueArray_.get(i));
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

    interface OnGetViewListener {
        void onGetView(File file, int i);
    }
}
