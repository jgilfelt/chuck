/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readystatesoftware.chuck.internal.support;

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
