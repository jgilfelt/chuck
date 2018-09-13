package com.readystatesoftware.chuck.internal.ui.error;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.data.RecordedThrowable;

import java.text.DateFormat;

import static com.readystatesoftware.chuck.internal.data.ChuckContentProvider.LOADER_ERROR_DETAIL;

/**
 * @author Olivier Perez
 */
public class ErrorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_ID = "EXTRA_ID";
    private long throwableId;
    private RecordedThrowable throwable;

    private TextView title;
    private TextView tag;
    private TextView clazz;
    private TextView message;
    private TextView date;
    private TextView stacktrace;

    public static void start(Context context, Long id) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chuck_activity_error);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.toolbar_title);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        tag = findViewById(R.id.tag);
        clazz = findViewById(R.id.clazz);
        message = findViewById(R.id.message);
        date = findViewById(R.id.date);
        stacktrace = findViewById(R.id.stacktrace);

        date.setVisibility(View.GONE);

        throwableId = getIntent().getLongExtra(EXTRA_ID, 0);
        getSupportLoaderManager().initLoader(LOADER_ERROR_DETAIL, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(LOADER_ERROR_DETAIL, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chuck_error, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share_text) {
            share(throwable);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void share(RecordedThrowable throwable) {
        String text = getString(R.string.chuck_share_error_content,
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(throwable.getDate()),
                throwable.getClazz(),
                throwable.getTag(),
                throwable.getMessage(),
                throwable.getContent());

        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(getString(R.string.chuck_share_error_title))
                .setText(text)
                .createChooserIntent());
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(ContentUris.withAppendedId(ChuckContentProvider.ERROR_URI, throwableId));
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        throwable = LocalCupboard.getInstance().withCursor(data).get(RecordedThrowable.class);
        populateUI();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    private void populateUI() {
        if (throwable != null) {
            String dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(throwable.getDate());
            title.setText(dateStr);
            tag.setText(throwable.getTag());
            clazz.setText(throwable.getClazz());
            message.setText(throwable.getMessage());
            stacktrace.setText(throwable.getContent());
        }
    }
}
