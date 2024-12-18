package com.example.nutrifit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.doinglab.foodlens.sdk.FoodLens;
import com.doinglab.foodlens.sdk.NetworkService;
import com.doinglab.foodlens.sdk.errors.BaseError;
import com.doinglab.foodlens.sdk.network.model.RecognitionResult;
import com.doinglab.foodlens.sdk.RecognizeResultHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class   CameraActivity extends AppCompatActivity {

    private Button cameraButton, galleryButton, sendButton;
    private ImageView imageView;

    // 카메라 및 갤러리 권한 요청 런처
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private byte[] imageByteData;
    private Uri photoUri;
    private static final String TAG_E = "ERROR";
    private static final String TAG_S = "SUCCESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        sendButton = findViewById(R.id.sendButton);

        // Send 버튼 비활성화
        sendButton.setEnabled(false);

        // 권한 요청 런처 초기화
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        openCamera(); // 권한이 승인되면 카메라 열기
                    } else {
                        Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 카메라 런처 초기화
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        // 사진 촬영 후 image view에 이미지 표시
                        imageView.setImageURI(photoUri);
                        imageByteData = convertUriToByteArray(photoUri); // Byte array로 변환
                        sendButton.setEnabled(true);
                    } else {
                        Log.e(TAG_E, "카메라 종료.");
                    }
                }
        );

        // 갤러리 버튼 클릭리스너
        galleryButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Send 버튼 클릭리스너
        sendButton.setOnClickListener(v -> {
            if (imageByteData != null) {
                sendImageToFoodLens(imageByteData);
            } else {
                Toast.makeText(this, "사진이 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 갤러리에서 사진 선택
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> handleImageSelection(null, uri)
        );

        // 카메라 버튼 클릭 시 권한이 있다면 카메라 실행, 없다면 권한 요청
        cameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void openCamera() {
        // 이미지 파일 생성 하고 촬영된 이미지를 photoFile에 저장
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.i(TAG_S, "photoFile opened.");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "촬영 실패.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            // FileProvider를 통해 사진의 위치(Uri) 얻기
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(photoUri); // 카메라 실행
        }
    }

    // 이미지 파일 생성
    private File createImageFile() throws IOException {
        // 사진이 촬영된 시간을 파일명으로 사진 파일 생성
        String imageFileName =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir("Pictures");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        Log.i(TAG_S, "image file 생성.");
        return image;
    }

    // 사진 촬영 후 저장된 사진의 bitmap 혹은 갤러리에서 선택된 사진의 uri를 받고 메서드 호출
    private void handleImageSelection(Bitmap bitmap, Uri uri) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageByteData = convertBitmapToByteArray(bitmap);
        } else if (uri != null) {
            imageView.setImageURI(uri);
            imageByteData = convertUriToByteArray(uri);
        }

        if (imageByteData != null) {
            Log.i(TAG_S, "이미지 선택 완료.");
            sendButton.setEnabled(true);
        } else {
            sendButton.setEnabled(false);
        }
    }

    // 이미지가 저장된 Bitmap을 byte array로 변환
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] convertUriToByteArray(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            return convertBitmapToByteArray(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // FoodLens API로 사진 전송
    private void sendImageToFoodLens(byte[] imageData) {
        NetworkService networkService = FoodLens.createNetworkService(getApplicationContext());

        networkService.predictMultipleFood(imageData, new RecognizeResultHandler() {
            @Override
            public void onSuccess(RecognitionResult result) {
                String resultJson = result.toJSONString();
                Intent intent = new Intent(CameraActivity.this, CameraResultActivity.class);
                intent.putExtra("result_json", resultJson);
                startActivity(intent);
            }

            @Override
            public void onError(BaseError error) {
                Log.e(TAG_E, "FoodLens API 호출 실패: " + error.getMessage());
            }
        });
    }
}