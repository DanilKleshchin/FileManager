package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
/**
 * Created by Danil Kleshchin on 11.05.2017.
 */

public class ListAdapter extends BaseAdapter {
    private @NonNull ArrayList<Pair<Integer, String>> objects_ = new ArrayList<>();
    private @NonNull Context context_;
    public ListAdapter(Context context, ArrayList<Pair<Integer, String>> objects) {
        objects_ = objects;
        context_ = context;
    }

    @Override
    public int getCount() {
        return objects_.size();
    }

    @Override
    public Pair<Integer, String> getItem(int i) {
        return objects_.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context_).inflate(R.layout.list_items, viewGroup, false);
        }
        Pair<Integer, String> listItem = getListItem(i);
        Context context = viewGroup.getContext();
        ((TextView) view.findViewById(R.id.fileName)).setText(listItem.second);
        ((ImageView) view.findViewById(R.id.fileImage)).setImageDrawable(context.getResources()
                .getDrawable(listItem.first));
        return view;
    }
    Pair<Integer, String> getListItem(int position) {
        return getItem(position);
    }
}
