package com.ue.gobang.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.google.gson.reflect.TypeToken;
import com.ue.gobang.R;
import com.ue.gobang.ai.NegamaxPlayer;
import com.ue.gobang.model.ChessRecord;
import com.ue.gobang.util.GobangUtils;
import com.ue.gobang.util.Roles;
import com.ue.resource.GameBoardDelegate;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.constant.GameResults;
import com.ue.resource.event.PlayListener;
import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import java.util.ArrayList;

/**
 * Reference:
 * Author:
 * Date:2016/8/5.
 * 棋盘view
 */
public class GBBoardDelegate extends GameBoardDelegate {
    private Paint mPaint;
    //棋盘格子宽高
    private float mSquareWidth;

    private Bitmap background;
    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;
    private Bitmap mMovingPiece;

    private int[][] qzs;

    private boolean isMyBlack;
    private int myChess;
    private int oppoChess;

    private Point movingChessPoint;
    private boolean isMoving;//根据这个标识判断是否是滑动下子，点击不能下子

    private NegamaxPlayer aiPlayer;
    private ArrayList<Point> moves;

    private ChessRecord lastMove;

    public void init(Context context, int gameFlag, int mGameMode, PlayListener playListener) {
        super.init(context, gameFlag, mGameMode, playListener);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);//抖动处理，平滑处理
        mPaint.setStrokeWidth(2f);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        boardWidth = displayMetrics.widthPixels * 0.95f;
        mSquareWidth = boardWidth / Roles.LEN;

        int pieceWidth = (int) (mSquareWidth * 0.85f);
        background = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg_game);
        background = Bitmap.createScaledBitmap(background, (int) boardWidth + 1, (int) boardWidth + 1, false);
        mBlackPiece = BitmapFactory.decodeResource(context.getResources(), R.mipmap.gb_black);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
        mWhitePiece = BitmapFactory.decodeResource(context.getResources(), R.mipmap.gb_white);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mMovingPiece = BitmapFactory.decodeResource(context.getResources(), R.mipmap.gb_moving);
        mMovingPiece = Bitmap.createScaledBitmap(mMovingPiece, pieceWidth, pieceWidth, false);
    }

    @Override
    public int[] getAiMove() {
        Point aiPoint = aiPlayer.getMove(moves, oppoChess);
        return new int[]{aiPoint.x, aiPoint.y};
    }

    public boolean isUndoable() {
        return lastMove != null;
    }

    public void drawPieces(Canvas canvas) {
        if (qzs == null) {
            return;
        }
        if (movingChessPoint != null) {
            canvas.drawBitmap(mMovingPiece, (movingChessPoint.x + 0.125f) * mSquareWidth, (movingChessPoint.y + 0.125f) * mSquareWidth, null);
        }
        for (int i = 0; i < Roles.LEN; i++) {
            for (int j = 0; j < Roles.LEN; j++) {
                if (qzs[i][j] != Roles.EMPTY) {
                    drawPiece(canvas, i, j);
                }
            }
        }
        //最后下的一只棋要特殊标注
        if (lastMove != null) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.BLUE);
            canvas.drawCircle((lastMove.x + 0.5f) * mSquareWidth, (lastMove.y + 0.5f) * mSquareWidth, 3 * mSquareWidth / 16, mPaint);
        }
    }

    private void drawPiece(Canvas canvas, int x, int y) {
        canvas.drawBitmap(qzs[x][y] == Roles.BLACK ? mBlackPiece : mWhitePiece, (x + 0.08f) * mSquareWidth, (y + 0.08f) * mSquareWidth, null);
    }

    public boolean undoOnce(boolean isMyTurn) {
        if (lastMove == null) {
            return isMyTurn;
        }
        moves.remove(moves.size() - 1);

        qzs[lastMove.x][lastMove.y] = Roles.EMPTY;
        Object lastRecord = gameRecordManager.delToGetLastRecord(lastMove);
        lastMove = lastRecord == null ? null : (ChessRecord) lastRecord;

        return !isMyTurn;
    }

    public void drawBoard(Canvas canvas) {
        canvas.drawBitmap(background, 0, 0, null);

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);//描边

        float halfSquareWidth = mSquareWidth / 2;
        float endWidth = boardWidth - halfSquareWidth;
        float tempFloat;
        for (int i = 0; i < Roles.LEN; i++) {
            tempFloat = (i + 0.5f) * mSquareWidth;
            //横线
            canvas.drawLine(halfSquareWidth, tempFloat, endWidth, tempFloat, mPaint);
            //纵线
            canvas.drawLine(tempFloat, halfSquareWidth, tempFloat, endWidth, mPaint);
        }
        //中间和四周的小黑点
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        canvas.drawCircle(7.5f * mSquareWidth, 7.5f * mSquareWidth, 6, mPaint);//中心点
        canvas.drawCircle(3.5f * mSquareWidth, 3.5f * mSquareWidth, 6, mPaint);//上左
        canvas.drawCircle(11.5f * mSquareWidth, 3.5f * mSquareWidth, 6, mPaint);//上右
        canvas.drawCircle(3.5f * mSquareWidth, 11.5f * mSquareWidth, 6, mPaint);//下左
        canvas.drawCircle(11.5f * mSquareWidth, 11.5f * mSquareWidth, 6, mPaint);//下右
    }

    //落子方式：滑动落子
    @Override
    public boolean onTouchEvent(MotionEvent event, boolean isMyTurn) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isMoving = false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isMoving) {
                isMoving = false;
                int x = movingChessPoint.x, y = movingChessPoint.y;
                if (x < 0 || x > 14 || y < 0 || y > 14) {
                    //out of bound
                    movingChessPoint = null;
                    return true;
                }
                movingChessPoint = null;
                if (qzs[x][y] != Roles.EMPTY) {
                    return true;
                }
                playChess(isMyTurn, new int[]{x, y});
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            isMoving = true;
            //显示移动中的棋子的图片
            int pointX = (int) (event.getX() / mSquareWidth);
            int pointY = (int) (event.getY() / mSquareWidth) - 3;

            pointX = pointX < 0 ? 0 : (pointX < Roles.LEN ? pointX : Roles.LEN - 1);
            pointY = pointY < 0 ? 0 : (pointY < Roles.LEN ? pointY : Roles.LEN - 1);

            movingChessPoint = new Point(pointX, pointY);
        }
        return true;
    }

    public void playChess(boolean isMyTurn, int[] data) {
        int x = data[0], y = data[1];

        qzs[x][y] = isMyTurn ? myChess : oppoChess;
        saveRecord(x, y, isMyTurn);

        int flag = GobangUtils.terminal(qzs, moves);
        if (flag != Roles.ING) {
            //terminal
            gameRecordManager.clearGameRecords();
            mPlayListener.onGameOver(flag == Roles.ERROR ? GameResults.DRAW : (flag == myChess ? GameResults.I_WON : GameResults.OPPO_WON));
        }
        mPlayListener.onPlayed(isMyTurn, data, !isMyTurn);
    }

    @Override
    public void enterGame() {
        gameRecordManager.getChessRecords(ChessRecord.class)
                .subscribe(gameRecords -> {
                    if (mContext == null || ((Activity) mContext).isFinishing()) {
                        return;
                    }
                    boolean noHisRecords = gameRecords.size() == 0;

                    boolean isIRunFirst = noHisRecords ? true : (((ChessRecord) gameRecords.get(0)).isMyTurn);
                    boolean isMyTurn = isIRunFirst;

                    initChessBoard(isIRunFirst);
                    initColor(isIRunFirst);
                    if (moves == null) {
                        moves = new ArrayList<>();
                    }

                    if (!noHisRecords) {
                        ChessRecord move;
                        for (IChessRecord chessRecord : gameRecords) {
                            move = (ChessRecord) chessRecord;
                            qzs[move.x][move.y] = move.isMyTurn ? myChess : oppoChess;
                            moves.add(new Point(move.x, move.y));
                            isMyTurn = !isMyTurn;
                        }
                        lastMove = (ChessRecord) gameRecords.get(gameRecords.size() - 1);
                    }

                    mPlayListener.onGameDataReset(isIRunFirst, isMyTurn);
                    if (mGameMode == GameConstants.MODE_SINGLE) {
                        aiPlayer = new NegamaxPlayer(mAiLevel < 3 ? mAiLevel + 1 : mAiLevel + 2);
                        mPlayListener.onPlayed(!isMyTurn, null, isMyTurn);
                    }
                });
    }

    private void saveRecord(int x, int y, boolean isMyTurn) {
        moves.add(new Point(x, y));
        lastMove = new ChessRecord(x, y, isMyTurn);
        gameRecordManager.saveChessRecord(lastMove);
    }

    public void initChessBoard(boolean isIFirst) {
        if (qzs == null) {
            qzs = new int[Roles.LEN][Roles.LEN];
        }
        for (int i = 0; i < Roles.LEN; i++) {
            for (int j = 0; j < Roles.LEN; j++) {
                qzs[i][j] = Roles.EMPTY;
            }
        }
        if (moves != null) {
            moves.clear();
        }
        lastMove = null;
    }

    private void initColor(boolean isMyBlack) {
        this.isMyBlack = isMyBlack;
        if (isMyBlack) {
            myChess = Roles.BLACK;
            oppoChess = Roles.WHITE;
        } else {
            myChess = Roles.WHITE;
            oppoChess = Roles.BLACK;
        }
    }

    public void startGame(boolean isIRunFirst) {
        gameRecordManager.clearGameRecords();
        initChessBoard(isIRunFirst);
        initColor(isIRunFirst);
        if (moves == null) {
            moves = new ArrayList<>();
        }
        if (mGameMode == GameConstants.MODE_SINGLE) {
            aiPlayer = new NegamaxPlayer(mAiLevel < 3 ? mAiLevel + 1 : mAiLevel + 2);
        }
    }

    private static final String IS_MY_BLACK = "isMyBlack";
    private static final String MOVES = "moves";
    private static final String BOARD = "board";
    private static final String LAST_MOVE = "lastMove";

    @Override
    public Parcelable onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putBoolean(IS_MY_BLACK, isMyBlack);

        if (moves != null) {
            bundle.putParcelableArrayList(MOVES, moves);
        }
        if (qzs != null) {
            bundle.putString(BOARD, GsonHolder.getGson().toJson(qzs));
        }
        if (lastMove != null) {
            bundle.putString(LAST_MOVE, lastMove.toString());
        }

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        isMyBlack = bundle.getBoolean(IS_MY_BLACK);
        initColor(isMyBlack);

        if (bundle.containsKey(MOVES)) {
            moves = bundle.getParcelableArrayList(MOVES);
        }
        if (bundle.containsKey(BOARD)) {
            qzs = GsonHolder.getGson().fromJson(bundle.getString(BOARD), new TypeToken<int[][]>() {
            }.getType());
        }
        if (bundle.containsKey(LAST_MOVE)) {
            lastMove = GsonHolder.getGson().fromJson(bundle.getString(LAST_MOVE), ChessRecord.class);
        }
    }
}