package com.kleshchin.danil.filemanager;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Danil Kleshchin on 26.06.2017.
 */

final class ViewHolder {
    TextView fileName;
    TextView fileSize;
    ImageView fileImage;
    ProgressBar progressBar;

    ViewHolder(View view) {
        fileName = (EditText) view.findViewById(R.id.file_name);
        fileImage = (ImageView) view.findViewById(R.id.file_image);
        fileSize = (TextView) view.findViewById(R.id.file_size);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
    }
}