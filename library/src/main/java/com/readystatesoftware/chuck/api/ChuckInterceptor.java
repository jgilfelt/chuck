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
package com.readystatesoftware.chuck.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.support.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    private static final String LOG_TAG = ChuckInterceptor.class.getSimpleName();
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final ChuckCollector collector;
    private final IOUtils io;

    private long maxContentLength = 250000L;

    public ChuckInterceptor(Context context) {
        collector = new ChuckCollector(context);
        io = new IOUtils(context);
    }

    public ChuckInterceptor(Context context, ChuckCollector collector) {
        this.collector = collector;
        io = new IOUtils(context);
    }

    /**
     * Set the maximum length for request and response content before it is truncated.
     * Warning: setting this value too high may cause unexpected results.
     *
     * @param max the maximum length (in bytes) for request/response content.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckInterceptor maxContentLength(long max) {
        this.maxContentLength = max;
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        HttpTransaction transaction = new HttpTransaction()
                .setRequestDate(new Date())
                .setMethod(request.method())
                .setUrl(request.url().toString())
                .setRequestHeaders(request.headers());

        if (hasRequestBody) {
            if (requestBody.contentType() != null) {
                transaction.setRequestContentType(requestBody.contentType().toString());
            }
            if (requestBody.contentLength() != -1) {
                transaction.setRequestContentLength(requestBody.contentLength());
            }
        }

        boolean encodingIsSupported = io.bodyHasSupportedEncoding(request.headers().get("Content-Encoding"));
        transaction.setRequestBodyIsPlainText(encodingIsSupported);

        if (hasRequestBody && encodingIsSupported) {
            BufferedSource source = io.getNativeSource(new Buffer(), io.bodyIsGzipped(request.headers().get("Content-Encoding")));
            Buffer buffer = source.buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            if (io.isPlaintext(buffer)) {
                String content = io.readFromBuffer(buffer, charset, maxContentLength);
                transaction.setRequestBody(content);
            } else {
                transaction.setResponseBodyIsPlainText(false);
            }
        }

        Uri transactionUri = collector.onRequestSent(transaction);

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            transaction.setError(e.toString());
            collector.onResponseReceived(transaction, transactionUri);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();

        transaction.setRequestHeaders(response.request().headers()) // includes headers added later in the chain
                .setResponseDate(new Date())
                .setTookMs(tookMs)
                .setProtocol(response.protocol().toString())
                .setResponseCode(response.code())
                .setResponseMessage(response.message());

        transaction.setResponseContentLength(responseBody.contentLength());
        if (responseBody.contentType() != null) {
            transaction.setResponseContentType(responseBody.contentType().toString());
        }
        transaction.setResponseHeaders(response.headers());

        boolean responseEncodingIsSupported = io.bodyHasSupportedEncoding(response.headers().get("Content-Encoding"));
        transaction.setResponseBodyIsPlainText(responseEncodingIsSupported);

        if (HttpHeaders.hasBody(response) && responseEncodingIsSupported) {
            BufferedSource source = getNativeSource(response);
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    collector.onResponseReceived(transaction, transactionUri);
                    return response;
                }
            }
            if (io.isPlaintext(buffer)) {
                String content = io.readFromBuffer(buffer.clone(), charset, maxContentLength);
                transaction.setResponseBody(content);
            } else {
                transaction.setResponseBodyIsPlainText(false);
            }
            transaction.setResponseContentLength(buffer.size());
        }

        collector.onResponseReceived(transaction, transactionUri);

        return response;
    }

    private BufferedSource getNativeSource(Response response) throws IOException {
        if (io.bodyIsGzipped(response.headers().get("Content-Encoding"))) {
            BufferedSource source = response.peekBody(maxContentLength).source();
            if (source.buffer().size() < maxContentLength) {
                return io.getNativeSource(source, true);
            } else {
                Log.w(LOG_TAG, "gzip encoded response was too long");
            }
        }
        return response.body().source();
    }
}
