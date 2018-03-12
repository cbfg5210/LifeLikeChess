package com.ue.chess_life.feature.main;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.chess_life.R;
import com.ue.chess_life.event.InvitedEvent;
import com.ue.chess_life.feature.about.AboutActivity;
import com.ue.chess_life.feature.common.InvitedMsgHandler;
import com.ue.chess_life.feature.user.UserProfileActivity;
import com.ue.library.util.IntentUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 邀请通知、关于(带反馈)、个人资料
 */
public class MainActivity extends RxAppCompatActivity {
    private static final int REQ_CODE_AD_PERMISSIONS = 200;

    private MainFragment fragmentMain;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.sel_avatar);

        fragmentMain = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMain);
        checkAdPermissions();
    }

    private void checkAdPermissions() {
        AndPermission.with(this)
                .requestCode(REQ_CODE_AD_PERMISSIONS)
                .permission(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        if (requestCode != REQ_CODE_AD_PERMISSIONS) {
                            return;
                        }
                        // 是否有不再提示并拒绝的权限。
                        if (!AndPermission.hasAlwaysDeniedPermission(MainActivity.this, deniedPermissions)) {
                            return;
                        }
                        showPermissionTipDialog();
                    }
                })
                .start();
    }

    private void showPermissionTipDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.no_ad_permissions))
                .setMessage(getString(R.string.ad_permissions_tip))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.go_settings), (DialogInterface dialogInterface, int i) -> {
                    IntentUtils.forwardAppDetailPage(MainActivity.this);
                })
                .create()
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvitedEvent(InvitedEvent invitedEvent) {
        InvitedMsgHandler.handleInvitedMsg(this, invitedEvent.invitedMsg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            UserProfileActivity.start(this);
            return true;
        }
        if (itemId == R.id.actionAbout) {
            AboutActivity.start(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fragmentMain != null) {
            fragmentMain.onBackPressed();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}