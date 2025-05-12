# Android Display

A system information display application for Android devices that shows real-time system metrics and information.

## Features

- Displays real-time system information including:
  - CPU usage and temperature
  - Memory usage
  - Battery status
  - Network information
  - Storage statistics
- Clean, modern Material Design interface
- Background service for continuous monitoring
- Low resource usage

## Requirements

- Android 4.4 (API level 19) or higher
- Minimum screen resolution: 480x800 (mdpi)
- Internet permission for network information
- Storage permission for storage statistics
- JDK 16 (Eclipse Temurin)

## Building

1. Install JDK 16:
   - Download Eclipse Temurin JDK 16 from [Adoptium](https://adoptium.net/temurin/releases/?version=16)
   - Install the JDK and set JAVA_HOME environment variable to the installation directory
   - Example: `JAVA_HOME=C:\Program Files\Eclipse Foundation\jdk-16.0.2.7-hotspot`

2. Set up environment variables:
   - Set `JAVA_HOME` to your JDK installation path
   - Add `%JAVA_HOME%\bin` to your system's `PATH` variable
   - Set `ANDROID_HOME` to your Android SDK location (typically `%LOCALAPPDATA%\Android\Sdk`)
   - Add `%ANDROID_HOME%\platform-tools` to your system's `PATH` variable

3. Clone the repository
4. Open the project in Android Studio
5. Build and run on your device or emulator

## Deployment

### Using Android Emulator

1. Set up the emulator:
   - Open Android Studio
   - Go to Tools > Device Manager
   - Click "Create Device"
   - Select "Pixel 2" (or any device with API level 19 or higher)
   - Download and select a system image (recommended: API 30)
   - Complete the setup with default settings

2. Run the app:
   - Select the emulator from the device dropdown in Android Studio
   - Click the "Run" button (green play icon)
   - The app will build and install automatically

### Using Physical Device (Galaxy Nexus)

1. Enable Developer Options:
   - Go to Settings > About phone
   - Tap "Build number" 7 times to enable developer options
   - Go back to Settings > System > Developer options
   - Enable "USB debugging"

2. Connect the device:
   - Connect your Galaxy Nexus to your computer via USB
   - Accept the USB debugging prompt on your device
   - Verify connection by running `adb devices` in terminal

3. Install the app:
   - Select your device from the device dropdown in Android Studio
   - Click the "Run" button
   - The app will build and install automatically

### Command Line Deployment

1. Build the APK:
   ```powershell
   ./gradlew clean build
   ```

2. Start the emulator (if using):
   ```powershell
   emulator -avd Pixel_2_API_30
   ```

3. List connected devices:
   ```powershell
   adb devices
   ```
   This will show output like:
   ```
   List of devices attached
   emulator-5554    device
   0146A54914015003    device
   ```

4. Install on specific device:
   ```powershell
   # For emulator (using device ID)
   adb -s emulator-5554 install app/build/outputs/apk/debug/app-debug.apk

   # For physical device (using device ID)
   adb -s 0146A54914015003 install app/build/outputs/apk/debug/app-debug.apk
   ```

5. Launch the app on specific device:
   ```powershell
   # For emulator
   adb -s emulator-5554 shell am start -n com.systeminfo.display/com.systeminfo.display.MainActivity

   # For physical device
   adb -s 0146A54914015003 shell am start -n com.systeminfo.display/com.systeminfo.display.MainActivity
   ```

### Starting the Python Server

1. Navigate to the server directory:
   ```powershell
   cd server
   ```

2. Create virtual environment:
   ```powershell
   python -m venv venv310
   ```

3. Activate virtual environment:
   ```powershell
   .\venv310\Scripts\activate
   ```

4. Install dependencies:
   ```powershell
   pip install -r requirements.txt
   ```

5. Start the server:
   ```powershell
   python system_info_server.py
   ```

The server will start on `http://localhost:5000` and provide system information via HTTP API endpoints.

## Development

The project consists of two main components:

1. Android App (`app/` directory)
   - Main activity for displaying system information
   - Background service for monitoring
   - Material Design UI components

2. Python Server (`server/` directory)
   - Provides system information via HTTP API
   - Uses Flask for the web server
   - Requirements in `requirements.txt`

## License

This project is licensed under the MIT License - see the LICENSE file for details. 