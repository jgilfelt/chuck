package com.github.jgilfelt.chuck.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.httpbin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SampleService.Httpbin httpbin = SampleService.getInstance(MainActivity.this);
                Callback<SampleService.RestData> cb = new Callback<SampleService.RestData>() {
                    @Override
                    public void onResponse(Call<SampleService.RestData> call, Response<SampleService.RestData> response) {
                        Toast.makeText(MainActivity.this, response.body().origin, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Call<SampleService.RestData> call, Throwable t) {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                Callback<Void> cbv = new Callback<Void>() {
                    @Override public void onResponse(Call call, Response response) {}
                    @Override public void onFailure(Call call, Throwable t) {}
                };

                httpbin.get().enqueue(cb);
                httpbin.post(new SampleService.RestData("fuck post")).enqueue(cb);
                httpbin.patch(new SampleService.RestData("fuck patch")).enqueue(cb);
                httpbin.put(new SampleService.RestData("fuck put")).enqueue(cb);
                httpbin.delete().enqueue(cb);
                httpbin.status(201).enqueue(cbv);
                httpbin.status(401).enqueue(cbv);
                httpbin.status(500).enqueue(cbv);
                httpbin.delay(15).enqueue(cbv);
                httpbin.redirectTo("http://example.com").enqueue(cbv);
                httpbin.redirect(3).enqueue(cbv);
                httpbin.stream(500).enqueue(cbv);
                httpbin.streamBytes(2048).enqueue(cbv);
                httpbin.image("image/png").enqueue(cbv);

            }
        });

        findViewById(R.id.sql).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DevModeUtils.browseSQLiteDatabase(MainActivity.this);
            }
        });

        findViewById(R.id.chuck).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, com.github.jgilfelt.chuck.ui.MainActivity.class));
            }
        });

    }
}
