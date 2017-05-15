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
public class ListAdapter extends BaseAdapter {

    @NonNull
    private ArrayList<File> file_;
    @NonNull
    private Context context_;

    public ListAdapter(@NonNull Context context, File file) {
        context_ = context;
        file_ = new ArrayList<>(Arrays.asList(file.listFiles()));
        fillList(file_);
    }

    private static class ViewHolder {
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
        File file = getListItem(i);
        Context context = viewGroup.getContext();
        viewHolder.fileName.setText(file.getName());
        if (file.isDirectory()) {
            viewHolder.fileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.folder));
        } else {
            viewHolder.fileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.file));
        }
        return view;
    }

    private File getListItem(int position) {
        return getItem(position);
    }

    private void fillList(ArrayList<File> file) {
        Collections.sort(file, new SortFileName());
        Collections.sort(file, new SortFolder());
    }

    private class SortFileName implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

    private class SortFolder implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            if (f1.isDirectory() == f2.isDirectory())
                return 0;
            else if (f1.isDirectory() && !f2.isDirectory())
                return -1;
            else
                return 1;
        }
    }
}
