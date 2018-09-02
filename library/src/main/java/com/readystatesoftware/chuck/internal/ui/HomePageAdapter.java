package com.readystatesoftware.chuck.internal.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.ui.error.ErrorListFragment;
import com.readystatesoftware.chuck.internal.ui.transaction.TransactionListFragment;

/**
 * @author Olivier Perez
 */
class HomePageAdapter extends FragmentPagerAdapter {

    private final Context context;

    public HomePageAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return TransactionListFragment.newInstance();
        } else {
            return ErrorListFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.chuck_tab_network);
        } else {
            return context.getString(R.string.chuck_tab_errors);
        }
    }
}
