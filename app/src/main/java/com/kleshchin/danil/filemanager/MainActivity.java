package com.kleshchin.danil.filemanager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static ActionBar actionBar;
    public static EditText toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentOfList listFragment = new FragmentOfList();
        replaceFragment(listFragment);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        actionBar = getSupportActionBar();
        toolbarTitle = (EditText) findViewById(R.id.toolbar_title);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            transaction.replace(R.id.place_holder, fragment).addToBackStack(null).commit();
        } else {
            transaction.add(R.id.fragment_holder, fragment).addToBackStack(null).commit();
        }
    }
}
