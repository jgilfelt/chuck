package com.github.jgilfelt.chuck;

import android.content.Context;
import android.content.Intent;

import com.github.jgilfelt.chuck.ui.MainActivity;

public class Chuck {

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}