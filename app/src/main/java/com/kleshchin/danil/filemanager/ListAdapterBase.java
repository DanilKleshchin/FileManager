package com.kleshchin.danil.filemanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danil Kleshchin on 21.06.2017.
 */
abstract class ListAdapterBase extends BaseAdapter {

    @NonNull
    private List<OnItemChangedListener> listener_ = new ArrayList<>();

    void notifyItemChanged(int position) {
        for (OnItemChangedListener l : listener_) {
            l.onItemChanged(position);
        }
    }

    void setListener(@Nullable ListViewBase listener) {
        listener_.add(listener);
    }

    void removeListener(@Nullable ListViewBase listener) {
        listener_.remove(listener);
    }

    interface OnItemChangedListener {
        void onItemChanged(int position);
    }
}
