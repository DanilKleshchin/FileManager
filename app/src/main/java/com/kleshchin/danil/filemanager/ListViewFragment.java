package com.kleshchin.danil.filemanager;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Danil Kleshchin on 19.05.2017.
 */
public class ListViewFragment extends Fragment implements
        SizeManager.OnCountFileSizeListener, DialogInterface.OnClickListener {

    private static final String MAIN_PATH = "/";
    private static final String PATH_KEY = "path";
    @NonNull
    private AppCompatActivity currentActivity_ = (AppCompatActivity) getActivity();
    private ListAdapter listAdapter_;
    @NonNull
    private File file_ = new File(MAIN_PATH);

    @Nullable
    private static ProgressDialog dialog_ = null;
    private Uri uri_;
    private Intent intent_;

    @NonNull
    public static ListViewFragment newInstance(@Nullable String path) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString(PATH_KEY, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        currentActivity_ = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        ListViewBase listView_ = (ListViewBase) view.findViewById(R.id.listView);
        listView_.setEmptyView(view.findViewById(R.id.list_view_empty_state));
        listView_.setOnItemClickListener(new ItemClickListener());
        dialog_ = null;
        Bundle arguments = getArguments();
        if (arguments != null) {
            String path = arguments.getString(PATH_KEY);
            if (path != null) {
                file_ = new File(path);
            }
        }
        SizeManager manager_ = SizeManager.getInstance(currentActivity_);
        manager_.setListener(this);
        manager_.startFileSizeCounting(file_);
        listAdapter_ = new ListAdapter(file_, manager_.getFiles());
        listView_.setAdapter(listAdapter_, listView_);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PATH_KEY, file_.getPath());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog_ != null && !currentActivity_.isFinishing()) {
            dialog_.setTitle(getString(R.string.loading_title));
            dialog_.setCancelable(false);
            dialog_.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialog_ != null) {
            dialog_.dismiss();
            dialog_ = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dialog_ != null) {
            dialog_.dismiss();
            dialog_ = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        OnStopFragmentListener listener = (OnStopFragmentListener) currentActivity_;
        listener.onStopFragment(file_.getPath());
    }

    @Override
    public void onFileSizeCounted(final @NonNull File file, @NonNull Long sizeValue) {
        if (listAdapter_ != null) {
            listAdapter_.setFileSize(file, sizeValue);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog_ = new ProgressDialog(currentActivity_);
        intent_.setDataAndType(uri_, "*/*").addFlags(FLAG_ACTIVITY_NEW_TASK);
        currentActivity_.startActivity(intent_);
    }

    private void callActivityForFile(@NonNull String path, @NonNull final Context context)
            throws ActivityNotFoundException {

        uri_ = Uri.fromFile(new File(path));
        intent_ = new Intent(android.content.Intent.ACTION_VIEW);
        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri_.toString());
        try {
            if (!mimeTypeMap.hasExtension(fileExtension)) {
                UtilDialogHelper.makeDialog(context, ListViewFragment.this).show();
            } else {
                intent_.setDataAndType(uri_, mimeTypeMap.getMimeTypeFromExtension(fileExtension))
                        .addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent_);
            }
        } catch (Exception ignored) {

        }
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            File file = listAdapter_.getItem(i);
            if (file.isDirectory()) {
                OnListItemClickListener listener = (OnListItemClickListener) currentActivity_;
                listener.onListItemClick(file);
                file_ = file;
            } else {
                try {
                    callActivityForFile(file.getPath(), currentActivity_);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(currentActivity_, R.string.activity_not_found,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    interface OnListItemClickListener {
        void onListItemClick(@NonNull File file);
    }

    interface OnStopFragmentListener {
        void onStopFragment(@NonNull String path);
    }
}
