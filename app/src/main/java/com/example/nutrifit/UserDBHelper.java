package com.example.nutrifit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_info.db";
    private static final int DATABASE_VERSION = 1;

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private static final String TABLE_NAME = "user_info";
    private static final String COLUMN_HEIGHT = "height";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_ACTIVITY = "activity";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_HEIGHT + " REAL NOT NULL, " +
                    COLUMN_WEIGHT + " REAL NOT NULL, " +
                    COLUMN_AGE + " INTEGER NOT NULL, " +
                    COLUMN_GENDER + " TEXT NOT NULL, " +
                    COLUMN_ACTIVITY + " TEXT NOT NULL)";

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //입력된 user 정보를 DB에 저장
    public void insertUser(double height, double weight, int age, String gender, String activity) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 기존 테이블 초기화
        db.execSQL("DELETE FROM " + TABLE_NAME);

        ContentValues values = new ContentValues();
        values.put("height", height);
        values.put("weight", weight);
        values.put("age", age);
        values.put("gender", gender);
        values.put("activity", activity);

        db.insert("user_info", null, values);
        db.close();
    }

    public Cursor getUserInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
