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
package com.readystatesoftware.chuck;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * No-op implementation.
 */
public final class ChuckInterceptor implements Interceptor {

    public ChuckInterceptor setFilterBody(boolean filterBoby) {
        return this;
    }

    public ChuckInterceptor setFilterHeaderList(List<String> keyWordHeaderList) {
        return this;
    }

    public ChuckInterceptor setFilterUrlList(List<String> keyWordUrlList) {
        return this;
    }

    public enum Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }

    public ChuckInterceptor(Context context) {
    }

    public ChuckInterceptor showNotification(boolean show) {
        return this;
    }

    public ChuckInterceptor maxContentLength(long max) {
        return this;
    }

    public ChuckInterceptor retainDataFor(Period period) {
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(request);
    }
}
