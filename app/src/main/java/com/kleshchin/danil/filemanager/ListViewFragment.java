package com.kleshchin.danil.filemanager;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Danil Kleshchin on 19.05.2017.
 */
public class ListViewFragment extends Fragment implements OnBackPressedListener,
        OnCountFileSizeListener, ListAdapter.OnGetViewListener {
    private static final String MAIN_PATH = Environment.getExternalStorageDirectory().getParent();
    private static final String PATH_KEY = "path";
    private static final String LAST_FILE_PATH = "LAST_FILE_PATH";
    private static File currentFile_ = new File(MAIN_PATH);
    private AppCompatActivity currentActivity_;
    private ListView listView_;
    private ListAdapter listAdapter_;
    private ArrayList<String> sizeValueArray_ = new ArrayList<>();
    @Nullable
    private static ProgressDialog dialog_ = null;
    private static SizeManager manager_;


    @NonNull
    public static ListViewFragment newInstance(@NonNull String path) {
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
        manager_ = SizeManager.getInstance();
        manager_.setListener(this);
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        currentActivity_ = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        listView_ = (ListView) view.findViewById(R.id.listView);
        listView_.setEmptyView(view.findViewById(R.id.list_view_empty_state));
        listView_.setOnItemClickListener(new ItemClickListener());
        dialog_ = null;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String path = arguments.getString(PATH_KEY);
            if (path != null) {
                fillListView(new File(path));
            }
        } else {
            SharedPreferences preferences = currentActivity_.getPreferences(Context.MODE_PRIVATE);
            String lastPath = preferences.getString(LAST_FILE_PATH, "");
            if (!lastPath.equals("")) {
                currentFile_ = new File(lastPath);
            }
            ArrayList<File> arr = new ArrayList<>();
            File temp = currentFile_;
            while (!temp.getPath().equals(MAIN_PATH)) {
                arr.add(temp);
                temp = temp.getParentFile();
            }
            arr.add(new File(MAIN_PATH));
            OnPopBackStackListener popBackStackListener = (OnPopBackStackListener) currentActivity_;
            popBackStackListener.onPopBackStack(1);
            Collections.reverse(arr);
            for (File file : arr) {
                ListViewFragment fragment = new ListViewFragment();
                Bundle args = new Bundle();
                args.putString(PATH_KEY, file.getPath());
                fragment.setArguments(args);
                OnAddFragmentListener listener = (OnAddFragmentListener) currentActivity_;
                listener.onAddFragment(fragment, file);
            }
        }
        int count = currentFile_.list().length;
        for (int i = 0; i < count; i++) {
            sizeValueArray_.add("Counting...");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backPressedState();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        backPressedState();
    }

    private void backPressedState() {
        OnToolbarTextChangeListener listener = (OnToolbarTextChangeListener) currentActivity_;
        String parent = currentFile_.getParent();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (parent.equals(MAIN_PATH)) {
                listener.onToolbarTextChange(getResources().getString(R.string.root_directory),
                        currentFile_.getParentFile());
            } else {
                listener.onToolbarTextChange(parent, currentFile_.getParentFile());
            }
        }
        if (!currentFile_.getPath().equals(MAIN_PATH)) {
            currentFile_ = new File(parent);
        }
        OnPopBackStackListener popBackStackListener = (OnPopBackStackListener) currentActivity_;
        popBackStackListener.onPopBackStack(0);
    }

    @Override
    public void onCountFileSize(int position, String sizeValue) {
        sizeValueArray_.set(position, sizeValue);
        updateView(position);
    }

    @Override
    public void onGetView(File file, int i) {
        manager_.setSize(file.getPath(), i);
    }

    private void fillListView(@NonNull File file) {
        listAdapter_ = new ListAdapter(file, this, sizeValueArray_);
        listView_.setAdapter(listAdapter_);
        String path = file.getPath();
        OnToolbarTextChangeListener listener = (OnToolbarTextChangeListener) currentActivity_;
        listener.onToolbarTextChange((path.equals(MAIN_PATH))
                ? getResources().getString(R.string.root_directory)
                : path, file);
    }

    private void updateView(int position) {
        View view = listView_.getChildAt(position - listView_.getFirstVisiblePosition());
        OnUpdateListViewListener listener = listAdapter_;
        listener.onUpdateListView(view, position);
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

    interface OnAddFragmentListener {
        void onAddFragment(ListViewFragment fragment, File file);
    }

    interface OnPopBackStackListener {
        void onPopBackStack(int state);
    }

    interface OnSaveCurrentFile {
        void onSaveCurrent(String path);
    }
}

interface OnUpdateListViewListener {
    void onUpdateListView(View view, int position);
}
