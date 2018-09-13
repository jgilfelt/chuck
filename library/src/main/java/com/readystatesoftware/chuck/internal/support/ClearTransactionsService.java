package com.readystatesoftware.chuck.internal.support;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;

public class ClearTransactionsService extends IntentService {

    public static final int CLEAR_TRANSACTIONS = 0;
    public static final int CLEAR_ERRORS = 1;
    public static final String EXTRA_ITEM_TO_CLEAR = "EXTRA_ITEM_TO_CLEAR";

    public ClearTransactionsService() {
        super("Chuck-ClearTransactionsService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int itemToClear = intent.getIntExtra(EXTRA_ITEM_TO_CLEAR, -1);
        switch (itemToClear) {
            case CLEAR_TRANSACTIONS: {
                getContentResolver().delete(ChuckContentProvider.TRANSACTION_URI, null, null);
                NotificationHelper.clearBuffer();
                NotificationHelper notificationHelper = new NotificationHelper(this);
                notificationHelper.dismissTransactionsNotification();
                break;
            }
            case CLEAR_ERRORS: {
                getContentResolver().delete(ChuckContentProvider.ERROR_URI, null, null);
                NotificationHelper notificationHelper = new NotificationHelper(this);
                notificationHelper.dismissErrorsNotification();
                break;
            }
        }
    }

    @IntDef(value = {CLEAR_TRANSACTIONS, CLEAR_ERRORS})
    public @interface Clear {
    }
}