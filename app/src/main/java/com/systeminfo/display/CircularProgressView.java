package com.systeminfo.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {
    private Paint memoryPaint;
    private Paint textPaint;
    private Paint labelBackgroundPaint;
    private RectF memoryRectF;
    private RectF labelRectF;
    private float progress = 0;
    private float memoryProgress = 0;
    private String label = "";
    private String memoryUsed = "";
    private boolean isGpu = false;
    private UtilizationGraph utilizationGraph;
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
        labelBackgroundPaint = new Paint();
        labelBackgroundPaint.setColor(0xFF000000); // Black background
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
        labelBackgroundPaint.setAntiAlias(true);

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

        memoryRectF = new RectF();
        labelRectF = new RectF();
        utilizationGraph = new UtilizationGraph();
        
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
        memoryRectF.set(16f, 16f, w - 16f, h - 16f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw utilization graph as background for the usage circle (centered, clipped)
        utilizationGraph.draw(canvas, usageCircle.getRectF());

        if (isGpu) {
            // Draw memory circle
            memoryCircle.draw(canvas);
        }
        
        // Draw usage circle
        usageCircle.draw(canvas);

        // Draw label at the top (all caps, bold)
        textPaint.setTextSize(getWidth() / 14f);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        String labelText = label == null ? "" : label.toUpperCase();
        
        // Adjust vertical position based on whether it's GPU or not
        float labelY = isGpu ? getHeight() / 5.5f : getHeight() / 6f;
        
        // Calculate text bounds for the label background
        float textWidth = textPaint.measureText(labelText);
        float padding = getWidth() / 40f; // Padding around text
        float textHeight = textPaint.getTextSize();
        
        // Center the text vertically within its background
        labelRectF.set(
            getWidth() / 2f - textWidth / 2f - padding,
            labelY - textHeight / 2f - padding,
            getWidth() / 2f + textWidth / 2f + padding,
            labelY + textHeight / 2f + padding
        );
        
        // Draw black background for label
        canvas.drawRoundRect(labelRectF, padding, padding, labelBackgroundPaint);
        
        // Draw label text in white
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText(labelText, getWidth() / 2f, labelY + textHeight / 3f, textPaint);

        // Draw value in the center
        textPaint.setColor(0xFF111111); // Reset to black for percentage text
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
        usageCircle.setProgress(progress);
        utilizationGraph.initializeWithValue(progress);
        invalidate();
    }

    public void setMemoryProgress(float progress) {
        this.memoryProgress = progress;
        memoryCircle.setProgress(progress);
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

    /**
     * Add a new utilization sample (0-100).
     * Call this periodically to update the graph.
     * For GPU circles, pass GPU utilization; for others, pass CPU or relevant value.
     */
    public void addUtilizationSample(float utilization) {
        utilizationGraph.addSample(utilization);
        invalidate();
    }
} 