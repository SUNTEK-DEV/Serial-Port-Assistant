package com.suntek.colorprobe;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LedColorActivity extends AppCompatActivity {

    // UI Components
    private Spinner portSpinner;
    private MaterialButton openCloseBtn;
    private TextView statusTextView;
    private View colorPreview;
    private ScrollView mainScrollView;
    private SeekBar hueSeekBar;
    private SeekBar saturationSeekBar;
    private SeekBar brightnessSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private TextView hueValueText;
    private TextView saturationValueText;
    private TextView brightnessValueText;
    private TextView redValueText;
    private TextView greenValueText;
    private TextView blueValueText;
    private TextInputEditText hexEditText;
    private MaterialButton sendBtn;
    private MaterialButton offBtn;
    private TextView sendResultText;
    private ImageButton redColorBtn;
    private ImageButton greenColorBtn;
    private ImageButton blueColorBtn;
    private ImageButton whiteColorBtn;
    private ImageButton yellowColorBtn;
    private ImageButton cyanColorBtn;
    private ImageButton magentaColorBtn;
    private ImageButton offColorBtn;

    // Target color components
    private TextInputEditText targetColorEdit;
    private MaterialButton applyTargetColorBtn;
    private View targetColorPreview;
    private TextView targetColorHexText;
    private MaterialButton swapToRgbBtn;
    private View targetCompareView;
    private View ledCompareView;
    private TextView colorDiffText;

    // Serial port
    private SerialPort serialPort;
    private boolean isOpen = false;
    private String[] ports = {"/dev/ttyS5", "/dev/ttyS11"};

    // Current color values
    private int currentRed = 255;
    private int currentGreen = 0;
    private int currentBlue = 0;
    private float currentHue = 0f;
    private float currentSaturation = 1f;
    private float currentBrightness = 1f;

    // Target color values
    private int targetRed = 255;
    private int targetGreen = 0;
    private int targetBlue = 0;

    // Debounce handler
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable sendColorRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_color);

        initViews();
        setupListeners();
        serialPort = new SerialPort();
        checkPorts();

        // Initialize with red color
        updateColorFromRGB(255, 0, 0);

        // Initialize target color (also red by default)
        targetRed = 255;
        targetGreen = 0;
        targetBlue = 0;
        applyTargetColor();
    }

    private void initViews() {
        portSpinner = findViewById(R.id.portSpinner);
        openCloseBtn = findViewById(R.id.openCloseBtn);
        statusTextView = findViewById(R.id.statusTextView);
        colorPreview = findViewById(R.id.colorPreview);
        mainScrollView = findViewById(R.id.mainScrollView);
        hueSeekBar = findViewById(R.id.hueSeekBar);
        saturationSeekBar = findViewById(R.id.saturationSeekBar);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        redSeekBar = findViewById(R.id.redSeekBar);
        greenSeekBar = findViewById(R.id.greenSeekBar);
        blueSeekBar = findViewById(R.id.blueSeekBar);
        hueValueText = findViewById(R.id.hueValueText);
        saturationValueText = findViewById(R.id.saturationValueText);
        brightnessValueText = findViewById(R.id.brightnessValueText);
        redValueText = findViewById(R.id.redValueText);
        greenValueText = findViewById(R.id.greenValueText);
        blueValueText = findViewById(R.id.blueValueText);
        hexEditText = findViewById(R.id.hexEditText);
        sendBtn = findViewById(R.id.sendBtn);
        offBtn = findViewById(R.id.offBtn);
        sendResultText = findViewById(R.id.sendResultText);
        redColorBtn = findViewById(R.id.redColorBtn);
        greenColorBtn = findViewById(R.id.greenColorBtn);
        blueColorBtn = findViewById(R.id.blueColorBtn);
        whiteColorBtn = findViewById(R.id.whiteColorBtn);
        yellowColorBtn = findViewById(R.id.yellowColorBtn);
        cyanColorBtn = findViewById(R.id.cyanColorBtn);
        magentaColorBtn = findViewById(R.id.magentaColorBtn);
        offColorBtn = findViewById(R.id.offColorBtn);

        // Target color views
        targetColorEdit = findViewById(R.id.targetColorEdit);
        applyTargetColorBtn = findViewById(R.id.applyTargetColorBtn);
        targetColorPreview = findViewById(R.id.targetColorPreview);
        targetColorHexText = findViewById(R.id.targetColorHexText);
        swapToRgbBtn = findViewById(R.id.swapToRgbBtn);
        targetCompareView = findViewById(R.id.targetCompareView);
        ledCompareView = findViewById(R.id.ledCompareView);
        colorDiffText = findViewById(R.id.colorDiffText);
    }

    private void setupListeners() {
        // Serial port
        openCloseBtn.setOnClickListener(v -> toggleSerialPort());

        // HSB sliders
        hueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentHue = progress;
                    updateColorFromHSB();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        saturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentSaturation = progress / 100f;
                    updateColorFromHSB();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentBrightness = progress / 100f;
                    updateColorFromHSB();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // RGB sliders
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateColorFromRGB(progress, greenSeekBar.getProgress(), blueSeekBar.getProgress());
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateColorFromRGB(redSeekBar.getProgress(), progress, blueSeekBar.getProgress());
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateColorFromRGB(redSeekBar.getProgress(), greenSeekBar.getProgress(), progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Send buttons
        sendBtn.setOnClickListener(v -> sendColorToLED());
        offBtn.setOnClickListener(v -> sendOffCommand());

        // Quick color buttons
        redColorBtn.setOnClickListener(v -> setQuickColor(255, 0, 0));
        greenColorBtn.setOnClickListener(v -> setQuickColor(0, 255, 0));
        blueColorBtn.setOnClickListener(v -> setQuickColor(0, 0, 255));
        whiteColorBtn.setOnClickListener(v -> setQuickColor(255, 255, 255));
        yellowColorBtn.setOnClickListener(v -> setQuickColor(255, 255, 0));
        cyanColorBtn.setOnClickListener(v -> setQuickColor(0, 255, 255));
        magentaColorBtn.setOnClickListener(v -> setQuickColor(255, 0, 255));
        offColorBtn.setOnClickListener(v -> setQuickColor(0, 0, 0));

        // Target color
        applyTargetColorBtn.setOnClickListener(v -> applyTargetColor());
        swapToRgbBtn.setOnClickListener(v -> swapTargetToCurrent());
        targetColorEdit.setOnEditorActionListener((v, actionId, event) -> {
            applyTargetColor();
            return true;
        });
    }

    private void checkPorts() {
        ArrayList<String> availablePorts = new ArrayList<>();
        String[] portLabels = {"LED light controller", "scanner"};

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

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availablePorts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        portSpinner.setAdapter(adapter);
        portSpinner.setSelection(0);
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

        for (String p : ports) {
            if (portPath.contains(p)) {
                portPath = p;
                break;
            }
        }

        if (!portPath.startsWith("/dev/ttyS")) {
            portPath = ports[0];
        }

        File portFile = new File(portPath);
        if (!portFile.exists()) {
            Toast.makeText(this, "Port " + portPath + " does not exist", Toast.LENGTH_LONG).show();
            updateStatus("Error: Port unavailable");
            return;
        }

        int result = serialPort.open(portPath, 9600);
        if (result == 0) {
            isOpen = true;
            openCloseBtn.setText("Close");
            portSpinner.setEnabled(false);
            updateStatus("Connected: " + portPath + " @ 9600 baud");
            Toast.makeText(this, "Serial port opened successfully", Toast.LENGTH_SHORT).show();
            sendBtn.setEnabled(true);
            offBtn.setEnabled(true);
        } else {
            Toast.makeText(this, "Failed to open serial port", Toast.LENGTH_LONG).show();
            updateStatus("Error: Opening failed");
        }
    }

    private void closeSerialPort() {
        if (serialPort != null) {
            serialPort.close();
        }
        isOpen = false;
        openCloseBtn.setText("Open");
        portSpinner.setEnabled(true);
        updateStatus("Not connected");
        sendBtn.setEnabled(false);
        offBtn.setEnabled(false);
        Toast.makeText(this, "Serial port closed", Toast.LENGTH_SHORT).show();
    }

    private void updateColorFromRGB(int r, int g, int b) {
        currentRed = r;
        currentGreen = g;
        currentBlue = b;

        // Update RGB sliders
        redSeekBar.setProgress(r);
        greenSeekBar.setProgress(g);
        blueSeekBar.setProgress(b);

        // Update RGB value texts
        redValueText.setText(String.valueOf(r));
        greenValueText.setText(String.valueOf(g));
        blueValueText.setText(String.valueOf(b));

        // Calculate HSB values
        float[] hsb = new float[3];
        Color.colorToHSV(Color.rgb(r, g, b), hsb);
        currentHue = hsb[0];
        currentSaturation = hsb[1];
        currentBrightness = hsb[2];

        // Update HSB sliders
        hueSeekBar.setProgress((int) currentHue);
        saturationSeekBar.setProgress((int) (currentSaturation * 100));
        brightnessSeekBar.setProgress((int) (currentBrightness * 100));

        // Update HSB value texts
        hueValueText.setText(String.format(Locale.getDefault(), "Hue: %.0f°", currentHue));
        saturationValueText.setText(String.format(Locale.getDefault(), "Saturation: %.0f%%", currentSaturation * 100));
        brightnessValueText.setText(String.format(Locale.getDefault(), "Brightness: %.0f%%", currentBrightness * 100));

        // Update color preview
        updateColorPreview(r, g, b);

        // Update HEX display
        String hexCommand = String.format(Locale.getDefault(), "%02X %02X %02X", r, g, b);
        hexEditText.setText(hexCommand);

        // Update LED compare view
        updateLedCompareView();

        // Update color difference
        updateColorDifference();

        // Auto-send color to LED with debounce
        scheduleColorSend();
    }

    private void updateColorFromHSB() {
        int rgb = Color.HSVToColor(new float[]{currentHue, currentSaturation, currentBrightness});
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);
        updateColorFromRGB(r, g, b);
    }

    private void updateColorPreview(int r, int g, int b) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.rgb(r, g, b));
        drawable.setStroke(2, Color.GRAY);
        colorPreview.setBackground(drawable);
    }

    private void scheduleColorSend() {
        if (sendColorRunnable != null) {
            handler.removeCallbacks(sendColorRunnable);
        }

        sendColorRunnable = () -> {
            if (isOpen) {
                sendColorToLED();
            }
        };

        // Debounce: send after 100ms of no changes
        handler.postDelayed(sendColorRunnable, 100);
    }

    private void sendColorToLED() {
        if (!isOpen) {
            sendResultText.setText("Please open serial port first");
            sendResultText.setTextColor(Color.RED);
            return;
        }

        byte[] command = new byte[]{(byte) currentRed, (byte) currentGreen, (byte) currentBlue};
        int result = serialPort.write(command);

        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date());

        if (result >= 0) {
            String hexStr = String.format(Locale.getDefault(), "%02X %02X %02X", currentRed, currentGreen, currentBlue);
            sendResultText.setText(String.format("Sent [%s]: %s", timestamp, hexStr));
            sendResultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            sendResultText.setText("Send failed");
            sendResultText.setTextColor(Color.RED);
        }
    }

    private void sendOffCommand() {
        setQuickColor(0, 0, 0);
    }

    private void setQuickColor(int r, int g, int b) {
        updateColorFromRGB(r, g, b);
        if (isOpen) {
            handler.postDelayed(this::sendColorToLED, 50);
        }
    }

    private void updateStatus(String status) {
        statusTextView.setText("Status: " + status);
    }

    private void applyTargetColor() {
        String hexColor = targetColorEdit.getText().toString().trim();

        // Remove # prefix if present
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }

        // Remove spaces
        hexColor = hexColor.replaceAll("\\s+", "");

        // Validate hex color
        if (hexColor.length() != 6) {
            Toast.makeText(this, "Invalid hex color. Use format: RRGGBB or #RRGGBB", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);

            targetRed = r;
            targetGreen = g;
            targetBlue = b;

            // Update target color preview
            GradientDrawable targetDrawable = new GradientDrawable();
            targetDrawable.setShape(GradientDrawable.OVAL);
            targetDrawable.setColor(Color.rgb(r, g, b));
            targetDrawable.setStroke(2, Color.GRAY);
            targetColorPreview.setBackground(targetDrawable);

            // Update hex text
            targetColorHexText.setText(String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b));

            // Set as background color
            mainScrollView.setBackgroundColor(Color.rgb(r, g, b));

            // Update compare view
            GradientDrawable compareDrawable = new GradientDrawable();
            compareDrawable.setShape(GradientDrawable.OVAL);
            compareDrawable.setColor(Color.rgb(r, g, b));
            compareDrawable.setStroke(2, Color.GRAY);
            targetCompareView.setBackground(compareDrawable);

            // Update LED compare view with current color
            updateLedCompareView();

            // Calculate and display color difference
            updateColorDifference();

            Toast.makeText(this, "Target color applied: " + targetColorHexText.getText(), Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid hex color format", Toast.LENGTH_SHORT).show();
        }
    }

    private void swapTargetToCurrent() {
        // Set current LED color as target
        targetRed = currentRed;
        targetGreen = currentGreen;
        targetBlue = currentBlue;

        String hexColor = String.format(Locale.getDefault(), "%02X%02X%02X", targetRed, targetGreen, targetBlue);
        targetColorEdit.setText(hexColor);

        applyTargetColor();
    }

    private void updateLedCompareView() {
        GradientDrawable ledDrawable = new GradientDrawable();
        ledDrawable.setShape(GradientDrawable.OVAL);
        ledDrawable.setColor(Color.rgb(currentRed, currentGreen, currentBlue));
        ledDrawable.setStroke(2, Color.GRAY);
        ledCompareView.setBackground(ledDrawable);
    }

    private void updateColorDifference() {
        int diffR = Math.abs(targetRed - currentRed);
        int diffG = Math.abs(targetGreen - currentGreen);
        int diffB = Math.abs(targetBlue - currentBlue);

        colorDiffText.setText(String.format(Locale.getDefault(),
            "Color difference: R:%d G:%d B:%d", diffR, diffG, diffB));

        // Color code the difference
        int totalDiff = diffR + diffG + diffB;
        if (totalDiff == 0) {
            colorDiffText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (totalDiff < 30) {
            colorDiffText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            colorDiffText.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isOpen) {
            closeSerialPort();
        }
        if (handler != null && sendColorRunnable != null) {
            handler.removeCallbacks(sendColorRunnable);
        }
    }
}
