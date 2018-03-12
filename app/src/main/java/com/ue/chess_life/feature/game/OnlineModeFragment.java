package com.ue.chess_life.feature.game;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.google.gson.reflect.TypeToken;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.transitionseverywhere.TransitionManager;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxFragment;
import com.ue.chess_life.R;
import com.ue.chess_life.constant.ActionFlags;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.entity.GameConfig;
import com.ue.chess_life.entity.ResponseResult;
import com.ue.chess_life.event.ChangePlayerEvent;
import com.ue.chess_life.event.EMMessageEventAdapter;
import com.ue.chess_life.event.ShowChatPageEvent;
import com.ue.chess_life.event.ToastChatEvent;
import com.ue.chess_life.util.AppUserUtils;
import com.ue.chess_life.util.DialogHelper;
import com.ue.chess_life.util.EaseSmileUtils;
import com.ue.chess_life.util.GameUtils;
import com.ue.chess_life.widget.ChessBoardView;
import com.ue.chess_life.widget.UserPanelView;
import com.ue.library.util.DateUtils;
import com.ue.library.util.SPUtils;
import com.ue.library.util.ToastUtils;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.constant.GameResults;
import com.ue.resource.event.PlayListener;
import com.ue.resource.model.AppUser;
import com.ue.resource.util.GsonHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2016/12/25.
 */

public class OnlineModeFragment extends RxFragment {
    private static final int TIME_OUT_R_INVITE = 5;
    private static final int TIME_OUT_REQUEST = 10;
    private static final int ACTION_INVITE = 1;
    private static final int ACTION_UNDO = 3;
    private static final int ACTION_DRAW = 4;

    @BindView(R.id.upvUserPanel)
    UserPanelView mUpvUserPanel;
    @BindView(R.id.cbvGameBoard)
    ChessBoardView mCbvGameBoard;

    private ViewGroup vgGameMenuPanel;
    @BindView(R.id.vgGameMenu)
    ViewGroup vgGameMenu;
    @BindView(R.id.btnStartGame)
    View mBtnStartGame;

    Unbinder unbinder;

    private boolean isInvitedByMe;
    private String myChessColor;
    private String oppoChessColor;
    private boolean isOppoStarted;
    private boolean isIStarted;

    private long enterTime;
    private String oppoUserName;
    private AppUser mePlayer;
    private AppUser oppoPlayer;

    private GameConfig mGameConfig;

    private Disposable actionDisposable;//邀请倒计时、和棋倒计时、悔棋倒计时
    private int gameMode;
    private boolean isOnline;//true:online false:invite

    private String gameName;

    private String roomId;
    private boolean isInAdoptStatus;
    private long lastSentInviteTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null) {
            getActivity().finish();
            return;
        }
        int gameFlag = arguments.getInt(EaseConstants.ARG_GAME_FLAG, -1);
        if (gameFlag == -1) {
            getActivity().finish();
            return;
        }
        mGameConfig = new GameConfig(getContext(), gameFlag);
        mePlayer = AppUserUtils.getCurrentUser();

        gameMode = arguments.getInt(EaseConstants.ARG_GAME_MODE, GameConstants.MODE_ONLINE);
        isOnline = gameMode == GameConstants.MODE_ONLINE;

        //从主页应邀进入，对方点击开始的消息时间比enterTime早
        enterTime = System.currentTimeMillis() - 10000;
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_game, container, false);

        int menuLayoutId = isOnline ? R.id.vsMenuOnline : R.id.vsMenuInvite;
        vgGameMenuPanel = (ViewGroup) ((ViewStub) layoutView.findViewById(menuLayoutId)).inflate();

        unbinder = ButterKnife.bind(this, layoutView);

        mCbvGameBoard.init(mGameConfig.getGameFlag(), gameMode, new PlayListener() {
            @Override
            public void onPlayed(boolean isIPlay, int[] data, boolean isMyTurn) {
                if (isIPlay) {
                    EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                    EMCmdMessageBody cmdBody = new EMCmdMessageBody("" + ActionFlags.DATA);
                    cmdMsg.addBody(cmdBody);
                    cmdMsg.setTo(oppoUserName);
                    cmdMsg.setAttribute(EaseConstants.GAME_DATA, GsonHolder.getGson().toJson(data));

                    EMClient.getInstance().chatManager().sendMessage(cmdMsg);
                }
                updatePanel();
            }

            @Override
            public void onGameOver(int resultFlag) {
                gameOver(resultFlag);
            }

            @Override
            public void onGameDataReset(boolean isIFirst, boolean isMyTurn) {
            }

            @Override
            public void onUndo(boolean isMyTurn) {
                updatePanel();
            }
        });

        mUpvUserPanel.setMePlayer(mePlayer);

        if (isOnline) {
            initOnlineMode();
        } else {
            initInviteMode();
            if (oppoPlayer != null) {
                mUpvUserPanel.setOppoPlayer(oppoPlayer);
            } else {
                showInviteDialog();
            }
        }

        return layoutView;
    }

    private void updatePanel() {
        mUpvUserPanel.updateFocus(mCbvGameBoard.isMyTurn());
        int[] scores = mCbvGameBoard.getScores();
        if (scores != null) {
            mUpvUserPanel.updateMyScoreInfo(myChessColor + scores[0]);
            mUpvUserPanel.updateOppoScoreInfo(oppoChessColor + scores[1]);
        }
    }

    private void clickStartBtn(boolean isMyStart) {
        if (!isIStarted && !isOppoStarted) {
            if (isInvitedByMe) {
                myChessColor = mGameConfig.getFirstColor();
                oppoChessColor = mGameConfig.getLastColor();
            } else {
                myChessColor = mGameConfig.getLastColor();
                oppoChessColor = mGameConfig.getFirstColor();
            }
        }

        if (isMyStart) {
            GameUtils.sendCMDMessage(oppoUserName, ActionFlags.START, null);

            isIStarted = true;
            toggleStartBtn(true);

            //以下的两个updateMyScoreInfo不能统一为一个放到底下
            if (isOppoStarted) {
                mUpvUserPanel.updateMyScoreInfo(myChessColor);
                startGame();
                return;
            }
            //oppo not started
            mCbvGameBoard.initChessBoard(isInvitedByMe);
            mUpvUserPanel.updateMyScoreInfo(myChessColor);
            return;
        }
        //oppo click start
        isOppoStarted = true;
        //以下的两个updateMyScoreInfo不能统一为一个放到底下
        if (isIStarted) {
            mUpvUserPanel.updateOppoScoreInfo(oppoChessColor);
            startGame();
            return;
        }
        //i not started
        mCbvGameBoard.initChessBoard(isInvitedByMe);
        mUpvUserPanel.updateOppoScoreInfo(oppoChessColor);
    }

    private void startGame() {
        mCbvGameBoard.startGame(isInvitedByMe);
        updatePanel();
        isOppoStarted = false;
        isIStarted = false;
        isInvitedByMe = !isInvitedByMe;
    }

    private void toggleStartBtn(boolean isStartGame) {
        TransitionManager.beginDelayedTransition(vgGameMenuPanel);
        if (isStartGame) {
            mBtnStartGame.setVisibility(View.GONE);
            vgGameMenu.setVisibility(View.VISIBLE);
        } else {
            mBtnStartGame.setVisibility(View.VISIBLE);
            vgGameMenu.setVisibility(View.GONE);
        }
    }

    private void gameOver(int resultFlag) {
        mCbvGameBoard.stopGame();
        toggleStartBtn(false);

        isOppoStarted = false;
        isIStarted = false;

        GameResultDialog.newInstance(gameMode, resultFlag)
                .show(getChildFragmentManager(), "");
    }

    @Optional
    @OnClick({R.id.btnExit, R.id.btnChat, R.id.btnSurrender, R.id.btnDraw, R.id.btnUndo, R.id.btnInvite, R.id.btnStartGame, R.id.btnLevel, R.id.btnChange})
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.btnExit:
                onBackPressed();
                break;

            case R.id.btnChat:
                EventBus.getDefault().post(new ShowChatPageEvent());
                break;

            case R.id.btnSurrender:
                if (mCbvGameBoard.isGaming()) {
                    DialogHelper.getInstance(getActivity()).showSurrenderTipDialog((DialogInterface dialogInterface, int i) -> {
                        GameUtils.sendCMDMessage(oppoUserName, ActionFlags.SURRENDER, null);
                        gameOver(GameResults.I_SURRENDER);
                    });
                }
                break;

            case R.id.btnDraw:
                if (mCbvGameBoard.isGaming()) {
                    DialogHelper.getInstance(getActivity()).showToDrawDialog((DialogInterface dialogInterface, int i) -> {
                        GameUtils.sendCMDMessage(oppoUserName, ActionFlags.REQ_DRAW, null);
                        countDownTime(ACTION_DRAW, getString(R.string.sending_draw_request), TIME_OUT_REQUEST, R.string.draw_time_out);
                    });
                }
                break;

            case R.id.btnUndo:
                if (mCbvGameBoard.isGaming() && mCbvGameBoard.isUndoable()) {
                    DialogHelper.getInstance(getActivity()).showToUndoDialog((DialogInterface dialogInterface, int i) -> {
                        GameUtils.sendCMDMessage(oppoUserName, ActionFlags.REQ_UNDO, null);
                        countDownTime(ACTION_UNDO, getString(R.string.sending_undo_request), TIME_OUT_REQUEST, R.string.undo_time_out);
                    });
                }
                break;

            case R.id.btnInvite:
                onInviteClicked();
                break;

            case R.id.btnStartGame:
                if (TextUtils.isEmpty(oppoUserName)) {
                    if (isOnline) {
                        DialogHelper.getInstance(getActivity()).showOnlyTipDialog(R.string.finding_player, true);
                        return;
                    }
                    showInviteDialog();
                    return;
                }
                clickStartBtn(true);
                break;

            case R.id.btnLevel:
                if (!isInAdoptStatus && !mCbvGameBoard.isGaming()) {
                    selectRoomLevel();
                    return;
                }
                DialogHelper.getInstance(getActivity()).showChangeRoomDialog((DialogInterface dialogInterface, int i) -> {
                    selectRoomLevel();
                });
                break;

            case R.id.btnChange:
                if (!isInAdoptStatus && !mCbvGameBoard.isGaming()) {
                    //提醒，当前正在游戏中，确定换桌吗
                    changeDesk();
                    return;
                }
                DialogHelper.getInstance(getActivity()).showChangeDeskDialog((DialogInterface dialogInterface, int i) -> {
                    changeDesk();
                });
                break;
        }
    }

    /*************only used by online mode**************/

    private void initOnlineMode() {
        int roomLevel = SPUtils.getInt(mGameConfig.getSpRoomLevelKey(), EaseConstants.ROOM_PRIMARY);
        roomId = roomLevel == EaseConstants.ROOM_PRIMARY ? mGameConfig.getRoomOne() : mGameConfig.getRoomTwo();
        joinGameRoom(roomId);
    }

    private void joinGameRoom(String roomId) {
        Log.e("OnlineModeFragment", "joinGameRoom: roomId=" + roomId);
        DialogHelper.getInstance(getActivity()).showOnlyTipDialog(getString(R.string.sending_room));

        Observable
                .create((ObservableEmitter<ResponseResult> e) -> {
                    EMClient.getInstance().chatroomManager().joinChatRoom(roomId, new EMValueCallBack<EMChatRoom>() {
                        @Override
                        public void onSuccess(EMChatRoom emChatRoom) {
                            e.onNext(new ResponseResult(ResponseResult.CODE_SUCCESS, null));
                            e.onComplete();
                        }

                        @Override
                        public void onError(int i, final String s) {
                            e.onNext(new ResponseResult(ResponseResult.CODE_FAILURE, s));
                            e.onComplete();
                        }
                    });
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(result -> {
                    if (result.code == ResponseResult.CODE_SUCCESS) {
                        changeDesk();
                        return;
                    }
                    //dialog,传送失败，重试/退出
                    DialogHelper.getInstance(getActivity()).dismissDialogs();
                    ToastUtils.showShort(getContext(), getString(R.string.error_enter_room) + result.msg);
                    getActivity().finish();
                });
    }

    private void sendRandomInvitation(String roomId) {
        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody cmdBody = new EMCmdMessageBody("" + ActionFlags.R_INVITE);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(roomId);
        cmdMsg.setChatType(EMMessage.ChatType.ChatRoom);
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);
        lastSentInviteTime = System.currentTimeMillis();

        dispose();
        actionDisposable = Observable.timer(TIME_OUT_R_INVITE, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(aLong -> {
                    sendRandomInvitation(roomId);
                });
    }

    private void showOppoExitDialog() {
        DialogHelper.getInstance(getActivity()).showOppoExitDialog((DialogInterface dialogInterface, int i) -> {
            oppoUserName = null;
            isInAdoptStatus = false;
            changeDesk();
        });
    }

    private void changeDesk() {
        if (isInAdoptStatus || mCbvGameBoard.isGaming()) {
            GameUtils.sendCMDMessage(oppoUserName, ActionFlags.LEAVE, null);
            mCbvGameBoard.stopGame();
            isInAdoptStatus = false;
            oppoUserName = null;
            mUpvUserPanel.setOppoPlayer(null);
        }
        toggleStartBtn(false);
        DialogHelper.getInstance(getActivity()).showOnlyTipDialog(R.string.finding_player, true);
        sendRandomInvitation(roomId);
    }

    private void selectRoomLevel() {
        int curRoomLevel = SPUtils.getInt(mGameConfig.getSpRoomLevelKey(), EaseConstants.ROOM_PRIMARY);
        DialogHelper.getInstance(getActivity()).showRoomOptionsDialog(curRoomLevel, ((adapterView, view, position, l) -> {
            int newLevel = position == 0 ? EaseConstants.ROOM_PRIMARY : EaseConstants.ROOM_HIGH;
            if (curRoomLevel == newLevel) {
                return;
            }
            //保存当前选择的级别
            SPUtils.putInt(mGameConfig.getSpRoomLevelKey(), newLevel);
            //退出当前房间
            EMClient.getInstance().chatroomManager().leaveChatRoom(roomId);

            if (isInAdoptStatus || mCbvGameBoard.isGaming()) {
                GameUtils.sendCMDMessage(oppoUserName, ActionFlags.LEAVE, null);
                isInAdoptStatus = false;
                oppoUserName = null;
                mUpvUserPanel.setOppoPlayer(null);
                mCbvGameBoard.stopGame();
            }
            toggleStartBtn(false);
            //加入新房间
            roomId = newLevel == EaseConstants.ROOM_PRIMARY ? mGameConfig.getRoomOne() : mGameConfig.getRoomTwo();
            joinGameRoom(roomId);
        }));
    }

    private void onRInviteReceived(EMMessage message) {
        if (isInAdoptStatus || mCbvGameBoard.isGaming()) {
            return;
        }
        if (System.currentTimeMillis() - lastSentInviteTime <= 2500) {
            //在发送邀请两秒半内，不接受其它邀请
            return;
        }
        GameUtils.sendCMDMessage(message.getFrom(), ActionFlags.ACCEPT, null);
    }

    private void onAcceptReceived(EMMessage message) {
        if (isInAdoptStatus || mCbvGameBoard.isGaming()) {
            return;
        }
        //不再对其它用户发送通过消息，只发送一个通过消息
        isInAdoptStatus = true;
        dispose();

        Map attrs = new HashMap<>();
        attrs.put(EaseConstants.FROM_PLAYER, mePlayer.toString());
        GameUtils.sendCMDMessage(message.getFrom(), ActionFlags.ADOPT, attrs);
    }

    private void onAdoptReceived(EMMessage message) {
        if (isInAdoptStatus || mCbvGameBoard.isGaming()) {
            GameUtils.sendCMDMessage(message.getFrom(), ActionFlags.LEAVE, null);
            return;
        }
        isInAdoptStatus = true;
        dispose();

        isInvitedByMe = false;
        oppoUserName = message.getFrom();

        oppoPlayer = getPlayer(message.getStringAttribute(EaseConstants.FROM_PLAYER, null), oppoUserName);
        mUpvUserPanel.setOppoPlayer(oppoPlayer);

        Map attrs = new HashMap<>();
        attrs.put(EaseConstants.FROM_PLAYER, mePlayer.toString());
        GameUtils.sendCMDMessage(oppoUserName, ActionFlags.READY, attrs);
        EventBus.getDefault().post(new ChangePlayerEvent(oppoPlayer));

        //隐藏提示框，显示开始按钮
        DialogHelper.getInstance(getActivity()).dismissDialogs();
    }

    /**********************************************************/


    /*************only used by invite mode**************/

    private void initInviteMode() {
        //from invited dialog
        gameName = GameUtils.getGameName(getContext(), mGameConfig.getGameFlag());
        oppoUserName = getArguments().getString(EaseConstants.OPPO_USER_NAME);

        if (TextUtils.isEmpty(oppoUserName)) {
            return;
        }
        oppoPlayer = getPlayer(getArguments().getString(EaseConstants.FROM_PLAYER), oppoUserName);

        Map attrs = new HashMap<>();
        attrs.put(EaseConstants.FROM_PLAYER, mePlayer.toString());
        GameUtils.sendCMDMessage(oppoUserName, ActionFlags.READY, attrs);

        EventBus.getDefault().postSticky(new ChangePlayerEvent(oppoPlayer));
    }

    private void showInviteDialog() {
        DialogHelper.getInstance(getActivity()).showInvitePlayerDialog(result -> {
            if (TextUtils.isEmpty(result)) {
                ToastUtils.showShort(getContext(), getString(R.string.user_name_cannot_null));
                return;
            }

            oppoUserName = result;

            Map<String, Object> attrs = new HashMap<>();
            attrs.put(EaseConstants.WHICH_GAME, mGameConfig.getGameFlag());
            attrs.put(EaseConstants.FROM_PLAYER, mePlayer.toString());
            GameUtils.sendCMDMessage(oppoUserName, ActionFlags.INVITE, attrs);

            countDownTime(ACTION_INVITE, getString(R.string.inviting), EaseConstants.TIME_OUT_INVITE, R.string.invitation_time_out);

            isIStarted = false;
            isOppoStarted = false;
            isInvitedByMe = true;
        });
    }

    private void showInvitedDialog(EMMessage replyMessage) {
        int timeout = EaseConstants.TIME_OUT_INVITE - (int) ((System.currentTimeMillis() - replyMessage.getMsgTime()) / 1000);// - EaseConstants.TIME_GAP_REQ_REP;
        if (timeout <= 0) {
            return;
        }

        String timeStr = DateUtils.getFormatTime(replyMessage.getMsgTime(), DateUtils.FORMAT_TIME);
        String msg = getString(R.string.invite_tip, timeStr, replyMessage.getFrom(), gameName);

        DialogHelper.getInstance(getActivity()).showInvitedDialog(msg, (DialogInterface dialogInterface, int i) -> {
            dispose();
            //接受
            isIStarted = false;
            isOppoStarted = false;
            isInvitedByMe = false;
            oppoUserName = replyMessage.getFrom();

            oppoPlayer = getPlayer(replyMessage.getStringAttribute(EaseConstants.FROM_PLAYER, null), oppoUserName);
            mUpvUserPanel.setOppoPlayer(oppoPlayer);

            GameUtils.sendCMDMessage(oppoUserName, ActionFlags.READY, null);
            EventBus.getDefault().post(new ChangePlayerEvent(oppoPlayer));

        }, (DialogInterface dialogInterface, int i) -> {
            dispose();
        });

        countDownTime(msg, timeout);
    }

    private void onInviteClicked() {
        if (isCountingDown()) {
            ToastUtils.showShort(getActivity(), getString(R.string.inviting_someone, oppoUserName));
            return;
        }
        if (TextUtils.isEmpty(oppoUserName)) {
            showInviteDialog();
            return;
        }
        DialogHelper.getInstance(getActivity()).showReInviteDialog((DialogInterface dialogInterface, int i) -> {
            GameUtils.sendCMDMessage(oppoUserName, ActionFlags.LEAVE, null);

            mCbvGameBoard.stopGame();
            toggleStartBtn(false);

            oppoUserName = null;
            oppoPlayer = null;
            mUpvUserPanel.setOppoPlayer(null);

            showInviteDialog();
        });
    }

    private void onInviteReceived(EMMessage message) {
        int gameFlag = message.getIntAttribute(EaseConstants.WHICH_GAME, -1);
        if (gameFlag != mGameConfig.getGameFlag()
                || mCbvGameBoard.isGaming()
                || isCountingDown()) {
            return;
        }
        showInvitedDialog(message);
    }

    /**********************************************************/

    private AppUser getPlayer(String playerInfo, String playerUserName) {
        return TextUtils.isEmpty(playerInfo) ? AppUserUtils.getUser(playerUserName) : GsonHolder.getGson().fromJson(playerInfo, AppUser.class);
    }

    /*************游戏消息处理**************/

    private EMMessageListener messageListener = new EMMessageEventAdapter() {
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            Log.e("OnlineModeFragment", "onMessageReceived: ");

            Observable.fromIterable(messages)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(message -> {
                        handleMessage(message);
                    });
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            Log.e("OnlineModeFragment", "onCmdMessageReceived: ");

            Observable.fromIterable(messages)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(message -> {
                        handleCmdMessage(message);
                    });
        }
    };

    private void handleMessage(EMMessage message) {
        if (TextUtils.isEmpty(oppoUserName)
                || !message.getFrom().equals(oppoUserName)
                || message.getChatType() != EMMessage.ChatType.Chat
                || TextUtils.isEmpty(message.getStringAttribute(EaseConstants.ATTR_GAME_CHAT, null))) {
            return;
        }
        if (!message.isAcked()) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        String msgTxt = ((EMTextMessageBody) message.getBody()).getMessage();
        onChatToast(new ToastChatEvent(false, msgTxt));

        EventBus.getDefault().post(message);
    }

    private void handleCmdMessage(EMMessage message) {
        //消息的时间是进入页面之前的
        if (message.getMsgTime() < enterTime) {
            Log.e("BaseOnlineFragment", "onCmdMessageReceived: timeout");
            return;
        }
        //消息不是来自当前的游戏对方
        if (!TextUtils.isEmpty(oppoUserName) && !message.getFrom().equals(oppoUserName)) {
            Log.e("BaseOnlineFragment", "onCmdMessageReceived: oppo user not equal");
            return;
        }

        String action = ((EMCmdMessageBody) message.getBody()).action();
        int actionFlag;
        try {
            actionFlag = Integer.parseInt(action);
        } catch (Exception exp) {
            actionFlag = -10;
        }
        Log.e("OnlineModeFragment", "run: actionFlag=" + actionFlag);
        switch (actionFlag) {
            case ActionFlags.REQ_DRAW:
                if (mCbvGameBoard.isGaming()) {
                    int timeout = TIME_OUT_REQUEST - (int) ((System.currentTimeMillis() - message.getMsgTime()) / 1000);// - EaseConstants.TIME_GAP_REQ_REP;
                    if (timeout > 0) {
                        showDrawDialog(timeout);
                    }
                }
                break;

            case ActionFlags.REQ_UNDO:
                if (mCbvGameBoard.isGaming() && mCbvGameBoard.isUndoable()) {
                    int timeout = TIME_OUT_REQUEST - (int) ((System.currentTimeMillis() - message.getMsgTime()) / 1000);// - EaseConstants.TIME_GAP_REQ_REP;
                    if (timeout > 0) {
                        showUndoDialog(timeout);
                    }
                }
                break;

            case ActionFlags.REP_DRAW:
                onDrawRepReceived(message);
                break;
            case ActionFlags.REP_UNDO:
                onUndoRepReceived(message);
                break;
            case ActionFlags.LEAVE:
                onLeaveReceived(message);
                break;

            case ActionFlags.DATA:
                if (!mCbvGameBoard.isGaming()) {
                    return;
                }
                //可能我请求悔棋，对方取消了继续下棋
                DialogHelper.getInstance(getActivity()).dismissDialogs();

                String gameData = message.getStringAttribute(EaseConstants.GAME_DATA, null);
                if (TextUtils.isEmpty(gameData)) {
                    ToastUtils.showLong(getContext(), R.string.game_data_error);
                    return;
                }
                int[] data = GsonHolder.getGson().fromJson(gameData, new TypeToken<int[]>() {
                }.getType());

                mCbvGameBoard.playChess(data);
                break;

            case ActionFlags.R_INVITE:
                onRInviteReceived(message);
                break;
            case ActionFlags.INVITE:
                onInviteReceived(message);
                break;
            case ActionFlags.ACCEPT:
                onAcceptReceived(message);
                break;
            case ActionFlags.REFUSE:
                onRefuseReceived(message);
                break;
            case ActionFlags.ADOPT:
                onAdoptReceived(message);
                break;
            case ActionFlags.READY:
                onReadyReceived(message);
                break;

            case ActionFlags.START:
                if (!isOnline) {
                    Log.e("InviteMode", "onStartReceived: ");
                    dispose();
                    DialogHelper.getInstance(getActivity()).dismissDialogs();
                }
                clickStartBtn(false);
                break;

            case ActionFlags.SURRENDER:
                if (mCbvGameBoard.isGaming()) {
                    gameOver(GameResults.OPPO_SURRENDER);
                }
                break;
        }
    }

    private void onRefuseReceived(EMMessage message) {
        if (isOnline) {
            isInAdoptStatus = false;
            sendRandomInvitation(roomId);
            return;
        }
        //invite mode
        dispose();
        DialogHelper.getInstance(getActivity()).showSingleBtnDialog(R.string.tip_title, R.string.refuse_invitation, R.string.got_it);
        oppoUserName = null;
    }

    private void onReadyReceived(EMMessage message) {
        if (isOnline) {
            if (mCbvGameBoard.isGaming()) {
                GameUtils.sendCMDMessage(message.getFrom(), ActionFlags.REFUSE, null);
                return;
            }
            //隐藏提示框，显示开始按钮
            DialogHelper.getInstance(getActivity()).dismissDialogs();
            isInvitedByMe = true;
            oppoUserName = message.getFrom();

            oppoPlayer = getPlayer(message.getStringAttribute(EaseConstants.FROM_PLAYER, null), oppoUserName);
            mUpvUserPanel.setOppoPlayer(oppoPlayer);

            ToastUtils.showShort(getContext(), getString(R.string.enter_room, oppoUserName));
            EventBus.getDefault().post(new ChangePlayerEvent(oppoPlayer));
            return;
        }
        //invite mode
        dispose();
        DialogHelper.getInstance(getActivity()).dismissDialogs();

        oppoPlayer = getPlayer(message.getStringAttribute(EaseConstants.FROM_PLAYER, null), oppoUserName);
        mUpvUserPanel.setOppoPlayer(oppoPlayer);

        EventBus.getDefault().post(new ChangePlayerEvent(oppoPlayer));
    }

    private void onLeaveReceived(EMMessage message) {
        oppoPlayer = null;
        mUpvUserPanel.setOppoPlayer(null);

        if (isOnline) {
            if (mCbvGameBoard.isGaming()) {
                DialogHelper.getInstance(getActivity()).dismissDialogs();
                mCbvGameBoard.stopGame();
                toggleStartBtn(false);
                showOppoExitDialog();
            } else if (isInAdoptStatus) {
                ToastUtils.showShort(getContext(), getString(R.string.oppo_leave_room, oppoUserName));
                changeDesk();
            }
            return;
        }
        //invite mode
        oppoUserName = null;
        mCbvGameBoard.stopGame();
        toggleStartBtn(false);
        DialogHelper.getInstance(getActivity()).dismissDialogs();//可能我请求悔棋的时候对方离开了
        DialogHelper.getInstance(getActivity()).showOppoExitDialog(null);
    }

    private void onDrawRepReceived(EMMessage message) {
        dispose();
        if (!mCbvGameBoard.isGaming()) {
            return;
        }
        DialogHelper.getInstance(getActivity()).dismissDialogs();

        if (message.getIntAttribute(EaseConstants.ATTR_CODE, -1) == EaseConstants.CODE_ACCEPT) {
            gameOver(GameResults.DRAW);
            return;
        }
        ToastUtils.showShort(getContext(), getString(R.string.oppo_not_draw));
    }

    private void onUndoRepReceived(EMMessage message) {
        dispose();
        if (!mCbvGameBoard.isGaming()) {
            return;
        }
        DialogHelper.getInstance(getActivity()).dismissDialogs();

        if (message.getIntAttribute(EaseConstants.ATTR_CODE, -1) == EaseConstants.CODE_ACCEPT) {
            mCbvGameBoard.undoChess(true);
            return;
        }
        ToastUtils.showShort(getContext(), getString(R.string.oppo_not_undo));
    }

    private void showUndoDialog(int timeout) {
        DialogHelper.getInstance(getActivity()).showOppoUndoDialog(oppoUserName, (DialogInterface dialogInterface, int i) -> {
            dispose();
            //同意悔棋
            mCbvGameBoard.undoChess(false);
        }, (DialogInterface dialogInterface, int i) -> {
            dispose();
        });
        countDownTime(getString(R.string.undo_tip), timeout);
    }

    private void showDrawDialog(int timeout) {
        DialogHelper.getInstance(getActivity()).showDrawDialog(oppoUserName, (DialogInterface dialogInterface, int i) -> {
            dispose();
            //同意和棋
            gameOver(GameResults.DRAW);
        }, (DialogInterface dialogInterface, int i) -> {
            dispose();
        });
        countDownTime(getString(R.string.draw_tip), timeout);
    }

    /**********************************************************/

    /**
     * @param message
     * @param timeout
     */
    private void countDownTime(String message, int timeout) {
        dispose();
        actionDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(aLong -> {
                    if (aLong < timeout) {
                        DialogHelper.getInstance(getActivity()).updateDoubleBtnMessage(message + (timeout - aLong));
                        return;
                    }
                    dispose();
                    DialogHelper.getInstance(getActivity()).dismissDialogs();
                });
    }


    /**
     * @param action
     * @param message
     * @param timeout
     * @param timeoutTipRes
     */
    private void countDownTime(int action, String message, int timeout, @StringRes int timeoutTipRes) {
        dispose();
        DialogHelper.getInstance(getActivity()).showOnlyTipDialog(message);
        actionDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(aLong -> {
                    if (aLong < timeout) {
                        DialogHelper.getInstance(getActivity()).updateNoBtnMessage(message + (timeout - aLong));
                        return;
                    }
                    dispose();
                    if (action == ACTION_INVITE) {
                        //invite mode
                        oppoUserName = null;
                    }
                    DialogHelper.getInstance(getActivity()).showSingleBtnDialog(R.string.tip_title, timeoutTipRes, R.string.got_it);
                });
    }

    private boolean isCountingDown() {
        return (actionDisposable != null && !actionDisposable.isDisposed());
    }

    private void dispose() {
        if (actionDisposable != null && !actionDisposable.isDisposed()) {
            actionDisposable.dispose();
        }
    }

    @Subscribe
    public void onChatToast(ToastChatEvent event) {
        Spannable spanTxt = EaseSmileUtils.getSmiledText(getContext(), event.txt);
        ToastUtils.showChatToast(getContext(), spanTxt, event.isSent, mUpvUserPanel.getHeight());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispose();
        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
        if (isOnline) {
            //退出聊天室
            EMClient.getInstance().chatroomManager().leaveChatRoom(roomId);
        }
    }

    public void onBackPressed() {
        DialogHelper.getInstance(getActivity()).showOnlineExitDialog(oppoUserName);
    }
}
