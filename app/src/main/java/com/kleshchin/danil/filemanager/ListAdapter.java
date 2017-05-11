package com.kleshchin.danil.filemanager;

import android.content.Context;
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
    ArrayList<ListItems> _objects = new ArrayList<ListItems>();
    Context _context;
    LayoutInflater _inflater;

    public ListAdapter(Context context, ArrayList<ListItems> objects) {
        _context = context;
        _objects = objects;
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _objects.size();
    }

    @Override
    public Object getItem(int i) {
        return _objects.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = _inflater.inflate(R.layout.list_items, viewGroup, false);
        }
        ListItems listItem = getListItem(i);
        Context context = viewGroup.getContext();
        ((TextView) view.findViewById(R.id.fileName)).setText(listItem.fileName);
        ((ImageView) view.findViewById(R.id.fileImage)).setImageDrawable(context.getResources().getDrawable(listItem.imageName));

        return view;
    }

    ListItems getListItem(int position) {
        return ((ListItems) getItem(position));
    }

}
