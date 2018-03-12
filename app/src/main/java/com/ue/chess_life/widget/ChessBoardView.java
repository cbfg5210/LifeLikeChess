package com.ue.chess_life.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ue.chess_life.util.GameUtils;
import com.ue.library.util.CallbackUtils;
import com.ue.resource.GameBoardDelegate;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.event.PlayListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2017/11/19.
 */

public class ChessBoardView extends SurfaceView {
    private static final String INSTANCE = "instance";
    private static final String GAME_OVER = "isGaming";
    private static final String IS_MY_TURN = "isMyTurn";

    private GameBoardDelegate mGameBoardDelegate;

    private int gameMode;
    private boolean isMyTurn;
    private boolean isGameIng;

    private SurfaceHolder mSurfaceHolder;

    public ChessBoardView(Context context) {
        this(context, null, 0);
    }

    public ChessBoardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSurfaceHolder = getHolder();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mGameBoardDelegate != null) {
            float[] boardSize = mGameBoardDelegate.getBoardSize();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) boardSize[0] + 1, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) boardSize[1] + 1, MeasureSpec.EXACTLY);
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void invalidate() {
        if (mGameBoardDelegate == null) {
            return;
        }
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        mGameBoardDelegate.drawBoard(canvas);
        mGameBoardDelegate.drawPieces(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void enterGame() {
        mGameBoardDelegate.enterGame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGameBoardDelegate == null) {
            return super.onTouchEvent(event);
        }
        if (!isGameIng) {
            return true;
        }
        if (gameMode != GameConstants.MODE_DOUBLE && !isMyTurn) {
            return true;
        }
        mGameBoardDelegate.onTouchEvent(event, isMyTurn);
        invalidate();
        return true;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        if (mGameBoardDelegate != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
            bundle.putBoolean(GAME_OVER, isGameIng);
            bundle.putBoolean(IS_MY_TURN, isMyTurn);
            return mGameBoardDelegate.onSaveInstanceState(bundle);
        }
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (mGameBoardDelegate != null && state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            isGameIng = bundle.getBoolean(GAME_OVER);
            isMyTurn = bundle.getBoolean(IS_MY_TURN);
            mGameBoardDelegate.onRestoreInstanceState(bundle);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void undoChess(boolean isMyUndo) {
        mGameBoardDelegate.undoChess(isMyUndo, isMyTurn);
    }

    public boolean isGaming() {
        return isGameIng;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public boolean isUndoable() {
        return mGameBoardDelegate.isUndoable();
    }

    public void init(int gameFlag, int gameMode, PlayListener playListener) {
        this.gameMode = gameMode;

        mGameBoardDelegate = GameUtils.getGameBoardDelegate(gameFlag);
        if (mGameBoardDelegate == null) {
            return;
        }
        mGameBoardDelegate.init(getContext(),
                gameFlag,
                gameMode,
                new PlayListener() {
                    @Override
                    public void onPlayed(boolean isIPlay, int[] data, boolean mIsMyTurn) {
                        isMyTurn = mIsMyTurn;
                        //国际象棋升兵要在这里刷新一下才行，其它不用也行
                        invalidate();
                        playListener.onPlayed(isIPlay, data, mIsMyTurn);

                        if (gameMode == GameConstants.MODE_SINGLE
                                && isGameIng
                                && !isMyTurn) {
                            aiPlay();
                        }
                    }

                    @Override
                    public void onGameOver(int resultFlag) {
                        isGameIng = false;
                        playListener.onGameOver(resultFlag);
                    }

                    @Override
                    public void onGameDataReset(boolean isIFirst, boolean mIsMyTurn) {
                        invalidate();
                        isGameIng = true;
                        isMyTurn = mIsMyTurn;
                        playListener.onGameDataReset(isIFirst, mIsMyTurn);
                    }

                    @Override
                    public void onUndo(boolean mIsMyTurn) {
                        isMyTurn = mIsMyTurn;
                        invalidate();
                        playListener.onUndo(isMyTurn);
                    }
                });

        SurfaceHolder.Callback callback = mGameBoardDelegate.getSurfaceCallback();
        mSurfaceHolder.addCallback(callback == null ? getDefaultCallback() : callback);

        requestLayout();
    }

    private void aiPlay() {
        Observable
                .create((ObservableEmitter<int[]> e) -> {
                    int[] move = mGameBoardDelegate.getAiMove();
                    if (move != null) {
                        //这里要判断context是否valid，否则会crash
                        e.onNext(move);
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((int[] ints) -> {
                    if (CallbackUtils.isContextValid(getContext())) {
                        //这里要判断context是否valid，否则会crash
                        mGameBoardDelegate.playChess(isMyTurn, ints);
                        invalidate();
                    }
                });
    }

    public void playChess(int[] data) {
        if (!isGameIng) {
            return;
        }
        mGameBoardDelegate.playChess(isMyTurn, data);
        invalidate();
    }

    public void startGame(boolean isIFirst) {
        isGameIng = true;
        isMyTurn = isIFirst;
        mGameBoardDelegate.startGame(isIFirst);
        invalidate();
    }

    public void stopGame() {
        isGameIng = false;
    }

    public void initChessBoard(boolean isIFirst) {
        mGameBoardDelegate.initChessBoard(isIFirst);
        invalidate();
    }

    public void setAiLevel(int aiLevel) {
        mGameBoardDelegate.setAiLevel(aiLevel);
    }

    public int[] getScores() {
        return mGameBoardDelegate.getScores();
    }

    private SurfaceHolder.Callback getDefaultCallback() {
        return new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                invalidate();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        };
    }
}
