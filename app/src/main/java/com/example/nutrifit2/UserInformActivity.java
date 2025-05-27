package com.example.nutrifit2;

import android.annotation.SuppressLint;
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class UserInformActivity extends AppCompatActivity {
    private EditText heightEditText, weightEditText, ageEditText;
    private RadioGroup genderRadioGroup, riceRadioGroup, stewRadioGroup, sideDishRadioGroup;
    private Spinner activitySpinner;
    private Button nextButton;
    private UserDBHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);

        // UI 요소 초기화
        heightEditText = findViewById(R.id.userHeight);
        weightEditText = findViewById(R.id.userWeight);
        ageEditText = findViewById(R.id.userAge);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        activitySpinner = findViewById(R.id.activitySpinner);
        nextButton = findViewById(R.id.nextButton);

        stewRadioGroup = findViewById(R.id.stewRadioGroup);
        sideDishRadioGroup = findViewById(R.id.sideDishRadioGroup);

        userDbHelper = new UserDBHelper(this);

        // 다음 버튼 클릭 시 데이터 저장
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

    private void saveUserInfo() {
        String heightText = heightEditText.getText().toString();
        String weightText = weightEditText.getText().toString();
        String ageText = ageEditText.getText().toString();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = (selectedGenderButton != null) ? selectedGenderButton.getText().toString() : null;
        String activity = activitySpinner.getSelectedItem().toString();

        // 입력 확인
        if (heightText.isEmpty() || weightText.isEmpty() || ageText.isEmpty() || gender == null) {
            Toast.makeText(this, "모든 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 데이터 변환
        double height = Double.parseDouble(heightText);
        double weight = Double.parseDouble(weightText);
        int age = Integer.parseInt(ageText);
        int genderToDB = gender.equals("남성") ? 1 : 2;

        int activityToDB = 5;
        switch (activity) {
            case "매우 활동적":
                activityToDB = 5;
                break;
            case "활동적":
                activityToDB = 4;
                break;
            case "보통 활동적":
                activityToDB = 3;
                break;
            case "적게 활동적":
                activityToDB = 2;
                break;
            case "거의 활동하지 않음":
                activityToDB = 1;
                break;
        }

        // 찌개 및 전골, 국 및 탕 순
        int taste1 = getSelected(stewRadioGroup, R.id.radioCasserole, 1,  R.id.radioSoup, 2);

        // 튀김, 구이, 볶음, 찜, 전·적 및 부침, 조림
        int taste2 = getSelected(sideDishRadioGroup,
                R.id.radioFried, 11, R.id.radioGrilled, 12, R.id.radioStirFried, 13,
                R.id.radioSteam, 14, R.id.radioKP, 25, R.id.radioBraised, 26);

        // SQLite 데이터 저장
        userDbHelper.insertUser(height, weight, age, genderToDB, activityToDB, taste1, taste2);
        Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();

        //JSON format 저장
        saveDataAsJson();

        // Logcat으로 저장된 데이터 확인
        Cursor cursor = userDbHelper.getUserInfo();
        if (cursor.moveToLast()) {
            @SuppressLint("Range") double savedHeight = cursor.getDouble(cursor.getColumnIndex("height"));
            @SuppressLint("Range") double savedWeight = cursor.getDouble(cursor.getColumnIndex("weight"));
            @SuppressLint("Range") int savedAge = cursor.getInt(cursor.getColumnIndex("age"));
            @SuppressLint("Range") int savedGender = cursor.getInt(cursor.getColumnIndex("gender"));
            @SuppressLint("Range") int savedActivity = cursor.getInt(cursor.getColumnIndex("activity"));

            Log.i("DB_update", "Saved Data -> Height: " + savedHeight + ", Weight: " + savedWeight +
                    ", Age: " + savedAge + ", Gender: " + genderToDB + ", Activity: " + activityToDB +
                    ", Taste1: " + taste1 + ", Taste2: " + taste2);
        }
        cursor.close();

        // 메인 화면으로 이동
        Intent intent = new Intent(UserInformActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // 선택된 RadioButton이 가지고 있는 값을 반환 KEY:VALUE
    private int getSelected(RadioGroup group, int... mapping) {
        int selectedId = group.getCheckedRadioButtonId();
        for (int i = 0; i < mapping.length; i += 2) {
            if (mapping[i] == selectedId) {
                return mapping[i + 1];
            }
        }
        return 0;
    }

    private void saveDataAsJson() {
        Cursor cursor = userDbHelper.getUserInfo();
        if (cursor != null) {
            JSONArray userArray = new JSONArray();

            while (cursor.moveToNext()) {
                try {
                    JSONObject userObject = new JSONObject();
                    @SuppressLint("Range") double height = cursor.getDouble(cursor.getColumnIndex("height"));
                    @SuppressLint("Range") double weight = cursor.getDouble(cursor.getColumnIndex("weight"));
                    @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex("age"));
                    @SuppressLint("Range") int gender = cursor.getInt(cursor.getColumnIndex("gender"));
                    @SuppressLint("Range") int activity = cursor.getInt(cursor.getColumnIndex("activity"));
                    @SuppressLint("Range") int taste1 = cursor.getInt(cursor.getColumnIndex("taste1"));
                    @SuppressLint("Range") int taste2 = cursor.getInt(cursor.getColumnIndex("taste2"));

                    userObject.put("height", height);
                    userObject.put("weight", weight);
                    userObject.put("age", age);
                    userObject.put("gender", gender);
                    userObject.put("activity", activity);
                    userObject.put("taste1", taste1);
                    userObject.put("taste2", taste2);

                    userArray.put(userObject); // JSONArray에 추가
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            // JSON 데이터를 파일로 저장
            saveJsonToFile(userArray);
        }
    }

    private void saveJsonToFile(JSONArray userArray) {
        String fileName = "user_data.json";
        String jsonString = userArray.toString();

        try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(jsonString.getBytes());
            Log.i("JSON_SAVE", "JSON 파일이 저장되었습니다: " + fileName);
        } catch (IOException e) {
            Log.e("JSON_SAVE", "파일 저장 실패", e);
        }
    }
}