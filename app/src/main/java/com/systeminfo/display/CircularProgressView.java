package com.systeminfo.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint memoryPaint;
    private Paint textPaint;
    private RectF rectF;
    private RectF memoryRectF;
    private float progress = 0;
    private float memoryProgress = 0;
    private String label = "";
    private String memoryUsed = "";
    private boolean isGpu = false;

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFE0E0E0); // Light gray background
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(12f);
        backgroundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(0xFF2196F3); // Material Blue
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setAntiAlias(true);

        memoryPaint = new Paint();
        memoryPaint.setColor(0xFF4CAF50); // Material Green
        memoryPaint.setStyle(Paint.Style.STROKE);
        memoryPaint.setStrokeWidth(12f);
        memoryPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(0xFF424242); // Dark gray text
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        rectF = new RectF();
        memoryRectF = new RectF();
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
        float padding = 8f;
        float memoryPadding = 16f; // Slightly larger for the outer circle
        rectF.set(padding, padding, w - padding, h - padding);
        memoryRectF.set(memoryPadding, memoryPadding, w - memoryPadding, h - memoryPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (isGpu) {
            // Draw memory background circle
            canvas.drawArc(memoryRectF, 0, 360, false, backgroundPaint);
            
            // Draw memory progress arc
            canvas.drawArc(memoryRectF, -90, memoryProgress * 3.6f, false, memoryPaint);
        }
        
        // Draw usage background circle
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);
        
        // Draw usage progress arc
        canvas.drawArc(rectF, -90, progress * 3.6f, false, progressPaint);
        
        // Draw label
        textPaint.setTextSize(getWidth() / 10f);
        canvas.drawText(label, getWidth() / 2f, getHeight() / 4f, textPaint);
        
        // Draw value
        textPaint.setTextSize(getWidth() / 8f);
        if (isGpu) {
            canvas.drawText(String.format("%s GB • %d%%", memoryUsed, (int)progress), 
                          getWidth() / 2f, getHeight() * 0.6f, textPaint);
        } else {
            canvas.drawText(String.format("%d%%", (int)progress), 
                          getWidth() / 2f, getHeight() * 0.6f, textPaint);
        }
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMemoryProgress(float progress) {
        this.memoryProgress = progress;
        invalidate();
    }

    public void setLabel(String label) {
        this.label = label;
        this.isGpu = label.startsWith("GPU");
        invalidate();
    }

    public void setMemoryUsed(String memoryUsed) {
        this.memoryUsed = memoryUsed;
        invalidate();
    }
} 