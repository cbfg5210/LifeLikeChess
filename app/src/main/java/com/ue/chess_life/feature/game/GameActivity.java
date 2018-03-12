package com.ue.chess_life.feature.game;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.hyphenate.chat.EMClient;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.chess_life.BuildConfig;
import com.ue.chess_life.R;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.event.InvitedEvent;
import com.ue.chess_life.event.ShowChatPageEvent;
import com.ue.chess_life.feature.ad.ADManager;
import com.ue.chess_life.feature.common.InvitedMsgHandler;
import com.ue.chess_life.feature.login.LoginActivity;
import com.ue.library.util.ToastUtils;
import com.ue.resource.constant.GameConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GameActivity extends RxAppCompatActivity {
    private static final String ARG_GAME_FRAGMENT = "arg_game_fragment";
    private static final String ARG_IS_ONLINE = "arg_is_online";
    private static final String ARG_FRAGMENT_ARGUMENTS = "arg_fragment_arguments";
    @BindView(R.id.drawer_panel)
    DrawerLayout mDrawerPanel;

    private Fragment gameFragment;
    private boolean isOnline;
    private boolean goToLogin;

    public static void start(Context context, int gameFlag, int gameMode, String gameFragment) {
        Bundle arguments = new Bundle();
        arguments.putInt(EaseConstants.ARG_GAME_FLAG, gameFlag);
        arguments.putInt(EaseConstants.ARG_GAME_MODE, gameMode);
        start(context, gameFragment, arguments, (gameMode == GameConstants.MODE_ONLINE || gameMode == GameConstants.MODE_INVITE));
    }

    public static void startFromInvited(Context context, int gameFlag, String gameFragment, Bundle arguments) {
        arguments.putInt(EaseConstants.ARG_GAME_FLAG, gameFlag);
        arguments.putInt(EaseConstants.ARG_GAME_MODE, GameConstants.MODE_INVITE);
        start(context, gameFragment, arguments, true);
    }

    private static void start(Context context, String gameFragment, Bundle arguments, boolean isOnline) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(ARG_GAME_FRAGMENT, gameFragment);
        intent.putExtra(ARG_FRAGMENT_ARGUMENTS, arguments);
        intent.putExtra(ARG_IS_ONLINE, isOnline);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        onNewIntent(getIntent());
        showBannerAd();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //onNewIntent->onResume

        isOnline = intent.getBooleanExtra(ARG_IS_ONLINE, false);
        String gameFragmentName = intent.getStringExtra(ARG_GAME_FRAGMENT);
        Bundle fragmentArguments = intent.getBundleExtra(ARG_FRAGMENT_ARGUMENTS);

        int lockMode = isOnline ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        mDrawerPanel.setDrawerLockMode(lockMode);
        //移除之前的gameFragment
        if (gameFragment != null && gameFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .remove(gameFragment)
                    .commit();
        }

        mDrawerPanel.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mDrawerPanel.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        try {
            Class fragmentClass = Class.forName(gameFragmentName);
            gameFragment = (Fragment) fragmentClass.newInstance();
            gameFragment.setArguments(fragmentArguments);
        } catch (Exception e) {
            ToastUtils.showShort(this, getString(R.string.try_upgrade));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addGameFragment();
    }

    private void addGameFragment() {
        if (gameFragment == null || gameFragment.isAdded()) {
            return;
        }
        if (isOnline && !EMClient.getInstance().isLoggedInBefore()) {
            //如果是在线模式，还没登录要前往登录
            if (goToLogin) {
                //从登录页返回但是没有登录
                finish();
                return;
            }
            goToLogin = true;
            LoginActivity.start(this);
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_game_container, gameFragment)
                .commit();
    }

    private void showBannerAd() {
        ViewGroup bannerContainer = this.findViewById(R.id.fl_game_banner_container);
        View bannerView = ADManager.getBannerView(this, BuildConfig.GDT_GAME_BANNER_ID);
        bannerContainer.addView(bannerView);
    }

    @Subscribe
    public void onShowChatPageEvent(ShowChatPageEvent showChatPageEvent) {
        mDrawerPanel.openDrawer(GravityCompat.END);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvitedEvent(InvitedEvent invitedEvent) {
        if (!isOnline) {
            InvitedMsgHandler.handleInvitedMsg(this, invitedEvent.invitedMsg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        //如果聊天页面正在显示要先关闭
        if (mDrawerPanel.isDrawerOpen(GravityCompat.END)) {
            mDrawerPanel.closeDrawer(GravityCompat.END);
            return;
        }
        if (gameFragment != null) {
            if (isOnline) {
                ((OnlineModeFragment) gameFragment).onBackPressed();
            } else {
                ((OfflineModeFragment) gameFragment).onBackPressed();
            }
            return;
        }
        super.onBackPressed();
    }
}