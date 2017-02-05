package com.readystatesoftware.chuck;

import android.content.Context;
import android.content.Intent;

import com.readystatesoftware.chuck.internal.ui.MainActivity;

public class Chuck {

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}