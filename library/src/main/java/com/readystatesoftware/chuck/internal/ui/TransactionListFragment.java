/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readystatesoftware.chuck.internal.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.support.DividerItemDecoration;
import com.readystatesoftware.chuck.internal.support.NotificationHelper;
import com.readystatesoftware.chuck.internal.support.SQLiteUtils;

public class TransactionListFragment extends Fragment implements
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private String currentFilter;
    private OnListFragmentInteractionListener listener;
    private TransactionAdapter adapter;
    private String selectedStatus;
    private int selectedStatusItem = -1;

    public TransactionListFragment() {}

    public static TransactionListFragment newInstance() {
        return new TransactionListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chuck_fragment_transaction_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL_LIST));
            adapter = new TransactionAdapter(getContext(), listener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chuck_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            getContext().getContentResolver().delete(ChuckContentProvider.TRANSACTION_URI, null, null);
            NotificationHelper.clearBuffer();
            return true;
        } else if (item.getItemId() == R.id.filter) {
            showFilters();
            return true;
        }  else if (item.getItemId() == R.id.browse_sql) {
            SQLiteUtils.browseDatabase(getContext());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showFilters() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(R.array.status_codes, selectedStatusItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedStatusItem = which;
                selectedStatus = getResources().getStringArray(R.array.status_codes_values)[which];
            }
        });
        builder.setTitle(R.string.chuck_filter_title);
        builder.setPositiveButton(R.string.chuck_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getLoaderManager().restartLoader(0, null, TransactionListFragment.this);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.chuck_neutral_filter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedStatus = null;
                selectedStatusItem = -1;
                getLoaderManager().restartLoader(0, null, TransactionListFragment.this);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.chuck_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext());
        loader.setUri(ChuckContentProvider.TRANSACTION_URI);
        if (!TextUtils.isEmpty(selectedStatus) && !TextUtils.isEmpty(currentFilter)) {
            loader.setSelection("responseCode LIKE ? AND path LIKE ?");
            loader.setSelectionArgs(new String[] { selectedStatus + "%", "%" + currentFilter + "%" });
        } else if (!TextUtils.isEmpty(selectedStatus)) {
            loader.setSelection("responseCode LIKE ?");
            loader.setSelectionArgs(new String[] { selectedStatus + "%" });
        } else if (!TextUtils.isEmpty(currentFilter)) {
            loader.setSelection("path LIKE ?");
            loader.setSelectionArgs(new String[] { "%" + currentFilter + "%" });
        }
        loader.setProjection(HttpTransaction.PARTIAL_PROJECTION);
        loader.setSortOrder("requestDate DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentFilter = newText;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(HttpTransaction item);
    }
}
