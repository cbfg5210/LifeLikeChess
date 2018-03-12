package com.ue.chess_life.feature.splash;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.chess_life.BuildConfig;
import com.ue.chess_life.R;
import com.ue.chess_life.feature.main.MainActivity;
import com.yanzhenjie.permission.AndPermission;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * 开屏页
 */
public class SplashActivity extends RxAppCompatActivity {
    @BindView(R.id.vgSplashAdContainer)
    ViewGroup mVgSplashAdContainer;
    @BindView(R.id.vgAppInfoContainer)
    ViewGroup vgAppInfoContainer;
    @BindView(R.id.tvSkipView)
    TextView tvSkipView;

    private boolean canJump;
    private boolean isShowingSplashAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        boolean hasPermissions = AndPermission.hasPermission(this,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasPermissions) {
            pullSplashAd();
        }
        //3秒内如果没有展示广告则跳转主页
        Observable.timer(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(aLong -> {
                    if (isShowingSplashAd) {
                        return;
                    }
                    canJump = true;
                    next();
                });
    }

    private void pullSplashAd() {
        /**
         * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
         *
         * @param activity        展示广告的activity
         * @param adContainer     展示广告的大容器
         * @param skipContainer   自定义的跳过按钮：传入该view给SDK后，SDK会自动给它绑定点击跳过事件。SkipView的样式可以由开发者自由定制，其尺寸限制请参考activity_splash.xml或者接入文档中的说明。
         * @param appId           应用ID
         * @param posId           广告位ID
         * @param adListener      广告状态监听器
         * @param fetchDelay      拉取广告的超时时长：取值范围[3000, 5000]，设为0表示使用广点通SDK默认的超时时长。
         */
        new SplashAD(this, mVgSplashAdContainer, tvSkipView, BuildConfig.GDT_APP_ID, BuildConfig.GDT_SPLASH_ID,
                new SplashADListener() {
                    @Override
                    public void onADDismissed() {
                        Log.e("AD_DEMO", "SplashADDismissed");
                        next();
                    }

                    @Override
                    public void onNoAD(AdError error) {
                        Log.e("AD_DEMO", String.format("LoadSplashADFail, eCode=%d, errorMsg=%s", error.getErrorCode(), error.getErrorMsg()));
                        //eCode=6000, errorMsg=未知错误，详细码：102006
                        isShowingSplashAd = false;
                    }

                    @Override
                    public void onADPresent() {
                        Log.e("SplashActivity", "onADPresent: ");
                        isShowingSplashAd = true;
                        tvSkipView.setVisibility(View.VISIBLE);
                        showBottomAppInfo();
                    }

                    @Override
                    public void onADClicked() {
                    }

                    @Override
                    public void onADTick(long millisUntilFinished) {
                        tvSkipView.setText(getString(R.string.skip_text, Math.round(millisUntilFinished / 1000f)));
                        Log.e("SplashActivity", "onADTick: canJump=" + canJump);
                    }
                }, 0);
    }

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     */
    private void next() {
        if (canJump) {
            MainActivity.start(this);
            finish();
        } else {
            canJump = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("SplashActivity", "onPause: ");
        canJump = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("SplashActivity", "onResume: ");
        if (canJump) {
            next();
        }
        canJump = true;
    }

    private void showBottomAppInfo() {
        vgAppInfoContainer.setVisibility(View.VISIBLE);
        /*ObjectAnimator animator = ObjectAnimator.ofFloat(vgAppInfoContainer, "alpha", 0.5f, 1f);
        animator.setDuration(3000);
        animator.start();*/
    }

    /**
     * 开屏页最好禁止用户对返回按钮的控制
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}