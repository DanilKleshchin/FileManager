package com.kleshchin.danil.filemanager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String MAIN_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String FRAGMENT_STATE = "Fragment";
    public static ActionBar actionBar_;
    public static EditText toolbarTitle_;
    private FragmentOfList fragment_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentOfList listFragment = new FragmentOfList();
        replaceFragment(listFragment);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        actionBar_ = getSupportActionBar();
        toolbarTitle_ = (EditText) findViewById(R.id.toolbar_title);
        /*if(savedInstanceState != null) {
            fragment_ = (FragmentOfList) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_STATE);
        }*/
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment: fm.getFragments()) {
            if (fragment instanceof  OnBackPressedListener) {
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


    /*@Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_STATE, fragment_);
    }*/

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        manager.popBackStack();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            transaction.replace(R.id.place_holder, fragment).addToBackStack(MAIN_PATH).commit();
        } else {
            transaction.replace(R.id.fragment_holder, fragment).addToBackStack(MAIN_PATH).commit();
        }
    }

    public interface OnBackPressedListener {
        void onBackPressed();
    }
}
