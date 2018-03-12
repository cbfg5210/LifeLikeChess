package com.ue.chess_life.feature.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle2.components.support.RxFragment;
import com.ue.chess_life.R;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.entity.GameConfig;
import com.ue.chess_life.util.AppUserUtils;
import com.ue.chess_life.util.DialogHelper;
import com.ue.chess_life.widget.ChessBoardView;
import com.ue.chess_life.widget.UserPanelView;
import com.ue.library.util.CallbackUtils;
import com.ue.library.util.SPUtils;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.event.PlayListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;

/**
 * Created by hawk on 2016/12/11.
 */

public class OfflineModeFragment extends RxFragment {
    @BindView(R.id.upvUserPanel)
    UserPanelView mUpvUserPanel;
    @BindView(R.id.cbvGameBoard)
    ChessBoardView mCbvGameBoard;

    Unbinder unbinder;

    private GameConfig mGameConfig;

    private boolean isIPlayFirst;
    private String myChessColor;
    private String oppoChessColor;

    private boolean isDoubleMode;
    private int gameMode;
    private int aiLevel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        gameMode = arguments.getInt(EaseConstants.ARG_GAME_MODE, GameConstants.MODE_DOUBLE);
        isDoubleMode = gameMode == GameConstants.MODE_DOUBLE;
        mGameConfig = new GameConfig(getContext(), gameFlag);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_game, container, false);

        int menuLayoutId = isDoubleMode ? R.id.vsMenuDouble : R.id.vsMenuSingle;
        layoutView.findViewById(menuLayoutId).setVisibility(View.VISIBLE);

        unbinder = ButterKnife.bind(this, layoutView);

        mCbvGameBoard.init(mGameConfig.getGameFlag(), gameMode,
                new PlayListener() {
                    @Override
                    public void onPlayed(boolean isIPlay, int[] data, boolean isMyTurn) {
                        updatePanel();
                    }

                    @Override
                    public void onGameOver(int resultFlag) {
                        GameResultDialog.newInstance(gameMode, resultFlag)
                                .show(getChildFragmentManager(), "");
                    }

                    @Override
                    public void onUndo(boolean isMyTurn) {
                        updatePanel();
                    }

                    @Override
                    public void onGameDataReset(boolean isIFirst, boolean isMyTurn) {
                        initUserInfo(isIFirst);
                        updatePanel();
                        isIPlayFirst = isDoubleMode ? !isIFirst : true;
                    }
                });

        if (isDoubleMode) {
            mUpvUserPanel.setMePlayer(AppUserUtils.getCurrentUser());
            mUpvUserPanel.setOppoPlayer(AppUserUtils.getUser(getString(R.string.player_two)));
        } else {
            aiLevel = SPUtils.getInt(mGameConfig.getSpAILevelKey(), 0);
            mCbvGameBoard.setAiLevel(aiLevel);

            mUpvUserPanel.setMePlayer(AppUserUtils.getCurrentUser());
            String aiName = getResources().getStringArray(R.array.ai_options)[aiLevel];
            mUpvUserPanel.setRobotPlayer(AppUserUtils.getUser(aiName));
        }

        mCbvGameBoard.enterGame();

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

    private void initUserInfo(boolean isIPlayFirst) {
        if (isIPlayFirst) {
            myChessColor = mGameConfig.getFirstColor();
            oppoChessColor = mGameConfig.getLastColor();
        } else {
            myChessColor = mGameConfig.getLastColor();
            oppoChessColor = mGameConfig.getFirstColor();
        }
        mUpvUserPanel.updateMyScoreInfo(myChessColor);
        mUpvUserPanel.updateOppoScoreInfo(oppoChessColor);
    }

    private void startGame() {
        isIPlayFirst = isDoubleMode ? !isIPlayFirst : true;
        initUserInfo(isIPlayFirst);
        mCbvGameBoard.startGame(isIPlayFirst);
        updatePanel();
    }

    @Optional
    @OnClick({R.id.btnNew, R.id.btnLevel, R.id.btnExit, R.id.btnUndo})
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.btnNew:
                if (!mCbvGameBoard.isGaming()
                        || isDoubleMode
                        || mCbvGameBoard.isMyTurn()) {
                    //不是ai下棋的话才能开始新一局
                    startGame();
                }
                break;
            case R.id.btnLevel:
                if (!mCbvGameBoard.isGaming()
                        || mCbvGameBoard.isMyTurn()) {
                    //不是ai下棋的话才能切换ai等级
                    chooseAILevel();
                }
                break;
            case R.id.btnExit:
                onBackPressed();
                break;
            case R.id.btnUndo:
                if (!mCbvGameBoard.isGaming()
                        || !mCbvGameBoard.isUndoable()
                        || (!isDoubleMode && !mCbvGameBoard.isMyTurn())) {
                    return;
                }
                mCbvGameBoard.undoChess(mCbvGameBoard.isMyTurn());
                break;
        }
    }

    private void chooseAILevel() {
        AIOptionsFragment mAILevelDialog = AIOptionsFragment.newInstance(mGameConfig.getAiNum(), aiLevel);

        mAILevelDialog.setAiChangedListener((int difficulty) -> {
            SPUtils.putInt(mGameConfig.getSpAILevelKey(), difficulty);
            aiLevel = difficulty;
            mCbvGameBoard.setAiLevel(aiLevel);

            String aiName = getResources().getStringArray(R.array.ai_options)[aiLevel];
            mUpvUserPanel.setRobotPlayer(AppUserUtils.getUser(aiName));
            startGame();
        });
        CallbackUtils.showDialogFragment(this, mAILevelDialog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void onBackPressed() {
        DialogHelper.getInstance(getActivity()).showOfflineExitDialog();
    }
}