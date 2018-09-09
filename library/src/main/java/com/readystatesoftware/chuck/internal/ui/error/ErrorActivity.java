package com.readystatesoftware.chuck.internal.ui.error;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Olivier Perez
 */
public class ErrorActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "EXTRA_ID";

    public static void start(Context context, Long id) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }

}
