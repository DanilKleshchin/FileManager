package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListViewFragment.OnToolbarTextChangeListener,
        ListViewFragment.OnListItemClickListener, ListViewFragment.OnSaveCurrentFile {
    private static final String MAIN_PATH = Environment.getExternalStorageDirectory().getParent();
    private static final String LAST_FILE_PATH = "LAST_FILE_PATH";
    public static ActionBar actionBar_;
    private EditText toolbarTitle_;
    private HorizontalScrollView scrollView_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String lastPath = preferences.getString(LAST_FILE_PATH, "");
        if (!lastPath.equals("")) {
            List<File> arr = new ArrayList<>();
            File temp = new File(lastPath);
            String rootFile = new File(MAIN_PATH).getParent();
            while (!temp.getPath().equals(rootFile)) {
                arr.add(temp);
                temp = temp.getParentFile();
            }
            Collections.reverse(arr);
            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            for (File file : arr) {
                ListViewFragment listViewFragment = ListViewFragment.newInstance(file.getPath());
                addFragment(listViewFragment, file);
            }
        } else {
            ListViewFragment listFragment = new ListViewFragment();
            addFragment(listFragment, new File(MAIN_PATH));
        }
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        actionBar_ = getSupportActionBar();
        toolbarTitle_ = (EditText) findViewById(R.id.toolbar_title);
    }

    @Override
    public void onStart() {
        super.onStart();
        getSupportFragmentManager().addOnBackStackChangedListener(new BackStackListener());
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager manager = getSupportFragmentManager();
                OnMenuItemClickListener menuClickListener = null;
                for (Fragment fragment : manager.getFragments()) {
                    if (fragment instanceof OnMenuItemClickListener) {
                        menuClickListener = (OnMenuItemClickListener) fragment;
                        break;
                    }
                }
                if (menuClickListener != null) {
                    menuClickListener.onMenuItemClick(this);
                } else {
                    super.onOptionsItemSelected(item);
                }
                manager.popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onToolbarTextChange(@NonNull String toolbarText, @NonNull File file) {
        toolbarTitle_.setText(toolbarText);
        toolbarTitle_.setSelection(toolbarTitle_.getText().length());
        initToolbar(file);
    }

    @Override
    public void onListItemClick(@NonNull File file) {
        ListViewFragment fragment = ListViewFragment.newInstance(file.getPath());
        addFragment(fragment, file);
    }

    @Override
    public void onSaveCurrent(@NonNull String path) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_FILE_PATH, path);
        editor.apply();
    }

    private void addFragment(@NonNull Fragment fragment, @NonNull File file) {
        FragmentManager manager = getSupportFragmentManager();
        String path = file.getPath();
        if (file.getName().equals(MAIN_PATH)) {
            manager.popBackStack();
        } else {
            manager.popBackStack(file.getParent(), 0);
        }
        FragmentTransaction transaction = manager.beginTransaction();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            transaction.replace(R.id.view_for_replace, fragment, path);
        } else {
            transaction.add(R.id.view_for_replace, fragment, path);
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(path)
                .commit();
    }

    private void initToolbar(@NonNull File file) {
        if (actionBar_ == null) {
            return;
        }
        actionBar_.setDisplayShowTitleEnabled(false);
        actionBar_.setDisplayShowHomeEnabled(true);
        if (!file.getPath().equals(MAIN_PATH)) {
            actionBar_.setDisplayHomeAsUpEnabled(true);
            actionBar_.setLogo(null);
        } else {
            actionBar_.setLogo(R.drawable.drawable_toolbar_logo);
            actionBar_.setHomeButtonEnabled(false);
            actionBar_.setDisplayHomeAsUpEnabled(false);
        }
    }

    private class HorizontalScrollViewListener implements Runnable {
        @Override
        public void run() {
            scrollView_.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        }
    }

    private class BackStackListener implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                finish();
            } else {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    scrollView_ = (HorizontalScrollView) findViewById(R.id.horizontal_scroll_view);
                    scrollView_.postDelayed(new HorizontalScrollViewListener(), 100L);
                }
            }
        }
    }
}

interface OnMenuItemClickListener {
    void onMenuItemClick(Context context);
}
