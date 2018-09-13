package com.readystatesoftware.chuck.api;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.data.RecordedThrowable;
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

    /**
     * Call this method when you send an HTTP request.
     *
     * @param transaction The HTTP transaction sent
     * @return The URI of the request in the provider, give it to {@link ChuckCollector#onResponseReceived}
     */
    public Uri onRequestSent(HttpTransaction transaction) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        Uri uri = context.getContentResolver().insert(ChuckContentProvider.TRANSACTION_URI, values);
        transaction.setId(Long.valueOf(uri.getLastPathSegment()));
        if (showNotification) {
            notificationHelper.show(transaction);
        }
        retentionManager.doMaintenance();
        return uri;
    }

    /**
     * Call this method when you received the reponse of an HTTP request. It must be called after {@link ChuckCollector#onRequestSent}.
     *
     * @param transaction The sent HTTP transaction completed with the response
     * @param uri The URI of the request in the provider
     * @return 1 if the HTTP transaction is updated, 0 otherwise
     */
    public int onResponseReceived(HttpTransaction transaction, Uri uri) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        int updated = context.getContentResolver().update(uri, values, null, null);
        if (showNotification && updated > 0) {
            notificationHelper.show(transaction);
        }
        return updated;
    }

    /**
     * Call this method when a throwable is triggered and you want to record it.
     *
     * @param tag A tag you choose
     * @param throwable The triggered throwable
     */
    public void onError(String tag, Throwable throwable) {
        RecordedThrowable recordedThrowable = new RecordedThrowable(tag, throwable);
        ContentValues values = LocalCupboard.getInstance()
                .withEntity(RecordedThrowable.class)
                .toContentValues(recordedThrowable);
        context.getContentResolver().insert(ChuckContentProvider.ERROR_URI, values);
        if (showNotification) {
            notificationHelper.show(recordedThrowable);
        }
        retentionManager.doMaintenance();
    }

    /**
     * Control whether a notification is shown while HTTP activity is recorded.
     *
     * @param showNotification true to show a notification, false to suppress it.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckCollector showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    /**
     * Set the retention period for HTTP transaction data captured by this interceptor.
     * The default is one week.
     *
     * @param retentionManager the manager of retention of stored transactions and errors.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckCollector retentionManager(RetentionManager retentionManager) {
        this.retentionManager = retentionManager;
        return this;
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
