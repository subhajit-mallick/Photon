package com.subhajit.photon.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.subhajit.photon.R;

public class MainActivity extends AppCompatActivity {
        //mainActivity is for splash screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent (MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
