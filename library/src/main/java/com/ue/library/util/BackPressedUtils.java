package com.ue.library.util;

import android.app.Activity;

/**
 * Created by xin on 4/14/17.
 */

public final class BackPressedUtils {
    private static long lastBackPressedTime = 0;

    private BackPressedUtils() {
    }

    public static void exitIfBackTwice(Activity activity, String toast) {
        long currentTime = System.currentTimeMillis();
        long timeD = currentTime - lastBackPressedTime;
        if (timeD >= 3000) {
            lastBackPressedTime = currentTime;
            ToastUtils.showShort(activity, toast);
            return;
        }
        activity.finish();
    }
}
