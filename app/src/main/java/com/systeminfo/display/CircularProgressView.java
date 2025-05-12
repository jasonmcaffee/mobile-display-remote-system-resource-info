package com.systeminfo.display;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {
    private StatCircle usageCircle;
    private StatCircle memoryCircle;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize circles with different padding
        usageCircle = new StatCircle(8f);
        memoryCircle = new StatCircle(16f);
        setBackgroundColor(0xFFFFFFFF); // White background
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Make the view square
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        usageCircle.setSize(w, h);
        memoryCircle.setSize(w, h);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);
        
        if (usageCircle.isGpu()) {
            memoryCircle.draw(canvas);
        }
        usageCircle.draw(canvas);
    }

    public void setProgress(float progress) {
        usageCircle.setProgress(progress);
        invalidate();
    }

    public void setMemoryProgress(float progress) {
        memoryCircle.setProgress(progress);
        invalidate();
    }

    public void setLabel(String label) {
        usageCircle.setLabel(label);
        memoryCircle.setLabel(label);
        invalidate();
    }

    public void setMemoryUsed(String memoryUsed) {
        usageCircle.setMemoryUsed(memoryUsed);
        memoryCircle.setMemoryUsed(memoryUsed);
        invalidate();
    }

    public void addUtilizationSample(float utilization) {
        usageCircle.addUtilizationSample(utilization);
        invalidate();
    }
} 