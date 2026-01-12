# CameraUse Module

## 简介 / Introduction

### 中文

CameraUse 是一个用于测试设备摄像头的 Android 模块。该模块支持检测和打开设备上的多个摄像头（包括前置、后置和外接摄像头），并可以在不同摄像头之间进行切换。

### English

CameraUse is an Android module for testing device cameras. This module supports detecting and opening multiple cameras on the device (including front, back, and external cameras), and can switch between different cameras.

## 功能特性 / Features

### 中文

- ✅ **自动检测所有摄像头**：自动扫描并识别设备上的所有可用摄像头
- ✅ **多摄像头支持**：支持前置摄像头、后置摄像头和外接摄像头
- ✅ **实时预览**：提供流畅的摄像头实时画面预览
- ✅ **摄像头切换**：一键切换不同的摄像头
- ✅ **信息显示**：显示当前摄像头的详细信息（ID、类型、序号）
- ✅ **权限管理**：自动请求和管理摄像头权限

### English

- ✅ **Auto-detect all cameras**：Automatically scans and identifies all available cameras on the device
- ✅ **Multi-camera support**：Supports front, back, and external cameras
- ✅ **Real-time preview**：Provides smooth real-time camera preview
- ✅ **Camera switching**：Switch between different cameras with one click
- ✅ **Information display**：Displays detailed information about the current camera (ID, type, index)
- ✅ **Permission management**：Automatically requests and manages camera permissions

## 技术实现 / Technical Implementation

### 中文

- **Camera2 API**：使用 Android Camera2 API 进行摄像头操作
- **TextureView**：使用自定义的 AutoFitTextureView 实现预览显示
- **生命周期管理**：正确处理 Activity 生命周期，确保资源正确释放

### English

- **Camera2 API**：Uses Android Camera2 API for camera operations
- **TextureView**：Uses custom AutoFitTextureView for preview display
- **Lifecycle management**：Properly handles Activity lifecycle to ensure correct resource release

## 使用方法 / Usage

### 中文

#### 1. 运行应用

直接运行 `camerause` 模块，应用会自动启动。

#### 2. 授予权限

首次运行时，应用会请求摄像头权限，请点击"允许"。

#### 3. 查看摄像头预览

权限授予后，应用会自动打开第一个检测到的摄像头并显示预览画面。

#### 4. 切换摄像头

点击界面底部的"switch camera"按钮，可以在所有检测到的摄像头之间循环切换。

#### 5. 查看摄像头信息

界面顶部会显示当前摄像头的详细信息：
- 摄像头序号（当前/总数）
- 摄像头 ID
- 摄像头类型（前置/后置/外接）

### English

#### 1. Run the Application

Run the `camerause` module directly, and the application will start automatically.

#### 2. Grant Permissions

On first run, the application will request camera permission. Please click "Allow".

#### 3. View Camera Preview

After permission is granted, the application will automatically open the first detected camera and display the preview.

#### 4. Switch Cameras

Click the "switch camera" button at the bottom of the interface to cycle through all detected cameras.

#### 5. View Camera Information

The top of the interface displays detailed information about the current camera:
- Camera index (current/total)
- Camera ID
- Camera type (front/back/external)

## 系统要求 / System Requirements

### 中文

- **最低 Android 版本**：Android 11 (API 30)
- **目标 Android 版本**：Android 14 (API 36)
- **编译 SDK 版本**：36
- **硬件要求**：设备必须支持 Camera2 API

### English

- **Minimum Android Version**：Android 11 (API 30)
- **Target Android Version**：Android 14 (API 36)
- **Compile SDK Version**：36
- **Hardware Requirements**：Device must support Camera2 API

## 项目结构 / Project Structure

```
camerause/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/xie/camerause/
│       │       ├── MainActivity.java          # 主活动，实现摄像头功能
│       │       └── AutoFitTextureView.java    # 自定义预览视图
│       ├── res/
│       │   └── layout/
│       │       └── activity_main.xml           # 主界面布局
│       └── AndroidManifest.xml                 # 应用清单文件
└── README.md                                   # 本文件
```

## 注意事项 / Notes

### 中文

1. **权限要求**：应用需要摄像头权限才能正常工作
2. **外接摄像头**：如果设备连接了外接 USB 摄像头，应用会自动检测并显示在摄像头列表中
3. **资源释放**：应用会在 Activity 暂停时自动关闭摄像头以释放资源
4. **摄像头数量**：如果设备只有一个摄像头，切换按钮仍然可用，但会显示提示信息

### English

1. **Permission Requirements**：The application requires camera permission to function properly
2. **External Cameras**：If the device is connected to an external USB camera, the application will automatically detect and display it in the camera list
3. **Resource Release**：The application will automatically close the camera when the Activity is paused to release resources
4. **Camera Count**：If the device has only one camera, the switch button is still available but will show a prompt message

## 常见问题 / FAQ

### 中文

**Q: 为什么看不到摄像头预览？**  
A: 请检查是否已授予摄像头权限，并确保设备支持 Camera2 API。

**Q: 如何知道当前使用的是哪个摄像头？**  
A: 查看界面顶部显示的摄像头信息，包括 ID 和类型。

**Q: 支持同时打开多个摄像头吗？**  
A: 当前版本不支持同时打开多个摄像头，只能一次打开一个并进行切换。

### English

**Q: Why can't I see the camera preview?**  
A: Please check if camera permission has been granted and ensure the device supports Camera2 API.

**Q: How do I know which camera is currently being used?**  
A: Check the camera information displayed at the top of the interface, including ID and type.

**Q: Does it support opening multiple cameras simultaneously?**  
A: The current version does not support opening multiple cameras simultaneously, only one camera can be opened at a time and switched between.

## 开发者信息 / Developer Information

### 中文

- **模块名称**：CameraUse
- **包名**：com.xie.camerause
- **应用 ID**：com.xie.camerause

### English

- **Module Name**：CameraUse
- **Package Name**：com.xie.camerause
- **Application ID**：com.xie.camerause

## 更新日志 / Changelog

### 中文

**v1.0.0** (初始版本)
- 实现基本的摄像头检测和打开功能
- 支持多摄像头切换
- 添加摄像头信息显示
- 实现权限管理

### English

**v1.0.0** (Initial Version)
- Implemented basic camera detection and opening functionality
- Support for multi-camera switching
- Added camera information display
- Implemented permission management

