package com.github.jgilfelt.chuck.ui;

import com.github.jgilfelt.chuck.data.HttpTransaction;

interface TransactionFragment {
    void transactionUpdated(HttpTransaction transaction);
}