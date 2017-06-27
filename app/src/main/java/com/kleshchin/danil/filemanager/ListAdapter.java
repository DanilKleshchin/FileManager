package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
class ListAdapter extends ListAdapterBase {
    private List<File> fileNameArr_ = new ArrayList<>();
    private Map<File, Long> fileSizeArr_ = new HashMap<>();

    ListAdapter(@NonNull File file, Map<File, Long> size) {
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
    public View getView(int i, @Nullable View view, @NonNull ViewGroup viewGroup) {
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
        Long value = fileSizeArr_.get(file);
        fillViewHolder(context, viewHolder, file, value);
        return view;
    }

    void setFileSize(@NonNull File file, @NonNull Long size) {
        fileSizeArr_.put(file, size);
        int position = fileNameArr_.indexOf(file);
        notifyItemChanged(position);
    }

    private static void fillViewHolder(@NonNull Context context, @NonNull ViewHolder holder,
                                       @NonNull File file, @Nullable Long value) {
        holder.fileName.setText(file.getName());
        String size = value == null
                ? null
                : countCorrectValue(context, value, 0);
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
    private static String countCorrectValue(@NonNull Context context, double value, int index) {
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
