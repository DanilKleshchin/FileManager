package com.kleshchin.danil.filemanager;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;

/**
 * Created by Danil Kleshchin on 21.06.2017.
 */
abstract class ListBaseAdapter extends BaseAdapter{

    private final ListDataSetObservable mDataSetObservable = new ListDataSetObservable();

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    void notifyItemChanged(int position) {
        mDataSetObservable.notifyItemChanged(position);
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }
}
