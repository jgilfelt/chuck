package com.readystatesoftware.chuck;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ChuckInterceptor implements Interceptor {

    public ChuckInterceptor(Context context) {
    }

    public ChuckInterceptor showNotification(boolean show) {
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(request);
    }
}
