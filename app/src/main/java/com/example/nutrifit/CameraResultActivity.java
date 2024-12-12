package com.example.nutrifit;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CameraResultActivity extends AppCompatActivity {

    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_result);

        resultTextView = findViewById(R.id.resultTextView);

        // 반환된 JSON 데이터를 받고 String으로 변환
        String resultJson = getIntent().getStringExtra("result_json");

        // 결과 표시
        if (resultJson != null) {
            resultTextView.setText(resultJson);
        } else {
            resultTextView.setText("No result available.");
        }
    }
}