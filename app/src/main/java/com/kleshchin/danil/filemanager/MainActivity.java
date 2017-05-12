package com.kleshchin.danil.filemanager;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ListAdapter listAdapter;
    private ArrayList<Pair<Integer, String>> listItems = new ArrayList<>();
    private String mainPath = "/storage";
    private String currentDirectory;
    private EditText toolbarTitle;
    public static final String LISTVIEW_STATE = "listview.state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new ListAdapter(this, listItems);
        toolbarTitle = (EditText) findViewById(R.id.toolbar_title);
        initToolbar();
        fillListView(new File(mainPath));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String path = toolbarTitle.getText().toString() + "/" + listItems.get(i).second;
                if (new File(path).isDirectory()) {
                    fillListView(new File(path));
                } else {
                    onFileSelected(path);
                }
            }
        });
    }

    private void onFileSelected(String path) {
        Uri uri = Uri.fromFile(new File(path));
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        String mime = "*/*";
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (mimeTypeMap.hasExtension(
                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
            mime = mimeTypeMap.getMimeTypeFromExtension(
                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        intent.setDataAndType(uri, mime);
        startActivity(intent);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_36dp);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        String toolbarText = toolbarTitle.getText().toString();
        if (!toolbarText.equals("") && !toolbarText.equals(mainPath)) {
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                String toolbarText = toolbarTitle.getText().toString();
                fillListView(new File(toolbarText.substring(0,
                        toolbarText.length() - 1 - currentDirectory.length() + 1)));
                break;
        }
        return true;
    }

    private void fillListView(File file) {
        listItems.clear();
        File list[] = file.listFiles();
        toolbarTitle.setText(file.getPath());
        toolbarTitle.setSelection(toolbarTitle.getText().length());
        initToolbar();
        currentDirectory = file.getName();
        if (list != null) {
            for (File currentFile : list) {
                if (currentFile.isDirectory()) {
                    listItems.add(new Pair(Integer.valueOf(R.drawable.folder), currentFile.getName()));
                } else {
                    listItems.add(new Pair(Integer.valueOf(R.drawable.file), currentFile.getName()));
                }
            }
            Collections.sort(listItems, new Comparator<Pair<Integer, String>>() {
                @Override
                public int compare(Pair<Integer, String> l1, Pair<Integer, String> l2) {
                    int nameCompare = String.valueOf(l2.first)
                            .compareToIgnoreCase(String.valueOf(l1.first));
                    if (nameCompare != 0) {
                        return nameCompare;
                    } else {
                        return l1.second.compareTo(l2.second);
                    }
                }
            });
            listView.setAdapter(listAdapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LISTVIEW_STATE, listView.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        initToolbar();
        Parcelable listViewState = savedInstanceState.getParcelable(LISTVIEW_STATE);
        listView.onRestoreInstanceState(listViewState);
    }
}
