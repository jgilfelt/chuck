package com.readystatesoftware.chuck.internal.ui;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;

public class MainActivity extends BaseChuckActivity implements TransactionListFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chuck_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(getApplicationName());
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

    private String getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }
}
