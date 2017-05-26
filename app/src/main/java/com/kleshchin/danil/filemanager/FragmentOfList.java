package com.kleshchin.danil.filemanager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Danil Kleshchin on 19.05.2017.
 */
public class FragmentOfList extends Fragment implements MainActivity.OnBackPressedListener{
    private static final String MAIN_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String PATH_KEY = "path";

    private static File currentFile_ = new File(MAIN_PATH);

    private AppCompatActivity currentActivity_;
    private ListView listView_;
    private ListAdapter listAdapter_;
    private EditText toolbarTitle_;
    private View currentView_;
    private FragmentManager manager_;

    private static void onFileSelected(String path, @NonNull Context context)
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
        toolbarTitle_ = MainActivity.toolbarTitle_;
        manager_ = currentActivity_.getSupportFragmentManager();
        listView_.setOnItemClickListener(new ItemClickListener());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        manager_.addOnBackStackChangedListener(new BackStackListener());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PATH_KEY, currentFile_.getPath());
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
            ArrayList<File> arr = new ArrayList<>();
            File temp = currentFile_;
            while (!temp.getPath().equals(MAIN_PATH)) {
                arr.add(temp);
                temp = temp.getParentFile();
            }
            arr.add(new File(MAIN_PATH));
            manager_.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Collections.reverse(arr);
            for (File file : arr) {
                FragmentOfList fragment = new FragmentOfList();
                Bundle args = new Bundle();
                args.putString(PATH_KEY, file.getPath());
                fragment.setArguments(args);
                addFragment(fragment, file);
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

    private void initToolbar(File file) {
        ActionBar actionBar = MainActivity.actionBar_;
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            final Drawable upArrow = ContextCompat.getDrawable(currentView_.getContext(),
                    R.drawable.ic_arrow_back_black_36dp);
            upArrow.setColorFilter(ContextCompat.getColor(currentView_.getContext(), R.color.colorWhite),
                    PorterDuff.Mode.SRC_ATOP);
            String pathToFile = file.getPath();
            if (!pathToFile.equals(MAIN_PATH)) {
                actionBar.setHomeAsUpIndicator(upArrow);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            } else {
                actionBar.setHomeButtonEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
            }
        }
    }

    private void backPressedState() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            String parent = currentFile_.getParent();
            if (parent.equals(MAIN_PATH)) {
                toolbarTitle_.setText(R.string.root_directory);
            } else {
                toolbarTitle_.setText(parent);
            }
            toolbarTitle_.setSelection(toolbarTitle_.getText().length());
            initToolbar(currentFile_.getParentFile());
            currentFile_ = new File(parent);
        }
        manager_.popBackStack();
    }

    private void addFragment(Fragment fragment, File file) {
        String path = file.getPath();
        manager_.popBackStack(file.getParent(), 0);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            manager_.beginTransaction()
                    .replace(R.id.place_holder, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(path)
                    .commit();
        } else {
            manager_.beginTransaction()
                    .add(R.id.fragment_holder, fragment, path)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(path)
                    .commit();
        }
        currentFile_ = file;
    }

    private void fillListView(File file) {
        listAdapter_ = new ListAdapter(currentView_.getContext(), file);
        String path = file.getPath();
        if (path.equals(MAIN_PATH)) {
            toolbarTitle_.setText(R.string.root_directory);
        } else {
            toolbarTitle_.setText(path);
        }
        toolbarTitle_.setSelection(toolbarTitle_.getText().length());
        initToolbar(file);
        listView_.setAdapter(listAdapter_);
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String path = listAdapter_.getItem(i).getPath();
            File file = new File(path);
            if (file.isDirectory()) {
                FragmentOfList fragment = new FragmentOfList();
                Bundle args = new Bundle();
                args.putString(PATH_KEY, path);
                fragment.setArguments(args);
                addFragment(fragment, file);
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

    private class BackStackListener implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            if (manager_.getBackStackEntryCount() == 0) {
                currentActivity_.finish();
            }
        }
    }
}
