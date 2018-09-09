package com.readystatesoftware.chuck.internal.ui.error;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.data.RecordedThrowable;

import java.text.DateFormat;

/**
 * @author Olivier Perez
 */
public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder> {
    private final CursorAdapter cursorAdapter;
    private final ErrorListListener listener;
    private final Context context;

    public ErrorAdapter(@NonNull Context context, @NonNull ErrorListListener listener) {
        this.context = context;
        this.listener = listener;
        cursorAdapter = new CursorAdapter(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = LayoutInflater.from(context).inflate(R.layout.chuck_list_item_error, parent, false);
                view.setTag(new ErrorViewHolder(view));
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final RecordedThrowable throwable = LocalCupboard.getInstance().withCursor(cursor).get(RecordedThrowable.class);
                final ErrorViewHolder holder = (ErrorViewHolder) view.getTag();
                holder.bind(throwable);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ErrorAdapter.this.listener.onClick(holder.throwable);
                    }
                });
            }
        };
    }

    @NonNull
    @Override
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = cursorAdapter.newView(context, cursorAdapter.getCursor(), parent);
        return new ErrorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorViewHolder holder, final int position) {
        cursorAdapter.getCursor().moveToPosition(position);
        cursorAdapter.bindView(holder.itemView, context, cursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }

    public void swapCursor(Cursor data) {
        cursorAdapter.swapCursor(data);
        notifyDataSetChanged();
    }

    public static class ErrorViewHolder extends RecyclerView.ViewHolder {

        private final TextView tag;
        private final TextView clazz;
        private final TextView message;
        private final TextView date;
        private RecordedThrowable throwable;

        public ErrorViewHolder(View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);
            clazz = itemView.findViewById(R.id.clazz);
            message = itemView.findViewById(R.id.message);
            date = itemView.findViewById(R.id.date);
        }

        public void bind(RecordedThrowable throwable) {
            this.throwable = throwable;
            tag.setText(throwable.getTag());
            clazz.setText(throwable.getClazz());
            message.setText(throwable.getMessage());
            date.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(throwable.getDate()));
        }
    }

    public interface ErrorListListener {
        void onClick(RecordedThrowable throwable);
    }
}
