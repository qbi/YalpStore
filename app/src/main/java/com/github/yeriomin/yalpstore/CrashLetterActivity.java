package com.github.yeriomin.yalpstore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;

import java.util.ArrayList;

public class CrashLetterActivity extends Activity {

    static public boolean send(Context context, String stackTrace) {
        Intent emailIntent = getEmailIntent(context, stackTrace);
        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(emailIntent);
            return true;
        }
        return false;
    }

    static private Intent getEmailIntent(Context context, String stackTrace) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        String developerEmail = context.getString(R.string.about_developer_email);
        emailIntent.setData(Uri.fromParts("mailto", developerEmail, null));
        emailIntent.setType("plain/text");
        emailIntent.setType("message/rfc822");
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {developerEmail});
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            context.getString(
                TextUtils.isEmpty(stackTrace) ? R.string.email_subject_feedback : R.string.email_subject_crash_report,
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME
            )
        );
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(new CrashLetterDeviceInfoBuilder(context).getUri());
        uris.add(new CrashLetterLogBuilder(context).getUri());
        if (!TextUtils.isEmpty(stackTrace)) {
            uris.add(new CrashLetterStackTraceBuilder(context).setStackTrace(stackTrace).getUri());
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        return emailIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCrashDialog();
    }

    private AlertDialog showCrashDialog() {
        return new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title_application_crashed))
            .setMessage(getString(R.string.dialog_message_application_crashed))
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            })
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    send(
                        CrashLetterActivity.this,
                        getIntent().getExtras().getString(YalpStoreUncaughtExceptionHandler.INTENT_MESSAGE)
                    );
                    dialog.dismiss();
                    finish();
                }
            })
            .show()
        ;
    }
}
