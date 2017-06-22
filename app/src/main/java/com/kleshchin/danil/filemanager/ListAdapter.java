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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Danil Kleshchin on 11.05.2017.
 */
//class ListAdapter extends ListBaseAdapter implements android.widget.ListAdapter {
class ListAdapter extends ListBaseAdapter {
    private List<File> fileNameArr_ = new ArrayList<>();
    private Map<File, Long> fileSizeArr_ = new HashMap<>();

    ListAdapter(File file, Map<File, Long> size) {
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
    @NonNull
    public View getView(int i, View view, ViewGroup viewGroup) {
        File file = getItem(i);

        Context context = viewGroup.getContext();
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list_view, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        fillViewHolder(context, viewHolder, file);

        return view;
    }

    /*int getPositionByFile(File file) {
        return fileNameArr_.indexOf(file);
    }*/

    void setFileSize(@NonNull Context context, View view, Long size, File file) {
        fileSizeArr_.put(file, size);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        fillViewHolder(context, viewHolder, file);
    }

    private void fillViewHolder(Context context, @NonNull ViewHolder holder, File file) {
        holder.fileName.setText(file.getName());

        Long val = fileSizeArr_.get(file);
        String size = val == null
                ? null
                : countCorrectValue(context, val, 0);
        if (size == null) {
            holder.progressBar.setVisibility(ProgressBar.VISIBLE);
            holder.fileSize.setText(R.string.place_holder_for_counting);
        } else {
            holder.progressBar.setVisibility(ProgressBar.INVISIBLE);
            holder.fileSize.setText(size);
        }

        boolean directory = file.isDirectory();
        holder.fileImage.setImageDrawable(directory
                ? (ContextCompat.getDrawable(context, R.mipmap.folder_image))
                : (ContextCompat.getDrawable(context, R.mipmap.file_image)));
        holder.fileImage.setContentDescription(directory
                ? (context.getString(R.string.directory_desc))
                : (context.getString(R.string.file_desc)));
    }

    @NonNull
    private String countCorrectValue(@NonNull Context context, double value, int index) {
        if (value < 0) {
            return context.getString(R.string.cannot_count);
        }
        String units[] = {"B", "kB", "MB", "GB"};
        double boundaryValue = 1024.0;
        if (value > boundaryValue) {
            if (index <= units.length) {
                return countCorrectValue(context, value / boundaryValue, ++index);
            }
        }
        return String.format(Locale.getDefault(), "%.2f", value) + " " + units[index];
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public boolean hasStableIds() {
        return false;
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
