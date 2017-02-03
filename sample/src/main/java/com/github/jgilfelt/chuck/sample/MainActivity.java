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
                Callback cb = new Callback<SampleService.RestData>() {
                    @Override
                    public void onResponse(Call<SampleService.RestData> call, Response<SampleService.RestData> response) {
                        Toast.makeText(MainActivity.this, response.body().origin, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Call<SampleService.RestData> call, Throwable t) {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                Call<SampleService.RestData> getCall = httpbin.get();
                Call<SampleService.RestData> postCall = httpbin.post(new SampleService.RestData("fuck"));

                getCall.enqueue(cb);
                postCall.enqueue(cb);
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
