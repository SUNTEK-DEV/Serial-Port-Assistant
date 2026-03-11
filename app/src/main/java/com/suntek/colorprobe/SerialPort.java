package com.suntek.colorprobe;

public class SerialPort {
    static {
        System.loadLibrary("serialport");
    }

    public native int open(String path, int baudrate);
    public native int close();
    public native int write(byte[] buffer);
    public native byte[] read(int maxSize);
}

