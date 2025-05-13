package com.systeminfo.display;

import android.content.Context;
import android.util.AttributeSet;

public class PowerProgressView extends CircularProgressView {
    private StatCircle powerCircle;
    private String powerCost = "";
    private String currentPower = "";

    public PowerProgressView(Context context) {
        super(context);
        initPowerCircle();
    }

    public PowerProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPowerCircle();
    }

    private void initPowerCircle() {
        // Initialize power circle with larger padding to make it smaller
        powerCircle = new StatCircle(24f);
        powerCircle.setLabel("PWR");
        powerCircle.setShowPercentage(false); // Don't show percentage in the center
        powerCircle.setLabelPositionMultiplier(1.5f); // Move label down by 50%
        // Clear the background color set by parent
        setBackgroundColor(0x00000000); // Transparent background
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        powerCircle.setSize(w, h);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        // Don't call super.onDraw() to avoid drawing parent's circles
        powerCircle.draw(canvas);
    }

    public void setPowerProgress(float progress) {
        powerCircle.setProgress(progress);
        powerCircle.addUtilizationSample(progress);
        invalidate();
    }

    public void setPowerUsed(String powerUsed) {
        this.currentPower = powerUsed;
        updateDisplay();
    }

    public void setPowerCost(String cost) {
        this.powerCost = cost;
        updateDisplay();
    }

    private void updateDisplay() {
        // Combine power usage and cost into a single string with newline
        String displayText = currentPower;
        if (!powerCost.isEmpty()) {
            displayText += "\n" + powerCost;
        }
        powerCircle.setMemoryUsed(displayText);
        invalidate();
    }
} 