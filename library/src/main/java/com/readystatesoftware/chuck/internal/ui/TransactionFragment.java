package com.readystatesoftware.chuck.internal.ui;

import com.readystatesoftware.chuck.internal.data.HttpTransaction;

interface TransactionFragment {
    void transactionUpdated(HttpTransaction transaction);
}