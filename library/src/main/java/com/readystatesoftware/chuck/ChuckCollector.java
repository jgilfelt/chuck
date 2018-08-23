package com.readystatesoftware.chuck;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.support.NotificationHelper;
import com.readystatesoftware.chuck.internal.support.RetentionManager;

/**
 * @author Olivier Perez
 */
public class ChuckCollector {

    private static final Period DEFAULT_RETENTION = Period.ONE_WEEK;

    private final Context context;
    private final NotificationHelper notificationHelper;
    private RetentionManager retentionManager;

    private boolean showNotification;

    public ChuckCollector(Context context) {
        this.context = context;
        notificationHelper = new NotificationHelper(this.context);
        showNotification = true;
        retentionManager = new RetentionManager(context, DEFAULT_RETENTION);
    }

    public Uri create(HttpTransaction transaction) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        Uri uri = context.getContentResolver().insert(ChuckContentProvider.TRANSACTION_URI, values);
        transaction.setId(Long.valueOf(uri.getLastPathSegment()));
        if (showNotification) {
            notificationHelper.show(transaction);
        }
        retentionManager.doMaintenance();
        return uri;
    }

    public int update(HttpTransaction transaction, Uri uri) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        int updated = context.getContentResolver().update(uri, values, null, null);
        if (showNotification && updated > 0) {
            notificationHelper.show(transaction);
        }
        return updated;
    }

    public boolean bodyHasSupportedEncoding(String contentEncoding) {
        return contentEncoding != null &&
                (contentEncoding.equalsIgnoreCase("identity") ||
                        contentEncoding.equalsIgnoreCase("gzip"));
    }

    public boolean bodyGzipped(String contentEncoding) {
        return "gzip".equalsIgnoreCase(contentEncoding);
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    public void setRetentionManager(RetentionManager retentionManager) {
        this.retentionManager = retentionManager;
    }

    public enum Period {
        /**
         * Retain data for the last hour.
         */
        ONE_HOUR,
        /**
         * Retain data for the last day.
         */
        ONE_DAY,
        /**
         * Retain data for the last week.
         */
        ONE_WEEK,
        /**
         * Retain data forever.
         */
        FOREVER
    }

}
