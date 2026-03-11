package com.suntek.camerause;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    
    private AutoFitTextureView textureView;
    private Button btnSwitchCamera;
    private TextView tvCameraInfo;
    
    private CameraManager cameraManager;
    private String[] cameraIds;
    private List<String> availableCameraIds;
    private int currentCameraIndex = 0;
    
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            // 处理尺寸变化
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            // 处理纹理更新
        }
    };
    
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            Toast.makeText(MainActivity.this, "Failed to turn on the camera: " + error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews();
        initCamera();
        checkCameraPermission();
    }
    
    private void initViews() {
        textureView = findViewById(R.id.textureView);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        tvCameraInfo = findViewById(R.id.tvCameraInfo);
        
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
    }
    
    private void initCamera() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraIds = cameraManager.getCameraIdList();
            availableCameraIds = new ArrayList<>();
            
            // 查找所有可用的摄像头
            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                // 收集所有摄像头（包括前置、后置和外接摄像头）
                availableCameraIds.add(cameraId);
            }
            
            if (availableCameraIds.isEmpty()) {
                Toast.makeText(this, "No available camera found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            updateCameraInfo();
            
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to access the camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            startBackgroundThread();
            if (textureView.isAvailable()) {
                openCamera();
            } else {
                textureView.setSurfaceTextureListener(textureListener);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBackgroundThread();
                if (textureView.isAvailable()) {
                    openCamera();
                } else {
                    textureView.setSurfaceTextureListener(textureListener);
                }
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    
    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void openCamera() {
        if (availableCameraIds == null || availableCameraIds.isEmpty()) {
            Toast.makeText(this, "No cameras available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentCameraIndex >= availableCameraIds.size()) {
            currentCameraIndex = 0;
        }
        
        String cameraId = availableCameraIds.get(currentCameraIndex);
        
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            
            if (map == null) {
                return;
            }
            
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            Size previewSize = chooseOptimalSize(sizes, textureView.getWidth(), textureView.getHeight());
            
            textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            updateCameraInfo();
            
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to turn on the camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }
    
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
    
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                return;
            }
            
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);
            
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            
            cameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) {
                                return;
                            }
                            
                            captureSession = session;
                            try {
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                CaptureRequest captureRequest = captureRequestBuilder.build();
                                captureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(MainActivity.this, "Configuration failed", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
            
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    
    private void switchCamera() {
        if (availableCameraIds == null || availableCameraIds.size() < 2) {
            Toast.makeText(this, "At least two cameras are required for switching", Toast.LENGTH_SHORT).show();
            return;
        }
        
        closeCamera();
        currentCameraIndex = (currentCameraIndex + 1) % availableCameraIds.size();
        openCamera();
    }
    
    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
    
    private void updateCameraInfo() {
        if (availableCameraIds == null || availableCameraIds.isEmpty()) {
            tvCameraInfo.setText("Camera not found");
            return;
        }
        
        try {
            String cameraId = availableCameraIds.get(currentCameraIndex);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            
            String facingStr = "null";
            if (facing != null) {
                switch (facing) {
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        facingStr = "front";
                        break;
                    case CameraCharacteristics.LENS_FACING_BACK:
                        facingStr = "postposition";
                        break;
                    case CameraCharacteristics.LENS_FACING_EXTERNAL:
                        facingStr = "external connection";
                        break;
                }
            }
            
            String info = String.format("camera %d/%d\nID: %s\ntype: %s",
                    currentCameraIndex + 1,
                    availableCameraIds.size(),
                    cameraId,
                    facingStr);
            
            tvCameraInfo.setText(info);
            
        } catch (CameraAccessException e) {
            e.printStackTrace();
            tvCameraInfo.setText("Failed to obtain camera information");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    
    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
}