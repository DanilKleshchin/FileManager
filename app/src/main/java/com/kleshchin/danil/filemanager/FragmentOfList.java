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
import android.os.Parcelable;
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
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Danil Kleshchin on 19.05.2017.
 */
public class FragmentOfList extends Fragment {
    private static final String MAIN_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String LIST_VIEW_STATE = "listview.state";
    private static final String FILE_PATH = "filePath";
    private static final String PATH_KEY = "path";

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
        toolbarTitle_ = MainActivity.toolbarTitle;
//        try {
//            String path = getArguments().getString(PATH_KEY);
//            if (path != null) {
//                fillListView(new File(path));
//            }
//        } catch (NullPointerException e) {
//            fillListView(new File(MAIN_PATH));
//        }
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
        outState.putParcelable(LIST_VIEW_STATE, listView_.onSaveInstanceState());
        outState.putParcelable(FILE_PATH, listView_.onSaveInstanceState());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            String path = getArguments().getString(PATH_KEY);
            if (path != null) {
                fillListView(new File(path));
            }
        } catch (NullPointerException e) {
            fillListView(new File(MAIN_PATH));
        }
        if (savedInstanceState != null) {
            FragmentOfList fragment = new FragmentOfList();
            Bundle args = new Bundle();
            args.putString(PATH_KEY, savedInstanceState.getString(FILE_PATH));
            fragment.setArguments(args);
//            addFragment(fragment);

            Parcelable listViewState = savedInstanceState.getParcelable(LIST_VIEW_STATE);
            listView_.onRestoreInstanceState(listViewState);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                manager_.popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initToolbar(File file) {
        ActionBar actionBar = MainActivity.actionBar;
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

    private void addFragment(Fragment fragment, File file) {
        String path = file.getParent();
        prepareStackForAdding(path);
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
    }

    private void prepareStackForAdding(String path) {
        int index = 0;
        for (int i = 1; i < manager_.getBackStackEntryCount(); i++) {
            try {
                String name = manager_.getBackStackEntryAt(i).getName();
                if (name.equalsIgnoreCase(path)) {
                    index = i;
                    break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if(index != 0) {
            while (index != manager_.getBackStackEntryCount()) {
                manager_.popBackStack();
                ++index;
            }
        }
    }

    private void fillListView(File file) {
        listAdapter_ = new ListAdapter(currentView_.getContext(), file);
        if (file.getPath().equals(MAIN_PATH)) {
            toolbarTitle_.setText(R.string.root_directory);
        } else {
            toolbarTitle_.setText(file.getPath());
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
