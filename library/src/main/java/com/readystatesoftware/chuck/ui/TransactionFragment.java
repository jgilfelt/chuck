package com.readystatesoftware.chuck.ui;

import com.readystatesoftware.chuck.data.HttpTransaction;

interface TransactionFragment {
    void transactionUpdated(HttpTransaction transaction);
}