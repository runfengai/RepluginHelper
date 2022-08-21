package com.jarry.repluginhelper;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NoRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "NoRegister pause", Toast.LENGTH_LONG).show();
    }
}