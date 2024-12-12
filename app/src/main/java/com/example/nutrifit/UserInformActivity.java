package com.example.nutrifit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserInformActivity extends AppCompatActivity {
    private EditText heightEditText;
    private EditText weightEditText;
    private EditText ageEditText;
    private RadioGroup genderRadioGroup;
    private Spinner activitySpinner;
    private Button nextButton;
    private UserDBHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);

        heightEditText = findViewById(R.id.userHeight);
        weightEditText = findViewById(R.id.userWeight);
        ageEditText = findViewById(R.id.userAge);
        genderRadioGroup = findViewById(R.id.userGenderGroup);
        activitySpinner = findViewById(R.id.activitySpinner);
        nextButton = findViewById(R.id.nextButton);
        userDbHelper = new UserDBHelper(this);

        // 다음 버튼 누르면 SQLite에 데이터 저장
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

    private void saveUserInfo() {
        //사용자 입력값 height/weight/age/gender/activity
        String heightText = heightEditText.getText().toString();
        String weightText = weightEditText.getText().toString();
        String ageText = ageEditText.getText().toString();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = (selectedGenderButton != null) ? selectedGenderButton.getText().toString() : null;
        String activity = activitySpinner.getSelectedItem().toString();

        // 입력값 확인
        if (heightText.isEmpty() || weightText.isEmpty() || ageText.isEmpty() || gender == null) {
            Toast.makeText(this, "모든 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 입력값 데이터 타입 변환
        double height = Double.parseDouble(heightText);
        double weight = Double.parseDouble(weightText);
        int age = Integer.parseInt(ageText);

        // SQLite에 데이터 저장
        userDbHelper.insertUser(height, weight, age, gender, activity);
        Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        //저장 후 Logcat으로 저장된 데이터 확인
        Cursor cursor = userDbHelper.getUserInfo();
        if (cursor.moveToLast()) { // 방금 저장한 마지막 데이터 조회
            @SuppressLint("Range") double savedHeight = cursor.getDouble(cursor.getColumnIndex("height"));
            @SuppressLint("Range") double savedWeight = cursor.getDouble(cursor.getColumnIndex("weight"));
            @SuppressLint("Range") int savedAge = cursor.getInt(cursor.getColumnIndex("age"));
            @SuppressLint("Range") String savedGender = cursor.getString(cursor.getColumnIndex("gender"));
            @SuppressLint("Range") String savedActivity = cursor.getString(cursor.getColumnIndex("activity"));

            Log.i("DB_DEBUG", "Saved Data -> Height: " + savedHeight + ", Weight: " + savedWeight +
                    ", Age: " + savedAge + ", Gender: " + savedGender + ", Activity: " + savedActivity);
        }
        cursor.close();

        // SQLite에 데이터 저장 후 메인화면으로 이동
        Intent intent = new Intent(UserInformActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}