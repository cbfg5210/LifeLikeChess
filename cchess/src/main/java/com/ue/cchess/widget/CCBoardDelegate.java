package com.ue.cchess.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.google.gson.reflect.TypeToken;
import com.ue.cchess.R;
import com.ue.cchess.ai.AlphaBetaEngine;
import com.ue.cchess.entity.ChessPoint;
import com.ue.cchess.entity.ChessRecord;
import com.ue.cchess.entity.MoveGenerator;
import com.ue.cchess.util.ChessUtils;
import com.ue.resource.GameBoardDelegate;
import com.ue.resource.constant.GameConstants;
import com.ue.resource.constant.GameResults;
import com.ue.resource.event.PlayListener;
import com.ue.resource.model.IChessRecord;
import com.ue.resource.util.GsonHolder;

import static com.ue.cchess.entity.ChessID.B_GENERAL;
import static com.ue.cchess.entity.ChessID.EMPTY;
import static com.ue.cchess.entity.ChessID.R_GENERAL;

public class CCBoardDelegate extends GameBoardDelegate {
    private Paint paint;
    private float mLineHeight;
    private float pieceSize;
    private Bitmap backgroundBitmap;

    //棋盘现有棋子的位子记录
    public int[][] qzs;
    private ChessRecord lastMove;
    private ChessPoint selectedChess;

    private boolean isIRunFirst;
    private MoveGenerator mMoveGenerator;

    private AlphaBetaEngine mAlphaBetaEngine;
    private boolean isOnlineMode;

    @Override
    public void init(Context context, int gameFlag, int gameMode, PlayListener playListener) {
        super.init(context, gameFlag, gameMode, playListener);

        paint = new Paint();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        boardWidth = displayMetrics.widthPixels * 0.95f;

        mLineHeight = boardWidth / 9;
        int addLineHeight = (int) (mLineHeight + 1);

        boardHeight = boardWidth + addLineHeight;

        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg_game);
        backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap, (int) boardWidth + 1, (int) boardHeight + 1, false);

        mMoveGenerator = new MoveGenerator();
        isOnlineMode = (mGameMode == GameConstants.MODE_INVITE || mGameMode == GameConstants.MODE_ONLINE);
    }

    @Override
    public int[] getAiMove() {
        mAlphaBetaEngine.searchBestMove(qzs);
        ChessRecord move = mAlphaBetaEngine.getBestMove();
        return new int[]{move.fromX, move.fromY, move.toX, move.toY};
    }

    //绘制棋盘
    public void drawBoard(Canvas canvas) {
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);//设置去锯齿
        paint.setStrokeWidth(2);//线条宽度
        //横线
        for (int i = 0, start = (int) (mLineHeight / 2), end = ((int) boardWidth + 1 - start); i < 10; i++) {
            float y = (0.5f + i) * mLineHeight;
            canvas.drawLine(start, y, end, y, paint);
        }
        //上-纵线
        for (int i = 1, j = 8, start = (int) (mLineHeight / 2), end = (int) (4.5f * mLineHeight); i < j; i++) {
            int x = (int) ((0.5f + i) * mLineHeight);
            canvas.drawLine(x, start, x, end, paint);
        }
        //下-纵线
        for (int i = 1, j = 8, start = (int) (5.5f * mLineHeight), end = (int) (boardHeight - mLineHeight / 2); i < j; i++) {
            float x = (0.5f + i) * mLineHeight;
            canvas.drawLine(x, start, x, end, paint);
        }
        int x = (int) (mLineHeight / 2);
        int y = (int) (boardHeight - mLineHeight / 2);
        canvas.drawLine(x, x, x, y, paint);//第一条纵线
        x = (int) (boardWidth - mLineHeight / 2);
        canvas.drawLine(x, mLineHeight / 2, x, y, paint);//最后一条纵线

        //交叉线
        int startX = (int) (3.5f * mLineHeight);
        int startY = (int) (mLineHeight / 2);
        int stopX = (int) (5.5f * mLineHeight);
        int stopY = (int) (2.5f * mLineHeight);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        canvas.drawLine(startX, stopY, stopX, startY, paint);

        startY = (int) (7.5f * mLineHeight);
        stopY = (int) (9.5f * mLineHeight);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        canvas.drawLine(startX, stopY, stopX, startY, paint);

        //绘制折线
        drawItemKH(canvas, 1, 2);
        drawItemKH(canvas, 7, 2);
        drawItemKH(canvas, 1, 7);
        drawItemKH(canvas, 7, 7);
        for (int i = 0, j = 0; i < 5; i++, j += 2) {
            drawItemKH(canvas, j, 3);
            drawItemKH(canvas, j, 6);
        }

        paint.setTextSize(mLineHeight / 2);
        float centy = 5.2f * mLineHeight;
        float centx = 2.5f * mLineHeight;
        float centx2 = 5.5f * mLineHeight;
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("楚河", centx, centy, paint); // 画出文字
        canvas.drawText("汉界", centx2, centy, paint); // 画出文字
    }

    // 绘制折线
    private void drawItemKH(Canvas c, int pointX, int pointY) {
        // 获取中心坐标
        float x = (pointX + 0.5f) * mLineHeight;
        float y = (pointY + 0.5f) * mLineHeight;
        // 线长
        float len = mLineHeight / 4;
        // 距离中心
        float dc = mLineHeight / 10;

        // left，如果x是0的话，不绘制左侧
        if (pointX != 0) {
            c.drawLine(x - dc, y + dc, x - dc - len, y + dc, paint);
            c.drawLine(x - dc, y - dc, x - dc - len, y - dc, paint);
            c.drawLine(x - dc, y + dc, x - dc, y + dc + len, paint);
            c.drawLine(x - dc, y - dc, x - dc, y - dc - len, paint);
        }
        // right
        if (pointX != 8) {
            c.drawLine(x + dc, y + dc, x + dc + len, y + dc, paint);
            c.drawLine(x + dc, y - dc, x + dc + len, y - dc, paint);
            c.drawLine(x + dc, y + dc, x + dc, y + dc + len, paint);
            c.drawLine(x + dc, y - dc, x + dc, y - dc - len, paint);
        }
    }

    //绘制棋子
    public void drawPieces(Canvas canvas) {
        if (qzs == null) {
            return;
        }
        pieceSize = 0.4f * mLineHeight;
        paint.setTextSize(mLineHeight / 2);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (qzs[i][j] != EMPTY) {
                    drawPiece(canvas, i, j);
                }
            }
        }
        if (selectedChess != null) {
            paint.setColor(Color.parseColor("#66cc99"));
            canvas.drawCircle((selectedChess.x + 0.5f) * mLineHeight, (selectedChess.y + 0.5f) * mLineHeight, pieceSize, paint);
            //绘制棋子上面的字
            paint.setColor(Color.WHITE);
            canvas.drawText(ChessUtils.getChessName(selectedChess.f), (selectedChess.x + 0.25f) * mLineHeight, (selectedChess.y + 0.7f) * mLineHeight, paint);
        }
        if (lastMove != null) {
            paint.setStyle(Paint.Style.STROKE);// 变成空心的圆

            paint.setColor(Color.GREEN);
            canvas.drawCircle((0.5f + lastMove.fromX) * mLineHeight, (0.5f + lastMove.fromY) * mLineHeight, pieceSize, paint);

            paint.setColor(Color.BLUE);
            canvas.drawCircle((0.5f + lastMove.toX) * mLineHeight, (0.5f + lastMove.toY) * mLineHeight, pieceSize, paint);
        }
    }

    //画棋子
    private void drawPiece(Canvas canvas, int x, int y) {
        int tempInt = Math.abs(qzs[x][y]);
        if (tempInt == EMPTY) {
            if (selectedChess == null) {
                return;
            }
            tempInt = selectedChess.f;
        }
        if (qzs[x][y] < 0) {
            paint.setColor(Color.parseColor("#bababa"));
            canvas.drawCircle((x + 0.5f) * mLineHeight, (y + 0.5f) * mLineHeight, pieceSize, paint);
            //绘制棋子上面的字
            paint.setColor(Color.WHITE);
            canvas.drawText(ChessUtils.getChessName(tempInt), (x + 0.25f) * mLineHeight, (y + 0.7f) * mLineHeight, paint);
            return;
        }
        //偏移量：1/2*mLineHeight-ratioPieceOfLineHeight*mLineHight/2=((1-ratioPieceOfLineHeight)/2)*mLineHeight;(0,0)距离panel最左边的距离
        if (tempInt > 7) {
            paint.setColor(Color.BLACK);
            canvas.drawCircle((x + 0.5f) * mLineHeight, (y + 0.5f) * mLineHeight, pieceSize, paint);
            //绘制棋子上面的字
            paint.setColor(Color.WHITE);
            canvas.drawText(ChessUtils.getChessName(tempInt), (x + 0.25f) * mLineHeight, (y + 0.7f) * mLineHeight, paint);
            return;
        } else {
            paint.setColor(Color.parseColor("#ffff4444"));
            canvas.drawCircle((x + 0.5f) * mLineHeight, (y + 0.5f) * mLineHeight, pieceSize, paint);
            //绘制棋子上面的字
            paint.setColor(Color.WHITE);
            canvas.drawText(ChessUtils.getChessName(tempInt), (x + 0.25f) * mLineHeight, (y + 0.7f) * mLineHeight, paint);
        }
    }

    //这里面的可以进行方法的提取
    @Override
    public boolean onTouchEvent(MotionEvent event, boolean isMyTurn) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int i = (int) (event.getX() / mLineHeight);
            int j = (int) (event.getY() / mLineHeight);

            //越界
            if (i > 8 || j > 9) {
                return true;
            }

            playChess(isMyTurn, new int[]{i, j});
        }
        return true;
    }

    //online/single
    private int[] translateData(int[] data) {
        int fromX = data[0], fromY = data[1], toX = data[2], toY = data[3];
        //在线模式的话需要对数据进行一下转换,左右对称上下对称
        if (isOnlineMode) {
            fromX = 8 - fromX;
            fromY = 9 - fromY;//6->3,5->4
            toX = 8 - toX;
            toY = 9 - toY;
        }
        selectedChess = new ChessPoint(qzs[fromX][fromY], fromX, fromY);
        qzs[toX][toY] = -qzs[toX][toY];
        return new int[]{toX, toY};
    }

    private void resetStatus() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (qzs[i][j] < 0) {
                    qzs[i][j] = -qzs[i][j];
                }
            }
        }
    }

    public void notifyUndo() {
        selectedChess = null;
        resetStatus();
    }

    public boolean undoOnce(boolean isMyTurn) {
        if (lastMove == null) {
            return isMyTurn;
        }
        qzs[lastMove.fromX][lastMove.fromY] = lastMove.fromF;
        qzs[lastMove.toX][lastMove.toY] = lastMove.toF < 0 ? -lastMove.toF : lastMove.toF;

        Object lastRecord = gameRecordManager.delToGetLastRecord(lastMove);
        lastMove = lastRecord == null ? null : (ChessRecord) lastRecord;

        return !isMyTurn;
    }

    @Override
    public void playChess(boolean isMyTurn, int[] oData) {
        int[] data = (isMyTurn || mGameMode == GameConstants.MODE_DOUBLE) ? oData : translateData(oData);
        int toX = data[0];
        int toY = data[1];

        if (qzs[toX][toY] == EMPTY) {
            //当前位置不可下,要<0才行
            selectedChess = null;
            resetStatus();
            return;
        }
        if (selectedChess == null) {
            //还没选中棋子，要先选中棋子
            if ((isMyTurn && !ChessUtils.isMyChess(qzs[toX][toY], isIRunFirst)) ||
                    (!isMyTurn && ChessUtils.isMyChess(qzs[toX][toY], isIRunFirst))) {
                //玩家和对应的棋不一致的话返回
                return;
            }
            selectedChess = new ChessPoint(qzs[toX][toY], toX, toY);
            mMoveGenerator.showValidMoves(qzs, selectedChess.x, selectedChess.y);
            return;
        }
        if (qzs[toX][toY] > 0) {
            //如果是己方棋子则设为选中，否则取消选中
            resetStatus();
            if (ChessUtils.isSameSide(selectedChess.f, qzs[toX][toY])) {
                selectedChess = new ChessPoint(qzs[toX][toY], toX, toY);
                mMoveGenerator.showValidMoves(qzs, selectedChess.x, selectedChess.y);
            } else {
                selectedChess = null;
            }
            return;
        }
        //qzs[toX][toY] <= 0，可以移动到目标位置
        //save record
        saveRecord(selectedChess.x, selectedChess.y, toX, toY);
        //is game over
        if (qzs[toX][toY] < 0) {
            qzs[toX][toY] = -qzs[toX][toY];
        }
        boolean isGameIng = (qzs[toX][toY] != R_GENERAL && qzs[toX][toY] != B_GENERAL);
        //move
        int fromX = selectedChess.x;
        int fromY = selectedChess.y;
        qzs[toX][toY] = qzs[fromX][fromY];
        qzs[fromX][fromY] = EMPTY;
        selectedChess = null;
        resetStatus();
        //on play

        if (!isGameIng) {
            gameRecordManager.clearGameRecords();
            //terminal qzs[toX][toY]是新移动的，如果该棋是我方的则我方胜
            mPlayListener.onGameOver(ChessUtils.isMyChess(qzs[toX][toY], isIRunFirst) ? GameResults.I_WON : GameResults.OPPO_WON);
        }

        mPlayListener.onPlayed(isMyTurn, new int[]{fromX, fromY, toX, toY}, !isMyTurn);
    }

    @Override
    public void enterGame() {
        gameRecordManager.getChessRecords(ChessRecord.class)
                .subscribe(gameRecords -> {
                    if (mContext == null || ((Activity) mContext).isFinishing()) {
                        return;
                    }
                    boolean noHisRecords = gameRecords.size() == 0;
                    boolean isMyTurn = noHisRecords ? true : (((ChessRecord) gameRecords.get(0)).fromY > 4);

                    initChessBoard(isMyTurn);

                    if (!noHisRecords) {
                        ChessRecord move;
                        for (IChessRecord chessRecord : gameRecords) {
                            move = (ChessRecord) chessRecord;
                            qzs[move.toX][move.toY] = move.fromF;
                            qzs[move.fromX][move.fromY] = EMPTY;
                            isMyTurn = !isMyTurn;
                        }
                        lastMove = (ChessRecord) gameRecords.get(gameRecords.size() - 1);
                    }

                    mPlayListener.onGameDataReset(isIRunFirst, isMyTurn);
                    if (mGameMode == GameConstants.MODE_SINGLE) {
                        mAlphaBetaEngine = new AlphaBetaEngine(mAiLevel + 1);//搜索算法
                        mPlayListener.onPlayed(!isMyTurn, null, isMyTurn);
                    }
                });
    }

    private void saveRecord(int fromX, int fromY, int toX, int toY) {
        lastMove = new ChessRecord();
        lastMove.setFXY(true, qzs[fromX][fromY], fromX, fromY);
        lastMove.setFXY(false, qzs[toX][toY], toX, toY);

        gameRecordManager.saveChessRecord(lastMove);
    }

    @Override
    public void startGame(boolean isIRunFirst) {
        gameRecordManager.clearGameRecords();
        initChessBoard(isIRunFirst);

        if (mGameMode == GameConstants.MODE_SINGLE) {
            mAlphaBetaEngine = new AlphaBetaEngine(mAiLevel + 1);//搜索算法
        }
    }

    public boolean isUndoable() {
        return lastMove != null;
    }

    @Override
    public void initChessBoard(boolean isIFirst) {
        this.isIRunFirst = isIFirst;
        qzs = ChessUtils.getChessBoard(isIFirst);

        lastMove = null;
        selectedChess = null;
        mMoveGenerator.setMyFirst(isIRunFirst);
    }

    private static final String LAST_MOVE = "lastMove";
    private static final String CHESS_ARRAY = "qzs";

    @Override
    public Parcelable onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        if (lastMove != null) {
            bundle.putString(LAST_MOVE, lastMove.toString());
        }
        bundle.putString(CHESS_ARRAY, GsonHolder.getGson().toJson(qzs));
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        qzs = GsonHolder.getGson().fromJson(bundle.getString(CHESS_ARRAY), new TypeToken<int[][]>() {
        }.getType());

        if (bundle.containsKey(LAST_MOVE)) {
            String lastChessMoveJson = bundle.getString(LAST_MOVE);
            lastMove = GsonHolder.getGson().fromJson(lastChessMoveJson, ChessRecord.class);
        }
    }
}