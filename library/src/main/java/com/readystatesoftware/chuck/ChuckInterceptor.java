/*
 * Copyright (C) 2015 Square, Inc, 2017 Jeff Gilfelt.
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
package com.readystatesoftware.chuck;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.support.NotificationHelper;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;

/**
 * An OkHttp Interceptor which persists and displays HTTP activity in your application for later inspection.
 */
public final class ChuckInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private Context context;
    private NotificationHelper notificationHelper;
    private boolean showNotification;

    /**
     * @param context The current Context.
     */
    public ChuckInterceptor(Context context) {
        this.context = context.getApplicationContext();
        notificationHelper = new NotificationHelper(this.context);
        showNotification = true;
    }

    /**
     * Control whether a notification is shown while HTTP activity is recorded.
     *
     * @param show true to show a notification, false to suppress it.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckInterceptor showNotification(boolean show) {
        showNotification = show;
        return this;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        HttpTransaction transaction = new HttpTransaction();
        transaction.setRequestDate(new Date());

        transaction.setMethod(request.method());
        transaction.setUrl(request.url().toString());

        transaction.setRequestHeaders(request.headers());
        if (hasRequestBody) {
            if (requestBody.contentType() != null) {
                transaction.setRequestContentType(requestBody.contentType().toString());
            }
            if (requestBody.contentLength() != -1) {
                transaction.setRequestContentLength(requestBody.contentLength());
            }
        }

        transaction.setRequestBodyIsPlainText(!bodyEncoded(request.headers()));
        if (hasRequestBody && transaction.requestBodyIsPlainText()) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            if (isPlaintext(buffer)) {
                transaction.setRequestBody(buffer.readString(charset));
            } else {
                transaction.setResponseBodyIsPlainText(false);
            }
        }

        Uri transactionUri = create(transaction);

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            transaction.setError(e.toString());
            update(transaction, transactionUri);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();

        transaction.setRequestHeaders(response.request().headers()); // includes headers added later in the chain
        transaction.setResponseDate(new Date());
        transaction.setTookMs(tookMs);
        transaction.setProtocol(response.protocol().toString());
        transaction.setResponseCode(response.code());
        transaction.setResponseMessage(response.message());

        transaction.setResponseContentLength(responseBody.contentLength());
        if (responseBody.contentType() != null) {
            transaction.setResponseContentType(responseBody.contentType().toString());
        }
        transaction.setResponseHeaders(response.headers());

        transaction.setResponseBodyIsPlainText(!bodyEncoded(response.headers()));
        if (HttpHeaders.hasBody(response) && transaction.responseBodyIsPlainText()) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    update(transaction, transactionUri);
                    return response;
                }
            }
            if (isPlaintext(buffer)) {
                transaction.setResponseBody(buffer.clone().readString(charset));
            } else {
                transaction.setResponseBodyIsPlainText(false);
            }
            transaction.setResponseContentLength(buffer.size());
        }

        update(transaction, transactionUri);

        return response;
    }

    private Uri create(HttpTransaction transaction) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        Uri uri = context.getContentResolver().insert(ChuckContentProvider.TRANSACTION_URI, values);
        transaction.setId(Long.valueOf(uri.getLastPathSegment()));
        if (showNotification) {
            notificationHelper.show(transaction);
        }
        return uri;
    }

    private int update(HttpTransaction transaction, Uri uri) {
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        int updated = context.getContentResolver().update(uri, values, null, null);
        if (showNotification && updated > 0) {
            notificationHelper.show(transaction);
        }
        return updated;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }
}
