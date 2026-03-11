#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <errno.h>

#define LOG_TAG "SerialPort"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static int s_fd = -1;

extern "C" {

JNIEXPORT jint JNICALL
Java_com_suntek_colorprobe_SerialPort_open(JNIEnv *env, jobject thiz, jstring path, jint baudrate) {
    const char *path_utf = env->GetStringUTFChars(path, nullptr);
    if (path_utf == nullptr) {
        return -1;
    }

    // 保存路径字符串用于日志
    std::string port_path(path_utf);
    
    // 打开串口设备
    s_fd = open(path_utf, O_RDWR | O_NOCTTY | O_NDELAY);

    if (s_fd == -1) {
        LOGE("Cannot open port %s: %s", path_utf, strerror(errno));
        env->ReleaseStringUTFChars(path, path_utf);
        return -1;
    }
    
    env->ReleaseStringUTFChars(path, path_utf);

    // 配置串口参数
    struct termios options;
    if (tcgetattr(s_fd, &options) != 0) {
        LOGE("Cannot get port attributes: %s", strerror(errno));
        close(s_fd);
        s_fd = -1;
        return -1;
    }

    // 设置波特率
    speed_t speed;
    switch (baudrate) {
        case 9600:
            speed = B9600;
            break;
        case 19200:
            speed = B19200;
            break;
        case 38400:
            speed = B38400;
            break;
        case 57600:
            speed = B57600;
            break;
        case 115200:
            speed = B115200;
            break;
        default:
            speed = B9600;
            break;
    }

    cfsetispeed(&options, speed);
    cfsetospeed(&options, speed);

    // 8N1: 8数据位, 无校验, 1停止位
    options.c_cflag &= ~PARENB;    // 无校验
    options.c_cflag &= ~CSTOPB;    // 1停止位
    options.c_cflag &= ~CSIZE;     // 清除数据位设置
    options.c_cflag |= CS8;        // 8数据位
    options.c_cflag |= (CLOCAL | CREAD); // 本地连接, 接收使能
    options.c_cflag &= ~CRTSCTS;   // 无硬件流控

    // 输入模式
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG); // 原始模式
    options.c_iflag &= ~(IXON | IXOFF | IXANY); // 无软件流控
    options.c_iflag &= ~(INLCR | IGNCR | ICRNL); // 不转换回车换行

    // 输出模式
    options.c_oflag &= ~OPOST; // 原始输出

    // 设置超时
    options.c_cc[VMIN] = 0;  // 非阻塞读取
    options.c_cc[VTIME] = 10; // 100ms超时

    // 应用设置
    if (tcsetattr(s_fd, TCSANOW, &options) != 0) {
        LOGE("Cannot set port attributes: %s", strerror(errno));
        close(s_fd);
        s_fd = -1;
        return -1;
    }

    LOGI("Serial port opened successfully: %s", port_path.c_str());
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_suntek_colorprobe_SerialPort_close(JNIEnv *env, jobject thiz) {
    if (s_fd != -1) {
        close(s_fd);
        s_fd = -1;
        LOGI("Serial port closed");
        return 0;
    }
    return -1;
}

JNIEXPORT jint JNICALL
Java_com_suntek_colorprobe_SerialPort_write(JNIEnv *env, jobject thiz, jbyteArray buffer) {
    if (s_fd == -1) {
        LOGE("Port not opened");
        return -1;
    }

    jsize len = env->GetArrayLength(buffer);
    jbyte *buf = env->GetByteArrayElements(buffer, nullptr);
    if (buf == nullptr) {
        return -1;
    }

    int result = write(s_fd, buf, len);
    env->ReleaseByteArrayElements(buffer, buf, 0);

    if (result < 0) {
        LOGE("Write error: %s", strerror(errno));
        return -1;
    }

    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_suntek_colorprobe_SerialPort_read(JNIEnv *env, jobject thiz, jint maxSize) {
    if (s_fd == -1) {
        LOGE("Port not opened");
        return nullptr;
    }

    jbyte *buffer = new jbyte[maxSize];
    int len = read(s_fd, buffer, maxSize);

    if (len < 0) {
        if (errno != EAGAIN && errno != EWOULDBLOCK) {
            LOGE("Read error: %s", strerror(errno));
        }
        delete[] buffer;
        return nullptr;
    }

    if (len == 0) {
        delete[] buffer;
        return nullptr;
    }

    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, buffer);
    delete[] buffer;

    return result;
}

} // extern "C"

