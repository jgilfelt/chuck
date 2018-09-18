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

/**
 * No-op implementation.
 */
public class Chuck {

    public static final int SCREEN_HTTP = 1;
    public static final int SCREEN_ERROR = 2;

    public static Intent getLaunchIntent(Context context, int screen) {
        return new Intent();
    }

    public static void registerDefaultCrashHanlder(ChuckCollector collector) {
    }

    public static void dismissTransactionsNotification() {
    }

    public static void dismissErrorsNotification() {
    }
}
