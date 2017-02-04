package com.github.jgilfelt.chuck.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.github.jgilfelt.chuck.R;
import com.github.jgilfelt.chuck.data.HttpTransaction;
import com.github.jgilfelt.chuck.data.LocalCupboard;

public class MainActivity extends AppCompatActivity implements TransactionListFragment.OnListFragmentInteractionListener {

    // TODO
    static {
        LocalCupboard.getInstance().register(HttpTransaction.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chuck_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, TransactionListFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void onListFragmentInteraction(HttpTransaction transaction) {
        TransactionActivity.start(this, transaction.getId());
    }
}
