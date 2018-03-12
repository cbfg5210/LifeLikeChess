package com.ue.chess_life.feature.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.chess_life.R;
import com.ue.chess_life.event.InvitedEvent;
import com.ue.chess_life.feature.common.InvitedMsgHandler;
import com.ue.chess_life.feature.login.LoginActivity;
import com.ue.chess_life.util.AppUserUtils;
import com.ue.chess_life.util.DialogHelper;
import com.ue.library.util.ToastUtils;
import com.ue.resource.model.AppUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserProfileActivity extends RxAppCompatActivity {
    @BindView(R.id.profile_avatar)
    ImageView mProfileAvatar;
    @BindView(R.id.profile_username)
    TextView mProfileUsername;
    @BindView(R.id.profile_nickname)
    TextView mProfileNickname;
    @BindView(R.id.profile_sex)
    TextView mProfileSex;

    private AppUser mLocalAppUser;

    private boolean goToLogin;

    public static void start(Context context) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        setTitle(getString(R.string.user_profile));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void refreshUserInfo(AppUser localAppUser) {
        mProfileUsername.setText(localAppUser.userName);
        mProfileNickname.setText(localAppUser.userNick);
        mProfileSex.setText(localAppUser.isMale ? getString(R.string.sex_male) : getString(R.string.sex_female));
        mProfileAvatar.setSelected(localAppUser.isMale);
    }

    /**
     * 更新昵称
     */
    private void onUpdateNicknameClick() {
        DialogHelper.getInstance(this).showNewNickDialog(newNick -> {
            if (TextUtils.isEmpty(newNick)) {
                ToastUtils.showShort(UserProfileActivity.this, getString(R.string.nick_not_null));
                return;
            }
            mLocalAppUser.userNick = newNick;
            AppUserUtils.saveAppUser(mLocalAppUser);

            mProfileNickname.setText(newNick);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    private void updateSex(final boolean isMale) {
        mLocalAppUser.isMale = isMale;
        AppUserUtils.saveAppUser(mLocalAppUser);

        mProfileSex.setText(isMale ? getString(R.string.sex_male) : getString(R.string.sex_female));
        mProfileAvatar.setSelected(isMale);
    }

    @OnClick({
            R.id.ll_nick_panel,
            R.id.ll_sex_panel,
            R.id.btn_logout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_nick_panel:
                onUpdateNicknameClick();
                break;
            case R.id.ll_sex_panel:
                DialogHelper.getInstance(this).showSexOptionsDialog((AdapterView<?> adapterView, View v, int position, long l) -> {
                    updateSex(position == 0);
                });
                break;
            case R.id.btn_logout:
                EMClient.getInstance().logout(true);
                finish();
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvitedEvent(InvitedEvent invitedEvent) {
        InvitedMsgHandler.handleInvitedMsg(this, invitedEvent.invitedMsg);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (EMClient.getInstance().isLoggedInBefore()) {
            mLocalAppUser = AppUserUtils.getCurrentUser();
            refreshUserInfo(mLocalAppUser);
            return;
        }
        if (!goToLogin) {
            goToLogin = true;
            LoginActivity.start(this);
            return;
        }
        //从登录页回来但是没有登录则关闭页面
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}