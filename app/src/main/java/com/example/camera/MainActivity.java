package com.example.camera;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

public class MainActivity extends ComponentActivity {
    private final String IMAGE_PREFIX = "MyImage_";
    private String JPG_SUFFIX = ".jpg";
    private String FOLDER_NAME = "MyPhoto";
    private String cameraPermission = "android.permission.CAMERA";
    private String storagePermission = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final String[] REQUIRED_PERMISSIONS = new String[]{cameraPermission, storagePermission};

    private ImageCapture capture;
    private PreviewView cameraView;
    private ImageView preview;
    private Button button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        cameraView = findViewById(R.id.camera);
        preview = findViewById(R.id.preview);
        button = findViewById(R.id.button);
        button.setOnClickListener(v -> takePhoto());
    }

    private void takePhoto() {
        File folder = new File(
                this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsoluteFile()
                        + File.separator
                        + FOLDER_NAME
        );
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(
                this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsoluteFile()
                        + File.separator
                        + FOLDER_NAME
                        + File.separator
                        + IMAGE_PREFIX
                        + System.currentTimeMillis()
                        + JPG_SUFFIX
        );
        ImageCapture.OutputFileOptions outputOption = new ImageCapture.OutputFileOptions.Builder(file).build();

        capture.takePicture(
                outputOption,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(new File(outputFileResults.getSavedUri().getPath()));

                        Log.d("loggg", "savedUri " + savedUri);
                        preview.setImageURI(savedUri);
                        cameraView.setVisibility(View.GONE);
                        button.setVisibility(View.GONE);
                        preview.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                    }
                }
        );
    }

    private void takePhotoWithoutSaving() {
        capture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        preview.setImageBitmap(image.toBitmap());

                        cameraView.setVisibility(View.GONE);
                        button.setVisibility(View.GONE);
                        preview.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                    }
                }
        );
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraView.getSurfaceProvider());
                capture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        MainActivity.this,
                        cameraSelector,
                        preview,
                        capture
                );
            } catch (Throwable t) {}
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 123);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
