package com.droidlogic.musicplayer.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.droidlogic.musicplayer.event.EventActivityState;

import org.greenrobot.eventbus.EventBus;

import static com.droidlogic.musicplayer.event.EventActivityState.State.BACKGROUND;
import static com.droidlogic.musicplayer.event.EventActivityState.State.FOREGROUND;

public class App extends Application {

    public int activityActive = 0;
    public int activityAlive = 0;
    private boolean isRunInBackground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activityAlive++;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityActive++;
                if (activityActive == 1 && isRunInBackground) {
                    EventBus.getDefault().post(new EventActivityState(FOREGROUND));
                }
                isRunInBackground = false;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityActive--;
                if (activityActive == 0) {
                    isRunInBackground = true;
                    EventBus.getDefault().post(new EventActivityState(BACKGROUND));
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                activityAlive--;
                if (activityAlive == 0) {
                    isRunInBackground = false;
                }
            }
        });

    }
}
