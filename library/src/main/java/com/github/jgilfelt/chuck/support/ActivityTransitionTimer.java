package com.github.jgilfelt.chuck.support;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityTransitionTimer {

    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 1000;
    private static final ActivityTransitionTimer ATT = new ActivityTransitionTimer();

    public static ActivityTransitionTimer getInstance() {
        return ATT;
    }

    private ActivityTransitionTimer() {
        // No instances.
    }

    private Timer timer;
    private TimerTask timerTask;
    private boolean timedOut;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                timedOut = true;
            }
        };
        timer.schedule(timerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
        timedOut = false;
    }

    public boolean didTimeOut() {
        return timedOut;
    }
}
