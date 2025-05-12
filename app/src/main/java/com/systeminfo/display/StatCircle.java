package com.systeminfo.display;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class StatCircle {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float progress = 0;
    private float padding;

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

        progressPaint = new Paint();
        progressPaint.setColor(0xFF222222); // Very dark gray/black for progress
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setAntiAlias(true);

        rectF = new RectF();
    }

    public void setSize(int width, int height) {
        rectF.set(padding, padding, width - padding, height - padding);
    }

    public void draw(Canvas canvas) {
        // Draw background circle
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);
        
        // Draw progress arc
        canvas.drawArc(rectF, -90, progress * 3.6f, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public RectF getRectF() {
        return rectF;
    }
} 