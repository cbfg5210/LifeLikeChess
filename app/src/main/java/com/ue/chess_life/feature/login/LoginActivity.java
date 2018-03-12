/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ue.chess_life.feature.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.chess_life.R;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.entity.ResponseResult;
import com.ue.chess_life.util.AppUserUtils;
import com.ue.chess_life.util.DialogHelper;
import com.ue.chess_life.widget.ClearEditText;
import com.ue.library.util.NetworkUtils;
import com.ue.library.util.ToastUtils;
import com.ue.resource.model.AppUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Login screen
 */
public class LoginActivity extends RxAppCompatActivity {
    @BindView(R.id.tvUserName)
    ClearEditText mTvUserName;
    @BindView(R.id.tvPassword)
    ClearEditText mTvPassword;
    @BindView(R.id.ivPswVisible)
    ImageView mIvPswVisible;
    @BindView(R.id.btnSignUpIn)
    Button mBtnSignUpIn;

    private boolean isRegisterPage;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setTitle(getString(R.string.login));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String currentUser = AppUserUtils.getLastUsername();
        if (!TextUtils.isEmpty(currentUser)) {
            mTvUserName.setText(currentUser);
        }

        if (getIntent().getBooleanExtra(EaseConstants.IS_CONFLICT, false)) {
            DialogHelper.getInstance(this).showAccountConflictDialog();
        }
    }

    private boolean isInfoValid(String userName, String password) {
        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showShort(this, getString(R.string.network_unavailable));
            return false;
        }
        if (TextUtils.isEmpty(userName)) {
            ToastUtils.showShort(this, getString(R.string.input_username));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtils.showShort(this, getString(R.string.input_password));
            return false;
        }
        return true;
    }

    private void register(String userName, String password) {
        DialogHelper.getInstance(this).showOnlyTipDialog(getString(R.string.Is_the_registered));

        Observable
                .create((ObservableEmitter<ResponseResult> e) -> {
                    try {
                        EMClient.getInstance().createAccount(userName, password);
                        AppUserUtils.setLastUserName(userName);

                        AppUser appUser = new AppUser();
                        appUser.isMale = true;
                        appUser.userName = userName;
                        appUser.userNick = userName;
                        AppUserUtils.saveAppUser(appUser);

                        e.onNext(new ResponseResult(ResponseResult.CODE_SUCCESS, null));

                    } catch (HyphenateException exp) {
                        String errMsg = getErrMsgByCode(exp.getErrorCode());
                        e.onNext(new ResponseResult(ResponseResult.CODE_FAILURE, getString(R.string.register_failed) + ":" + errMsg));
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(responseResult -> {
                    if (responseResult.code == ResponseResult.CODE_FAILURE) {
                        DialogHelper.getInstance(LoginActivity.this).dismissDialogs();
                        ToastUtils.showShort(LoginActivity.this, responseResult.msg);
                        return;
                    }
                    login(true, userName, password);
                });
    }

    private String getErrMsgByCode(int errorCode) {
        if (errorCode == EMError.NETWORK_ERROR) {
            return getString(R.string.network_anomalies);
        }
        if (errorCode == EMError.USER_ALREADY_EXIST) {
            return getString(R.string.user_already_exist);
        }
        if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
            return getString(R.string.authentication_failed);
        }
        if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
            return getString(R.string.illegal_user_name);
        }
        return getString(R.string.unknow_error, errorCode);
    }

    private void login(boolean fromRegister, String userName, String password) {
        DialogHelper.getInstance(this).showOnlyTipDialog(getString(fromRegister ? R.string.register_success_login : R.string.is_landing));
        // reset current user name before login
        AppUserUtils.setLastUserName(userName);

        Observable
                .create((ObservableEmitter<ResponseResult> e) -> {
                    EMClient.getInstance().login(userName, password, new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            e.onNext(new ResponseResult(ResponseResult.CODE_SUCCESS, null));
                            e.onComplete();
                        }

                        @Override
                        public void onError(int i, final String s) {
                            e.onNext(new ResponseResult(ResponseResult.CODE_FAILURE, getString(R.string.Login_failed) + s));
                            e.onComplete();
                        }

                        @Override
                        public void onProgress(int i, String s) {
                        }
                    });
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(responseResult -> {
                    DialogHelper.getInstance(LoginActivity.this).dismissDialogs();
                    if (responseResult.code == ResponseResult.CODE_FAILURE) {
                        ToastUtils.showShort(LoginActivity.this, responseResult.msg);
                        return;
                    }
                    finish();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (itemId == R.id.menu_login_register) {
            isRegisterPage = !isRegisterPage;
            if (isRegisterPage) {
                item.setTitle(getString(R.string.login));
                setTitle(getString(R.string.register));
                mBtnSignUpIn.setText(getString(R.string.register));
            } else {
                item.setTitle(getString(R.string.register));
                setTitle(getString(R.string.login));
                mBtnSignUpIn.setText(getString(R.string.login));
            }
        }
        return true;
    }

    @OnClick({R.id.ivPswVisible, R.id.btnSignUpIn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivPswVisible:
                togglePswVisibility();
                break;
            case R.id.btnSignUpIn:
                signUpIn();
                break;
        }
    }

    private void togglePswVisibility() {
        boolean isVisible = !mIvPswVisible.isSelected();
        mIvPswVisible.setSelected(isVisible);
        mTvPassword.setTransformationMethod(isVisible ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
    }

    private void signUpIn() {
        if (!NetworkUtils.isNetworkConnected(LoginActivity.this)) {
            DialogHelper.getInstance(this).showSingleBtnDialog(R.string.prompt, R.string.network_unavailable, R.string.got_it);
            return;
        }

        String userName = mTvUserName.getText().toString().trim();
        String password = mTvPassword.getText().toString().trim();
        if (!isInfoValid(userName, password)) {
            return;
        }
        if (isRegisterPage) {
            register(userName, password);
        } else {
            login(false, userName, password);
        }
    }
}