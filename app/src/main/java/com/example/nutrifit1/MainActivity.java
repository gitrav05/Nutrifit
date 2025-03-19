package com.example.nutrifit1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button cameraButton, userInformationButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInformationButton = findViewById(R.id.userInformationButton);
        cameraButton = findViewById(R.id.searchButton);

        // 메인화면에서 UserInformActivity로 이동
        userInformationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserInformActivity.class);
            startActivity(intent);
        });

        // 메인화면에서 CameraActivity로 이동
        cameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });
    }
}