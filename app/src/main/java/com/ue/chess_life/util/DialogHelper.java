package com.ue.chess_life.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ue.chess_life.R;
import com.ue.chess_life.constant.ActionFlags;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.library.util.CallbackUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 封装通用的提示框，方便统一修改
 * <p>
 * 对可能相对频繁的dialog进行复用
 */
public class DialogHelper {
    private static DialogHelper instance;

    private Activity mContext;
    //noBtn singleBtn doubleBtn 三种形式的dialog在同一个页面中用到的频率相对高,进行复用;自定义类型或其它不复用
    private AlertDialog noBtnDialog;
    private AlertDialog singleBtnDialog;
    private AlertDialog doubleBtnDialog;
    private AlertDialog otherDialog;

    private DialogInterface.OnClickListener emptyListener;

    private DialogHelper() {
    }

    public static DialogHelper getInstance(Activity context) {
        if (instance == null) {
            instance = new DialogHelper();
            instance.mContext = context;
        }
        if (instance.mContext != context) {
            //destroy
            instance.dismissDialogs();
            instance.noBtnDialog = null;
            instance.singleBtnDialog = null;
            instance.doubleBtnDialog = null;
            instance.otherDialog = null;
            //////
            instance.mContext = context;
        }
        return instance;
    }

    public void dismissDialogs() {
        dismissDialog(noBtnDialog);
        dismissDialog(singleBtnDialog);
        dismissDialog(doubleBtnDialog);
        dismissDialog(otherDialog);
    }

    private void dismissDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /**************noBtnDialog**************/

    public void showOnlyTipDialog(@StringRes int msgRes, boolean isCancelable) {
        String msg = msgRes == 0 ? null : mContext.getString(msgRes);
        showOnlyTipDialog(msg, isCancelable);
    }

    public void showOnlyTipDialog(String msg, boolean isCancelable) {
        if (!CallbackUtils.isActivityValid(mContext)) {
            return;
        }
        dismissDialogs();

        if (noBtnDialog == null) {
            noBtnDialog = new AlertDialog.Builder(mContext).create();
        }
        noBtnDialog.setMessage(msg);
        noBtnDialog.setCancelable(isCancelable);

        noBtnDialog.show();
    }

    public void showOnlyTipDialog(String msg) {
        showOnlyTipDialog(msg, false);
    }

    public void updateNoBtnMessage(String msgRes) {
        if (noBtnDialog != null) {
            noBtnDialog.setMessage(msgRes);
        }
    }

    /*******************/


    /**************singleBtnDialog**************/

    public void showSingleBtnDialog(@StringRes int titleRes, @StringRes int msgRes, @StringRes int btnTxtRes) {
        if (!CallbackUtils.isActivityValid(mContext)) {
            return;
        }
        dismissDialogs();

        if (singleBtnDialog == null) {
            singleBtnDialog = new AlertDialog.Builder(mContext).create();
            singleBtnDialog.setCancelable(false);
        }
        String title = titleRes == 0 ? null : mContext.getString(titleRes);
        String msg = msgRes == 0 ? null : mContext.getString(msgRes);
        String btnTxt = btnTxtRes == 0 ? null : mContext.getString(btnTxtRes);

        singleBtnDialog.setTitle(title);
        singleBtnDialog.setMessage(msg);
        singleBtnDialog.setButton(DialogInterface.BUTTON_POSITIVE, btnTxt, emptyListener);

        Button positiveBtn = singleBtnDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveBtn != null) {
            positiveBtn.setText(btnTxt);
        }

        singleBtnDialog.show();
    }

    public void showAccountConflictDialog() {
        showSingleBtnDialog(R.string.prompt, R.string.connect_conflict, R.string.got_it);
    }

    /*******************/


    /**************doubleBtnDialog**************/

    private void showDoubleBtnDialog(@StringRes int titleRes, String message,
                                     @StringRes int positiveBtnRes, DialogInterface.OnClickListener positiveListener,
                                     @StringRes int negativeBtnRes, DialogInterface.OnClickListener negativeListener) {

        if (!CallbackUtils.isActivityValid(mContext)) {
            return;
        }
        dismissDialogs();

        if (doubleBtnDialog == null) {
            doubleBtnDialog = new AlertDialog.Builder(mContext).create();
            doubleBtnDialog.setCancelable(false);
        }

        String title = titleRes == 0 ? null : mContext.getString(titleRes);
        String positiveTxt = positiveBtnRes == 0 ? null : mContext.getString(positiveBtnRes);
        String negativeTxt = negativeBtnRes == 0 ? null : mContext.getString(negativeBtnRes);

        doubleBtnDialog.setTitle(title);
        doubleBtnDialog.setMessage(message);
        //setButton方法，text不更新
        doubleBtnDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveTxt, positiveListener);
        doubleBtnDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeTxt, negativeListener);
        //获取button,设置新的text
        Button positiveBtn = doubleBtnDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeBtn = doubleBtnDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (positiveBtn != null) {
            positiveBtn.setText(positiveTxt);
        }
        if (negativeBtn != null) {
            negativeBtn.setText(negativeTxt);
        }

        doubleBtnDialog.show();
    }

    private void showDoubleBtnDialog(@StringRes int titleRes, @StringRes int messageRes,
                                     @StringRes int positiveBtnRes, DialogInterface.OnClickListener positiveListener,
                                     @StringRes int negativeBtnRes, DialogInterface.OnClickListener negativeListener) {
        showDoubleBtnDialog(titleRes, mContext.getString(messageRes), positiveBtnRes, positiveListener, negativeBtnRes, negativeListener);
    }

    public void showOfflineExitDialog() {
        showDoubleBtnDialog(R.string.tip_title, R.string.is_exit_game,
                R.string.sure, (DialogInterface dialogInterface, int i) -> {
                    mContext.finish();
                },
                R.string.cancel, null);
    }

    public void showOnlineExitDialog(String oppoName) {
        showDoubleBtnDialog(R.string.tip_title, R.string.is_exit_game,
                R.string.sure, (DialogInterface dialogInterface, int i) -> {
                    if (!TextUtils.isEmpty(oppoName)) {
                        GameUtils.sendCMDMessage(oppoName, ActionFlags.LEAVE, null);
                    }
                    mContext.finish();
                },
                R.string.cancel, null);
    }

    public void showSurrenderTipDialog(DialogInterface.OnClickListener listener) {
        showDoubleBtnDialog(R.string.tip_title, R.string.is_surrender, R.string.sure, listener, R.string.cancel, null);
    }

    public void updateDoubleBtnMessage(String msgRes) {
        if (doubleBtnDialog != null) {
            doubleBtnDialog.setMessage(msgRes);
        }
    }

    public void showInvitedDialog(String msg, DialogInterface.OnClickListener acceptListener, DialogInterface.OnClickListener refuseListener) {
        showDoubleBtnDialog(R.string.game_invitation, msg, R.string.accept, acceptListener, R.string.refuse, refuseListener);
    }

    public void showReInviteDialog(DialogInterface.OnClickListener listener) {
        showDoubleBtnDialog(R.string.tip_title, R.string.re_invite_tip, R.string.sure, listener, R.string.cancel, null);
    }

    public void showChangeDeskDialog(DialogInterface.OnClickListener listener) {
        showDoubleBtnDialog(R.string.tip_title, R.string.change_desk_tip, R.string.sure, listener, R.string.cancel, null);
    }

    public void showChangeRoomDialog(DialogInterface.OnClickListener listener) {
        showDoubleBtnDialog(R.string.tip_title, R.string.change_room_tip, R.string.sure, listener, R.string.cancel, null);
    }

    public void showDrawDialog(String oppoUserName, DialogInterface.OnClickListener agreeListener, DialogInterface.OnClickListener disagreeListener) {
        showDoubleBtnDialog(R.string.draw_request, R.string.draw_tip,
                R.string.agree, (DialogInterface dialogInterface, int i) -> {
                    //同意和棋
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put(EaseConstants.ATTR_CODE, EaseConstants.CODE_ACCEPT);
                    GameUtils.sendCMDMessage(oppoUserName, ActionFlags.REP_DRAW, attrs);

                    agreeListener.onClick(dialogInterface, i);
                },
                R.string.disagree, (DialogInterface dialogInterface, int i) -> {
                    //不同意和棋
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put(EaseConstants.ATTR_CODE, EaseConstants.CODE_REFUSE);
                    GameUtils.sendCMDMessage(oppoUserName, ActionFlags.REP_DRAW, attrs);

                    disagreeListener.onClick(dialogInterface, i);
                });
    }

    public void showOppoUndoDialog(String oppoUserName, DialogInterface.OnClickListener agreeListener, DialogInterface.OnClickListener disagreeListener) {
        showDoubleBtnDialog(R.string.undo_request, R.string.undo_tip,
                R.string.agree, (DialogInterface dialogInterface, int i) -> {
                    //同意悔棋
                    Map<String, Object> undoAttrs = new HashMap<>();
                    undoAttrs.put(EaseConstants.ATTR_CODE, EaseConstants.CODE_ACCEPT);
                    GameUtils.sendCMDMessage(oppoUserName, ActionFlags.REP_UNDO, undoAttrs);

                    agreeListener.onClick(dialogInterface, i);
                },
                R.string.disagree, (DialogInterface dialogInterface, int i) -> {
                    //不同意悔棋
                    Map<String, Object> undoAttrs = new HashMap<>();
                    undoAttrs.put(EaseConstants.ATTR_CODE, EaseConstants.CODE_REFUSE);
                    GameUtils.sendCMDMessage(oppoUserName, ActionFlags.REP_UNDO, undoAttrs);

                    disagreeListener.onClick(dialogInterface, i);
                });
    }

    public void showOppoExitDialog(DialogInterface.OnClickListener onClickListener) {
        showDoubleBtnDialog(R.string.tip_title, R.string.oppo_exit_game,
                R.string.go_on, onClickListener,
                R.string.exit, (DialogInterface dialogInterface, int i) -> {
                    mContext.finish();
                });
    }

    public void showToDrawDialog(DialogInterface.OnClickListener onClickListener) {
        showDoubleBtnDialog(R.string.tip_title, R.string.draw_sure, R.string.sure, onClickListener, R.string.cancel, null);
    }

    public void showToUndoDialog(DialogInterface.OnClickListener onClickListener) {
        showDoubleBtnDialog(R.string.tip_title, R.string.undo_sure, R.string.sure, onClickListener, R.string.cancel, null);
    }

    /*******************/


    /**************自定义contentView dialog**************/

    private void showInputDialog(@StringRes int titleRes, OnStringResultListener onStrResultListener) {
        if (!CallbackUtils.isActivityValid(mContext)) {
            return;
        }
        dismissDialogs();

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_input, null);
        EditText etInput = contentView.findViewById(R.id.etInput);

        String title = titleRes == 0 ? null : mContext.getString(titleRes);

        otherDialog = new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setView(contentView)
                .setPositiveButton(R.string.sure, (DialogInterface dialogInterface, int i) -> {
                    String text = etInput.getText().toString().trim();
                    if (onStrResultListener != null) {
                        onStrResultListener.onResult(text);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        otherDialog.show();
    }

    public void showNewNickDialog(OnStringResultListener onStrResultListener) {
        showInputDialog(R.string.input_new_nick, onStrResultListener);
    }

    public void showInvitePlayerDialog(OnStringResultListener onStrResultListener) {
        showInputDialog(R.string.oppo_user_name, onStrResultListener);
    }

    private void showOptionsDialog(String titleRes, String[] options, @LayoutRes int itemLayoutRes, AdapterView.OnItemClickListener itemClickListener) {
        if (!CallbackUtils.isActivityValid(mContext)) {
            return;
        }
        dismissDialogs();

        ListView listView = new ListView(mContext);
        otherDialog = new AlertDialog.Builder(mContext)
                .setTitle(titleRes)
                .setView(listView)
                .create();

        ArrayAdapter adapter = new ArrayAdapter<>(mContext, itemLayoutRes, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            otherDialog.dismiss();
            itemClickListener.onItemClick(adapterView, view, i, l);
        });

        otherDialog.show();
    }

    public void showSexOptionsDialog(AdapterView.OnItemClickListener itemClickListener) {
        String[] sexOptions = new String[]{
                mContext.getString(R.string.sex_male),
                mContext.getString(R.string.sex_female)
        };
        showOptionsDialog(null, sexOptions, R.layout.item_room_option, itemClickListener);
    }

    public void showRoomOptionsDialog(int currentRoom, AdapterView.OnItemClickListener itemClickListener) {
        String[] rooms = mContext.getResources().getStringArray(R.array.online_rooms);
        String title = mContext.getString(R.string.choose_room, rooms[currentRoom]);

        showOptionsDialog(title, rooms, R.layout.item_room_option, itemClickListener);
    }

    /*******************/

    public interface OnStringResultListener {
        void onResult(String result);
    }
}
