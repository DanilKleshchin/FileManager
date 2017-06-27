package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Danil Kleshchin on 26.06.2017.
 */
public class ListViewBase extends ListView implements ListAdapterBase.OnItemChangedListener {

    public ListViewBase(Context context) {
        super(context);
    }

    public ListViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAdapter(ListAdapterBase adapter) {
        adapter.setListener(this);
        super.setAdapter(adapter);
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
