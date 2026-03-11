package com.suntek.colorprobe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner portSpinner;
    private EditText baudrateEdit;
    private Button openCloseBtn;
    private TextView receiveTextView;
    private TextView statusTextView;
    private Button clearReceiveBtn;
    private Button sendBtn;
    private Button sendHexBtn;
    private EditText sendEditText;
    private Button ledColorBtn;

    private SerialPort serialPort;
    private boolean isOpen = false;
    private Handler handler;
    private Thread readThread;
    private boolean shouldRead = false;

    private String[] ports = {"/dev/ttyS5", "/dev/ttyS11"};
    private boolean sendHexMode = false;

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
        handler = new Handler(Looper.getMainLooper());
        serialPort = new SerialPort();

        // 检查端口是否可用
        checkPorts();
    }

    private void initViews() {
        portSpinner = findViewById(R.id.portSpinner);
        baudrateEdit = findViewById(R.id.baudrateEdit);
        openCloseBtn = findViewById(R.id.openCloseBtn);
        receiveTextView = findViewById(R.id.receiveTextView);
        statusTextView = findViewById(R.id.statusTextView);
        clearReceiveBtn = findViewById(R.id.clearReceiveBtn);
        sendBtn = findViewById(R.id.sendBtn);
        sendHexBtn = findViewById(R.id.sendHexBtn);
        sendEditText = findViewById(R.id.sendEditText);
        ledColorBtn = findViewById(R.id.ledColorBtn);

        openCloseBtn.setOnClickListener(v -> toggleSerialPort());
        clearReceiveBtn.setOnClickListener(v -> clearReceive());
        sendBtn.setOnClickListener(v -> sendData());
        sendHexBtn.setOnClickListener(v -> toggleHexMode());
        ledColorBtn.setOnClickListener(v -> openLedColorController());
    }

    private void checkPorts() {
        // 检查端口文件是否存在
        ArrayList<String> availablePorts = new ArrayList<>();
        String[] portLabels = {"LED light controller", "scanner"}; // 端口用途标签
        
        for (int i = 0; i < ports.length; i++) {
            String port = ports[i];
            String label = portLabels[i];
            File file = new File(port);
            if (file.exists()) {
                availablePorts.add(port + " - " + label + " (available)");
            } else {
                availablePorts.add(port + " - " + label + " (Unavailable)");
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availablePorts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        portSpinner.setAdapter(adapter);
        portSpinner.setSelection(0); // 默认选择第一个
    }

    private void toggleSerialPort() {
        if (!isOpen) {
            openSerialPort();
        } else {
            closeSerialPort();
        }
    }

    private void openSerialPort() {
        Object selectedItem = portSpinner.getSelectedItem();
        String portPath = selectedItem != null ? selectedItem.toString() : "";
        
        // 移除可能的后缀 "(可用)" 或 "(不可用)"
        for (String p : ports) {
            if (portPath.contains(p)) {
                portPath = p;
                break;
            }
        }
        
        if (!portPath.startsWith("/dev/ttyS")) {
            portPath = ports[0]; // 默认使用第一个
        }

        String baudrateStr = baudrateEdit.getText().toString().trim();
        int baudrate = 9600;
        try {
            baudrate = Integer.parseInt(baudrateStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "The baud rate is invalid. Using the default 9600", Toast.LENGTH_SHORT).show();
        }

        // 检查设备文件是否存在
        File portFile = new File(portPath);
        if (!portFile.exists()) {
            Toast.makeText(this, "port " + portPath + "Does not exist or does not have access permission", Toast.LENGTH_LONG).show();
            updateStatus("Error: Port unavailable");
            return;
        }

        int result = serialPort.open(portPath, baudrate);
        if (result == 0) {
            isOpen = true;
            shouldRead = true;
            openCloseBtn.setText("Close the serial port");
            portSpinner.setEnabled(false);
            baudrateEdit.setEnabled(false);
            sendBtn.setEnabled(true);
            sendHexBtn.setEnabled(true);
            updateStatus("Connected: " + portPath + " @ " + baudrate + " baud");
            Toast.makeText(this, "Serial port opened successfully", Toast.LENGTH_SHORT).show();

            // 启动读取线程
            startReadThread();
        } else {
            Toast.makeText(this, "Failed to open the serial port. Please check permissions", Toast.LENGTH_LONG).show();
            updateStatus("Error: Opening failed");
        }
    }

    private void closeSerialPort() {
        shouldRead = false;
        
        if (readThread != null) {
            try {
                readThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (serialPort != null) {
            serialPort.close();
        }

        isOpen = false;
        openCloseBtn.setText("Open the serial port");
        portSpinner.setEnabled(true);
        baudrateEdit.setEnabled(true);
        sendBtn.setEnabled(false);
        sendHexBtn.setEnabled(false);
        updateStatus("Not connected");
        Toast.makeText(this, "The serial port is closed", Toast.LENGTH_SHORT).show();
    }

    private void startReadThread() {
        readThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (shouldRead && isOpen) {
                try {
                    byte[] data = serialPort.read(buffer.length);
                    if (data != null && data.length > 0) {
                        final byte[] finalData = data;
                        handler.post(() -> appendReceiveData(finalData));
                    }
                    Thread.sleep(10); // 避免CPU占用过高
                } catch (Exception e) {
                    if (shouldRead) {
                        handler.post(() -> {
                            appendText("\nRead error: " + e.getMessage());
                            updateStatus("Read error");
                        });
                    }
                    break;
                }
            }
        });
        readThread.start();
    }

    private void appendReceiveData(byte[] data) {
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
        
        if (sendHexMode) {
            // Hex模式显示
            StringBuilder hexStr = new StringBuilder();
            for (byte b : data) {
                hexStr.append(String.format("%02X ", b));
            }
            appendText("\n[" + timestamp + "] RX: " + hexStr.toString().trim());
        } else {
            // 文本模式显示
            String text = new String(data, StandardCharsets.UTF_8);
            // 显示可打印字符，不可打印的显示为十六进制
            StringBuilder display = new StringBuilder();
            for (byte b : data) {
                if (b >= 32 && b < 127) {
                    display.append((char) b);
                } else {
                    display.append(String.format("[%02X]", b));
                }
            }
            appendText("\n[" + timestamp + "] RX: " + display.toString());
        }
    }

    private void appendText(String text) {
        String current = receiveTextView.getText().toString();
        if ("Waiting for data...".equals(current)) {
            receiveTextView.setText(text);
        } else {
            receiveTextView.append(text);
        }
        // 自动滚动到底部
        receiveTextView.post(() -> {
            int scrollAmount = receiveTextView.getLayout().getLineTop(receiveTextView.getLineCount()) - receiveTextView.getHeight();
            if (scrollAmount > 0) {
                receiveTextView.scrollTo(0, scrollAmount);
            }
        });
    }

    private void clearReceive() {
        receiveTextView.setText("Waiting for data...");
    }

    private void toggleHexMode() {
        sendHexMode = !sendHexMode;
        sendHexBtn.setText(sendHexMode ? "text sending" : "Hex send");
        Toast.makeText(this, sendHexMode ? "Switched to Hex mode" : "Switched to text mode", Toast.LENGTH_SHORT).show();
    }

    private void sendData() {
        if (!isOpen) {
            Toast.makeText(this, "Please open the serial port first", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataStr = sendEditText.getText().toString().trim();
        if (TextUtils.isEmpty(dataStr)) {
            Toast.makeText(this, "Please enter the data to be sent", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] dataToSend;
        try {
            if (sendHexMode) {
                // Hex模式：将字符串解析为十六进制字节
                dataToSend = hexStringToByteArray(dataStr.replaceAll("\\s+", ""));
                if (dataToSend == null) {
                    Toast.makeText(this, "Hex format error", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // 文本模式
                dataToSend = dataStr.getBytes(StandardCharsets.UTF_8);
            }

            int result = serialPort.write(dataToSend);
            if (result >= 0) {
                String timestamp = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                
                if (sendHexMode) {
                    StringBuilder hexStr = new StringBuilder();
                    for (byte b : dataToSend) {
                        hexStr.append(String.format("%02X ", b));
                    }
                    appendText("\n[" + timestamp + "] TX: " + hexStr.toString().trim());
                } else {
                    appendText("\n[" + timestamp + "] TX: " + dataStr);
                }
                
                Toast.makeText(this, "Sent successfully (" + result + " byte)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
                updateStatus("Failed to send");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            updateStatus("Failed to send: " + e.getMessage());
        }
    }

    private byte[] hexStringToByteArray(String hex) {
        try {
            int len = hex.length();
            if (len % 2 != 0) {
                return null;
            }
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateStatus(String status) {
        statusTextView.setText("status: " + status);
    }

    private void openLedColorController() {
        Intent intent = new Intent(MainActivity.this, LedColorActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isOpen) {
            closeSerialPort();
        }
    }
}
