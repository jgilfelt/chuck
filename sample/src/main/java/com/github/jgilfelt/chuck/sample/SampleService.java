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
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
        @PATCH("/patch")
        Call<RestData> patch(@Body RestData body);
        @PUT("/put")
        Call<RestData> put(@Body RestData body);
        @DELETE("/delete")
        Call<RestData> delete();
        @GET("/status/{code}")
        Call<Void> status(@Path("code") int code);
        @GET("/stream/{lines}")
        Call<Void> stream(@Path("lines") int lines);
        @GET("/stream-bytes/{bytes}")
        Call<Void> streamBytes(@Path("bytes") int bytes);
        @GET("/delay/{seconds}")
        Call<Void> delay(@Path("seconds") int seconds);
        @GET("/redirect-to")
        Call<Void> redirectTo(@Query("url") String url);
        @GET("/redirect/{times}")
        Call<Void> redirect(@Path("times") int times);
        @GET("/image")
        Call<Void> image(@Header("Accept") String accept);
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
