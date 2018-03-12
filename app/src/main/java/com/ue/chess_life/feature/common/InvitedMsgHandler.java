package com.ue.chess_life.feature.common;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

import com.hyphenate.chat.EMMessage;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.chess_life.R;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.constant.RouterLinks;
import com.ue.chess_life.feature.game.GameActivity;
import com.ue.chess_life.util.DialogHelper;
import com.ue.chess_life.util.GameUtils;
import com.ue.library.base.LifecycleCallbacksAdapter;
import com.ue.library.util.DateUtils;
import com.ue.library.util.ToastUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2017/11/10.
 */

public class InvitedMsgHandler {
    private static Disposable mDisposable;

    public static void handleInvitedMsg(RxAppCompatActivity mContext, EMMessage emMessage) {
        if (LifecycleCallbacksAdapter.get().hasPaused(mContext)) {
            return;
        }
        int timeout = EaseConstants.TIME_OUT_INVITE - (int) ((System.currentTimeMillis() - emMessage.getMsgTime()) / 1000);// - EaseConstants.TIME_GAP_REQ_REP;
        //超时时间判定: 请求发送失效时间-(当前时间-发送时间)/1000-接收时差，单位：秒
        if (timeout <= 0) {
            //邀请超时失效
            return;
        }

        String fragmentName = RouterLinks.FRAGMENT_ONLINE_LINK;
        int gameFlag = emMessage.getIntAttribute(EaseConstants.WHICH_GAME, -1);
        String gameName = GameUtils.getGameName(mContext, gameFlag);

        String timeStr = DateUtils.getFormatTime(emMessage.getMsgTime(), DateUtils.FORMAT_TIME);
        String msg = mContext.getString(R.string.invite_tip, timeStr, emMessage.getFrom(), gameName);

        DialogHelper.getInstance(mContext).showInvitedDialog(msg, (DialogInterface dialogInterface, int i) -> {
            dispose();
            if (TextUtils.isEmpty(fragmentName)) {
                ToastUtils.showShort(mContext, mContext.getString(R.string.try_upgrade));
                return;
            }

            //进入游戏页
            String oppoPlayer = emMessage.getStringAttribute(EaseConstants.FROM_PLAYER, "");

            Bundle arguments = new Bundle();
            arguments.putString(EaseConstants.OPPO_USER_NAME, emMessage.getFrom());
            arguments.putString(EaseConstants.FROM_PLAYER, oppoPlayer);

            GameActivity.startFromInvited(mContext, gameFlag, fragmentName, arguments);

        }, (DialogInterface dialogInterface, int i) -> {
            dispose();
        });

        mDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mContext.bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(aLong -> {
                    if (aLong < timeout) {
                        DialogHelper.getInstance(mContext).updateDoubleBtnMessage(msg + (timeout - aLong));
                        return;
                    }
                    dispose();
                    DialogHelper.getInstance(mContext).dismissDialogs();
                });
    }

    private static void dispose() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
