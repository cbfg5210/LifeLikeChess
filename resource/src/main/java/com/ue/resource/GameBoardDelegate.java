package com.ue.resource;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.ue.resource.db.GameRecordManager;
import com.ue.resource.event.PlayListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2017/11/19.
 */
public abstract class GameBoardDelegate {
    private static final String GAME_MODE = "gameMode";
    private static final String AI_LEVEL = "aiLevel";

    public Context mContext;
    public GameRecordManager gameRecordManager;
    public PlayListener mPlayListener;
    public int mGameMode;
    public int mAiLevel;

    public float boardWidth;
    public float boardHeight;

    /*********base game board***********/

    public void init(Context context, int gameFlag, int gameMode, PlayListener playListener) {
        this.mContext = context;
        this.mGameMode = gameMode;
        this.mPlayListener = playListener;

        gameRecordManager = new GameRecordManager(context, gameFlag, gameMode);
    }

    public float[] getBoardSize() {
        return boardHeight == 0 ? new float[]{boardWidth, boardWidth} : new float[]{boardWidth, boardHeight};
    }

    public abstract void drawBoard(Canvas canvas);

    public abstract void drawPieces(Canvas canvas);

    public abstract boolean onTouchEvent(MotionEvent event, boolean isMyTurn);

    public SurfaceHolder.Callback getSurfaceCallback() {
        return null;
    }

    public Parcelable onSaveInstanceState(Bundle bundle) {
        bundle.putInt(GAME_MODE, mGameMode);
        bundle.putInt(AI_LEVEL, mAiLevel);
        return bundle;
    }

    public void onRestoreInstanceState(Bundle bundle) {
        mGameMode = bundle.getInt(GAME_MODE);
        mAiLevel = bundle.getInt(AI_LEVEL);
    }

    /*******************/


    /*********game board action*********/

    public abstract void playChess(boolean isMyTurn, int[] data);

    public abstract void enterGame();

    public abstract void startGame(boolean isIFirst);

    public abstract boolean undoOnce(boolean isMyTurn);

    public abstract int[] getAiMove();

    public abstract void initChessBoard(boolean isIFirst);

    public abstract boolean isUndoable();

    public void undoChess(boolean isMyUndo, boolean mIsMyTurn) {
        Observable
                .create((ObservableEmitter<Boolean> e) -> {
                    boolean isMyTurn = mIsMyTurn;
                    if ((isMyUndo && isMyTurn) || (!isMyUndo && !isMyTurn)) {
                        //isMyUndo和isMyTurn一致的时候才要退两步
                        isMyTurn = undoOnce(isMyTurn);
                        isMyTurn = undoOnce(isMyTurn);
                    } else {
                        isMyTurn = undoOnce(isMyTurn);
                    }
                    e.onNext(isMyTurn);
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isMyTurn -> {
                    if (mContext == null || ((Activity) mContext).isFinishing()) {
                        return;
                    }
                    notifyUndo();
                    mPlayListener.onUndo(isMyTurn);
                });
    }

    public void notifyUndo() {
    }

    public int[] getScores() {
        return null;
    }

    public void setAiLevel(int aiLevel) {
        this.mAiLevel = aiLevel;
    }
    /**********************************/
}
