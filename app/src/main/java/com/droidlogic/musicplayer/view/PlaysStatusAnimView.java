package com.droidlogic.musicplayer.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import me.jessyan.autosize.utils.AutoSizeUtils;

import static android.animation.ValueAnimator.INFINITE;
import static android.animation.ValueAnimator.REVERSE;

public class PlaysStatusAnimView extends View {

    private final float minR = 0.3f;
    private final float maxR = 0.7f;
    private final ValueAnimator valueAnimator = ValueAnimator.ofFloat(minR, maxR);
    private final Paint barPaint = new Paint();
    private final int barMaxNum = 3;
    private float barSpace = 0f;
    private int viewH = 0;
    private final float strokeWidth;

    public PlaysStatusAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        strokeWidth = AutoSizeUtils.dp2px(getContext(), 5f);

        barPaint.setColor(Color.WHITE);
        barPaint.setAntiAlias(true);
        barPaint.setStrokeWidth(strokeWidth);
        barPaint.setStrokeCap(Paint.Cap.ROUND);

        valueAnimator.setRepeatCount(INFINITE);
        valueAnimator.setRepeatMode(REVERSE);
        long duration = 550L;
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewH = h;
        barSpace = (w - barMaxNum * strokeWidth) / (barMaxNum - 1);
        barSpace = Math.max(0, barSpace);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float value = (float) valueAnimator.getAnimatedValue();
        float rate;
        rate = (maxR - minR) / (barMaxNum - 1);

        for (int index = 0; index < barMaxNum; index++) {
            drawBar(index, value + (index * rate), canvas);
        }
    }

    private void drawBar(int index, float value, Canvas canvas) {
        float rValue = value;
        if (value > maxR) {
            rValue = maxR * 2 - value;
        }
        if (rValue < minR) {
            rValue = minR * 2 - rValue;
        }
        float startX = index * barSpace + strokeWidth * 1.5f;
        float barHeight = rValue * viewH;
        float startY = (viewH - barHeight) / 2;
        float endY = startY + barHeight;
        canvas.drawLine(startX, startY, startX, endY, barPaint);
    }

    public void startAnim() {
        if (!valueAnimator.isStarted() || valueAnimator.isPaused()) {
            valueAnimator.addUpdateListener(animation -> invalidate());
            valueAnimator.start();
        }
    }

    public void stopAnim() {
        if (valueAnimator.isRunning()) {
            valueAnimator.pause();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        valueAnimator.end();
    }

}
