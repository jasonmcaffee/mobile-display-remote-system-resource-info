package com.systeminfo.display;

import android.content.Context;
import android.util.AttributeSet;

public class PowerProgressView extends CircularProgressView {
    private StatCircle powerCircle;

    public PowerProgressView(Context context) {
        super(context);
        init();
    }

    public PowerProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize power circle with larger padding to make it smaller
        powerCircle = new StatCircle(24f);
        powerCircle.setLabel("PWR");
        powerCircle.setShowPercentage(false); // Don't show percentage in the center
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
        powerCircle.setMemoryUsed(powerUsed);
        invalidate();
    }
} 