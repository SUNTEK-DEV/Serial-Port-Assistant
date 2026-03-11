# Serial Port Debug Assistant User Guide

## Application Overview

Serial Port Debug Assistant is a serial communication tool developed by the SUNTEK team for Android devices, supporting communication and debugging with LED controllers and barcode scanners through serial ports.

**v2.0 New Feature**: LED Color Controller - Visual color picker with real-time color comparison and hex color matching!

## Port Description

This application supports two serial port devices:

- **ttyS5** - LED Controller Serial Port
  - Used for controlling LED display, brightness adjustment, color settings, and other operations
  - Baud rate: 9600

- **ttyS11** - Barcode Scanner Serial Port
  - Used for receiving scan data from barcode scanners
  - Baud rate: 9600

## Features

### 1. Port Selection
- Supports selection of `/dev/ttyS5` or `/dev/ttyS11`
- Automatically detects port availability, displaying "Available" or "Unavailable" status
- Drop-down list selection for easy operation

### 2. Baud Rate Setting
- Default baud rate: 9600
- Baud rate can be modified according to device requirements
- Supports standard baud rates: 9600, 19200, 38400, 57600, 115200

### 3. Serial Port Control
- **Open Serial Port**: Click the "Open Serial Port" button to connect to the selected port
- **Close Serial Port**: After opening, the button changes to "Close Serial Port". Click to disconnect
- After opening the serial port, port selection and baud rate settings will be locked to prevent misoperation

### 4. Data Reception
- Real-time serial port data reception with timestamp display
- Display formats:
  - Text mode: Shows printable characters, non-printable characters displayed in hexadecimal format `[XX]`
  - Each data entry displays reception time: `[HH:mm:ss.SSS]`
- Auto-scroll to latest data
- Supports long-press to select and copy received data

### 5. Data Transmission
- **Text Mode**: Directly input text content to send
- **Hex Mode**: Click the "Hex Send" button to switch to hexadecimal transmission mode
  - Input hexadecimal data, such as: `01 02 03 FF` or `010203FF`
  - Automatically filters spaces
  - Hex mode will also display data in hexadecimal format in the receive area
- Each sent data will be displayed in the receive area with a timestamp
- Displays the number of bytes sent after successful transmission

### 6. LED Color Controller (New Feature)
Click the **"LED Color Controller"** button on the main interface to enter the LED color controller:

#### 6.1 Target Color Input
- Supports input of hex color values: `RRGGBB` or `#RRGGBB` (e.g., `FF0000` or `#FF0000`)
- Click "Apply" button to apply the target color
- When applied, the entire interface background changes to the target color for easy comparison
- "Use as Current" button copies the current LED color to the target

#### 6.2 Color Comparison Display
- Side-by-side display of two circular color swatches:
  - **Target** - User-input hex color
  - **LED** - Current color being sent to the LED strip
- Color difference value displayed below: `Color difference: R:xx G:xx B:xx`
- Difference value color coding:
  - 🟢 Green: Perfect match (difference = 0)
  - 🟠 Orange: Slight difference (difference < 30)
  - 🔴 Red: Significant difference (difference ≥ 30)

#### 6.3 HSB Color Picker
- **Hue**: 0-360° slider for selecting base color
- **Saturation**: 0-100% slider
- **Brightness**: 0-100% slider

#### 6.4 RGB Precision Adjustment
- Independent R/G/B sliders, range 0-255
- Real-time display of each channel value
- Colors are sent to LED strip in real-time when dragging sliders

#### 6.5 Quick Color Buttons
- 8 preset color buttons: Red, Green, Blue, White, Yellow, Cyan, Magenta, Off
- One-click selection of common colors

#### 6.6 HEX Command Display
- Real-time display of current color's hex command (e.g., `FF 00 00`)
- Shows send status and timestamp
- "LED OFF" button to turn off the LED strip

### 7. Other Features
- **Clear Receive**: Clear all data in the receive area
- **Status Display**: Bottom displays current connection status and error messages
- **Auto Check**: Automatically checks if port devices exist on startup

## Usage Instructions

### Connecting to LED Controller (ttyS5)

1. **Select Port**
   - Click the port dropdown list
   - Select `/dev/ttyS5 (Available)` or `/dev/ttyS5 (Unavailable)`
   - If it shows "Unavailable", check device permissions

2. **Set Baud Rate**
   - Confirm the baud rate is `9600` (default value)
   - If modification is needed, input the corresponding baud rate value

3. **Open Serial Port**
   - Click the "Open Serial Port" button
   - Upon success, it will display: "Connected: /dev/ttyS5 @ 9600 baud"
   - Status bar shows "Connected" status

4. **Send LED Control Commands**
   - Input LED control commands in the send area (refer to LED communication protocol documentation)
   - Select "Text Send" or "Hex Send" according to command format
   - Click the "Send Data" button

5. **Receive Response Data**
   - Data returned by the LED controller will be displayed in the receive area
   - Each data entry has a timestamp
   - You can view the response status of the LED controller

6. **Close Serial Port**
   - After debugging is complete, click the "Close Serial Port" button
   - Disconnect the serial port connection

### Using LED Color Controller

1. **Enter Color Controller**
   - Click the **"LED Color Controller"** button on the main interface

2. **Connect Serial Port**
   - Select `/dev/ttyS5` port
   - Click "Open" button to open the serial port

3. **Set Target Color**
   - Enter hex color value in "Target Color" input field (e.g., `3A57DF`)
   - Click "Apply" - the interface background changes to the target color
   - Now you can visually compare the target color with the actual LED color

4. **Adjust LED Color**
   - Method 1: Drag HSB sliders to adjust hue, saturation, brightness
   - Method 2: Drag R/G/B sliders for precise channel adjustment
   - Method 3: Click quick color buttons for preset colors
   - Colors are automatically sent to the LED strip in real-time

5. **Compare Color Difference**
   - Observe the two color swatches in "Color Comparison" area
   - Check the difference value below to judge matching degree
   - When difference is 0, LED color perfectly matches the target

6. **Send Color to LED**
   - Adjusted colors are automatically sent (100ms debounce)
   - Or click "Send to LED" button to manually send
   - HEX Command area displays the current hex command being sent

### Connecting to Barcode Scanner (ttyS11)

1. **Select Port**
   - Click the port dropdown list
   - Select `/dev/ttyS11 (Available)`

2. **Set Baud Rate**
   - Confirm the baud rate is `9600`

3. **Open Serial Port**
   - Click the "Open Serial Port" button
   - Wait for connection success prompt

4. **Receive Scan Data**
   - After the serial port is opened, barcode/QR code data scanned by the scanner will automatically display in the receive area
   - Each data entry has a timestamp: `[HH:mm:ss.SSS] RX: Scan content`
   - You can view scan results in real-time

5. **Send Configuration Commands** (if needed)
   - If the scanner needs to be configured, input configuration commands in the send area
   - Click "Send Data" to send

6. **Close Serial Port**
   - After use, click "Close Serial Port" to disconnect

## Data Format Description

### Text Mode
- Directly input text content, such as: `AT+CONFIG=1`
- Suitable for sending ASCII text commands

### Hex Mode
- Input hexadecimal data, format examples:
  - `01 02 03 FF` (with spaces)
  - `010203FF` (without spaces)
  - `0A 0D` (carriage return and line feed)
- Automatically filters spaces and case
- Suitable for sending binary data or protocol data packets

### LED Color Command Format
- Set color: `RR GG BB` (3-byte RGB values)
- Examples:
  - Red: `FF 00 00`
  - Green: `00 FF 00`
  - Blue: `00 00 FF`
  - White: `FF FF FF`
  - Off: `00 00 00`

### Received Data Display
- **Text Mode**: Printable characters display normally, non-printable characters displayed in `[XX]` format
- **Hex Mode**: All data displayed in hexadecimal, such as: `01 02 03 FF`

## Interface Description

### Main Interface
```
┌─────────────────────────┐
│   Serial Port Debug     │
│      Assistant          │
├─────────────────────────┤
│ Port: [ttyS5 ▼]        │
│ Baud Rate: [9600]       │
│ [Open Serial Port]      │
│ [LED Color Controller]  │
├─────────────────────────┤
│ Receive Area:           │
│ ┌─────────────────────┐ │
│ │ [10:23:45.123] RX:  │ │
│ │ Data content...     │ │
│ └─────────────────────┘ │
│ [Clear] [Hex Send]      │
├─────────────────────────┤
│ Send Area:              │
│ ┌─────────────────────┐ │
│ │ Input data to send  │ │
│ └─────────────────────┘ │
│ [Send Data]             │
├─────────────────────────┤
│ Status: Connected       │
└─────────────────────────┘
```

### LED Color Controller Interface
```
┌───────────────────────────────┐
│   LED Color Controller        │
├───────────────────────────────┤
│ Target Color: [______] [Apply]│
│ Target: [■] #RRGGBB           │
├───────────────────────────────┤
│ Color Comparison:             │
│ [Target]  │  [LED]           │
│ Diff: R:0 G:0 B:0             │
├───────────────────────────────┤
│ Port: [ttyS5 ▼] [Open]        │
│ Status: Connected             │
├───────────────────────────────┤
│ [■] Color Preview             │
├───────────────────────────────┤
│ Hue:        [====●====] 0°    │
│ Saturation: [====●====] 100%  │
│ Brightness: [====●====] 100%  │
├───────────────────────────────┤
│ R: [====●====] 255            │
│ G: [====●====] 0              │
│ B: [====●====] 0              │
├───────────────────────────────┤
│ HEX: FF 00 00                 │
│ [Send to LED] [LED OFF]       │
├───────────────────────────────┤
│ [R][G][B][W]                  │
│ [Y][C][M][OFF]                │
└───────────────────────────────┘
```

## Important Notes

### Permission Requirements
- Access to `/dev/ttyS*` devices requires appropriate system permissions
- If the application cannot open the serial port, you may need:
  1. **Root Permission**: Run on a rooted device
  2. **System Permission**: Device manufacturer has authorized application to access serial ports
  3. **SELinux Policy**: SELinux policy needs to be adjusted to allow access to serial port devices

### Serial Port Usage Rules
1. **Exclusive Access**: Only one application can access the serial port at a time
2. **Close Connection**: Always close the serial port after use to release resources
3. **Exception Handling**: If connection is abnormal, close the serial port first, then reopen

### Baud Rate Setting
- Baud rate must match the device, otherwise normal communication is impossible
- LED controller and barcode scanner default to **9600** baud rate
- After modifying the baud rate, close and reopen the serial port for changes to take effect

### Data Transmission
- Ensure the serial port is opened before sending data
- In Hex mode, ensure the input hexadecimal data format is correct
- If transmission fails, check the serial port connection status

### Data Reception
- Receive area shows maximum visible content, regular clearing is recommended
- Received data will automatically add timestamps for easy debugging
- Long-press text in receive area to select and copy

## Frequently Asked Questions

### Q1: Port shows "Unavailable", unable to open serial port
**A:**
- Check if device files exist: `/dev/ttyS5` or `/dev/ttyS11`
- Check if the application has access permissions
- Try running the application with root permissions

### Q2: Opening serial port prompts "Failed to open serial port"
**A:**
- Check if the port is occupied by another application
- Check device permission settings
- Try restarting the device and opening again

### Q3: No response after sending data
**A:**
- Check if baud rate matches the device
- Confirm the sent data format is correct
- Check if the device is working normally
- View receive area for any error messages

### Q4: Incomplete barcode scanner data reception
**A:**
- Check baud rate setting (should be 9600)
- Confirm serial port connection is normal
- Check if scanner configuration is correct

### Q5: Hex mode transmission failed
**A:**
- Check if the input hexadecimal data format is correct
- Ensure every two characters represent one byte (00-FF)
- Check for illegal characters

### Q6: LED color doesn't match target color
**A:**
- Check the color difference value to judge the deviation
- Fine-tune R/G/B sliders to reduce the difference
- Note that LED physical characteristics may cause color deviation
- Compare colors under the same lighting environment

### Q7: Application crashes or unresponsive
**A:**
- Close serial port before exiting the application
- Clear application data and reinstall
- Check Android system logs (logcat) for error information

## LED Controller Usage Examples

### Example 1: Query LED Status
1. Select port: `ttyS5`
2. Open serial port
3. Input query command in send area (refer to LED communication protocol)
4. Click "Send Data"
5. View returned status information in receive area

### Example 2: Control LED Display
1. Open `ttyS5` serial port
2. Switch to Hex mode (click "Hex Send")
3. Input LED control command hexadecimal data
4. Send data, LED displays corresponding content

### Example 3: Match Target Color
1. Enter LED Color Controller interface
2. Open serial port to connect to ttyS5
3. Enter `#3A57DF` in Target Color input
4. Click Apply - interface background changes to target color
5. Drag R/G/B sliders and observe the color difference value
6. When difference is 0, LED color perfectly matches target

## Barcode Scanner Usage Examples

### Example 1: Receive Scan Data
1. Select port: `ttyS11`
2. Open serial port
3. Use scanner to scan barcode/QR code
4. Scan results automatically display in receive area with timestamp

### Example 2: Configure Scanner
1. Open `ttyS11` serial port
2. Send configuration commands (refer to scanner protocol documentation)
3. Receive configuration response, confirm if configuration is successful

## Technical Support

If you encounter problems or need technical support, please:
1. Check error messages in the receive area
2. Check prompt messages in the status bar
3. View Android system logs (logcat)
4. Contact SUNTEK technical support team

## Version Information

- Application Name: Serial Port Debug Assistant
- Version: 2.0
- Update Date: 2026.3.11
- New Features: LED Color Controller (Visual color picker, Color comparison, Hex matching)

---

**Note**: Before using serial port communication, please ensure you understand the communication protocols of the LED controller and barcode scanner. Refer to relevant technical documentation.
