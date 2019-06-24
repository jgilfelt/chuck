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
package com.readystatesoftware.chuck.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.readystatesoftware.chuck.Chuck;
import com.readystatesoftware.chuck.ChuckInterceptor;
import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        findViewById(R.id.export_json_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportJsonData();
            }
        });
    }

    private void exportJsonData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String export = ChuckContentProvider.export(MainActivity.this);
                Log.v("MainActivity", "Exported json:" + export);
            }
        }).run();
    }

    private OkHttpClient getClient(Context context) {

        ChuckInterceptor interceptor = new ChuckInterceptor(context)
                .retainDataFor(ChuckInterceptor.Period.ONE_DAY)
                .showNotification(true);
        addFilters(interceptor);

        return new OkHttpClient.Builder()
                // Add a ChuckInterceptor instance to your OkHttp client
                .addInterceptor(interceptor)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    private void addFilters(ChuckInterceptor interceptor) {
        List<String> keyWordHeaderList = new ArrayList<>();
        keyWordHeaderList.add("Access-Control-Allow-Credentials");
        keyWordHeaderList.add("Access-Control-Allow-Origin");

        List<String> keyWordUrlList = new ArrayList<>();
        keyWordUrlList.add("cookies");
        keyWordUrlList.add("auth");


        interceptor.setFilterBody(true)
                .setFilterHeaderList(keyWordHeaderList)
                .setFilterUrlList(keyWordUrlList);
    }

    private void launchChuckDirectly() {
        // Optionally launch Chuck directly from your own app UI
        startActivity(Chuck.getLaunchIntent(this));
    }

    private void doHttpActivity() {
        SampleApiService.HttpbinApi api = SampleApiService.getInstance(getClient(this));
        Callback<Void> cb = new Callback<Void>() {
            @Override public void onResponse(Call call, Response response) {}
            @Override public void onFailure(Call call, Throwable t) { t.printStackTrace(); }
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
        api.redirectTo("https://http2.akamai.com").enqueue(cb);
        api.redirect(3).enqueue(cb);
        api.redirectRelative(2).enqueue(cb);
        api.redirectAbsolute(4).enqueue(cb);
        api.stream(500).enqueue(cb);
        api.streamBytes(2048).enqueue(cb);
        api.image("image/png").enqueue(cb);
        api.gzip().enqueue(cb);
        api.xml().enqueue(cb);
        api.utf8().enqueue(cb);
        api.deflate().enqueue(cb);
        api.cookieSet("v").enqueue(cb);
        api.basicAuth("me", "pass").enqueue(cb);
        api.drip(512, 5, 1, 200).enqueue(cb);
        api.deny().enqueue(cb);
        api.cache("Mon").enqueue(cb);
        api.cache(30).enqueue(cb);
    }
}