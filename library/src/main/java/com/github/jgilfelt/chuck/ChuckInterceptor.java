package com.github.jgilfelt.chuck;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.LongSparseArray;

import com.github.jgilfelt.chuck.data.ChuckContentProvider;
import com.github.jgilfelt.chuck.data.HttpTransaction;
import com.github.jgilfelt.chuck.ui.MainActivity;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public final class ChuckInterceptor implements Interceptor {

    public static final int NOTIFICATION_ID = 1138;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static LongSparseArray<HttpTransaction> transactionBuffer = new LongSparseArray<>();

    private Context context;
    private NotificationManager notificationManager;

    // TODO
    static {
        cupboard().register(HttpTransaction.class);
    }

    public ChuckInterceptor(Context context) {
        this.context = context.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;

        HttpTransaction transaction = new HttpTransaction();
        transaction.setDate(new Date());

        transaction.setMethod(request.method());
        transaction.setProtocol(protocol.toString());
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

        transaction.setRequestBodyIsPlainText(hasRequestBody && !bodyEncoded(request.headers()));
        if (transaction.requestBodyIsPlainText()) {
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
            transaction.setError(e.getMessage());
            update(transaction, transactionUri);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();

        transaction.setTookMs(tookMs);
        transaction.setResponseCode(response.code());
        transaction.setResponseMessage(response.message());

        transaction.setResponseContentLength(responseBody.contentLength());
        if (responseBody.contentType() != null) {
            transaction.setResponseContentType(responseBody.contentType().toString());
        }
        transaction.setResponseHeaders(response.headers());

        transaction.setResponseBodyIsPlainText(HttpHeaders.hasBody(response) && !bodyEncoded(response.headers()));
        if (transaction.responseBodyIsPlainText()) {
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
                if (contentLength > 0) {
                    transaction.setResponseBody(buffer.clone().readString(charset));
                }
            } else {
                transaction.setResponseBodyIsPlainText(false);
            }
            transaction.setResponseContentLength(buffer.size());
        }

        update(transaction, transactionUri);

        return response;
    }

    private Uri create(HttpTransaction transaction) {
        ContentValues values = cupboard().withEntity(HttpTransaction.class).toContentValues(transaction);
        Uri uri = context.getContentResolver().insert(ChuckContentProvider.TRANSACTION_URI, values);
        transaction.setId(Long.valueOf(uri.getLastPathSegment()));
        addToBuffer(transaction);
        showNotification();
        return uri;
    }

    private int update(HttpTransaction transaction, Uri uri) {
        addToBuffer(transaction);
        showNotification();
        ContentValues values = cupboard().withEntity(HttpTransaction.class).toContentValues(transaction);
        return context.getContentResolver().update(uri, values, null, null);
    }

    private synchronized void addToBuffer(HttpTransaction transaction) {
        transactionBuffer.put(transaction.getId(), transaction);
        if (transactionBuffer.size() > 10) {
            transactionBuffer.remove(0);
        }
    }

    private synchronized void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0))
                .setSmallIcon(R.drawable.chuck_ic_notification_black_24dp)
                .setColor(Color.parseColor("#00BCD4"))
                .setContentTitle(context.getString(R.string.chuck_notification_title));
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        int count = 0;
        for (int i = transactionBuffer.size() - 1; i >= 0; i--) {
            if (count < 10) {
                if (count == 0) mBuilder.setContentText(transactionBuffer.valueAt(i).getNotificationText());
                inboxStyle.addLine(transactionBuffer.valueAt(i).getNotificationText());
            }
            count++;
        }
        mBuilder.setNumber(count);
        mBuilder.setStyle(inboxStyle);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
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
