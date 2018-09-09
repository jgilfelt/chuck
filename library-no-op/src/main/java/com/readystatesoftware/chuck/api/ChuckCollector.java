package com.readystatesoftware.chuck.api;

import android.content.Context;
import android.net.Uri;

/**
 * @author Olivier Perez
 */
public class ChuckCollector {

    private final Context context;

    public ChuckCollector(Context context) {
        this.context = context;
    }

    public Uri onRequestSent(Object object) {
        return null;
    }

    public int onResponseReceived(Object object, Object object2) {
        return 0;
    }

    public void onError(Object object) {
    }

    public boolean bodyHasSupportedEncoding(String contentEncoding) {
        return false;
    }

    public boolean bodyGzipped(String contentEncoding) {
        return false;
    }

    public void setShowNotification(boolean showNotification) {
    }

    public void setRetentionManager(Object object) {
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