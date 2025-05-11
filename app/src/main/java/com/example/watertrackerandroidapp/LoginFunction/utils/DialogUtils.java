package com.example.watertrackerandroidapp.LoginFunction.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtils {

    public static void showConnectionErrorDialog(Context context, String title, String message,
                                                 DialogInterface.OnClickListener retryListener,
                                                 DialogInterface.OnClickListener exitListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("THỬ LẠI", retryListener)
                .setNegativeButton("THOÁT", exitListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}