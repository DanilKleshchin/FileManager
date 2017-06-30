package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import com.facebook.stetho.Stetho;

public class MainActivity extends AppCompatActivity implements
        ListViewFragment.OnListItemClickListener, ListViewFragment.OnStopFragmentListener {

    private static final String MAIN_PATH = "/";
    private static final String LAST_FILE_PATH = "LAST_FILE_PATH";
    public static ActionBar actionBar_;
    private EditText toolbarTitle_;
    private HorizontalScrollView scrollView_;
    @NonNull
    private File currentFile_ = new File(MAIN_PATH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Stetho.initializeWithDefaults(this);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        actionBar_ = getSupportActionBar();
        toolbarTitle_ = (EditText) findViewById(R.id.toolbar_title);
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String lastPath = preferences.getString(LAST_FILE_PATH, "");
        File temp = new File(lastPath);
        if (temp.exists()) {
            currentFile_ = temp;
            List<File> arr = new ArrayList<>();
            while (!temp.getPath().equals(MAIN_PATH)) {
                arr.add(temp);
                temp = temp.getParentFile();
            }
            arr.add(temp);
            Collections.reverse(arr);
            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            for (File file : arr) {
                ListViewFragment listViewFragment = ListViewFragment.newInstance(file.getPath());
                addFragment(listViewFragment, file);
            }
        } else {
            ListViewFragment listFragment = ListViewFragment.newInstance(null);
            addFragment(listFragment, currentFile_);
        }
    }

    @Override
    public void onStart() {
        getSupportFragmentManager().addOnBackStackChangedListener(new BackStackListener());
        super.onStart();
    }

    @Override
    public void onDestroy() {
        SizeManager.releaseSpace();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        onBackPressedState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressedState();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Pop fragment from back stack and init toolbar with path of parent file.
     */
    private void onBackPressedState() {
        currentFile_ = currentFile_.getParentFile();
        getSupportFragmentManager().popBackStack();
        setToolbar(currentFile_);
    }


    @Override
    public void onListItemClick(@NonNull File file) {
        currentFile_ = file;
        ListViewFragment fragment = ListViewFragment.newInstance(file.getPath());
        addFragment(fragment, file);
    }

    /**
     * Save path of last file in shared preferences
     * @param path - path of last file
     */
    @Override
    public void onStopFragment(@NonNull String path) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_FILE_PATH, path);
        editor.apply();
    }

    private void addFragment(@NonNull Fragment fragment, @NonNull File file) {
        FragmentManager manager = getSupportFragmentManager();
        String path = file.getPath();
        setToolbar(file);
        if (path.equals(MAIN_PATH)) {
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

    private void setToolbar(@NonNull File file) {
        String filePath = file.getPath();
        String name = filePath.equals(MAIN_PATH) ?
                getResources().getString(R.string.root_directory) :
                filePath;
        toolbarTitle_.setText(name);
        toolbarTitle_.setSelection(toolbarTitle_.getText().length());
        if (actionBar_ == null) {
            return;
        }
        actionBar_.setDisplayShowTitleEnabled(false);
        actionBar_.setDisplayShowHomeEnabled(true);
        if (!filePath.equals(MAIN_PATH)) {
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
                    scrollView_.postDelayed(new HorizontalScrollViewListener(), 1000L);
                }
            }
        }
    }
}
