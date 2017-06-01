package com.kleshchin.danil.filemanager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
public class ListViewFragment extends Fragment implements OnBackPressedListener {
    private static final String MAIN_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String PATH_KEY = "path";
    private static final String LAST_FILE_PATH = "LAST_FILE_PATH";
    private static File currentFile_ = new File(MAIN_PATH);
    private AppCompatActivity currentActivity_;
    private ListView listView_;
    private ListAdapter listAdapter_;
    private View currentView_;

    @NonNull
    public static ListViewFragment newInstance(@NonNull String path) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString(PATH_KEY, path);
        fragment.setArguments(args);
        return fragment;
    }

    private static void onFileSelected(@NonNull String path, @NonNull Context context)
            throws ActivityNotFoundException {
        Uri uri = Uri.fromFile(new File(path));
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        String mime = "*/*";
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (mimeTypeMap.hasExtension(fileExtension)) {
            mime = mimeTypeMap.getMimeTypeFromExtension(fileExtension);
        }
        intent.setDataAndType(uri, mime).addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        currentView_ = view;
        currentActivity_ = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        listView_ = (ListView) view.findViewById(R.id.listView);
        listView_.setEmptyView(view.findViewById(R.id.list_view_empty_state));
        listView_.setOnItemClickListener(new ItemClickListener());
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PATH_KEY, currentFile_.getPath());
    }

    @Override
    public void onStop() {
        super.onStop();
        OnSaveCurrentFile listener = (OnSaveCurrentFile) currentActivity_;
        listener.onSaveCurrentFile(currentFile_.getPath());
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
            popBackStackListener.onPopBackStackListener(1);
            Collections.reverse(arr);
            for (File file : arr) {
                ListViewFragment fragment = new ListViewFragment();
                Bundle args = new Bundle();
                args.putString(PATH_KEY, file.getPath());
                fragment.setArguments(args);
                OnAddFragmentListener listener = (OnAddFragmentListener) currentActivity_;
                listener.onAddFragmentListener(fragment, file);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                listener.onToolbarTextChangeListener(getResources().getString(R.string.root_directory),
                        currentFile_.getParentFile());
            } else {
                listener.onToolbarTextChangeListener(parent, currentFile_.getParentFile());
            }
        }
        if (!currentFile_.getPath().equals(MAIN_PATH)) {
            currentFile_ = new File(parent);
        }
        OnPopBackStackListener popBackStackListener = (OnPopBackStackListener) currentActivity_;
        popBackStackListener.onPopBackStackListener(0);
    }


    private void fillListView(@NonNull File file) {
        OnToolbarTextChangeListener listener = (OnToolbarTextChangeListener) currentActivity_;
        listAdapter_ = new ListAdapter(currentView_.getContext(), file);
        String path = file.getPath();
        if (path.equals(MAIN_PATH)) {
            listener.onToolbarTextChangeListener(getResources().getString(R.string.root_directory), file);
        } else {
            listener.onToolbarTextChangeListener(path, file);
        }
        listView_.setAdapter(listAdapter_);
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String path = listAdapter_.getItem(i).getPath();
            File file = new File(path);
            if (file.isDirectory()) {
                OnListItemClickListener listener = (OnListItemClickListener) currentActivity_;
                listener.onListItemClickListener(file);
                currentFile_ = file;
            } else {
                try {
                    onFileSelected(path, currentView_.getContext());
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(currentView_.getContext(), R.string.activity_not_found,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    interface OnToolbarTextChangeListener {
        void onToolbarTextChangeListener(String toolbarText, File file);
    }

    interface OnListItemClickListener {
        void onListItemClickListener(File file);
    }

    interface OnAddFragmentListener {
        void onAddFragmentListener(ListViewFragment fragment, File file);
    }

    interface OnPopBackStackListener {
        void onPopBackStackListener(int state);
    }

    interface OnSaveCurrentFile {
        void onSaveCurrentFile(String path);
    }
}
