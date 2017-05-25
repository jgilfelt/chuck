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
package com.readystatesoftware.chuck;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.Nullable;
import com.readystatesoftware.chuck.internal.ui.MainActivity;
import java.util.Collections;

/**
 * Chuck utilities.
 */
public class Chuck {

    /**
     * Get an Intent to launch the Chuck UI directly.
     *
     * @param context A Context.
     * @return An Intent for the main Chuck Activity that can be started with {@link Context#startActivity(Intent)}.
     */
    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * Register an app shortcut to launch the Chuck UI directly from the launcher on Android 7.0 and above.
     *
     * @param context A valid {@link Context}
     * @return The id of the added shortcut (<code>null</code> if this feature is not supported on the device).
     * It can be used if you want to remove this shortcut later on.
     */
    @TargetApi(Build.VERSION_CODES.N_MR1)
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static String addAppShortcut(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            final String id = context.getPackageName() + ".chuck_ui";
            final ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            final ShortcutInfo shortcut = new ShortcutInfo.Builder(context, id).setShortLabel("Chuck")
                .setLongLabel("Open Chuck UI")
                .setIcon(Icon.createWithResource(context, R.drawable.chuck_ic_notification_white_24dp))
                .setIntent(getLaunchIntent(context).setAction(Intent.ACTION_VIEW))
                .build();
            shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcut));
            return id;
        } else {
            return null;
        }
    }
}