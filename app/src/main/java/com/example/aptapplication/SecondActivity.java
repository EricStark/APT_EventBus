package com.example.aptapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.apt_annotations.Subscribe;
import com.example.apt_annotations.ThreadMode;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        //注册
        final BohouEventBus bohouEventBus = BohouEventBus.getDefaultEventbus();
        bohouEventBus.register(this);

        findViewById(R.id.btn_tow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 发送事件
                 */
                bohouEventBus.post(new Student("bohou", "123456"));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN_THREAD)
    private void recieve(Teacher teacher) {
        Log.e("bohou", "recieve_thread = " + Thread.currentThread().getName());
        Log.e("bohou", "recieve_student = " + teacher.toString());
    }
}
