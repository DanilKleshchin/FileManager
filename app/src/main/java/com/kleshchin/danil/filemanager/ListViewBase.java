package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Danil Kleshchin on 26.06.2017.
 */
public class ListViewBase extends ListView implements ListAdapterBase.OnItemChangedListener {

    @Nullable
    private ListAdapterBase adapter_ = null;

    public ListViewBase(@NonNull Context context) {
        super(context);
    }

    public ListViewBase(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewBase(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAdapter(@Nullable ListAdapterBase adapter, @NonNull ListViewBase listView) {
        if(adapter_ != null) {
            adapter_.removeListener(this);
        }
        if (adapter != null) {
            adapter.setListener(this);
            super.setAdapter(adapter);
        }
    }

    @Override
    public void onItemChanged(int position) {
        int index = position - this.getFirstVisiblePosition();
        View view = this.getChildAt(index);
        if (view != null) {
            getAdapter().getView(position, view, this);
        }
    }
}
