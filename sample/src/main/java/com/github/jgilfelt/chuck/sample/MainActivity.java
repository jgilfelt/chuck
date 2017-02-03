package com.github.jgilfelt.chuck.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.jgilfelt.chuck.Chuck;
import com.github.jgilfelt.chuck.ChuckInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.do_http).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHttpActivity();
            }
        });
        findViewById(R.id.launch_chuck_directly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchChuckDirectly();
            }
        });
        findViewById(R.id.browse_sql).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteUtils.browseDatabase(MainActivity.this);
            }
        });
    }

    private OkHttpClient getClient(Context context) {
        return new OkHttpClient.Builder()
                .addInterceptor(new ChuckInterceptor(context)) // <- Add ChuckInterceptor in your OkHttp client builder
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    private void doHttpActivity() {
        SampleApiService.HttpbinApi api = SampleApiService.getInstance(getClient(this));
        Callback<Void> cb = new Callback<Void>() {
            @Override public void onResponse(Call call, Response response) {}
            @Override public void onFailure(Call call, Throwable t) {}
        };
        api.get().enqueue(cb);
        api.post(new SampleApiService.Data("posted")).enqueue(cb);
        api.patch(new SampleApiService.Data("patched")).enqueue(cb);
        api.put(new SampleApiService.Data("put")).enqueue(cb);
        api.delete().enqueue(cb);
        api.status(201).enqueue(cb);
        api.status(401).enqueue(cb);
        api.status(500).enqueue(cb);
        api.delay(9).enqueue(cb);
        api.delay(15).enqueue(cb);
        api.redirectTo("http://example.com").enqueue(cb);
        api.redirect(3).enqueue(cb);
        api.stream(500).enqueue(cb);
        api.streamBytes(2048).enqueue(cb);
        api.image("image/png").enqueue(cb);
        api.gzip().enqueue(cb);
    }

    private void launchChuckDirectly() {
        startActivity(Chuck.getLaunchIntent(this)); // <- Call Chuck.getLaunchIntent to launch the Chuck UI directly
    }
}
