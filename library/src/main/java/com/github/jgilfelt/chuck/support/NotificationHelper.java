package com.github.jgilfelt.chuck.support;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.LongSparseArray;

import com.github.jgilfelt.chuck.Chuck;
import com.github.jgilfelt.chuck.R;
import com.github.jgilfelt.chuck.data.HttpTransaction;
import com.github.jgilfelt.chuck.ui.BaseChuckActivity;

public class NotificationHelper {

    private static final int NOTIFICATION_ID = 1138;
    private static final int BUFFER_SIZE = 10;

    private static LongSparseArray<HttpTransaction> transactionBuffer = new LongSparseArray<>();

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public synchronized void show(HttpTransaction transaction) {
        addToBuffer(transaction);
        if (!BaseChuckActivity.isInForeground()) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setContentIntent(PendingIntent.getActivity(context, 0, Chuck.getLaunchIntent(context), 0))
                    .setSmallIcon(R.drawable.chuck_ic_notification_black_24dp)
                    .setColor(Color.parseColor("#00BCD4"))
                    .setContentTitle(context.getString(R.string.chuck_notification_title));
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            int count = 0;
            for (int i = transactionBuffer.size() - 1; i >= 0; i--) {
                if (count < BUFFER_SIZE) {
                    if (count == 0)
                        mBuilder.setContentText(transactionBuffer.valueAt(i).getNotificationText());
                    inboxStyle.addLine(transactionBuffer.valueAt(i).getNotificationText());
                }
                count++;
            }
            mBuilder.setAutoCancel(true);
            mBuilder.setStyle(inboxStyle);
            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else {
            dismiss();
        }
    }

    public void dismiss() {
        notificationManager.cancel(NOTIFICATION_ID);
        transactionBuffer.clear();
    }

    private synchronized void addToBuffer(HttpTransaction transaction) {
        transactionBuffer.put(transaction.getId(), transaction);
        if (transactionBuffer.size() > BUFFER_SIZE) {
            transactionBuffer.remove(0);
        }
    }
}
