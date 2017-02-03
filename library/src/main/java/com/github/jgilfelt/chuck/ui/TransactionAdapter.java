package com.github.jgilfelt.chuck.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jgilfelt.chuck.R;
import com.github.jgilfelt.chuck.data.HttpTransaction;
import com.github.jgilfelt.chuck.ui.TransactionListFragment.OnListFragmentInteractionListener;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Context context;
    private final OnListFragmentInteractionListener listener;
    private CursorAdapter cursorAdapter;

    public TransactionAdapter(Context context, OnListFragmentInteractionListener listener) {
        this.listener = listener;
        this.context = context;
        cursorAdapter = new CursorAdapter(TransactionAdapter.this.context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false);
                ViewHolder holder = new ViewHolder(itemView);
                itemView.setTag(holder);
                return itemView;
            }

            @Override
            public void bindView(View view, final Context context, Cursor cursor) {
                final HttpTransaction httpTransaction = cupboard().withCursor(cursor).get(HttpTransaction.class);
                final ViewHolder holder = (ViewHolder) view.getTag();
                holder.id.setText(String.valueOf(httpTransaction.getResponseCode()));
                holder.content.setText(httpTransaction.getPath());
                holder.transaction = httpTransaction;
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != TransactionAdapter.this.listener) {
                            TransactionAdapter.this.listener.onListFragmentInteraction(holder.transaction);
                        }
                    }
                });
            }
        };
    }

    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursorAdapter.getCursor().moveToPosition(position);
        cursorAdapter.bindView(holder.itemView, context, cursorAdapter.getCursor());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = cursorAdapter.newView(context, cursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    public void swapCursor(Cursor newCursor) {
        cursorAdapter.swapCursor(newCursor);
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return cursorAdapter.getCursor();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView id;
        public final TextView content;
        public HttpTransaction transaction;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            id = (TextView) view.findViewById(R.id.id);
            content = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + content.getText() + "'";
        }
    }
}
