package com.systeminfo.display;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

public class UtilizationGraph {
    private static final int HISTORY_SIZE = 60;
    private final float[] utilizationHistory = new float[HISTORY_SIZE];
    private int historyIndex = 0;
    private boolean historyInitialized = false;

    public void addSample(float utilization) {
        utilizationHistory[historyIndex] = utilization;
        historyIndex = (historyIndex + 1) % HISTORY_SIZE;
        if (!historyInitialized && historyIndex == 0) historyInitialized = true;
    }

    public void draw(Canvas canvas, RectF bounds) {
        try {
            int save = canvas.save();
            Path clipPath = new Path();
            float cx = bounds.centerX();
            float cy = bounds.centerY();
            float radius = (bounds.width() / 2f);
            clipPath.addCircle(cx, cy, radius, Path.Direction.CW);
            canvas.clipPath(clipPath, Region.Op.INTERSECT);

            // 1. Draw a white circle as the background
            Paint whitePaint = new Paint();
            whitePaint.setColor(0xFFFFFFFF);
            whitePaint.setStyle(Paint.Style.FILL);
            whitePaint.setAntiAlias(true);
            canvas.drawCircle(cx, cy, radius * 0.96f, whitePaint); // 0.96 to avoid border overlap

            // 2. Prepare for utilization bowl path
            float margin = radius * 0.03f; // Reduced margin for tighter fit
            float graphRadius = radius - margin;
            float graphLeft = cx - graphRadius;
            float graphRight = cx + graphRadius;
            float graphBottom = cy + graphRadius;
            float graphTop = cy - graphRadius;
            float graphWidth = graphRight - graphLeft;
            float graphHeight = graphBottom - graphTop;
            float baselineY = graphBottom;
            float availableHeight = graphHeight;

            int count = historyInitialized ? HISTORY_SIZE : historyIndex;
            if (count < 2) return;
            float[] xs = new float[count];
            float[] ys = new float[count];
            float step = graphWidth / (count - 1);

            // Only sample points within the circle
            int validStart = -1, validEnd = -1;
            for (int i = 0; i < count; i++) {
                xs[i] = graphLeft + i * step;
                float relX = (xs[i] - cx) / graphRadius;
                if (Math.abs(relX) <= 1f) {
                    if (validStart == -1) validStart = i;
                    validEnd = i;
                }
                int idx = (historyIndex + i) % HISTORY_SIZE;
                float util = utilizationHistory[idx];
                if (util < 0f) util = 0f;
                if (util > 100f) util = 100f;
                float unclampedY = graphBottom - (util / 100f) * availableHeight;
                // Clamp y so it never goes below the circle's bottom edge
                float relXForClamp = (xs[i] - cx) / graphRadius;
                relXForClamp = Math.max(-1f, Math.min(1f, relXForClamp));
                float circleY = cy + graphRadius * (float)Math.sqrt(1 - relXForClamp * relXForClamp);
                ys[i] = Math.min(unclampedY, circleY);
            }
            if (validStart == -1 || validEnd == -1 || validEnd - validStart < 1) return;

            // 3. Draw the bowl path (area under the utilization line)
            Path bowlPath = new Path();
            bowlPath.moveTo(xs[validStart], ys[validStart]);
            for (int i = validStart + 1; i <= validEnd; i++) {
                bowlPath.lineTo(xs[i], ys[i]);
            }
            // Draw the bottom arc of the circle from rightmost to leftmost using fine angle steps
            float leftRelX = (xs[validStart] - cx) / graphRadius;
            float rightRelX = (xs[validEnd] - cx) / graphRadius;
            leftRelX = Math.max(-1f, Math.min(1f, leftRelX));
            rightRelX = Math.max(-1f, Math.min(1f, rightRelX));
            float leftAngle = (float)Math.acos(leftRelX);
            float rightAngle = (float)Math.acos(rightRelX);
            if (ys[validStart] < cy) leftAngle = (float)(2 * Math.PI - leftAngle);
            if (ys[validEnd] < cy) rightAngle = (float)(2 * Math.PI - rightAngle);
            int arcSteps = 60;
            for (int i = arcSteps; i >= 0; i--) {
                float t = (float)i / arcSteps;
                float angle = leftAngle + t * (rightAngle - leftAngle);
                float arcX = cx + graphRadius * (float)Math.cos(angle);
                float arcY = cy + graphRadius * (float)Math.sin(angle);
                bowlPath.lineTo(arcX, arcY);
            }
            bowlPath.close();
            Paint bowlPaint = new Paint();
            bowlPaint.setColor(0xFFF2F2F2);
            bowlPaint.setStyle(Paint.Style.FILL);
            bowlPaint.setAntiAlias(true);
            canvas.drawPath(bowlPath, bowlPaint);

            // Draw a thin border around the graph
            Paint graphBorderPaint = new Paint();
            graphBorderPaint.setColor(0xFFE0E0E0); // Slightly darker than the graph color
            graphBorderPaint.setStyle(Paint.Style.STROKE);
            graphBorderPaint.setStrokeWidth(0.5f);
            graphBorderPaint.setAntiAlias(true);
            canvas.drawPath(bowlPath, graphBorderPaint);

            // 4. Draw a thin, darker border for the graph
            Paint borderPaint = new Paint();
            borderPaint.setColor(0xFFB0B0B0);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(0.5f);
            borderPaint.setAntiAlias(true);
            canvas.drawCircle(cx, cy, graphRadius, borderPaint);

            canvas.restoreToCount(save);
        } catch (Exception e) {
            android.util.Log.e("UtilizationGraph", "Exception in draw", e);
        }
    }

    public void initializeWithValue(float value) {
        if (!historyInitialized && historyIndex == 0) {
            for (int i = 0; i < HISTORY_SIZE; i++) {
                utilizationHistory[i] = value;
            }
            historyIndex = 1;
            historyInitialized = true;
        }
    }
} 