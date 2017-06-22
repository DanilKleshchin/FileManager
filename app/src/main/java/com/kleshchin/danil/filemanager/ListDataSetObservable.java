package com.kleshchin.danil.filemanager;

import android.database.DataSetObserver;
import android.database.Observable;

/**
 * Created by Danil Kleshchin on 22.06.2017.
 */
class ListDataSetObservable extends Observable<DataSetObserver> {

    void notifyChanged() {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }
    }

    void notifyInvalidated() {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onInvalidated();
            }
        }
    }

    void notifyItemChanged(int position) {
        synchronized (mObservers) {
            mObservers.get(position).onChanged();
        }
    }
}
