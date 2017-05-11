package com.kleshchin.danil.filemanager;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ListAdapter listAdapter;
    ArrayList<ListItems> listItems;
    ArrayList<ListItems> listFilesItems;
    Toolbar toolbar;
    String mainPath = "/storage";
    String currentDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        listItems = new ArrayList<>();
        listFilesItems = new ArrayList<>();
        listAdapter = new ListAdapter(getApplicationContext(), listItems);
        initToolbar();
        fillListView(new File(mainPath));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String path = ((EditText) findViewById(R.id.toolbar_title)).getText().toString() +
                        "/" + listItems.get(i).fileName;
                if (new File(path).isDirectory()) {
                    fillListView(new File(path));
                } else {
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
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setLogo(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        final EditText toolbarText = (EditText) findViewById(R.id.toolbar_title);
        //toolbarText.setLines(1);
        if (!toolbarText.getText().equals("") &&
                !toolbarText.getText().equals(mainPath)) {
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_36dp);
            upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
            toolbar.setLogo(upArrow);
            setLogoOnClickListener(toolbar, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fillListView(new File(toolbarText.getText().
                            toString().substring(0, toolbarText.
                            getText().toString().length() - 1 - currentDirectory.length() + 1)));
                }
            });
        }
    }

    private void setLogoOnClickListener(Toolbar toolbar, View.OnClickListener listener) {
        try {
            Class<?> toolbarClass = Toolbar.class;
            Field logoField = toolbarClass.getDeclaredField("mLogoView");
            logoField.setAccessible(true);
            ImageView logoView = (ImageView) logoField.get(toolbar);
            if (logoView != null) {
                logoView.setOnClickListener(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillListView(File file) {
        listItems.clear();
        listFilesItems.clear();
        listView.setAdapter(listAdapter);
        File list[] = file.listFiles();
        ((EditText) findViewById(R.id.toolbar_title)).setText(file.getPath());
        initToolbar();
        currentDirectory = file.getName();
        if (list != null) {
            for (File currentFile : list) {
                if (currentFile.isDirectory()) {
                    listItems.add(new ListItems(currentFile.getName(), R.drawable.folder));
                } else {
                    listFilesItems.add(new ListItems(currentFile.getName(), R.drawable.file));
                }
            }
            listItems.addAll(listFilesItems);
            listView.setAdapter(listAdapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        listView.setAdapter(listAdapter);
        outState.putParcelable("listview.state", listView.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        initToolbar();
        Parcelable listViewState = savedInstanceState.getParcelable("listview.state");
        listView.onRestoreInstanceState(listViewState);
    }
}