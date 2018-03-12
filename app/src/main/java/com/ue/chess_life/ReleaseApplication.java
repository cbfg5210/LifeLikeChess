package com.ue.chess_life;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.tencent.bugly.Bugly;
import com.ue.library.base.LifecycleCallbacksAdapter;
import com.ue.library.util.SPUtils;
import com.ue.recommend.util.BmobUtils;

import java.util.List;

/**
 * Created by hawk on 2017/6/8.
 */

public class ReleaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 多进程启动会多次调用 Application＃onCreate() 方法，区分进程作初始化操作，能有效减少不必要的开销
        String packageName = getPackageName();
        if (packageName.equals(getProcessName(this))) {
            // init for main process
            //lifecycle callback
            LifecycleCallbacksAdapter.init(this, null);
            //sharedPreferences
            SPUtils.init(this);
            //ease
            EaseDelegate.getInstance().init(this);
            //support svg
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
            // init bugly
            Bugly.init(this, BuildConfig.BUGLY_APP_ID, false);
            //init bmob
            BmobUtils.getInstance().initBmob(BuildConfig.BMOB_APP_ID, BuildConfig.BMOB_API_KEY);
            // init debug tools
            initDebugTools();
        } else {
            // init for other process
        }
    }

    protected void initDebugTools() {
    }

    /**
     * @param context 上下文
     * @return 当前进程名
     */
    private static String getProcessName(@NonNull Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoApp = am.getRunningAppProcesses();
        if (infoApp == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : infoApp) {
            if (proInfo.pid == Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }
}
