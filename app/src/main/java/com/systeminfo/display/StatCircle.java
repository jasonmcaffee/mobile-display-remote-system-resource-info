package com.systeminfo.display;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

public class StatCircle {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint labelBackgroundPaint;
    private Paint gpuMemoryPaint;
    private RectF rectF;
    private RectF labelRectF;
    private float progress = 0;
    private float padding;
    private String label = "";
    private String memoryUsed = "";
    private boolean isGpu = false;
    private UtilizationGraph utilizationGraph;
    private float gpuMemoryPercent = 0f;

    public StatCircle(float padding) {
        this.padding = padding;
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFE0E0E0); // Light gray background
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(12f);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeCap(Paint.Cap.BUTT);

        progressPaint = new Paint();
        progressPaint.setColor(0xFF222222); // Very dark gray/black for progress
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.BUTT);

        gpuMemoryPaint = new Paint();
        gpuMemoryPaint.setColor(0xFF808080); // Darker gray for GPU memory
        gpuMemoryPaint.setStyle(Paint.Style.STROKE);
        gpuMemoryPaint.setStrokeWidth(12f);
        gpuMemoryPaint.setAntiAlias(true);
        gpuMemoryPaint.setStrokeCap(Paint.Cap.BUTT);

        labelBackgroundPaint = new Paint();
        labelBackgroundPaint.setColor(0xFF000000); // Black background
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
        labelBackgroundPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(0xFF111111); // Black for text
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

        rectF = new RectF();
        labelRectF = new RectF();
        utilizationGraph = new UtilizationGraph();
    }

    public void setSize(int width, int height) {
        rectF.set(padding, padding, width - padding, height - padding);
    }

    public void draw(Canvas canvas) {
        // Draw utilization graph as background
        utilizationGraph.draw(canvas, rectF);

        // Draw background circle
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);
        
        // For GPU circles, draw memory arc on a slightly smaller rectF
        if (isGpu && gpuMemoryPercent > 0) {
            float borderInset = 3f; // half of 6f stroke width
            RectF memoryRectF = new RectF(
                rectF.left + borderInset,
                rectF.top + borderInset,
                rectF.right - borderInset,
                rectF.bottom - borderInset
            );
            canvas.drawArc(memoryRectF, -90, gpuMemoryPercent * 3.6f, false, gpuMemoryPaint);
        }
        
        // Draw progress arc on a slightly larger rectF so it touches the outer edge
        if (isGpu) {
            float borderInset = 3f;
            RectF progressRectF = new RectF(
                rectF.left - borderInset,
                rectF.top - borderInset,
                rectF.right + borderInset,
                rectF.bottom + borderInset
            );
            canvas.drawArc(progressRectF, -90, progress * 3.6f, false, progressPaint);
        } else {
            canvas.drawArc(rectF, -90, progress * 3.6f, false, progressPaint);
        }

        // Draw label at the top (all caps, bold)
        textPaint.setTextSize(rectF.width() / 14f);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        String labelText = label == null ? "" : label.toUpperCase();
        
        // Adjust vertical position based on whether it's GPU or not
        float labelY = isGpu ? rectF.height() / 5.5f : rectF.height() / 6f;
        
        // Calculate text bounds for the label background
        float textWidth = textPaint.measureText(labelText);
        float padding = rectF.width() / 40f; // Padding around text
        float textHeight = textPaint.getTextSize();
        
        // Center the text vertically within its background
        labelRectF.set(
            rectF.centerX() - textWidth / 2f - padding,
            labelY - textHeight / 2f - padding,
            rectF.centerX() + textWidth / 2f + padding,
            labelY + textHeight / 2f + padding
        );
        
        // Draw black background for label
        canvas.drawRoundRect(labelRectF, padding, padding, labelBackgroundPaint);
        
        // Draw label text in white
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText(labelText, rectF.centerX(), labelY + textHeight / 3f, textPaint);

        // Draw value in the center
        textPaint.setColor(0xFF111111); // Reset to black for percentage text
        if (isGpu) {
            textPaint.setTextSize(rectF.width() / 8f);
            textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            String valueText = String.format("%s GB • %d%%", memoryUsed, (int)progress);
            float valueY = rectF.centerY() + rectF.width() / 16f;
            canvas.drawText(valueText, rectF.centerX(), valueY, textPaint);
        } else {
            textPaint.setTextSize(rectF.width() / 7f);
            textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            String percentText = String.format("%d%%", (int)progress);
            float percentY = rectF.centerY() + rectF.width() / 14f;
            canvas.drawText(percentText, rectF.centerX(), percentY, textPaint);
        }
    }

    public void setProgress(float progress) {
        this.progress = progress;
        utilizationGraph.initializeWithValue(progress);
    }

    public void setMemoryProgress(float progress) {
        if (isGpu) {
            // For GPU circles, this sets the GPU memory border
            setGpuMemoryPercent(progress);
        } else {
            // For main memory circle, this sets the main progress
            this.progress = progress;
            utilizationGraph.initializeWithValue(progress);
        }
    }

    public void setLabel(String label) {
        this.label = label;
        this.isGpu = label != null && label.startsWith("GPU");
        if (isGpu) {
            backgroundPaint.setStrokeWidth(12f);
            progressPaint.setStrokeWidth(6f);  // Exactly half width
            gpuMemoryPaint.setStrokeWidth(6f); // Exactly half width
        } else {
            backgroundPaint.setStrokeWidth(12f);
            progressPaint.setStrokeWidth(12f);
            gpuMemoryPaint.setStrokeWidth(12f);
        }
    }

    public void setMemoryUsed(String memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public void addUtilizationSample(float utilization) {
        utilizationGraph.addSample(utilization);
    }

    public RectF getRectF() {
        return rectF;
    }

    public boolean isGpu() {
        return isGpu;
    }

    public void setGpuMemoryPercent(float percent) {
        this.gpuMemoryPercent = percent;
    }
} 