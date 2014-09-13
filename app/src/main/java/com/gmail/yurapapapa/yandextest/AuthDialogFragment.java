package com.gmail.yurapapapa.yandextest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by yurap_000 on 010 10 сен.
 */
public class AuthDialogFragment extends DialogFragment{
    public AuthDialogFragment() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Authorization")
                .setMessage("AuthorizationMess")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MyActivity.AUTH_URL)));
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });
        return builder.create();
    }
}
