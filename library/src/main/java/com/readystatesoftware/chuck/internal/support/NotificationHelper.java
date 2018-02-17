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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.LongSparseArray;

import com.readystatesoftware.chuck.Chuck;
import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.ui.BaseChuckActivity;

import java.lang.reflect.Method;

public class NotificationHelper {

    private static final String CHANNEL_ID = "chuck";
    private static final int NOTIFICATION_ID = 1138;
    private static final int BUFFER_SIZE = 10;

    private static final LongSparseArray<HttpTransaction> transactionBuffer = new LongSparseArray<>();
    private static int transactionCount;

    private final Context context;
    private final NotificationManager notificationManager;
    private Method setChannelId;

    public static synchronized void clearBuffer() {
        transactionBuffer.clear();
        transactionCount = 0;
    }

    private static synchronized void addToBuffer(HttpTransaction transaction) {
        if (transaction.getStatus() == HttpTransaction.Status.Requested) {
            transactionCount++;
        }
        transactionBuffer.put(transaction.getId(), transaction);
        if (transactionBuffer.size() > BUFFER_SIZE) {
            transactionBuffer.removeAt(0);
        }
    }

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID,
                            context.getString(R.string.notification_category), NotificationManager.IMPORTANCE_LOW));
            try {
                setChannelId = NotificationCompat.Builder.class.getMethod("setChannelId", String.class);
            } catch (Exception ignored) {}
        }
    }

    public synchronized void show(HttpTransaction transaction) {
        addToBuffer(transaction);
        if (!BaseChuckActivity.isInForeground()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setContentIntent(PendingIntent.getActivity(context, 0, Chuck.getLaunchIntent(context), 0))
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.chuck_ic_notification_white_24dp)
                    .setColor(ContextCompat.getColor(context, R.color.chuck_colorPrimary))
                    .setContentTitle(context.getString(R.string.chuck_notification_title));
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            if (setChannelId != null) {
                try { setChannelId.invoke(builder, CHANNEL_ID); } catch (Exception ignored) {}
            }
            int count = 0;
            for (int i = transactionBuffer.size() - 1; i >= 0; i--) {
                if (count < BUFFER_SIZE) {
                    if (count == 0) {
                        builder.setContentText(transactionBuffer.valueAt(i).getNotificationText());
                    }
                    inboxStyle.addLine(transactionBuffer.valueAt(i).getNotificationText());
                }
                count++;
            }
            builder.setAutoCancel(true);
            builder.setStyle(inboxStyle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setSubText(String.valueOf(transactionCount));
            } else {
                builder.setNumber(transactionCount);
            }
            builder.addAction(getClearAction());
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    @NonNull
    private NotificationCompat.Action getClearAction() {
        CharSequence clearTitle = context.getString(R.string.chuck_clear);
        Intent deleteIntent = new Intent(context, ClearTransactionsService.class);
        PendingIntent intent = PendingIntent.getService(context, 11, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
        return new NotificationCompat.Action(R.drawable.chuck_ic_delete_white_24dp,
            clearTitle, intent);
    }

    public void dismiss() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
