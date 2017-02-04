package com.github.jgilfelt.chuck.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jgilfelt.chuck.R;
import com.github.jgilfelt.chuck.data.HttpTransaction;

public class OverviewFragment extends Fragment implements TransactionFragment {

    TextView url;

    private HttpTransaction transaction;

    public OverviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chuck_fragment_overview, container, false);
        url = (TextView) view.findViewById(R.id.url);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateUI();
    }

    @Override
    public void transactionUpdated(HttpTransaction transaction) {
        this.transaction = transaction;
        populateUI();
    }

    private void populateUI() {
        if (isAdded() && transaction != null) {
            url.setText(transaction.getUrl() + " " + transaction.getResponseCode());
            // TODO
        }
    }
}
