package com.github.jgilfelt.chuck.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.jgilfelt.chuck.support.ActivityTransitionTimer;
import com.github.jgilfelt.chuck.support.NotificationHelper;

public abstract class BaseChuckActivity extends AppCompatActivity {

    private static boolean inForeground;

    private NotificationHelper notificationHelper;

    public static boolean isInForeground() {
        return inForeground;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationHelper = new NotificationHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityTransitionTimer.getInstance().startTimer();
        inForeground = true;
        notificationHelper.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityTransitionTimer.getInstance().didTimeOut()) {
            inForeground = false;
        }
        ActivityTransitionTimer.getInstance().stopTimer();
    }

}
