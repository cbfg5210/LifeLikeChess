package com.ue.library.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper to avoid implementing all lifecycle callback methods.
 */
public class LifecycleCallbacksAdapter implements Application.ActivityLifecycleCallbacks {
    private static LifecycleCallbacksAdapter instance;

    private Application.ActivityLifecycleCallbacks lifecycleCallbacks;
    private Map<String, Boolean> pauseStatus;

    public static void init(Application app, Application.ActivityLifecycleCallbacks lifecycleCallbacks) {
        if (instance == null) {
            instance = new LifecycleCallbacksAdapter(lifecycleCallbacks);
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static LifecycleCallbacksAdapter get() {
        return instance;
    }

    private LifecycleCallbacksAdapter(Application.ActivityLifecycleCallbacks lifecycleCallbacks) {
        this.lifecycleCallbacks = lifecycleCallbacks;
        pauseStatus = new HashMap<>();
    }

    public boolean hasPaused(Activity activity) {
        if (activity == null) {
            return true;
        }
        String key = activity.getClass().getSimpleName();
        return pauseStatus.get(key);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        pauseStatus.put(activity.getClass().getSimpleName(), false);
        if (lifecycleCallbacks != null) {
            lifecycleCallbacks.onActivityCreated(activity, savedInstanceState);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (lifecycleCallbacks != null) {
            lifecycleCallbacks.onActivityStarted(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        pauseStatus.put(activity.getClass().getSimpleName(), false);
        if (lifecycleCallbacks != null) {
            lifecycleCallbacks.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        pauseStatus.put(activity.getClass().getSimpleName(), true);
        if (lifecycleCallbacks != null) {
            lifecycleCallbacks.onActivityPaused(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (lifecycleCallbacks != null) {
            lifecycleCallbacks.onActivityStopped(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (lifecycleCallbacks != null) {
            lifecycleCallbacks.onActivitySaveInstanceState(activity, outState);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (lifecycleCallbacks != null) {
            lifecycleCallbacks.onActivityDestroyed(activity);
        }
    }
}