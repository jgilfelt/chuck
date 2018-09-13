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
package com.readystatesoftware.chuck.internal.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.readystatesoftware.chuck.api.Chuck;

public class ChuckContentProvider extends ContentProvider {

    public static Uri TRANSACTION_URI;
    public static Uri ERROR_URI;

    private static final int TRANSACTION = 0;
    private static final int TRANSACTIONS = 1;
    private static final int ERROR = 2;
    private static final int ERRORS = 3;

    public static final int LOADER_TRANSACTION_DETAIL = 0;
    public static final int LOADER_TRANSACTIONS = 1;
    public static final int LOADER_ERROR_DETAIL = 2;
    public static final int LOADER_ERRORS = 3;

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private ChuckDbOpenHelper databaseHelper;

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        Chuck.init(context);

        TRANSACTION_URI = Uri.parse("content://" + info.authority + "/transaction");
        ERROR_URI = Uri.parse("content://" + info.authority + "/error");
        matcher.addURI(info.authority, "transaction/#", TRANSACTION);
        matcher.addURI(info.authority, "transaction", TRANSACTIONS);
        matcher.addURI(info.authority, "error/#", ERROR);
        matcher.addURI(info.authority, "error", ERRORS);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new ChuckDbOpenHelper(getContext());
        return true;
    }

    @Override
    @Nullable
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = null;
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                cursor = LocalCupboard.getInstance().withDatabase(db).query(HttpTransaction.class).
                        withProjection(projection).
                        withSelection(selection, selectionArgs).
                        orderBy(sortOrder).
                        getCursor();
                break;
            case TRANSACTION:
                cursor = LocalCupboard.getInstance().withDatabase(db).query(HttpTransaction.class).
                        byId(ContentUris.parseId(uri)).
                        getCursor();
                break;
            case ERRORS:
                cursor = LocalCupboard.getInstance().withDatabase(db).query(RecordedThrowable.class)
                        .withProjection(projection)
                        .withSelection(selection, selectionArgs)
                        .orderBy(sortOrder)
                        .getCursor();
                break;
            case ERROR:
                cursor = LocalCupboard.getInstance().withDatabase(db).query(RecordedThrowable.class).
                        byId(ContentUris.parseId(uri)).
                        getCursor();
                break;
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    @Nullable
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    @Nullable
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                long id = db.insert(LocalCupboard.getInstance().getTable(HttpTransaction.class), null, contentValues);
                if (id > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(TRANSACTION_URI, id);
                }
                break;
            case ERRORS:
                long errorId = db.insert(LocalCupboard.getInstance().getTable(RecordedThrowable.class), null, contentValues);
                if (errorId > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(TRANSACTION_URI, errorId);
                }
                break;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int result = 0;
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                result = db.delete(LocalCupboard.getInstance().getTable(HttpTransaction.class), selection, selectionArgs);
                break;
            case TRANSACTION:
                result = db.delete(LocalCupboard.getInstance().getTable(HttpTransaction.class),
                        "_id = ?", new String[]{uri.getPathSegments().get(1)});
                break;
            case ERRORS:
                result = db.delete(LocalCupboard.getInstance().getTable(RecordedThrowable.class), selection, selectionArgs);
                break;
            case ERROR:
                result = db.delete(LocalCupboard.getInstance().getTable(RecordedThrowable.class),
                        "_id = ?", new String[]{uri.getPathSegments().get(1)});
                break;
        }
        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int result = 0;
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                result = db.update(LocalCupboard.getInstance().getTable(HttpTransaction.class), contentValues, selection, selectionArgs);
                break;
            case TRANSACTION:
                result = db.update(LocalCupboard.getInstance().getTable(HttpTransaction.class), contentValues,
                        "_id = ?", new String[]{uri.getPathSegments().get(1)});
                break;
        }
        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }
}
