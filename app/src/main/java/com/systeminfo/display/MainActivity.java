package com.systeminfo.display;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private TextView statusText;
    private TextView cpuInfo;
    private TextView memoryInfo;
    private TextView diskInfo;
    private TextView networkInfo;
    private TextView uptimeInfo;
    private Button connectButton;
    private OkHttpClient client;
    private Handler handler;
    private boolean isConnected = false;
    private static final String SERVER_URL = "http://192.168.0.157:8080/system-info"; // Updated with your PC's IP
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This must be called before setContentView
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);

        // For API 16+ hide the status bar
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Initialize views
        statusText = findViewById(R.id.statusText);
        cpuInfo = findViewById(R.id.cpuInfo);
        memoryInfo = findViewById(R.id.memoryInfo);
        diskInfo = findViewById(R.id.diskInfo);
        networkInfo = findViewById(R.id.networkInfo);
        uptimeInfo = findViewById(R.id.uptimeInfo);
        connectButton = findViewById(R.id.connectButton);

        // Initialize OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        handler = new Handler(Looper.getMainLooper());

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    startPolling();
                    connectButton.setText("Disconnect");
                    isConnected = true;
                } else {
                    stopPolling();
                    connectButton.setText("Connect to PC");
                    isConnected = false;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-apply in case system cleared it
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void startPolling() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    fetchSystemInfo();
                    handler.postDelayed(this, 2000); // Poll every 2 seconds
                }
            }
        });
    }

    private void stopPolling() {
        handler.removeCallbacksAndMessages(null);
        statusText.setText("Status: Disconnected");
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
                    JsonObject data = new Gson().fromJson(jsonData, JsonObject.class);
                    statusText.setText("Status: Connected");
                    cpuInfo.setText("CPU Usage: " + data.get("cpuUsage").getAsString() + "%");
                    memoryInfo.setText("Memory Usage: " + data.get("memoryUsage").getAsString() + "%");
                    diskInfo.setText("Disk Space: " + data.get("diskSpace").getAsString());
                    networkInfo.setText("Network Status: " + data.get("networkStatus").getAsString());
                    uptimeInfo.setText("System Uptime: " + data.get("uptime").getAsString());
                } catch (Exception e) {
                    showError("Error parsing data");
                }
            }
        });
    }

    private void resetInfoTexts() {
        cpuInfo.setText("CPU Usage: --");
        memoryInfo.setText("Memory Usage: --");
        diskInfo.setText("Disk Space: --");
        networkInfo.setText("Network Status: --");
        uptimeInfo.setText("System Uptime: --");
    }

    private void showError(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                statusText.setText("Status: Error");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
    }
} 