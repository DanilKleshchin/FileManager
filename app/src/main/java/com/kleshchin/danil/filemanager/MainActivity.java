package com.kleshchin.danil.filemanager;

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
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements ListViewFragment.OnToolbarTextChangeListener,
        ListViewFragment.OnListItemClickListener, ListViewFragment.OnAddFragmentListener,
        ListViewFragment.OnPopBackStackListener, ListViewFragment.OnSaveCurrentFile {
    private static final String MAIN_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String LAST_FILE_PATH = "LAST_FILE_PATH";
    public static ActionBar actionBar_;
    private EditText toolbarTitle_;
    private FragmentManager manager_;
    private HorizontalScrollView scrollView_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListViewFragment listFragment = new ListViewFragment();
        replaceFragment(listFragment);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        actionBar_ = getSupportActionBar();
        toolbarTitle_ = (EditText) findViewById(R.id.toolbar_title);
        manager_ = getSupportFragmentManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        manager_.addOnBackStackChangedListener(new BackStackListener());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment : fm.getFragments()) {
            if (fragment instanceof OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fragment;
                break;
            }
        }
        if (backPressedListener != null) {
            backPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onToolbarTextChangeListener(@NonNull String toolbarText, @NonNull File file) {
        toolbarTitle_.setText(toolbarText);
        toolbarTitle_.setSelection(toolbarTitle_.getText().length());
        initToolbar(file);
    }

    @Override
    public void onAddFragmentListener(@NonNull ListViewFragment fragment, @NonNull File file) {
        addFragment(fragment, file);
    }

    @Override
    public void onPopBackStackListener(int state) {
        switch (state) {
            case 0:
                manager_.popBackStack();
                break;
            case 1:
                manager_.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
        }
    }

    @Override
    public void onListItemClickListener(@NonNull File file) {
        ListViewFragment fragment = ListViewFragment.newInstance(file.getPath());
        addFragment(fragment, file);
    }

    @Override
    public void onSaveCurrentFile(@NonNull String path) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_FILE_PATH, path);
        editor.apply();
    }

    private void addFragment(@NonNull Fragment fragment, @NonNull File file) {
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

    private void replaceFragment(@NonNull Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        manager.popBackStack();
        transaction.replace(R.id.view_for_replace, fragment)
                .addToBackStack(MAIN_PATH)
                .commit();
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
            if (manager_.getBackStackEntryCount() == 0) {
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

interface OnBackPressedListener {
    void onBackPressed();
}
