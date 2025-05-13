package com.systeminfo.display;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.os.PowerManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private static final float FLAT_THRESHOLD = 1.5f; // Threshold for detecting flat position

    private CircularProgressView cpuProgress;
    private CircularProgressView memoryProgress;
    private CircularProgressView gpu1Progress;
    private CircularProgressView gpu2Progress;
    private PowerProgressView powerProgress;
    private OkHttpClient client;
    private Handler handler;
    private boolean isConnected = false;
    private static final String SERVER_URL = "http://192.168.0.157:8080/system-info"; // Updated with your PC's IP
    private View decorView;
    
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isFlat = false;
    private boolean wakeLockAcquired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        
        // Initialize WakeLock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK | 
            PowerManager.ACQUIRE_CAUSES_WAKEUP |
            PowerManager.ON_AFTER_RELEASE,
            "SystemInfoDisplay:WakeLockTag");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);

        // For API 16+ hide the status bar
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Initialize views
        cpuProgress = findViewById(R.id.cpuProgress);
        memoryProgress = findViewById(R.id.memoryProgress);
        gpu1Progress = findViewById(R.id.gpu1Progress);
        gpu2Progress = findViewById(R.id.gpu2Progress);
        powerProgress = findViewById(R.id.powerProgress);

        // Set labels
        cpuProgress.setLabel("CPU");
        memoryProgress.setLabel("Mem");
        gpu1Progress.setLabel("GPU 1");
        gpu2Progress.setLabel("GPU 2");

        // Initialize OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        handler = new Handler(Looper.getMainLooper());
        startPolling();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-apply in case system cleared it
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        // Register sensor listener
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Ensure we're still connected
        if (!isConnected) {
            startPolling();
            isConnected = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listener
        sensorManager.unregisterListener(this);
        // Release wake lock if held
        releaseWakeLock();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            
            // Check if phone is laid flat (z-axis points up with gravity ~9.8 m/s²)
            // and x and y values are close to 0
            boolean nowFlat = (Math.abs(z) > 9.0f) && 
                            (Math.abs(x) < FLAT_THRESHOLD) && 
                            (Math.abs(y) < FLAT_THRESHOLD);
            
            // Only take action if the state changes
            if (nowFlat != isFlat) {
                isFlat = nowFlat;
                if (isFlat) {
                    // Phone is now flat, acquire wake lock
                    acquireWakeLock();
                    Log.d(TAG, "Phone is flat - keeping screen on");
                } else {
                    // Phone is no longer flat, release wake lock
                    releaseWakeLock();
                    Log.d(TAG, "Phone is not flat - allowing screen to time out");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this implementation
    }
    
    private void acquireWakeLock() {
        if (!wakeLockAcquired && wakeLock != null) {
            wakeLock.acquire();
            wakeLockAcquired = true;
        }
    }
    
    private void releaseWakeLock() {
        if (wakeLockAcquired && wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLockAcquired = false;
        }
    }

    private void startPolling() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    fetchSystemInfo();
                    handler.postDelayed(this, 500); // Poll every 500ms
                }
            }
        });
    }

    private void stopPolling() {
        handler.removeCallbacksAndMessages(null);
        resetInfoTexts();
    }

    private void fetchSystemInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url(SERVER_URL)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String jsonData = response.body().string();
                        updateUI(jsonData);
                    } else {
                        showError("Failed to fetch system info");
                    }
                } catch (IOException e) {
                    showError("Connection error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void updateUI(final String jsonData) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject data = new JSONObject(jsonData);
                    Log.d(TAG, "Received data: " + jsonData);
                    
                    // Update CPU
                    if (data.has("cpuUsage")) {
                        float cpuUsage = (float) data.getDouble("cpuUsage");
                        cpuProgress.setProgress(cpuUsage);
                        cpuProgress.addUtilizationSample(cpuUsage);
                    }
                    
                    // Update Memory
                    if (data.has("memoryUsage")) {
                        float memoryUsage = (float) data.getDouble("memoryUsage");
                        memoryProgress.setProgress(memoryUsage);
                        memoryProgress.addUtilizationSample(memoryUsage);
                    }
                    
                    // Update GPUs
                    if (data.has("gpu1")) {
                        JSONObject gpu1 = data.getJSONObject("gpu1");
                        if (gpu1.has("usage")) {
                            float gpu1Usage = (float) gpu1.getDouble("usage");
                            gpu1Progress.setProgress(gpu1Usage);
                            gpu1Progress.addUtilizationSample(gpu1Usage);
                            
                            // Update GPU 1 memory
                            if (gpu1.has("memoryUsed") && gpu1.has("memoryTotal") && gpu1.has("memoryPercent")) {
                                String memoryUsed = gpu1.getString("memoryUsed");
                                float memoryPercent = (float) gpu1.getDouble("memoryPercent");
                                gpu1Progress.setMemoryUsed(memoryUsed);
                                gpu1Progress.setMemoryProgress(memoryPercent);
                            }
                        }
                    }
                    
                    if (data.has("gpu2")) {
                        JSONObject gpu2 = data.getJSONObject("gpu2");
                        if (gpu2.has("usage")) {
                            float gpu2Usage = (float) gpu2.getDouble("usage");
                            gpu2Progress.setProgress(gpu2Usage);
                            gpu2Progress.addUtilizationSample(gpu2Usage);
                            
                            // Update GPU 2 memory
                            if (gpu2.has("memoryUsed") && gpu2.has("memoryTotal") && gpu2.has("memoryPercent")) {
                                String memoryUsed = gpu2.getString("memoryUsed");
                                float memoryPercent = (float) gpu2.getDouble("memoryPercent");
                                gpu2Progress.setMemoryUsed(memoryUsed);
                                gpu2Progress.setMemoryProgress(memoryPercent);
                            }
                        }
                    }
                    
                    // Update Power
                    if (data.has("totalPower") && data.has("totalPowerLimit")) {
                        float totalPower = Float.parseFloat(data.getString("totalPower"));
                        float totalPowerLimit = Float.parseFloat(data.getString("totalPowerLimit"));
                        
                        // Calculate percentage for the border (0-100 range)
                        float powerPercentage = (totalPower / totalPowerLimit) * 100f;
                        Log.d(TAG, String.format("Power: %.1fW / %.1fW = %.2f%%", 
                            totalPower, totalPowerLimit, powerPercentage));
                        powerProgress.setPowerProgress(powerPercentage); // Pass as 0-100 range
                        
                        // Display just the power value in watts
                        powerProgress.setPowerUsed(String.format("%.0fW", totalPower));
                    }
                    
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON data", e);
                    e.printStackTrace();
                    showError("Error parsing data: " + e.getMessage());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing power values", e);
                    e.printStackTrace();
                    showError("Error parsing power values: " + e.getMessage());
                }
            }
        });
    }

    private void resetInfoTexts() {
        cpuProgress.setProgress(0);
        memoryProgress.setProgress(0);
        gpu1Progress.setProgress(0);
        gpu2Progress.setProgress(0);
        powerProgress.setPowerProgress(0);
    }

    private void showError(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
        releaseWakeLock();
    }
} 