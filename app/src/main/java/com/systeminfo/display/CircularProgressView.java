package com.systeminfo.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
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
        progressPaint.setColor(0xFF222222); // Very dark gray/black for progress
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setAntiAlias(true);

        memoryPaint = new Paint();
        memoryPaint.setColor(0xFF222222); // Use same as progress for consistency
        memoryPaint.setStyle(Paint.Style.STROKE);
        memoryPaint.setStrokeWidth(12f);
        memoryPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(0xFF111111); // Black for text
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

        rectF = new RectF();
        memoryRectF = new RectF();
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

        // Draw label at the top (all caps, bold)
        textPaint.setTextSize(getWidth() / 14f);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        String labelText = label == null ? "" : label.toUpperCase();
        float labelY = getHeight() / 6f;
        canvas.drawText(labelText, getWidth() / 2f, labelY, textPaint);

        // Draw value in the center
        if (isGpu) {
            textPaint.setTextSize(getWidth() / 8f);
            textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            String valueText = String.format("%s GB • %d%%", memoryUsed, (int)progress);
            float valueY = getHeight() / 2f + getWidth() / 16f;
            canvas.drawText(valueText, getWidth() / 2f, valueY, textPaint);
        } else {
            textPaint.setTextSize(getWidth() / 7f);
            textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            String percentText = String.format("%d%%", (int)progress);
            float percentY = getHeight() / 2f + getWidth() / 14f;
            canvas.drawText(percentText, getWidth() / 2f, percentY, textPaint);
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