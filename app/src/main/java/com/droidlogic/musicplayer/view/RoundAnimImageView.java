package com.droidlogic.musicplayer.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class RoundAnimImageView extends ImageView {

    private final ObjectAnimator objectAnimator;

    public RoundAnimImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        objectAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
        objectAnimator.setDuration(20000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
    }

    public void startAnim() {
        if (objectAnimator.isPaused()) {
            objectAnimator.resume();
        } else if (!objectAnimator.isStarted()) {
            objectAnimator.start();
        }
    }

    public void pauseAnim() {
        if (objectAnimator.isRunning()) {
            objectAnimator.pause();
        }
    }

    public void stopAnim() {
        objectAnimator.end();
    }

}
