package com.kleshchin.danil.filemanager;

import android.support.annotation.Nullable;
import android.widget.BaseAdapter;

/**
 * Created by Danil Kleshchin on 21.06.2017.
 */
abstract class ListAdapterBase extends BaseAdapter{

    @Nullable private OnItemChangedListener listener_;

    void notifyItemChanged(int position) {
        if (listener_ != null) {
            listener_.onItemChanged(position);
        }
    }

    void setListener(@Nullable ListViewBase listener) {
        listener_ = listener;
    }

    interface OnItemChangedListener {
        void onItemChanged(int position);
    }
}
