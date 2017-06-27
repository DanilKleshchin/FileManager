package com.kleshchin.danil.filemanager;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Danil Kleshchin on 19.05.2017.
 */
public class ListViewFragment extends Fragment implements
        OnCountFileSizeListener, OnMenuItemClickListener, OnBackPressedListener {

    @NonNull
    private static final String MAIN_PATH = "/";
    @NonNull
    private static final String PATH_KEY = "path";
    @NonNull
    private static File currentFile_ = new File(MAIN_PATH);
    private AppCompatActivity currentActivity_;
    private ListViewBase listView_;
    private ListAdapter listAdapter_;
    private Map<File, Long> fileSizeArr_ = new HashMap<>();
    @Nullable
    private static ProgressDialog dialog_ = null;

    @NonNull
    public static ListViewFragment newInstance(@Nullable String path) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString(PATH_KEY, path);
        fragment.setArguments(args);
        return fragment;
    }

    private static void callActivityForFile(@NonNull String path, @NonNull final Context context)
            throws ActivityNotFoundException {
        final Uri uri = Uri.fromFile(new File(path));
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        try {
            if (!mimeTypeMap.hasExtension(fileExtension)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.attention)
                        .setMessage(R.string.open_all_activities)
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog_ = new ProgressDialog(context);
                                intent.setDataAndType(uri, "*/*").addFlags(FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no_button, null);
                builder.create().show();
            } else {
                intent.setDataAndType(uri, mimeTypeMap.getMimeTypeFromExtension(fileExtension))
                        .addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception ignored) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        currentActivity_ = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        listView_ = (ListViewBase) view.findViewById(R.id.listView);
        listView_.setEmptyView(view.findViewById(R.id.list_view_empty_state));
        listView_.setOnItemClickListener(new ItemClickListener());
        dialog_ = null;
        Bundle arguments = getArguments();
        if (arguments != null) {
            String path = arguments.getString(PATH_KEY);
            if (path != null) {
                File file = new File(path);
                fillListView(file);
                currentFile_ = file;
            } else {
                fillListView(currentFile_);
            }
        }
        SizeManager manager = SizeManager.getInstance();
        manager.openDB(currentActivity_);
        manager.setListener(this);
        manager.getWritableDB(currentFile_);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PATH_KEY, currentFile_.getPath());
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
        OnSaveCurrentFile listener = (OnSaveCurrentFile) currentActivity_;
        listener.onSaveCurrent(currentFile_.getPath());
    }

    @Override
    public void onFileSizeCounted(final @NonNull File file, @NonNull Long sizeValue) {
        fileSizeArr_.put(file, sizeValue);
        try {
            listAdapter_.setFileSize(file, sizeValue);
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void onMenuItemClick(@NonNull Context context) {
        OnToolbarTextChangeListener listener = (OnToolbarTextChangeListener) context;
        String parent = currentFile_.getParent();
        File parentFile = currentFile_.getParentFile();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (parent.equals(MAIN_PATH)) {
                listener.onToolbarTextChange(getResources().getString(R.string.root_directory),
                        parentFile);
            } else {
                listener.onToolbarTextChange(parent, parentFile);
            }
        }
        if (!currentFile_.getPath().equals(MAIN_PATH)) {
            currentFile_ = parentFile;
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fillListView(currentFile_);
        }
    }

    @Override
    public void onBackPressed() {
        currentFile_ = currentFile_.getParentFile();
        OnToolbarTextChangeListener listener = (OnToolbarTextChangeListener) currentActivity_;
        listener.onToolbarTextChange(currentFile_.getPath(), currentFile_);
    }

    private void fillListView(@NonNull File file) {
        if (fileSizeArr_.isEmpty()) {
            File list[] = file.listFiles();
            if (list != null) {
                for (File aList : list) {
                    fileSizeArr_.put(aList, null);
                }
            }
        }
        listAdapter_ = new ListAdapter(file, fileSizeArr_);
        listView_.setAdapter(listAdapter_);
        String path = file.getPath();
        OnToolbarTextChangeListener listener = (OnToolbarTextChangeListener) currentActivity_;
        listener.onToolbarTextChange((path.equals(MAIN_PATH))
                ? getResources().getString(R.string.root_directory)
                : path, file);
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            File file = listAdapter_.getItem(i);
            if (file.isDirectory()) {
                OnListItemClickListener listener = (OnListItemClickListener) currentActivity_;
                listener.onListItemClick(file);
                currentFile_ = file;
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

    interface OnToolbarTextChangeListener {
        void onToolbarTextChange(String toolbarText, File file);
    }

    interface OnListItemClickListener {
        void onListItemClick(File file);
    }

    interface OnSaveCurrentFile {
        void onSaveCurrent(String path);
    }
}
