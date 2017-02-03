package com.github.jgilfelt.chuck.sample;

import android.content.Context;

import com.github.jgilfelt.chuck.ChuckInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class SampleService {

    private static OkHttpClient client;

    public static OkHttpClient getClient(Context context) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(60000, TimeUnit.MILLISECONDS)
                    .addNetworkInterceptor(new ChuckInterceptor(context))
                    .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }
        return client;
    }

    public static final String API_URL = "https://httpbin.org";

    public static class RestData {
        public final String origin;
        public RestData(String origin) {
            this.origin = origin;
        }
    }

    public interface Httpbin {
        @GET("/get")
        Call<RestData> get();
        @POST("/post")
        Call<RestData> post(@Body RestData body);
    }

    public static Httpbin getInstance(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient(context))
                .build();
        return retrofit.create(Httpbin.class);
    }

}
