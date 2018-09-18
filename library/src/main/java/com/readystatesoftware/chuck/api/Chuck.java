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
package com.readystatesoftware.chuck.api;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;

import com.readystatesoftware.chuck.internal.support.ChuckCrashHandler;
import com.readystatesoftware.chuck.internal.support.NotificationHelper;
import com.readystatesoftware.chuck.internal.ui.MainActivity;

/**
 * Chuck utilities.
 */
public class Chuck {

    public static final int SCREEN_HTTP = 1;
    public static final int SCREEN_ERROR = 2;

    private static Context context;

    public static void init(Context context) {

        Chuck.context = context;
    }

    /**
     * Get an Intent to launch the Chuck UI directly.
     *
     * @param context A Context.
     * @param screen The screen to display: SCREEN_HTTP or SCREEN_ERROR.
     * @return An Intent for the main Chuck Activity that can be started with {@link Context#startActivity(Intent)}.
     */
    public static Intent getLaunchIntent(Context context, @Screen int screen) {
        return new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MainActivity.EXTRA_SCREEN, screen);
    }

    /**
     * Configure the default crash handler of the JVM to report all uncaught Throwable to Chuck.
     * You may only use it for debugging purpose.
     *
     * @param collector the ChuckCollector
     */
    public static void registerDefaultCrashHanlder(final ChuckCollector collector) {
        Thread.setDefaultUncaughtExceptionHandler(new ChuckCrashHandler(collector));
    }

    public static void dismissTransactionsNotification() {
        new NotificationHelper(context).dismissTransactionsNotification();
    }

    public static void dismissErrorsNotification() {
        new NotificationHelper(context).dismissErrorsNotification();
    }

    @IntDef(value = {SCREEN_HTTP, SCREEN_ERROR})
    public @interface Screen {}
}