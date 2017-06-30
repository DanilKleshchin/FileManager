package com.kleshchin.danil.filemanager;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

/**
 * Created by Danil Kleshchin on 30.06.2017.
 */
final class UtilDialogHelper {

    static AlertDialog makeDialog(@NonNull final Context context,
                                  @NonNull DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.attention)
                .setMessage(R.string.open_all_activities)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .setCancelable(false)
                .setPositiveButton(R.string.yes_button, listener)
                .setNegativeButton(R.string.no_button, null);
        return builder.create();
    }
}
