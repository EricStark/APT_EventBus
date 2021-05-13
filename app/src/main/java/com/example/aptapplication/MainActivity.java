package com.example.aptapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.apt_annotations.Subscribe;
import com.example.apt_annotations.ThreadMode;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册
        final Intent intent = new Intent(this, SecondActivity.class);
        BohouEventBus.getDefaultEventbus().register(this);

        findViewById(R.id.btn_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }

    /**
     * 通过反射回调方法
     *
     * @param student
     */
    @Subscribe(threadMode = ThreadMode.MAIN_THREAD)
    private void onRecieve(Student student) {
        Log.e("bohou", "onRecieve_thread = " + Thread.currentThread().getName());
        Log.e("bohou", "onRecieve_student = " + student.toString());
    }

    /**
     * 通过反射回调方法
     *
     * @param teacher
     */
    @Subscribe(threadMode = ThreadMode.MAIN_THREAD)
    private void recieve(Teacher teacher) {
        Log.e("bohou", "recieve_thread = " + Thread.currentThread().getName());
        Log.e("bohou", "recieve_student = " + teacher.toString());
    }
}
